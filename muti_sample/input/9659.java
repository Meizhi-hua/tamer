class MissingResourceException extends RuntimeException {
    public MissingResourceException(String s, String className, String key) {
        super(s);
        this.className = className;
        this.key = key;
    }
    MissingResourceException(String message, String className, String key, Throwable cause) {
        super(message, cause);
        this.className = className;
        this.key = key;
    }
    public String getClassName() {
        return className;
    }
    public String getKey() {
        return key;
    }
    private static final long serialVersionUID = -4876345176062000401L;
    private String className;
    private String key;
}
