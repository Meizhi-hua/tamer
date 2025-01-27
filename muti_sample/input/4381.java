public class ReadTimeoutTest {
    public static void main(String[] args) throws Exception {
        boolean passed = false;
        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.read.timeout", "1000");
        env.put(Context.PROVIDER_URL, "ldap:
        Server s = new Server();
        try {
            s.start();
            DirContext ctx = new InitialDirContext(env);
            System.out.println("LDAP Client: Connected to the Server");
            SearchControls scl = new SearchControls();
            scl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            System.out.println("Performing Search");
            NamingEnumeration answer =
                ctx.search("ou=People,o=JNDITutorial", "(objectClass=*)", scl);
            ctx.close();
        } catch (NamingException e) {
            passed = true;
            e.printStackTrace();
        }
        s.interrupt();
        if (!passed) {
            throw new Exception("Read timeout test failed," +
                         " read timeout exception not thrown");
        }
        System.out.println("The test PASSED");
    }
    static class Server extends Thread {
        static int serverPort = 2001;
        Server() {
        }
        public void run() {
            try {
                ServerSocket serverSock = new ServerSocket(serverPort);
                Socket socket = serverSock.accept();
                System.out.println("Server: Connection accepted");
                BufferedInputStream bin = new BufferedInputStream(socket.
                                getInputStream());
                while (true) {
                    bin.read();
                }
            } catch (IOException e) {
            }
    }
}
}
