public class UnixPrintService implements PrintService, AttributeUpdater,
                                         SunPrinterJobService {
    private static String encoding = "ISO8859_1";
    private static DocFlavor textByteFlavor;
    private static DocFlavor[] supportedDocFlavors = null;
    private static final DocFlavor[] supportedDocFlavorsInit = {
         DocFlavor.BYTE_ARRAY.POSTSCRIPT,
         DocFlavor.INPUT_STREAM.POSTSCRIPT,
         DocFlavor.URL.POSTSCRIPT,
         DocFlavor.BYTE_ARRAY.GIF,
         DocFlavor.INPUT_STREAM.GIF,
         DocFlavor.URL.GIF,
         DocFlavor.BYTE_ARRAY.JPEG,
         DocFlavor.INPUT_STREAM.JPEG,
         DocFlavor.URL.JPEG,
         DocFlavor.BYTE_ARRAY.PNG,
         DocFlavor.INPUT_STREAM.PNG,
         DocFlavor.URL.PNG,
         DocFlavor.CHAR_ARRAY.TEXT_PLAIN,
         DocFlavor.READER.TEXT_PLAIN,
         DocFlavor.STRING.TEXT_PLAIN,
         DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8,
         DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16,
         DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16BE,
         DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE,
         DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII,
         DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8,
         DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16,
         DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16BE,
         DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16LE,
         DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII,
         DocFlavor.URL.TEXT_PLAIN_UTF_8,
         DocFlavor.URL.TEXT_PLAIN_UTF_16,
         DocFlavor.URL.TEXT_PLAIN_UTF_16BE,
         DocFlavor.URL.TEXT_PLAIN_UTF_16LE,
         DocFlavor.URL.TEXT_PLAIN_US_ASCII,
         DocFlavor.SERVICE_FORMATTED.PAGEABLE,
         DocFlavor.SERVICE_FORMATTED.PRINTABLE,
         DocFlavor.BYTE_ARRAY.AUTOSENSE,
         DocFlavor.URL.AUTOSENSE,
         DocFlavor.INPUT_STREAM.AUTOSENSE
    };
    private static final DocFlavor[] supportedHostDocFlavors = {
        DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST,
        DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST,
        DocFlavor.URL.TEXT_PLAIN_HOST
    };
    String[] lpcStatusCom = {
      "",
      "| grep -E '^[ 0-9a-zA-Z_-]*@' | awk '{print $2, $3}'"
    };
    String[] lpcQueueCom = {
      "",
      "| grep -E '^[ 0-9a-zA-Z_-]*@' | awk '{print $4}'"
    };
    static {
        encoding = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("file.encoding"));
    }
    private static final Class[] serviceAttrCats = {
        PrinterName.class,
        PrinterIsAcceptingJobs.class,
        QueuedJobCount.class,
    };
    private static final Class[] otherAttrCats = {
        Chromaticity.class,
        Copies.class,
        Destination.class,
        Fidelity.class,
        JobName.class,
        JobSheets.class,
        Media.class, 
        MediaPrintableArea.class,
        OrientationRequested.class,
        PageRanges.class,
        RequestingUserName.class,
        SheetCollate.class,
        Sides.class,
    };
    private static int MAXCOPIES = 1000;
    private static final MediaSizeName mediaSizes[] = {
        MediaSizeName.NA_LETTER,
        MediaSizeName.TABLOID,
        MediaSizeName.LEDGER,
        MediaSizeName.NA_LEGAL,
        MediaSizeName.EXECUTIVE,
        MediaSizeName.ISO_A3,
        MediaSizeName.ISO_A4,
        MediaSizeName.ISO_A5,
        MediaSizeName.ISO_B4,
        MediaSizeName.ISO_B5,
    };
    private String printer;
    private PrinterName name;
    private boolean isInvalid;
    transient private PrintServiceAttributeSet lastSet;
    transient private ServiceNotifier notifier = null;
    UnixPrintService(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null printer name");
        }
        printer = name;
        isInvalid = false;
    }
    public void invalidateService() {
        isInvalid = true;
    }
    public String getName() {
        return printer;
    }
    private PrinterName getPrinterName() {
        if (name == null) {
            name = new PrinterName(printer, null);
        }
        return name;
    }
    private PrinterIsAcceptingJobs getPrinterIsAcceptingJobsSysV() {
        String command = "/usr/bin/lpstat -a " + printer;
        String results[]= UnixPrintServiceLookup.execCmd(command);
        if (results != null && results.length > 0) {
            if (results[0].startsWith(printer + " accepting requests")) {
                return PrinterIsAcceptingJobs.ACCEPTING_JOBS;
            }
            else if (results[0].startsWith(printer)) {
                int index = printer.length();
                String str = results[0];
                if (str.length() > index &&
                    str.charAt(index) == '@' &&
                    str.indexOf(" accepting requests", index) > 0 &&
                    str.indexOf(" not accepting requests", index) == -1) {
                   return PrinterIsAcceptingJobs.ACCEPTING_JOBS;
                }
            }
        }
        return PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS ;
    }
    private PrinterIsAcceptingJobs getPrinterIsAcceptingJobsBSD() {
        if (UnixPrintServiceLookup.cmdIndex ==
            UnixPrintServiceLookup.UNINITIALIZED) {
            UnixPrintServiceLookup.cmdIndex =
                UnixPrintServiceLookup.getBSDCommandIndex();
        }
        String command = "/usr/sbin/lpc status " + printer
            + lpcStatusCom[UnixPrintServiceLookup.cmdIndex];
        String results[]= UnixPrintServiceLookup.execCmd(command);
        if (results != null && results.length > 0) {
            if (UnixPrintServiceLookup.cmdIndex ==
                UnixPrintServiceLookup.BSD_LPD_NG) {
                if (results[0].startsWith("enabled enabled")) {
                    return PrinterIsAcceptingJobs.ACCEPTING_JOBS ;
                }
            } else {
                if ((results[1].trim().startsWith("queuing is enabled") &&
                    results[2].trim().startsWith("printing is enabled")) ||
                    (results.length >= 4 &&
                     results[2].trim().startsWith("queuing is enabled") &&
                     results[3].trim().startsWith("printing is enabled"))) {
                    return PrinterIsAcceptingJobs.ACCEPTING_JOBS ;
                }
            }
        }
        return PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS ;
    }
    private PrinterIsAcceptingJobs getPrinterIsAcceptingJobs() {
        if (UnixPrintServiceLookup.isSysV()) {
            return getPrinterIsAcceptingJobsSysV();
        } else if (UnixPrintServiceLookup.isBSD()) {
            return getPrinterIsAcceptingJobsBSD();
        } else {
            return PrinterIsAcceptingJobs.ACCEPTING_JOBS;
        }
    }
    private PrinterState getPrinterState() {
        if (isInvalid) {
            return PrinterState.STOPPED;
        } else {
            return null;
        }
    }
    private PrinterStateReasons getPrinterStateReasons() {
        if (isInvalid) {
            PrinterStateReasons psr = new PrinterStateReasons();
            psr.put(PrinterStateReason.SHUTDOWN, Severity.ERROR);
            return psr;
        } else {
            return null;
        }
    }
    private QueuedJobCount getQueuedJobCountSysV() {
        String command = "/usr/bin/lpstat -R " + printer;
        String results[]= UnixPrintServiceLookup.execCmd(command);
        int qlen = (results == null) ? 0 : results.length;
        return new QueuedJobCount(qlen);
    }
    private QueuedJobCount getQueuedJobCountBSD() {
        if (UnixPrintServiceLookup.cmdIndex ==
            UnixPrintServiceLookup.UNINITIALIZED) {
            UnixPrintServiceLookup.cmdIndex =
                UnixPrintServiceLookup.getBSDCommandIndex();
        }
        int qlen = 0;
        String command = "/usr/sbin/lpc status " + printer
            + lpcQueueCom[UnixPrintServiceLookup.cmdIndex];
        String results[] = UnixPrintServiceLookup.execCmd(command);
        if (results != null && results.length > 0) {
            String queued;
            if (UnixPrintServiceLookup.cmdIndex ==
                UnixPrintServiceLookup.BSD_LPD_NG) {
                queued = results[0];
            } else {
                queued = results[3].trim();
                if (queued.startsWith("no")) {
                    return new QueuedJobCount(0);
                } else {
                    queued = queued.substring(0, queued.indexOf(' '));
                }
            }
            try {
                qlen = Integer.parseInt(queued);
            } catch (NumberFormatException e) {
            }
        }
        return new QueuedJobCount(qlen);
    }
    private QueuedJobCount getQueuedJobCount() {
        if (UnixPrintServiceLookup.isSysV()) {
            return getQueuedJobCountSysV();
        } else if (UnixPrintServiceLookup.isBSD()) {
            return getQueuedJobCountBSD();
        } else {
            return new QueuedJobCount(0);
        }
    }
    private PrintServiceAttributeSet getSysVServiceAttributes() {
        PrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
        attrs.add(getQueuedJobCountSysV());
        attrs.add(getPrinterIsAcceptingJobsSysV());
        return attrs;
    }
    private PrintServiceAttributeSet getBSDServiceAttributes() {
        PrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
        attrs.add(getQueuedJobCountBSD());
        attrs.add(getPrinterIsAcceptingJobsBSD());
        return attrs;
    }
    private boolean isSupportedCopies(Copies copies) {
        int numCopies = copies.getValue();
        return (numCopies > 0 && numCopies < MAXCOPIES);
    }
    private boolean isSupportedMedia(MediaSizeName msn) {
        for (int i=0; i<mediaSizes.length; i++) {
            if (msn.equals(mediaSizes[i])) {
                return true;
            }
        }
        return false;
    }
    public DocPrintJob createPrintJob() {
      SecurityManager security = System.getSecurityManager();
      if (security != null) {
        security.checkPrintJobAccess();
      }
        return new UnixPrintJob(this);
    }
    private PrintServiceAttributeSet getDynamicAttributes() {
        if (UnixPrintServiceLookup.isSysV()) {
            return getSysVServiceAttributes();
        } else {
            return getBSDServiceAttributes();
        }
    }
    public PrintServiceAttributeSet getUpdatedAttributes() {
        PrintServiceAttributeSet currSet = getDynamicAttributes();
        if (lastSet == null) {
            lastSet = currSet;
            return AttributeSetUtilities.unmodifiableView(currSet);
        } else {
            PrintServiceAttributeSet updates =
                new HashPrintServiceAttributeSet();
            Attribute []attrs = currSet.toArray();
            Attribute attr;
            for (int i=0; i<attrs.length; i++) {
                attr = attrs[i];
                if (!lastSet.containsValue(attr)) {
                    updates.add(attr);
                }
            }
            lastSet = currSet;
            return AttributeSetUtilities.unmodifiableView(updates);
        }
    }
    public void wakeNotifier() {
        synchronized (this) {
            if (notifier != null) {
                notifier.wake();
            }
        }
    }
    public void addPrintServiceAttributeListener(
                                 PrintServiceAttributeListener listener) {
        synchronized (this) {
            if (listener == null) {
                return;
            }
            if (notifier == null) {
                notifier = new ServiceNotifier(this);
            }
            notifier.addListener(listener);
        }
    }
    public void removePrintServiceAttributeListener(
                                  PrintServiceAttributeListener listener) {
        synchronized (this) {
            if (listener == null || notifier == null ) {
                return;
            }
            notifier.removeListener(listener);
            if (notifier.isEmpty()) {
                notifier.stopNotifier();
                notifier = null;
            }
        }
    }
    public <T extends PrintServiceAttribute>
        T getAttribute(Class<T> category)
    {
        if (category == null) {
            throw new NullPointerException("category");
        }
        if (!(PrintServiceAttribute.class.isAssignableFrom(category))) {
            throw new IllegalArgumentException("Not a PrintServiceAttribute");
        }
        if (category == PrinterName.class) {
            return (T)getPrinterName();
        } else if (category == PrinterState.class) {
            return (T)getPrinterState();
        } else if (category == PrinterStateReasons.class) {
            return (T)getPrinterStateReasons();
        } else if (category == QueuedJobCount.class) {
            return (T)getQueuedJobCount();
        } else if (category == PrinterIsAcceptingJobs.class) {
            return (T)getPrinterIsAcceptingJobs();
        } else {
            return null;
        }
    }
    public PrintServiceAttributeSet getAttributes() {
        PrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
        attrs.add(getPrinterName());
        attrs.add(getPrinterIsAcceptingJobs());
        PrinterState prnState = getPrinterState();
        if (prnState != null) {
            attrs.add(prnState);
        }
        PrinterStateReasons prnStateReasons = getPrinterStateReasons();
        if (prnStateReasons != null) {
            attrs.add(prnStateReasons);
        }
        attrs.add(getQueuedJobCount());
        return AttributeSetUtilities.unmodifiableView(attrs);
    }
    private void initSupportedDocFlavors() {
        String hostEnc = DocFlavor.hostEncoding.toLowerCase(Locale.ENGLISH);
        if (!hostEnc.equals("utf-8") && !hostEnc.equals("utf-16") &&
            !hostEnc.equals("utf-16be") && !hostEnc.equals("utf-16le") &&
            !hostEnc.equals("us-ascii")) {
            int len = supportedDocFlavorsInit.length;
            DocFlavor[] flavors =
                new DocFlavor[len + supportedHostDocFlavors.length];
            System.arraycopy(supportedHostDocFlavors, 0, flavors,
                             len, supportedHostDocFlavors.length);
            System.arraycopy(supportedDocFlavorsInit, 0, flavors, 0, len);
            supportedDocFlavors = flavors;
        } else {
            supportedDocFlavors = supportedDocFlavorsInit;
        }
    }
    public DocFlavor[] getSupportedDocFlavors() {
        if (supportedDocFlavors == null) {
            initSupportedDocFlavors();
        }
        int len = supportedDocFlavors.length;
        DocFlavor[] flavors = new DocFlavor[len];
        System.arraycopy(supportedDocFlavors, 0, flavors, 0, len);
        return flavors;
    }
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        if (supportedDocFlavors == null) {
            initSupportedDocFlavors();
        }
        for (int f=0; f<supportedDocFlavors.length; f++) {
            if (flavor.equals(supportedDocFlavors[f])) {
                return true;
            }
        }
        return false;
    }
    public Class[] getSupportedAttributeCategories() {
        int totalCats = otherAttrCats.length;
        Class [] cats = new Class[totalCats];
        System.arraycopy(otherAttrCats, 0, cats, 0, otherAttrCats.length);
        return cats;
    }
    public boolean
        isAttributeCategorySupported(Class<? extends Attribute> category)
    {
        if (category == null) {
            throw new NullPointerException("null category");
        }
        if (!(Attribute.class.isAssignableFrom(category))) {
            throw new IllegalArgumentException(category +
                                             " is not an Attribute");
        }
        for (int i=0;i<otherAttrCats.length;i++) {
            if (category == otherAttrCats[i]) {
                return true;
            }
        }
        return false;
    }
    public Object
        getDefaultAttributeValue(Class<? extends Attribute> category)
    {
        if (category == null) {
            throw new NullPointerException("null category");
        }
        if (!Attribute.class.isAssignableFrom(category)) {
            throw new IllegalArgumentException(category +
                                             " is not an Attribute");
        }
        if (!isAttributeCategorySupported(category)) {
            return null;
        }
        if (category == Copies.class) {
            return new Copies(1);
        } else if (category == Chromaticity.class) {
            return Chromaticity.COLOR;
        } else if (category == Destination.class) {
            try {
                return new Destination((new File("out.ps")).toURI());
            } catch (SecurityException se) {
                try {
                    return new Destination(new URI("file:out.ps"));
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        } else if (category == Fidelity.class) {
            return Fidelity.FIDELITY_FALSE;
        } else if (category == JobName.class) {
            return new JobName("Java Printing", null);
        } else if (category == JobSheets.class) {
            return JobSheets.STANDARD;
        } else if (category == Media.class) {
            String defaultCountry = Locale.getDefault().getCountry();
            if (defaultCountry != null &&
                (defaultCountry.equals("") ||
                 defaultCountry.equals(Locale.US.getCountry()) ||
                 defaultCountry.equals(Locale.CANADA.getCountry()))) {
                return MediaSizeName.NA_LETTER;
            } else {
                 return MediaSizeName.ISO_A4;
            }
        } else if (category == MediaPrintableArea.class) {
            String defaultCountry = Locale.getDefault().getCountry();
            float iw, ih;
            if (defaultCountry != null &&
                (defaultCountry.equals("") ||
                 defaultCountry.equals(Locale.US.getCountry()) ||
                 defaultCountry.equals(Locale.CANADA.getCountry()))) {
                iw = MediaSize.NA.LETTER.getX(Size2DSyntax.INCH) - 0.5f;
                ih = MediaSize.NA.LETTER.getY(Size2DSyntax.INCH) - 0.5f;
            } else {
                iw = MediaSize.ISO.A4.getX(Size2DSyntax.INCH) - 0.5f;
                ih = MediaSize.ISO.A4.getY(Size2DSyntax.INCH) - 0.5f;
            }
            return new MediaPrintableArea(0.25f, 0.25f, iw, ih,
                                          MediaPrintableArea.INCH);
        } else if (category == OrientationRequested.class) {
            return OrientationRequested.PORTRAIT;
        } else if (category == PageRanges.class) {
            return new PageRanges(1, Integer.MAX_VALUE);
        } else if (category == RequestingUserName.class) {
            String userName = "";
            try {
              userName = System.getProperty("user.name", "");
            } catch (SecurityException se) {
            }
            return new RequestingUserName(userName, null);
        } else if (category == SheetCollate.class) {
            return SheetCollate.UNCOLLATED;
        } else if (category == Sides.class) {
            return Sides.ONE_SIDED;
        } else
            return null;
    }
    private boolean isAutoSense(DocFlavor flavor) {
        if (flavor.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE) ||
            flavor.equals(DocFlavor.INPUT_STREAM.AUTOSENSE) ||
            flavor.equals(DocFlavor.URL.AUTOSENSE)) {
            return true;
        }
        else {
            return false;
        }
    }
    public Object
        getSupportedAttributeValues(Class<? extends Attribute> category,
                                    DocFlavor flavor,
                                    AttributeSet attributes)
    {
        if (category == null) {
            throw new NullPointerException("null category");
        }
        if (!Attribute.class.isAssignableFrom(category)) {
            throw new IllegalArgumentException(category +
                                             " does not implement Attribute");
        }
        if (flavor != null) {
            if (!isDocFlavorSupported(flavor)) {
                throw new IllegalArgumentException(flavor +
                                               " is an unsupported flavor");
            } else if (isAutoSense(flavor)) {
                return null;
            }
        }
        if (!isAttributeCategorySupported(category)) {
            return null;
        }
        if (category == Chromaticity.class) {
            if (flavor == null || isServiceFormattedFlavor(flavor)) {
                Chromaticity[]arr = new Chromaticity[1];
                arr[0] = Chromaticity.COLOR;
                return (arr);
            } else {
                return null;
            }
        } else if (category == Destination.class) {
            try {
                return new Destination((new File("out.ps")).toURI());
            } catch (SecurityException se) {
                try {
                    return new Destination(new URI("file:out.ps"));
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        } else if (category == JobName.class) {
            return new JobName("Java Printing", null);
        } else if (category == JobSheets.class) {
            JobSheets arr[] = new JobSheets[2];
            arr[0] = JobSheets.NONE;
            arr[1] = JobSheets.STANDARD;
            return arr;
        } else if (category == RequestingUserName.class) {
            String userName = "";
            try {
              userName = System.getProperty("user.name", "");
            } catch (SecurityException se) {
            }
            return new RequestingUserName(userName, null);
        } else if (category == OrientationRequested.class) {
            if (flavor == null || isServiceFormattedFlavor(flavor)) {
                OrientationRequested []arr = new OrientationRequested[3];
                arr[0] = OrientationRequested.PORTRAIT;
                arr[1] = OrientationRequested.LANDSCAPE;
                arr[2] = OrientationRequested.REVERSE_LANDSCAPE;
                return arr;
            } else {
                return null;
            }
        } else if ((category == Copies.class) ||
                   (category == CopiesSupported.class)) {
            if (flavor == null ||
                !(flavor.equals(DocFlavor.INPUT_STREAM.POSTSCRIPT) ||
                  flavor.equals(DocFlavor.URL.POSTSCRIPT) ||
                  flavor.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT))) {
                return new CopiesSupported(1, MAXCOPIES);
            } else {
                return null;
            }
        } else if (category == Media.class) {
            Media []arr = new Media[mediaSizes.length];
            System.arraycopy(mediaSizes, 0, arr, 0, mediaSizes.length);
            return arr;
        } else if (category == Fidelity.class) {
            Fidelity []arr = new Fidelity[2];
            arr[0] = Fidelity.FIDELITY_FALSE;
            arr[1] = Fidelity.FIDELITY_TRUE;
            return arr;
        } else if (category == MediaPrintableArea.class) {
            if (attributes == null) {
                return getAllPrintableAreas();
            }
            MediaSize mediaSize = (MediaSize)attributes.get(MediaSize.class);
            Media media = (Media)attributes.get(Media.class);
            MediaPrintableArea []arr = new MediaPrintableArea[1];
            if (mediaSize == null) {
                if (media instanceof MediaSizeName) {
                    MediaSizeName msn = (MediaSizeName)media;
                    mediaSize = MediaSize.getMediaSizeForName(msn);
                    if (mediaSize == null) {
                        media = (Media)getDefaultAttributeValue(Media.class);
                        if (media instanceof MediaSizeName) {
                            msn = (MediaSizeName)media;
                            mediaSize = MediaSize.getMediaSizeForName(msn);
                        }
                        if (mediaSize == null) {
                            arr[0] = new MediaPrintableArea(0.25f, 0.25f,
                                                            8f, 10.5f,
                                                            MediaSize.INCH);
                            return arr;
                        }
                    }
                } else {
                    return getAllPrintableAreas();
                }
            }
            assert mediaSize != null;
            arr[0] = new MediaPrintableArea(0.25f, 0.25f,
                                mediaSize.getX(MediaSize.INCH)-0.5f,
                                mediaSize.getY(MediaSize.INCH)-0.5f,
                                MediaSize.INCH);
            return arr;
        } else if (category == PageRanges.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                PageRanges []arr = new PageRanges[1];
                arr[0] = new PageRanges(1, Integer.MAX_VALUE);
                return arr;
            } else {
                return null;
            }
        } else if (category == SheetCollate.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                SheetCollate []arr = new SheetCollate[2];
                arr[0] = SheetCollate.UNCOLLATED;
                arr[1] = SheetCollate.COLLATED;
                return arr;
            } else {
                return null;
            }
        } else if (category == Sides.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                Sides []arr = new Sides[3];
                arr[0] = Sides.ONE_SIDED;
                arr[1] = Sides.TWO_SIDED_LONG_EDGE;
                arr[2] = Sides.TWO_SIDED_SHORT_EDGE;
                return arr;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    private static MediaPrintableArea[] mpas = null;
    private MediaPrintableArea[] getAllPrintableAreas() {
        if (mpas == null) {
            Media[] media = (Media[])getSupportedAttributeValues(Media.class,
                                                                 null, null);
            mpas = new MediaPrintableArea[media.length];
            for (int i=0; i< mpas.length; i++) {
                if (media[i] instanceof MediaSizeName) {
                    MediaSizeName msn = (MediaSizeName)media[i];
                    MediaSize mediaSize = MediaSize.getMediaSizeForName(msn);
                    if (mediaSize == null) {
                        mpas[i] = (MediaPrintableArea)
                            getDefaultAttributeValue(MediaPrintableArea.class);
                    } else {
                        mpas[i] = new MediaPrintableArea(0.25f, 0.25f,
                                        mediaSize.getX(MediaSize.INCH)-0.5f,
                                        mediaSize.getY(MediaSize.INCH)-0.5f,
                                        MediaSize.INCH);
                    }
                }
            }
        }
        MediaPrintableArea[] mpasCopy = new MediaPrintableArea[mpas.length];
        System.arraycopy(mpas, 0, mpasCopy, 0, mpas.length);
        return mpasCopy;
    }
    private boolean isServiceFormattedFlavor(DocFlavor flavor) {
        return
            flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
            flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
            flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
            flavor.equals(DocFlavor.URL.GIF) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
            flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
            flavor.equals(DocFlavor.URL.JPEG) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.PNG) ||
            flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
            flavor.equals(DocFlavor.URL.PNG);
    }
    public boolean isAttributeValueSupported(Attribute attr,
                                             DocFlavor flavor,
                                             AttributeSet attributes) {
        if (attr == null) {
            throw new NullPointerException("null attribute");
        }
        if (flavor != null) {
            if (!isDocFlavorSupported(flavor)) {
                throw new IllegalArgumentException(flavor +
                                               " is an unsupported flavor");
            } else if (isAutoSense(flavor)) {
                return false;
            }
        }
        Class category = attr.getCategory();
        if (!isAttributeCategorySupported(category)) {
            return false;
        }
        else if (attr.getCategory() == Chromaticity.class) {
            if (flavor == null || isServiceFormattedFlavor(flavor)) {
                return attr == Chromaticity.COLOR;
            } else {
                return false;
            }
        }
        else if (attr.getCategory() == Copies.class) {
            return (flavor == null ||
                   !(flavor.equals(DocFlavor.INPUT_STREAM.POSTSCRIPT) ||
                     flavor.equals(DocFlavor.URL.POSTSCRIPT) ||
                     flavor.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT))) &&
                isSupportedCopies((Copies)attr);
        } else if (attr.getCategory() == Destination.class) {
            URI uri = ((Destination)attr).getURI();
                if ("file".equals(uri.getScheme()) &&
                    !(uri.getSchemeSpecificPart().equals(""))) {
                return true;
            } else {
            return false;
            }
        } else if (attr.getCategory() == Media.class) {
            if (attr instanceof MediaSizeName) {
                return isSupportedMedia((MediaSizeName)attr);
            } else {
                return false;
            }
        } else if (attr.getCategory() == OrientationRequested.class) {
            if (attr == OrientationRequested.REVERSE_PORTRAIT ||
                (flavor != null) &&
                !isServiceFormattedFlavor(flavor)) {
                return false;
            }
        } else if (attr.getCategory() == PageRanges.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        } else if (attr.getCategory() == SheetCollate.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        } else if (attr.getCategory() == Sides.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        }
        return true;
    }
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor,
                                                 AttributeSet attributes) {
        if (flavor != null && !isDocFlavorSupported(flavor)) {
            throw new IllegalArgumentException("flavor " + flavor +
                                               "is not supported");
        }
        if (attributes == null) {
            return null;
        }
        Attribute attr;
        AttributeSet unsupp = new HashAttributeSet();
        Attribute []attrs = attributes.toArray();
        for (int i=0; i<attrs.length; i++) {
            try {
                attr = attrs[i];
                if (!isAttributeCategorySupported(attr.getCategory())) {
                    unsupp.add(attr);
                } else if (!isAttributeValueSupported(attr, flavor,
                                                      attributes)) {
                    unsupp.add(attr);
                }
            } catch (ClassCastException e) {
            }
        }
        if (unsupp.isEmpty()) {
            return null;
        } else {
            return unsupp;
        }
    }
    public ServiceUIFactory getServiceUIFactory() {
        return null;
    }
    public String toString() {
        return "Unix Printer : " + getName();
    }
    public boolean equals(Object obj) {
        return  (obj == this ||
                 (obj instanceof UnixPrintService &&
                  ((UnixPrintService)obj).getName().equals(getName())));
    }
    public int hashCode() {
        return this.getClass().hashCode()+getName().hashCode();
    }
    public boolean usesClass(Class c) {
        return (c == sun.print.PSPrinterJob.class);
    }
}
