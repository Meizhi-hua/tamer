public class CUPSPrinter  {
    private static final String debugPrefix = "CUPSPrinter>> ";
    private static final double PRINTER_DPI = 72.0;
    private boolean initialized;
    private static native String getCupsServer();
    private static native int getCupsPort();
    private static native boolean canConnect(String server, int port);
    private static native boolean initIDs();
    private static synchronized native String[] getMedia(String printer);
    private static synchronized native float[] getPageSizes(String printer);
    private MediaPrintableArea[] cupsMediaPrintables;
    private MediaSizeName[] cupsMediaSNames;
    private CustomMediaSizeName[] cupsCustomMediaSNames;
    private MediaTray[] cupsMediaTrays;
    public  int nPageSizes = 0;
    public  int nTrays = 0;
    private  String[] media;
    private  float[] pageSizes;
    private String printer;
    private static boolean libFound;
    private static String cupsServer = null;
    private static int cupsPort = 0;
    static {
        java.security.AccessController.doPrivileged(
            new sun.security.action.LoadLibraryAction("awt"));
        libFound = initIDs();
        if (libFound) {
           cupsServer = getCupsServer();
           cupsPort = getCupsPort();
        }
    }
    CUPSPrinter (String printerName) {
        if (printerName == null) {
            throw new IllegalArgumentException("null printer name");
        }
        printer = printerName;
        cupsMediaSNames = null;
        cupsMediaPrintables = null;
        cupsMediaTrays = null;
        initialized = false;
        if (!libFound) {
            throw new RuntimeException("cups lib not found");
        } else {
            media =  getMedia(printer);
            if (media == null) {
                throw new RuntimeException("error getting PPD");
            }
            pageSizes = getPageSizes(printer);
            if (pageSizes != null) {
                nPageSizes = pageSizes.length/6;
                nTrays = media.length/2-nPageSizes;
                assert (nTrays >= 0);
            }
        }
    }
    public MediaSizeName[] getMediaSizeNames() {
        initMedia();
        return cupsMediaSNames;
    }
    public CustomMediaSizeName[] getCustomMediaSizeNames() {
        initMedia();
        return cupsCustomMediaSNames;
    }
    public MediaPrintableArea[] getMediaPrintableArea() {
        initMedia();
        return cupsMediaPrintables;
    }
    public MediaTray[] getMediaTrays() {
        initMedia();
        return cupsMediaTrays;
    }
    private synchronized void initMedia() {
        if (initialized) {
            return;
        } else {
            initialized = true;
        }
        if (pageSizes == null) {
            return;
        }
        cupsMediaPrintables = new MediaPrintableArea[nPageSizes];
        cupsMediaSNames = new MediaSizeName[nPageSizes];
        cupsCustomMediaSNames = new CustomMediaSizeName[nPageSizes];
        CustomMediaSizeName msn;
        MediaPrintableArea mpa;
        float length, width, x, y, w, h;
        for (int i=0; i<nPageSizes; i++) {
            width = (float)(pageSizes[i*6]/PRINTER_DPI);
            length = (float)(pageSizes[i*6+1]/PRINTER_DPI);
            x = (float)(pageSizes[i*6+2]/PRINTER_DPI);
            h = (float)(pageSizes[i*6+3]/PRINTER_DPI);
            w = (float)(pageSizes[i*6+4]/PRINTER_DPI);
            y = (float)(pageSizes[i*6+5]/PRINTER_DPI);
            msn = new CustomMediaSizeName(media[i*2], media[i*2+1],
                                          width, length);
            if ((cupsMediaSNames[i] = msn.getStandardMedia()) == null) {
                cupsMediaSNames[i] = msn;
                if ((width > 0.0) && (length > 0.0)) {
                    new MediaSize(width, length,
                                  Size2DSyntax.INCH, msn);
                }
            }
            cupsCustomMediaSNames[i] = msn;
            mpa = null;
            try {
                mpa = new MediaPrintableArea(x, y, w, h,
                                             MediaPrintableArea.INCH);
            } catch (IllegalArgumentException e) {
                if (width > 0 && length > 0) {
                    mpa = new MediaPrintableArea(0, 0, width, length,
                                             MediaPrintableArea.INCH);
                }
            }
            cupsMediaPrintables[i] = mpa;
        }
        cupsMediaTrays = new MediaTray[nTrays];
        MediaTray mt;
        for (int i=0; i<nTrays; i++) {
            mt = new CustomMediaTray(media[(nPageSizes+i)*2],
                                     media[(nPageSizes+i)*2+1]);
            cupsMediaTrays[i] = mt;
        }
    }
    public static String getDefaultPrinter() {
        try {
            URL url = new URL("http", getServer(), getPort(), "");
            final HttpURLConnection urlConnection =
                IPPPrintService.getIPPConnection(url);
            if (urlConnection != null) {
                OutputStream os = (OutputStream)java.security.AccessController.
                    doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            try {
                                return urlConnection.getOutputStream();
                            } catch (Exception e) {
                            }
                            return null;
                        }
                    });
                if (os == null) {
                    return null;
                }
                AttributeClass attCl[] = {
                    AttributeClass.ATTRIBUTES_CHARSET,
                    AttributeClass.ATTRIBUTES_NATURAL_LANGUAGE,
                    new AttributeClass("requested-attributes",
                                       AttributeClass.TAG_KEYWORD,
                                       "printer-name")
                };
                if (IPPPrintService.writeIPPRequest(os,
                                        IPPPrintService.OP_CUPS_GET_DEFAULT,
                                        attCl)) {
                    HashMap defaultMap = null;
                    InputStream is = urlConnection.getInputStream();
                    HashMap[] responseMap = IPPPrintService.readIPPResponse(
                                         is);
                    is.close();
                    if (responseMap.length > 0) {
                        defaultMap = responseMap[0];
                    }
                    if (defaultMap == null) {
                        os.close();
                        urlConnection.disconnect();
                        return null;
                    }
                    AttributeClass attribClass = (AttributeClass)
                        defaultMap.get("printer-name");
                    if (attribClass != null) {
                        String nameStr = attribClass.getStringValue();
                        os.close();
                        urlConnection.disconnect();
                        return nameStr;
                    }
                }
                os.close();
                urlConnection.disconnect();
            }
        } catch (Exception e) {
        }
        return null;
    }
    public static String[] getAllPrinters() {
        try {
            URL url = new URL("http", getServer(), getPort(), "");
            final HttpURLConnection urlConnection =
                IPPPrintService.getIPPConnection(url);
            if (urlConnection != null) {
                OutputStream os = (OutputStream)java.security.AccessController.
                    doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            try {
                                return urlConnection.getOutputStream();
                            } catch (Exception e) {
                            }
                            return null;
                        }
                    });
                if (os == null) {
                    return null;
                }
                AttributeClass attCl[] = {
                    AttributeClass.ATTRIBUTES_CHARSET,
                    AttributeClass.ATTRIBUTES_NATURAL_LANGUAGE,
                    new AttributeClass("requested-attributes",
                                       AttributeClass.TAG_KEYWORD,
                                       "printer-uri-supported")
                };
                if (IPPPrintService.writeIPPRequest(os,
                                IPPPrintService.OP_CUPS_GET_PRINTERS, attCl)) {
                    InputStream is = urlConnection.getInputStream();
                    HashMap[] responseMap =
                        IPPPrintService.readIPPResponse(is);
                    is.close();
                    os.close();
                    urlConnection.disconnect();
                    if (responseMap == null || responseMap.length == 0) {
                        return null;
                    }
                    ArrayList printerNames = new ArrayList();
                    for (int i=0; i< responseMap.length; i++) {
                        AttributeClass attribClass = (AttributeClass)
                            responseMap[i].get("printer-uri-supported");
                        if (attribClass != null) {
                            String nameStr = attribClass.getStringValue();
                            printerNames.add(nameStr);
                        }
                    }
                    return (String[])printerNames.toArray(new String[] {});
                } else {
                    os.close();
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    public static String getServer() {
        return cupsServer;
    }
    public static int getPort() {
        return cupsPort;
    }
    public static boolean isCupsRunning() {
        IPPPrintService.debug_println(debugPrefix+"libFound "+libFound);
        if (libFound) {
            IPPPrintService.debug_println(debugPrefix+"CUPS server "+getServer()+
                                          " port "+getPort());
            return canConnect(getServer(), getPort());
        } else {
            return false;
        }
    }
}
