public class XRGlyphCache implements GlyphDisposedListener {
    XRBackend con;
    XRCompositeManager maskBuffer;
    HashMap<MutableInteger, XRGlyphCacheEntry> cacheMap = new HashMap<MutableInteger, XRGlyphCacheEntry>(256);
    int nextID = 1;
    MutableInteger tmp = new MutableInteger(0);
    int grayGlyphSet;
    int lcdGlyphSet;
    int time = 0;
    int cachedPixels = 0;
    static final int MAX_CACHED_PIXELS = 100000;
    ArrayList<Integer> freeGlyphIDs = new ArrayList<Integer>(255);
    static final boolean batchGlyphUpload = true; 
    public XRGlyphCache(XRCompositeManager maskBuf) {
        this.con = maskBuf.getBackend();
        this.maskBuffer = maskBuf;
        grayGlyphSet = con.XRenderCreateGlyphSet(XRUtils.PictStandardA8);
        lcdGlyphSet = con.XRenderCreateGlyphSet(XRUtils.PictStandardARGB32);
        StrikeCache.addGlyphDisposedListener(this);
    }
    public void glyphDisposed(ArrayList<Long> glyphPtrList) {
        try {
            SunToolkit.awtLock();
            GrowableIntArray glyphIDList = new GrowableIntArray(1, glyphPtrList.size());
            for (long glyphPtr : glyphPtrList) {
                int glyphID = XRGlyphCacheEntry.getGlyphID(glyphPtr);
                if (glyphID != 0) {
                   glyphIDList.addInt(glyphID);
                }
            }
            freeGlyphs(glyphIDList);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    protected int getFreeGlyphID() {
        if (freeGlyphIDs.size() > 0) {
            int newID = freeGlyphIDs.remove(freeGlyphIDs.size() - 1);
            return newID;
        }
        return nextID++;
    }
    protected XRGlyphCacheEntry getEntryForPointer(long imgPtr) {
        int id = XRGlyphCacheEntry.getGlyphID(imgPtr);
        if (id == 0) {
            return null;
        }
        tmp.setValue(id);
        return cacheMap.get(tmp);
    }
    public XRGlyphCacheEntry[] cacheGlyphs(GlyphList glyphList) {
        time++;
        XRGlyphCacheEntry[] entries = new XRGlyphCacheEntry[glyphList.getNumGlyphs()];
        long[] imgPtrs = glyphList.getImages();
        ArrayList<XRGlyphCacheEntry> uncachedGlyphs = null;
        for (int i = 0; i < glyphList.getNumGlyphs(); i++) {
            XRGlyphCacheEntry glyph;
            if ((glyph = getEntryForPointer(imgPtrs[i])) == null) {
                glyph = new XRGlyphCacheEntry(imgPtrs[i], glyphList);
                glyph.setGlyphID(getFreeGlyphID());
                cacheMap.put(new MutableInteger(glyph.getGlyphID()), glyph);
                if (uncachedGlyphs == null) {
                    uncachedGlyphs = new ArrayList<XRGlyphCacheEntry>();
                }
                uncachedGlyphs.add(glyph);
            }
            glyph.setLastUsed(time);
            entries[i] = glyph;
        }
        if (uncachedGlyphs != null) {
            uploadGlyphs(entries, uncachedGlyphs, glyphList, null);
        }
        return entries;
    }
    protected void uploadGlyphs(XRGlyphCacheEntry[] glyphs, ArrayList<XRGlyphCacheEntry> uncachedGlyphs, GlyphList gl, int[] glIndices) {
        for (XRGlyphCacheEntry glyph : uncachedGlyphs) {
            cachedPixels += glyph.getPixelCnt();
        }
        if (cachedPixels > MAX_CACHED_PIXELS) {
            clearCache(glyphs);
        }
        boolean containsLCDGlyphs = containsLCDGlyphs(uncachedGlyphs);
        List<XRGlyphCacheEntry>[] seperatedGlyphList = seperateGlyphTypes(uncachedGlyphs, containsLCDGlyphs);
        List<XRGlyphCacheEntry> grayGlyphList = seperatedGlyphList[0];
        List<XRGlyphCacheEntry> lcdGlyphList = seperatedGlyphList[1];
        if (batchGlyphUpload) {
            if (grayGlyphList != null && grayGlyphList.size() > 0) {
                con.XRenderAddGlyphs(grayGlyphSet, gl, grayGlyphList, generateGlyphImageStream(grayGlyphList));
            }
            if (lcdGlyphList != null && lcdGlyphList.size() > 0) {
                con.XRenderAddGlyphs(lcdGlyphSet, gl, lcdGlyphList, generateGlyphImageStream(lcdGlyphList));
            }
        } else {
            ArrayList<XRGlyphCacheEntry> tmpList = new ArrayList<XRGlyphCacheEntry>(1);
            tmpList.add(null);
            for (XRGlyphCacheEntry entry : uncachedGlyphs) {
                tmpList.set(0, entry);
                if (entry.getGlyphSet() == grayGlyphSet) {
                    con.XRenderAddGlyphs(grayGlyphSet, gl, tmpList, generateGlyphImageStream(tmpList));
                } else {
                    con.XRenderAddGlyphs(lcdGlyphSet, gl, tmpList, generateGlyphImageStream(tmpList));
                }
            }
        }
    }
    protected List<XRGlyphCacheEntry>[] seperateGlyphTypes(List<XRGlyphCacheEntry> glyphList, boolean containsLCDGlyphs) {
        ArrayList<XRGlyphCacheEntry> lcdGlyphs = null;
        ArrayList<XRGlyphCacheEntry> grayGlyphs = null;
        for (XRGlyphCacheEntry cacheEntry : glyphList) {
            if (cacheEntry.isGrayscale(containsLCDGlyphs)) {
                if (grayGlyphs == null) {
                    grayGlyphs = new ArrayList<XRGlyphCacheEntry>(glyphList.size());
                }
                cacheEntry.setGlyphSet(grayGlyphSet);
                grayGlyphs.add(cacheEntry);
            } else {
                if (lcdGlyphs == null) {
                    lcdGlyphs = new ArrayList<XRGlyphCacheEntry>(glyphList.size());
                }
                cacheEntry.setGlyphSet(lcdGlyphSet);
                lcdGlyphs.add(cacheEntry);
            }
        }
        return new List[] { grayGlyphs, lcdGlyphs };
    }
    protected byte[] generateGlyphImageStream(List<XRGlyphCacheEntry> glyphList) {
        boolean isLCDGlyph = glyphList.get(0).getGlyphSet() == lcdGlyphSet;
        ByteArrayOutputStream stream = new ByteArrayOutputStream((isLCDGlyph ? 4 : 1) * 48 * glyphList.size());
        for (XRGlyphCacheEntry cacheEntry : glyphList) {
            cacheEntry.writePixelData(stream, isLCDGlyph);
        }
        return stream.toByteArray();
    }
    protected boolean containsLCDGlyphs(List<XRGlyphCacheEntry> entries) {
        boolean containsLCDGlyphs = false;
        for (XRGlyphCacheEntry entry : entries) {
            containsLCDGlyphs = !(entry.getSourceRowBytes() == entry.getWidth());
            if (containsLCDGlyphs) {
                return true;
            }
        }
        return false;
    }
    protected void clearCache(XRGlyphCacheEntry[] glyps) {
        ArrayList<XRGlyphCacheEntry> cacheList = new ArrayList<XRGlyphCacheEntry>(cacheMap.values());
        Collections.sort(cacheList, new Comparator<XRGlyphCacheEntry>() {
            public int compare(XRGlyphCacheEntry e1, XRGlyphCacheEntry e2) {
                return e2.getLastUsed() - e1.getLastUsed();
            }
        });
        for (XRGlyphCacheEntry glyph : glyps) {
            glyph.setPinned();
        }
        GrowableIntArray deleteGlyphList = new GrowableIntArray(1, 10);
        int pixelsToRelease = cachedPixels - MAX_CACHED_PIXELS;
        for (int i = cacheList.size() - 1; i >= 0 && pixelsToRelease > 0; i--) {
            XRGlyphCacheEntry entry = cacheList.get(i);
            if (!entry.isPinned()) {
                pixelsToRelease -= entry.getPixelCnt();
                deleteGlyphList.addInt(entry.getGlyphID());
            }
        }
        for (XRGlyphCacheEntry glyph : glyps) {
            glyph.setUnpinned();
        }
        freeGlyphs(deleteGlyphList);
    }
    private void freeGlyphs(GrowableIntArray glyphIdList) {
        GrowableIntArray removedLCDGlyphs = new GrowableIntArray(1, 10);
        GrowableIntArray removedGrayscaleGlyphs = new GrowableIntArray(1, 10);
        for (int i=0; i < glyphIdList.getSize(); i++) {
            int glyphId = glyphIdList.getInt(i);
            freeGlyphIDs.add(glyphId);
            tmp.setValue(glyphId);
            XRGlyphCacheEntry entry = cacheMap.get(tmp);
            cachedPixels -= entry.getPixelCnt();
            cacheMap.remove(tmp);
            if (entry.getGlyphSet() == grayGlyphSet) {
                removedGrayscaleGlyphs.addInt(glyphId);
            } else {
                removedLCDGlyphs.addInt(glyphId);
            }
            entry.setGlyphID(0);
        }
        if (removedGrayscaleGlyphs.getSize() > 0) {
            con.XRenderFreeGlyphs(grayGlyphSet, removedGrayscaleGlyphs.getSizedArray());
        }
        if (removedLCDGlyphs.getSize() > 0) {
            con.XRenderFreeGlyphs(lcdGlyphSet, removedLCDGlyphs.getSizedArray());
        }
    }
}
