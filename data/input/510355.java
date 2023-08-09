final  class AttributeListParser {
    private final DirectClassFile cf;
    private final int context;
    private final int offset;
    private final AttributeFactory attributeFactory;
    private final StdAttributeList list;
    private int endOffset;
    private ParseObserver observer;
    public AttributeListParser(DirectClassFile cf, int context, int offset,
                               AttributeFactory attributeFactory) {
        if (cf == null) {
            throw new NullPointerException("cf == null");
        }
        if (attributeFactory == null) {
            throw new NullPointerException("attributeFactory == null");
        }
        int size = cf.getBytes().getUnsignedShort(offset);
        this.cf = cf;
        this.context = context;
        this.offset = offset;
        this.attributeFactory = attributeFactory;
        this.list = new StdAttributeList(size);
        this.endOffset = -1;
    }
    public void setObserver(ParseObserver observer) {
        this.observer = observer;
    }
    public int getEndOffset() {
        parseIfNecessary();
        return endOffset;
    }
    public StdAttributeList getList() {
        parseIfNecessary();
        return list;
    }
    private void parseIfNecessary() {
        if (endOffset < 0) {
            parse();
        }
    }
    private void parse() {
        int sz = list.size();
        int at = offset + 2; 
        ByteArray bytes = cf.getBytes();
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "attributes_count: " + Hex.u2(sz));
        }
        for (int i = 0; i < sz; i++) {
            try {
                if (observer != null) {
                    observer.parsed(bytes, at, 0,
                                    "\nattributes[" + i + "]:\n");
                    observer.changeIndent(1);
                }
                Attribute attrib =
                    attributeFactory.parse(cf, context, at, observer);
                at += attrib.byteLength();
                list.set(i, attrib);
                if (observer != null) {
                    observer.changeIndent(-1);
                    observer.parsed(bytes, at, 0,
                                    "end attributes[" + i + "]\n");
                }
            } catch (ParseException ex) {
                ex.addContext("...while parsing attributes[" + i + "]");
                throw ex;
            } catch (RuntimeException ex) {
                ParseException pe = new ParseException(ex);
                pe.addContext("...while parsing attributes[" + i + "]");
                throw pe;
            }
        }
        endOffset = at;
    }
}
