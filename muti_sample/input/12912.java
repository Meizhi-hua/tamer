public class XIconWindow extends XBaseWindow {
    private final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XIconWindow");
    XDecoratedPeer parent;
    Dimension size;
    long iconPixmap = 0;
    long iconMask = 0;
    int iconWidth = 0;
    int iconHeight = 0;
    XIconWindow(XDecoratedPeer parent) {
        super(new XCreateWindowParams(new Object[] {
            PARENT, parent,
            DELAYED, Boolean.TRUE}));
    }
    void instantPreInit(XCreateWindowParams params) {
        super.instantPreInit(params);
        this.parent = (XDecoratedPeer)params.get(PARENT);
    }
    private XIconSize[] getIconSizes() {
        XToolkit.awtLock();
        try {
            AwtGraphicsConfigData adata = parent.getGraphicsConfigurationData();
            final long screen = adata.get_awt_visInfo().get_screen();
            final long display = XToolkit.getDisplay();
            if (log.isLoggable(PlatformLogger.FINEST)) log.finest(adata.toString());
            long status =
                XlibWrapper.XGetIconSizes(display, XToolkit.getDefaultRootWindow(),
                                          XlibWrapper.larg1, XlibWrapper.iarg1);
            if (status == 0) {
                return null;
            }
            int count = Native.getInt(XlibWrapper.iarg1);
            long sizes_ptr = Native.getLong(XlibWrapper.larg1); 
            log.finest("count = {1}, sizes_ptr = {0}", Long.valueOf(sizes_ptr), Integer.valueOf(count));
            XIconSize[] res = new XIconSize[count];
            for (int i = 0; i < count; i++, sizes_ptr += XIconSize.getSize()) {
                res[i] = new XIconSize(sizes_ptr);
                log.finest("sizes_ptr[{1}] = {0}", res[i], Integer.valueOf(i));
            }
            return res;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private Dimension calcIconSize(int widthHint, int heightHint) {
        if (XWM.getWMID() == XWM.ICE_WM) {
            log.finest("Returning ICE_WM icon size: 16x16");
            return new Dimension(16, 16);
        }
        XIconSize[] sizeList = getIconSizes();
        log.finest("Icon sizes: {0}", sizeList);
        if (sizeList == null) {
            return new Dimension(16, 16);
        }
        boolean found = false;
        int dist = 0xffffffff, newDist, diff = 0, closestHeight, closestWidth;
        int saveWidth = 0, saveHeight = 0;
        for (int i = 0; i < sizeList.length; i++) {
            if (widthHint >= sizeList[i].get_min_width() &&
                widthHint <= sizeList[i].get_max_width() &&
                heightHint >= sizeList[i].get_min_height() &&
                heightHint <= sizeList[i].get_max_height()) {
                found = true;
                if ((((widthHint-sizeList[i].get_min_width())
                      % sizeList[i].get_width_inc()) == 0) &&
                    (((heightHint-sizeList[i].get_min_height())
                      % sizeList[i].get_height_inc()) ==0)) {
                    saveWidth = widthHint;
                    saveHeight = heightHint;
                    dist = 0;
                    break;
                }
                diff = widthHint - sizeList[i].get_min_width();
                if (diff == 0) {
                    closestWidth = widthHint;
                } else {
                    diff = diff%sizeList[i].get_width_inc();
                    closestWidth = widthHint - diff;
                }
                diff = heightHint - sizeList[i].get_min_height();
                if (diff == 0) {
                    closestHeight = heightHint;
                } else {
                    diff = diff%sizeList[i].get_height_inc();
                    closestHeight = heightHint - diff;
                }
                newDist = closestWidth*closestWidth +
                    closestHeight*closestHeight;
                if (dist > newDist) {
                    saveWidth = closestWidth;
                    saveHeight = closestHeight;
                    dist = newDist;
                }
            }
        }
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("found=" + found);
        }
        if (!found) {
            if (log.isLoggable(PlatformLogger.FINEST)) {
                log.finest("widthHint=" + widthHint + ", heightHint=" + heightHint
                           + ", saveWidth=" + saveWidth + ", saveHeight=" + saveHeight
                           + ", max_width=" + sizeList[0].get_max_width()
                           + ", max_height=" + sizeList[0].get_max_height()
                           + ", min_width=" + sizeList[0].get_min_width()
                           + ", min_height=" + sizeList[0].get_min_height());
            }
            if (widthHint  > sizeList[0].get_max_width() ||
                heightHint > sizeList[0].get_max_height())
            {
                int wdiff = widthHint - sizeList[0].get_max_width();
                int hdiff = heightHint - sizeList[0].get_max_height();
                if (log.isLoggable(PlatformLogger.FINEST)) {
                    log.finest("wdiff=" + wdiff + ", hdiff=" + hdiff);
                }
                if (wdiff >= hdiff) { 
                    saveWidth = sizeList[0].get_max_width();
                    saveHeight =
                        (int)(((double)sizeList[0].get_max_width()/widthHint) * heightHint);
                } else {
                    saveWidth =
                        (int)(((double)sizeList[0].get_max_height()/heightHint) * widthHint);
                    saveHeight = sizeList[0].get_max_height();
                }
            } else if (widthHint  < sizeList[0].get_min_width() ||
                       heightHint < sizeList[0].get_min_height())
            {
                saveWidth = (sizeList[0].get_min_width()+sizeList[0].get_max_width())/2;
                saveHeight = (sizeList[0].get_min_height()+sizeList[0].get_max_height())/2;
            } else {
                saveWidth = widthHint;
                saveHeight = widthHint;
            }
        }
        XToolkit.awtLock();
        try {
            XlibWrapper.XFree(sizeList[0].pData);
        } finally {
            XToolkit.awtUnlock();
        }
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("return " + saveWidth + "x" + saveHeight);
        }
        return new Dimension(saveWidth, saveHeight);
    }
    Dimension getIconSize(int widthHint, int heightHint) {
        if (size == null) {
            size = calcIconSize(widthHint, heightHint);
        }
        return size;
    }
   void replaceImage(Image img)
    {
        if (parent == null) {
            return;
        }
        BufferedImage bi = null;
        if (img != null && iconWidth != 0 && iconHeight != 0) {
            GraphicsConfiguration defaultGC = parent.getGraphicsConfiguration().getDevice().getDefaultConfiguration();
            ColorModel model = defaultGC.getColorModel();
            WritableRaster raster = model.createCompatibleWritableRaster(iconWidth, iconHeight);
            bi = new BufferedImage(model, raster, model.isAlphaPremultiplied(), null);
            Graphics g = bi.getGraphics();
            try {
                g.setColor(SystemColor.window);
                g.fillRect(0, 0, iconWidth, iconHeight);
                if (g instanceof Graphics2D) {
                    ((Graphics2D)g).setComposite(AlphaComposite.Src);
                }
                g.drawImage(img, 0, 0, iconWidth, iconHeight, null);
            } finally {
                g.dispose();
            }
        }
        XToolkit.awtLock();
        try {
            if (iconPixmap != 0) {
                XlibWrapper.XFreePixmap(XToolkit.getDisplay(), iconPixmap);
                iconPixmap = 0;
                log.finest("Freed previous pixmap");
            }
            if (bi == null || iconWidth == 0 || iconHeight == 0) {
                return;  
            }
            AwtGraphicsConfigData adata = parent.getGraphicsConfigurationData();
            awtImageData awtImage = adata.get_awtImage(0);
            XVisualInfo visInfo = adata.get_awt_visInfo();
            iconPixmap = XlibWrapper.XCreatePixmap(XToolkit.getDisplay(),
                                                   XlibWrapper.RootWindow(XToolkit.getDisplay(), visInfo.get_screen()),
                                                   iconWidth,
                                                   iconHeight,
                                                   awtImage.get_Depth()
                                                   );
            if (iconPixmap == 0) {
                log.finest("Can't create new pixmap for icon");
                return; 
            }
            long bytes = 0;
            DataBuffer srcBuf = bi.getData().getDataBuffer();
            if (srcBuf instanceof DataBufferByte) {
                byte[] buf = ((DataBufferByte)srcBuf).getData();
                ColorData cdata = adata.get_color_data(0);
                int num_colors = cdata.get_awt_numICMcolors();
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = (buf[i] >= num_colors) ?
                        0 : cdata.get_awt_icmLUT2Colors(buf[i]);
                }
                bytes = Native.toData(buf);
            } else if (srcBuf instanceof DataBufferInt) {
                bytes = Native.toData(((DataBufferInt)srcBuf).getData());
            } else if (srcBuf instanceof DataBufferUShort) {
                bytes = Native.toData(((DataBufferUShort)srcBuf).getData());
            } else {
                throw new IllegalArgumentException("Unknown data buffer: " + srcBuf);
            }
            int bpp = awtImage.get_wsImageFormat().get_bits_per_pixel();
            int slp =awtImage.get_wsImageFormat().get_scanline_pad();
            int bpsl = paddedwidth(iconWidth*bpp, slp) >> 3;
            if (((bpsl << 3) / bpp) < iconWidth) {
                log.finest("Image format doesn't fit to icon width");
                return;
            }
            long dst = XlibWrapper.XCreateImage(XToolkit.getDisplay(),
                                                visInfo.get_visual(),
                                                (int)awtImage.get_Depth(),
                                                (int)XConstants.ZPixmap,
                                                0,
                                                bytes,
                                                iconWidth,
                                                iconHeight,
                                                32,
                                                bpsl);
            if (dst == 0) {
                log.finest("Can't create XImage for icon");
                XlibWrapper.XFreePixmap(XToolkit.getDisplay(), iconPixmap);
                iconPixmap = 0;
                return;
            } else {
                log.finest("Created XImage for icon");
            }
            long gc = XlibWrapper.XCreateGC(XToolkit.getDisplay(), iconPixmap, 0, 0);
            if (gc == 0) {
                log.finest("Can't create GC for pixmap");
                XlibWrapper.XFreePixmap(XToolkit.getDisplay(), iconPixmap);
                iconPixmap = 0;
                return;
            } else {
                log.finest("Created GC for pixmap");
            }
            try {
                XlibWrapper.XPutImage(XToolkit.getDisplay(), iconPixmap, gc,
                                      dst, 0, 0, 0, 0, iconWidth, iconHeight);
            } finally {
                XlibWrapper.XFreeGC(XToolkit.getDisplay(), gc);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void replaceMask(Image img) {
        if (parent == null) {
            return;
        }
        BufferedImage bi = null;
        if (img != null && iconWidth != 0 && iconHeight != 0) {
            bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.getGraphics();
            try {
                g.drawImage(img, 0, 0, iconWidth, iconHeight, null);
            } finally {
                g.dispose();
            }
        }
        XToolkit.awtLock();
        try {
            if (iconMask != 0) {
                XlibWrapper.XFreePixmap(XToolkit.getDisplay(), iconMask);
                iconMask = 0;
                log.finest("Freed previous mask");
            }
            if (bi == null || iconWidth == 0 || iconHeight == 0) {
                return;  
            }
            AwtGraphicsConfigData adata = parent.getGraphicsConfigurationData();
            awtImageData awtImage = adata.get_awtImage(0);
            XVisualInfo visInfo = adata.get_awt_visInfo();
            ColorModel cm = bi.getColorModel();
            DataBuffer srcBuf = bi.getRaster().getDataBuffer();
            int sidx = 0;
            int bpl = (iconWidth + 7) >> 3;
            byte[] destBuf = new byte[bpl * iconHeight];
            int didx = 0;
            for (int i = 0; i < iconHeight; i++) {
                int dbit = 0;
                int cv = 0;
                for (int j = 0; j < iconWidth; j++) {
                    if (cm.getAlpha(srcBuf.getElem(sidx)) != 0 ) {
                        cv = cv + (1 << dbit);
                    }
                    dbit++;
                    if (dbit == 8) {
                        destBuf[didx] = (byte)cv;
                        cv = 0;
                        dbit = 0;
                        didx++;
                    }
                    sidx++;
                }
            }
            iconMask = XlibWrapper.XCreateBitmapFromData(XToolkit.getDisplay(),
                XlibWrapper.RootWindow(XToolkit.getDisplay(), visInfo.get_screen()),
                Native.toData(destBuf),
                iconWidth, iconHeight);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void setIconImages(java.util.List<XIconInfo> icons) {
        if (icons == null || icons.size() == 0) return;
        int minDiff = Integer.MAX_VALUE;
        Image min = null;
        for (XIconInfo iconInfo : icons) {
            if (iconInfo.isValid()) {
                Image image = iconInfo.getImage();
                Dimension dim = calcIconSize(image.getWidth(null), image.getHeight(null));
                int widthDiff = Math.abs(dim.width - image.getWidth(null));
                int heightDiff = Math.abs(image.getHeight(null) - dim.height);
                if (minDiff >= (widthDiff + heightDiff)) {
                    minDiff = (widthDiff + heightDiff);
                    min = image;
                }
            }
        }
        if (min != null) {
            log.finer("Icon: {0}x{1}", min.getWidth(null), min.getHeight(null));
            setIconImage(min);
        }
    }
    void setIconImage(Image img) {
        if (img == null) {
            replaceImage(null);
            replaceMask(null);
        } else {
            int width;
            int height;
            if (img instanceof ToolkitImage) {
                ImageRepresentation ir = ((ToolkitImage)img).getImageRep();
                ir.reconstruct(ImageObserver.ALLBITS);
                width = ir.getWidth();
                height = ir.getHeight();
            }
            else {
                width = img.getWidth(null);
                height = img.getHeight(null);
            }
            Dimension iconSize = getIconSize(width, height);
            if (iconSize != null) {
                log.finest("Icon size: {0}", iconSize);
                iconWidth = iconSize.width;
                iconHeight = iconSize.height;
            } else {
                log.finest("Error calculating image size");
                iconWidth = 0;
                iconHeight = 0;
            }
            replaceImage(img);
            replaceMask(img);
        }
        XToolkit.awtLock();
        try {
            AwtGraphicsConfigData adata = parent.getGraphicsConfigurationData();
            awtImageData awtImage = adata.get_awtImage(0);
            XVisualInfo visInfo = adata.get_awt_visInfo();
            XWMHints hints = parent.getWMHints();
            window = hints.get_icon_window();
            if (window == 0) {
                log.finest("Icon window wasn't set");
                XCreateWindowParams params = getDelayedParams();
                params.add(BORDER_PIXEL, Long.valueOf(XToolkit.getAwtDefaultFg()));
                params.add(BACKGROUND_PIXMAP, iconPixmap);
                params.add(COLORMAP, adata.get_awt_cmap());
                params.add(DEPTH, awtImage.get_Depth());
                params.add(VISUAL_CLASS, (int)XConstants.InputOutput);
                params.add(VISUAL, visInfo.get_visual());
                params.add(VALUE_MASK, XConstants.CWBorderPixel | XConstants.CWColormap | XConstants.CWBackPixmap);
                params.add(PARENT_WINDOW, XlibWrapper.RootWindow(XToolkit.getDisplay(), visInfo.get_screen()));
                params.add(BOUNDS, new Rectangle(0, 0, iconWidth, iconHeight));
                params.remove(DELAYED);
                init(params);
                if (getWindow() == 0) {
                    log.finest("Can't create new icon window");
                } else {
                    log.finest("Created new icon window");
                }
            }
            if (getWindow() != 0) {
                XlibWrapper.XSetWindowBackgroundPixmap(XToolkit.getDisplay(), getWindow(), iconPixmap);
                XlibWrapper.XClearWindow(XToolkit.getDisplay(), getWindow());
            }
            long newFlags = hints.get_flags() | XUtilConstants.IconPixmapHint | XUtilConstants.IconMaskHint;
            if (getWindow()  != 0) {
                newFlags |= XUtilConstants.IconWindowHint;
            }
            hints.set_flags(newFlags);
            hints.set_icon_pixmap(iconPixmap);
            hints.set_icon_mask(iconMask);
            hints.set_icon_window(getWindow());
            XlibWrapper.XSetWMHints(XToolkit.getDisplay(), parent.getShell(), hints.pData);
            log.finest("Set icon window hint");
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static int paddedwidth(int number, int boundary)
    {
        return (((number) + ((boundary) - 1)) & (~((boundary) - 1)));
    }
}
