public class AlphaPaintPipe implements CompositePipe {
    static WeakReference cachedLastRaster;
    static WeakReference cachedLastColorModel;
    static WeakReference cachedLastData;
    static class TileContext {
        SunGraphics2D sunG2D;
        PaintContext paintCtxt;
        ColorModel paintModel;
        WeakReference lastRaster;
        WeakReference lastData;
        MaskBlit lastMask;
        Blit     lastBlit;
        SurfaceData dstData;
        public TileContext(SunGraphics2D sg, PaintContext pc) {
            sunG2D = sg;
            paintCtxt = pc;
            paintModel = pc.getColorModel();
            dstData = sg.getSurfaceData();
            synchronized (AlphaPaintPipe.class) {
                if (cachedLastColorModel != null &&
                    cachedLastColorModel.get() == paintModel)
                {
                    this.lastRaster = cachedLastRaster;
                    this.lastData = cachedLastData;
                }
            }
        }
    }
    public Object startSequence(SunGraphics2D sg, Shape s, Rectangle devR,
                                int[] abox) {
        PaintContext paintContext =
            sg.paint.createContext(sg.getDeviceColorModel(),
                                   devR,
                                   s.getBounds2D(),
                                   sg.cloneTransform(),
                                   sg.getRenderingHints());
        return new TileContext(sg, paintContext);
    }
    public boolean needTile(Object context, int x, int y, int w, int h) {
        return true;
    }
    private static final int TILE_SIZE = 32;
    public void renderPathTile(Object ctx,
                               byte[] atile, int offset, int tilesize,
                               int x, int y, int w, int h) {
        TileContext context = (TileContext) ctx;
        PaintContext paintCtxt = context.paintCtxt;
        SunGraphics2D sg = context.sunG2D;
        SurfaceData dstData = context.dstData;
        SurfaceData srcData = null;
        Raster lastRas = null;
        if (context.lastData != null && context.lastRaster != null) {
            srcData = (SurfaceData) context.lastData.get();
            lastRas = (Raster) context.lastRaster.get();
            if (srcData == null || lastRas == null) {
                srcData = null;
                lastRas = null;
            }
        }
        ColorModel paintModel = context.paintModel;
        for (int rely = 0; rely < h; rely += TILE_SIZE) {
            int ty = y + rely;
            int th = Math.min(h-rely, TILE_SIZE);
            for (int relx = 0; relx < w; relx += TILE_SIZE) {
                int tx = x + relx;
                int tw = Math.min(w-relx, TILE_SIZE);
                Raster srcRaster = paintCtxt.getRaster(tx, ty, tw, th);
                if ((srcRaster.getMinX() != 0) || (srcRaster.getMinY() != 0)) {
                    srcRaster = srcRaster.createTranslatedChild(0, 0);
                }
                if (lastRas != srcRaster) {
                    lastRas = srcRaster;
                    context.lastRaster = new WeakReference(lastRas);
                    BufferedImage bImg =
                        new BufferedImage(paintModel,
                                          (WritableRaster) srcRaster,
                                          paintModel.isAlphaPremultiplied(),
                                          null);
                    srcData = BufImgSurfaceData.createData(bImg);
                    context.lastData = new WeakReference(srcData);
                    context.lastMask = null;
                    context.lastBlit = null;
                }
                if (atile == null) {
                    if (context.lastBlit == null) {
                        CompositeType comptype = sg.imageComp;
                        if (CompositeType.SrcOverNoEa.equals(comptype) &&
                            paintModel.getTransparency() == Transparency.OPAQUE)
                        {
                            comptype = CompositeType.SrcNoEa;
                        }
                        context.lastBlit =
                            Blit.getFromCache(srcData.getSurfaceType(),
                                              comptype,
                                              dstData.getSurfaceType());
                    }
                    context.lastBlit.Blit(srcData, dstData,
                                          sg.composite, null,
                                          0, 0, tx, ty, tw, th);
                } else {
                    if (context.lastMask == null) {
                        CompositeType comptype = sg.imageComp;
                        if (CompositeType.SrcOverNoEa.equals(comptype) &&
                            paintModel.getTransparency() == Transparency.OPAQUE)
                        {
                            comptype = CompositeType.SrcNoEa;
                        }
                        context.lastMask =
                            MaskBlit.getFromCache(srcData.getSurfaceType(),
                                                  comptype,
                                                  dstData.getSurfaceType());
                    }
                    int toff = offset + rely * tilesize + relx;
                    context.lastMask.MaskBlit(srcData, dstData,
                                              sg.composite, null,
                                              0, 0, tx, ty, tw, th,
                                              atile, toff, tilesize);
                }
            }
        }
    }
    public void skipTile(Object context, int x, int y) {
        return;
    }
    public void endSequence(Object ctx) {
        TileContext context = (TileContext) ctx;
        if (context.paintCtxt != null) {
            context.paintCtxt.dispose();
        }
        synchronized (AlphaPaintPipe.class) {
            if (context.lastData != null) {
                cachedLastRaster = context.lastRaster;
                if (cachedLastColorModel == null ||
                    cachedLastColorModel.get() != context.paintModel)
                {
                    cachedLastColorModel =
                        new WeakReference(context.paintModel);
                }
                cachedLastData = context.lastData;
            }
        }
    }
}
