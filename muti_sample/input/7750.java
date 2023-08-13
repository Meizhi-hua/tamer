public final class AttachPermission extends java.security.BasicPermission {
    static final long serialVersionUID = -4619447669752976181L;
    public AttachPermission(String name) {
        super(name);
        if (!name.equals("attachVirtualMachine") && !name.equals("createAttachProvider")) {
            throw new IllegalArgumentException("name: " + name);
        }
    }
    public AttachPermission(String name, String actions) {
        super(name);
        if (!name.equals("attachVirtualMachine") && !name.equals("createAttachProvider")) {
            throw new IllegalArgumentException("name: " + name);
        }
        if (actions != null && actions.length() > 0) {
            throw new IllegalArgumentException("actions: " + actions);
        }
    }
}
