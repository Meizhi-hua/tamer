@TestTargetClass(Statement.class)
public class StressTest extends TestCase {
    Vector<Connection> vc = new Vector<Connection>();
    private static Connection conn;
    private static Statement statement;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        conn = Support_SQL.getConnection();
        statement = conn.createStatement();
        createTestTables();
        vc.clear();
    }
    protected void tearDown() throws Exception {
        closeConnections();
        statement.close();
        conn.close();
        super.tearDown();
    }
    private void createTestTables() {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE2)) {
                    statement.execute(DatabaseCreator.DROP_TABLE2);
                }
            }
            statement.execute(DatabaseCreator.CREATE_TABLE2);
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
        return;
    }
    private void dropTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE2);
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
        return;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "connect",
        clazz = Driver.class,
        args = {String.class, Properties.class}
    )
    public void testManyConnectionsUsingOneThread() {
        try {
            int maxConnections = getConnectionNum();
            openConnections(maxConnections);
            assertEquals("Incorrect number of created connections",
                    maxConnections, vc.size());
        } catch (Exception e) {
            fail("Unexpected Exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Stress test: Create many connections to the DataBase using some threads",
        method = "connect",
        clazz = Driver.class,
        args = {String.class, Properties.class}
    )
    public void testManyConnectionsUsingManyThreads() {
        int numTasks = getConnectionNum();
        ThreadPool threadPool = new ThreadPool(numTasks);
        for (int i = 0; i < numTasks; i++) {
            threadPool.runTask(createTask(i));
        }
        threadPool.join();
        assertEquals("Unable to create a connection", numTasks, vc.size());
        if (numTasks != Support_SQL.sqlMaxConnections) {
            try {
                Connection c = Support_SQL.getConnection();
                c.close();
                fail("It is possible to create more than " + numTasks
                        + "connections");
            } catch (SQLException sql) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "connect",
        clazz = Driver.class,
        args = {String.class, Properties.class}
    )
    public void testInsertOfManyRowsUsingOneThread() {
        Logger.global
                .info("java.sql stress test: single thread and many operations.");
        int maxConnections = getConnectionNum();
        Logger.global.info("Opening " + maxConnections + " to database "
                + Support_SQL.getFilename());
        openConnections(maxConnections);
        int tasksPerConnection = Support_SQL.sqlMaxTasks / maxConnections;
        Logger.global.info("TasksPerConnection =  " + Support_SQL.sqlMaxTasks
                + " by (maxConnections) " + maxConnections + " = "
                + tasksPerConnection);
        int pk = 1;
        for (int i = 0; i < vc.size(); ++i) {
            Logger.global.info(" creating " + tasksPerConnection
                    + "tasks for Connection " + i);
            Connection c = vc.elementAt(i);
            for (int j = 0; j < tasksPerConnection; ++j) {
                insertNewRecord(c, pk++);
            }
        }
        try {
            ResultSet rs = statement
                    .executeQuery("SELECT COUNT(*) as counter FROM "
                            + DatabaseCreator.TEST_TABLE2);
            assertTrue("RecordSet is empty", rs.next());
            assertEquals("Incorrect number of records", tasksPerConnection
                    * maxConnections, rs.getInt("counter"));
            rs.close();
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "connect",
            clazz = Driver.class,
            args = {String.class, Properties.class}
    )
    public void testInsertOfManyRowsUsingManyThreads() {
        Logger.global.info("java.sql stress test: multiple threads and many operations.");
        int numConnections = getConnectionNum();
        int tasksPerConnection = Support_SQL.sqlMaxTasks / numConnections;
        Logger.global.info("Opening "+numConnections+" to database "+Support_SQL.getFilename());
        ThreadPool threadPool = new ThreadPool(numConnections);
        for (int i = 0; i < numConnections; ++i) {
            Logger.global.info(" creating "+tasksPerConnection+ " tasks for Connection "+i);
            threadPool.runTask(insertTask(numConnections, i));
        }
        threadPool.join();
        assertEquals("Unable to create a connection", numConnections, vc.size());
        try {
            ResultSet rs = statement
                    .executeQuery("SELECT COUNT(*) as counter FROM "
                            + DatabaseCreator.TEST_TABLE2);
            assertTrue("RecordSet is empty", rs.next());
            assertEquals("Incorrect number of records", tasksPerConnection
                    * numConnections, rs.getInt("counter"));
            rs.close();
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
    }
    private int getConnectionNum() {
        int num = Support_SQL.sqlMaxConnections;
        try {
            int mc = conn.getMetaData().getMaxConnections();
            if (mc != 0) {
                if (num != mc) {
                    System.err.println("Will be used no more than " + mc
                            + " connections to the DataBase");
                }
                num = mc;
            }
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
        return num;
    }
    private void openConnections(int maxConnections) {
        int i = 0;
        try {
            for (; i < maxConnections; ++i) {
                Connection c = Support_SQL.getConnection();
                if (c == null) {
                    assertEquals("Unable to create a connection",
                            maxConnections, i);
                }
                vc.add(c);
            }
        } catch (SQLException sql) {
            assertEquals("Unable to create a connection", maxConnections, i);
        }
        return;
    }
    private void closeConnections() {
        int i = 0;
        try {
            for (; i < vc.size(); ++i) {
                vc.elementAt(i).close();
            }
        } catch (SQLException sql) {
            assertEquals("Unable to close a connection", vc.size(), i);
        }
        return;
    }
    private Runnable createTask(final int taskID) {
        return new Runnable() {
            public void run() {
                try {
                    Connection c = Support_SQL.getConnection();
                    if (c == null) {
                        return;
                    }
                    synchronized (this) {
                        vc.add(c);
                    }
                } catch (SQLException sql) {
                }
            }
        };
    }
    private Runnable insertTask(final int numConnections, final int taskID) {
        return new Runnable() {
            public void run() {
                try {
                    Connection c = Support_SQL.getConnection();
                    if (c == null) {
                        return;
                    }
                    synchronized (this) {
                        vc.add(c);
                    }
                    int tasksPerConnection = Support_SQL.sqlMaxTasks
                            / numConnections;
                    for (int i = 0; i < tasksPerConnection; ++i) {
                        insertNewRecord(c, (i + 1) + tasksPerConnection
                                * taskID);
                    }
                } catch (SQLException sql) {
                }
            }
        };
    }
    private void insertNewRecord(Connection c, int pk) {
        String query = "INSERT INTO " + DatabaseCreator.TEST_TABLE2
                + "(finteger, ftext, fcharacter, fdecimal, fnumeric,"
                + " fsmallint, ffloat, freal, fdouble, fdate, ftime)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = c.prepareStatement(query);
            ps.setInt(1, pk);
            ps.setString(2, "text");
            ps.setString(3, "chr");
            ps.setFloat(4, 0.1f);
            ps.setFloat(5, 0.2f);
            ps.setShort(6, (short) 3);
            ps.setFloat(7, 0.4f);
            ps.setDouble(8, 0.5);
            ps.setDouble(9, 0.6);
            ps.setDate(10, new java.sql.Date(System.currentTimeMillis()));
            ps.setTime(11, new java.sql.Time(System.currentTimeMillis()));
            ps.execute();
            ps.close();
        } catch (SQLException sql) {
            fail("Unexpected SQLException " + sql.toString());
        }
        return;
    }
}
