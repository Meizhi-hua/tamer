public class WBMPMetadataFormat extends IIOMetadataFormatImpl {
    private static IIOMetadataFormat instance = null;
    private WBMPMetadataFormat() {
        super(WBMPMetadata.nativeMetadataFormatName,
              CHILD_POLICY_SOME);
        addElement("ImageDescriptor",
                   WBMPMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("ImageDescriptor", "WBMPType",
                     DATATYPE_INTEGER, true, "0");
        addAttribute("ImageDescriptor", "Width",
                     DATATYPE_INTEGER, true, null,
                     "0", "65535", true, true);
        addAttribute("ImageDescriptor", "Height",
                     DATATYPE_INTEGER, true, null,
                     "1", "65535", true, true);
    }
    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
        return true;
    }
    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new WBMPMetadataFormat();
        }
        return instance;
    }
}
