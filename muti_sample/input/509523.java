public class Matches implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -6657694947057597484L;
    private final String regex;
    public Matches(String regex) {
        this.regex = regex;
    }
    public boolean matches(Object actual) {
        return (actual instanceof String) && ((String) actual).matches(regex);
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("matches(\"" + regex.replaceAll("\\\\", "\\\\\\\\")
                + "\")");
    }
}
