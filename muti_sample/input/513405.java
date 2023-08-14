public class DdmHandleHeap extends ChunkHandler {
    public static final int CHUNK_HPIF = type("HPIF");
    public static final int CHUNK_HPSG = type("HPSG");
    public static final int CHUNK_HPDU = type("HPDU");
    public static final int CHUNK_HPDS = type("HPDS");
    public static final int CHUNK_NHSG = type("NHSG");
    public static final int CHUNK_HPGC = type("HPGC");
    public static final int CHUNK_REAE = type("REAE");
    public static final int CHUNK_REAQ = type("REAQ");
    public static final int CHUNK_REAL = type("REAL");
    private static DdmHandleHeap mInstance = new DdmHandleHeap();
    private DdmHandleHeap() {}
    public static void register() {
        DdmServer.registerHandler(CHUNK_HPIF, mInstance);
        DdmServer.registerHandler(CHUNK_HPSG, mInstance);
        DdmServer.registerHandler(CHUNK_HPDU, mInstance);
        DdmServer.registerHandler(CHUNK_HPDS, mInstance);
        DdmServer.registerHandler(CHUNK_NHSG, mInstance);
        DdmServer.registerHandler(CHUNK_HPGC, mInstance);
        DdmServer.registerHandler(CHUNK_REAE, mInstance);
        DdmServer.registerHandler(CHUNK_REAQ, mInstance);
        DdmServer.registerHandler(CHUNK_REAL, mInstance);
    }
    public void connected() {}
    public void disconnected() {}
    public Chunk handleChunk(Chunk request) {
        if (Config.LOGV)
            Log.v("ddm-heap", "Handling " + name(request.type) + " chunk");
        int type = request.type;
        if (type == CHUNK_HPIF) {
            return handleHPIF(request);
        } else if (type == CHUNK_HPSG) {
            return handleHPSGNHSG(request, false);
        } else if (type == CHUNK_HPDU) {
            return handleHPDU(request);
        } else if (type == CHUNK_HPDS) {
            return handleHPDS(request);
        } else if (type == CHUNK_NHSG) {
            return handleHPSGNHSG(request, true);
        } else if (type == CHUNK_HPGC) {
            return handleHPGC(request);
        } else if (type == CHUNK_REAE) {
            return handleREAE(request);
        } else if (type == CHUNK_REAQ) {
            return handleREAQ(request);
        } else if (type == CHUNK_REAL) {
            return handleREAL(request);
        } else {
            throw new RuntimeException("Unknown packet "
                + ChunkHandler.name(type));
        }
    }
    private Chunk handleHPIF(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        int when = in.get();
        if (Config.LOGV)
            Log.v("ddm-heap", "Heap segment enable: when=" + when);
        boolean ok = DdmVmInternal.heapInfoNotify(when);
        if (!ok) {
            return createFailChunk(1, "Unsupported HPIF what");
        } else {
            return null;        
        }
    }
    private Chunk handleHPSGNHSG(Chunk request, boolean isNative) {
        ByteBuffer in = wrapChunk(request);
        int when = in.get();
        int what = in.get();
        if (Config.LOGV)
            Log.v("ddm-heap", "Heap segment enable: when=" + when
                + ", what=" + what + ", isNative=" + isNative);
        boolean ok = DdmVmInternal.heapSegmentNotify(when, what, isNative);
        if (!ok) {
            return createFailChunk(1, "Unsupported HPSG what/when");
        } else {
            return null;        
        }
    }
    private Chunk handleHPDU(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        byte result;
        int len = in.getInt();
        String fileName = getString(in, len);
        if (Config.LOGD)
            Log.d("ddm-heap", "Heap dump: file='" + fileName + "'");
        try {
            Debug.dumpHprofData(fileName);
            result = 0;
        } catch (UnsupportedOperationException uoe) {
            Log.w("ddm-heap", "hprof dumps not supported in this VM");
            result = -1;
        } catch (IOException ioe) {
            result = -1;
        } catch (RuntimeException re) {
            result = -1;
        }
        byte[] reply = { result };
        return new Chunk(CHUNK_HPDU, reply, 0, reply.length);
    }
    private Chunk handleHPDS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        byte result;
        if (Config.LOGD)
            Log.d("ddm-heap", "Heap dump: [DDMS]");
        String failMsg = null;
        try {
            Debug.dumpHprofDataDdms();
        } catch (UnsupportedOperationException uoe) {
            failMsg = "hprof dumps not supported in this VM";
        } catch (RuntimeException re) {
            failMsg = "Exception: " + re.getMessage();
        }
        if (failMsg != null) {
            Log.w("ddm-heap", failMsg);
            return createFailChunk(1, failMsg);
        } else {
            return null;
        }
    }
    private Chunk handleHPGC(Chunk request) {
        if (Config.LOGD)
            Log.d("ddm-heap", "Heap GC request");
        System.gc();
        return null;        
    }
    private Chunk handleREAE(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        boolean enable;
        enable = (in.get() != 0);
        if (Config.LOGD)
            Log.d("ddm-heap", "Recent allocation enable request: " + enable);
        DdmVmInternal.enableRecentAllocations(enable);
        return null;        
    }
    private Chunk handleREAQ(Chunk request) {
        byte[] reply = new byte[1];
        reply[0] = DdmVmInternal.getRecentAllocationStatus() ? (byte)1 :(byte)0;
        return new Chunk(CHUNK_REAQ, reply, 0, reply.length);
    }
    private Chunk handleREAL(Chunk request) {
        if (Config.LOGD)
            Log.d("ddm-heap", "Recent allocations request");
        byte[] reply = DdmVmInternal.getRecentAllocations();
        return new Chunk(CHUNK_REAL, reply, 0, reply.length);
    }
}