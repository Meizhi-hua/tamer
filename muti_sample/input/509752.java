public class Captures<T> implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -5048595127450771363L;
    private final Capture<T> capture;
    private T potentialValue;
    public Captures(Capture<T> captured) {
        this.capture = captured;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("capture(").append(capture).append(")");
    }
    public void setPotentialValue(T potentialValue) {
        this.potentialValue = potentialValue;
    }
    @SuppressWarnings("unchecked")
    public boolean matches(Object actual) {
        LastControl.getCurrentInvocation().addCapture((Captures<Object>) this,
                actual);
        return true;
    }
    public void validateCapture() {
        capture.setValue(potentialValue);
    }
}
