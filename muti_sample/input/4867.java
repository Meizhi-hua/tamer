public class CachedRowSetImpl extends BaseRowSet implements RowSet, RowSetInternal, Serializable, Cloneable, CachedRowSet {
    private SyncProvider provider;
    private RowSetReader rowSetReader;
    private RowSetWriter rowSetWriter;
    private transient Connection conn;
    private transient ResultSetMetaData RSMD;
    private RowSetMetaDataImpl RowSetMD;
    private int keyCols[];
    private String tableName;
    private Vector<Object> rvh;
    private int cursorPos;
    private int absolutePos;
    private int numDeleted;
    private int numRows;
    private InsertRow insertRow;
    private boolean onInsertRow;
    private int currentRow;
    private boolean lastValueNull;
    private SQLWarning sqlwarn;
    private String strMatchColumn ="";
    private int iMatchColumn = -1;
    private RowSetWarning rowsetWarning;
    private String DEFAULT_SYNC_PROVIDER = "com.sun.rowset.providers.RIOptimisticProvider";
    private boolean dbmslocatorsUpdateCopy;
    private transient ResultSet resultSet;
    private int endPos;
    private int prevEndPos;
    private int startPos;
    private int startPrev;
    private int pageSize;
    private int maxRowsreached;
    private boolean pagenotend = true;
    private boolean onFirstPage;
    private boolean onLastPage;
    private int populatecallcount;
    private int totalRows;
    private boolean callWithCon;
    private CachedRowSetReader crsReader;
    private Vector<Integer> iMatchColumns;
    private Vector<String> strMatchColumns;
    private boolean tXWriter = false;
    private TransactionalWriter tWriter = null;
    protected transient JdbcRowSetResourceBundle resBundle;
    private boolean updateOnInsert;
    public CachedRowSetImpl() throws SQLException {
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        provider =
        (SyncProvider)SyncFactory.getInstance(DEFAULT_SYNC_PROVIDER);
        if (!(provider instanceof RIOptimisticProvider)) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidp").toString());
        }
        rowSetReader = (CachedRowSetReader)provider.getRowSetReader();
        rowSetWriter = (CachedRowSetWriter)provider.getRowSetWriter();
        initParams();
        initContainer();
        initProperties();
        onInsertRow = false;
        insertRow = null;
        sqlwarn = new SQLWarning();
        rowsetWarning = new RowSetWarning();
    }
    public CachedRowSetImpl(Hashtable env) throws SQLException {
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (env == null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.nullhash").toString());
        }
        String providerName = (String)env.get(
        javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER);
        provider =
        (SyncProvider)SyncFactory.getInstance(providerName);
        rowSetReader = provider.getRowSetReader();
        rowSetWriter = provider.getRowSetWriter();
        initParams(); 
        initContainer();
        initProperties(); 
    }
    private void initContainer() {
        rvh = new Vector<Object>(100);
        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;
    }
    private void initProperties() throws SQLException {
        if(resBundle == null) {
            try {
               resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
            } catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
        setConcurrency(ResultSet.CONCUR_UPDATABLE);
        if((rvh.size() > 0) && (isReadOnly() == false))
            setReadOnly(false);
        else
            setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        checkTransactionalWriter();
        iMatchColumns = new Vector<Integer>(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,Integer.valueOf(-1));
        }
        strMatchColumns = new Vector<String>(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }
    private void checkTransactionalWriter() {
        if (rowSetWriter != null) {
            Class c = rowSetWriter.getClass();
            if (c != null) {
                Class[] theInterfaces = c.getInterfaces();
                for (int i = 0; i < theInterfaces.length; i++) {
                    if ((theInterfaces[i].getName()).indexOf("TransactionalWriter") > 0) {
                        tXWriter = true;
                        establishTransactionalWriter();
                    }
                }
            }
        }
    }
    private void establishTransactionalWriter() {
        tWriter = (TransactionalWriter)provider.getRowSetWriter();
    }
    public void setCommand(String cmd) throws SQLException {
        super.setCommand(cmd);
        if(!buildTableName(cmd).equals("")) {
            this.setTableName(buildTableName(cmd));
        }
    }
     public void populate(ResultSet data) throws SQLException {
        int rowsFetched;
        Row currentRow;
        int numCols;
        int i;
        Map<String, Class<?>> map = getTypeMap();
        Object obj;
        int mRows;
        if (data == null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.populate").toString());
        }
        this.resultSet = data;
        RSMD = data.getMetaData();
        RowSetMD = new RowSetMetaDataImpl();
        initMetaData(RowSetMD, RSMD);
        RSMD = null;
        numCols = RowSetMD.getColumnCount();
        mRows = this.getMaxRows();
        rowsFetched = 0;
        currentRow = null;
        while ( data.next()) {
            currentRow = new Row(numCols);
            if ( rowsFetched > mRows && mRows > 0) {
                rowsetWarning.setNextWarning(new RowSetWarning("Populating rows "
                + "setting has exceeded max row setting"));
            }
            for ( i = 1; i <= numCols; i++) {
                if (map == null) {
                    obj = data.getObject(i);
                } else {
                    obj = data.getObject(i, map);
                }
                if (obj instanceof Struct) {
                    obj = new SerialStruct((Struct)obj, map);
                } else if (obj instanceof SQLData) {
                    obj = new SerialStruct((SQLData)obj, map);
                } else if (obj instanceof Blob) {
                    obj = new SerialBlob((Blob)obj);
                } else if (obj instanceof Clob) {
                    obj = new SerialClob((Clob)obj);
                } else if (obj instanceof java.sql.Array) {
                    if(map != null)
                        obj = new SerialArray((java.sql.Array)obj, map);
                    else
                        obj = new SerialArray((java.sql.Array)obj);
                }
                ((Row)currentRow).initColumnObject(i, obj);
            }
            rowsFetched++;
            rvh.add(currentRow);
        }
        numRows = rowsFetched ;
        notifyRowSetChanged();
    }
    private void initMetaData(RowSetMetaDataImpl md, ResultSetMetaData rsmd) throws SQLException {
        int numCols = rsmd.getColumnCount();
        md.setColumnCount(numCols);
        for (int col=1; col <= numCols; col++) {
            md.setAutoIncrement(col, rsmd.isAutoIncrement(col));
            if(rsmd.isAutoIncrement(col))
                updateOnInsert = true;
            md.setCaseSensitive(col, rsmd.isCaseSensitive(col));
            md.setCurrency(col, rsmd.isCurrency(col));
            md.setNullable(col, rsmd.isNullable(col));
            md.setSigned(col, rsmd.isSigned(col));
            md.setSearchable(col, rsmd.isSearchable(col));
            int size = rsmd.getColumnDisplaySize(col);
            if (size < 0) {
                size = 0;
            }
            md.setColumnDisplaySize(col, size);
            md.setColumnLabel(col, rsmd.getColumnLabel(col));
            md.setColumnName(col, rsmd.getColumnName(col));
            md.setSchemaName(col, rsmd.getSchemaName(col));
            int precision = rsmd.getPrecision(col);
            if (precision < 0) {
                precision = 0;
            }
            md.setPrecision(col, precision);
            int scale = rsmd.getScale(col);
            if (scale < 0) {
                scale = 0;
            }
            md.setScale(col, scale);
            md.setTableName(col, rsmd.getTableName(col));
            md.setCatalogName(col, rsmd.getCatalogName(col));
            md.setColumnType(col, rsmd.getColumnType(col));
            md.setColumnTypeName(col, rsmd.getColumnTypeName(col));
        }
        if( conn != null){
            dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
        }
    }
    public void execute(Connection conn) throws SQLException {
        setConnection(conn);
        if(getPageSize() != 0){
            crsReader = (CachedRowSetReader)provider.getRowSetReader();
            crsReader.setStartPosition(1);
            callWithCon = true;
            crsReader.readData((RowSetInternal)this);
        }
        else {
           rowSetReader.readData((RowSetInternal)this);
        }
        RowSetMD = (RowSetMetaDataImpl)this.getMetaData();
        if(conn != null){
            dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
        }
    }
    private void setConnection (Connection connection) {
        conn = connection;
    }
    public void acceptChanges() throws SyncProviderException {
        if (onInsertRow == true) {
            throw new SyncProviderException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }
        int saveCursorPos = cursorPos;
        boolean success = false;
        boolean conflict = false;
        try {
            if (rowSetWriter != null) {
                saveCursorPos = cursorPos;
                conflict = rowSetWriter.writeData((RowSetInternal)this);
                cursorPos = saveCursorPos;
            }
            if ((tXWriter) && this.COMMIT_ON_ACCEPT_CHANGES) {
                if (!conflict) {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    tWriter.rollback();
                    success = false;
                } else {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    if (tWriter instanceof CachedRowSetWriter) {
                        ((CachedRowSetWriter)tWriter).commit(this, updateOnInsert);
                    } else {
                        tWriter.commit();
                    }
                    success = true;
                }
            }
            if (success == true) {
                setOriginal();
            } else if (!(success) && !(this.COMMIT_ON_ACCEPT_CHANGES)) {
                throw new SyncProviderException(resBundle.handleGetObject("cachedrowsetimpl.accfailed").toString());
            }
        } catch (SyncProviderException spe) {
               throw spe;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SyncProviderException(e.getMessage());
        } catch (SecurityException e) {
            throw new SyncProviderException(e.getMessage());
        }
    }
    public void acceptChanges(Connection con) throws SyncProviderException{
      setConnection(con);
      acceptChanges();
    }
    public void restoreOriginal() throws SQLException {
        Row currentRow;
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            currentRow = (Row)i.next();
            if (currentRow.getInserted() == true) {
                i.remove();
                --numRows;
            } else {
                if (currentRow.getDeleted() == true) {
                    currentRow.clearDeleted();
                }
                if (currentRow.getUpdated() == true) {
                    currentRow.clearUpdated();
                }
            }
        }
        cursorPos = 0;
        notifyRowSetChanged();
    }
    public void release() throws SQLException {
        initContainer();
        notifyRowSetChanged();
    }
    public void undoDelete() throws SQLException {
        if (getShowDeleted() == false) {
            return;
        }
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getDeleted() == true) {
            currentRow.clearDeleted();
            --numDeleted;
            notifyRowChanged();
        }
    }
    public void undoInsert() throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getInserted() == true) {
            rvh.remove(cursorPos-1);
            --numRows;
            notifyRowChanged();
        } else {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.illegalop").toString());
        }
    }
    public void undoUpdate() throws SQLException {
        moveToCurrentRow();
        undoDelete();
        undoInsert();
    }
    public RowSet createShared() throws SQLException {
        RowSet clone;
        try {
            clone = (RowSet)clone();
        } catch (CloneNotSupportedException ex) {
            throw new SQLException(ex.getMessage());
        }
        return clone;
    }
    protected Object clone() throws CloneNotSupportedException  {
        return (super.clone());
    }
    public CachedRowSet createCopy() throws SQLException {
        ObjectOutputStream out;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            out = new ObjectOutputStream(bOut);
            out.writeObject(this);
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }
        ObjectInputStream in;
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            in = new ObjectInputStream(bIn);
        } catch (StreamCorruptedException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }
        try {
            CachedRowSetImpl crsTemp = (CachedRowSetImpl)in.readObject();
            crsTemp.resBundle = this.resBundle;
            return ((CachedRowSet)crsTemp);
        } catch (ClassNotFoundException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (OptionalDataException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }
    }
    public CachedRowSet createCopySchema() throws SQLException {
        int nRows = numRows;
        numRows = 0;
        CachedRowSet crs = this.createCopy();
        numRows = nRows;
        return crs;
    }
    public CachedRowSet createCopyNoConstraints() throws SQLException {
        CachedRowSetImpl crs;
        crs = (CachedRowSetImpl)this.createCopy();
        crs.initProperties();
        try {
            crs.unsetMatchColumn(crs.getMatchColumnIndexes());
        } catch(SQLException sqle) {
        }
        try {
            crs.unsetMatchColumn(crs.getMatchColumnNames());
        } catch(SQLException sqle) {
        }
        return crs;
    }
    public Collection<?> toCollection() throws SQLException {
        TreeMap<Integer, Object> tMap = new TreeMap<>();
        for (int i = 0; i<numRows; i++) {
            tMap.put(Integer.valueOf(i), rvh.get(i));
        }
        return (tMap.values());
    }
    public Collection<?> toCollection(int column) throws SQLException {
        int nRows = numRows;
        Vector<Object> vec = new Vector<>(nRows);
        CachedRowSetImpl crsTemp;
        crsTemp = (CachedRowSetImpl) this.createCopy();
        while(nRows!=0) {
            crsTemp.next();
            vec.add(crsTemp.getObject(column));
            nRows--;
        }
        return (Collection)vec;
    }
    public Collection<?> toCollection(String column) throws SQLException {
        return toCollection(getColIdxByName(column));
    }
    public SyncProvider getSyncProvider() throws SQLException {
        return provider;
    }
    public void setSyncProvider(String providerStr) throws SQLException {
        provider =
        (SyncProvider)SyncFactory.getInstance(providerStr);
        rowSetReader = provider.getRowSetReader();
        rowSetWriter = provider.getRowSetWriter();
    }
    public void execute() throws SQLException {
        execute(null);
    }
    public boolean next() throws SQLException {
        if (cursorPos < 0 || cursorPos >= numRows + 1) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        boolean ret = this.internalNext();
        notifyCursorMoved();
        return ret;
    }
    protected boolean internalNext() throws SQLException {
        boolean ret = false;
        do {
            if (cursorPos < numRows) {
                ++cursorPos;
                ret = true;
            } else if (cursorPos == numRows) {
                ++cursorPos;
                ret = false;
                break;
            }
        } while ((getShowDeleted() == false) && (rowDeleted() == true));
        if (ret == true)
            absolutePos++;
        else
            absolutePos = 0;
        return ret;
    }
    public void close() throws SQLException {
        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;
        initProperties();
        rvh.clear();
    }
    public boolean wasNull() throws SQLException {
        return lastValueNull;
    }
    private void setLastValueNull(boolean value) {
        lastValueNull = value;
    }
    private void checkIndex(int idx) throws SQLException {
        if (idx < 1 || idx > RowSetMD.getColumnCount()) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcol").toString());
        }
    }
    private void checkCursor() throws SQLException {
        if (isAfterLast() == true || isBeforeFirst() == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
    }
    private int getColIdxByName(String name) throws SQLException {
        RowSetMD = (RowSetMetaDataImpl)this.getMetaData();
        int cols = RowSetMD.getColumnCount();
        for (int i=1; i <= cols; ++i) {
            String colName = RowSetMD.getColumnName(i);
            if (colName != null)
                if (name.equalsIgnoreCase(colName))
                    return (i);
                else
                    continue;
        }
        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalcolnm").toString());
    }
    protected BaseRow getCurrentRow() {
        if (onInsertRow == true) {
            return (BaseRow)insertRow;
        } else {
            return (BaseRow)(rvh.get(cursorPos - 1));
        }
    }
    protected void removeCurrentRow() {
        ((Row)getCurrentRow()).setDeleted();
        rvh.remove(cursorPos - 1);
        --numRows;
    }
    public String getString(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value.toString();
    }
    public boolean getBoolean(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return false;
        }
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        try {
            Double d = new Double(value.toString());
            if (d.compareTo(new Double((double)0)) == 0) {
                return false;
            } else {
                return true;
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.boolfail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public byte getByte(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (byte)0;
        }
        try {
            return ((Byte.valueOf(value.toString())).byteValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.bytefail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public short getShort(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (short)0;
        }
        try {
            return ((Short.valueOf(value.toString().trim())).shortValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.shortfail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public int getInt(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (int)0;
        }
        try {
            return ((Integer.valueOf(value.toString().trim())).intValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.intfail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public long getLong(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (long)0;
        }
        try {
            return ((Long.valueOf(value.toString().trim())).longValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.longfail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public float getFloat(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (float)0;
        }
        try {
            return ((new Float(value.toString())).floatValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.floatfail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public double getDouble(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (double)0;
        }
        try {
            return ((new Double(value.toString().trim())).doubleValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.doublefail").toString(),
                  new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        Object value;
        BigDecimal bDecimal, retVal;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return (new BigDecimal(0));
        }
        bDecimal = this.getBigDecimal(columnIndex);
        retVal = bDecimal.setScale(scale);
        return retVal;
    }
    public byte[] getBytes(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        return (byte[])(getCurrentRow().getColumnObject(columnIndex));
    }
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        switch (RowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat df = DateFormat.getDateInstance();
                    return ((java.sql.Date)(df.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.datefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.datefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
            }
        }
    }
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        switch (RowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.TIME: {
                return (java.sql.Time)value;
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Time(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Time)(tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.timefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.timefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
            }
        }
    }
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        switch (RowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.TIMESTAMP: {
                return (java.sql.Timestamp)value;
            }
            case java.sql.Types.TIME: {
                long sec = ((java.sql.Time)value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date)value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Timestamp)(tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.timefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.timefail").toString(),
                        new Object[] {value.toString().trim(), columnIndex}));
            }
        }
    }
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        Object value;
        asciiStream = null;
        checkIndex(columnIndex);
        checkCursor();
        value =  getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        try {
            if (isString(RowSetMD.getColumnType(columnIndex))) {
                asciiStream = new ByteArrayInputStream(((String)value).getBytes("ASCII"));
            } else {
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
            }
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new SQLException(ex.getMessage());
        }
        return (java.io.InputStream)asciiStream;
    }
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        unicodeStream = null;
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex)) == false &&
        isString(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        Object value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        unicodeStream = new StringBufferInputStream(value.toString());
        return (java.io.InputStream)unicodeStream;
    }
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        binaryStream = null;
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        Object value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        binaryStream = new ByteArrayInputStream((byte[])value);
        return (java.io.InputStream)binaryStream;
    }
    public String getString(String columnName) throws SQLException {
        return getString(getColIdxByName(columnName));
    }
    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(getColIdxByName(columnName));
    }
    public byte getByte(String columnName) throws SQLException {
        return getByte(getColIdxByName(columnName));
    }
    public short getShort(String columnName) throws SQLException {
        return getShort(getColIdxByName(columnName));
    }
    public int getInt(String columnName) throws SQLException {
        return getInt(getColIdxByName(columnName));
    }
    public long getLong(String columnName) throws SQLException {
        return getLong(getColIdxByName(columnName));
    }
    public float getFloat(String columnName) throws SQLException {
        return getFloat(getColIdxByName(columnName));
    }
    public double getDouble(String columnName) throws SQLException {
        return getDouble(getColIdxByName(columnName));
    }
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getBigDecimal(getColIdxByName(columnName), scale);
    }
    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(getColIdxByName(columnName));
    }
    public java.sql.Date getDate(String columnName) throws SQLException {
        return getDate(getColIdxByName(columnName));
    }
    public java.sql.Time getTime(String columnName) throws SQLException {
        return getTime(getColIdxByName(columnName));
    }
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        return getTimestamp(getColIdxByName(columnName));
    }
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        return getAsciiStream(getColIdxByName(columnName));
    }
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        return getUnicodeStream(getColIdxByName(columnName));
    }
    public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
        return getBinaryStream(getColIdxByName(columnName));
    }
    public SQLWarning getWarnings() {
        return sqlwarn;
    }
    public void clearWarnings() {
        sqlwarn = null;
    }
    public String getCursorName() throws SQLException {
        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.posupdate").toString());
    }
    public ResultSetMetaData getMetaData() throws SQLException {
        return (ResultSetMetaData)RowSetMD;
    }
    public Object getObject(int columnIndex) throws SQLException {
        Object value;
        Map<String, Class<?>> map;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        if (value instanceof Struct) {
            Struct s = (Struct)value;
            map = getTypeMap();
            Class c = (Class)map.get(s.getSQLTypeName());
            if (c != null) {
                SQLData obj = null;
                try {
                    obj = (SQLData)c.newInstance();
                } catch (java.lang.InstantiationException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                    ex.getMessage()));
                } catch (java.lang.IllegalAccessException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                    ex.getMessage()));
                }
                Object attribs[] = s.getAttributes(map);
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                obj.readSQL(sqlInput, s.getSQLTypeName());
                return (Object)obj;
            }
        }
        return value;
    }
    public Object getObject(String columnName) throws SQLException {
        return getObject(getColIdxByName(columnName));
    }
    public int findColumn(String columnName) throws SQLException {
        return getColIdxByName(columnName);
    }
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException{
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex))) {
            Object value = getCurrentRow().getColumnObject(columnIndex);
            if (value == null) {
                lastValueNull = true;
                return null;
            }
            charStream = new InputStreamReader
            (new ByteArrayInputStream((byte[])value));
        } else if (isString(RowSetMD.getColumnType(columnIndex))) {
            Object value = getCurrentRow().getColumnObject(columnIndex);
            if (value == null) {
                lastValueNull = true;
                return null;
            }
            charStream = new StringReader(value.toString());
        } else {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        return (java.io.Reader)charStream;
    }
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        return getCharacterStream(getColIdxByName(columnName));
    }
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        try {
            return (new BigDecimal(value.toString().trim()));
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.doublefail").toString(),
                new Object[] {value.toString().trim(), columnIndex}));
        }
    }
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(getColIdxByName(columnName));
    }
    public int size() {
        return numRows;
    }
    public boolean isBeforeFirst() throws SQLException {
        if (cursorPos == 0 && numRows > 0) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isAfterLast() throws SQLException {
        if (cursorPos == numRows+1 && numRows > 0) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isFirst() throws SQLException {
        int saveCursorPos = cursorPos;
        int saveAbsoluteCursorPos = absolutePos;
        internalFirst();
        if (cursorPos == saveCursorPos) {
            return true;
        } else {
            cursorPos = saveCursorPos;
            absolutePos = saveAbsoluteCursorPos;
            return false;
        }
    }
    public boolean isLast() throws SQLException {
        int saveCursorPos = cursorPos;
        int saveAbsoluteCursorPos = absolutePos;
        boolean saveShowDeleted = getShowDeleted();
        setShowDeleted(true);
        internalLast();
        if (cursorPos == saveCursorPos) {
            setShowDeleted(saveShowDeleted);
            return true;
        } else {
            setShowDeleted(saveShowDeleted);
            cursorPos = saveCursorPos;
            absolutePos = saveAbsoluteCursorPos;
            return false;
        }
    }
    public void beforeFirst() throws SQLException {
       if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.beforefirst").toString());
        }
        cursorPos = 0;
        absolutePos = 0;
        notifyCursorMoved();
    }
    public void afterLast() throws SQLException {
        if (numRows > 0) {
            cursorPos = numRows + 1;
            absolutePos = 0;
            notifyCursorMoved();
        }
    }
    public boolean first() throws SQLException {
        if(getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.first").toString());
        }
        boolean ret = this.internalFirst();
        notifyCursorMoved();
        return ret;
    }
    protected boolean internalFirst() throws SQLException {
        boolean ret = false;
        if (numRows > 0) {
            cursorPos = 1;
            if ((getShowDeleted() == false) && (rowDeleted() == true)) {
                ret = internalNext();
            } else {
                ret = true;
            }
        }
        if (ret == true)
            absolutePos = 1;
        else
            absolutePos = 0;
        return ret;
    }
    public boolean last() throws SQLException {
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.last").toString());
        }
        boolean ret = this.internalLast();
        notifyCursorMoved();
        return ret;
    }
    protected boolean internalLast() throws SQLException {
        boolean ret = false;
        if (numRows > 0) {
            cursorPos = numRows;
            if ((getShowDeleted() == false) && (rowDeleted() == true)) {
                ret = internalPrevious();
            } else {
                ret = true;
            }
        }
        if (ret == true)
            absolutePos = numRows - numDeleted;
        else
            absolutePos = 0;
        return ret;
    }
    public int getRow() throws SQLException {
        if (numRows > 0 &&
        cursorPos > 0 &&
        cursorPos < (numRows + 1) &&
        (getShowDeleted() == false && rowDeleted() == false)) {
            return absolutePos;
        } else if (getShowDeleted() == true) {
            return cursorPos;
        } else {
            return 0;
        }
    }
    public boolean absolute( int row ) throws SQLException {
        if (row == 0 || getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.absolute").toString());
        }
        if (row > 0) { 
            if (row > numRows) {
                afterLast();
                return false;
            } else {
                if (absolutePos <= 0)
                    internalFirst();
            }
        } else { 
            if (cursorPos + row < 0) {
                beforeFirst();
                return false;
            } else {
                if (absolutePos >= 0)
                    internalLast();
            }
        }
        while (absolutePos != row) {
            if (absolutePos < row) {
                if (!internalNext())
                    break;
            }
            else {
                if (!internalPrevious())
                    break;
            }
        }
        notifyCursorMoved();
        if (isAfterLast() || isBeforeFirst()) {
            return false;
        } else {
            return true;
        }
    }
    public boolean relative(int rows) throws SQLException {
        if (numRows == 0 || isBeforeFirst() ||
        isAfterLast() || getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.relative").toString());
        }
        if (rows == 0) {
            return true;
        }
        if (rows > 0) { 
            if (cursorPos + rows > numRows) {
                afterLast();
            } else {
                for (int i=0; i < rows; i++) {
                    if (!internalNext())
                        break;
                }
            }
        } else { 
            if (cursorPos + rows < 0) {
                beforeFirst();
            } else {
                for (int i=rows; i < 0; i++) {
                    if (!internalPrevious())
                        break;
                }
            }
        }
        notifyCursorMoved();
        if (isAfterLast() || isBeforeFirst()) {
            return false;
        } else {
            return true;
        }
    }
    public boolean previous() throws SQLException {
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.last").toString());
        }
        if (cursorPos < 0 || cursorPos > numRows + 1) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        boolean ret = this.internalPrevious();
        notifyCursorMoved();
        return ret;
    }
    protected boolean internalPrevious() throws SQLException {
        boolean ret = false;
        do {
            if (cursorPos > 1) {
                --cursorPos;
                ret = true;
            } else if (cursorPos == 1) {
                --cursorPos;
                ret = false;
                break;
            }
        } while ((getShowDeleted() == false) && (rowDeleted() == true));
        if (ret == true)
            --absolutePos;
        else
            absolutePos = 0;
        return ret;
    }
    public boolean rowUpdated() throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }
        return(((Row)getCurrentRow()).getUpdated());
    }
    public boolean columnUpdated(int idx) throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }
        return (((Row)getCurrentRow()).getColUpdated(idx - 1));
    }
    public boolean columnUpdated(String columnName) throws SQLException {
        return columnUpdated(getColIdxByName(columnName));
    }
    public boolean rowInserted() throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }
        return(((Row)getCurrentRow()).getInserted());
    }
    public boolean rowDeleted() throws SQLException {
        if (isAfterLast() == true ||
        isBeforeFirst() == true ||
        onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        return(((Row)getCurrentRow()).getDeleted());
    }
    private boolean isNumeric(int type) {
        switch (type) {
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
            case java.sql.Types.REAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
                return true;
            default:
                return false;
        }
    }
    private boolean isString(int type) {
        switch (type) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                return true;
            default:
                return false;
        }
    }
    private boolean isBinary(int type) {
        switch (type) {
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }
    private boolean isTemporal(int type) {
        switch (type) {
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                return true;
            default:
                return false;
        }
    }
    private boolean isBoolean(int type) {
        switch (type) {
            case java.sql.Types.BIT:
            case java.sql.Types.BOOLEAN:
                return true;
            default:
                return false;
        }
    }
    private Object convertNumeric(Object srcObj, int srcType,
    int trgType) throws SQLException {
        if (srcType == trgType) {
            return srcObj;
        }
        if (isNumeric(trgType) == false && isString(trgType) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString() + trgType);
        }
        try {
            switch (trgType) {
                case java.sql.Types.BIT:
                    Integer i = Integer.valueOf(srcObj.toString().trim());
                    return i.equals(Integer.valueOf((int)0)) ?
                    Boolean.valueOf(false) :
                        Boolean.valueOf(true);
                case java.sql.Types.TINYINT:
                    return Byte.valueOf(srcObj.toString().trim());
                case java.sql.Types.SMALLINT:
                    return Short.valueOf(srcObj.toString().trim());
                case java.sql.Types.INTEGER:
                    return Integer.valueOf(srcObj.toString().trim());
                case java.sql.Types.BIGINT:
                    return Long.valueOf(srcObj.toString().trim());
                case java.sql.Types.NUMERIC:
                case java.sql.Types.DECIMAL:
                    return new BigDecimal(srcObj.toString().trim());
                case java.sql.Types.REAL:
                case java.sql.Types.FLOAT:
                    return new Float(srcObj.toString().trim());
                case java.sql.Types.DOUBLE:
                    return new Double(srcObj.toString().trim());
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    return srcObj.toString();
                default:
                    throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString()+ trgType);
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString() + trgType);
        }
    }
    private Object convertTemporal(Object srcObj,
    int srcType, int trgType) throws SQLException {
        if (srcType == trgType) {
            return srcObj;
        }
        if (isNumeric(trgType) == true ||
        (isString(trgType) == false && isTemporal(trgType) == false)) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        try {
            switch (trgType) {
                case java.sql.Types.DATE:
                    if (srcType == java.sql.Types.TIMESTAMP) {
                        return new java.sql.Date(((java.sql.Timestamp)srcObj).getTime());
                    } else {
                        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
                    }
                case java.sql.Types.TIMESTAMP:
                    if (srcType == java.sql.Types.TIME) {
                        return new Timestamp(((java.sql.Time)srcObj).getTime());
                    } else {
                        return new Timestamp(((java.sql.Date)srcObj).getTime());
                    }
                case java.sql.Types.TIME:
                    if (srcType == java.sql.Types.TIMESTAMP) {
                        return new Time(((java.sql.Timestamp)srcObj).getTime());
                    } else {
                        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
                    }
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    return srcObj.toString();
                default:
                    throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
    }
    private Object convertBoolean(Object srcObj, int srcType,
    int trgType) throws SQLException {
        if (srcType == trgType) {
            return srcObj;
        }
        if (isNumeric(trgType) == true ||
        (isString(trgType) == false && isBoolean(trgType) == false)) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        try {
            switch (trgType) {
                case java.sql.Types.BIT:
                    Integer i = Integer.valueOf(srcObj.toString().trim());
                    return i.equals(Integer.valueOf((int)0)) ?
                    Boolean.valueOf(false) :
                        Boolean.valueOf(true);
                case java.sql.Types.BOOLEAN:
                    return Boolean.valueOf(srcObj.toString().trim());
                default:
                    throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString()+ trgType);
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString() + trgType);
        }
    }
    public void updateNull(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        BaseRow row = getCurrentRow();
        row.setColumnObject(columnIndex, null);
    }
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertBoolean(Boolean.valueOf(x),
        java.sql.Types.BIT,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateByte(int columnIndex, byte x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(Byte.valueOf(x),
        java.sql.Types.TINYINT,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateShort(int columnIndex, short x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(Short.valueOf(x),
        java.sql.Types.SMALLINT,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateInt(int columnIndex, int x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(Integer.valueOf(x),
        java.sql.Types.INTEGER,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateLong(int columnIndex, long x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(Long.valueOf(x),
        java.sql.Types.BIGINT,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateFloat(int columnIndex, float x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(new Float(x),
        java.sql.Types.REAL,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateDouble(int columnIndex, double x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(new Double(x),
        java.sql.Types.DOUBLE,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertNumeric(x,
        java.sql.Types.NUMERIC,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateString(int columnIndex, String x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertTemporal(x,
        java.sql.Types.DATE,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertTemporal(x,
        java.sql.Types.TIME,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        Object obj = convertTemporal(x,
        java.sql.Types.TIMESTAMP,
        RowSetMD.getColumnType(columnIndex));
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if (isString(RowSetMD.getColumnType(columnIndex)) == false &&
        isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        byte buf[] = new byte[length];
        try {
            int charsRead = 0;
            do {
                charsRead += x.read(buf, charsRead, length - charsRead);
            } while (charsRead != length);
        } catch (java.io.IOException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.asciistream").toString());
        }
        String str = new String(buf);
        getCurrentRow().setColumnObject(columnIndex, str);
    }
    public void updateBinaryStream(int columnIndex, java.io.InputStream x,int length) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if (isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        byte buf[] = new byte[length];
        try {
            int bytesRead = 0;
            do {
                bytesRead += x.read(buf, bytesRead, length - bytesRead);
            } while (bytesRead != -1);
        } catch (java.io.IOException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.binstream").toString());
        }
        getCurrentRow().setColumnObject(columnIndex, buf);
    }
    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if (isString(RowSetMD.getColumnType(columnIndex)) == false &&
        isBinary(RowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        char buf[] = new char[length];
        try {
            int charsRead = 0;
            do {
                charsRead += x.read(buf, charsRead, length - charsRead);
            } while (charsRead != length);
        } catch (java.io.IOException ex) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.binstream").toString());
        }
        String str = new String(buf);
        getCurrentRow().setColumnObject(columnIndex, str);
    }
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        int type = RowSetMD.getColumnType(columnIndex);
        if (type == Types.DECIMAL || type == Types.NUMERIC) {
            ((java.math.BigDecimal)x).setScale(scale);
        }
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    public void updateObject(int columnIndex, Object x) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    public void updateNull(String columnName) throws SQLException {
        updateNull(getColIdxByName(columnName));
    }
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        updateBoolean(getColIdxByName(columnName), x);
    }
    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(getColIdxByName(columnName), x);
    }
    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(getColIdxByName(columnName), x);
    }
    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(getColIdxByName(columnName), x);
    }
    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(getColIdxByName(columnName), x);
    }
    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(getColIdxByName(columnName), x);
    }
    public void updateDouble(String columnName, double x) throws SQLException {
        updateDouble(getColIdxByName(columnName), x);
    }
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        updateBigDecimal(getColIdxByName(columnName), x);
    }
    public void updateString(String columnName, String x) throws SQLException {
        updateString(getColIdxByName(columnName), x);
    }
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        updateBytes(getColIdxByName(columnName), x);
    }
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        updateDate(getColIdxByName(columnName), x);
    }
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        updateTime(getColIdxByName(columnName), x);
    }
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
        updateTimestamp(getColIdxByName(columnName), x);
    }
    public void updateAsciiStream(String columnName,
    java.io.InputStream x,
    int length) throws SQLException {
        updateAsciiStream(getColIdxByName(columnName), x, length);
    }
    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        updateBinaryStream(getColIdxByName(columnName), x, length);
    }
    public void updateCharacterStream(String columnName,
    java.io.Reader reader,
    int length) throws SQLException {
        updateCharacterStream(getColIdxByName(columnName), reader, length);
    }
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        updateObject(getColIdxByName(columnName), x, scale);
    }
    public void updateObject(String columnName, Object x) throws SQLException {
        updateObject(getColIdxByName(columnName), x);
    }
    public void insertRow() throws SQLException {
        int pos;
        if (onInsertRow == false ||
            insertRow.isCompleteRow(RowSetMD) == false) {
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.failedins").toString());
        }
        Object [] toInsert = getParams();
        for(int i = 0;i < toInsert.length; i++) {
          insertRow.setColumnObject(i+1,toInsert[i]);
        }
        Row insRow = new Row(RowSetMD.getColumnCount(),
        insertRow.getOrigRow());
        insRow.setInserted();
        if (currentRow >= numRows || currentRow < 0) {
            pos = numRows;
        } else {
            pos = currentRow;
        }
        rvh.add(pos, insRow);
        ++numRows;
        notifyRowChanged();
    }
    public void updateRow() throws SQLException {
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.updateins").toString());
        }
        ((Row)getCurrentRow()).setUpdated();
        notifyRowChanged();
    }
    public void deleteRow() throws SQLException {
        checkCursor();
        ((Row)getCurrentRow()).setDeleted();
        ++numDeleted;
        notifyRowChanged();
    }
    public void refreshRow() throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        Row currentRow = (Row)getCurrentRow();
        currentRow.clearUpdated();
    }
    public void cancelRowUpdates() throws SQLException {
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getUpdated() == true) {
            currentRow.clearUpdated();
            notifyRowChanged();
        }
    }
    public void moveToInsertRow() throws SQLException {
        if (getConcurrency() == ResultSet.CONCUR_READ_ONLY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.movetoins").toString());
        }
        if (insertRow == null) {
            if (RowSetMD == null)
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.movetoins1").toString());
            int numCols = RowSetMD.getColumnCount();
            if (numCols > 0) {
                insertRow = new InsertRow(numCols);
            } else {
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.movetoins2").toString());
            }
        }
        onInsertRow = true;
        currentRow = cursorPos;
        cursorPos = -1;
        insertRow.initInsertRow();
    }
    public void moveToCurrentRow() throws SQLException {
        if (onInsertRow == false) {
            return;
        } else {
            cursorPos = currentRow;
            onInsertRow = false;
        }
    }
    public Statement getStatement() throws SQLException {
        return null;
    }
     public Object getObject(int columnIndex,
                             java.util.Map<String,Class<?>> map)
         throws SQLException
     {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        if (value instanceof Struct) {
            Struct s = (Struct)value;
            Class c = (Class)map.get(s.getSQLTypeName());
            if (c != null) {
                SQLData obj = null;
                try {
                    obj = (SQLData)c.newInstance();
                } catch (java.lang.InstantiationException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                    ex.getMessage()));
                } catch (java.lang.IllegalAccessException ex) {
                    throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                    ex.getMessage()));
                }
                Object attribs[] = s.getAttributes(map);
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                obj.readSQL(sqlInput, s.getSQLTypeName());
                return (Object)obj;
            }
        }
        return value;
    }
    public Ref getRef(int columnIndex) throws SQLException {
        Ref value;
        checkIndex(columnIndex);
        checkCursor();
        if (RowSetMD.getColumnType(columnIndex) != java.sql.Types.REF) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        setLastValueNull(false);
        value = (Ref)(getCurrentRow().getColumnObject(columnIndex));
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value;
    }
    public Blob getBlob(int columnIndex) throws SQLException {
        Blob value;
        checkIndex(columnIndex);
        checkCursor();
        if (RowSetMD.getColumnType(columnIndex) != java.sql.Types.BLOB) {
            System.out.println(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.type").toString(), RowSetMD.getColumnType(columnIndex)));
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        setLastValueNull(false);
        value = (Blob)(getCurrentRow().getColumnObject(columnIndex));
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value;
    }
    public Clob getClob(int columnIndex) throws SQLException {
        Clob value;
        checkIndex(columnIndex);
        checkCursor();
        if (RowSetMD.getColumnType(columnIndex) != java.sql.Types.CLOB) {
            System.out.println(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.type").toString(), RowSetMD.getColumnType(columnIndex)));
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        setLastValueNull(false);
        value = (Clob)(getCurrentRow().getColumnObject(columnIndex));
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value;
    }
    public Array getArray(int columnIndex) throws SQLException {
        java.sql.Array value;
        checkIndex(columnIndex);
        checkCursor();
        if (RowSetMD.getColumnType(columnIndex) != java.sql.Types.ARRAY) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        setLastValueNull(false);
        value = (java.sql.Array)(getCurrentRow().getColumnObject(columnIndex));
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value;
    }
    public Object getObject(String columnName,
                            java.util.Map<String,Class<?>> map)
    throws SQLException {
        return getObject(getColIdxByName(columnName), map);
    }
    public Ref getRef(String colName) throws SQLException {
        return getRef(getColIdxByName(colName));
    }
    public Blob getBlob(String colName) throws SQLException {
        return getBlob(getColIdxByName(colName));
    }
    public Clob getClob(String colName) throws SQLException {
        return getClob(getColIdxByName(colName));
    }
    public Array getArray(String colName) throws SQLException {
        return getArray(getColIdxByName(colName));
    }
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        value = convertTemporal(value,
        RowSetMD.getColumnType(columnIndex),
        java.sql.Types.DATE);
        Calendar defaultCal = Calendar.getInstance();
        defaultCal.setTime((java.util.Date)value);
        cal.set(Calendar.YEAR, defaultCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, defaultCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, defaultCal.get(Calendar.DAY_OF_MONTH));
        return new java.sql.Date(cal.getTime().getTime());
    }
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        return getDate(getColIdxByName(columnName), cal);
    }
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        value = convertTemporal(value,
        RowSetMD.getColumnType(columnIndex),
        java.sql.Types.TIME);
        Calendar defaultCal = Calendar.getInstance();
        defaultCal.setTime((java.util.Date)value);
        cal.set(Calendar.HOUR_OF_DAY, defaultCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, defaultCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, defaultCal.get(Calendar.SECOND));
        return new java.sql.Time(cal.getTime().getTime());
    }
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        return getTime(getColIdxByName(columnName), cal);
    }
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        Object value;
        checkIndex(columnIndex);
        checkCursor();
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        value = convertTemporal(value,
        RowSetMD.getColumnType(columnIndex),
        java.sql.Types.TIMESTAMP);
        Calendar defaultCal = Calendar.getInstance();
        defaultCal.setTime((java.util.Date)value);
        cal.set(Calendar.YEAR, defaultCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, defaultCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, defaultCal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, defaultCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, defaultCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, defaultCal.get(Calendar.SECOND));
        return new java.sql.Timestamp(cal.getTime().getTime());
    }
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return getTimestamp(getColIdxByName(columnName), cal);
    }
    public Connection getConnection() throws SQLException{
        return conn;
    }
    public void setMetaData(RowSetMetaData md) throws SQLException {
        RowSetMD =(RowSetMetaDataImpl) md;
    }
    public ResultSet getOriginal() throws SQLException {
        CachedRowSetImpl crs = new CachedRowSetImpl();
        crs.RowSetMD = RowSetMD;
        crs.numRows = numRows;
        crs.cursorPos = 0;
        int colCount = RowSetMD.getColumnCount();
        Row orig;
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            orig = new Row(colCount, ((Row)i.next()).getOrigRow());
            crs.rvh.add(orig);
        }
        return (ResultSet)crs;
    }
    public ResultSet getOriginalRow() throws SQLException {
        CachedRowSetImpl crs = new CachedRowSetImpl();
        crs.RowSetMD = RowSetMD;
        crs.numRows = 1;
        crs.cursorPos = 0;
        crs.setTypeMap(this.getTypeMap());
        Row orig = new Row(RowSetMD.getColumnCount(),
        getCurrentRow().getOrigRow());
        crs.rvh.add(orig);
        return (ResultSet)crs;
    }
    public void setOriginalRow() throws SQLException {
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }
        Row row = (Row)getCurrentRow();
        makeRowOriginal(row);
        if (row.getDeleted() == true) {
            removeCurrentRow();
        }
    }
    private void makeRowOriginal(Row row) {
        if (row.getInserted() == true) {
            row.clearInserted();
        }
        if (row.getUpdated() == true) {
            row.moveCurrentToOrig();
        }
    }
    public void setOriginal() throws SQLException {
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            Row row = (Row)i.next();
            makeRowOriginal(row);
            if (row.getDeleted() == true) {
                i.remove();
                --numRows;
            }
        }
        numDeleted = 0;
        notifyRowSetChanged();
    }
    public String getTableName() throws SQLException {
        return tableName;
    }
    public void setTableName(String tabName) throws SQLException {
        if (tabName == null)
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.tablename").toString());
        else
            tableName = tabName;
    }
    public int[] getKeyColumns() throws SQLException {
        return keyCols;
    }
    public void setKeyColumns(int [] keys) throws SQLException {
        int numCols = 0;
        if (RowSetMD != null) {
            numCols = RowSetMD.getColumnCount();
            if (keys.length > numCols)
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.keycols").toString());
        }
        keyCols = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (RowSetMD != null && (keys[i] <= 0 ||
            keys[i] > numCols)) {
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcol").toString() +
                keys[i]);
            }
            keyCols[i] = keys[i];
        }
    }
    public void updateRef(int columnIndex, java.sql.Ref ref) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        getCurrentRow().setColumnObject(columnIndex, new SerialRef(ref));
    }
    public void updateRef(String columnName, java.sql.Ref ref) throws SQLException {
        updateRef(getColIdxByName(columnName), ref);
    }
    public void updateClob(int columnIndex, Clob c) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if(dbmslocatorsUpdateCopy){
           getCurrentRow().setColumnObject(columnIndex, new SerialClob(c));
        }
        else{
           throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.opnotsupp").toString());
        }
    }
    public void updateClob(String columnName, Clob c) throws SQLException {
        updateClob(getColIdxByName(columnName), c);
    }
    public void updateBlob(int columnIndex, Blob b) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        if(dbmslocatorsUpdateCopy){
           getCurrentRow().setColumnObject(columnIndex, new SerialBlob(b));
        }
        else{
           throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.opnotsupp").toString());
        }
    }
    public void updateBlob(String columnName, Blob b) throws SQLException {
        updateBlob(getColIdxByName(columnName), b);
    }
    public void updateArray(int columnIndex, Array a) throws SQLException {
        checkIndex(columnIndex);
        checkCursor();
        getCurrentRow().setColumnObject(columnIndex, new SerialArray(a));
    }
    public void updateArray(String columnName, Array a) throws SQLException {
        updateArray(getColIdxByName(columnName), a);
    }
    public java.net.URL getURL(int columnIndex) throws SQLException {
        java.net.URL value;
        checkIndex(columnIndex);
        checkCursor();
        if (RowSetMD.getColumnType(columnIndex) != java.sql.Types.DATALINK) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
        }
        setLastValueNull(false);
        value = (java.net.URL)(getCurrentRow().getColumnObject(columnIndex));
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        return value;
    }
    public java.net.URL getURL(String columnName) throws SQLException {
        return getURL(getColIdxByName(columnName));
    }
    public RowSetWarning getRowSetWarnings() {
        try {
            notifyCursorMoved();
        } catch (SQLException e) {} 
        return rowsetWarning;
    }
    private String buildTableName(String command) throws SQLException {
        int indexFrom, indexComma;
        String strTablename ="";
        command = command.trim();
        if(command.toLowerCase().startsWith("select")) {
            indexFrom = command.toLowerCase().indexOf("from");
            indexComma = command.indexOf(",", indexFrom);
            if(indexComma == -1) {
                strTablename = (command.substring(indexFrom+"from".length(),command.length())).trim();
                String tabName = strTablename;
                int idxWhere = tabName.toLowerCase().indexOf("where");
                if(idxWhere != -1)
                {
                   tabName = tabName.substring(0,idxWhere).trim();
                }
                strTablename = tabName;
            } else {
            }
        } else if(command.toLowerCase().startsWith("insert")) {
        } else if(command.toLowerCase().startsWith("update")) {
        }
        return strTablename;
    }
    public void commit() throws SQLException {
        conn.commit();
    }
    public void rollback() throws SQLException {
        conn.rollback();
    }
    public void rollback(Savepoint s) throws SQLException {
        conn.rollback(s);
    }
    public void unsetMatchColumn(int[] columnIdxes) throws SQLException {
         int i_val;
         for( int j= 0 ;j < columnIdxes.length; j++) {
            i_val = (Integer.parseInt(iMatchColumns.get(j).toString()));
            if(columnIdxes[j] != i_val) {
               throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols").toString());
            }
         }
         for( int i = 0;i < columnIdxes.length ;i++) {
            iMatchColumns.set(i,Integer.valueOf(-1));
         }
    }
    public void unsetMatchColumn(String[] columnIdxes) throws SQLException {
        for(int j = 0 ;j < columnIdxes.length; j++) {
           if( !columnIdxes[j].equals(strMatchColumns.get(j)) ){
              throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols").toString());
           }
        }
        for(int i = 0 ; i < columnIdxes.length; i++) {
           strMatchColumns.set(i,null);
        }
    }
    public String[] getMatchColumnNames() throws SQLException {
        String []str_temp = new String[strMatchColumns.size()];
        if( strMatchColumns.get(0) == null) {
           throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.setmatchcols").toString());
        }
        strMatchColumns.copyInto(str_temp);
        return str_temp;
    }
    public int[] getMatchColumnIndexes() throws SQLException {
        Integer []int_temp = new Integer[iMatchColumns.size()];
        int [] i_temp = new int[iMatchColumns.size()];
        int i_val;
        i_val = ((Integer)iMatchColumns.get(0)).intValue();
        if( i_val == -1 ) {
           throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.setmatchcols").toString());
        }
        iMatchColumns.copyInto(int_temp);
        for(int i = 0; i < int_temp.length; i++) {
           i_temp[i] = (int_temp[i]).intValue();
        }
        return i_temp;
    }
    public void setMatchColumn(int[] columnIdxes) throws SQLException {
        for(int j = 0 ; j < columnIdxes.length; j++) {
           if( columnIdxes[j] < 0 ) {
              throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols1").toString());
           }
        }
        for(int i = 0 ;i < columnIdxes.length; i++) {
           iMatchColumns.add(i,Integer.valueOf(columnIdxes[i]));
        }
    }
    public void setMatchColumn(String[] columnNames) throws SQLException {
        for(int j = 0; j < columnNames.length; j++) {
           if( columnNames[j] == null || columnNames[j].equals("")) {
              throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols2").toString());
           }
        }
        for( int i = 0; i < columnNames.length; i++) {
           strMatchColumns.add(i,columnNames[i]);
        }
    }
    public void setMatchColumn(int columnIdx) throws SQLException {
        if(columnIdx < 0) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols1").toString());
        } else {
            iMatchColumns.set(0, Integer.valueOf(columnIdx));
        }
    }
    public void setMatchColumn(String columnName) throws SQLException {
        if(columnName == null || (columnName= columnName.trim()).equals("") ) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.matchcols2").toString());
        } else {
            strMatchColumns.set(0, columnName);
        }
    }
    public void unsetMatchColumn(int columnIdx) throws SQLException {
        if(! iMatchColumns.get(0).equals(Integer.valueOf(columnIdx) )  ) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.unsetmatch").toString());
        } else if(strMatchColumns.get(0) != null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.unsetmatch1").toString());
        } else {
               iMatchColumns.set(0, Integer.valueOf(-1));
        }
    }
    public void unsetMatchColumn(String columnName) throws SQLException {
        columnName = columnName.trim();
        if(!((strMatchColumns.get(0)).equals(columnName))) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.unsetmatch").toString());
        } else if( ((Integer)(iMatchColumns.get(0))).intValue() > 0) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.unsetmatch2").toString());
        } else {
            strMatchColumns.set(0, null);   
        }
    }
    public void rowSetPopulated(RowSetEvent event, int numRows) throws SQLException {
        if( numRows < 0 || numRows < getFetchSize()) {
           throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.numrows").toString());
        }
        if(size() % numRows == 0) {
            RowSetEvent event_temp = new RowSetEvent(this);
            event = event_temp;
            notifyRowSetChanged();
        }
    }
     public void populate(ResultSet data, int start) throws SQLException{
        int rowsFetched;
        Row currentRow;
        int numCols;
        int i;
        Map<String, Class<?>> map = getTypeMap();
        Object obj;
        int mRows;
        cursorPos = 0;
        if(populatecallcount == 0){
            if(start < 0){
               throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.startpos").toString());
            }
            if(getMaxRows() == 0){
               data.absolute(start);
               while(data.next()){
                   totalRows++;
               }
               totalRows++;
            }
            startPos = start;
        }
        populatecallcount = populatecallcount +1;
        resultSet = data;
        if((endPos - startPos) >= getMaxRows() && (getMaxRows() > 0)){
            endPos = prevEndPos;
            pagenotend = false;
            return;
        }
        if((maxRowsreached != getMaxRows() || maxRowsreached != totalRows) && pagenotend) {
           startPrev = start - getPageSize();
        }
        if( pageSize == 0){
           prevEndPos = endPos;
           endPos = start + getMaxRows() ;
        }
        else{
            prevEndPos = endPos;
            endPos = start + getPageSize();
        }
        if (start == 1){
            resultSet.beforeFirst();
        }
        else {
            resultSet.absolute(start -1);
        }
        if( pageSize == 0) {
           rvh = new Vector<Object>(getMaxRows());
        }
        else{
            rvh = new Vector<Object>(getPageSize());
        }
        if (data == null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.populate").toString());
        }
        RSMD = data.getMetaData();
        RowSetMD = new RowSetMetaDataImpl();
        initMetaData(RowSetMD, RSMD);
        RSMD = null;
        numCols = RowSetMD.getColumnCount();
        mRows = this.getMaxRows();
        rowsFetched = 0;
        currentRow = null;
        if(!data.next() && mRows == 0){
            endPos = prevEndPos;
            pagenotend = false;
            return;
        }
        data.previous();
        while ( data.next()) {
            currentRow = new Row(numCols);
          if(pageSize == 0){
            if ( rowsFetched >= mRows && mRows > 0) {
                rowsetWarning.setNextException(new SQLException("Populating rows "
                + "setting has exceeded max row setting"));
                break;
            }
          }
          else {
              if ( (rowsFetched >= pageSize) ||( maxRowsreached >= mRows && mRows > 0)) {
                rowsetWarning.setNextException(new SQLException("Populating rows "
                + "setting has exceeded max row setting"));
                break;
            }
          }
            for ( i = 1; i <= numCols; i++) {
                if (map == null) {
                    obj = data.getObject(i);
                } else {
                    obj = data.getObject(i, map);
                }
                if (obj instanceof Struct) {
                    obj = new SerialStruct((Struct)obj, map);
                } else if (obj instanceof SQLData) {
                    obj = new SerialStruct((SQLData)obj, map);
                } else if (obj instanceof Blob) {
                    obj = new SerialBlob((Blob)obj);
                } else if (obj instanceof Clob) {
                    obj = new SerialClob((Clob)obj);
                } else if (obj instanceof java.sql.Array) {
                    obj = new SerialArray((java.sql.Array)obj, map);
                }
                ((Row)currentRow).initColumnObject(i, obj);
            }
            rowsFetched++;
            maxRowsreached++;
            rvh.add(currentRow);
        }
        numRows = rowsFetched ;
        notifyRowSetChanged();
     }
     public boolean nextPage() throws SQLException {
         if (populatecallcount == 0){
             throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.nextpage").toString());
         }
         onFirstPage = false;
         if(callWithCon){
            crsReader.setStartPosition(endPos);
            crsReader.readData((RowSetInternal)this);
            resultSet = null;
         }
         else {
            populate(resultSet,endPos);
         }
         return pagenotend;
     }
     public void setPageSize (int size) throws SQLException {
        if (size < 0) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.pagesize").toString());
        }
        if (size > getMaxRows() && getMaxRows() != 0) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.pagesize1").toString());
        }
        pageSize = size;
     }
    public int getPageSize() {
        return pageSize;
    }
    public boolean previousPage() throws SQLException {
        int pS;
        int mR;
        int rem;
        pS = getPageSize();
        mR = maxRowsreached;
        if (populatecallcount == 0){
             throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.nextpage").toString());
         }
        if( !callWithCon){
           if(resultSet.getType() == ResultSet.TYPE_FORWARD_ONLY){
               throw new SQLException (resBundle.handleGetObject("cachedrowsetimpl.fwdonly").toString());
           }
        }
        pagenotend = true;
        if(startPrev < startPos ){
                onFirstPage = true;
               return false;
            }
        if(onFirstPage){
            return false;
        }
        rem = mR % pS;
        if(rem == 0){
            maxRowsreached -= (2 * pS);
            if(callWithCon){
                crsReader.setStartPosition(startPrev);
                crsReader.readData((RowSetInternal)this);
                resultSet = null;
            }
            else {
               populate(resultSet,startPrev);
            }
            return true;
        }
        else
        {
            maxRowsreached -= (pS + rem);
            if(callWithCon){
                crsReader.setStartPosition(startPrev);
                crsReader.readData((RowSetInternal)this);
                resultSet = null;
            }
            else {
               populate(resultSet,startPrev);
            }
            return true;
        }
    }
    public void setRowInserted(boolean insertFlag) throws SQLException {
        checkCursor();
        if(onInsertRow == true)
          throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        if( insertFlag ) {
          ((Row)getCurrentRow()).setInserted();
        } else {
          ((Row)getCurrentRow()).clearInserted();
        }
    }
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public SQLXML getSQLXML(String colName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public RowId getRowId(String columnName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateRowId(String columnName, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public int getHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public boolean isClosed() throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateNString(String columnName, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateNClob(String columnName, NClob nClob) throws SQLException {
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public NClob getNClob(int i) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public NClob getNClob(String colName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
        return null;
    }
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        return false;
    }
     public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
   public void setRowId(String parameterName, RowId x) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
     public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
     }
    public void setNClob(String parameterName, NClob value) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
    public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException {
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
    public java.io.Reader getNCharacterStream(String columnName) throws SQLException {
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
     }
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public void updateSQLXML(String columnName, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public String getNString(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
    public String getNString(String columnName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
    }
       public void updateNCharacterStream(int columnIndex,
                            java.io.Reader x,
                            long length)
                            throws SQLException {
          throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
       }
       public void updateNCharacterStream(String columnName,
                            java.io.Reader x,
                            long length)
                            throws SQLException {
          throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.opnotysupp").toString());
       }
    public void updateNCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateNCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(int columnIndex,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(String columnLabel,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(int columnIndex,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(String columnLabel,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(int columnIndex,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(String columnLabel,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(int columnIndex,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(String columnLabel,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(int columnIndex,
                           java.io.InputStream x,
                           long length) throws SQLException {
    }
    public void updateBinaryStream(int columnIndex,
                            java.io.InputStream x,
                            long length) throws SQLException {
    }
    public void updateCharacterStream(int columnIndex,
                             java.io.Reader x,
                             long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(String columnLabel,
                             java.io.Reader reader,
                             long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(String columnLabel,
                           java.io.InputStream x,
                           long length) throws SQLException {
    }
    public void updateBinaryStream(String columnLabel,
                            java.io.InputStream x,
                            long length) throws SQLException {
    }
    public void updateBinaryStream(int columnIndex,
                            java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateBinaryStream(String columnLabel,
                            java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(int columnIndex,
                           java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(String columnLabel,
                           java.io.InputStream x) throws SQLException {
    }
  public void setURL(int parameterIndex, java.net.URL x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setNClob(int parameterIndex, Reader reader)
    throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
            public void setNClob(String parameterName, Reader reader, long length)
    throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setNClob(String parameterName, Reader reader)
    throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
     public void setNClob(int parameterIndex, Reader reader, long length)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
     public void setNClob(int parameterIndex, NClob value) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setNString(int parameterIndex, String value) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
 public void setNString(String parameterName, String value)
         throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
 public void setNCharacterStream(String parameterName, Reader value, long length)
         throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setNCharacterStream(String parameterName, Reader value) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
      public  void setClob(String parameterName, Reader reader, long length)
      throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setClob (String parameterName, Clob x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setClob(String parameterName, Reader reader)
      throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setDate(String parameterName, java.sql.Date x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setDate(String parameterName, java.sql.Date x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setTime(String parameterName, java.sql.Time x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setTime(String parameterName, java.sql.Time x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setClob(int parameterIndex, Reader reader)
     throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setClob(int parameterIndex, Reader reader, long length)
     throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(int parameterIndex, InputStream inputStream)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
     public void setBlob(String parameterName, InputStream inputStream, long length)
        throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBlob (String parameterName, Blob x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(String parameterName, InputStream inputStream)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    public void setObject(String parameterName, Object x, int targetSqlType)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setObject(String parameterName, Object x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setAsciiStream(String parameterName, java.io.InputStream x, int length)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBinaryStream(String parameterName, java.io.InputStream x,
                        int length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setCharacterStream(String parameterName,
                           java.io.Reader reader,
                           int length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
  public void setAsciiStream(String parameterName, java.io.InputStream x)
          throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBinaryStream(String parameterName, java.io.InputStream x)
   throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setCharacterStream(String parameterName,
                         java.io.Reader reader) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setString(String parameterName, String x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBytes(String parameterName, byte x[]) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setTimestamp(String parameterName, java.sql.Timestamp x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setNull(String parameterName, int sqlType) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setNull (String parameterName, int sqlType, String typeName)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setBoolean(String parameterName, boolean x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setByte(String parameterName, byte x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setShort(String parameterName, short x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setInt(String parameterName, int x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setLong(String parameterName, long x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setFloat(String parameterName, float x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
   public void setDouble(String parameterName, double x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("cachedrowsetimpl.featnotsupp").toString());
   }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }
    static final long serialVersionUID =1884577171200622428L;
}
