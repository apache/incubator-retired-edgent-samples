package edgent.samples.topology;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edgent.function.Function;
import edgent.function.Predicate;
import edgent.providers.direct.DirectProvider;
import edgent.topology.TStream;
import edgent.topology.Topology;

public class DerivedTypesTest {
  
  static class Base {
    void hi(String s) {
      System.out.println(s+": I'm a "+this.getClass().getSimpleName());
    }
    public String toString() { return "I'm a Base"; }
  }
  
  static class Derived1 extends Base {
    String derived1Only() {
      return "derived1Only";
    }
    public String toString() { return "I'm a Derived1"; }
  }
  
  static class Derived2 extends Base {
    public String toString() { return "I'm a Derived2"; }
  }
  
  static class Other {
  }
  
  static class GBase<T> {
    void hi(String s) {
      System.out.println(s+": I'm a "+this.getClass().getSimpleName());
    }
  }
  
  static class GDerived1<T> extends GBase<T> {
    String derived1Only() {
      return "derived1Only";
    }
  }
  
  static class GDerived2<T> extends GBase<T> {
  }
  
  void fn(TStream<Base> s, String tag) {
    s.peek(t -> t.hi(tag));
  }
  
//  <T extends Base> void fnExtends(TStream<T> s, String tag) {
  void fnExtends(TStream<? extends Base> s, String tag) {
    s.peek(t -> t.hi(tag));
  }
  
  void fnList(TStream<List<Base>> s, String tag) {
    s.peek(l -> { for(Base b : l) { b.hi(tag); } });
  }
  
  <T extends Base> void fnListExtends(TStream<List<T>> s, String tag) {
    s.peek(l -> { for(Base b : l) { b.hi(tag); } });
  }
  
//  <T> TStream<T> union(TStream<T> s, TStream<T> s2) {
//    return s.union(s2);
//  }
//  
//  <T> TStream<T> union2(TStream<? extends T> s, TStream<? extends T> s2) {
//    return s.union(s2);  // Nope... just because TStream.union isn't "? extends T"?  ... doesn't follow the guidelines?
//        // NOPE... even after changing TStream.union(TStream<? extends T>) still doesn't compile...
//        // ... because s is "? extends T"
//    TStream<T> t;
//  }
//  
//  void mapTest(Topology top) {
//    TStream<Base> base = top.of(new Base());
//    TStream<Derived1> derived1 = top.of(new Derived1());
//    
//    // TStream<Base> b2 = derived1;  // type mismatch
//    
//    TStream<Base> bMap = derived1.map(t -> t);  // OK but does additional per-tuple handling at runtime
//    TStream<Base> bCast = cast(derived1, Base.class);  // much better
//    
//    Function<Base,Base> baseFn = t -> t;
//    TStream<Base> bMap2 = derived1.map(baseFn);  // type mismatch  TStream.map doesn't use extends
//
//    
//    
//  }
//  
//  <T,U> TStream<T> map(TStream<? extends T> s, Function<? extends T,U> fn) {
//    return s.map(fn);
//  }
  
  void mapTest2(Topology top) {
    TStream<Base> base = top.of(new Base());
    TStream<Derived1> derived1 = top.of(new Derived1());
    
    TStream<Base> bMap1 = derived1.map(t -> t);  // OK
    
    // compiler error: can't convert from TStream<Derived1> to TStream<Base>
    // demonstrative of a larger (Function reusability) usability issue?
    //TStream<Base> bMap2 = derived1.map(Functions.identity()); 
  }
  
//  void filterTest(Topology top) {
//    TStream<Base> base = top.of(new Base());
//    TStream<Derived1> derived1 = top.of(new Derived1());
//    
//    TStream<Derived1> derived2 = derived1.filter(Functions.alwaysTrue());  // OK
//    
//    Predicate<Base> basePredicate = t -> true;
//    Predicate<? extends Base> extendedBasePredicate = t -> true;
//    derived1 = derived1.filter(basePredicate);  // doesn't compile
//    derived1 = derived1.filter(extendedBasePredicate);  // doesn't compile
//    derived1 = derived1.filter(t -> basePredicate.test(t));  // OK
//    
//    // compiler error: can't convert from TStream<Derived1> to TStream<Base>
//    // demonstrative of a larger (Function reusability) usability issue?
//    //TStream<Base> bMap2 = derived1.map(Functions.identity()); 
//  }
  
  static <T> void extendsXyzzy(TStream<? extends T> s, Function<? extends T, String> fn) { }
  
  static class Foo<T> {
    T t;
    
    Foo(T t) { this.t = t; }
    public String toString() { return "I'm Foo<> for a: " + t; }

    
    Foo<T> filter(Predicate<T> p) { return this; }
    T get() { return t; }
  
    // no compiletime nor runtime error here.  can get a CCE when using result **later on**
    //    Foo<Base> b = new Foo<>(new Base());
    //    Foo<String> s = b.cast(String.class);  // no throw - yikes.
    //    String s = b.get(); // throws CCE
    //
    // any way to make a runtime check with this signature?
    // I cant figure out a way.
    
    <U> Foo<U> nonTypeSafeCast(Class<U> classU) {
      @SuppressWarnings("unchecked")
      Foo<U> u = (Foo<U>) this;
      return u;
    }

    // no compiletime. does generate a CCE if !(T extends U) (i.e., better than nonTypeSafeCast)
    //    Foo<Base> b = new Foo<>(new Base());
    //    Foo<String> s = b.cast(Base.class, String.class);  // CCE
    //
    // but pain to have to provide 2 class args... even though compiletime error
    // if first isn't a Class<T>.
    //    Foo<Base> d = d.cast(Derived.class, Base.class);  // OK
    // Hence 2 arg staticCast is easier?... and compiletime check...
    //    Foo<String> s = Foo.staticCast(d, String.class);  // compiletime
    
    <U> Foo<U> cast(Class<T> classT, Class<U> classU) {
      if (!classU.isAssignableFrom(classT))
        throw new ClassCastException();
      @SuppressWarnings("unchecked")
      Foo<U> u = (Foo<U>) this;
      return u;
    }

    // a useless signature because nothing "checks" the result assignment.
    // nothing relates U to a T
    // Foo<String> s = derived.cast(Base.class); // bad: doesn't throw (derived isa Base) but nothing checks result assign 
    
    <U> Foo<U> cast(Class<? super T> classU) {
      @SuppressWarnings("unchecked")
      Foo<U> u = (Foo<U>) this;
      return u;
    }

    // trying to relate U to T...  just getting syntax errors due to "super"
    //
    // <U> Foo<U> castU(Class<U super T> classU) { } // syntax error
    // <U super T> Foo<U> castU(Class<U> classU) { } // syntax error
    

  // compiletime typesafe... but static.  OK w/fluent 
  //   Foo<Base> b = Foo.staticCast(derived, Base.class); // OK
  //   Foo<Base> b = Foo.staticCast(derived, Base.class).filter(t -> t); // OK
  static <T extends U,U> Foo<U> staticCast(Foo<T> f, Class<U> classU) {
    @SuppressWarnings("unchecked")
    Foo<U> u = (Foo<U>) f;
    return u;
  }

  // compiletime typesafe... but static - single arg is nice... but can't use fluent
  //   Foo<Base> b = Foo.staticCast(derived); // OK
  //   Foo<Base> b = Foo.staticCast(derived).filter(t -> t); // compiletime
  static <T extends U,U> Foo<U> staticCast(Foo<T> f) {
    @SuppressWarnings("unchecked")
    Foo<U> u = (Foo<U>) f;
    return u;
  }
    
//    <U> Foo<T> filter(Predicate<U super T> p) { return null; }
//    <V extends T> Foo<T> filter(Predicate<V> p) { return null; }
//    <U super T> Foo<T> filter(Predicate<U> p) { return null; }
  }
  
  void FooTest(Topology top) {
    Foo<Base> base = new Foo<>(new Base());
    Foo<Derived1> derived1 = new Foo<>(new Derived1());
    Foo<Derived2> derived2 = new Foo<>(new Derived2());
    
    System.out.println("base: " + base);
    System.out.println("derived1: " + derived1);
    System.out.println("derived2: " + derived2);
    
//    Foo<Base> bB = base.cast(Base.class);
//    Foo<Base> bD1 = derived1.cast(Base.class);
    // Foo<Derived1> d1 = base.cast(Derived1.class);  // not a compiletime error
    
    Foo<Base> bB = base.nonTypeSafeCast(Base.class);
    Foo<Base> bD1 = derived1.nonTypeSafeCast(Base.class);
    Foo<Derived1> d1B = base.nonTypeSafeCast(Derived1.class);  // not a runtime error
    System.out.println("d1B: " + d1B);
    Foo<String> sB2 = base.nonTypeSafeCast(String.class);  // not a runtime error!!!
    System.out.println("sB2: " + sB2);
    String s = sB2.get();  // throws CCE
    int i = s.length();
    
    // Foo<String> sB3 = base.cast(String.class);  // good: compiletime "base isn't a String" 
    Foo<String> sB3 = base.cast(Base.class);  // ugh no compiletime (base isa Base)  
    Foo<Base> bB3 = base.cast(Base.class);  // OK
    bB3 = derived1.cast(Base.class);  // OK

    // d1 = Foo.staticCast(base, Derived1.class);  // compiletime err  base isn't a Derived1 (possibly legit downcast)
    bD1 = Foo.staticCast(derived1, Base.class); // OK
    bD1 = Foo.staticCast(derived1, Base.class).filter(t -> true); // fluent OK
    bD1 = Foo.staticCast(derived1); // OK but not fluent
    // bD1 = Foo.staticCast(derived1).filter(t -> true); // compile error

//    d1 = base.cast2(Derived1.class);  // not a compiletime error

    
    System.out.println("bB: " + bB);
    System.out.println("bD1: " + bD1);
    
  }

//  void KafkaPublishTest(Topology top) {
//    // TStream<Base> base = top.of(new Base());
//    TStream<Derived1> derived1 = top.of(new Derived1());
//    TStream<Derived2> derived2 = top.of(new Derived2());
//
//    KafkaProducer producer = new KafkaProducer(top, () -> null);
//    Function<Base,String> baseKeyFn = t -> "hi";
//    Function<Base,String> basePayloadFn = t -> t.toString();
//    Function<? extends Base,String> extendsBaseKeyFn = t -> "hi";
//    Function<? extends Base,String> extendsBasePayloadFn = t -> t.toString();
//    
//    TStream<Base> base = cast(derived1,Base.class);
//    producer.publish(derived1, baseKeyFn, basePayloadFn, t -> "topic", t -> 0); // doesn't compile
//    producer.publish(base, baseKeyFn, basePayloadFn, t -> "topic", t -> 0);
//    producer.publish(cast(derived1,Base.class), baseKeyFn, basePayloadFn, t -> "topic", t -> 0);
//
//    extendsXyzzy(derived1, extendsBaseKeyFn); // OK
//    extendsXyzzy(base, extendsBaseKeyFn); // OK
//
//    producer.publish(derived1, extendsBaseKeyFn, extendsBasePayloadFn, t -> "topic", t -> 0); // doesn't compile
//    producer.publish(base, extendsBaseKeyFn, extendsBasePayloadFn, t -> "topic", t -> 0); // doesn't compile
//  }

  // Hmm... this all works fine with U is not a generic (e.g., List<X>)
  // Does that diminish the utility/validity of this? 
  // A List<Derived> isa List<Base> == false. ... but isn't that just fallout
  // from the generics implementation?  In a perfect world couldn't it have been "true"?
  //
  // This correctly generates compile-time error is T doesn't extend U.
  // would be:  TStream<T>{ <T extends U> TStream<U> cast(Class<U> classU); }
  //
  // NOTE: a variant of this without the Class<U> arg is possible
  // but it can't be used in a fluent style because the
  // compiler can't, or at least doesn't, guess the right type for U.
  // e.g., this generates a compile-time error
  //     TStream<Base> base = asA(derived).filter(t -> true);
  // though this works:  TStream<Base> base = asA(derived);
  // To keep the API less cluttered, just provide this version
  // as it likely won't really be a burden to the user,
  // and it's nicely balanced with downcast() below
  //
  static <T extends U,U> TStream<U> cast(TStream<T> s, Class<U> classU) {
    // Since T extends U, this should be a safe cast but the compiler warns
    // hence the suppress.  Do an intermediate cast to <?> doesn't help.
    @SuppressWarnings("unchecked")
    TStream<U> u = (TStream<U>) s;
    return u;
  }

  // Maybe this is too dangerous and shouldb't be included...
  // make the app change their code so as to not need it,
  // or at least make them define their own fn for this.
  // Alternatively, define this so that instead of a CCE, the
  // contents of the stream will only include tuples that are actually a U
  //    (  TStream<U> u = (TStream<U>) s2.filter(t -> t instanceof Class<U>)
  // Keep the simple/easy-easy and don't offer a variant that lets the user
  // supply their own predicate and/or be notified if a tuple is being filtered out;
  // they can roll their own for that.
  //
  // This generates a compile-time error unless the downcast is plausible: U extends T
  // It it succeeds, U does extend T, it will still subsequently yield an ugly
  // Edgent runtime failure if a tuple in TStream<U> isn't a T.
  // would be:  TStream<T>{ <U extends T> TStream<U> downcast(Class<U> klassU); }
  static <T, U extends T> TStream<U> downcast(TStream<T> s,  Class<U> klassU) {
//    TStream<?> s2 = (TStream<?>) s;
//    @SuppressWarnings("unchecked")
//    TStream<U> u = (TStream<U>) s2;
    @SuppressWarnings("unchecked")
    TStream<U> u = (TStream<U>) s;
    return u.filter(t -> klassU.isInstance(t));  // filter out those not actually a U
  }

  public static void main(String[] args) {
    new DerivedTypesTest().run();
  }
  
  private void run()  {
    DirectProvider dp = new DirectProvider();
    Topology top = dp.newTopology();
    
    TStream<Base> base = top.of(new Base());
    TStream<Derived1> derived1 = top.of(new Derived1());
    TStream<Derived2> derived2 = top.of(new Derived2());
    TStream<Other> other = top.of(new Other());

    {
    TStream<Base> baseU1 = base.union(base);
    // TStream<Base> baseU2 = base.union(derived1);  // OK with "fixed" TStream
//    Set<Base> bases = new HashSet<>();
//    bases.add(new Base());
//    bases.add(new Derived1());
    // OK Set<TStream<Base>> bases = new HashSet<>();
    //   bases.add(derived1);  // NO.  Why doesn't Set.add(? extends E) ?  Didn't follow the guidelines?
    Set<TStream<? extends Base>> bases = new HashSet<>();
    bases.add(derived1);  // OK
    
    // TStream<Base> b = (TStream<Base>) derived1;  // cannot cast compile error
    // fn(derived1, "derived1");  // compile error
    fnExtends(derived1, "fnExtends derived1");
    }
    
    TStream<? extends Base> b = derived1;
    //fn(b, "");  // compile-time error
    fnExtends(b, "fnExtends ");
    
    TStream<Base> bFromD1, bFromD2;
    TStream<Derived1> d1FromB;
    TStream<Derived1> d1FromD2;
    TStream<Base> bFromO;
    TStream<Other> oFromD1;
    TStream<Base> bFromUnion;
    
    bFromD1 = cast(derived1, Base.class);
    fn(bFromD1, "bFromD1");
    fn(cast(derived1, Base.class), "implicit bFromD1");
    bFromD1 = cast(derived1, Base.class).filter(t -> true);
    
    // legal downcast
    d1FromB = downcast(bFromD1, Derived1.class);
    //fn(d1FromB, "downcast d1FromB");  // correctly get compile-time error
    fnExtends(d1FromB, "downcast d1FromB");

    // Other isn't a Base
    // bFromO = asA(other);  // correctly get a compile-time error
    // bFromO = downcast(other, Other.class, Base.class);  // correctly get a compile-time error
    // bFromO.peek(t -> System.err.println("bFromO shouldn't get here"));
    
    // Derived1 isn't a Derived2
    // d1FromD2 = asA(derived2);  // correctly get a compile-time error
    // d1FromD2 = downcast(derived2, Derived2.class, Derived1.class);  // correctly get a compile-time error
    // fnExtends(d1FromD2, "d1FromD2 shouldn't get here");
    
    // plausible (so downcast doesn't throw) but ultimately illegal
    // and get CCE from Edgent when processing a tuple
    bFromD2 = cast(derived2, Base.class);
    fn(bFromD2, "bFromD2");
    d1FromB = downcast(bFromD2, Derived1.class);
    fnExtends(d1FromB, "d1FromB but really D2 - downcast filters out the non-D1 tuples so don't get this msg");
    d1FromB.peek(t -> System.err.println(t.derived1Only() + "downcast filters out non-D1 so don't get this msg"));
    
    bFromUnion = bFromD1.union(cast(derived2, Base.class));
    fn(bFromUnion, "bFromUnion");

    Set<TStream<Base>> bases = new HashSet<>();
    bases.add(cast(derived1, Base.class));
    bases.add(cast(derived2, Base.class));
    bFromD1.union(bases);
    fn(bFromUnion, "bFromUnion(Set)");
    
    TStream<Base> bFromMap = bFromD1.map(t -> t);
    fn(bFromMap, "bFromMap");
    TStream<Base> bFromMap2 = bFromD1.map(t -> new Base());
    fn(bFromMap2, "bFromMap2");
    TStream<Base> bFromMap3 = bFromD1.map(t -> new Derived1());
    fn(bFromMap3, "bFromMap3");
    TStream<Base> bFromMap4 = bFromD1.map(t -> new Derived2());
    fn(bFromMap4, "bFromMap4");
    
    tryGenerics(top);
    
    FooTest(top);
        
    dp.submit(top);
  }
  
  void tryGenerics(Topology top) {
    List<Derived1> d1List = Arrays.asList(new Derived1());
    TStream<List<Derived1>> derived1 = top.of(d1List);
    List<Derived2> d2List = Arrays.asList(new Derived2());
    TStream<List<Derived2>> derived2 = top.of(d2List);
    

    // TStream<List<Base>> b = (TStream<List<Base>>) derived1;  // cannot cast compile error
    //fnList(derived1, "derived1");  // compile error
    fnListExtends(derived1, "fnExtends derived1");
    
    
    TStream<List<Base>> bFromD1, bFromD2;
    TStream<List<Derived1>> d1FromB;
    TStream<List<Derived1>> d1FromD2;
    TStream<List<Base>> bFromO;
    TStream<List<Other>> oFromD1;
    TStream<List<Base>> bFromUnion;
    
    // bFromD1 = cast(derived1, List<Base>.class);  // compile-time error
    // bFromD1 = cast(derived1, List.class);  // compile-time error
    // List<Base> baseList = Collections.emptyList();
    // bFromD1 = cast(derived1, baseList.getClass());  // compile-time error
    // bFromD1 = (TStream<List<Base>>) ((TStream<List<?>>) derived1);  // compile-time error
    
    bFromD1 = (TStream<List<Base>>) ((TStream<?>) derived1);  // Type safety warning: unchecked cast
    
    fnList(bFromD1, "bFromD1 list");
  //  fnList(cast(derived1, Base.class), "implicit bFromD1");
  //  bFromD1 = cast(derived1, Base.class).filter(t -> true);
    
//    // legal downcast
//    d1FromB = downcast(bFromD1, Derived1.class);
//    //fn(d1FromB, "downcast d1FromB");  // correctly get compile-time error
//    fnExtends(d1FromB, "downcast d1FromB");
//
//    // Other isn't a Base
//    // bFromO = asA(other);  // correctly get a compile-time error
//    // bFromO = downcast(other, Other.class, Base.class);  // correctly get a compile-time error
//    // bFromO.peek(t -> System.err.println("bFromO shouldn't get here"));
//    
//    // Derived1 isn't a Derived2
//    // d1FromD2 = asA(derived2);  // correctly get a compile-time error
//    // d1FromD2 = downcast(derived2, Derived2.class, Derived1.class);  // correctly get a compile-time error
//    // fnExtends(d1FromD2, "d1FromD2 shouldn't get here");
//    
//    // plausible (so downcast doesn't throw) but ultimately illegal
//    // and get CCE from Edgent when processing a tuple
//    bFromD2 = cast(derived2, Base.class);
//    fn(bFromD2, "bFromD2");
//    d1FromB = downcast(bFromD2, Derived1.class);
//    fnExtends(d1FromB, "d1FromB but really D2 - downcast filters out the non-D1 tuples so don't get this msg");
//    d1FromB.peek(t -> System.err.println(t.derived1Only() + "downcast filters out non-D1 so don't get this msg"));
//    
//    bFromUnion = bFromD1.union(cast(derived2, Base.class));
//    fn(bFromUnion, "bFromUnion");
//
//    Set<TStream<Base>> bases = new HashSet<>();
//    bases.add(cast(derived1, Base.class));
//    bases.add(cast(derived2, Base.class));
//    bFromD1.union(bases);
//    fn(bFromUnion, "bFromUnion(Set)");
    
    
    
  }

}
