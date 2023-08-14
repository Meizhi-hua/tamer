@TestTargetClass(Database.class)
public class DatabaseTest extends SQLiteTest {
    private static ErrorTracker tracker = null;
    private Statement statement;
    private Database db = null;
    private static final int numThreads = 10;
    private static final int numOfRecords = 30;
    public void setUp() throws java.lang.Exception {
        try {
            super.setUp();
            assertNotNull("Could not establish DB connection",conn);
            tracker = new ErrorTracker();
            statement = conn.createStatement();
            DatabaseMetaData meta = conn.getMetaData();
            assertNotNull(meta);
            if (meta != null) {
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
            String tableName = userTab.getString("TABLE_NAME");
               this.statement.execute("drop table "+tableName);
            }
            }
            statement.execute(DatabaseCreator.CREATE_TABLE_SIMPLE1);
            statement.close();
            try {
            db = new Database();
            db.open(dbFile.getPath(), 0);
            db.busy_handler(null);
            } catch (Exception e) {
                System.out.println("2: Error opening File: Dir "+dbFile.getPath()+" Name: "+dbFile.getPath());
            } catch (java.lang.Exception e) {
                System.err.println("Non SQLException "+e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Database setup fails: "+e.getMessage());
            e.printStackTrace();
        }
    }
    public void tearDown() {
        try {
            db.close();
        }catch (Exception e) {
            if (! (e.getMessage().equals("database already closed"))) {
                System.err.println("Error closing DB "+dbFile.getPath());
            }
        }
        tracker.reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "constructor test",
        method = "Database",
        args = {}
    )
    public void testDatabase() {
        Database db2 = new Database();
        try {
            db.close();
            db2 = new Database();
            db2.open(dbFile.getPath(), 0);
            db2.close();
            db.open(dbFile.getPath(), 0);
        } catch (Exception e) {
            fail("Database object could not be created "+e.getMessage());
            e.printStackTrace();
        }
        try {
            db2.open(dbFile.getPath(), 0);
            db2.close();          
        } catch (Exception e) {
            fail("Second Database object could not be created "+e.getMessage());
            e.printStackTrace();
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "method test",
        method = "finalize",
        args = {}
    )
    public void testFinalize() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test. Test fails.",
        method = "open",
        args = {java.lang.String.class, int.class}
    )
    public void testOpen() {
        try {
            db.close();
            db.open(dbFile.getPath(), 0);
        } catch (Exception e) {
            fail("Database object could not be opened: " + e.getMessage());
            e.printStackTrace();
        }
        Database db2 = new Database();
        try {
            db2.open(dbFile.getPath(), 0);
            db2.open(dbFile.getPath(), 0);
            db2.close();
        } catch (Exception e) {
            fail("Database object could not be opened: " + e.getMessage());
            e.printStackTrace();
        }
        File tempDir = Support_Resources.createTempFolder();
        final String resourceName = "blob.c";
        try {
            URL file = Class.forName(this.getClass().getName())
            .getResource("/blob.c");
            db2.open(file.getPath(), 0);
           fail("Should not be able to open non db file");
        } catch (Exception e) {
            assertEquals("unknown error in open", e.getMessage());
        } catch (java.lang.Exception e) {
            fail("Error in setup " + e.getMessage());
            e.printStackTrace();
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "open_aux_file",
        args = {java.lang.String.class}
    )
    public void testOpen_aux_file() {
        File temp = null;
        try {
            db.open_aux_file("");
            fail("open should fail");
        } catch (Exception e) {
            assertEquals("unsupported", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "close",
        args = {}
    )
    public void testClose() {
        try {
            db.close();
            db.get_table("test");
        } catch (Exception e) {
            assertTrue(e.getMessage().equals("database already closed"));
            try {
                db.open(dbFile.getPath(), 0);
            } catch (Exception e1) {
                fail("Database object could not be reopened after 'close': "
                        + e.getMessage());
                e1.printStackTrace();
            }
        }
        try {
            db.close();
            db.close();
        } catch (Exception e) {
            assertTrue(e.getMessage().equals("database already closed"));
            try {
                db.open(dbFile.getPath(), 0);
            } catch (Exception e1) {
                fail("Database object could not be reopened after 'close': "
                        + e.getMessage());
                e1.printStackTrace();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "exec",
        args = {java.lang.String.class, Callback.class}
    )
    public void testExecStringCallback() {
        TableResult res = new TableResult();
        try {
            db.exec("insert into " + DatabaseCreator.SIMPLE_TABLE1
                    + " VALUES(1, 10, 20)", null);
            db.exec("select * from " + DatabaseCreator.SIMPLE_TABLE1, res);
            db
                    .exec("delete from " + DatabaseCreator.SIMPLE_TABLE1
                            + " where 1", null);
        } catch (Exception e) {
            fail("Database error");
            e.printStackTrace();
        }
        String row[] = (String[]) res.rows.elementAt(0);
        assertEquals(Integer.parseInt(row[0]), 1);
        assertEquals(Integer.parseInt(row[1]), 10);
        assertEquals(Integer.parseInt(row[2]), 20);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "exec",
        args = {java.lang.String.class, Callback.class, java.lang.String[].class}
    )
    public void testExecStringCallbackStringArray() {
        TableResult res = new TableResult();
        String args[] = new String[1];
        args[0] = "table";
        try {
            db.exec("select name from sqlite_master where type = '%q';", res,
                    args);
            String[] s = (String[]) res.rows.elementAt(0);
            assertEquals(s[0], DatabaseCreator.SIMPLE_TABLE1);
        } catch (Exception e) {
            fail("DB Error occurred");
            e.printStackTrace();
        }
        try {
            db.exec("select name from sqlite_master where type = ", res, args);
            fail("Testmethod should fail");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "last_insert_rowid",
        args = {}
    )
    public void testLast_insert_rowid() {
        assertEquals(0, db.last_insert_rowid());
        try {
            db
                    .exec(
                            "create table TEST5(id integer, firstname text, lastname text);",
                            null);
            db.exec("insert into TEST5 values (1,'James','Bond');", null);
            db.exec("insert into TEST5 values (2,'Fiona','Apple');", null);
        } catch (Exception e) {
            fail("Error in test setup: " + e.getMessage());
            e.printStackTrace();
        }
        assertEquals(2, db.last_insert_rowid());
        assertEquals(db.last_insert_rowid(), db.last_insert_rowid());
        try {
            db.exec("drop table TEST5;", null);
        } catch (Exception e) {
            fail("Error in test setup: " + e.getMessage());
            e.printStackTrace();
        }
        assertEquals(2, db.last_insert_rowid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "interrupt",
        args = {}
    )
    @KnownFailure("Reason for failure unknown: Database should be locked. " +
                   "Specification of interrupt is scarce.")
    public void testInterrupt() throws Exception {
        ThreadPool threadPool = new ThreadPool(numThreads);
        ResultSet userTabs;
        try {
            userTabs = conn.getMetaData().getTables(null, null, null, null);
            while (userTabs.next()) {
                String tableName = userTabs.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                }
            }
            db.exec(DatabaseCreator.CREATE_TABLE3, null);
            db.exec(DatabaseCreator.CREATE_TABLE1, null);
        } catch (SQLException e1) {
            fail("Error initializing test " + e1.toString());
            e1.printStackTrace();
        } catch (Exception e) {
            fail("Error initializing test " + e.getMessage());
            e.printStackTrace();
        }
        int id1 = numOfRecords - 3;
        threadPool.runTask(createTask1(id1, dbFile.getPath(), tracker));
        int id2 = numOfRecords + 3;
        threadPool
                .runTask(createTask2Interrupt(id2, dbFile.getPath(), tracker));
        threadPool.join();
        List<String> errors = tracker.getErrors();
        System.out.println("Last error: " + db.error_message());
        if (errors.size() > 0) {
            assertEquals(errors.get(0), db
                    .error_string(Constants.SQLITE_LOCKED));
            for (String s : errors) {
                Logger.global.info("INTERRUPT Error: " + s);
            }
        } else {
            fail("Should have one exception: database should be locked.");
        }
        db
                .exec(
                        "delete from " + DatabaseCreator.TEST_TABLE1
                                + " where 1", null);
        db
                .exec(
                        "delete from " + DatabaseCreator.TEST_TABLE3
                                + " where 1", null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test fails",
        method = "changes",
        args = {}
    )
    @KnownFailure("Returns wrong number for updates: returns value > 1 for select.")
    public void testChanges() {
        TableResult res = new TableResult();
        try {
            assertTrue(db.changes() == 0);
            db.exec("INSERT INTO " + DatabaseCreator.SIMPLE_TABLE1
                    + " VALUES(2, 5, 7);", null);
            int rows = (int) db.changes();
            assertEquals(1,db.changes());
            db.exec("update " + DatabaseCreator.SIMPLE_TABLE1
                    + " set speed = 7, size= 5 where id = 2;", null);
            assertEquals(1,db.changes());
            db.exec("select * from " + DatabaseCreator.SIMPLE_TABLE1, res);
            assertEquals(0,db.changes());
            db.exec("INSERT INTO " + DatabaseCreator.SIMPLE_TABLE1
                    + " VALUES(8, 5, 7);", null);
            db.exec("Update "+DatabaseCreator.SIMPLE_TABLE1+" set speed = 10;",null);
           assertTrue(db.changes() > 2);
        } catch (Exception e) {
            fail("Could not get changes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "method test fails once in a while. Cannot be sure that exception is thrown every time.",
        method = "busy_handler",
        args = {BusyHandler.class}
    ),
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "method test fails once in a while. Cannot be sure that exception is thrown every time.",
            method = "busy",
            clazz = BusyHandler.class,
            args = {java.lang.String.class, int.class}
        )
    })
    @KnownFailure("method test fails once in a while. "+
            "Cannot be sure that exception is thrown in every test execution.")
    public void testBusy_handler() throws SQLException, Exception {
        TestBusyHandler bh = new TestBusyHandler();
        db.busy_handler(bh);
        int counter = 0;
        ThreadPool threadPool = new ThreadPool(numThreads);
        ResultSet userTabs;
        try {
            userTabs = conn.getMetaData().getTables(null, null, null, null);
            while (userTabs.next()) {
                String tableName = userTabs.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                }
            }
            db.exec(DatabaseCreator.CREATE_TABLE3, null);
            db.exec(DatabaseCreator.CREATE_TABLE1, null);
        } catch (SQLException e1) {
            fail("Error initializing test " + e1.toString());
            e1.printStackTrace();
        } catch (Exception e) {
            fail("Error initializing test " + e.getMessage());
            e.printStackTrace();
        }
        try {
            conn.setAutoCommit(false);
            int id1 = numOfRecords - 3;
            threadPool.runTask(createTask1(id1, dbFile.getPath(), tracker));
            int id2 = numOfRecords + 3;
            threadPool.runTask(createTask2(id2, dbFile.getPath(), tracker));
            int oldID = 5;
            int newID = 100;
            threadPool.runTask(createTask3(oldID, dbFile.getPath(), newID,
                    tracker));
            threadPool.join();
            List<String> errors = tracker.getErrors();
            if (errors.size() > 0) {
                for (String s: errors) {
                  System.out.println("Round 2 Error: "+s);
              }
            } else {
                fail("No error happened");
            }
            db.exec("delete from " + DatabaseCreator.TEST_TABLE1 + " where 1",
                    null);
            db.exec("delete from " + DatabaseCreator.TEST_TABLE3 + " where 1",
                            null);
        } catch (Exception e) {
            fail("Error in test setup " + e.getMessage());
            try {
                db.get_table("select * from " + DatabaseCreator.TEST_TABLE1,
                        null).toString();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
            db.exec(DatabaseCreator.DROP_TABLE1, null);
            db.exec(DatabaseCreator.DROP_TABLE3, null);
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "test fails. Cannot be sure that exception is thrown every time.",
        method = "busy_timeout",
        args = {int.class}
    )
    @KnownFailure("Database does not lock values")
    public void testBusy_timeout() throws Exception, SQLException {
        int counter = 0;
        ThreadPool threadPool = new ThreadPool(numThreads);
        ResultSet userTabs;
        try {
            userTabs = conn.getMetaData().getTables(null, null, null, null);
            while (userTabs.next()) {
                String tableName = userTabs.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                }
            }
            db.exec(DatabaseCreator.CREATE_TABLE3, null);
            db.exec(DatabaseCreator.CREATE_TABLE1, null);
        } catch (SQLException e1) {
            fail("Error initializing test " + e1.toString());
            e1.printStackTrace();
        } catch (Exception e) {
            fail("Error initializing test " + e.getMessage());
            e.printStackTrace();
        }
        try {
            conn.setAutoCommit(false);
            db.busy_handler(null);
            db.busy_timeout(0);
            int id1 = numOfRecords - 3;
            threadPool.runTask(createTask2(id1, dbFile.getPath(), tracker));
            int id2 = numOfRecords + 3;
            threadPool.runTask(createTask1(id2, dbFile.getPath(), tracker));
            int oldID = 5;
            int newID = 100;
            threadPool.runTask(createTask3(oldID, dbFile.getPath(), newID,
                    tracker));
            threadPool.join();
            List<String> errors = tracker.getErrors();
            assertTrue("No error occurred on DB but should have",errors.size() > 0);
            assertEquals(errors.get(0),
            db.error_string(Constants.SQLITE_LOCKED));
            assertEquals(errors.get(0), "database is locked");
            db.exec("delete from " + DatabaseCreator.TEST_TABLE1 + " where 1",
                    null);
            db.exec("delete from " + DatabaseCreator.TEST_TABLE3 + " where 1",
                            null);
            db.busy_timeout(10000);
            db.busy_handler(null);
            tracker.reset();
            threadPool = new ThreadPool(numThreads);
            threadPool.runTask(createTask1(id1, dbFile.getPath(), tracker));
            threadPool.runTask(createTask2(id2, dbFile.getPath(), tracker));
            threadPool.join();
            errors = tracker.getErrors();
            if (errors.size() > 0) {
                fail("busy timeout should prevent from lock exception!");
                for (String s: errors) {
                    System.out.println("Round 2 Error"+s);
                }
            } else {
            }
        } catch (Exception e) {
            fail("Error in test setup " + e.getMessage());
            try {
                db.get_table("select * from " + DatabaseCreator.TEST_TABLE1,
                        null).toString();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
            db.exec(DatabaseCreator.DROP_TABLE1, null);
            db.exec(DatabaseCreator.DROP_TABLE3, null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "get_table",
        args = {java.lang.String.class}
    )
    public void testGet_tableString() {
        TableResult emptyTable = new TableResult();
        try {
            TableResult res = db.get_table("select * from "
                    + DatabaseCreator.SIMPLE_TABLE1);
            assertEquals(res.toString(), emptyTable.toString());
            try {
                db.exec("insert into " + DatabaseCreator.SIMPLE_TABLE1
                        + " VALUES(1, 10, 20)", null);
                res = db.get_table("select * from " + DatabaseCreator.SIMPLE_TABLE1);
                db
                        .exec("delete from " + DatabaseCreator.SIMPLE_TABLE1
                                + " where 1", null);
            } catch (Exception e) {
                fail("Database error");
                e.printStackTrace();
            }
            String row[] = (String[]) res.rows.elementAt(0);
            assertEquals(Integer.parseInt(row[0]), 1);
            assertEquals(Integer.parseInt(row[1]), 10);
            assertEquals(Integer.parseInt(row[2]), 20);
        } catch (Exception e) {
            fail("Error getting table " + e.getMessage());
            e.printStackTrace();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "get_table",
        args = {java.lang.String.class, java.lang.String[].class}
    )
    public void testGet_tableStringStringArray() {
        String args[] = new String[1];
        args[0] = "table";
        String argsFail[] = new String[1];
        try {
            TableResult res = db.get_table(
                    "select name from sqlite_master where type = ", argsFail);
            fail("Testmethod should fail");
        } catch (Exception e) {
            try {
                TableResult res = db.get_table(
                        "select name from sqlite_master where type = '%q'",
                        args);
                String[] s = (String[]) res.rows.elementAt(0);
                assertEquals(s[0], DatabaseCreator.SIMPLE_TABLE1);
            } catch (Exception e2) {
                fail("Testmethod failed: " + e2.getMessage());
                e.printStackTrace();
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "get_table",
            args = {java.lang.String.class, java.lang.String[].class, TableResult.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "method test",
                method = "toString",
                clazz = TableResult.class,
                args = {}
            ),
       @TestTargetNew(
                    level = TestLevel.COMPLETE,
                    notes = "method test",
                    method = "types",
                    clazz = TableResult.class,
                    args = {String[].class}
            ),
       @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "method test",
                method = "TableResult",
                clazz = TableResult.class,
                args = {}
           ),
       @TestTargetNew(
               level = TestLevel.NOT_NECESSARY,
               notes = "method test",
               method = "columns",
               clazz = TableResult.class,
               args = {String[].class}
          ),
      @TestTargetNew(
              level = TestLevel.NOT_NECESSARY,
              notes = "method test",
              method = "newrow",
              clazz = TableResult.class,
              args = {String[].class}
         ),
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         notes = "method test",
         method = "clear",
         clazz = TableResult.class,
         args = {}
        )
    })
    public void testGet_tableStringStringArrayTableResult() {
        String args[] = new String[1];
        String argsFail[] = new String[1];
        TableResult res = new TableResult();
        TableResult defaultTableRes = new TableResult();
        args[0] = "table";
        try {
            db.get_table("select name from sqlite_master where type = '%q'",
                    argsFail, res);
            assertEquals(defaultTableRes.toString(), res.toString());
        } catch (Exception e) {
            try {
                db.get_table(
                        "select name from sqlite_master where type = '%q'",
                        args, res);
                String[] s = (String[]) res.rows.elementAt(0);
                assertEquals(s[0], DatabaseCreator.SIMPLE_TABLE1);
                String[] types = res.types;
                System.out
                        .println("DatabaseTest.testGet_tableStringStringArrayTableResult() "+types.toString());
            } catch (Exception e2) {
                fail("Testmethod failed: " + e2.getMessage());
                e.printStackTrace();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "complete",
        args = {java.lang.String.class}
    )
    public void testComplete() {
        assertFalse(db.complete("create"));
        assertTrue(db.complete("create table TEST (res double);"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "version",
        args = {}
    )
    public void testVersion() {
        String version = db.version();
        if (version != null) {
        assertTrue(Integer.parseInt(db.version().substring(0,1)) > 0);
        assertEquals(db.version(), db.version());
        } else {
            fail("DB version info missing");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "dbversion",
        args = {}
    )
    public void testDbversion() {
        String verNo = "";
        try {
            verNo = db.dbversion();
            db.close();
            assertEquals(db.dbversion(),"unknown");
            db.open(dbFile.getPath(), 0);
            assertEquals(verNo,db.dbversion());
        } catch (Exception e) {
            try {
                db.open(dbFile.getPath(), 0);
            } catch (Exception e1) {
                fail("error in db setup "+e.getMessage());
                e.printStackTrace();
            }
            fail("error in db setup "+e.getMessage());
            e.printStackTrace();
        }
        assertTrue(Integer.parseInt(verNo.substring(0, 1))>= 3 );
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "create_function",
        args = {java.lang.String.class, int.class, Function.class}
    ),
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "create_function",
        args = {java.lang.String.class, int.class, Function.class}
        )
    })
    public void testCreate_function() {
        try {
            double input = 1.0;
            db.exec("create table TEST (res double)", null);
            db.exec("insert into TEST values (" + Double.toString(input) + ")",
                    null);
            TableResult res = new TableResult();
            Function sinFunc = (Function) new SinFunc();
            db.create_function("sin", 1, sinFunc);
            db.exec("select sin(res) from TEST WHERE res = "
                    + Double.toString(input), res);
            String row[] = (String[]) res.rows.elementAt(0);
            String val = row[0];
            double sinusVal = Double.parseDouble(val);
            double funcVal = Math.sin(input);
            assertTrue(Math.round(funcVal) == Math.round(sinusVal));
        } catch (Exception e) {
            fail("Error happened creating function:" + e.getMessage());
            e.printStackTrace();
        }
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "create_aggregate",
        args = {java.lang.String.class, int.class, Function.class}
    ),
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "step",
        clazz = Function.class,
        args = {FunctionContext.class, String[].class}
            ),
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "last_step",
        clazz = Function.class,
        args = {FunctionContext.class}
                ),
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "function",
        clazz = Function.class,
        args = {FunctionContext.class, String[].class}
                    )
    })
    @KnownFailure("Aggregation function not called")
    public void testCreate_aggregate() {
        TestTrace t = new TestTrace();
        MockFunction aggFunction = new MockFunction();
        try {
            db
                    .exec(
                            "create table TEST(id integer, firstname text, lastname text)",
                            null);
            db.exec("insert into TEST values(3, 'James', 'Bond'); ", null);
            db.exec("insert into TEST values(4, 'Fiona', 'Apple'); ", null);
            db.trace((Trace) t);
            db.create_aggregate("myaggfunc", 1, aggFunction);
            db.function_type("myaggfunc", Constants.SQLITE_TEXT);
            db.exec("PRAGMA show_datatypes = on", null);
            assertFalse(aggFunction.functionCalled);
            assertFalse(aggFunction.stepCalled);
            assertFalse(aggFunction.lastStepCalled);
            db.exec("select myaggfunc(TEST.firstname) from TEST", t);
            assertTrue(aggFunction.stepCalled);
            assertTrue(aggFunction.lastStepCalled);
            assertTrue(aggFunction.functionCalled);
            assertEquals("James Fiona ",aggFunction.getAggValue());
            db.exec("drop table TEST", null);
        } catch (Exception e) {
            System.out.println(t.getTrace());
            fail("Error in test setup: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            db.create_aggregate("myaggfunc", 0, null);
        } catch (Throwable e) {
            assertEquals("null SQLite.Function not allowed",e.getMessage());
        }
          try {
            db.create_aggregate("myaggfunc", 0, aggFunction);
        } catch (Throwable e) {
            assertEquals("wrong number of arguments to function myaggfunc()",e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Method does not make sense: for functions, return type is already set.",
        method = "function_type",
        args = {java.lang.String.class, int.class}
    )
    public void testFunction_type() throws Exception {
        double input = 1.0;
        TableResult res = new TableResult();
        Function sinFunc = (Function) new SinFunc();
        db.exec("PRAGMA show_datatypes = on", null);
        db.exec("create table TEST (res double)", null);
        db.exec("insert into TEST values (" + Double.toString(input) + ")",
                null);
        db.create_function("sin", 1, sinFunc);
        db.function_type("sin", Constants.SQLITE_NUMERIC);
        res = db.get_table("select sin(res) from TEST WHERE res = "
                + Double.toString(input));
        String row[] = (String[]) res.rows.elementAt(0);
        String val = row[0];
        assertTrue("double".equalsIgnoreCase(res.types[0]));
        assertSame(Math.round(Math.sin(input)), Math.round(Double.parseDouble(val)));
        db.function_type("sin", Constants.SQLITE_BLOB);
        Stmt s = db.prepare("select sin(res) from TEST WHERE res = ?");
        s.bind(1,input);
        s.step();
        res = db.get_table("select sin(res) from TEST WHERE res = "
                + Double.toString(input));
        assertTrue("double".equalsIgnoreCase(res.types[0]));
        row = (String[]) res.rows.elementAt(0);
        val = row[0]; 
        assertSame(Math.round(Math.sin(input)), Math.round(Double.parseDouble(val)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "last_error",
        args = {}
    )
    public void testLast_error() {
        assertEquals(db.last_error(), Constants.SQLITE_OK);
        try {
            db.exec("create table TEST (res double)",null);
            db.exec("create table TEST (res double)",null);
            fail("Error should have happened");
        } catch (Exception e) {
            assertEquals(db.last_error(),db.last_error());
            assertEquals(db.last_error(),Constants.SQLITE_ERROR);         
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "don't now which other errors may occur from black-box approach.",
        method = "set_last_error",
        args = {int.class}
    )
    public void testSet_last_error() {
       assertEquals(db.last_error(), Constants.SQLITE_OK);
       try {
           db.exec("sel from test;", null);
       } catch (Exception e) {
           assertEquals(Constants.SQLITE_ERROR,db.last_error());
       }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "error_message",
        args = {}
    )
    public void testError_message() {
        String statement = "create table TEST (res double)";
        try {
            db.exec(statement,null);
            db.exec(statement,null);
            fail("DB Error expected");
        } catch (Exception e) {
            String dbError = db.error_message();
            assertTrue(e.getMessage().equals(dbError)); 
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "error_string",
        args = {int.class}
    )
    public void testError_string() {
        TestTrace t = new TestTrace();
        assertEquals(db.last_error(), Constants.SQLITE_OK);
        String errorString = db.error_string(Constants.SQLITE_ERROR);
        try {
            db.trace((Trace) t);
            db.exec("create table TEST (res double)", t);
            db.exec("create table TEST (res double)", t);
        } catch (Exception e) {
            assertEquals(db.last_error(), Constants.SQLITE_ERROR);
            if (db.is3()) {
                assertEquals("Unsupported Method (sqlite 3): error_string", db
                        .error_string(db.last_error()), errorString);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test fails.",
        method = "set_encoding",
        args = {java.lang.String.class}
    )
    @KnownFailure("ASCII encoding does not work: a UTF encoded val is returned. Spec is not sufficient. "
            + "Might be that test impl is wrong or String constructor for the ASCII encoding.")
    public void testSet_encoding() throws UnsupportedEncodingException {
        String input = "\u00bfMa\u00f1ana\u003f"; 
        TableResult res = new TableResult();
        String refOutput = null;
        Stmt stat = null;
        try {
            db.exec("create table encodingTest (encoded text DEFAULT NULL);",
                    null);
            stat = db
                    .prepare("insert into encodingTest(encoded) values(:one);");
            stat.bind(1, input);
            stat.step();
            db.exec("select * from encodingTest;", res);
            String[] encInput = (String[]) res.rows.elementAt(0);
            String output = encInput[0];
            assertEquals(input, output);
        } catch (Exception e1) {
            fail("Error in test setup: " + e1.getMessage());
            e1.printStackTrace();
        }
        String[] charsetNames = {"UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE"};
        for (int i = 0; i < charsetNames.length; i++) {
            try {
                byte[] encInput = input.getBytes(charsetNames[i]);
                db.set_encoding(charsetNames[i]);
                db.exec("select * from encodingTest;", res);
                String[] encOutput = (String[]) res.rows.elementAt(0);
                String inputAsString = new String(encInput,charsetNames[i]);
                assertEquals(inputAsString, encOutput[0]);
            } catch (Exception e4) {
                fail("Error setting the encoding." + e4.getMessage());
                e4.printStackTrace();
            } catch (UnsupportedEncodingException e2) {
                fail(e2.getMessage());
                e2.printStackTrace();
            }
        }
        try {
            db.set_encoding("UTF-16");
            db.exec("select * from encodingTest;", res);
            String[] encOutput1 = (String[]) res.rows.elementAt(0);
            assertEquals("Got "+encOutput1[0]+" as UTF-16",input,encOutput1[0]);
            db.set_encoding("US-ASCII");
            db.exec("select * from encodingTest;", res);
            String[] encOutput2 = (String[]) res.rows.elementAt(0);
            assertEquals(new String(input.getBytes(),"US-ASCII"),encOutput2[0]);
        } catch (Exception e) {
            fail("Error setting the encoding." + e.getMessage());
            e.printStackTrace();
        }
        try {
            stat.close();
            db.exec("delete from encodingTest", null);
        } catch (Exception e3) {
            fail("Error in teardown of encoding environment");
            e3.printStackTrace();
        }
        try {
            db.set_encoding("");
            fail("invalid input should fail");
        } catch (Exception e) {
        }
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test fails.",
        method = "set_authorizer",
        args = {Authorizer.class}
    ),
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test fails.",
            method = "authorize",
            clazz = Authorizer.class,
            args = {int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
        )
    })
    @KnownFailure("Callback never made for authorization. "+
            "Results of private table are returned withouth furhter checks.")
    public void testSet_authorizer() {
        TableResult resPriv = null;
        TableResult resPub = null;
        TableResult emptyTable = new TableResult();
        String insertPublic = "insert into public_table values(1,2)";
        String insertPrivate = "insert into private_table values(1,2)";
        try {
            db.exec("create table public_table(c1 integer, c2 integer);", null); 
            db.exec("create table private_table(c1 integer, c2 integer);", null);
            db.exec(insertPublic, null);
            db.exec(insertPrivate, null);
            resPriv = db.get_table("select * from private_table");
            resPub = db.get_table("select * from public_table");
            AuthorizerCallback cb = new AuthorizerCallback();
            db.set_authorizer(cb);
            db.exec("select * from private_table", cb);
            assertTrue(cb.wasCalled());
        } catch (Exception e) {
            fail("Error testing authorization: "+e.getMessage());
        }
        try {
            db.exec(insertPublic, null);
            fail("authorization failed");
        } catch (Exception e) {
            try {
                db.exec(insertPrivate, null);
                fail("authorization failed");
            } catch (Exception e1) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "trace",
        args = {Trace.class}
    )
    public void testTrace() {
        String stmt = "create table TEST (res double);";
        TestTrace t = new TestTrace();
        assertFalse(t.traceCalled);
        assertEquals(db.last_error(),Constants.SQLITE_OK);
        try {
            db.trace((Trace) t);
            db.exec(stmt,t);
            assertTrue(t.traceCalled);
            assertEquals(t.getTrace(),stmt);
        } catch (Exception e) {
            fail("Error testing traces: "+e.getMessage());
            e.printStackTrace();
        }
        try {
            db.close();
            db.exec(stmt,t);
            fail("Exception Expected");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "compile",
        args = {java.lang.String.class}
    )
    public void testCompileString() {
        try {
            db.compile("select name from sqlite_master;");
        } catch (Exception e) {
            fail("Error compiling sql statement " + e.getMessage());
            e.printStackTrace();
        }
        try {
            db.compile("test");
            fail("Compiling of inaccurate statement does not fail.");
        } catch (Exception e) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "compile",
        args = {java.lang.String.class, java.lang.String[].class}
    )
    public void testCompileStringStringArray() {
        String args[] = new String[1];
        args[0] = "table";
        try {
            db.compile("select name from sqlite_master where type = '%q';",args);
        } catch (Exception e) {
            fail("Error compiling sql statement " + e.getMessage());
            e.printStackTrace();
        }
        try {
            db.compile("test",null);
            fail("Compiling of inaccurate statement does not fail.");
        } catch (Exception e) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "prepare",
        args = {java.lang.String.class}
    )
    public void testPrepare() {
        Stmt st = null;
        Stmt st2 = null;
        try {
            st = db.prepare("");
            assertEquals(0, st.bind_parameter_count());
            st.step();
            fail("stmt should not be prepared");
        } catch (Exception e) {
            assertEquals("stmt already closed", e.getMessage());
        }
        try {
            st2 = db.prepare("insert into " + DatabaseCreator.SIMPLE_TABLE1
                    + " values (:one,:two,:three)");
            assertEquals(3, st2.bind_parameter_count());
            assertEquals(3, st2.bind_parameter_index(":three"));
            assertEquals(":two", st2.bind_parameter_name(2));
        } catch (Exception e) {
            fail("error in prepare method: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                st2.close();
            } catch (Exception e) {
                fail("error in prepare method cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            db.prepare("insert into " + DatabaseCreator.SIMPLE_TABLE1
                    + " values(:one,:two,:three,:four);");
        } catch (Exception e) {
            assertEquals("table " + DatabaseCreator.SIMPLE_TABLE1
                    + " has 3 columns but 4 values were supplied", e
                    .getMessage());
        }
        try {
            db.prepare("insert into " + DatabaseCreator.SIMPLE_TABLE1
                    + " values(5, '10, 20);");
        } catch (Exception e) {
            assertEquals("unrecognized token: \"'10, 20);\"", e.getMessage());
        }
        try {
            db.prepare("insert into " + DatabaseCreator.SIMPLE_TABLE1
                    + " values(5, 10 20);");
        } catch (Exception e) {
            assertEquals("near \"20\": syntax error", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "open_blob",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, long.class, boolean.class}
    )
    @KnownFailure("not supported")
    public void testOpen_blob() throws Exception, java.lang.Exception {
        Stmt statement2;
        Blob blobInput = new Blob();
        InputStream inStream = null;
        byte[] in = {(byte) 1, (byte) 2, (byte) 3, (byte) 4};
        db.exec("create table TEST (res blob)",null);
        inStream = Class.forName(this.getClass().getName()).getResourceAsStream("/blob.c");
        assertNotNull(inStream);
        try {
            statement2 = db.prepare("insert into TEST(res) values (?)");
            statement2.bind(1, in);
            statement2.step();
            statement2.close();
        } catch (Exception e) {
            fail("Error happened inserting blob" + e.getMessage());
            e.printStackTrace();
        }
        byte[] output = null;
        Blob blob;
            blob = db.open_blob(dbFile.getPath(), "TEST", "res", 1, true);
            if (blob == null) {
                fail("Blob could not be retrieved");
            }
            InputStream is = blob.getInputStream();
            int i = 0;
            int outByte = 0;
            byte[] out = new byte[4];
            while ((outByte = is.read()) > -1) {
                out[i] = (byte) outByte;
                i++;
            }
            is.close();
            blob.close();
            assertTrue(Arrays.equals(in, out));
            db.exec("insert into TEST values(zeroblob(128))", null);
            Blob blob2 = db.open_blob(dbFile.getPath(), "TEST", "res", 2, true);
            is = blob2.getInputStream();
            for (i = 0; i < 128; i++)  {
               assertEquals(0, is.read());
            }
            is.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "is3",
        args = {}
    )
    public void testIs3() {
        int ver = Integer.parseInt(db.version().substring(0,1));
        if (db.is3()) {
            assertTrue( ver == 3);
        } else {
            assertTrue(ver != 3);
        }
    }
    @TestTargets ({
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "progress_handler",
        args = {int.class, ProgressHandler.class}
    ),
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "progress",
            clazz = ProgressHandler.class,
            args = {}
        )
    })
    public void testProgress_handler() {
        int inputVal = 3;
        TestProgressHandler prog = new TestProgressHandler();
        try {
            db.exec("create table TEST5(id integer, firstname text, lastname text)",null);
            Vm vm = db.compile("select * from TEST5; "
                    + "insert into TEST5 values(3, 'James', 'Bond'); "
                    + "delete from TEST5 where id = 3; "
                    + "select * from TEST5");
            int stmt = 0;
            do {
                ++stmt;
                if (stmt > inputVal) {
                    db.progress_handler(inputVal, prog);
                } else {
                    assertEquals(0, prog.getCounts());
                }
                while (vm.step(prog)) {
                }
            } while (vm.compile());
            assertEquals(inputVal,prog.getCounts());
        } catch (Exception e) {
            fail("Error in test setup: "+e.getMessage());
            e.printStackTrace();
        }
        inputVal = 0;
        TestProgressHandler progBoundary = new TestProgressHandler();
        db.progress_handler(inputVal, progBoundary);
        try {
        Vm vm2 = db.compile("select * from TEST5; "
                + "insert into TEST5 values(3, 'James', 'Bond'); "
                + "delete from TEST5 where id = 3; "
                + "select * from TEST5");
        do {
            vm2.step(progBoundary);
        } while (vm2.compile());
        assertEquals(inputVal, progBoundary.getCounts());
        }catch (Exception e) {
            fail("Error in test setup: "+e.getMessage());
            e.printStackTrace();
        }
        try {
            db.exec("drop table TEST5",null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    class SinFunc implements Function {
        public void function(FunctionContext fc, String args[]) {
            Double d = new Double(args[0]);
            fc.set_result(Math.sin(d.doubleValue()));
        }
        public void last_step(FunctionContext fc) {
        }
        public void step(FunctionContext fc, String[] args) {
        }
    }
   @TestTargetClass(Trace.class)
    class TestTrace implements Trace,Callback {
    private StringBuffer buf = new StringBuffer();
    public boolean traceCalled = false;
    public String getTrace() {
        return buf.toString();
    }
    public void trace(String stmt) {
        traceCalled = true;
        buf.append(stmt);
    }
    public void columns(String[] coldata) {
    }
    public boolean newrow(String[] rowdata) {
        return false;
    }
    public void types(String[] types) {
    }
   }
   @TestTargetClass(Authorizer.class)
   class AuthorizerCallback implements Authorizer,Callback {
       private boolean isAuthorizing = false;
       public boolean wasCalled() {
           return isAuthorizing;
       }
    public int authorize(int action, String arg1, String arg2, String arg3,
            String arg4) {
        Logger.global.info("DB authorization callback "+action+" "+arg1+" "+arg2+" "+arg3+" "+arg4+" ");
        this.isAuthorizing = true;
        if (action != Constants.SQLITE_SELECT || arg1.contains("private_table")) {
        return Constants.SQLITE_DENY;
        } else {
        return Constants.SQLITE_OK;
        }
    }
    public void columns(String[] coldata) {
    }
    public boolean newrow(String[] rowdata) {
        return false;
    }
    public void types(String[] types) {
    }
   }
   class TestBusyHandler implements BusyHandler, Callback {
    public boolean busy(String table, int count) {
        System.out.println("BUSY!");
        return true;
    }
    public void columns(String[] coldata) {
    }
    public boolean newrow(String[] rowdata) {
        return false;
    }
    public void types(String[] types) {
    }
   }
   class TestProgressHandler implements ProgressHandler,Callback {
    private boolean progressed = false;
    private int counter = 0;
    public boolean isProgressed() {
        return progressed;
    }
    public int getCounts() {
        return counter;
    }
    public boolean progress() {
        this.progressed = true;
        counter++;
        return true;
    }
    public void columns(String[] coldata) {
    }
    public boolean newrow(String[] rowdata) {
        return false;
    }
    public void types(String[] types) {
    }
   }
   private static Runnable createTask2Interrupt(final int id,
            final String dbName, final ErrorTracker errorTracker) {
        return new Runnable() {
            public void run() {
                Database db = new Database();
                try {
                    String value = DatabaseCreator.defaultString + id;
                    db.open(dbName, 0);
                    String insertQuery = "INSERT INTO "
                            + DatabaseCreator.TEST_TABLE1
                            + " (id, field1, field2, field3) VALUES(" + id
                            + ", '" + value + "', " + id + ", " + id + ")";
                    db.exec(insertQuery, null);
                } catch (Exception e) {
                    errorTracker.registerException(this, e);
                    try {
                        db.interrupt();
                        db.exec("DELETE FROM " + DatabaseCreator.SIMPLE_TABLE1
                                + " WHERE id=" + id, null);
                    } catch (Exception e1) {
                        errorTracker.registerException(this, e1);
                    }
                }
            }
        };
   }
   private static Runnable createTask1(final int id,final String dbName, final ErrorTracker errorTracker) {
       return new Runnable() {
           public void run() {
               try {
                   Database db = new Database();
                   db.open(dbName, 0);
                   db.exec("DELETE FROM "
                           + DatabaseCreator.SIMPLE_TABLE1 + " WHERE id=" + id,null);
               } catch (Exception e) {
                   errorTracker.registerException(this, e);
               }
           }
       };
   }
   private static Runnable createTask2(final int id, final String dbName, final ErrorTracker errorTracker ) {
       return new Runnable() {
           public void run() {
               try {
                   String value = DatabaseCreator.defaultString + id;
                   Database db = new Database();
                   db.open(dbName, 0);
                   String insertQuery = "INSERT INTO "
                           + DatabaseCreator.TEST_TABLE1
                           + " (id, field1, field2, field3) VALUES(" + id
                           + ", '" + value + "', " + id + ", " + id + ")";
                   db.exec(insertQuery,null);
               } catch (Exception e) {  
                   errorTracker.registerException(this, e);
               }
           }
       };
   }
   private static Runnable createTask3(final int oldID, final String dbName,
            final int newID, final ErrorTracker errorTracker) {
        return new Runnable() {
            public void run() {
                Database db = new Database();
                try {
                    db.open(dbName, 0);
                    String value = DatabaseCreator.defaultString + newID;
                    String updateQuery = "UPDATE "
                            + DatabaseCreator.TEST_TABLE1 + " SET id=" + newID
                            + ", field1='" + value + "', field2=" + newID
                            + ", field3=" + newID + " WHERE id=" + oldID;
                    db.exec(updateQuery, null);
                } catch (Exception e) {
                    errorTracker.registerException(this, e);
                }
            }
        };
    }
   private class ErrorTracker {
        private List<String> errors = new ArrayList<String>();
        public void registerException(Runnable runnable, Exception e) {
            System.out.println("Registered: "+e.getMessage());
            errors.add(e.getMessage());
        }
        public List<String> getErrors() {
            return errors;
        }
        public void reset() {
            errors.clear();
        }
    }
}