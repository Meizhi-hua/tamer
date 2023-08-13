public class ShortHistogramTest {
    public static void main(String[] args) throws IOException {
        int numColors = 15;
        if (args.length > 0) {
            try {
                numColors = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number of colors: " + args[0]);
            }
        }
        System.out.println("Test number of colors: " + numColors);
        ShortHistogramTest t = new ShortHistogramTest(numColors);
        t.doTest();
    }
    int numColors;
    public ShortHistogramTest(int numColors) {
        this.numColors = numColors;
    }
    public void doTest() throws IOException {
        BufferedImage bi = createTestImage(numColors);
        File f = writeImageWithHist(bi);
        System.out.println("Test file is " + f.getCanonicalPath());
        try {
            ImageIO.read(f);
        } catch (IOException e) {
            throw new RuntimeException("Test FAILED!", e);
        }
        System.out.println("Test PASSED!");
    }
    protected File writeImageWithHist(BufferedImage bi) throws IOException {
        File f = File.createTempFile("hist_", ".png", new File("."));
        ImageWriter writer = ImageIO.getImageWritersByFormatName("PNG").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        ImageTypeSpecifier type = new ImageTypeSpecifier(bi);
        IIOMetadata imgMetadata = writer.getDefaultImageMetadata(type, param);
        imgMetadata = upgradeMetadata(imgMetadata, bi);
        IIOImage iio_img = new IIOImage(bi,
                                        null, 
                                        imgMetadata);
        writer.write(iio_img);
        ios.flush();
        ios.close();
        return f;
    }
    private IIOMetadata upgradeMetadata(IIOMetadata src, BufferedImage bi) {
        String format = src.getNativeMetadataFormatName();
        System.out.println("Native format: " + format);
        Node root = src.getAsTree(format);
        Node n = lookupChildNode(root, "hIST");
        if (n == null) {
            System.out.println("Appending new hIST node...");
            Node hIST = gethISTNode(bi);
            root.appendChild(hIST);
        }
        System.out.println("Upgraded metadata tree:");
        dump(root, "");
        System.out.println("Merging metadata...");
        try {
            src.mergeTree(format, root);
        } catch (IIOInvalidTreeException e) {
            throw new RuntimeException("Test FAILED!", e);
        }
        return src;
    }
    private IIOMetadataNode gethISTNode(BufferedImage bi) {
        IndexColorModel icm = (IndexColorModel)bi.getColorModel();
        int mapSize = icm.getMapSize();
        int[] hist = new int[mapSize];
        Arrays.fill(hist, 0);
        Raster r = bi.getData();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int s = r.getSample(x, y, 0);
                hist[s] ++;
            }
        }
        IIOMetadataNode hIST = new IIOMetadataNode("hIST");
        for (int i = 0; i < hist.length; i++) {
            IIOMetadataNode n = new IIOMetadataNode("hISTEntry");
            n.setAttribute("index", "" + i);
            n.setAttribute("value", "" + hist[i]);
            hIST.appendChild(n);
        }
        return hIST;
    }
    private static Node lookupChildNode(Node root, String name) {
        Node n = root.getFirstChild();
        while (n != null && !name.equals(n.getNodeName())) {
            n = n.getNextSibling();
        }
        return n;
    }
    private static void dump(Node node, String ident) {
        if (node == null) {
            return;
        }
        System.out.printf("%s%s\n", ident, node.getNodeName());
        NamedNodeMap attribs = node.getAttributes();
        if (attribs != null) {
            for (int i = 0; i < attribs.getLength(); i++) {
                Node a = attribs.item(i);
                System.out.printf("%s  %s: %s\n", ident,
                        a.getNodeName(), a.getNodeValue());
            }
        }
        dump(node.getFirstChild(), ident + "    ");
        dump(node.getNextSibling(), ident);
    }
    protected BufferedImage createTestImage(int numColors) {
        IndexColorModel icm = createTestICM(numColors);
        int w = numColors * 10;
        int h = 20;
        BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_BYTE_INDEXED, icm);
        Graphics2D g = img.createGraphics();
        for (int i = 0; i < numColors; i++) {
            int rgb = icm.getRGB(i);
            g.setColor(new Color(rgb));
            g.fillRect(i * 10, 0, w - i * 10, h);
        }
        g.dispose();
       return img;
    }
    protected IndexColorModel createTestICM(int numColors) {
        int[] palette = createTestPalette(numColors);
        int numBits = getNumBits(numColors);
        IndexColorModel icm = new IndexColorModel(numBits, numColors,
                palette, 0, false, -1,
                DataBuffer.TYPE_BYTE);
        return icm;
    }
    protected static int getNumBits(int numColors) {
        if (numColors < 0 || 256 < numColors) {
            throw new RuntimeException("Unsupported number of colors: " +
                                       numColors);
        }
        int numBits = 1;
        int limit = 1 << numBits;
        while (numColors > limit) {
            numBits++;
            limit = 1 << numBits;
        }
        return numBits;
    }
    private static Random rnd = new Random();
    protected static int[] createTestPalette(int numColors) {
        int[] palette = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            int r = rnd.nextInt(256);
            int g = rnd.nextInt(256);
            int b = rnd.nextInt(256);
            palette[i] = 0xff000000 | (r << 16) | (g << 8) | b;
        }
        return palette;
    }
}
