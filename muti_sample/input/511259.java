@TestTargetClass(value = SecurityManager.class, 
                 untestedMethods = {
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "AWTPermission class is not supported.",
                         method = "checkSystemClipboardAccess",
                         args = {}
                     )
}) 
public class SecurityManagerTest extends TestCase {
    MutableSecurityManager mutableSM = null;
    MockSecurityManager mockSM = null;
    SecurityManager originalSM = null;
    String deletedFile = "/";
    String readedFile  = "/";
    String writedFile  = "/";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityManager",
        args = {}
    )
    public void test_Constructor() {
        SecurityManager localManager = null;
        try {
            localManager = new MockSecurityManager();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            assertNotNull("Incorrect SecurityManager", localManager);
            System.setSecurityManager(localManager);
            try {
                new MockSecurityManager();
                fail("SecurityException was not thrown");
            } catch (SecurityException se) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPackageAccess",
        args = {java.lang.String.class}
    )
    public void test_checkPackageAccessLjava_lang_String() {
        final String old = Security.getProperty("package.access");
        Security.setProperty("package.access", "a.,bbb, c.d.");
        mutableSM
                .denyPermission(new RuntimePermission("accessClassInPackage.*"));
        try {
            mutableSM.checkPackageAccess("z.z.z");
            mutableSM.checkPackageAccess("aa");
            mutableSM.checkPackageAccess("bb");
            mutableSM.checkPackageAccess("c");
            try {
                mutableSM.checkPackageAccess("a");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            try {
                mutableSM.checkPackageAccess("bbb");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            try {
                mutableSM.checkPackageAccess("c.d.e");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            Security.setProperty("package.access", "QWERTY");
            mutableSM.checkPackageAccess("a");
            mutableSM.checkPackageAccess("qwerty");
            try {
                mutableSM.checkPackageAccess("QWERTY");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
        } finally {
            Security.setProperty("package.access", old == null ? "" : old);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPackageDefinition",
        args = {java.lang.String.class}
    )
    public void test_checkPackageDefinitionLjava_lang_String() {
        final String old = Security.getProperty("package.definition");
        Security.setProperty("package.definition", "a.,bbb, c.d.");
        mutableSM
                .denyPermission(new RuntimePermission("defineClassInPackage.*"));
        try {
            mutableSM.checkPackageDefinition("z.z.z");
            mutableSM.checkPackageDefinition("aa");
            mutableSM.checkPackageDefinition("bb");
            mutableSM.checkPackageDefinition("c");
            try {
                mutableSM.checkPackageDefinition("a");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            try {
                mutableSM.checkPackageDefinition("bbb");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            try {
                mutableSM.checkPackageDefinition("c.d.e");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
            Security.setProperty("package.definition", "QWERTY");
            mutableSM.checkPackageDefinition("a");
            mutableSM.checkPackageDefinition("qwerty");
            try {
                mutableSM.checkPackageDefinition("QWERTY");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }
        } finally {
            Security.setProperty("package.definition", old == null ? "" : old);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkMemberAccess",
        args = {java.lang.Class.class, int.class}
    )
    public void test_checkMemberAccessLjava_lang_ClassI() {
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(
                new RuntimePermission("accessDeclaredMembers"));
        System.setSecurityManager(mutableSM);
        try {
            getClass().getDeclaredFields();
            try {
                Object.class.getDeclaredFields();
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            try {
                delegateCallToCheckMemberAccess2(Object.class, Member.DECLARED);
                fail("SecurityException was not thrown.");
            } catch(SecurityException se) {
            }
            try {
                delegateCallToCheckMemberAccess2(null, Member.PUBLIC);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    private void delegateCallToCheckMemberAccess2(Class<Object> cls, int type) {
        delegateCallToCheckMemberAccess1(cls, type);
    }
    private void delegateCallToCheckMemberAccess1(Class<Object> cls, int type) {
        mutableSM.checkMemberAccess(cls, type);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPermission",
        args = {java.security.Permission.class}
    )
    public void test_checkPermissionLjava_security_Permission()
            throws Exception {
        checkPermissionLjava_security_PermissionTesting.class.getName();
       try {
            mutableSM.checkPermission(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    private static class checkPermissionLjava_security_PermissionTesting {
        public static void main(String[] args) {
            MutableSecurityManager sm = new MutableSecurityManager();
            sm.addPermission(MutableSecurityManager.SET_SECURITY_MANAGER);
            System.setSecurityManager(sm);
            try {
                try {
                    System.getSecurityManager().checkPermission(
                            new RuntimePermission("createClassLoader"));
                    fail("This should throw a SecurityException");
                } catch (SecurityException e) {
                }
                try {
                    sm.checkPermission(new SecurityPermission("setSystemScope"));
                } catch(SecurityException se) {
                    fail("SecurityException is thrown.");
                }                
            } finally {
                System.setSecurityManager(null);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkAccess",
        args = {java.lang.Thread.class}
    )
    public void test_checkAccessLjava_lang_Thread() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
            }
        };
        t.start();
        t.join();
        new SecurityManager().checkAccess(t);
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission( new RuntimePermission("modifyThread"));  
        System.setSecurityManager(mutableSM);        
        try {
            try {
                mutableSM.checkAccess(t);
            } catch(SecurityException se) {
                fail("SecurityException was thrown.");
            }
            try {
                ThreadGroup initialThreadGroup = Thread.currentThread().getThreadGroup();
                while (initialThreadGroup.getParent() != null) {
                    initialThreadGroup = initialThreadGroup.getParent();
                }                
                Thread [] systemThread = new Thread[1];
                initialThreadGroup.enumerate(systemThread);
                mutableSM.checkAccess(systemThread[0]);
                fail("SecurityException was not thrown.");
            } catch(SecurityException se) {
            }
        } finally { 
            System.setSecurityManager(null);  
        }
        try {
            mutableSM.checkAccess((Thread) null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }       
        try {
            new SecurityManager().checkAccess((Thread)null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe){
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkAccess",
        args = {java.lang.ThreadGroup.class}
    )
    public void test_checkAccessLjava_lang_ThreadGroup() {
        ThreadGroup tg = new ThreadGroup("name");
        RuntimePermission rp = new RuntimePermission("modifyThreadGroup");
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(rp);        
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkAccess(tg);
            } catch(SecurityException se) {
                fail("SecurityException was thrown.");   
            }
            try {
                ThreadGroup initialThreadGroup = Thread.currentThread().getThreadGroup();
                while (initialThreadGroup.getParent() != null) {
                    initialThreadGroup = initialThreadGroup.getParent();
                }                
                mutableSM.checkAccess(initialThreadGroup);
            } catch(SecurityException se) {
            }
        } finally {
            System.setSecurityManager(sm);  
        }
         try {
             mutableSM.checkAccess((ThreadGroup) null);
             fail("NullPointerException was not thrown.");
         } catch(NullPointerException npe) {
         }
    }   
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkAccept",
        args = {java.lang.String.class, int.class}
    )
    @SuppressWarnings("nls")
    public void test_checkAcceptLjava_lang_String_int() {
        mutableSM.addPermission(new AllPermission());
        System.setSecurityManager(mutableSM);
        try {
            assertFalse(startServerSocket());
            assertTrue(mutableSM.isCheckAcceptCalled);
            mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                                                    "accept, connect, listen")); 
            assertTrue(startServerSocket());        
            assertTrue(mutableSM.isCheckAcceptCalled);
            try {
                mutableSM.checkAccept(null, 0);
                fail("NullPointerException is not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);              
        }
    }
    boolean startServerSocket() {
        boolean isSecurityExceptionThrown = false;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(3132);
            Thread thr = new Thread() {
                Socket s = null;
                public void run() {
                    try {
                        s = new Socket(InetAddress.getLocalHost().getHostName(), 3132);
                        Thread.sleep(1);
                    } catch(InterruptedException ie) {
                        fail("InterruptedException was thrown.");
                    } catch(UnknownHostException uhe) {
                        fail("UnknownHostException was thrown.");
                    } catch(IOException ioe) {
                        fail("IOException was thrown.");
                    } finally {
                        try {
                            s.close();
                        } catch(Exception e) {}
                    }
                }
            };
            thr.start();
            ss.accept();
            ss.close();
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(SecurityException se) {
            isSecurityExceptionThrown = true;
        } finally {
            try {
                if(!ss.isClosed())
                    ss.close();
            } catch(Exception e) {              
            }
        }
        return isSecurityExceptionThrown;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkConnect",
        args = {java.lang.String.class, int.class}
    )
    public void test_checkConnectLjava_lang_StringI() {
        String hostName = "localhost";
        int port = 1024;
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "accept, connect, listen"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkConnect(hostName, port);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            } 
            assertTrue(createSocketAddress(hostName, port));
            try {
                mutableSM.checkConnect(hostName, -1);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            } 
            try {
                mutableSM.checkConnect(null, 1024);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
              System.setSecurityManager(null);
        }
        assertFalse(createSocketAddress(hostName, port));
    }
    boolean createSocketAddress(String hostname, int port) {
        try {
            new InetSocketAddress(hostname, port);
        } catch(SecurityException se) {
            return true;
        }
        return false;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkConnect",
        args = {java.lang.String.class, int.class, java.lang.Object.class}
    )
    @SuppressWarnings("nls")
    public void test_checkConnectLjava_lang_String_int_Ljava_lang_Object() {
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "accept, connect, listen"));
        System.setSecurityManager(mutableSM);
        try {
            ProtectionDomain pDomain = this.getClass().getProtectionDomain();
            ProtectionDomain[] pd = { pDomain };
            AccessControlContext acc = new AccessControlContext(pd);
            try {
                mutableSM.checkConnect("localhost", 1024, acc);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            try {
                mutableSM.checkConnect("localhost", -1, acc);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            assertTrue(createSocketAddress("localhost", 1024));
            try {
                mutableSM.checkConnect(null, 1024, acc);            
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
            System.setSecurityManager(null);
            try {
                mutableSM.checkConnect("localhost", 1024, null);
                fail("SecurityException was not thrown.");            
            } catch(SecurityException se) {
            }
        } finally {
            System.setSecurityManager(null);
        } 
        assertFalse(createSocketAddress("localhost", 1024));        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkCreateClassLoader",
        args = {}
    )   
    public void test_checkCreateClassLoader() {
        mutableSM.addPermission(new AllPermission());
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkCreateClassLoader();
        } catch (SecurityException e) {
            fail("Unexpected SecurityException " + e.toString());
        }
        SecurityManager localManager = new MockSecurityManager();
        try {
            System.setSecurityManager(localManager);
            try {
                localManager.checkCreateClassLoader();
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkDelete",
        args = {java.lang.String.class}
    )
    public void test_checkDeleteLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        mutableSM
        .denyPermission(new FilePermission("<<ALL FILES>>", "delete")); 
        try {
            System.setSecurityManager(mutableSM);
            try {
                mutableSM.checkDelete("new.file");
                fail("SecurityException was not thrown");
            } catch (SecurityException npe) {
            }            
            try {
                mutableSM.checkDelete(null);
                fail("NullPointerException was not thrown");
            } catch (NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);
        }
        SecurityManager localManager = new MockSecurityManager();
        try {
            System.setSecurityManager(localManager);
            try {
                localManager.checkDelete(deletedFile);
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkExec",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_checkExecLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        mutableSM
                .denyPermission(new FilePermission("<<ALL FILES>>", "execute"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkExec("java");
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(null);           
        }
        try {
            mutableSM.checkExec(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkExit",
        args = {int.class}
    )
    @SuppressWarnings("nls")
    public void test_checkExit_int() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkExit(0);
        } catch(SecurityException se) {
            fail("SecurityException was thrown.");
        }
        mutableSM.denyPermission(new RuntimePermission("exitVM"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkExit(0);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkLink",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_checkLinkLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("loadLibrary.harmony"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkLink("harmony");
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            try {
                mutableSM.checkLink(null);
                fail("NullPointerException is not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkListen",
        args = {int.class}
    )
    @SuppressWarnings("nls")
    public void test_checkListen_int() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkListen(80);
        } catch(SecurityException se) {
            fail("SecurityException was thrown.");
        }
        mutableSM
                .denyPermission(new SocketPermission("localhost:80", "listen"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkListen(80);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        }
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "listen"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkListen(0);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies SecurityException.",
        method = "checkMulticast",
        args = {java.net.InetAddress.class}
    )
    @SuppressWarnings("nls")
    public void test_checkMulticastLjava_net_InetAddress()
            throws UnknownHostException {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkMulticast(InetAddress.getByName("localhost"));
        } catch(SecurityException se) {
            fail("SecurityException is thrown.");
        }            
        mutableSM.denyPermission(new SocketPermission(InetAddress.getByName(
                "localhost").getHostAddress(), "accept,connect"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkMulticast(InetAddress.getByName("localhost"));
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(null);
        }
        try {
            mutableSM.checkMulticast(null);
            fail("NullPointerException was not thrown.");            
        } catch(NullPointerException e) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkMulticast",
        args = {java.net.InetAddress.class, byte.class}
    )
    @SuppressWarnings( { "nls", "deprecation" })
    public void test_checkMulticastLjava_net_InetAddress_int()
            throws UnknownHostException {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkMulticast(
                    InetAddress.getByName("localhost"), (byte) 0);
        } catch(SecurityException se) {
            fail("SecurityException is thrown.");
        }            
        mutableSM.denyPermission(new SocketPermission(InetAddress.getByName(
                "localhost").getHostAddress(), "accept,connect"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkMulticast(InetAddress.getByName("localhost"),
                        (byte) 0);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            try {
                mutableSM.checkMulticast(null, (byte) 0);                
                fail("NullPointerException is not thrown.");
            } catch(NullPointerException ne) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPermission",
        args = {java.security.Permission.class, java.lang.Object.class}
    )
    @SuppressWarnings("nls")
    public void test_checkPermissionLjava_security_PermissionLjava_lang_Object() {
        mutableSM.addPermission(new AllPermission());
        Permission denyp = new SocketPermission("localhost:1024-",
                "accept, connect, listen");
        mutableSM.denyPermission(denyp);
        System.setSecurityManager(mutableSM);
        ProtectionDomain pDomain = this.getClass().getProtectionDomain();
        ProtectionDomain[] pd = { pDomain };
        AccessControlContext acc = new AccessControlContext(pd);
        try {
            mutableSM.checkPermission(denyp, acc);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(null);
        }
        try {
            mutableSM.checkPermission(null, acc);
            fail("NullPointerException was not thrown.");
        } catch (NullPointerException npe) {
        } 
        try {
            mutableSM.checkPermission(denyp, null);
            fail("SecurityException was not thrown.");
        } catch (SecurityException se) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPrintJobAccess",
        args = {}
    )
    @SuppressWarnings("nls")
    public void test_checkPrintJobAccess() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkPrintJobAccess();
        } catch(SecurityException se) {
            fail("SecurityException is thrown.");
        }            
        mutableSM.denyPermission(new RuntimePermission("queuePrintJob"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkPrintJobAccess();
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPropertiesAccess",
        args = {}
    )
    public void test_checkPropertiesAccess() {
        mutableSM.addPermission(new AllPermission());
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkPropertiesAccess();
        } catch (SecurityException e) {
            fail("Unexpected SecurityException " + e.toString());
        } finally {
            System.setSecurityManager(null);
        }
        SecurityManager localManager = new MockSecurityManager();
        try {
            System.setSecurityManager(localManager);
            try {
                localManager.checkPropertiesAccess();
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkPropertyAccess",
        args = {java.lang.String.class}
    )
    public void test_checkPropertyAccessLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkPropertyAccess("key");
        } catch (SecurityException e) {
            fail("Unexpected SecurityException " + e.toString());
        } finally {
            System.setSecurityManager(null);
        }
        SecurityManager localManager = new MockSecurityManager();
        try {
            System.setSecurityManager(localManager);
            try {
                localManager.checkPropertyAccess("key");
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
            try {
                localManager.checkPropertyAccess("");
                fail("Expected IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException e) {
            }
            try {
                localManager.checkPropertyAccess(null);
                fail("Expected NullPointerException was not thrown");
            } catch (NullPointerException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkRead",
        args = {java.io.FileDescriptor.class}
    )
    @SuppressWarnings("nls")
    public void test_checkReadLjava_io_FileDescriptor() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkRead(new FileDescriptor());
        } catch(SecurityException se) {
            fail("SecurityException is thrown.");
        }            
        mutableSM.denyPermission(new RuntimePermission("readFileDescriptor"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkRead(new FileDescriptor());
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkRead",
        args = {java.lang.String.class}
    )
    public void test_checkReadLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkRead(readedFile);
        } catch(SecurityException se) {
            fail("SecurityException is thrown.");
        }        
        mutableSM.denyPermission(new RuntimePermission("readFileDescriptor"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkRead(readedFile);
            } catch (SecurityException e) {
                fail("Unexpected SecurityException " + e.toString());
            }
            SecurityManager localManager = new MockSecurityManager();
            System.setSecurityManager(localManager);
            try {
                localManager.checkRead(readedFile);
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
            try {
                localManager.checkRead((String) null);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies SecurityException.",
        method = "checkRead",
        args = {java.lang.String.class, java.lang.Object.class}
    )
    @SuppressWarnings("nls")
    public void test_checkReadLjava_lang_StringLjava_lang_Object() {
        mutableSM.addPermission(new AllPermission());
        ProtectionDomain pDomain = this.getClass().getProtectionDomain();
        ProtectionDomain[] pd = { pDomain };
        AccessControlContext acc = new AccessControlContext(pd);        
        mutableSM.denyPermission(new FilePermission("<<ALL FILES>>", "read"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkRead("aa", acc);
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            try {
                mutableSM.checkRead(null, acc);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkSecurityAccess",
        args = {java.lang.String.class}
    )
    public void test_checkSecurityAccessLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkSecurityAccess("getPolicy");
        } catch (SecurityException e) {
            fail("Unexpected SecurityException " + e.toString());
        } finally {
            System.setSecurityManager(null);
        }
        SecurityManager localManager = new MockSecurityManager();
        try {
            System.setSecurityManager(localManager);
            try {
                localManager.checkSecurityAccess("getPolicy");
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
            try {
                localManager.checkSecurityAccess("");
                fail("Expected IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException e) {
            }
            try {
                localManager.checkSecurityAccess(null);
                fail("Expected NullPointerException was not thrown");
            } catch (NullPointerException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkSetFactory",
        args = {}
    )
    @SuppressWarnings("nls")
    public void test_checkSetFactory() {
        mutableSM.addPermission(new AllPermission());
        assertFalse(setFactory());
        mutableSM.denyPermission(new RuntimePermission("setFactory"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.checkSetFactory();
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
            assertTrue(setFactory());
        } finally {
            System.setSecurityManager(null);            
        }
    }
    boolean setFactory() {
        try {
            ServerSocket.setSocketFactory(null);
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(SecurityException se) {
            return true;
        }
        return false;
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Mark this method not feasable: AWTPermission doesn't exist",
        method = "checkAwtEventQueueAccess",
        args = {}
    )
    public void test_checkAwtEventQueueAccess() {
        mutableSM.addPermission(new AllPermission());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Mark this method not feasable: AWTPermission doesn't exist",
        method = "checkTopLevelWindow",
        args = {java.lang.Object.class}
    )
    public void test_checkTopLevelWindowLjava_lang_Object() {
        try {
            SecurityManager localManager = new MockSecurityManager();
            System.setSecurityManager(localManager);
            assertTrue("Calling thread is trusted to bring up the top-level window",
                    localManager.checkTopLevelWindow(this));
            try {
                localManager.checkTopLevelWindow(null);
                fail("Expected NullPointerexception was not thrown");
            } catch (NullPointerException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkWrite",
        args = {java.io.FileDescriptor.class}
    )
    public void test_checkWriteLjava_io_FileDescriptor() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkWrite(new FileDescriptor());
        } catch(SecurityException se) {
            fail("SecurityException was thrown.");
        }
        mutableSM.denyPermission(new RuntimePermission("writeFileDescriptor"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkWrite(new FileDescriptor());
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(null);
        }
        try {
            mutableSM.checkWrite((FileDescriptor) null);
            fail("NullPointerException was not thrown.");            
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkWrite",
        args = {java.lang.String.class}
    )
    public void test_checkWriteLjava_lang_String() {
        mutableSM.addPermission(new AllPermission());
        try {
            mutableSM.checkWrite(writedFile);
        } catch(SecurityException se) {
            fail("SecurityException was thrown.");
        }
        mutableSM.denyPermission(new RuntimePermission("writeFileDescriptor"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkWrite(writedFile);
        } catch (SecurityException e) {
            fail("Unexpected SecurityException " + e.toString());
        } finally {
            System.setSecurityManager(null);
        }
        try {
            SecurityManager localManager = new MockSecurityManager();
            System.setSecurityManager(localManager);
            try {
                localManager.checkWrite(writedFile);
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
        try {
            mutableSM.checkWrite((String) null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInCheck",
        args = {}
    )
    public void test_getIncheck() {
        mockSM.setInCheck(false);
        assertFalse(mockSM.getInCheck());
        mockSM.setInCheck(true);
        assertTrue(mockSM.getInCheck());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSecurityContext",
        args = {}
    )
    @SuppressWarnings("nls")
    public void test_getSecurityContext() {
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new FilePermission("<<ALL FILES>>", "read"));
        System.setSecurityManager(mutableSM);
        try {
            try {
                mutableSM.getSecurityContext();
            } catch(Exception e) {
                fail("Unexpected exception was thrown: " + e.toString());
            }            
            try {
                mutableSM.checkRead("aa", mutableSM.getSecurityContext());
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);            
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getThreadGroup",
        args = {}
    )
    public void test_getThreadGroup() throws InterruptedException {
        final ThreadGroup tgroup = new ThreadGroup(mutableSM.getThreadGroup(), 
                "groupName");
        assertNotNull("Incorrect thread group", tgroup);
        class MyThread extends Thread{
            public int newCount;
            public MyThread() {
                super(tgroup, "threadName");
            }
            @Override
            public void run() {
                super.run();
                newCount = tgroup.activeCount();
            }
        }
        MyThread t = new MyThread();
        t.start();
        t.join();
        assertEquals("Incorrect active count value", 1, t.newCount);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = ".",
        method = "classDepth",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_classDepthLjava_lang_String() {
        assertEquals(-1, mockSM.classDepth("nothing"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "classLoaderDepth",
        args = {}
    )
    public void test_classLoaderDepth() {
        assertEquals(-1, mockSM.classLoaderDepth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "currentClassLoader",
        args = {}
    )
    public void test_currentClassLoader() {
        assertNull(mockSM.currentClassLoader());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "currentLoadedClass",
        args = {}
    )
    public void test_currentLoadedClass() {
        assertNull(mockSM.currentLoadedClass());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "inClass",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_inClassLjava_lang_String() {
        assertFalse(mockSM.inClass("nothing"));
        assertTrue(mockSM.inClass(MockSecurityManager.class.getName()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "inClassLoader",
        args = {}
    )
    public void test_inClassLoader() {
        assertFalse(mockSM.inClassLoader());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClassContext",
        args = {}
    )
    public void test_getClassContext() {
        Class [] stack = {MockSecurityManager.class,
                getClass(), TestCase.class};
        Class [] returnedStack = mockSM.getClassContext();
        assertNotNull(returnedStack);
        assertTrue(returnedStack.length > stack.length);
        for(int i = 0; i < stack.length; i++) {
            assertEquals(stack[i].getName() + " class should have " + i +
                    " position in the classes stack, but there is " +
                    returnedStack[i].getName(),
                    stack[i], returnedStack[i]);           
        }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mutableSM = new MutableSecurityManager();
        mockSM = new MockSecurityManager();
        originalSM = System.getSecurityManager();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setSecurityManager(originalSM);
    }
}
