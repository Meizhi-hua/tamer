public class BasicNameValuePair implements NameValuePair, Cloneable {
    private final String name;
    private final String value;
    public BasicNameValuePair(final String name, final String value) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return this.name;
    }
    public String getValue() {
        return this.value;
    }
    public String toString() {
        int len = this.name.length();
        if (this.value != null)
            len += 1 + this.value.length();
        CharArrayBuffer buffer = new CharArrayBuffer(len);
        buffer.append(this.name);
        if (this.value != null) {
            buffer.append("=");
            buffer.append(this.value);
        }
        return buffer.toString();
    }
    public boolean equals(final Object object) {
        if (object == null) return false;
        if (this == object) return true;
        if (object instanceof NameValuePair) {
            BasicNameValuePair that = (BasicNameValuePair) object;
            return this.name.equals(that.name)
                  && LangUtils.equals(this.value, that.value);
        } else {
            return false;
        }
    }
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.value);
        return hash;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}