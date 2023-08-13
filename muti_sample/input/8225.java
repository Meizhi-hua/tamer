public class ConfigConstructor {
    private static Subject s = new Subject();
    private static CallbackHandler ch =
                new com.sun.security.auth.callback.TextCallbackHandler();
    private static Configuration c = new MyConfig();
    public static void main(String[] args) throws Exception {
        LoginContext lc = new LoginContext
                        ("module1",
                        s,
                        ch,
                        c);
        lc.login();
        System.out.println("Test 1 Passed");
        LoginContext lc2 = new LoginContext
                        ("module2",
                        null,
                        null,
                        c);
        lc2.login();
        System.out.println("Test 2 Passed");
        LoginContext lc3 = new LoginContext
                        ("module3",
                        s,
                        ch,
                        null);
        lc3.login();
        System.out.println("Test 3 Passed");
        LoginContext lc4 = new LoginContext
                        ("module4",
                        null,
                        null,
                        null);
        lc4.login();
        System.out.println("Test 4 Passed");
        try {
            LoginContext lc5 = new LoginContext
                        ("module5",
                        null,
                        null,
                        c);
            lc5.login();
            throw new SecurityException("test failed - security check failed");
        } catch (LoginException le) {
            if (le.getCause() instanceof SecurityException) {
            } else {
                le.printStackTrace();
                throw new SecurityException("test failed: " +
                    "LoginException did not have chained SecurityException");
            }
        }
        System.out.println("Test 5 Passed");
        LoginContext lc6 = new LoginContext
                        ("module6",
                        null,
                        null,
                        c);
        lc6.login();
        System.out.println("Test 6 Passed");
        LoginContext lc7 = new LoginContext
                        ("goToOther",
                        null,
                        null,
                        c);
        lc7.login();
        System.out.println("Test 7 Passed");
        LoginContext lc8 = new LoginContext
                        ("goToOther");
        lc8.login();
        System.out.println("Test 8 Passed");
    }
    private static class MyConfig extends Configuration {
        public MyConfig() { }
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            java.util.HashMap map = new java.util.HashMap();
            AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
            if (name.equals("module1")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                        ("ConfigConstructor$MyModule1",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        map);
                entries[0] = entry;
            } else if (name.equals("module2")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule2",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else if (name.equals("module3")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule3",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else if (name.equals("module4")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule4",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else if (name.equals("module5")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule5",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else if (name.equals("module6")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule6",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else if (name.equalsIgnoreCase("other")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                    ("ConfigConstructor$MyModule2",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    map);
                entries[0] = entry;
            } else {
                entries = null;
            }
            return entries;
        }
        public void refresh() { }
    }
    public static class MyModule1 implements LoginModule {
        public MyModule1() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) {
            if (s != ConfigConstructor.s ||
                ch != ConfigConstructor.ch) {
                throw new SecurityException("Module 1 failed");
            }
        }
        public boolean login() throws LoginException { return true; }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
    public static class MyModule2 implements LoginModule {
        public MyModule2() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) {
            if (s == ConfigConstructor.s ||
                ch != null) {
                throw new SecurityException("Module 2 failed");
            }
        }
        public boolean login() throws LoginException { return true; }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
    public static class MyModule3 implements LoginModule {
        public MyModule3() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) {
            if (s != ConfigConstructor.s ||
                ch == null ||
                ch == ConfigConstructor.ch) {
                throw new SecurityException("Module 3 failed");
            }
        }
        public boolean login() throws LoginException { return true; }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
    public static class MyModule4 implements LoginModule {
        public MyModule4() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) {
            if (s == ConfigConstructor.s ||
                ch != null) {
                throw new SecurityException("Module 4 failed");
            }
        }
        public boolean login() throws LoginException { return true; }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
    public static class MyModule5 implements LoginModule {
        public MyModule5() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) { }
        public boolean login() throws LoginException {
            System.out.println(System.getProperty("user.name"));
            return true;
        }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
    public static class MyModule6 implements LoginModule {
        public MyModule6() { }
        public void initialize(Subject s, CallbackHandler ch,
                Map<String,?> state, Map<String,?> options) { }
        public boolean login() throws LoginException {
            System.out.println(System.getProperty("user.home"));
            return true;
        }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
    }
}
