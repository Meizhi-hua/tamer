public class ModelPerformer {
    private List<ModelOscillator> oscillators = new ArrayList<ModelOscillator>();
    private List<ModelConnectionBlock> connectionBlocks
            = new ArrayList<ModelConnectionBlock>();
    private int keyFrom = 0;
    private int keyTo = 127;
    private int velFrom = 0;
    private int velTo = 127;
    private int exclusiveClass = 0;
    private boolean releaseTrigger = false;
    private boolean selfNonExclusive = false;
    private Object userObject = null;
    private boolean addDefaultConnections = true;
    private String name = null;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<ModelConnectionBlock> getConnectionBlocks() {
        return connectionBlocks;
    }
    public void setConnectionBlocks(List<ModelConnectionBlock> connectionBlocks) {
        this.connectionBlocks = connectionBlocks;
    }
    public List<ModelOscillator> getOscillators() {
        return oscillators;
    }
    public int getExclusiveClass() {
        return exclusiveClass;
    }
    public void setExclusiveClass(int exclusiveClass) {
        this.exclusiveClass = exclusiveClass;
    }
    public boolean isSelfNonExclusive() {
        return selfNonExclusive;
    }
    public void setSelfNonExclusive(boolean selfNonExclusive) {
        this.selfNonExclusive = selfNonExclusive;
    }
    public int getKeyFrom() {
        return keyFrom;
    }
    public void setKeyFrom(int keyFrom) {
        this.keyFrom = keyFrom;
    }
    public int getKeyTo() {
        return keyTo;
    }
    public void setKeyTo(int keyTo) {
        this.keyTo = keyTo;
    }
    public int getVelFrom() {
        return velFrom;
    }
    public void setVelFrom(int velFrom) {
        this.velFrom = velFrom;
    }
    public int getVelTo() {
        return velTo;
    }
    public void setVelTo(int velTo) {
        this.velTo = velTo;
    }
    public boolean isReleaseTriggered() {
        return releaseTrigger;
    }
    public void setReleaseTriggered(boolean value) {
        this.releaseTrigger = value;
    }
    public Object getUserObject() {
        return userObject;
    }
    public void setUserObject(Object object) {
        userObject = object;
    }
    public boolean isDefaultConnectionsEnabled() {
        return addDefaultConnections;
    }
    public void setDefaultConnectionsEnabled(boolean addDefaultConnections) {
        this.addDefaultConnections = addDefaultConnections;
    }
}
