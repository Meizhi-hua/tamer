public class JDBCStatement implements java.sql.Statement {
    protected JDBCConnection conn;
    protected JDBCResultSet rs;
    protected int updcnt;
    private ArrayList<String> batch;
    public JDBCStatement(JDBCConnection conn) {
    this.conn = conn;
    this.updcnt = 0;
    this.rs = null;
    this.batch = null;    
    }
    public void setFetchSize(int fetchSize) throws SQLException {
    throw new SQLException("not supported");
    }
    public int getFetchSize() throws SQLException {
    return 1;
    }
    public int getMaxRows() throws SQLException {
    return 0;
    }
    public void setMaxRows(int max) throws SQLException {
    throw new SQLException("not supported");
    }
    public void setFetchDirection(int fetchDirection) throws SQLException {
    throw new SQLException("not supported");
    }
    public int getFetchDirection() throws SQLException {
    return ResultSet.FETCH_UNKNOWN;
    }
    public int getResultSetConcurrency() throws SQLException {
    return ResultSet.CONCUR_READ_ONLY;
    }
    public int getResultSetType() throws SQLException {
    return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }
    public void setQueryTimeout(int seconds) throws SQLException {
    conn.timeout = seconds * 1000;
    if (conn.timeout < 0) {
        conn.timeout = 120000;
    } else if (conn.timeout < 1000) {
        conn.timeout = 5000;
    }
    }
    public int getQueryTimeout() throws SQLException {
    return conn.timeout;
    }
    public ResultSet getResultSet() throws SQLException {
    return rs;
    }
    ResultSet executeQuery(String sql, String args[], boolean updonly)
    throws SQLException {
    SQLite.TableResult tr = null;
    if (rs != null) {
        rs.close();
        rs = null;
    }
    updcnt = -1;
    if (conn == null || conn.db == null) {
        throw new SQLException("stale connection");
    }
    int busy = 0;
    boolean starttrans = !conn.autocommit && !conn.intrans;
    while (true) {
        try {
        if (starttrans) {
            conn.db.exec("BEGIN TRANSACTION", null);
            conn.intrans = true;
        }
        if (args == null) {
            if (updonly) {
            conn.db.exec(sql, null);
            } else {
            tr = conn.db.get_table(sql);
            }
        } else {
            if (updonly) {
            conn.db.exec(sql, null, args);
            } else {
            tr = conn.db.get_table(sql, args);
            }
        }
        updcnt = (int) conn.db.changes();
        } catch (SQLite.Exception e) {
        if (conn.db.is3() &&
            conn.db.last_error() == SQLite.Constants.SQLITE_BUSY &&
            conn.busy3(conn.db, ++busy)) {
            try {
            if (starttrans && conn.intrans) {
                conn.db.exec("ROLLBACK", null);
                conn.intrans = false;
            }
            } catch (SQLite.Exception ee) {
            }
            try {
            int ms = 20 + busy * 10;
            if (ms > 1000) {
                ms = 1000;
            }
            synchronized (this) {
                this.wait(ms);
            }
            } catch (java.lang.Exception eee) {
            }
            continue;
        }
        throw new SQLException(e.toString());
        }
        break;
    }
    if (!updonly && tr == null) {
        throw new SQLException("no result set produced");
    }
    if (!updonly && tr != null) {
        rs = new JDBCResultSet(new TableResultX(tr), this);
    }
    return rs;
    }
    public ResultSet executeQuery(String sql) throws SQLException {
    return executeQuery(sql, null, false);
    }
    public boolean execute(String sql) throws SQLException {
    return executeQuery(sql) != null;
    }
    public void cancel() throws SQLException {
    if (conn == null || conn.db == null) {
        throw new SQLException("stale connection");
    }
    conn.db.interrupt();
    }
    public void clearWarnings() throws SQLException {
    }
    public Connection getConnection() throws SQLException {
    return conn;
    }
    public void addBatch(String sql) throws SQLException {
    if (batch == null) {
        batch = new ArrayList<String>(1);
    }
    batch.add(sql);
    }
    public int[] executeBatch() throws SQLException {
    if (batch == null) {
        return new int[0];
    }
    int[] ret = new int[batch.size()];
    for (int i = 0; i < ret.length; i++) {
        ret[i] = EXECUTE_FAILED;
    }
    int errs = 0;
    for (int i = 0; i < ret.length; i++) {
        try {
        execute((String) batch.get(i));
        ret[i] = updcnt;
        } catch (SQLException e) {
        ++errs;
        }
    }
    if (errs > 0) {
        throw new BatchUpdateException("batch failed", ret);
    }
    return ret;
    }
    public void clearBatch() throws SQLException {
    if (batch != null) {
        batch.clear();
        batch = null;
    }
    }
    public void close() throws SQLException {
    clearBatch();
    conn = null;
    }
    public int executeUpdate(String sql) throws SQLException {
    executeQuery(sql, null, true);
    return updcnt;
    }
    public int getMaxFieldSize() throws SQLException {
    return 0;
    }
    public boolean getMoreResults() throws SQLException {
    if (rs != null) {
        rs.close();
        rs = null;
    }
    return false;
    }
    public int getUpdateCount() throws SQLException {
    return updcnt;
    }
    public SQLWarning getWarnings() throws SQLException {
    return null;
    }
    public void setCursorName(String name) throws SQLException {
    throw new SQLException("not supported");
    }
    public void setEscapeProcessing(boolean enable) throws SQLException {
    throw new SQLException("not supported");
    }
    public void setMaxFieldSize(int max) throws SQLException {
    throw new SQLException("not supported");
    }
    public boolean getMoreResults(int x) throws SQLException {
    throw new SQLException("not supported");
    }
    public ResultSet getGeneratedKeys() throws SQLException {
    throw new SQLException("not supported");
    }
    public int executeUpdate(String sql, int autokeys)
    throws SQLException {
    if (autokeys != Statement.NO_GENERATED_KEYS) {
        throw new SQLException("not supported");
    }
    return executeUpdate(sql);
    }
    public int executeUpdate(String sql, int colIndexes[])
    throws SQLException {
    throw new SQLException("not supported");
    }
    public int executeUpdate(String sql, String colIndexes[])
    throws SQLException {
    throw new SQLException("not supported");
    }
    public boolean execute(String sql, int autokeys)
    throws SQLException {
    if (autokeys != Statement.NO_GENERATED_KEYS) {
        throw new SQLException("not supported");
    }
    return execute(sql);
    }
    public boolean execute(String sql, int colIndexes[])
    throws SQLException {
    throw new SQLException("not supported");
    }
    public boolean execute(String sql, String colIndexes[])
    throws SQLException {
    throw new SQLException("not supported");
    }
    public int getResultSetHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }
}
