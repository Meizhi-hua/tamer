public final class EncodedArrayItem extends OffsettedItem {
    private static final int ALIGNMENT = 1;
    private final CstArray array;
    private byte[] encodedForm;
    public EncodedArrayItem(CstArray array) {
        super(ALIGNMENT, -1);
        if (array == null) {
            throw new NullPointerException("array == null");
        }
        this.array = array;
        this.encodedForm = null;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ENCODED_ARRAY_ITEM;
    }
    @Override
    public int hashCode() {
        return array.hashCode();
    }
    @Override
    protected int compareTo0(OffsettedItem other) {
        EncodedArrayItem otherArray = (EncodedArrayItem) other;
        return array.compareTo(otherArray.array);
    }
    @Override
    public String toHuman() {
        return array.toHuman();
    }
    public void addContents(DexFile file) {
        ValueEncoder.addContents(file, array);
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        ValueEncoder encoder = new ValueEncoder(addedTo.getFile(), out);
        encoder.writeArray(array, false);
        encodedForm = out.toByteArray();
        setWriteSize(encodedForm.length);
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        if (annotates) {
            out.annotate(0, offsetString() + " encoded array");
            ValueEncoder encoder = new ValueEncoder(file, out);
            encoder.writeArray(array, true);
        } else {
            out.write(encodedForm);
        }
    }
}
