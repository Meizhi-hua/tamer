public class CaptureInSubtype {
    static class SuperOfFlaw<S>{
        S s;
        S get() { return s;}
        void put(S a) { s = a;}
        SuperOfFlaw(S a) { s = a;}
    }
    static class Flaw<T> extends SuperOfFlaw<List<T>> {
        List<T> fetch(){return s;}
        Flaw(T t){super(new ArrayList<T>()); s.add(t);}
    }
    static class SuperOfShowFlaw {
        SuperOfFlaw<List<?>> m(){return null;}
    }
    public static class ShowFlaw extends SuperOfShowFlaw {
        static Flaw<Number> fn =  new Flaw<Number>(new Integer(3));
        Flaw<?> m(){return fn;}
    }
    public static void main(String[] args) {
        SuperOfShowFlaw sosf = new ShowFlaw();
        SuperOfFlaw<List<?>> sof = sosf.m();
        List<String> ls = new ArrayList<String>();
        ls.add("Smalltalk rules!");
        sof.put(ls);
        Number n = ShowFlaw.fn.get().get(0);
    }
}
