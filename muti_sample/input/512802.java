    class SyncMode {
        public static final SyncMode INTERNAL_CLOCK = new SyncMode("INTERNAL_CLOCK"); 
        public static final SyncMode MIDI_SYNC = new SyncMode("MIDI_SYNC"); 
        public static final SyncMode MIDI_TIME_CODE = new SyncMode("MIDI_TIME_CODE"); 
        public static final SyncMode NO_SYNC = new SyncMode("NO_SYNC"); 
        private String name;
        protected SyncMode(String name) {
            this.name = name;
        }
        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SyncMode other = (SyncMode) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }
        @Override
        public final int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }
        @Override
        public final String toString() {
            return name;
        }
    }
    int[] addControllerEventListener(ControllerEventListener listener, int[] controllers);
    boolean addMetaEventListener(MetaEventListener listener);
    int getLoopCount();
    long getLoopEndPoint();
    long getLoopStartPoint();
    Sequencer.SyncMode getMasterSyncMode();
    Sequencer.SyncMode[] getMasterSyncModes();
    long getMicrosecondLength();
    long getMicrosecondPosition();
    Sequence getSequence();
    Sequencer.SyncMode getSlaveSyncMode();
    Sequencer.SyncMode[] getSlaveSyncModes();
    float getTempoFactor();
    float getTempoInBPM();
    float getTempoInMPQ();
    long getTickLength();
    long getTickPosition();
    boolean getTrackMute(int track);
    boolean getTrackSolo(int track);
    boolean isRecording();
    boolean isRunning();
    void recordDisable(Track track);
    void recordEnable(Track track, int channel);
    int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers);
    void removeMetaEventListener(MetaEventListener listener);
    void setLoopCount(int count);
    void setLoopEndPoint(long tick);
    void setLoopStartPoint(long tick);
    void setMasterSyncMode(Sequencer.SyncMode sync);
    void setMicrosecondPosition(long microseconds);
    void setSequence(InputStream stream) throws IOException, InvalidMidiDataException;
    void setSequence(Sequence sequence) throws InvalidMidiDataException;
    void setSlaveSyncMode(Sequencer.SyncMode sync);
    void setTempoFactor(float factor);
    void setTempoInBPM(float bpm);
    void setTempoInMPQ(float mpq);
    void setTickPosition(long tick);
    void setTrackMute(int track, boolean mute);
    void setTrackSolo(int track, boolean solo);
    void start();
    void startRecording();
    void stop();
    void stopRecording();
}
