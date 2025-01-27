public class UnixPrintJob implements CancelablePrintJob {
    private static String debugPrefix = "UnixPrintJob>> ";
    transient private Vector jobListeners;
    transient private Vector attrListeners;
    transient private Vector listenedAttributeSets;
    private PrintService service;
    private boolean fidelity;
    private boolean printing = false;
    private boolean printReturned = false;
    private PrintRequestAttributeSet reqAttrSet = null;
    private PrintJobAttributeSet jobAttrSet = null;
    private PrinterJob job;
    private Doc doc;
    private InputStream instream = null;
    private Reader reader = null;
    private String jobName = "Java Printing";
    private int copies = 1;
    private MediaSizeName mediaName = MediaSizeName.NA_LETTER;
    private MediaSize     mediaSize = MediaSize.NA.LETTER;
    private CustomMediaTray     customTray = null;
    private OrientationRequested orient = OrientationRequested.PORTRAIT;
    private NumberUp nUp = null;
    private Sides sides = null;
    UnixPrintJob(PrintService service) {
        this.service = service;
        mDestination = service.getName();
        mDestType = UnixPrintJob.DESTPRINTER;
    }
    public PrintService getPrintService() {
        return service;
    }
    public PrintJobAttributeSet getAttributes() {
        synchronized (this) {
            if (jobAttrSet == null) {
                PrintJobAttributeSet jobSet = new HashPrintJobAttributeSet();
                return AttributeSetUtilities.unmodifiableView(jobSet);
            } else {
              return jobAttrSet;
            }
        }
    }
    public void addPrintJobListener(PrintJobListener listener) {
        synchronized (this) {
            if (listener == null) {
                return;
            }
            if (jobListeners == null) {
                jobListeners = new Vector();
            }
            jobListeners.add(listener);
        }
    }
    public void removePrintJobListener(PrintJobListener listener) {
        synchronized (this) {
            if (listener == null || jobListeners == null ) {
                return;
            }
            jobListeners.remove(listener);
            if (jobListeners.isEmpty()) {
                jobListeners = null;
            }
        }
    }
    private void closeDataStreams() {
        if (doc == null) {
            return;
        }
        Object data = null;
        try {
            data = doc.getPrintData();
        } catch (IOException e) {
            return;
        }
        if (instream != null) {
            try {
                instream.close();
            } catch (IOException e) {
            } finally {
                instream = null;
            }
        }
        else if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            } finally {
                reader = null;
            }
        }
        else if (data instanceof InputStream) {
            try {
                ((InputStream)data).close();
            } catch (IOException e) {
            }
        }
        else if (data instanceof Reader) {
            try {
                ((Reader)data).close();
            } catch (IOException e) {
            }
        }
    }
    private void notifyEvent(int reason) {
        switch (reason) {
            case PrintJobEvent.DATA_TRANSFER_COMPLETE:
            case PrintJobEvent.JOB_CANCELED :
            case PrintJobEvent.JOB_FAILED :
            case PrintJobEvent.NO_MORE_EVENTS :
            case PrintJobEvent.JOB_COMPLETE :
                closeDataStreams();
        }
        synchronized (this) {
            if (jobListeners != null) {
                PrintJobListener listener;
                PrintJobEvent event = new PrintJobEvent(this, reason);
                for (int i = 0; i < jobListeners.size(); i++) {
                    listener = (PrintJobListener)(jobListeners.elementAt(i));
                    switch (reason) {
                        case PrintJobEvent.JOB_CANCELED :
                            listener.printJobCanceled(event);
                            break;
                        case PrintJobEvent.JOB_FAILED :
                            listener.printJobFailed(event);
                            break;
                        case PrintJobEvent.DATA_TRANSFER_COMPLETE :
                            listener.printDataTransferCompleted(event);
                            break;
                        case PrintJobEvent.NO_MORE_EVENTS :
                            listener.printJobNoMoreEvents(event);
                            break;
                        default:
                            break;
                    }
                }
            }
       }
    }
    public void addPrintJobAttributeListener(
                                  PrintJobAttributeListener listener,
                                  PrintJobAttributeSet attributes) {
        synchronized (this) {
            if (listener == null) {
                return;
            }
            if (attrListeners == null) {
                attrListeners = new Vector();
                listenedAttributeSets = new Vector();
            }
            attrListeners.add(listener);
            if (attributes == null) {
                attributes = new HashPrintJobAttributeSet();
            }
            listenedAttributeSets.add(attributes);
        }
    }
    public void removePrintJobAttributeListener(
                                        PrintJobAttributeListener listener) {
        synchronized (this) {
            if (listener == null || attrListeners == null ) {
                return;
            }
            int index = attrListeners.indexOf(listener);
            if (index == -1) {
                return;
            } else {
                attrListeners.remove(index);
                listenedAttributeSets.remove(index);
                if (attrListeners.isEmpty()) {
                    attrListeners = null;
                    listenedAttributeSets = null;
                }
            }
        }
    }
    public void print(Doc doc, PrintRequestAttributeSet attributes)
        throws PrintException {
        synchronized (this) {
            if (printing) {
                throw new PrintException("already printing");
            } else {
                printing = true;
            }
        }
        if ((PrinterIsAcceptingJobs)(service.getAttribute(
                         PrinterIsAcceptingJobs.class)) ==
                         PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS) {
            throw new PrintException("Printer is not accepting job.");
        }
        this.doc = doc;
        DocFlavor flavor = doc.getDocFlavor();
        Object data;
        try {
            data = doc.getPrintData();
        } catch (IOException e) {
            notifyEvent(PrintJobEvent.JOB_FAILED);
            throw new PrintException("can't get print data: " + e.toString());
        }
        if (flavor == null || (!service.isDocFlavorSupported(flavor))) {
            notifyEvent(PrintJobEvent.JOB_FAILED);
            throw new PrintJobFlavorException("invalid flavor", flavor);
        }
        initializeAttributeSets(doc, attributes);
        getAttributeValues(flavor);
        if ((service instanceof IPPPrintService) &&
            CUPSPrinter.isCupsRunning()) {
             IPPPrintService.debug_println(debugPrefix+
                        "instanceof IPPPrintService");
             if (mediaName != null) {
                 CustomMediaSizeName customMedia =
                     ((IPPPrintService)service).findCustomMedia(mediaName);
                 if (customMedia != null) {
                     mOptions = " media="+ customMedia.getChoiceName();
                 }
             }
             if (customTray != null &&
                 customTray instanceof CustomMediaTray) {
                 String choice = customTray.getChoiceName();
                 if (choice != null) {
                     mOptions += " media="+choice;
                 }
             }
             if (nUp != null) {
                 mOptions += " number-up="+nUp.getValue();
             }
             if (orient != OrientationRequested.PORTRAIT &&
                 (flavor != null) &&
                 !flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) {
                 mOptions += " orientation-requested="+orient.getValue();
             }
             if (sides != null) {
                 mOptions += " sides="+sides;
             }
        }
        IPPPrintService.debug_println(debugPrefix+"mOptions "+mOptions);
        String repClassName = flavor.getRepresentationClassName();
        String val = flavor.getParameter("charset");
        String encoding = "us-ascii";
        if (val != null && !val.equals("")) {
            encoding = val;
        }
        if (flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
            flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
            flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
            flavor.equals(DocFlavor.BYTE_ARRAY.PNG)) {
            try {
                instream = doc.getStreamForBytes();
                if (instream == null) {
                    notifyEvent(PrintJobEvent.JOB_FAILED);
                    throw new PrintException("No stream for data");
                }
                if (!(service instanceof IPPPrintService &&
                    ((IPPPrintService)service).isIPPSupportedImages(
                                                flavor.getMimeType()))) {
                    printableJob(new ImagePrinter(instream));
                    ((UnixPrintService)service).wakeNotifier();
                    return;
                }
            } catch (ClassCastException cce) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(cce);
            } catch (IOException ioe) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(ioe);
            }
        } else if (flavor.equals(DocFlavor.URL.GIF) ||
                   flavor.equals(DocFlavor.URL.JPEG) ||
                   flavor.equals(DocFlavor.URL.PNG)) {
            try {
                URL url = (URL)data;
                if ((service instanceof IPPPrintService) &&
                    ((IPPPrintService)service).isIPPSupportedImages(
                                               flavor.getMimeType())) {
                    instream = url.openStream();
                } else {
                    printableJob(new ImagePrinter(url));
                    ((UnixPrintService)service).wakeNotifier();
                    return;
                }
            } catch (ClassCastException cce) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(cce);
            } catch (IOException e) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(e.toString());
            }
        } else if (flavor.equals(DocFlavor.CHAR_ARRAY.TEXT_PLAIN) ||
                   flavor.equals(DocFlavor.READER.TEXT_PLAIN) ||
                   flavor.equals(DocFlavor.STRING.TEXT_PLAIN)) {
            try {
                reader = doc.getReaderForText();
                if (reader == null) {
                   notifyEvent(PrintJobEvent.JOB_FAILED);
                   throw new PrintException("No reader for data");
                }
            } catch (IOException ioe) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(ioe.toString());
            }
        } else if (repClassName.equals("[B") ||
                   repClassName.equals("java.io.InputStream")) {
            try {
                instream = doc.getStreamForBytes();
                if (instream == null) {
                    notifyEvent(PrintJobEvent.JOB_FAILED);
                    throw new PrintException("No stream for data");
                }
            } catch (IOException ioe) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(ioe.toString());
            }
        } else if  (repClassName.equals("java.net.URL")) {
            URL url = (URL)data;
            try {
                instream = url.openStream();
            } catch (IOException e) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(e.toString());
            }
        } else if (repClassName.equals("java.awt.print.Pageable")) {
            try {
                pageableJob((Pageable)doc.getPrintData());
                if (service instanceof IPPPrintService) {
                    ((IPPPrintService)service).wakeNotifier();
                } else {
                    ((UnixPrintService)service).wakeNotifier();
                }
                return;
            } catch (ClassCastException cce) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(cce);
            } catch (IOException ioe) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(ioe);
            }
        } else if (repClassName.equals("java.awt.print.Printable")) {
            try {
                printableJob((Printable)doc.getPrintData());
                if (service instanceof IPPPrintService) {
                    ((IPPPrintService)service).wakeNotifier();
                } else {
                    ((UnixPrintService)service).wakeNotifier();
                }
                return;
            } catch (ClassCastException cce) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(cce);
            } catch (IOException ioe) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException(ioe);
            }
        } else {
            notifyEvent(PrintJobEvent.JOB_FAILED);
            throw new PrintException("unrecognized class: "+repClassName);
        }
        PrinterOpener po = new PrinterOpener();
        java.security.AccessController.doPrivileged(po);
        if (po.pex != null) {
            throw po.pex;
        }
        OutputStream output = po.result;
        BufferedWriter bw = null;
        if ((instream == null && reader != null)) {
            BufferedReader br = new BufferedReader(reader);
            OutputStreamWriter osw = new OutputStreamWriter(output);
            bw = new BufferedWriter(osw);
            char []buffer = new char[1024];
            int cread;
            try {
                while ((cread = br.read(buffer, 0, buffer.length)) >=0) {
                    bw.write(buffer, 0, cread);
                }
                br.close();
                bw.flush();
                bw.close();
            } catch (IOException e) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException (e);
            }
        } else if (instream != null &&
                   flavor.getMediaType().equalsIgnoreCase("text")) {
            try {
                InputStreamReader isr = new InputStreamReader(instream,
                                                              encoding);
                BufferedReader br = new BufferedReader(isr);
                OutputStreamWriter osw = new OutputStreamWriter(output);
                bw = new BufferedWriter(osw);
                char []buffer = new char[1024];
                int cread;
                while ((cread = br.read(buffer, 0, buffer.length)) >=0) {
                    bw.write(buffer, 0, cread);
                }
                bw.flush();
            } catch (IOException e) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException (e);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (IOException e) {
                }
            }
        } else if (instream != null) {
            BufferedInputStream bin = new BufferedInputStream(instream);
            BufferedOutputStream bout = new BufferedOutputStream(output);
            byte[] buffer = new byte[1024];
            int bread = 0;
            try {
                while ((bread = bin.read(buffer)) >= 0) {
                    bout.write(buffer, 0, bread);
                }
                bin.close();
                bout.flush();
                bout.close();
            } catch (IOException e) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                throw new PrintException (e);
            }
        }
        notifyEvent(PrintJobEvent.DATA_TRANSFER_COMPLETE);
        if (mDestType == UnixPrintJob.DESTPRINTER) {
            PrinterSpooler spooler = new PrinterSpooler();
            java.security.AccessController.doPrivileged(spooler);
            if (spooler.pex != null) {
                throw spooler.pex;
            }
        }
        notifyEvent(PrintJobEvent.NO_MORE_EVENTS);
        if (service instanceof IPPPrintService) {
            ((IPPPrintService)service).wakeNotifier();
        } else {
            ((UnixPrintService)service).wakeNotifier();
        }
    }
    public void printableJob(Printable printable) throws PrintException {
        try {
            synchronized(this) {
                if (job != null) { 
                    throw new PrintException("already printing");
                } else {
                    job = new PSPrinterJob();
                }
            }
            job.setPrintService(getPrintService());
            job.setCopies(copies);
            job.setJobName(jobName);
            PageFormat pf = new PageFormat();
            if (mediaSize != null) {
                Paper p = new Paper();
                p.setSize(mediaSize.getX(MediaSize.INCH)*72.0,
                          mediaSize.getY(MediaSize.INCH)*72.0);
                p.setImageableArea(72.0, 72.0, p.getWidth()-144.0,
                                   p.getHeight()-144.0);
                pf.setPaper(p);
            }
            if (orient == OrientationRequested.REVERSE_LANDSCAPE) {
                pf.setOrientation(PageFormat.REVERSE_LANDSCAPE);
            } else if (orient == OrientationRequested.LANDSCAPE) {
                pf.setOrientation(PageFormat.LANDSCAPE);
            }
            job.setPrintable(printable, pf);
            job.print(reqAttrSet);
            notifyEvent(PrintJobEvent.DATA_TRANSFER_COMPLETE);
            return;
        } catch (PrinterException pe) {
            notifyEvent(PrintJobEvent.JOB_FAILED);
            throw new PrintException(pe);
        } finally {
            printReturned = true;
            notifyEvent(PrintJobEvent.NO_MORE_EVENTS);
        }
    }
    public void pageableJob(Pageable pageable) throws PrintException {
        try {
            synchronized(this) {
                if (job != null) { 
                    throw new PrintException("already printing");
                } else {
                    job = new PSPrinterJob();
                }
            }
            job.setPrintService(getPrintService());
            job.setCopies(copies);
            job.setJobName(jobName);
            job.setPageable(pageable);
            job.print(reqAttrSet);
            notifyEvent(PrintJobEvent.DATA_TRANSFER_COMPLETE);
            return;
        } catch (PrinterException pe) {
            notifyEvent(PrintJobEvent.JOB_FAILED);
            throw new PrintException(pe);
        } finally {
            printReturned = true;
            notifyEvent(PrintJobEvent.NO_MORE_EVENTS);
        }
    }
    private synchronized void
        initializeAttributeSets(Doc doc, PrintRequestAttributeSet reqSet) {
        reqAttrSet = new HashPrintRequestAttributeSet();
        jobAttrSet = new HashPrintJobAttributeSet();
        Attribute[] attrs;
        if (reqSet != null) {
            reqAttrSet.addAll(reqSet);
            attrs = reqSet.toArray();
            for (int i=0; i<attrs.length; i++) {
                if (attrs[i] instanceof PrintJobAttribute) {
                    jobAttrSet.add(attrs[i]);
                }
            }
        }
        DocAttributeSet docSet = doc.getAttributes();
        if (docSet != null) {
            attrs = docSet.toArray();
            for (int i=0; i<attrs.length; i++) {
                if (attrs[i] instanceof PrintRequestAttribute) {
                    reqAttrSet.add(attrs[i]);
                }
                if (attrs[i] instanceof PrintJobAttribute) {
                    jobAttrSet.add(attrs[i]);
                }
            }
        }
        String userName = "";
        try {
          userName = System.getProperty("user.name");
        } catch (SecurityException se) {
        }
        if (userName == null || userName.equals("")) {
            RequestingUserName ruName =
                (RequestingUserName)reqSet.get(RequestingUserName.class);
            if (ruName != null) {
                jobAttrSet.add(
                    new JobOriginatingUserName(ruName.getValue(),
                                               ruName.getLocale()));
            } else {
                jobAttrSet.add(new JobOriginatingUserName("", null));
            }
        } else {
            jobAttrSet.add(new JobOriginatingUserName(userName, null));
        }
        if (jobAttrSet.get(JobName.class) == null) {
            JobName jobName;
            if (docSet != null && docSet.get(DocumentName.class) != null) {
                DocumentName docName =
                    (DocumentName)docSet.get(DocumentName.class);
                jobName = new JobName(docName.getValue(), docName.getLocale());
                jobAttrSet.add(jobName);
            } else {
                String str = "JPS Job:" + doc;
                try {
                    Object printData = doc.getPrintData();
                    if (printData instanceof URL) {
                        str = ((URL)(doc.getPrintData())).toString();
                    }
                } catch (IOException e) {
                }
                jobName = new JobName(str, null);
                jobAttrSet.add(jobName);
            }
        }
        jobAttrSet = AttributeSetUtilities.unmodifiableView(jobAttrSet);
    }
    private void getAttributeValues(DocFlavor flavor) throws PrintException {
        Attribute attr;
        Class category;
        if (reqAttrSet.get(Fidelity.class) == Fidelity.FIDELITY_TRUE) {
            fidelity = true;
        } else {
            fidelity = false;
        }
        Attribute []attrs = reqAttrSet.toArray();
        for (int i=0; i<attrs.length; i++) {
            attr = attrs[i];
            category = attr.getCategory();
            if (fidelity == true) {
                if (!service.isAttributeCategorySupported(category)) {
                    notifyEvent(PrintJobEvent.JOB_FAILED);
                    throw new PrintJobAttributeException(
                        "unsupported category: " + category, category, null);
                } else if
                    (!service.isAttributeValueSupported(attr, flavor, null)) {
                    notifyEvent(PrintJobEvent.JOB_FAILED);
                    throw new PrintJobAttributeException(
                        "unsupported attribute: " + attr, null, attr);
                }
            }
            if (category == Destination.class) {
                URI uri = ((Destination)attr).getURI();
                if (!"file".equals(uri.getScheme())) {
                    notifyEvent(PrintJobEvent.JOB_FAILED);
                    throw new PrintException("Not a file: URI");
                } else {
                    try {
                        mDestType = DESTFILE;
                        mDestination = (new File(uri)).getPath();
                    } catch (Exception e) {
                        throw new PrintException(e);
                    }
                    SecurityManager security = System.getSecurityManager();
                    if (security != null) {
                      try {
                        security.checkWrite(mDestination);
                      } catch (SecurityException se) {
                        notifyEvent(PrintJobEvent.JOB_FAILED);
                        throw new PrintException(se);
                      }
                    }
                }
            } else if (category == JobSheets.class) {
                if ((JobSheets)attr == JobSheets.NONE) {
                   mNoJobSheet = true;
                }
            } else if (category == JobName.class) {
                jobName = ((JobName)attr).getValue();
            } else if (category == Copies.class) {
                copies = ((Copies)attr).getValue();
            } else if (category == Media.class) {
                if (attr instanceof MediaSizeName) {
                    mediaName = (MediaSizeName)attr;
                    IPPPrintService.debug_println(debugPrefix+
                                                  "mediaName "+mediaName);
                if (!service.isAttributeValueSupported(attr, null, null)) {
                    mediaSize = MediaSize.getMediaSizeForName(mediaName);
                }
              } else if (attr instanceof CustomMediaTray) {
                  customTray = (CustomMediaTray)attr;
              }
            } else if (category == OrientationRequested.class) {
                orient = (OrientationRequested)attr;
            } else if (category == NumberUp.class) {
                nUp = (NumberUp)attr;
            } else if (category == Sides.class) {
                sides = (Sides)attr;
            }
        }
    }
    private String[] printExecCmd(String printer, String options,
                                 boolean noJobSheet,
                                 String banner, int copies, String spoolFile) {
        int PRINTER = 0x1;
        int OPTIONS = 0x2;
        int BANNER  = 0x4;
        int COPIES  = 0x8;
        int NOSHEET  = 0x10;
        int pFlags = 0;
        String execCmd[];
        int ncomps = 2; 
        int n = 0;
        if (printer != null && !printer.equals("") && !printer.equals("lp")) {
            pFlags |= PRINTER;
            ncomps+=1;
        }
        if (options != null && !options.equals("")) {
            pFlags |= OPTIONS;
            ncomps+=1;
        }
        if (banner != null && !banner.equals("")) {
            pFlags |= BANNER;
            ncomps+=1;
        }
        if (copies > 1) {
            pFlags |= COPIES;
            ncomps+=1;
        }
        if (noJobSheet) {
            pFlags |= NOSHEET;
            ncomps+=1;
        }
        if (UnixPrintServiceLookup.osname.equals("SunOS")) {
            ncomps+=1; 
            execCmd = new String[ncomps];
            execCmd[n++] = "/usr/bin/lp";
            execCmd[n++] = "-c";           
            if ((pFlags & PRINTER) != 0) {
                execCmd[n++] = "-d" + printer;
            }
            if ((pFlags & BANNER) != 0) {
                String quoteChar = "\"";
                execCmd[n++] = "-t "  + quoteChar+banner+quoteChar;
            }
            if ((pFlags & COPIES) != 0) {
                execCmd[n++] = "-n " + copies;
            }
            if ((pFlags & NOSHEET) != 0) {
                execCmd[n++] = "-o nobanner";
            }
            if ((pFlags & OPTIONS) != 0) {
                execCmd[n++] = "-o " + options;
            }
        } else {
            execCmd = new String[ncomps];
            execCmd[n++] = "/usr/bin/lpr";
            if ((pFlags & PRINTER) != 0) {
                execCmd[n++] = "-P" + printer;
            }
            if ((pFlags & BANNER) != 0) {
                execCmd[n++] = "-J "  + banner;
            }
            if ((pFlags & COPIES) != 0) {
                execCmd[n++] = "-#" + copies;
            }
            if ((pFlags & NOSHEET) != 0) {
                execCmd[n++] = "-h";
            }
            if ((pFlags & OPTIONS) != 0) {
                execCmd[n++] = "-o" + options;
            }
        }
        execCmd[n++] = spoolFile;
        if (IPPPrintService.debugPrint) {
            System.out.println("UnixPrintJob>> execCmd");
            for (int i=0; i<execCmd.length; i++) {
                System.out.print(" "+execCmd[i]);
            }
            System.out.println();
        }
        return execCmd;
    }
    private static int DESTPRINTER = 1;
    private static int DESTFILE = 2;
    private int mDestType = DESTPRINTER;
    private File spoolFile;
    private String mDestination, mOptions="";
    private boolean mNoJobSheet = false;
    private class PrinterOpener implements java.security.PrivilegedAction {
        PrintException pex;
        OutputStream result;
        public Object run() {
            try {
                if (mDestType == UnixPrintJob.DESTFILE) {
                    spoolFile = new File(mDestination);
                } else {
                    spoolFile = File.createTempFile("javaprint", ".ps", null);
                    spoolFile.deleteOnExit();
                }
                result = new FileOutputStream(spoolFile);
                return result;
            } catch (IOException ex) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                pex = new PrintException(ex);
            }
            return null;
        }
    }
    private class PrinterSpooler implements java.security.PrivilegedAction {
        PrintException pex;
        public Object run() {
            try {
                if (spoolFile == null || !spoolFile.exists()) {
                   pex = new PrintException("No spool file");
                   notifyEvent(PrintJobEvent.JOB_FAILED);
                   return null;
                }
                String fileName = spoolFile.getAbsolutePath();
                String execCmd[] = printExecCmd(mDestination, mOptions,
                               mNoJobSheet, jobName, copies, fileName);
                Process process = Runtime.getRuntime().exec(execCmd);
                process.waitFor();
                spoolFile.delete();
                notifyEvent(PrintJobEvent.DATA_TRANSFER_COMPLETE);
            } catch (IOException ex) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                pex = new PrintException(ex);
            } catch (InterruptedException ie) {
                notifyEvent(PrintJobEvent.JOB_FAILED);
                pex = new PrintException(ie);
            } finally {
                notifyEvent(PrintJobEvent.NO_MORE_EVENTS);
            }
            return null;
        }
    }
    public void cancel() throws PrintException {
        synchronized (this) {
            if (!printing) {
                throw new PrintException("Job is not yet submitted.");
            } else if (job != null && !printReturned) {
                job.cancel();
                notifyEvent(PrintJobEvent.JOB_CANCELED);
                return;
            } else {
                throw new PrintException("Job could not be cancelled.");
            }
        }
    }
}
