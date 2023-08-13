public class GreaterThan<T extends Comparable<T>> extends CompareTo<T> {
    private static final long serialVersionUID = 2736983121197045828L;
    public GreaterThan(Comparable<T> value) {
        super(value);
    }
    @Override
    protected String getName() {
        return "gt";
    }
    @Override
    protected boolean matchResult(int result) {
        return result > 0;
    }    
}
