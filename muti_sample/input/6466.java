class T1 {
    public void print(Inner1 inf) {
        inf.print();
    }
    public class Inner1 {
        public void print() {
            System.out.println("Inner1");
        }
    }
}
class T2 extends T1 {
    public void print() {
        super.print(new Inner2());
    }
    private class Inner2
        extends tests.T1$Inner1 
    {
        public void print() {
            System.out.println("Inner2");
        }
    }
}
