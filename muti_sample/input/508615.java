public class InstanceOf implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -551735356674347591L;
    private final Class<?> clazz;
    public InstanceOf(Class<?> clazz) {
        this.clazz = clazz;
    }
    public boolean matches(Object actual) {
        return (actual != null) && clazz.isAssignableFrom(actual.getClass());
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("isA(" + clazz.getName() + ")");
    }
}
