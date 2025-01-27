public class TrueTypeGlyphMapper extends CharToGlyphMapper {
    static final char REVERSE_SOLIDUS = 0x005c; 
    static final char JA_YEN = 0x00a5;
    static final char JA_FULLWIDTH_TILDE_CHAR = 0xff5e;
    static final char JA_WAVE_DASH_CHAR = 0x301c;
    static final boolean isJAlocale = Locale.JAPAN.equals(Locale.getDefault());
    private final boolean needsJAremapping;
    private boolean remapJAWaveDash;
    TrueTypeFont font;
    CMap cmap;
    int numGlyphs;
    public TrueTypeGlyphMapper(TrueTypeFont font) {
        this.font = font;
        try {
            cmap = CMap.initialize(font);
        } catch (Exception e) {
            cmap = null;
        }
        if (cmap == null) {
            handleBadCMAP();
        }
        missingGlyph = 0; 
        ByteBuffer buffer = font.getTableBuffer(TrueTypeFont.maxpTag);
        numGlyphs = buffer.getChar(4); 
        if (FontUtilities.isSolaris && isJAlocale && font.supportsJA()) {
            needsJAremapping = true;
            if (FontUtilities.isSolaris8 &&
                getGlyphFromCMAP(JA_WAVE_DASH_CHAR) == missingGlyph) {
                remapJAWaveDash = true;
            }
        } else {
            needsJAremapping = false;
        }
    }
    public int getNumGlyphs() {
        return numGlyphs;
    }
    private char getGlyphFromCMAP(int charCode) {
        try {
            char glyphCode = cmap.getGlyph(charCode);
            if (glyphCode < numGlyphs ||
                glyphCode >= FileFontStrike.INVISIBLE_GLYPHS) {
                return glyphCode;
            } else {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger().warning
                        (font + " out of range glyph id=" +
                         Integer.toHexString((int)glyphCode) +
                         " for char " + Integer.toHexString(charCode));
                }
                return (char)missingGlyph;
            }
        } catch(Exception e) {
            handleBadCMAP();
            return (char) missingGlyph;
        }
    }
    private void handleBadCMAP() {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger().severe("Null Cmap for " + font +
                                      "substituting for this font");
        }
        SunFontManager.getInstance().deRegisterBadFont(font);
        cmap = CMap.theNullCmap;
    }
    private final char remapJAChar(char unicode) {
        switch (unicode) {
        case REVERSE_SOLIDUS:
            return JA_YEN;
        case JA_WAVE_DASH_CHAR:
            if (remapJAWaveDash) {
                return JA_FULLWIDTH_TILDE_CHAR;
            }
        default: return unicode;
        }
    }
    private final int remapJAIntChar(int unicode) {
        switch (unicode) {
        case REVERSE_SOLIDUS:
            return JA_YEN;
        case JA_WAVE_DASH_CHAR:
            if (remapJAWaveDash) {
                return JA_FULLWIDTH_TILDE_CHAR;
            }
        default: return unicode;
        }
    }
    public int charToGlyph(char unicode) {
        if (needsJAremapping) {
            unicode = remapJAChar(unicode);
        }
        int glyph = getGlyphFromCMAP(unicode);
        if (font.checkUseNatives() && glyph < font.glyphToCharMap.length) {
            font.glyphToCharMap[glyph] = unicode;
        }
        return glyph;
    }
    public int charToGlyph(int unicode) {
        if (needsJAremapping) {
            unicode = remapJAIntChar(unicode);
        }
        int glyph = getGlyphFromCMAP(unicode);
        if (font.checkUseNatives() && glyph < font.glyphToCharMap.length) {
            font.glyphToCharMap[glyph] = (char)unicode;
        }
        return glyph;
    }
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i=0;i<count;i++) {
            if (needsJAremapping) {
                glyphs[i] = getGlyphFromCMAP(remapJAIntChar(unicodes[i]));
            } else {
                glyphs[i] = getGlyphFromCMAP(unicodes[i]);
            }
            if (font.checkUseNatives() &&
                glyphs[i] < font.glyphToCharMap.length) {
                font.glyphToCharMap[glyphs[i]] = (char)unicodes[i];
            }
        }
    }
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            int code;
            if (needsJAremapping) {
                code = remapJAChar(unicodes[i]);
            } else {
                code = unicodes[i]; 
            }
            if (code >= HI_SURROGATE_START &&
                code <= HI_SURROGATE_END && i < count - 1) {
                char low = unicodes[i + 1];
                if (low >= LO_SURROGATE_START &&
                    low <= LO_SURROGATE_END) {
                    code = (code - HI_SURROGATE_START) *
                        0x400 + low - LO_SURROGATE_START + 0x10000;
                    glyphs[i] = getGlyphFromCMAP(code);
                    i += 1; 
                    glyphs[i] = INVISIBLE_GLYPH_ID;
                    continue;
                }
            }
            glyphs[i] = getGlyphFromCMAP(code);
            if (font.checkUseNatives() &&
                glyphs[i] < font.glyphToCharMap.length) {
                font.glyphToCharMap[glyphs[i]] = (char)code;
            }
        }
    }
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            int code;
            if (needsJAremapping) {
                code = remapJAChar(unicodes[i]);
            } else {
                code = unicodes[i]; 
            }
            if (code >= HI_SURROGATE_START &&
                code <= HI_SURROGATE_END && i < count - 1) {
                char low = unicodes[i + 1];
                if (low >= LO_SURROGATE_START &&
                    low <= LO_SURROGATE_END) {
                    code = (code - HI_SURROGATE_START) *
                        0x400 + low - LO_SURROGATE_START + 0x10000;
                    glyphs[i + 1] = INVISIBLE_GLYPH_ID;
                }
            }
            glyphs[i] = getGlyphFromCMAP(code);
            if (font.checkUseNatives() &&
                glyphs[i] < font.glyphToCharMap.length) {
                font.glyphToCharMap[glyphs[i]] = (char)code;
            }
            if (code < FontUtilities.MIN_LAYOUT_CHARCODE) {
                continue;
            }
            else if (FontUtilities.isComplexCharCode(code)) {
                return true;
            }
            else if (code >= 0x10000) {
                i += 1; 
                continue;
            }
        }
        return false;
    }
    boolean hasSupplementaryChars() {
        return
            cmap instanceof CMap.CMapFormat8 ||
            cmap instanceof CMap.CMapFormat10 ||
            cmap instanceof CMap.CMapFormat12;
    }
}
