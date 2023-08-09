public class ItxtUtf8Test {
    public static final String
    TEXT = "\u24c9\u24d4\u24e7\u24e3" +
      "\ud835\udc13\ud835\udc1e\ud835\udc31\ud835\udc2d" +
      "\u24c9\u24d4\u24e7\u24e3", 
    VERBATIM = "\u24e5\u24d4\u24e1\u24d1\u24d0\u24e3\u24d8\u24dc",
    COMPRESSED = "\u24d2\u24de\u24dc\u24df\u24e1\u24d4\u24e2\u24e2\u24d4\u24d3";
    public static final byte[]
    VBYTES = {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x56, 
        (byte)0x69, (byte)0x54, (byte)0x58, (byte)0x74, 
        (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x62,
        (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6d, 
        (byte)0x00, 
        (byte)0x00, 
        (byte)0x00, 
        (byte)0x78, (byte)0x2d, (byte)0x63, (byte)0x69,
        (byte)0x72, (byte)0x63, (byte)0x6c, (byte)0x65,
        (byte)0x64, 
        (byte)0x00, 
        (byte)0xe2, (byte)0x93, (byte)0xa5, 
        (byte)0xe2, (byte)0x93, (byte)0x94, 
        (byte)0xe2, (byte)0x93, (byte)0xa1, 
        (byte)0xe2, (byte)0x93, (byte)0x91, 
        (byte)0xe2, (byte)0x93, (byte)0x90, 
        (byte)0xe2, (byte)0x93, (byte)0xa3, 
        (byte)0xe2, (byte)0x93, (byte)0x98, 
        (byte)0xe2, (byte)0x93, (byte)0x9c, 
        (byte)0x00, 
        (byte)0xe2, (byte)0x93, (byte)0x89, 
        (byte)0xe2, (byte)0x93, (byte)0x94, 
        (byte)0xe2, (byte)0x93, (byte)0xa7, 
        (byte)0xe2, (byte)0x93, (byte)0xa3, 
        (byte)0xf0, (byte)0x9d, (byte)0x90, (byte)0x93, 
        (byte)0xf0, (byte)0x9d, (byte)0x90, (byte)0x9e, 
        (byte)0xf0, (byte)0x9d, (byte)0x90, (byte)0xb1, 
        (byte)0xf0, (byte)0x9d, (byte)0x90, (byte)0xad, 
        (byte)0xe2, (byte)0x93, (byte)0x89, 
        (byte)0xe2, (byte)0x93, (byte)0x94, 
        (byte)0xe2, (byte)0x93, (byte)0xa7, 
        (byte)0xe2, (byte)0x93, (byte)0xa3, 
        (byte)0xb5, (byte)0xcc, (byte)0x97, (byte)0x56 
    },
    CBYTES = {
        (byte)0x69, (byte)0x54, (byte)0x58, (byte)0x74, 
        (byte)0x63, (byte)0x6f, (byte)0x6d, (byte)0x70,
        (byte)0x72, (byte)0x65, (byte)0x73, (byte)0x73,
        (byte)0x65, (byte)0x64, 
        (byte)0x00, 
        (byte)0x01, 
        (byte)0x00, 
        (byte)0x78, (byte)0x2d, (byte)0x63, (byte)0x69,
        (byte)0x72, (byte)0x63, (byte)0x6c, (byte)0x65,
        (byte)0x64, 
        (byte)0x00, 
    };
    public static void main(String[] args) throws Exception {
        List argList = Arrays.asList(args);
        if (argList.contains("truncate")) {
            try {
                runTest(false, true);
                throw new AssertionError("Expect an error for truncated file");
            }
            catch (IIOException e) {
            }
        }
        else {
            runTest(argList.contains("dump"), false);
        }
    }
    public static void runTest(boolean dump, boolean truncate)
        throws Exception
    {
        String format = "javax_imageio_png_1.0";
        BufferedImage img =
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/png").next();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(os);
        iw.setOutput(ios);
        IIOMetadata meta =
            iw.getDefaultImageMetadata(new ImageTypeSpecifier(img), null);
        DOMImplementationRegistry registry;
        registry = DOMImplementationRegistry.newInstance();
        DOMImplementation impl = registry.getDOMImplementation("XML 3.0");
        Document doc = impl.createDocument(null, format, null);
        Element root, itxt, entry;
        root = doc.getDocumentElement();
        root.appendChild(itxt = doc.createElement("iTXt"));
        itxt.appendChild(entry = doc.createElement("iTXtEntry"));
        entry.setAttribute("keyword", "verbatim");
        entry.setAttribute("compressionFlag", "false");
        entry.setAttribute("compressionMethod", "0");
        entry.setAttribute("languageTag", "x-circled");
        entry.setAttribute("translatedKeyword", VERBATIM);
        entry.setAttribute("text", TEXT);
        itxt.appendChild(entry = doc.createElement("iTXtEntry"));
        entry.setAttribute("keyword", "compressed");
        entry.setAttribute("compressionFlag", "true");
        entry.setAttribute("compressionMethod", "0");
        entry.setAttribute("languageTag", "x-circled");
        entry.setAttribute("translatedKeyword", COMPRESSED);
        entry.setAttribute("text", TEXT);
        meta.mergeTree(format, root);
        iw.write(new IIOImage(img, null, meta));
        iw.dispose();
        byte[] bytes = os.toByteArray();
        if (dump)
            System.out.write(bytes);
        if (findBytes(VBYTES, bytes) < 0)
            throw new AssertionError("verbatim block not found");
        if (findBytes(CBYTES, bytes) < 0)
            throw new AssertionError("compressed block not found");
        int length = bytes.length;
        if (truncate)
            length = findBytes(VBYTES, bytes) + 32;
        ImageReader ir = ImageIO.getImageReader(iw);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes, 0, length);
        ImageInputStream iis = new MemoryCacheImageInputStream(is);
        ir.setInput(iis);
        meta = ir.getImageMetadata(0);
        Node node = meta.getAsTree(format);
        for (node = node.getFirstChild();
             !"iTXt".equals(node.getNodeName());
             node = node.getNextSibling());
        boolean verbatimSeen = false, compressedSeen = false;
        for (node = node.getFirstChild();
             node != null;
             node = node.getNextSibling()) {
            entry = (Element)node;
            String keyword = entry.getAttribute("keyword");
            String translatedKeyword = entry.getAttribute("translatedKeyword");
            String text = entry.getAttribute("text");
            if ("verbatim".equals(keyword)) {
                if (verbatimSeen) throw new AssertionError("Duplicate");
                verbatimSeen = true;
                if (!VERBATIM.equals(translatedKeyword))
                    throw new AssertionError("Wrong translated keyword");
                if (!TEXT.equals(text))
                    throw new AssertionError("Wrong text");
            }
            else if ("compressed".equals(keyword)) {
                if (compressedSeen) throw new AssertionError("Duplicate");
                compressedSeen = true;
                if (!COMPRESSED.equals(translatedKeyword))
                    throw new AssertionError("Wrong translated keyword");
                if (!TEXT.equals(text))
                    throw new AssertionError("Wrong text");
            }
            else {
                throw new AssertionError("Unexpected keyword");
            }
        }
        if (!(verbatimSeen && compressedSeen))
            throw new AssertionError("Missing chunk");
    }
    private static final int findBytes(byte[] needle, byte[] haystack) {
        HAYSTACK: for (int h = 0; h <= haystack.length - needle.length; ++h) {
            for (int n = 0; n < needle.length; ++n) {
                if (needle[n] != haystack[h + n]) {
                    continue HAYSTACK;
                }
            }
            return h;
        }
        return -1;
    }
}
