public class VirtualMachineDescriptor {
    private AttachProvider provider;
    private String id;
    private String displayName;
    private volatile int hash;        
    public VirtualMachineDescriptor(AttachProvider provider, String id, String displayName) {
        if (provider == null) {
            throw new NullPointerException("provider cannot be null");
        }
        if (id == null) {
            throw new NullPointerException("identifier cannot be null");
        }
        if (displayName == null) {
            throw new NullPointerException("display name cannot be null");
        }
        this.provider = provider;
        this.id = id;
        this.displayName = displayName;
    }
    public VirtualMachineDescriptor(AttachProvider provider, String id) {
        this(provider, id, id);
    }
    public AttachProvider provider() {
        return provider;
    }
    public String id() {
        return id;
    }
    public String displayName() {
        return displayName;
    }
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        hash = provider.hashCode() * 127 + id.hashCode();
        return hash;
    }
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof VirtualMachineDescriptor))
            return false;
        VirtualMachineDescriptor other = (VirtualMachineDescriptor)ob;
        if (other.provider() != this.provider()) {
            return false;
        }
        if (!other.id().equals(this.id())) {
            return false;
        }
        return true;
    }
    public String toString() {
        String s = provider.toString() + ": " + id;
        if (displayName != id) {
            s += " " + displayName;
        }
        return s;
    }
}
