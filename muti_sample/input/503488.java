public class EntityTemplate extends AbstractHttpEntity {
    private final ContentProducer contentproducer;
    public EntityTemplate(final ContentProducer contentproducer) {
        super();
        if (contentproducer == null) {
            throw new IllegalArgumentException("Content producer may not be null");
        }
        this.contentproducer = contentproducer; 
    }
    public long getContentLength() {
        return -1;
    }
    public InputStream getContent() {
        throw new UnsupportedOperationException("Entity template does not implement getContent()");
    }
    public boolean isRepeatable() {
        return true;
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        this.contentproducer.writeTo(outstream);
    }
    public boolean isStreaming() {
        return true;
    }
    public void consumeContent() throws IOException {
    }
}
