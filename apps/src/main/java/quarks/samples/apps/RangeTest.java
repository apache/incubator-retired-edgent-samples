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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import quarks.function.Supplier;

/**
 * Test Range.
 */
public class RangeTest {
    
    private <T extends Comparable<?>> void testContains(Range<T> range, T v, Boolean expected) {
        assertEquals("range"+range+".contains(range"+v+")", expected, range.contains(v));
    }
    
    private <T extends Comparable<?>> void testToString(Range<T> range, String expected) {
        assertEquals("range.toString()", expected, range.toString());
    }

    private <T extends Comparable<?>> void testValueOf(String str, Class<T> clazz, Range<T> expected) {
        assertEquals("Range.valueOf("+clazz.getSimpleName()+")", 
                Range.valueOf(str, clazz), expected);
    }
    
    private <T extends Comparable<?>> void testEquals(Range<T> r1, Range<T> r2, Boolean expected) {
        assertEquals("range"+r1+".equals(range"+r2+")", expected, r1.equals(r2));
    }
    
    private <T extends Comparable<?>> void testHashCode(Range<T> range, int hashCode, Boolean expected) {
        if (expected)
            assertEquals("range"+range+".hashCode()", hashCode, range.hashCode());
        else
            assertNotEquals("range"+range+".hashCode()", hashCode, range.hashCode());
    }

    private <T extends Comparable<?>> void testSupportedType(Supplier<Range<T>> range, Class<T> clazz, Boolean expected) {
        boolean act;
        try {
            range.get();
            act = true;
        }
        catch (IllegalArgumentException e) {
            act = false;
        }
        assertEquals("supported type "+clazz.toString(), expected, act);
    }
    
    private <T extends Comparable<?>> void testJson(Range<T> r1, Type typeOfT) {
        String json = new Gson().toJson(r1);
        Range<T> r2 = new Gson().fromJson(json, typeOfT);
        assertEquals("json="+json+" typeOfT="+typeOfT, r1, r2);
    }
    
    @Test
    public void testSupportedTypes() {
        testSupportedType(() -> Range.closed("a", "f"), String.class, false);
        testSupportedType(() -> Range.closed('a', 'f'), Character.class, false);
        testSupportedType(() -> Range.closed(1, 10), Integer.class, true);
        testSupportedType(() -> Range.closed((short)1, (short)10), Short.class, true);
        testSupportedType(() -> Range.closed((byte)1, (byte)10), Byte.class, true);
        testSupportedType(() -> Range.closed(1L, 10L), Long.class, true);
        testSupportedType(() -> Range.closed(1f, 10f), Float.class, true);
        testSupportedType(() -> Range.closed(1d, 10d), Double.class, true);
        testSupportedType(() -> Range.closed(BigInteger.valueOf(1), BigInteger.valueOf(10)), BigInteger.class, true);
        testSupportedType(() -> Range.closed(BigDecimal.valueOf(1), BigDecimal.valueOf(10)), BigDecimal.class, true);
    }
    
    @Test
    public void testContainsOpen() {
        testContains(Range.open(2,4), 1, false);
        testContains(Range.open(2,4), 2, false);
        testContains(Range.open(2,4), 3, true);
        testContains(Range.open(2,4), 4, false);
        testContains(Range.open(2,4), 5, false);
    }

    @Test
    public void testContainsClosed() {
        testContains(Range.closed(2,4), 1, false);
        testContains(Range.closed(2,4), 2, true);
        testContains(Range.closed(2,4), 3, true);
        testContains(Range.closed(2,4), 4, true);
        testContains(Range.closed(2,4), 5, false);
    }

    @Test
    public void testContainsOpenClosed() {
        testContains(Range.openClosed(2,4), 1, false);
        testContains(Range.openClosed(2,4), 2, false);
        testContains(Range.openClosed(2,4), 3, true);
        testContains(Range.openClosed(2,4), 4, true);
        testContains(Range.openClosed(2,4), 5, false);
    }

    @Test
    public void testContainsClosedOpen() {
        testContains(Range.closedOpen(2,4), 1, false);
        testContains(Range.closedOpen(2,4), 2, true);
        testContains(Range.closedOpen(2,4), 3, true);
        testContains(Range.closedOpen(2,4), 4, false);
        testContains(Range.closedOpen(2,4), 5, false);
    }

    @Test
    public void testContainsGreaterThan() {
        testContains(Range.greaterThan(2), 1, false);
        testContains(Range.greaterThan(2), 2, false);
        testContains(Range.greaterThan(2), 3, true);
    }

    @Test
    public void testContainsAtLeast() {
        testContains(Range.atLeast(2), 1, false);
        testContains(Range.atLeast(2), 2, true);
        testContains(Range.atLeast(2), 3, true);
    }

    @Test
    public void testContainsLessThan() {
        testContains(Range.lessThan(2), 1, true);
        testContains(Range.lessThan(2), 2, false);
        testContains(Range.lessThan(2), 3, false);
    }

    @Test
    public void testContainsAtMost() {
        testContains(Range.atMost(2), 1, true);
        testContains(Range.atMost(2), 2, true);
        testContains(Range.atMost(2), 3, false);
    }

    @Test
    public void testContainsSingleton() {
        testContains(Range.singleton(2), 1, false);
        testContains(Range.singleton(2), 2, true);
        testContains(Range.singleton(2), 3, false);
    }

    @Test
    public void testEquals() {
        testEquals(Range.closed(2,4), Range.closed(2,4), true);
        testEquals(Range.closed(2,4), Range.closed(2,3), false);
        testEquals(Range.closed(3,4), Range.closed(2,4), false);
        testEquals(Range.atMost(2), Range.atMost(2), true);
        testEquals(Range.atMost(2), Range.atMost(3), false);
        testEquals(Range.atLeast(2), Range.atLeast(2), true);
        testEquals(Range.atLeast(2), Range.atLeast(3), false);
        testEquals(Range.closed(2,2), Range.singleton(2), true);
    }

    @Test
    public void testHashCode() {
        testHashCode(Range.atMost(2), Range.atMost(2).hashCode(), true);
        testHashCode(Range.atMost(2), 0, false);
        testHashCode(Range.atMost(2), Range.atMost(3).hashCode(), false);
        testHashCode(Range.atLeast(2), Range.atMost(2).hashCode(), false);
    }

    @Test
    public void testToString() {
        testToString(Range.open(2,4), "(2..4)");
        testToString(Range.closed(2,4), "[2..4]");
        testToString(Range.openClosed(2,4), "(2..4]");
        testToString(Range.closedOpen(2,4), "[2..4)");
        testToString(Range.greaterThan(2), "(2..*)");
        testToString(Range.atLeast(2), "[2..*)");
        testToString(Range.lessThan(2), "(*..2)");
        testToString(Range.atMost(2), "(*..2]");
    }

    @Test
    public void testValueOf() {
        testValueOf("(2..4)", Integer.class, Range.open(2, 4));
        testValueOf("[2..4]", Integer.class, Range.closed(2, 4));
        testValueOf("(2..4]", Integer.class, Range.openClosed(2, 4));
        testValueOf("[2..4)", Integer.class, Range.closedOpen(2, 4));
        testValueOf("(2..*)", Integer.class, Range.greaterThan(2));
        testValueOf("[2..*)", Integer.class, Range.atLeast(2));
        testValueOf("(*..2)", Integer.class, Range.lessThan(2));
        testValueOf("(*..2]", Integer.class, Range.atMost(2));
    }

    @Test
    public void testContainsOtherByte() {
        testContains(Range.open((byte)2,(byte)4), (byte)1, false);
        testContains(Range.open((byte)2,(byte)4), (byte)2, false);
        testContains(Range.open((byte)2,(byte)4), (byte)3, true);
        testContains(Range.open((byte)2,(byte)4), (byte)4, false);
        testContains(Range.open((byte)2,(byte)4), (byte)5, false);
    }

    @Test
    public void testContainsOtherShort() {
        testContains(Range.open((short)2,(short)4), (short)1, false);
        testContains(Range.open((short)2,(short)4), (short)2, false);
        testContains(Range.open((short)2,(short)4), (short)3, true);
        testContains(Range.open((short)2,(short)4), (short)4, false);
        testContains(Range.open((short)2,(short)4), (short)5, false);
    }

    @Test
    public void testContainsOtherLong() {
        testContains(Range.open(2L,4L), 1L, false);
        testContains(Range.open(2L,4L), 2L, false);
        testContains(Range.open(2L,4L), 3L, true);
        testContains(Range.open(2L,4L), 4L, false);
        testContains(Range.open(2L,4L), 5L, false);
    }

    @Test
    public void testContainsOtherFloat() {
        testContains(Range.open(2f,4f), 1f, false);
        testContains(Range.open(2f,4f), 2f, false);
        testContains(Range.open(2f,4f), 2.001f, true);
        testContains(Range.open(2f,4f), 3.999f, true);
        testContains(Range.open(2f,4f), 4f, false);
        testContains(Range.open(2f,4f), 5f, false);
    }

    @Test
    public void testContainsOtherDouble() {
        testContains(Range.open(2d,4d), 1d, false);
        testContains(Range.open(2d,4d), 2d, false);
        testContains(Range.open(2d,4d), 2.001d, true);
        testContains(Range.open(2d,4d), 3.999d, true);
        testContains(Range.open(2d,4d), 4d, false);
        testContains(Range.open(2d,4d), 5d, false);
    }

    @Test
    public void testContainsOtherBigInteger() {
        testContains(Range.open(BigInteger.valueOf(2),BigInteger.valueOf(4)), BigInteger.valueOf(1), false);
        testContains(Range.open(BigInteger.valueOf(2),BigInteger.valueOf(4)), BigInteger.valueOf(2), false);
        testContains(Range.open(BigInteger.valueOf(2),BigInteger.valueOf(4)), BigInteger.valueOf(3), true);
        testContains(Range.open(BigInteger.valueOf(2),BigInteger.valueOf(4)), BigInteger.valueOf(4), false);
        testContains(Range.open(BigInteger.valueOf(2),BigInteger.valueOf(4)), BigInteger.valueOf(5), false);
    }

    @Test
    public void testContainsOtherBigDecimal() {
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(1), false);
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(2), false);
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(2.001), true);
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(3.999), true);
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(4), false);
        testContains(Range.open(new BigDecimal(2),new BigDecimal(4)), new BigDecimal(5), false);
    }

//  @Test
//  public void testContainsOtherString() {
//      testContains(Range.open("b","d"), "a", false);
//      testContains(Range.open("b","d"), "b", false);
//      testContains(Range.open("b","d"), "bc", true);
//      testContains(Range.open("b","d"), "c", true);
//      testContains(Range.open("b","d"), "cd", true);
//      testContains(Range.open("b","d"), "d", false);
//      testContains(Range.open("b","d"), "de", false);
//      testContains(Range.open("b","d"), "e", false);
//  }

//  @Test
//  public void testContainsOtherCharacter() {
//      testContains(Range.open('b','d'), 'a', false);
//      testContains(Range.open('b','d'), 'b', false);
//      testContains(Range.open('b','d'), 'c', true);
//      testContains(Range.open('b','d'), 'd', false);
//      testContains(Range.open('b','d'), 'e', false);
//  }

    @Test
    public void testValueOfOther() {
        testValueOf("(2..4)", Short.class, Range.open((short)2, (short)4));
        testValueOf("(2..4)", Byte.class, Range.open((byte)2, (byte)4));
        testValueOf("(2..4)", Long.class, Range.open(2L, 4L));
        testValueOf("(2.128..4.25)", Float.class, Range.open(2.128f, 4.25f));
        testValueOf("(2.128..4.25)", Double.class, Range.open(2.128d, 4.25d));
        testValueOf("(2..4)", BigInteger.class, Range.open(BigInteger.valueOf(2), BigInteger.valueOf(4)));
        testValueOf("(2.5..4.25)", BigDecimal.class, Range.open(new BigDecimal(2.5), new BigDecimal(4.25)));
//        testValueOf("(ab..fg)", String.class, Range.open("ab", "fg"));
//        testValueOf("(ab..c..fg)", String.class, Range.open("ab..c", "fg")); // yikes
//        testValueOf("(a..f)", Character.class, Range.open('a', 'f'));
    }

    @Test
    public void testJsonAllTypes() {
        testJson(Range.closed(1, 10), new TypeToken<Range<Integer>>(){}.getType());
        testJson(Range.closed((short)1, (short)10), new TypeToken<Range<Short>>(){}.getType());
        testJson(Range.closed((byte)1, (byte)10), new TypeToken<Range<Byte>>(){}.getType());
        testJson(Range.closed(1L, 10L), new TypeToken<Range<Long>>(){}.getType());
        testJson(Range.closed(1f, 10f), new TypeToken<Range<Float>>(){}.getType());
        testJson(Range.closed(1d, 10d), new TypeToken<Range<Double>>(){}.getType());
        testJson(Range.closed(BigInteger.valueOf(1), BigInteger.valueOf(10)), new TypeToken<Range<BigInteger>>(){}.getType());
        testJson(Range.closed(BigDecimal.valueOf(1), BigDecimal.valueOf(10)), new TypeToken<Range<BigDecimal>>(){}.getType());
//        testJson(Range("ab", "fg"), new TypeToken<Range<String>>(){}.getType());
//        testJson(Range("ab..c", "fg"), new TypeToken<Range<String>>(){}.getType());
//        testJson(Range('a', 'f'), new TypeToken<Range<Character>>(){}.getType());
    }

}
