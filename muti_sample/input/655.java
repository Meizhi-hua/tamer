public class XmlReaderContentHandler extends DefaultHandler {
    private HashMap propMap;
    private HashMap colDefMap;
    private HashMap dataMap;
    private HashMap typeMap;
    private Vector updates;
    private Vector keyCols;
    private String columnValue;
    private String propertyValue;
    private String metaDataValue;
    private int tag;
    private int state;
    private WebRowSetImpl rs;
    private boolean nullVal;
    private boolean emptyStringVal;
    private RowSetMetaData md;
    private int idx;
    private String lastval;
    private String Key_map;
    private String Value_map;
    private String tempStr;
    private String tempUpdate;
    private String tempCommand;
    private Object [] upd;
    private String [] properties = {"command", "concurrency", "datasource",
                            "escape-processing", "fetch-direction", "fetch-size",
                            "isolation-level", "key-columns", "map",
                            "max-field-size", "max-rows", "query-timeout",
                            "read-only", "rowset-type", "show-deleted",
                            "table-name", "url", "null", "column", "type",
                            "class", "sync-provider", "sync-provider-name",
                             "sync-provider-vendor", "sync-provider-version",
                             "sync-provider-grade","data-source-lock"};
    private final static int CommandTag = 0;
    private final static int ConcurrencyTag = 1;
    private final static int DatasourceTag = 2;
    private final static int EscapeProcessingTag = 3;
    private final static int FetchDirectionTag = 4;
    private final static int FetchSizeTag = 5;
    private final static int IsolationLevelTag = 6;
    private final static int KeycolsTag = 7;
    private final static int MapTag = 8;
    private final static int MaxFieldSizeTag = 9;
    private final static int MaxRowsTag = 10;
    private final static int QueryTimeoutTag = 11;
    private final static int ReadOnlyTag = 12;
    private final static int RowsetTypeTag = 13;
    private final static int ShowDeletedTag = 14;
    private final static int TableNameTag = 15;
    private final static int UrlTag = 16;
    private final static int PropNullTag = 17;
    private final static int PropColumnTag = 18;
    private final static int PropTypeTag = 19;
    private final static int PropClassTag = 20;
    private final static int SyncProviderTag = 21;
    private final static int SyncProviderNameTag = 22;
    private final static int SyncProviderVendorTag = 23;
    private final static int SyncProviderVersionTag = 24;
    private final static int SyncProviderGradeTag = 25;
    private final static int DataSourceLock = 26;
    private String [] colDef = {"column-count", "column-definition", "column-index",
                        "auto-increment", "case-sensitive", "currency",
                        "nullable", "signed", "searchable",
                        "column-display-size", "column-label", "column-name",
                        "schema-name", "column-precision", "column-scale",
                        "table-name", "catalog-name", "column-type",
                        "column-type-name", "null"};
    private final static int ColumnCountTag = 0;
    private final static int ColumnDefinitionTag = 1;
    private final static int ColumnIndexTag = 2;
    private final static int AutoIncrementTag = 3;
    private final static int CaseSensitiveTag = 4;
    private final static int CurrencyTag = 5;
    private final static int NullableTag = 6;
    private final static int SignedTag = 7;
    private final static int SearchableTag = 8;
    private final static int ColumnDisplaySizeTag = 9;
    private final static int ColumnLabelTag = 10;
    private final static int ColumnNameTag = 11;
    private final static int SchemaNameTag = 12;
    private final static int ColumnPrecisionTag = 13;
    private final static int ColumnScaleTag = 14;
    private final static int MetaTableNameTag = 15;
    private final static int CatalogNameTag = 16;
    private final static int ColumnTypeTag = 17;
    private final static int ColumnTypeNameTag = 18;
    private final static int MetaNullTag = 19;
    private String [] data = {"currentRow", "columnValue", "insertRow", "deleteRow", "insdel", "updateRow", "null" , "emptyString"};
    private final static int RowTag = 0;
    private final static int ColTag = 1;
    private final static int InsTag = 2;
    private final static int DelTag = 3;
    private final static int InsDelTag = 4;
    private final static int UpdTag = 5;
    private final static int NullTag = 6;
    private final static int EmptyStringTag = 7;
    private final static int INITIAL = 0;
    private final static int PROPERTIES = 1;
    private final static int METADATA = 2;
    private final static int DATA = 3;
    private  JdbcRowSetResourceBundle resBundle;
    public XmlReaderContentHandler(RowSet r) {
        rs = (WebRowSetImpl)r;
        initMaps();
        updates = new Vector();
        columnValue = "";
        propertyValue = "";
        metaDataValue = "";
        nullVal = false;
        idx = 0;
        tempStr = "";
        tempUpdate = "";
        tempCommand = "";
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    private void initMaps() {
        int items, i;
        propMap = new HashMap();
        items = properties.length;
        for (i=0;i<items;i++) {
            propMap.put(properties[i], Integer.valueOf(i));
        }
        colDefMap = new HashMap();
        items = colDef.length;
        for (i=0;i<items;i++) {
            colDefMap.put(colDef[i], Integer.valueOf(i));
        }
        dataMap = new HashMap();
        items = data.length;
        for (i=0;i<items;i++) {
            dataMap.put(data[i], Integer.valueOf(i));
        }
        typeMap = new HashMap();
    }
    public void startDocument() throws SAXException {
    }
    public void endDocument() throws SAXException {
    }
    public void startElement(String uri, String lName, String qName, Attributes attributes) throws SAXException {
        int tag;
        String name = "";
        name = lName;
        switch (getState()) {
        case PROPERTIES:
            tempCommand = "";
            tag = ((Integer)propMap.get(name)).intValue();
            if (tag == PropNullTag)
               setNullValue(true);
            else
                setTag(tag);
            break;
        case METADATA:
            tag = ((Integer)colDefMap.get(name)).intValue();
            if (tag == MetaNullTag)
                setNullValue(true);
            else
                setTag(tag);
            break;
        case DATA:
            tempStr = "";
            tempUpdate = "";
            if(dataMap.get(name) == null) {
                tag = NullTag;
            } else if(((Integer)dataMap.get(name)).intValue() == EmptyStringTag) {
                tag = EmptyStringTag;
            } else {
                 tag = ((Integer)dataMap.get(name)).intValue();
            }
            if (tag == NullTag) {
                setNullValue(true);
            } else if(tag == EmptyStringTag) {
                setEmptyStringValue(true);
            } else {
                setTag(tag);
                if (tag == RowTag || tag == DelTag || tag == InsTag) {
                    idx = 0;
                    try {
                        rs.moveToInsertRow();
                    } catch (SQLException ex) {
                        ;
                    }
                }
            }
            break;
        default:
            setState(name);
        }
    }
    public void endElement(String uri, String lName, String qName) throws SAXException {
        int tag;
        String name = "";
        name = lName;
        switch (getState()) {
        case PROPERTIES:
            if (name.equals("properties")) {
                state = INITIAL;
                break;
            }
            try {
                tag = ((Integer)propMap.get(name)).intValue();
                switch (tag) {
                case KeycolsTag:
                    if (keyCols != null) {
                        int i[] = new int[keyCols.size()];
                        for (int j = 0; j < i.length; j++)
                            i[j] = Integer.parseInt((String)keyCols.elementAt(j));
                        rs.setKeyColumns(i);
                    }
                    break;
                 case PropClassTag:
                     try{
                          typeMap.put(Key_map,Class.forName(Value_map));
                        }catch(ClassNotFoundException ex) {
                          throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errmap").toString(), ex.getMessage()));
                        }
                      break;
                 case MapTag:
                      rs.setTypeMap(typeMap);
                      break;
                default:
                    break;
                }
                if (getNullValue()) {
                    setPropertyValue(null);
                    setNullValue(false);
                } else {
                    setPropertyValue(propertyValue);
                }
            } catch (SQLException ex) {
                throw new SAXException(ex.getMessage());
            }
            propertyValue = "";
            setTag(-1);
            break;
        case METADATA:
            if (name.equals("metadata")) {
                try {
                    rs.setMetaData(md);
                    state = INITIAL;
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errmetadata").toString(), ex.getMessage()));
                }
            } else {
                try {
                    if (getNullValue()) {
                        setMetaDataValue(null);
                        setNullValue(false);
                    } else {
                        setMetaDataValue(metaDataValue);
                    }
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errmetadata").toString(), ex.getMessage()));
                }
                metaDataValue = "";
            }
            setTag(-1);
            break;
        case DATA:
            if (name.equals("data")) {
                state = INITIAL;
                return;
            }
            if(dataMap.get(name) == null) {
                tag = NullTag;
            } else {
                 tag = ((Integer)dataMap.get(name)).intValue();
            }
            switch (tag) {
            case ColTag:
                try {
                    idx++;
                    if (getNullValue()) {
                        insertValue(null);
                        setNullValue(false);
                    } else {
                        insertValue(tempStr);
                    }
                    columnValue = "";
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errinsertval").toString(), ex.getMessage()));
                }
                break;
            case RowTag:
                try {
                    rs.insertRow();
                    rs.moveToCurrentRow();
                    rs.next();
                    rs.setOriginalRow();
                    applyUpdates();
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errconstr").toString(), ex.getMessage()));
                }
                break;
            case DelTag:
                try {
                    rs.insertRow();
                    rs.moveToCurrentRow();
                    rs.next();
                    rs.setOriginalRow();
                    applyUpdates();
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errdel").toString() , ex.getMessage()));
                }
                break;
            case InsTag:
                try {
                    rs.insertRow();
                    rs.moveToCurrentRow();
                    rs.next();
                    applyUpdates();
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errinsert").toString() , ex.getMessage()));
                }
                break;
            case InsDelTag:
                try {
                    rs.insertRow();
                    rs.moveToCurrentRow();
                    rs.next();
                    rs.setOriginalRow();
                    applyUpdates();
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errinsdel").toString() , ex.getMessage()));
                }
                break;
             case UpdTag:
                 try {
                        if(getNullValue())
                         {
                          insertValue(null);
                          setNullValue(false);
                         } else if(getEmptyStringValue()) {
                               insertValue("");
                               setEmptyStringValue(false);
                         } else {
                            updates.add(upd);
                         }
                 }  catch(SQLException ex) {
                        throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errupdate").toString() , ex.getMessage()));
                 }
                break;
            default:
                break;
            }
        default:
            break;
        }
    }
    private void applyUpdates() throws SAXException {
        if (updates.size() > 0) {
            try {
                Object upd[];
                Iterator i = updates.iterator();
                while (i.hasNext()) {
                    upd = (Object [])i.next();
                    idx = ((Integer)upd[0]).intValue();
                   if(!(lastval.equals(upd[1]))){
                       insertValue((String)(upd[1]));
                    }
                }
                rs.updateRow();
                } catch (SQLException ex) {
                    throw new SAXException(MessageFormat.format(resBundle.handleGetObject("xmlrch.errupdrow").toString() , ex.getMessage()));
                }
            updates.removeAllElements();
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            switch (getState()) {
            case PROPERTIES:
                propertyValue = new String(ch, start, length);
                tempCommand = tempCommand.concat(propertyValue);
                propertyValue = tempCommand;
                if(tag == PropTypeTag)
                {
                        Key_map = propertyValue;
                }
                else if(tag == PropClassTag)
                {
                        Value_map = propertyValue;
                }
                break;
            case METADATA:
                if (tag == -1)
                {
                        break;
                }
                metaDataValue = new String(ch, start, length);
                break;
            case DATA:
                setDataValue(ch, start, length);
                break;
            default:
                ;
            }
        } catch (SQLException ex) {
            throw new SAXException(resBundle.handleGetObject("xmlrch.chars").toString() + ex.getMessage());
        }
    }
    private void setState(String s) throws SAXException {
        if (s.equals("webRowSet")) {
            state = INITIAL;
        } else if (s.equals("properties")) {
            if (state != PROPERTIES)
                state = PROPERTIES;
            else
                state = INITIAL;
        } else if (s.equals("metadata")) {
            if (state != METADATA)
                state = METADATA;
            else
                state = INITIAL;
        } else if (s.equals("data")) {
            if (state != DATA)
                state = DATA;
            else
                state = INITIAL;
        }
    }
    private int getState() {
        return state;
    }
    private void setTag(int t) {
        tag = t;
    }
    private int getTag() {
        return tag;
    }
    private void setNullValue(boolean n) {
        nullVal = n;
    }
    private boolean getNullValue() {
        return nullVal;
    }
    private void setEmptyStringValue(boolean e) {
        emptyStringVal = e;
    }
    private boolean getEmptyStringValue() {
        return emptyStringVal;
    }
    private String getStringValue(String s) {
         return s;
    }
    private int getIntegerValue(String s) {
        return Integer.parseInt(s);
    }
    private boolean getBooleanValue(String s) {
        return Boolean.valueOf(s).booleanValue();
    }
    private java.math.BigDecimal getBigDecimalValue(String s) {
        return new java.math.BigDecimal(s);
    }
    private byte getByteValue(String s) {
        return Byte.parseByte(s);
    }
    private short getShortValue(String s) {
        return Short.parseShort(s);
    }
    private long getLongValue(String s) {
        return Long.parseLong(s);
    }
    private float getFloatValue(String s) {
        return Float.parseFloat(s);
    }
    private double getDoubleValue(String s) {
        return Double.parseDouble(s);
    }
    private byte[] getBinaryValue(String s) {
        return s.getBytes();
    }
    private java.sql.Date getDateValue(String s) {
        return new java.sql.Date(getLongValue(s));
    }
    private java.sql.Time getTimeValue(String s) {
        return new java.sql.Time(getLongValue(s));
    }
    private java.sql.Timestamp getTimestampValue(String s) {
        return new java.sql.Timestamp(getLongValue(s));
    }
    private void setPropertyValue(String s) throws SQLException {
        boolean nullValue = getNullValue();
        switch(getTag()) {
        case CommandTag:
            if (nullValue)
               ; 
            else
                rs.setCommand(s);
            break;
        case ConcurrencyTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setConcurrency(getIntegerValue(s));
            break;
        case DatasourceTag:
            if (nullValue)
                rs.setDataSourceName(null);
            else
                rs.setDataSourceName(s);
            break;
        case EscapeProcessingTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setEscapeProcessing(getBooleanValue(s));
            break;
        case FetchDirectionTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setFetchDirection(getIntegerValue(s));
            break;
        case FetchSizeTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setFetchSize(getIntegerValue(s));
            break;
        case IsolationLevelTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setTransactionIsolation(getIntegerValue(s));
            break;
        case KeycolsTag:
            break;
        case PropColumnTag:
            if (keyCols == null)
                keyCols = new Vector();
            keyCols.add(s);
            break;
        case MapTag:
            break;
        case MaxFieldSizeTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setMaxFieldSize(getIntegerValue(s));
            break;
        case MaxRowsTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setMaxRows(getIntegerValue(s));
            break;
        case QueryTimeoutTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setQueryTimeout(getIntegerValue(s));
            break;
        case ReadOnlyTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setReadOnly(getBooleanValue(s));
            break;
        case RowsetTypeTag:
            if (nullValue) {
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            } else {
                String strType = getStringValue(s);
                int iType = 0;
                if(strType.trim().equals("ResultSet.TYPE_SCROLL_INSENSITIVE")) {
                   iType = 1004;
                } else if(strType.trim().equals("ResultSet.TYPE_SCROLL_SENSITIVE"))   {
                   iType = 1005;
                } else if(strType.trim().equals("ResultSet.TYPE_FORWARD_ONLY")) {
                   iType = 1003;
                }
                rs.setType(iType);
            }
            break;
        case ShowDeletedTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue").toString());
            else
                rs.setShowDeleted(getBooleanValue(s));
            break;
        case TableNameTag:
            if (nullValue)
                ;
            else
                rs.setTableName(s);
            break;
        case UrlTag:
            if (nullValue)
                rs.setUrl(null);
            else
                rs.setUrl(s);
            break;
        case SyncProviderNameTag:
            if (nullValue) {
                rs.setSyncProvider(null);
            } else {
                String str = s.substring(0,s.indexOf("@")+1);
                rs.setSyncProvider(str);
            }
            break;
        case SyncProviderVendorTag:
            break;
        case SyncProviderVersionTag:
            break;
        case SyncProviderGradeTag:
            break;
        case DataSourceLock:
            break;
        default:
            break;
        }
    }
    private void setMetaDataValue(String s) throws SQLException {
        boolean nullValue = getNullValue();
        switch (getTag()) {
        case ColumnCountTag:
            md = new RowSetMetaDataImpl();
            idx = 0;
            if (nullValue) {
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            } else {
                md.setColumnCount(getIntegerValue(s));
            }
            break;
        case ColumnDefinitionTag:
            break;
        case ColumnIndexTag:
            idx++;
            break;
        case AutoIncrementTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setAutoIncrement(idx, getBooleanValue(s));
            break;
        case CaseSensitiveTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setCaseSensitive(idx, getBooleanValue(s));
            break;
        case CurrencyTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setCurrency(idx, getBooleanValue(s));
            break;
        case NullableTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setNullable(idx, getIntegerValue(s));
            break;
        case SignedTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setSigned(idx, getBooleanValue(s));
            break;
        case SearchableTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setSearchable(idx, getBooleanValue(s));
            break;
        case ColumnDisplaySizeTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setColumnDisplaySize(idx, getIntegerValue(s));
            break;
        case ColumnLabelTag:
            if (nullValue)
                md.setColumnLabel(idx, null);
            else
                md.setColumnLabel(idx, s);
            break;
        case ColumnNameTag:
            if (nullValue)
                md.setColumnName(idx, null);
            else
                md.setColumnName(idx, s);
            break;
        case SchemaNameTag:
            if (nullValue) {
                md.setSchemaName(idx, null); }
            else
                md.setSchemaName(idx, s);
            break;
        case ColumnPrecisionTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setPrecision(idx, getIntegerValue(s));
            break;
        case ColumnScaleTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setScale(idx, getIntegerValue(s));
            break;
        case MetaTableNameTag:
            if (nullValue)
                md.setTableName(idx, null);
            else
                md.setTableName(idx, s);
            break;
        case CatalogNameTag:
            if (nullValue)
                md.setCatalogName(idx, null);
            else
                md.setCatalogName(idx, s);
            break;
        case ColumnTypeTag:
            if (nullValue)
                throw new SQLException(resBundle.handleGetObject("xmlrch.badvalue1").toString());
            else
                md.setColumnType(idx, getIntegerValue(s));
            break;
        case ColumnTypeNameTag:
            if (nullValue)
                md.setColumnTypeName(idx, null);
            else
                md.setColumnTypeName(idx, s);
            break;
        default:
            break;
        }
    }
    private void setDataValue(char[] ch, int start, int len) throws SQLException {
        switch (getTag()) {
        case ColTag:
            columnValue = new String(ch, start, len);
            tempStr = tempStr.concat(columnValue);
            break;
        case UpdTag:
            upd = new Object[2];
            tempUpdate = tempUpdate.concat(new String(ch,start,len));
            upd[0] = Integer.valueOf(idx);
            upd[1] = tempUpdate;
            lastval = (String)upd[1];
            break;
        case InsTag:
        }
    }
    private void insertValue(String s) throws SQLException {
        if (getNullValue()) {
            rs.updateNull(idx);
            return;
        }
        int type = rs.getMetaData().getColumnType(idx);
        switch (type) {
        case java.sql.Types.BIT:
            rs.updateBoolean(idx, getBooleanValue(s));
            break;
        case java.sql.Types.BOOLEAN:
            rs.updateBoolean(idx, getBooleanValue(s));
            break;
        case java.sql.Types.SMALLINT:
        case java.sql.Types.TINYINT:
            rs.updateShort(idx, getShortValue(s));
            break;
        case java.sql.Types.INTEGER:
            rs.updateInt(idx, getIntegerValue(s));
            break;
        case java.sql.Types.BIGINT:
            rs.updateLong(idx, getLongValue(s));
            break;
        case java.sql.Types.REAL:
        case java.sql.Types.FLOAT:
            rs.updateFloat(idx, getFloatValue(s));
            break;
        case java.sql.Types.DOUBLE:
            rs.updateDouble(idx, getDoubleValue(s));
            break;
        case java.sql.Types.NUMERIC:
        case java.sql.Types.DECIMAL:
            rs.updateObject(idx, getBigDecimalValue(s));
            break;
        case java.sql.Types.BINARY:
        case java.sql.Types.VARBINARY:
        case java.sql.Types.LONGVARBINARY:
            rs.updateBytes(idx, getBinaryValue(s));
            break;
        case java.sql.Types.DATE:
            rs.updateDate(idx,  getDateValue(s));
            break;
        case java.sql.Types.TIME:
            rs.updateTime(idx, getTimeValue(s));
            break;
        case java.sql.Types.TIMESTAMP:
            rs.updateTimestamp(idx, getTimestampValue(s));
            break;
        case java.sql.Types.CHAR:
        case java.sql.Types.VARCHAR:
        case java.sql.Types.LONGVARCHAR:
            rs.updateString(idx, getStringValue(s));
            break;
        default:
        }
    }
    public void error (SAXParseException e) throws SAXParseException {
            throw e;
    }
    public void warning (SAXParseException err) throws SAXParseException {
        System.out.println (MessageFormat.format(resBundle.handleGetObject("xmlrch.warning").toString(), new Object[] { err.getMessage(), err.getLineNumber(), err.getSystemId() }));
    }
    public void notationDecl(String name, String publicId, String systemId) {
    }
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) {
    }
    private Row getPresentRow(WebRowSetImpl rs) throws SQLException {
         return null;
   }
}
