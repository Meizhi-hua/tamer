class D3DRenderer extends BufferedRenderPipe {
    D3DRenderer(RenderQueue rq) {
        super(rq);
    }
    @Override
    protected void validateContext(SunGraphics2D sg2d) {
        int ctxflags =
            sg2d.paint.getTransparency() == Transparency.OPAQUE ?
                D3DContext.SRC_IS_OPAQUE : D3DContext.NO_CONTEXT_FLAGS;
        D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
        D3DContext.validateContext(dstData, dstData,
                                   sg2d.getCompClip(), sg2d.composite,
                                   null, sg2d.paint, sg2d, ctxflags);
    }
    @Override
    protected void validateContextAA(SunGraphics2D sg2d) {
        int ctxflags = D3DContext.NO_CONTEXT_FLAGS;
        D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
        D3DContext.validateContext(dstData, dstData,
                                   sg2d.getCompClip(), sg2d.composite,
                                   null, sg2d.paint, sg2d, ctxflags);
    }
    void copyArea(SunGraphics2D sg2d,
                  int x, int y, int w, int h, int dx, int dy)
    {
        rq.lock();
        try {
            int ctxflags =
                sg2d.surfaceData.getTransparency() == Transparency.OPAQUE ?
                    D3DContext.SRC_IS_OPAQUE : D3DContext.NO_CONTEXT_FLAGS;
            D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
            D3DContext.validateContext(dstData, dstData,
                                       sg2d.getCompClip(), sg2d.composite,
                                       null, null, null, ctxflags);
            rq.ensureCapacity(28);
            buf.putInt(COPY_AREA);
            buf.putInt(x).putInt(y).putInt(w).putInt(h);
            buf.putInt(dx).putInt(dy);
        } finally {
            rq.unlock();
        }
    }
    protected native void drawPoly(int[] xPoints, int[] yPoints,
                                   int nPoints, boolean isClosed,
                                   int transX, int transY);
    D3DRenderer traceWrap() {
        return new Tracer(this);
    }
    private class Tracer extends D3DRenderer {
        private D3DRenderer d3dr;
        Tracer(D3DRenderer d3dr) {
            super(d3dr.rq);
            this.d3dr = d3dr;
        }
        public ParallelogramPipe getAAParallelogramPipe() {
            final ParallelogramPipe realpipe = d3dr.getAAParallelogramPipe();
            return new ParallelogramPipe() {
                public void fillParallelogram(SunGraphics2D sg2d,
                                              double ux1, double uy1,
                                              double ux2, double uy2,
                                              double x, double y,
                                              double dx1, double dy1,
                                              double dx2, double dy2)
                {
                    GraphicsPrimitive.tracePrimitive("D3DFillAAParallelogram");
                    realpipe.fillParallelogram(sg2d,
                                               ux1, uy1, ux2, uy2,
                                               x, y, dx1, dy1, dx2, dy2);
                }
                public void drawParallelogram(SunGraphics2D sg2d,
                                              double ux1, double uy1,
                                              double ux2, double uy2,
                                              double x, double y,
                                              double dx1, double dy1,
                                              double dx2, double dy2,
                                              double lw1, double lw2)
                {
                    GraphicsPrimitive.tracePrimitive("D3DDrawAAParallelogram");
                    realpipe.drawParallelogram(sg2d,
                                               ux1, uy1, ux2, uy2,
                                               x, y, dx1, dy1, dx2, dy2,
                                               lw1, lw2);
                }
            };
        }
        protected void validateContext(SunGraphics2D sg2d) {
            d3dr.validateContext(sg2d);
        }
        public void drawLine(SunGraphics2D sg2d,
                             int x1, int y1, int x2, int y2)
        {
            GraphicsPrimitive.tracePrimitive("D3DDrawLine");
            d3dr.drawLine(sg2d, x1, y1, x2, y2);
        }
        public void drawRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("D3DDrawRect");
            d3dr.drawRect(sg2d, x, y, w, h);
        }
        protected void drawPoly(SunGraphics2D sg2d,
                                int[] xPoints, int[] yPoints,
                                int nPoints, boolean isClosed)
        {
            GraphicsPrimitive.tracePrimitive("D3DDrawPoly");
            d3dr.drawPoly(sg2d, xPoints, yPoints, nPoints, isClosed);
        }
        public void fillRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("D3DFillRect");
            d3dr.fillRect(sg2d, x, y, w, h);
        }
        protected void drawPath(SunGraphics2D sg2d,
                                Path2D.Float p2df, int transx, int transy)
        {
            GraphicsPrimitive.tracePrimitive("D3DDrawPath");
            d3dr.drawPath(sg2d, p2df, transx, transy);
        }
        protected void fillPath(SunGraphics2D sg2d,
                                Path2D.Float p2df, int transx, int transy)
        {
            GraphicsPrimitive.tracePrimitive("D3DFillPath");
            d3dr.fillPath(sg2d, p2df, transx, transy);
        }
        protected void fillSpans(SunGraphics2D sg2d, SpanIterator si,
                                 int transx, int transy)
        {
            GraphicsPrimitive.tracePrimitive("D3DFillSpans");
            d3dr.fillSpans(sg2d, si, transx, transy);
        }
        public void fillParallelogram(SunGraphics2D sg2d,
                                      double ux1, double uy1,
                                      double ux2, double uy2,
                                      double x, double y,
                                      double dx1, double dy1,
                                      double dx2, double dy2)
        {
            GraphicsPrimitive.tracePrimitive("D3DFillParallelogram");
            d3dr.fillParallelogram(sg2d,
                                   ux1, uy1, ux2, uy2,
                                   x, y, dx1, dy1, dx2, dy2);
        }
        public void drawParallelogram(SunGraphics2D sg2d,
                                      double ux1, double uy1,
                                      double ux2, double uy2,
                                      double x, double y,
                                      double dx1, double dy1,
                                      double dx2, double dy2,
                                      double lw1, double lw2)
        {
            GraphicsPrimitive.tracePrimitive("D3DDrawParallelogram");
            d3dr.drawParallelogram(sg2d,
                                   ux1, uy1, ux2, uy2,
                                   x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }
        public void copyArea(SunGraphics2D sg2d,
                             int x, int y, int w, int h, int dx, int dy)
        {
            GraphicsPrimitive.tracePrimitive("D3DCopyArea");
            d3dr.copyArea(sg2d, x, y, w, h, dx, dy);
        }
    }
}
