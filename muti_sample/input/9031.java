public abstract class InputStreamImageSource implements ImageProducer,
                                                        ImageFetchable
{
    ImageConsumerQueue consumers;
    ImageDecoder decoder;
    ImageDecoder decoders;
    boolean awaitingFetch = false;
    abstract boolean checkSecurity(Object context, boolean quiet);
    int countConsumers(ImageConsumerQueue cq) {
        int i = 0;
        while (cq != null) {
            i++;
            cq = cq.next;
        }
        return i;
    }
    synchronized int countConsumers() {
        ImageDecoder id = decoders;
        int i = countConsumers(consumers);
        while (id != null) {
            i += countConsumers(id.queue);
            id = id.next;
        }
        return i;
    }
    public void addConsumer(ImageConsumer ic) {
        addConsumer(ic, false);
    }
    synchronized void printQueue(ImageConsumerQueue cq, String prefix) {
        while (cq != null) {
            System.out.println(prefix+cq);
            cq = cq.next;
        }
    }
    synchronized void printQueues(String title) {
        System.out.println(title+"[ -----------");
        printQueue(consumers, "  ");
        for (ImageDecoder id = decoders; id != null; id = id.next) {
            System.out.println("    "+id);
            printQueue(id.queue, "      ");
        }
        System.out.println("----------- ]"+title);
    }
    synchronized void addConsumer(ImageConsumer ic, boolean produce) {
        checkSecurity(null, false);
        for (ImageDecoder id = decoders; id != null; id = id.next) {
            if (id.isConsumer(ic)) {
                return;
            }
        }
        ImageConsumerQueue cq = consumers;
        while (cq != null && cq.consumer != ic) {
            cq = cq.next;
        }
        if (cq == null) {
            cq = new ImageConsumerQueue(this, ic);
            cq.next = consumers;
            consumers = cq;
        } else {
            if (!cq.secure) {
                Object context = null;
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    context = security.getSecurityContext();
                }
                if (cq.securityContext == null) {
                    cq.securityContext = context;
                } else if (!cq.securityContext.equals(context)) {
                    errorConsumer(cq, false);
                    throw new SecurityException("Applets are trading image data!");
                }
            }
            cq.interested = true;
        }
        if (produce && decoder == null) {
            startProduction();
        }
    }
    public synchronized boolean isConsumer(ImageConsumer ic) {
        for (ImageDecoder id = decoders; id != null; id = id.next) {
            if (id.isConsumer(ic)) {
                return true;
            }
        }
        return ImageConsumerQueue.isConsumer(consumers, ic);
    }
    private void errorAllConsumers(ImageConsumerQueue cq, boolean needReload) {
        while (cq != null) {
            if (cq.interested) {
                errorConsumer(cq, needReload);
            }
            cq = cq.next;
        }
    }
    private void errorConsumer(ImageConsumerQueue cq, boolean needReload) {
        cq.consumer.imageComplete(ImageConsumer.IMAGEERROR);
        if ( needReload && cq.consumer instanceof ImageRepresentation) {
            ((ImageRepresentation)cq.consumer).image.flush();
        }
        removeConsumer(cq.consumer);
    }
    public synchronized void removeConsumer(ImageConsumer ic) {
        for (ImageDecoder id = decoders; id != null; id = id.next) {
            id.removeConsumer(ic);
        }
        consumers = ImageConsumerQueue.removeConsumer(consumers, ic, false);
    }
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic, true);
    }
    private synchronized void startProduction() {
        if (!awaitingFetch) {
            if (ImageFetcher.add(this)) {
                awaitingFetch = true;
            } else {
                ImageConsumerQueue cq = consumers;
                consumers = null;
                errorAllConsumers(cq, false);
            }
        }
    }
    private synchronized void stopProduction() {
        if (awaitingFetch) {
            ImageFetcher.remove(this);
            awaitingFetch = false;
        }
    }
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }
    protected abstract ImageDecoder getDecoder();
    protected ImageDecoder decoderForType(InputStream is,
                                          String content_type) {
        return null;
    }
    protected ImageDecoder getDecoder(InputStream is) {
        if (!is.markSupported())
            is = new BufferedInputStream(is);
        try {
          is.mark(8);
            int c1 = is.read();
            int c2 = is.read();
            int c3 = is.read();
            int c4 = is.read();
            int c5 = is.read();
            int c6 = is.read();
            int c7 = is.read();
            int c8 = is.read();
            is.reset();
            is.mark(-1);
            if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8') {
                return new GifImageDecoder(this, is);
            } else if (c1 == '\377' && c2 == '\330' && c3 == '\377') {
                return new JPEGImageDecoder(this, is);
            } else if (c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f') {
                return new XbmImageDecoder(this, is);
            } else if (c1 == 137 && c2 == 80 && c3 == 78 &&
                c4 == 71 && c5 == 13 && c6 == 10 &&
                c7 == 26 && c8 == 10) {
                return new PNGImageDecoder(this, is);
            }
        } catch (IOException e) {
        }
        return null;
    }
    public void doFetch() {
        synchronized (this) {
            if (consumers == null) {
                awaitingFetch = false;
                return;
            }
        }
        ImageDecoder imgd = getDecoder();
        if (imgd == null) {
            badDecoder();
        } else {
            setDecoder(imgd);
            try {
                imgd.produceImage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ImageFormatException e) {
                e.printStackTrace();
            } finally {
                removeDecoder(imgd);
                if ( Thread.currentThread().isInterrupted() || !Thread.currentThread().isAlive()) {
                    errorAllConsumers(imgd.queue, true);
                } else {
                    errorAllConsumers(imgd.queue, false);
            }
        }
    }
    }
    private void badDecoder() {
        ImageConsumerQueue cq;
        synchronized (this) {
            cq = consumers;
            consumers = null;
            awaitingFetch = false;
        }
        errorAllConsumers(cq, false);
    }
    private void setDecoder(ImageDecoder mydecoder) {
        ImageConsumerQueue cq;
        synchronized (this) {
            mydecoder.next = decoders;
            decoders = mydecoder;
            decoder = mydecoder;
            cq = consumers;
            mydecoder.queue = cq;
            consumers = null;
            awaitingFetch = false;
        }
        while (cq != null) {
            if (cq.interested) {
                if (!checkSecurity(cq.securityContext, true)) {
                    errorConsumer(cq, false);
                }
            }
            cq = cq.next;
        }
    }
    private synchronized void removeDecoder(ImageDecoder mydecoder) {
        doneDecoding(mydecoder);
        ImageDecoder idprev = null;
        for (ImageDecoder id = decoders; id != null; id = id.next) {
            if (id == mydecoder) {
                if (idprev == null) {
                    decoders = id.next;
                } else {
                    idprev.next = id.next;
                }
                break;
            }
            idprev = id;
        }
    }
    synchronized void doneDecoding(ImageDecoder mydecoder) {
        if (decoder == mydecoder) {
            decoder = null;
            if (consumers != null) {
                startProduction();
            }
        }
    }
    void latchConsumers(ImageDecoder id) {
        doneDecoding(id);
    }
    synchronized void flush() {
        decoder = null;
    }
}
