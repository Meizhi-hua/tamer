    TYPE_CLASS_DEF_ITEM(            0x0006, "class_def_item"),
    TYPE_MAP_LIST(                  0x1000, "map_list"),
    TYPE_TYPE_LIST(                 0x1001, "type_list"),
    TYPE_ANNOTATION_SET_REF_LIST(   0x1002, "annotation_set_ref_list"),
    TYPE_ANNOTATION_SET_ITEM(       0x1003, "annotation_set_item"),
    TYPE_CLASS_DATA_ITEM(           0x2000, "class_data_item"),
    TYPE_CODE_ITEM(                 0x2001, "code_item"),
    TYPE_STRING_DATA_ITEM(          0x2002, "string_data_item"),
    TYPE_DEBUG_INFO_ITEM(           0x2003, "debug_info_item"),
    TYPE_ANNOTATION_ITEM(           0x2004, "annotation_item"),
    TYPE_ENCODED_ARRAY_ITEM(        0x2005, "encoded_array_item"),
    TYPE_ANNOTATIONS_DIRECTORY_ITEM(0x2006, "annotations_directory_item"),
    TYPE_MAP_ITEM(                  -1,     "map_item"),
    TYPE_TYPE_ITEM(                 -1,     "type_item"),
    TYPE_EXCEPTION_HANDLER_ITEM(    -1,     "exception_handler_item"),
    TYPE_ANNOTATION_SET_REF_ITEM(   -1,     "annotation_set_ref_item");
    private final int mapValue;
    private final String typeName;
    private final String humanName;
    private ItemType(int mapValue, String typeName) {
        this.mapValue = mapValue;
        this.typeName = typeName;
        String human = typeName;
        if (human.endsWith("_item")) {
            human = human.substring(0, human.length() - 5);
        }
        this.humanName = human.replace('_', ' ');
    }
    public int getMapValue() {
        return mapValue;
    }
    public String getTypeName() {
        return typeName;
    }
    public String toHuman() {
        return humanName;
    }
}
