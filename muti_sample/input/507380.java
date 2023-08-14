public class RenderableImageOp implements RenderableImage {
    ContextualRenderedImageFactory CRIF;
    ParameterBlock paramBlock;
    float minX, minY, width, height;
    public RenderableImageOp(ContextualRenderedImageFactory CRIF, ParameterBlock paramBlock) {
        this.CRIF = CRIF;
        this.paramBlock = (ParameterBlock)paramBlock.clone();
        Rectangle2D r = CRIF.getBounds2D(paramBlock);
        minX = (float)r.getMinX();
        minY = (float)r.getMinY();
        width = (float)r.getWidth();
        height = (float)r.getHeight();
    }
    public Object getProperty(String name) {
        return CRIF.getProperty(paramBlock, name);
    }
    public ParameterBlock setParameterBlock(ParameterBlock paramBlock) {
        ParameterBlock oldParam = this.paramBlock;
        this.paramBlock = (ParameterBlock)paramBlock.clone();
        return oldParam;
    }
    public RenderedImage createRendering(RenderContext renderContext) {
        Vector<RenderableImage> sources = getSources();
        ParameterBlock rdParam = (ParameterBlock)paramBlock.clone();
        if (sources != null) {
            Vector<Object> rdSources = new Vector<Object>();
            int i = 0;
            while (i < sources.size()) {
                RenderContext newContext = CRIF
                        .mapRenderContext(i, renderContext, paramBlock, this);
                RenderedImage rdim = sources.elementAt(i).createRendering(newContext);
                if (rdim != null) {
                    rdSources.addElement(rdim);
                }
                i++;
            }
            if (rdSources.size() > 0) {
                rdParam.setSources(rdSources);
            }
        }
        return CRIF.create(renderContext, rdParam);
    }
    public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) {
        if (w == 0 && h == 0) {
            throw new IllegalArgumentException(Messages.getString("awt.60")); 
        }
        if (w == 0) {
            w = Math.round(h * (getWidth() / getHeight()));
        }
        if (h == 0) {
            h = Math.round(w * (getHeight() / getWidth()));
        }
        double sx = (double)w / getWidth();
        double sy = (double)h / getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(sx, sy);
        RenderContext context = new RenderContext(at, hints);
        return createRendering(context);
    }
    public Vector<RenderableImage> getSources() {
        if (paramBlock.getNumSources() == 0) {
            return null;
        }
        Vector<RenderableImage> v = new Vector<RenderableImage>();
        int i = 0;
        while (i < paramBlock.getNumSources()) {
            Object o = paramBlock.getSource(i);
            if (o instanceof RenderableImage) {
                v.addElement((RenderableImage)o);
            }
            i++;
        }
        return v;
    }
    public String[] getPropertyNames() {
        return CRIF.getPropertyNames();
    }
    public ParameterBlock getParameterBlock() {
        return paramBlock;
    }
    public RenderedImage createDefaultRendering() {
        AffineTransform at = new AffineTransform();
        RenderContext context = new RenderContext(at);
        return createRendering(context);
    }
    public boolean isDynamic() {
        return CRIF.isDynamic();
    }
    public float getWidth() {
        return width;
    }
    public float getMinY() {
        return minY;
    }
    public float getMinX() {
        return minX;
    }
    public float getHeight() {
        return height;
    }
}