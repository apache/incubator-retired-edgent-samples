/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package quarks.samples.apps;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;

/**
 * A range of values and a way to check a value for containment in the range.
 * <p>
 * Useful in filtering in predicates.  This is a lightweight implementation
 * of a subset of the Guava Range API.
 * <p> 
 * e.g.
 * <pre>{@code
 * Range.open(2,4).contains(2);      // returns false
 * Range.closed(2,4).contains(2);    // returns true
 * Range.atLeast(2).contains(2);     // returns true
 * Range.greaterThan(2).contains(2); // returns false
 * Range.atMost(2).contains(2);      // returns true
 * Range.lessThan(2).contains(2);    // returns false
 * 
 * String s = Range.closed(2,4).toString();
 * Range<Integer> range = Range.valueOf(s, Integer.class);
 * 
 * TStream<Integer> intStream = ...;
 * TStream<Integer> filtered = intStream.filter(tuple -> !range.contains(tuple));
 * 
 * TStream<JsonObject> jStream = ...;
 * TStream<JsonObject> filtered = jStream.filter(json -> !range.contains(json.getProperty("reading").asInteger());
 * }</pre>
 * 
 * <p>
 * Compared to Guava Range:
 * <ul>
 * <li>Guava Range doesn't mention any constraints with respect to types for {@code <T>}.
 *     This Range currently supports: Integer,Long,Short,Byte,Float,Double,BigInteger,BigDecimal.
 *     <br>String and Character, of questionable value to Quarks apps,
 *     are avoided at this time due to the Guava toString()/from-string
 *     items noted below.</li>
 * <li>Guava Range doesn't support unsigned type ranges.
 *     <br>This Range adds {@link #contains(Comparable, Comparator)}.
 * <li>Guava Range lacks a "Range from Range.toString()" function: https://github.com/google/guava/issues/1911.
 *     <br>This Range adds {@link #valueOf(String, Class) valueOf}.
 *     Possibly consider: (a) migrating this to a new Ranges class,
 *     (b) introducing convenience forms - e.g., {@code valueOfInteger(String)}.</li>
 * <li>Guava Range has issues with to/from Json with Gson: 
 *     https://github.com/google/guava/issues/1911.
 *     <br>This Range works but as doc'd by Gson, use {@code Gson#fromJson(String, java.lang.reflect.Type)} Don't know about this Range :-)</li>
 * <li>Guava Range's {@code apply(T value)} is documented as deprecated 
 *     so this Range does not "implement Predicate". 
 * <li>Guava Range.toString()
 *     <ul>
 *     <li> Guava uses some unprintable characters.
 *          Up to the latest Guava release - 19.0, Range.toString() uses \u2025 for
 *          the ".." separator and uses +/-\u221E for infinity.  That's caused problems:
 *          https://github.com/google/guava/issues/2376.
 *          Guava Range.toString() has been change to use ".." instead of \u2025.
 *          It still uses the unicode char for infinity.
 *          <br>This Range uses ".." for the separator like the not-yet-released Guava change.
 *          For convenience to users, this Range uses "*" and no leading +/- for infinity.</li>
 *      <li>Guava does not decorate String or Character values with \" or \' respectively.
 *          It does not generate an escaped encoding of the
 *          range separator if it is present in a value.
 *          Hard to guess whether this may change if/when Guava adds a
 *          "Range from Range.toString()" capability.
 *          <br>To avoid arbitrary deviations for Range types that may not be
 *          particularly interesting to Quarks users, at this time 
 *          this Range does not support String or Character.
 *          </li>
 *      </ul>
 *      </li>
 * </ul>
 *
 * @param <T> value type  N.B. at this time {@code T} must be one of:
 *        Integer,Long,Short,Byte,Float,Double,BigInteger,BigDecimal
 * <p>
 *        An IllegalArgumentException is thrown if an unsupported type
 *        is specified when constructing the Range.
 */
public class Range<T extends Comparable<?>> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final T lowerBound;  // null for infinity
    private final T upperBound;  // null for infinity
    private final BoundType lbt;
    private final BoundType ubt;
    private transient int hashCode;
    private static final Class<?> supportedTypes[] = {
            Integer.class, Short.class, Byte.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class
    };
    
    private enum BoundType {/** exclusive */ OPEN, /** inclusive */ CLOSED};
    
    private Range(T lowerBound, BoundType lbt, T upperBound, BoundType ubt) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lbt = lbt;
        this.ubt = ubt;
        checkSupportedType(lowerBound);
        checkSupportedType(upperBound);
        if (lowerBound != null && upperBound != null) {
            if (lowerBound.getClass() != upperBound.getClass())
                throw new IllegalArgumentException("lowerBound and upperBound are not the same type");
        }
    }
    
    private void checkSupportedType(Object obj) {
        if (obj==null) return;
        Class<?> objClass = obj.getClass();
        for (Class<?> c : supportedTypes) {
            if (c == objClass)
                return;
        }
        throw new IllegalArgumentException("unsupported type: "+objClass);
    }
    
    // TODO defer making these public due to BoundType
    private static <T extends Comparable<?>> Range<T> range(T lowerBound, BoundType b1, T upperBound, BoundType b2) {  return new Range<T>(lowerBound, b1, upperBound, b2); }
//    public static <T> Range<T> downTo(T v, BoundType b) { return range(v, b, null, null); }
//    public static <T> Range<T> upTo(T v, BoundType b) { return range(null, null, v, b); }

    /** (a..b) (both exclusive) */
    public static <T extends Comparable<?>> Range<T> open(T lowerBound, T upperBound) { 
        return range(lowerBound, BoundType.OPEN, upperBound, BoundType.OPEN);
    }
    /** [a..b] (both inclusive) */
    public static <T extends Comparable<?>> Range<T> closed(T lowerBound, T upperBound) {
        return range(lowerBound, BoundType.CLOSED, upperBound, BoundType.CLOSED); 
    }
    /** (a..b] (exclusive,inclusive) */
    public static <T extends Comparable<?>> Range<T> openClosed(T lowerBound, T upperBound) {
        return range(lowerBound, BoundType.OPEN, upperBound, BoundType.CLOSED);
    }
    /** [a..b) (inclusive,exclusive)*/
    public static <T extends Comparable<?>> Range<T> closedOpen(T lowerBound, T upperBound) {
        return range(lowerBound, BoundType.CLOSED, upperBound, BoundType.OPEN);
    }
    /** (a..*) (exclusive) */
    public static <T extends Comparable<?>> Range<T> greaterThan(T v) {
        return range(v, BoundType.OPEN, null, BoundType.OPEN);
    }
    /** [a..*) (inclusive) */
    public static <T extends Comparable<?>> Range<T> atLeast(T v) {
        return range(v, BoundType.CLOSED, null, BoundType.OPEN);
    }
    /** (*..b) (exclusive) */
    public static <T extends Comparable<?>> Range<T> lessThan(T v) {
        return range(null, BoundType.OPEN, v, BoundType.OPEN);
    }
    /** (*..b] (inclusive) */
    public static <T extends Comparable<?>> Range<T> atMost(T v) {
        return range(null, BoundType.OPEN, v, BoundType.CLOSED);
    }
    /** [v..v] (both inclusive) */
    public static  <T extends Comparable<?>> Range<T> singleton(T v) {
        return range(v, BoundType.CLOSED, v, BoundType.CLOSED);
    }
    
    /**
     * Returns true if o is a range having the same endpoints and bound types as this range.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o instanceof Range) {
            Range<?> r = (Range<?>) o;
            return r.lbt.equals(lbt)
                   && r.ubt.equals(ubt)
                   && (r.lowerBound==null ? r.lowerBound == lowerBound
                                          : r.lowerBound.equals(lowerBound))
                   && (r.upperBound==null ? r.upperBound == upperBound
                                          : r.upperBound.equals(upperBound));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0)
            hashCode = Objects.hash(lbt, lowerBound, ubt, upperBound);
        return hashCode;
    }

    // Avoid making the Guava {lower,upper}Endpoint() methods public for now.
    // It's not clear they have value in the absence of {lower,upper}BoundType()
    // and at this time we're avoiding exposing our BoundType
    
//    /**
//     * @return true iff the Range's lower endpoint isn't unbounded.
//     */
//    public boolean hasLowerEndpoint() {
//        return lowerBound != null;
//    }
//    
//    /**
//     * Get the range's lower endpoint / bound.
//     * @return the endpoint.
//     * @throws IllegalStateException if hasLowerEndpoint()==false
//     */
//    public T lowerEndpoint() {
//        if (hasLowerEndpoint())
//            return lowerBound;
//        throw new IllegalStateException("unbounded");
//    }
//    
//    /**
//     * @return true iff the Range's upper endpoint isn't unbounded.
//     */
//    public boolean hasUpperEndpoint() {
//        return upperBound != null;
//    }
//    
//    /**
//     * Get the range's upper endpoint / bound.
//     * @return the endpoint.
//     * @throws IllegalStateException if hasUpperEndpoint()==false
//     */
//    public T upperEndpoint() {
//        if (hasUpperEndpoint())
//            return upperBound;
//        throw new IllegalStateException("unbounded");
//    }
    
    /**
     * Determine if the Region contains the value.
     * <p>
     * {@code contains(v)} typically suffices.  This
     * is useful in the case where the Comparable's default
     * {@code Comparable.compareTo()} isn't sufficient.
     * e.g., for unsigned byte comparisons
     * <pre>
     * Comparator<Byte> unsignedByteComparator = new Comparator<Byte>() {
     *     public int compare(Byte b1, Byte b2) {
     *         return Integer.compareUnsigned(b1.toUnsignedInt(), b2.toUnsignedInt());
     *     }
     *     public boolean equals(Object o2) { return o2==this; }
     *     };
     * Range<Byte> unsignedByteRange = ...;
     * unsignedByteRange.contains(value, unsignedByteComparator);
     * </pre>
     * <p>
     * N.B. Guava Range lacks such a method.
     * <p>
     * @param v the value to check for containment
     * @param cmp the Comparator to use
     * @return true if the Region contains the value
     */
    public boolean contains(T v, Comparator<T> cmp) {
        if (lowerBound==null) {
            int r = cmp.compare(v, upperBound);
            return ubt == BoundType.OPEN ? r < 0 : r <= 0; 
        }
        if (upperBound==null) {
            int r = cmp.compare(v, lowerBound);
            return lbt == BoundType.OPEN ? r > 0 : r >= 0; 
        }
        int r = cmp.compare(v, upperBound);
        boolean ok1 = ubt == BoundType.OPEN ? r < 0 : r <= 0;
        if (!ok1) return false;
        r = cmp.compare(v, lowerBound);
        return lbt == BoundType.OPEN ? r > 0 : r >= 0; 
    }
    
    /**
     * Determine if the Region contains the value.
     * <p>
     * The Comparator used is the default one for the type
     * (e.g., {@code Integer#compareTo(Integer)}.
     * <p>
     * @param v the value to check for containment
     * @return true if the Region contains the value
     * @see #contains(Comparable, Comparator)
     */
    public boolean contains(T v) {
        Comparator<T> cmp = getComparator(v);
        return contains(v, cmp);
    }
    
    private Comparator<T> getComparator(T v) {
        if (v instanceof Double) 
            return (lowerBound,upperBound) -> ((Double)lowerBound).compareTo((Double)upperBound);
        if (v instanceof Float) 
            return (lowerBound,upperBound) -> ((Float)lowerBound).compareTo((Float)upperBound);
        if (v instanceof Long) 
            return (lowerBound,upperBound) -> ((Long)lowerBound).compareTo((Long)upperBound);
        if (v instanceof Integer) 
            return (lowerBound,upperBound) -> ((Integer)lowerBound).compareTo((Integer)upperBound);
        if (v instanceof Short) 
            return (lowerBound,upperBound) -> ((Short)lowerBound).compareTo((Short)upperBound);
        if (v instanceof Byte) 
            return (lowerBound,upperBound) -> ((Byte)lowerBound).compareTo((Byte)upperBound);
//        if (v instanceof String) 
//            return (lowerBound,upperBound) -> ((String)lowerBound).compareTo((String)upperBound);
//        if (v instanceof Character) 
//            return (lowerBound,upperBound) -> ((Character)lowerBound).compareTo((Character)upperBound);
        if (v instanceof BigDecimal) 
            return (lowerBound,upperBound) -> ((BigDecimal)lowerBound).compareTo((BigDecimal)upperBound);
        if (v instanceof BigInteger) 
            return (lowerBound,upperBound) -> ((BigInteger)lowerBound).compareTo((BigInteger)upperBound);
        throw new IllegalArgumentException("Unsupported type: "+v.getClass());
    }
    
    /**
     * Create a Range from a string produced by toString()
     * <p>
     * N.B. See note in classdoc wrt Guava Range behavior. i.e., it
     * currently lacks a "Range from Range.toString() analog".
     * <p>
     * @param s value from toString()
     * @param clazz the class of the values in {@code s}
     */
    public static <T extends Comparable<?>> Range<T> valueOf(String s, Class<T> clazz) {
        char lbm = s.charAt(0);
        if (lbm != '[' && lbm != '(')
            throw new IllegalArgumentException(s);
        char ubm = s.charAt(s.length()-1);
        if (ubm != ']' && ubm != ')')
            throw new IllegalArgumentException(s);
        
        BoundType lbt = lbm == '[' ? BoundType.CLOSED : BoundType.OPEN;
        BoundType ubt = ubm == ']' ? BoundType.CLOSED : BoundType.OPEN;
        
        s = s.substring(1,s.length()-1);
        // this parsing is weak - broken for String bounds with embedded ".."
        // not an issue right now since we don't support String
        String[] parts = s.split("\\.\\.");
        if (parts.length != 2)
            throw new IllegalArgumentException("The range string bound values contains the separator sequence \"..\": " + s);
        
        String lbs = parts[0];
        String ubs = parts[1];

        T lowerBound = lbs.equals("*") ? null : boundValue(lbs, clazz);
        T upperBound = ubs.equals("*") ? null : boundValue(ubs, clazz);
        
        return range(lowerBound, lbt, upperBound, ubt);
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<?>> T boundValue(String strVal, Class<T> clazz) {
        if (strVal.equals("*"))
            return null;
        if (clazz.equals(Integer.class))
            return (T) Integer.valueOf(strVal);
        if (clazz.equals(Long.class))
            return (T) Long.valueOf(strVal);
        if (clazz.equals(Short.class))
            return (T) Short.valueOf(strVal);
        if (clazz.equals(Byte.class))
            return (T) Byte.valueOf(strVal);
        if (clazz.equals(Float.class))
            return (T) Float.valueOf(strVal);
        if (clazz.equals(Double.class))
            return (T) Double.valueOf(strVal);
//        if (clazz.equals(String.class))
//            return (T) String.valueOf(strVal);
//        if (clazz.equals(Character.class))
//            return (T) Character.valueOf(strVal.charAt(0));
        if (clazz.equals(BigInteger.class))
            return (T) new BigInteger(strVal);
        if (clazz.equals(BigDecimal.class))
            return (T) new BigDecimal(strVal);
        throw new IllegalArgumentException("Unhandled type "+clazz);
    }
    
    /**
     * Yields {@code <lowerBoundMarker><lowerBound>..<upperBound><upperBoundMarker>}.
     * <p>
     * Where the lowerBoundMarker is either "[" (inclusive/closed) or "(" (exclusive/open)
     * and the upperBoundMarker is  either "]" (inclusive/closed) or ")" (exclusive/open)
     * <p>
     * The bound value "*" is used to indicate an infinite value.
     * <p>
     * N.B. See note in classdoc wrt Guava Range behavior.
     * <p>
     * .e.g.,
     * <pre>
     * "[120..156)"  // lowerBound=120 inclusive, upperBound=156 exclusive
     * "[120..*)"    // an "atLeast" 120 range
     * </pre> 
     */
    public String toString() {
        String[] parts = { "(", "*", "*", ")" };
        if (lowerBound!=null) {
            parts[0] = lbt==BoundType.CLOSED ? "[" : "(";
            parts[1] = lowerBound.toString();
        }
        if (upperBound!=null) {
            parts[2] = upperBound.toString();
            parts[3] = ubt==BoundType.CLOSED ? "]" : ")";
        }
            
        return parts[0]+parts[1]+".."+parts[2]+parts[3];
    }
    
}
