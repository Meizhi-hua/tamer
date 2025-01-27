public class Test5023550 extends AbstractTest {
    public static void main(String[] args) {
        new Test5023550().test(true);
    }
    private final Owner owner = new Owner();
    @Override
    protected void initialize(XMLEncoder encoder) {
        encoder.setOwner(this.owner);
        encoder.setPersistenceDelegate(A.class, new ADelegate());
        encoder.setPersistenceDelegate(B.class, new BDelegate());
        encoder.setPersistenceDelegate(C.class, new CDelegate());
    }
    @Override
    protected void initialize(XMLDecoder decoder) {
        decoder.setOwner(this.owner);
    }
    protected Object getObject() {
        return this.owner.newA(this.owner.newB().newC());
    }
    public static class Owner {
        public A newA(C c) {
            return new A(c);
        }
        public B newB() {
            return new B();
        }
    }
    public static class A {
        private final C c;
        private A(C c) {
            this.c = c;
        }
        public C getC() {
            return this.c;
        }
    }
    public static class B {
        public C newC() {
            return new C(this);
        }
    }
    public static class C {
        private final B b;
        private C(B b) {
            this.b = b;
        }
        public B getB() {
            return this.b;
        }
    }
    public static class ADelegate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object old, Encoder out) {
            XMLEncoder encoder = (XMLEncoder) out;
            A a = (A) old;
            return new Expression(old, encoder.getOwner(), "newA", new Object[] { a.getC() });
        }
    }
    public static class BDelegate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object old, Encoder out) {
            XMLEncoder encoder = (XMLEncoder) out;
            return new Expression(old, encoder.getOwner(), "newB", new Object[0]);
        }
    }
    public static class CDelegate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object old, Encoder out) {
            C c = (C) old;
            return new Expression(c, c.getB(), "newC", new Object[0]);
        }
    }
}
