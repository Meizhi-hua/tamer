public class CheckOptions {
    private static final String USER_PROVIDER_OPTION = "UsErPrOvIdeR";
    public static void main(String[] args) throws Exception {
        init();
        testInvalidOptions();
        testNullCallbackHandler();
        testWithCallbackHandler();
    }
    private static void init() throws Exception {
    }
    private static void testInvalidOptions() throws Exception {
        LdapLoginModule ldap = new LdapLoginModule();
        Subject subject = new Subject();
        ldap.initialize(subject, null, null, Collections.EMPTY_MAP);
        try {
            ldap.login();
            throw new SecurityException("expected a LoginException");
        } catch (LoginException le) {
            System.out.println("Caught a LoginException, as expected");
        }
        Map<String, String> options = new HashMap<>();
        options.put(USER_PROVIDER_OPTION, "ldap:
        ldap.initialize(subject, null, null, options);
        try {
            ldap.login();
            throw new SecurityException("expected a LoginException");
        } catch (LoginException le) {
            System.out.println("Caught a LoginException, as expected");
        }
    }
    private static void testNullCallbackHandler() throws Exception {
        LdapLoginModule ldap = new LdapLoginModule();
        Subject subject = new Subject();
        Map<String, String> options = new HashMap<>();
        ldap.initialize(subject, null, null, options);
        try {
            ldap.login();
            throw new SecurityException("expected LoginException");
        } catch (LoginException le) {
            System.out.println("Caught a LoginException, as expected");
        }
    }
    private static void testWithCallbackHandler() throws Exception {
        LdapLoginModule ldap = new LdapLoginModule();
        Subject subject = new Subject();
        Map<String, String> options = new HashMap<>();
        CallbackHandler goodHandler = new MyCallbackHandler(true);
        ldap.initialize(subject, goodHandler, null, options);
        try {
            ldap.login();
            throw new SecurityException("expected LoginException");
        } catch (LoginException le) {
            System.out.println("Caught a LoginException, as expected");
        }
        CallbackHandler badHandler = new MyCallbackHandler(false);
        ldap.initialize(subject, badHandler, null, options);
        try {
            ldap.login();
            throw new SecurityException("expected LoginException");
        } catch (LoginException le) {
            System.out.println("Caught a LoginException, as expected");
        }
    }
    private static class MyCallbackHandler implements CallbackHandler {
        private final boolean good;
        public MyCallbackHandler(boolean good) {
            this.good = good;
        }
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callbacks[i];
                    if (good) {
                        nc.setName("foo");
                    } else {
                    }
                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callbacks[i];
                    if (good) {
                        pc.setPassword("foo".toCharArray());
                    } else {
                    }
                }
            }
        }
    }
}
