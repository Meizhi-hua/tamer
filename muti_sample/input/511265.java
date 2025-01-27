@TestTargetClass(Statement.class)
public class MultiThreadAccessTest extends TestCase {
    private static Connection conn;
    private static Statement statement;
    private static final int numThreads = 10;
    private static final int numOfRecords = 20;
    private ThreadPool threadPool;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        try {
            conn = Support_SQL.getConnection();
            statement = conn.createStatement();
            createTestTables();
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
        threadPool = new ThreadPool(numThreads);
    }
    public void tearDown() throws Exception {
        threadPool.join();
        deleteTestTables();
        conn.close();
        statement.close();
        super.tearDown();
    }
    public void createTestTables() {
        try {
            ResultSet userTab = conn.getMetaData().getTables(null,
                    null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE2)) {
                    statement.execute(DatabaseCreator.DROP_TABLE2);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE4)) {
                    statement.execute(DatabaseCreator.DROP_TABLE4);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE3)) {
                    statement.execute(DatabaseCreator.DROP_TABLE3);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE3);
            statement.execute(DatabaseCreator.CREATE_TABLE4);
            statement.execute(DatabaseCreator.CREATE_TABLE1);
            statement.execute(DatabaseCreator.CREATE_TABLE2);
            DatabaseCreator.fillTestTable1(conn, numOfRecords);
            DatabaseCreator.fillTestTable2(conn, numOfRecords);
            DatabaseCreator.fillTestTable4(conn, numOfRecords);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    public void deleteTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE1);
            statement.execute(DatabaseCreator.DROP_TABLE2);
            statement.execute(DatabaseCreator.DROP_TABLE3);
            statement.execute(DatabaseCreator.DROP_TABLE4);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functionality test: A few threads execute select operation in the same time for one table in the database.",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void test_MultipleAccessToOneTable() throws SQLException {
        for (int i = 0; i < numThreads; i++) {
            threadPool.runTask(createTask1(i));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functionality test: A few threads execute select operation in the same time for different tables in the database",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void test_MultipleAccessToSeveralTables() throws SQLException {
        threadPool.runTask(createTask1(1));
        threadPool.runTask(createTask2(2));
        threadPool.runTask(createTask3(3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functionality test: A few threads execute update, insert and delete operations in the same time for one table in the database",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void test_MultipleOperationsInSeveralTables() throws SQLException {
        int id1 = numOfRecords - 1;
        threadPool.runTask(createTask4(id1));
        int id2 = numOfRecords + 1;
        threadPool.runTask(createTask5(id2));
        int oldID = 5;
        int newID = 100;
        threadPool.runTask(createTask6(oldID, newID));
        threadPool.join();
        Statement statement = conn.createStatement();
        String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1
                + " WHERE id=";
        ResultSet result = statement.executeQuery(selectQuery + id1);
        assertFalse("The record was not deleted", result.next());
        result = statement.executeQuery(selectQuery + id2);
        assertTrue("The record was not inserted", result.next());
        assertEquals("Wrong value of field1", DatabaseCreator.defaultString
                + id2, result.getString("field1"));
        assertEquals("Wrong value of field2", Integer.valueOf(id2).intValue(), result
                .getInt("field2"));
        assertEquals("Wrong value of field3", Integer.valueOf(id2).intValue(), result
                .getInt("field3"));
        result.close();
        result = statement.executeQuery(selectQuery + oldID);
        assertFalse("The record was not deleted", result.next());
        result.close();
        result = statement.executeQuery(selectQuery + newID);
        assertTrue("The record was not updated", result.next());
        assertEquals("Wrong value of field1", DatabaseCreator.defaultString
                + newID, result.getString("field1"));
        assertEquals("Wrong value of field2", Integer.valueOf(newID).intValue(), result
                .getInt("field2"));
        assertEquals("Wrong value of field3", Integer.valueOf(newID).intValue(), result
                .getInt("field3"));
        result.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test: A few threads execute update operation in the same time for one tables in the database",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void test_MultipleUpdatesInOneTables() throws SQLException {
        int id = 1;
        String field = "field3";
        String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1
                + " WHERE id=" + id;
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(selectQuery);
        assertTrue("There is no records with id = " + id, result.next());
        result.close();
        for (int i = 0; i < numThreads; i++) {
            threadPool.runTask(createTask7(id, field));
        }
        threadPool.join();
        double expectedVal = id + numThreads;
        result = statement.executeQuery(selectQuery);
        assertTrue("There is no records with id = " + id, result.next());
        result.close();
    }
    private static Runnable createTask1(final int taskID) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM "
                            + DatabaseCreator.TEST_TABLE1);
                    while (result.next()) {
                        assertEquals("Wrong value of id ",
                                DatabaseCreator.defaultString
                                        + result.getInt("id"), result
                                        .getString("field1"));
                        assertEquals("Wrong value of field2 ", result.getInt("id"), result
                                .getInt("field2"));
                        assertEquals("Wrong value of field3 ",result.getInt("id"), result
                                .getInt("field3"));
                    }
                    result.close();
                } catch (Exception e) {
                    System.err.println("Task 1 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask2(final int taskID) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM "
                            + DatabaseCreator.TEST_TABLE2);
                    while (result.next()) {
                        while (result.next()) {
                            int id = result.getInt("finteger");
                            assertEquals("Wrong value of ftext",
                                    DatabaseCreator.defaultString + id, result
                                            .getString("ftext"));
                            assertEquals("Wrong value of fcharacter",
                                    DatabaseCreator.defaultCharacter + id,
                                    result.getString("fcharacter"));
                            assertEquals("Wrong value of fdecimal",
                                    DatabaseCreator.defaultDouble + id, result
                                            .getDouble("fdecimal"));
                            assertEquals("Wrong value of fnumeric",
                                    DatabaseCreator.defaultDouble + id, result
                                            .getDouble("fnumeric"));
                            assertEquals("Wrong value of fsmallint", result
                                    .getInt("finteger"), result
                                    .getShort("fsmallint"));
                            assertEquals("Wrong value of ffloat",
                                    (float) DatabaseCreator.defaultDouble + id,
                                    result.getFloat("ffloat"));
                            assertEquals("Wrong value of freal",
                                    (float) DatabaseCreator.defaultDouble + id,
                                    result.getFloat("freal"));
                            assertEquals("Wrong value of fdouble",
                                    DatabaseCreator.defaultDouble + id, result
                                            .getDouble("fdouble"));
                        }
                    }
                    result.close();
                } catch (Exception e) {
                    System.err.println("Task2 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask3(final int taskID) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM "
                            + DatabaseCreator.TEST_TABLE4);
                    while (result.next()) {
                        assertEquals("Wrong value of field1",
                                DatabaseCreator.defaultString
                                        + result.getInt("fk"), result
                                        .getString("field1"));
                    }
                    result.close();
                } catch (Exception e) {
                    System.err.println("Task 3 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask4(final int id) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    statement.execute("DELETE FROM "
                            + DatabaseCreator.TEST_TABLE1 + " WHERE id=" + id);
                } catch (Exception e) {
                    System.err.println("Task 4 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask5(final int id) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    String value = DatabaseCreator.defaultString + id;
                    String insertQuery = "INSERT INTO "
                            + DatabaseCreator.TEST_TABLE1
                            + " (id, field1, field2, field3) VALUES(" + id
                            + ", '" + value + "', " + id + ", " + id + ")";
                    statement.execute(insertQuery);
                } catch (Exception e) {
                    System.err.println("Task 5 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask6(final int oldID, final int newID) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    String value = DatabaseCreator.defaultString + newID;
                    String updateQuery = "UPDATE "
                            + DatabaseCreator.TEST_TABLE1 + " SET id=" + newID
                            + ", field1='" + value + "', field2=" + newID
                            + ", field3=" + newID + " WHERE id=" + oldID;
                    statement.execute(updateQuery);
                } catch (Exception e) {
                    System.err.println("Task 6 "+e.getMessage());
                }
            }
        };
    }
    private static Runnable createTask7(final int id, final String field) {
        return new Runnable() {
            public void run() {
                try {
                    Statement statement = conn.createStatement();
                    String updateQuery = "UPDATE "
                            + DatabaseCreator.TEST_TABLE1 + " SET " + field
                            + "= " + field + "+ 1 WHERE id=" + id;
                    statement.execute(updateQuery);
                } catch (Exception e) {
                    System.err.println("Task 7 "+e.getMessage());
                }
            }
        };
    }
}