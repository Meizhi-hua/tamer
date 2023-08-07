public class TraceFileTraceArchiveTrace extends AbstractTraceArchiveTrace {
    private static final SangerTraceCodec traceCodec = SangerTraceParser.getInstance();
    private final SangerTrace trace;
    public TraceFileTraceArchiveTrace(TraceArchiveRecord record, String rootDirPath) {
        super(record, rootDirPath);
        InputStream inputStream = null;
        try {
            inputStream = this.getInputStreamFor(TraceInfoField.TRACE_FILE);
            trace = traceCodec.decode(inputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid trace file", e);
        } finally {
            IOUtil.closeAndIgnoreErrors(inputStream);
        }
    }
    @Override
    public Peaks getPeaks() {
        return trace.getPeaks();
    }
    @Override
    public NucleotideEncodedGlyphs getBasecalls() {
        return trace.getBasecalls();
    }
    @Override
    public QualityEncodedGlyphs getQualities() {
        return trace.getQualities();
    }
}
