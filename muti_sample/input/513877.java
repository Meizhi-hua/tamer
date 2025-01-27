public class Font implements Serializable {
    private static final long serialVersionUID = -4206021311591459213L;
    private static final TransformAttribute IDENTITY_TRANSFORM = new TransformAttribute(
            new AffineTransform());
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int ROMAN_BASELINE = 0;
    public static final int CENTER_BASELINE = 1;
    public static final int HANGING_BASELINE = 2;
    public static final int TRUETYPE_FONT = 0;
    public static final int TYPE1_FONT = 1;
    public static final int LAYOUT_LEFT_TO_RIGHT = 0;
    public static final int LAYOUT_RIGHT_TO_LEFT = 1;
    public static final int LAYOUT_NO_START_CONTEXT = 2;
    public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;
    static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12); 
    protected String name;
    protected int style;
    protected int size;
    protected float pointSize;
    private boolean transformed;
    private Hashtable<Attribute, Object> fRequestedAttributes;
    private transient FontPeerImpl fontPeer;
    private transient int numGlyphs = -1;
    private transient int missingGlyphCode = -1;
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        numGlyphs = -1;
        missingGlyphCode = -1;
    }
    public Font(Map<? extends Attribute, ?> attributes) {
        Object currAttr;
        this.name = "default"; 
        this.size = 12;
        this.pointSize = 12;
        this.style = Font.PLAIN;
        if (attributes != null) {
            fRequestedAttributes = new Hashtable<Attribute, Object>(attributes);
            currAttr = attributes.get(TextAttribute.SIZE);
            if (currAttr != null) {
                this.pointSize = ((Float)currAttr).floatValue();
                this.size = (int)Math.ceil(this.pointSize);
            }
            currAttr = attributes.get(TextAttribute.POSTURE);
            if (currAttr != null && currAttr.equals(TextAttribute.POSTURE_OBLIQUE)) {
                this.style |= Font.ITALIC;
            }
            currAttr = attributes.get(TextAttribute.WEIGHT);
            if ((currAttr != null)
                    && (((Float)currAttr).floatValue() >= (TextAttribute.WEIGHT_BOLD).floatValue())) {
                this.style |= Font.BOLD;
            }
            currAttr = attributes.get(TextAttribute.FAMILY);
            if (currAttr != null) {
                this.name = (String)currAttr;
            }
            currAttr = attributes.get(TextAttribute.TRANSFORM);
            if (currAttr != null) {
                if (currAttr instanceof TransformAttribute) {
                    this.transformed = !((TransformAttribute)currAttr).getTransform().isIdentity();
                } else if (currAttr instanceof AffineTransform) {
                    this.transformed = !((AffineTransform)currAttr).isIdentity();
                }
            }
        } else {
            fRequestedAttributes = new Hashtable<Attribute, Object>(5);
            fRequestedAttributes.put(TextAttribute.TRANSFORM, IDENTITY_TRANSFORM);
            this.transformed = false;
            fRequestedAttributes.put(TextAttribute.FAMILY, name);
            fRequestedAttributes.put(TextAttribute.SIZE, new Float(this.size));
            if ((this.style & Font.BOLD) != 0) {
                fRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            } else {
                fRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
            }
            if ((this.style & Font.ITALIC) != 0) {
                fRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
            } else {
                fRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
            }
        }
    }
    public Font(String name, int style, int size) {
        this.name = (name != null) ? name : "Default"; 
        this.size = (size >= 0) ? size : 0;
        this.style = (style & ~0x03) == 0 ? style : Font.PLAIN;
        this.pointSize = this.size;
        fRequestedAttributes = new Hashtable<Attribute, Object>(5);
        fRequestedAttributes.put(TextAttribute.TRANSFORM, IDENTITY_TRANSFORM);
        this.transformed = false;
        fRequestedAttributes.put(TextAttribute.FAMILY, this.name);
        fRequestedAttributes.put(TextAttribute.SIZE, new Float(this.size));
        if ((this.style & Font.BOLD) != 0) {
            fRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        } else {
            fRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
        }
        if ((this.style & Font.ITALIC) != 0) {
            fRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        } else {
            fRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
        }
    }
    public boolean canDisplay(char c) {
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        return peer.canDisplay(c);
    }
    public int canDisplayUpTo(char[] text, int start, int limit) {
        int st = start;
        int result;
        while ((st < limit) && canDisplay(text[st])) {
            st++;
        }
        if (st == limit) {
            result = -1;
        } else {
            result = st;
        }
        return result;
    }
    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        int st = start;
        char c = iter.setIndex(start);
        int result;
        while ((st < limit) && (canDisplay(c))) {
            st++;
            c = iter.next();
        }
        if (st == limit) {
            result = -1;
        } else {
            result = st;
        }
        return result;
    }
    public int canDisplayUpTo(String str) {
        char[] chars = str.toCharArray();
        return canDisplayUpTo(chars, 0, chars.length);
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
        return new AndroidGlyphVector(chars, frc, this, 0);
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator iter) {
        throw new RuntimeException("Not implemented!"); 
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes)
            throws org.apache.harmony.luni.util.NotImplementedException {
        throw new RuntimeException("Not implemented!"); 
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, String str) {
        return new AndroidGlyphVector(str.toCharArray(), frc, this, 0);
    }
    private static int getFontStyle(String fontStyleName) {
        int result = Font.PLAIN;
        if (fontStyleName.toUpperCase().equals("BOLDITALIC")) { 
            result = Font.BOLD | Font.ITALIC;
        } else if (fontStyleName.toUpperCase().equals("BOLD")) { 
            result = Font.BOLD;
        } else if (fontStyleName.toUpperCase().equals("ITALIC")) { 
            result = Font.ITALIC;
        }
        return result;
    }
    public static Font decode(String str) {
        if (str == null) {
            return DEFAULT_FONT;
        }
        StringTokenizer strTokens;
        String delim = "-"; 
        String substr;
        int fontSize = DEFAULT_FONT.size;
        int fontStyle = DEFAULT_FONT.style;
        String fontName = DEFAULT_FONT.name;
        strTokens = new StringTokenizer(str.trim(), delim);
        if (strTokens.hasMoreTokens()) {
            fontName = strTokens.nextToken(); 
        }
        if (strTokens.hasMoreTokens()) {
            substr = strTokens.nextToken();
            try {
                fontSize = Integer.parseInt(substr);
            } catch (NumberFormatException e) {
                fontStyle = getFontStyle(substr);
            }
        }
        if (strTokens.hasMoreTokens()) {
            try {
                fontSize = Integer.parseInt(strTokens.nextToken());
            } catch (NumberFormatException e) {
            }
        }
        return new Font(fontName, fontStyle, fontSize);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(AffineTransform trans) {
        if (trans == null) {
            throw new IllegalArgumentException(Messages.getString("awt.94")); 
        }
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        derivefRequestedAttributes.put(TextAttribute.TRANSFORM, new TransformAttribute(trans));
        return new Font(derivefRequestedAttributes);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(float size) {
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        derivefRequestedAttributes.put(TextAttribute.SIZE, new Float(size));
        return new Font(derivefRequestedAttributes);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(int style) {
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        if ((style & Font.BOLD) != 0) {
            derivefRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        } else if (derivefRequestedAttributes.get(TextAttribute.WEIGHT) != null) {
            derivefRequestedAttributes.remove(TextAttribute.WEIGHT);
        }
        if ((style & Font.ITALIC) != 0) {
            derivefRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        } else if (derivefRequestedAttributes.get(TextAttribute.POSTURE) != null) {
            derivefRequestedAttributes.remove(TextAttribute.POSTURE);
        }
        return new Font(derivefRequestedAttributes);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(int style, AffineTransform trans) {
        if (trans == null) {
            throw new IllegalArgumentException(Messages.getString("awt.94")); 
        }
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        if ((style & BOLD) != 0) {
            derivefRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        } else if (derivefRequestedAttributes.get(TextAttribute.WEIGHT) != null) {
            derivefRequestedAttributes.remove(TextAttribute.WEIGHT);
        }
        if ((style & ITALIC) != 0) {
            derivefRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        } else if (derivefRequestedAttributes.get(TextAttribute.POSTURE) != null) {
            derivefRequestedAttributes.remove(TextAttribute.POSTURE);
        }
        derivefRequestedAttributes.put(TextAttribute.TRANSFORM, new TransformAttribute(trans));
        return new Font(derivefRequestedAttributes);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(int style, float size) {
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        if ((style & BOLD) != 0) {
            derivefRequestedAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        } else if (derivefRequestedAttributes.get(TextAttribute.WEIGHT) != null) {
            derivefRequestedAttributes.remove(TextAttribute.WEIGHT);
        }
        if ((style & ITALIC) != 0) {
            derivefRequestedAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        } else if (derivefRequestedAttributes.get(TextAttribute.POSTURE) != null) {
            derivefRequestedAttributes.remove(TextAttribute.POSTURE);
        }
        derivefRequestedAttributes.put(TextAttribute.SIZE, new Float(size));
        return new Font(derivefRequestedAttributes);
    }
    @SuppressWarnings("unchecked")
    public Font deriveFont(Map<? extends Attribute, ?> attributes) {
        Attribute[] avalAttributes = this.getAvailableAttributes();
        Hashtable<Attribute, Object> derivefRequestedAttributes = (Hashtable<Attribute, Object>)fRequestedAttributes
                .clone();
        Object currAttribute;
        for (Attribute element : avalAttributes) {
            currAttribute = attributes.get(element);
            if (currAttribute != null) {
                derivefRequestedAttributes.put(element, currAttribute);
            }
        }
        return new Font(derivefRequestedAttributes);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null) {
            try {
                Font font = (Font)obj;
                return ((this.style == font.style) && (this.size == font.size)
                        && this.name.equals(font.name) && (this.pointSize == font.pointSize) && (this
                        .getTransform()).equals(font.getTransform()));
            } catch (ClassCastException e) {
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public Map<TextAttribute, ?> getAttributes() {
        return (Map<TextAttribute, ?>)fRequestedAttributes.clone();
    }
    public Attribute[] getAvailableAttributes() {
        Attribute[] attrs = {
                TextAttribute.FAMILY, TextAttribute.POSTURE, TextAttribute.SIZE,
                TextAttribute.TRANSFORM, TextAttribute.WEIGHT, TextAttribute.SUPERSCRIPT,
                TextAttribute.WIDTH
        };
        return attrs;
    }
    public byte getBaselineFor(char c) {
        return 0;
    }
    public String getFamily() {
        if (fRequestedAttributes != null) {
            fRequestedAttributes.get(TextAttribute.FAMILY);
        }
        return null;
    }
    public String getFamily(Locale l) {
        if (l == null) {
            throw new NullPointerException(Messages.getString("awt.01", "Locale")); 
        }
        return getFamily();
    }
    public static Font getFont(Map<? extends Attribute, ?> attributes) {
        Font fnt = (Font)attributes.get(TextAttribute.FONT);
        if (fnt != null) {
            return fnt;
        }
        return new Font(attributes);
    }
    public static Font getFont(String sp, Font f) {
        String pr = System.getProperty(sp);
        if (pr == null) {
            return f;
        }
        return decode(pr);
    }
    public static Font getFont(String sp) {
        return getFont(sp, null);
    }
    public String getFontName() {
        if (fRequestedAttributes != null) {
            fRequestedAttributes.get(TextAttribute.FAMILY);
        }
        return null;
    }
    public String getFontName(Locale l) {
        return getFamily();
    }
    public LineMetrics getLineMetrics(char[] chars, int start, int end, FontRenderContext frc) {
        if (frc == null) {
            throw new NullPointerException(Messages.getString("awt.00")); 
        }
        FontMetrics fm = new FontMetricsImpl(this);
        float[] fmet = {
                fm.getAscent(), fm.getDescent(), fm.getLeading()
        };
        return new LineMetricsImpl(chars.length, fmet, null);
    }
    public LineMetrics getLineMetrics(CharacterIterator iter, int start, int end,
            FontRenderContext frc) {
        if (frc == null) {
            throw new NullPointerException(Messages.getString("awt.00")); 
        }
        String resultString;
        int iterCount;
        iterCount = end - start;
        if (iterCount < 0) {
            resultString = ""; 
        } else {
            char[] chars = new char[iterCount];
            int i = 0;
            for (char c = iter.setIndex(start); c != CharacterIterator.DONE && (i < iterCount); c = iter
                    .next()) {
                chars[i] = c;
                i++;
            }
            resultString = new String(chars);
        }
        return this.getLineMetrics(resultString, frc);
    }
    public LineMetrics getLineMetrics(String str, FontRenderContext frc) {
        FontMetrics fm = new FontMetricsImpl(this);
        float[] fmet = {
                fm.getAscent(), fm.getDescent(), fm.getLeading()
        };
        return new LineMetricsImpl(str.length(), fmet, null);
    }
    public LineMetrics getLineMetrics(String str, int start, int end, FontRenderContext frc) {
        return this.getLineMetrics(str.substring(start, end), frc);
    }
    public Rectangle2D getStringBounds(CharacterIterator ci, int start, int end,
            FontRenderContext frc) {
        int first = ci.getBeginIndex();
        int finish = ci.getEndIndex();
        char[] chars;
        if (start < first) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.95", start)); 
        }
        if (end > finish) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.96", end)); 
        }
        if (start > end) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.97", 
                    (end - start)));
        }
        if (frc == null) {
            throw new NullPointerException(Messages.getString("awt.00")); 
        }
        chars = new char[end - start];
        ci.setIndex(start);
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ci.current();
            ci.next();
        }
        return this.getStringBounds(chars, 0, chars.length, frc);
    }
    public Rectangle2D getStringBounds(String str, FontRenderContext frc) {
        char[] chars = str.toCharArray();
        return this.getStringBounds(chars, 0, chars.length, frc);
    }
    public Rectangle2D getStringBounds(String str, int start, int end, FontRenderContext frc) {
        return this.getStringBounds((str.substring(start, end)), frc);
    }
    public Rectangle2D getStringBounds(char[] chars, int start, int end, FontRenderContext frc) {
        if (start < 0) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.95", start)); 
        }
        if (end > chars.length) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.96", end)); 
        }
        if (start > end) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.97", 
                    (end - start)));
        }
        if (frc == null) {
            throw new NullPointerException(Messages.getString("awt.00")); 
        }
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        final int TRANSFORM_MASK = AffineTransform.TYPE_GENERAL_ROTATION
                | AffineTransform.TYPE_GENERAL_TRANSFORM;
        Rectangle2D bounds;
        AffineTransform transform = getTransform();
        if ((transform.getType() & TRANSFORM_MASK) == 0) {
            int width = 0;
            for (int i = start; i < end; i++) {
                width += peer.charWidth(chars[i]);
            }
            LineMetrics nlm = getLineMetrics(chars, start, end, frc);
            bounds = transform.createTransformedShape(
                    new Rectangle2D.Float(0, -nlm.getAscent(), width, nlm.getHeight()))
                    .getBounds2D();
        } else {
            int len = end - start;
            char[] subChars = new char[len];
            System.arraycopy(chars, start, subChars, 0, len);
            bounds = createGlyphVector(frc, subChars).getLogicalBounds();
        }
        return bounds;
    }
    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
        if (frc == null) {
            throw new NullPointerException(Messages.getString("awt.00")); 
        }
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        Rectangle2D bounds = peer.getMaxCharBounds(frc);
        AffineTransform transform = getTransform();
        bounds = transform.createTransformedShape(bounds).getBounds2D();
        return bounds;
    }
    public GlyphVector layoutGlyphVector(FontRenderContext frc, char[] chars, int start, int count,
            int flags) {
        if (start < 0) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.95", 
                    start));
        }
        if (count < 0) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.98", 
                    count));
        }
        if (start + count > chars.length) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.99", 
                    (start + count)));
        }
        char[] out = new char[count];
        System.arraycopy(chars, start, out, 0, count);
        return new CommonGlyphVector(out, frc, this, flags);
    }
    @Override
    public String toString() {
        String stl = "plain"; 
        String result;
        if (this.isBold() && this.isItalic()) {
            stl = "bolditalic"; 
        }
        if (this.isBold() && !this.isItalic()) {
            stl = "bold"; 
        }
        if (!this.isBold() && this.isItalic()) {
            stl = "italic"; 
        }
        result = this.getClass().getName() + "[family=" + this.getFamily() + 
                ",name=" + this.name + 
                ",style=" + stl + 
                ",size=" + this.size + "]"; 
        return result;
    }
    public String getPSName() {
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        return peer.getPSName();
    }
    public String getName() {
        return (this.name);
    }
    @Deprecated
    public java.awt.peer.FontPeer getPeer() {
        if (fontPeer == null) {
            fontPeer = (FontPeerImpl)Toolkit.getDefaultToolkit().getGraphicsFactory().getFontPeer(
                    this);
        }
        return fontPeer;
    }
    public AffineTransform getTransform() {
        Object transform = fRequestedAttributes.get(TextAttribute.TRANSFORM);
        if (transform != null) {
            if (transform instanceof TransformAttribute) {
                return ((TransformAttribute)transform).getTransform();
            }
            if (transform instanceof AffineTransform) {
                return new AffineTransform((AffineTransform)transform);
            }
        } else {
            transform = new AffineTransform();
        }
        return (AffineTransform)transform;
    }
    public boolean isTransformed() {
        return this.transformed;
    }
    public boolean isPlain() {
        return (this.style == PLAIN);
    }
    public boolean isItalic() {
        return (this.style & ITALIC) != 0;
    }
    public boolean isBold() {
        return (this.style & BOLD) != 0;
    }
    public boolean hasUniformLineMetrics() {
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        return peer.hasUniformLineMetrics();
    }
    @Override
    public int hashCode() {
        HashCode hash = new HashCode();
        hash.append(this.name);
        hash.append(this.style);
        hash.append(this.size);
        return hash.hashCode();
    }
    public int getStyle() {
        return this.style;
    }
    public int getSize() {
        return this.size;
    }
    public int getNumGlyphs() {
        if (numGlyphs == -1) {
            FontPeerImpl peer = (FontPeerImpl)this.getPeer();
            this.numGlyphs = peer.getNumGlyphs();
        }
        return this.numGlyphs;
    }
    public int getMissingGlyphCode() {
        if (missingGlyphCode == -1) {
            FontPeerImpl peer = (FontPeerImpl)this.getPeer();
            this.missingGlyphCode = peer.getMissingGlyphCode();
        }
        return this.missingGlyphCode;
    }
    public float getSize2D() {
        return this.pointSize;
    }
    public float getItalicAngle() {
        FontPeerImpl peer = (FontPeerImpl)this.getPeer();
        return peer.getItalicAngle();
    }
    public static Font createFont(int fontFormat, File fontFile) throws FontFormatException,
            IOException {
        InputStream is = new FileInputStream(fontFile);
        try {
            return createFont(fontFormat, is);
        } finally {
            is.close();
        }
    }
    public static Font createFont(int fontFormat, InputStream fontStream)
            throws FontFormatException, IOException {
        BufferedInputStream buffStream;
        int bRead = 0;
        int size = 8192;
        byte buf[] = new byte[size];
        if (fontFormat != TRUETYPE_FONT) { 
            throw new IllegalArgumentException(Messages.getString("awt.9A")); 
        }
        File fontFile = Toolkit.getDefaultToolkit().getGraphicsFactory().getFontManager()
                .getTempFontFile();
        buffStream = new BufferedInputStream(fontStream, 8192);
        FileOutputStream fOutStream = new FileOutputStream(fontFile);
        bRead = buffStream.read(buf, 0, size);
        while (bRead != -1) {
            fOutStream.write(buf, 0, bRead);
            bRead = buffStream.read(buf, 0, size);
        }
        buffStream.close();
        fOutStream.close();
        Font font = null;
        font = Toolkit.getDefaultToolkit().getGraphicsFactory().embedFont(
                fontFile.getAbsolutePath());
        if (font == null) { 
            throw new FontFormatException(Messages.getString("awt.9B")); 
        }
        return font;
    }
}
