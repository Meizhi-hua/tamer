public final class TypeListItem extends OffsettedItem {
    private static final int ALIGNMENT = 4;
    private static final int ELEMENT_SIZE = 2;
    private static final int HEADER_SIZE = 4;
    private final TypeList list;
    public TypeListItem(TypeList list) {
        super(ALIGNMENT, (list.size() * ELEMENT_SIZE) + HEADER_SIZE);
        this.list = list;
    }
    @Override
    public int hashCode() {
        return StdTypeList.hashContents(list);
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_TYPE_LIST;
    }
    public void addContents(DexFile file) {
        TypeIdsSection typeIds = file.getTypeIds();
        int sz = list.size();
        for (int i = 0; i < sz; i++) {
            typeIds.intern(list.getType(i));
        }
    }
    @Override
    public String toHuman() {
        throw new RuntimeException("unsupported");
    }
    public TypeList getList() {
        return list;
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        TypeIdsSection typeIds = file.getTypeIds();
        int sz = list.size();
        if (out.annotates()) {
            out.annotate(0, offsetString() + " type_list");
            out.annotate(HEADER_SIZE, "  size: " + Hex.u4(sz));
            for (int i = 0; i < sz; i++) {
                Type one = list.getType(i);
                int idx = typeIds.indexOf(one);
                out.annotate(ELEMENT_SIZE,
                             "  " + Hex.u2(idx) + " 
            }
        }
        out.writeInt(sz);
        for (int i = 0; i < sz; i++) {
            out.writeShort(typeIds.indexOf(list.getType(i)));
        }
    }    
    @Override
    protected int compareTo0(OffsettedItem other) {
        TypeList thisList = this.list;
        TypeList otherList = ((TypeListItem) other).list;
        return StdTypeList.compareContents(thisList, otherList);
    }
}
