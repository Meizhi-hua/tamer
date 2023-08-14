public class JvmThreadInstanceTableMeta extends SnmpMibTable implements Serializable {
    public JvmThreadInstanceTableMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        super(myMib);
        objectserver = objserv;
    }
    protected JvmThreadInstanceEntryMeta createJvmThreadInstanceEntryMetaNode(String snmpEntryName, String tableName, SnmpMib mib, MBeanServer server)  {
        return new JvmThreadInstanceEntryMeta(mib, objectserver);
    }
    public void createNewEntry(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (factory != null)
            factory.createNewEntry(req, rowOid, depth, this);
        else
            throw new SnmpStatusException(
                SnmpStatusException.snmpRspNoAccess);
    }
    public boolean isRegistrationRequired()  {
        return false;
    }
    public void registerEntryNode(SnmpMib mib, MBeanServer server)  {
        node = createJvmThreadInstanceEntryMetaNode("JvmThreadInstanceEntry", "JvmThreadInstanceTable", mib, server);
    }
    public synchronized void addEntry(SnmpOid rowOid, ObjectName objname,
                 Object entry)
        throws SnmpStatusException {
        if (! (entry instanceof JvmThreadInstanceEntryMBean) )
            throw new ClassCastException("Entries for Table \"" +
                           "JvmThreadInstanceTable" + "\" must implement the \"" +
                           "JvmThreadInstanceEntryMBean" + "\" interface.");
        super.addEntry(rowOid, objname, entry);
    }
    public void get(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        JvmThreadInstanceEntryMBean entry = (JvmThreadInstanceEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.get(req,depth);
        }
    }
    public void set(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (req.getSize() == 0) return;
        JvmThreadInstanceEntryMBean entry = (JvmThreadInstanceEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.set(req,depth);
        }
    }
    public void check(SnmpMibSubRequest req, SnmpOid rowOid, int depth)
        throws SnmpStatusException {
        if (req.getSize() == 0) return;
        JvmThreadInstanceEntryMBean entry = (JvmThreadInstanceEntryMBean) getEntry(rowOid);
        synchronized (this) {
            node.setInstance(entry);
            node.check(req,depth);
        }
    }
    public void validateVarEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        node.validateVarId(var, data);
    }
    public boolean isReadableEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        return node.isReadable(var);
    }
    public long getNextVarEntryId( SnmpOid rowOid, long var, Object data )
        throws SnmpStatusException {
        long nextvar = node.getNextVarId(var, data);
        while (!isReadableEntryId(rowOid, nextvar, data))
            nextvar = node.getNextVarId(nextvar, data);
        return nextvar;
    }
    public boolean skipEntryVariable( SnmpOid rowOid, long var, Object data, int pduVersion) {
        try {
            JvmThreadInstanceEntryMBean entry = (JvmThreadInstanceEntryMBean) getEntry(rowOid);
            synchronized (this) {
                node.setInstance(entry);
                return node.skipVariable(var, data, pduVersion);
            }
        } catch (SnmpStatusException x) {
            return false;
        }
    }
    private JvmThreadInstanceEntryMeta node;
    protected SnmpStandardObjectServer objectserver;
}