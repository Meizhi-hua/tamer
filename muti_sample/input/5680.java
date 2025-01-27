public final class RIOptimisticProvider extends SyncProvider implements Serializable {
    private CachedRowSetReader reader;
    private CachedRowSetWriter writer;
    private String providerID = "com.sun.rowset.providers.RIOptimisticProvider";
    private String vendorName = "Oracle Corporation";
    private String versionNumber = "1.0";
    private JdbcRowSetResourceBundle resBundle;
    public RIOptimisticProvider() {
        providerID = this.getClass().getName();
        reader = new CachedRowSetReader();
        writer = new CachedRowSetWriter();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public String getProviderID() {
        return providerID;
    }
    public RowSetWriter getRowSetWriter() {
        try {
            writer.setReader(reader);
        } catch (java.sql.SQLException e) {}
        return writer;
    }
    public RowSetReader getRowSetReader() {
        return reader;
    }
    public int getProviderGrade() {
        return SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT;
    }
    public void setDataSourceLock(int datasource_lock) throws SyncProviderException {
        if(datasource_lock != SyncProvider.DATASOURCE_NO_LOCK ) {
          throw new SyncProviderException(resBundle.handleGetObject("riop.locking").toString());
        }
    }
    public int getDataSourceLock() throws SyncProviderException {
        return SyncProvider.DATASOURCE_NO_LOCK;
    }
    public int supportsUpdatableView() {
        return SyncProvider.NONUPDATABLE_VIEW_SYNC;
    }
    public String getVersion() {
        return this.versionNumber;
    }
    public String getVendor() {
        return this.vendorName;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID =-3143367176751761936L;
}
