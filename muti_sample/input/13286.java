class InferredDoNotConformToBounds {
   static class SuperFoo<X> {}
   static class Foo<X extends Number> extends SuperFoo<X> {
       Foo(X x) {}
   }
   SuperFoo<String> sf1 = new Foo<>("");
}
