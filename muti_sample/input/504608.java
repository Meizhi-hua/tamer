public class CompareEqual<T extends Comparable<T>> extends CompareTo<T> {
    private static final long serialVersionUID = 7616033998227799268L;
    public CompareEqual(Comparable<T> value) {
        super(value);
    }
    @Override
    protected String getName() {
        return "cmpEq";
    }
    @Override
    protected boolean matchResult(int result) {
        return result == 0;
    }
}
