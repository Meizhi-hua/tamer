public class IllegalCharsetNameException extends IllegalArgumentException {
    private static final long serialVersionUID = 1457525358470002989L;
    private String charsetName;
    public IllegalCharsetNameException(String charset) {
        super(Messages.getString("niochar.0F", charset)); 
        this.charsetName = charset;
    }
    public String getCharsetName() {
        return this.charsetName;
    }
}
