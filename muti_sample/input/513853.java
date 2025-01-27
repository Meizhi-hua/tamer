public class ConsoleUi {
    private static final String OS_NAME_LINUX = "Linux";
    private static final String LS_PLAN_SEPARATOR = "=================================";
    private static final String CMD_TYPE_LEADING_SPACE = "  ";
    private static final String CMD_OPT_LEADING_SPACE = "    ";
    private static final String CREATE_SESSION = "create a new session";
    private static final String CHOOSE_SESSION = "choose a session";
    private TestHost mHost;
    private boolean mKeepRunning;
    private BufferedReader mCommandInput;
    private CommandHistory mCommandHistory = new CommandHistory();
    private String mOsName = "none";
    static final String CASE_NAME_PATTERN_STR = "((\\S+\\.)+\\S+)\\.(\\S+):(\\S+)";
    private static HashMap<String, Integer> mResultCodeMap;
    static {
        mResultCodeMap = new HashMap<String, Integer>();
        mResultCodeMap.put(CtsTestResult.STR_PASS, CtsTestResult.CODE_PASS);
        mResultCodeMap.put(CtsTestResult.STR_FAIL, CtsTestResult.CODE_FAIL);
        mResultCodeMap.put(CtsTestResult.STR_ERROR, CtsTestResult.CODE_ERROR);
        mResultCodeMap.put(CtsTestResult.STR_NOT_EXECUTED, CtsTestResult.CODE_NOT_EXECUTED);
        mResultCodeMap.put(CtsTestResult.STR_TIMEOUT, CtsTestResult.CODE_TIMEOUT);
    }
    public ConsoleUi(TestHost host) {
        mHost = host;
        mCommandInput = new BufferedReader(new InputStreamReader(System.in));
        mKeepRunning = true;
        initInputStream();
    }
    public void startUi() {
        while (mKeepRunning) {
            try {
                String cmdLine = readLine(CUIOutputStream.CTS_PROMPT_SIGN);
                CommandParser cp = CommandParser.parse(cmdLine);
                processCommand(cp);
                mCommandHistory.addCommand(cp, cmdLine);
            } catch (CommandNotFoundException e) {
            } catch (Exception e) {
                Log.e("Got exception while processing command.", e);
                showHelp();
            }
        }
    }
    private void initInputStream() {
        if (mOsName.equals(OS_NAME_LINUX)) {
        } else {
            mCommandInput = new BufferedReader(new InputStreamReader(System.in));
        }
    }
    private String readLine(String prompt) throws IOException {
        String cmdLine = null;
        if (mOsName.equals(OS_NAME_LINUX)) {
        } else {
            CUIOutputStream.print(prompt);
            cmdLine = mCommandInput.readLine().trim();
        }
        return cmdLine;
    }
    private void showHelp() {
        CUIOutputStream.println("Usage: command options");
        CUIOutputStream.println("Avaiable commands and options:");
        showHostCmdHelp();
        showPlanCmdHelp();
        showPackageCmdHelp();
        showResultCmdHelp();
        showHistoryCmdHelp();
        showDeviceCmdHelp();
    }
    private void showHistoryCmdHelp() {
        final String cmdStr = CTSCommand.HISTORY + "/" + CTSCommand.H;
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "History:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + ": list all commands in command history");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " count: list the latest count records in command history");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " " + CTSCommand.OPTION_E
                + " num: run the command designated by 'num' in command history");
    }
    private void showResultCmdHelp() {
        final String cmdStr = CTSCommand.LIST + " " + CTSCommand.OPTION_R
                + "/" + CTSCommand.OPTION_RESULT;
        final String sessionStr = CTSCommand.OPTION_S + "/" + CTSCommand.OPTION_SESSION;
        final String resultsStr = " [" + CtsTestResult.STR_PASS
                       + "/" + CtsTestResult.STR_FAIL
                       + "/" + CtsTestResult.STR_NOT_EXECUTED
                       + "/" + CtsTestResult.STR_TIMEOUT
                       + "] ";
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "Result:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + ": list all result of sessions");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " " + sessionStr
                + " session_id: list detail case result of a specified session");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + resultsStr + sessionStr
                + " session_id: list detail cases of a specified"
                + " session by the specified result.");
    }
    private void showPackageCmdHelp() {
        final String cmdStr = CTSCommand.LIST + " " + CTSCommand.OPTION_P
                + "/" + CTSCommand.OPTION_PACKAGE;
        final String pkgStr = CTSCommand.OPTION_P + "/" + CTSCommand.OPTION_PACKAGE;
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "Package:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + ": list available packages");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE + cmdStr + " package_name: "
                + "list contents of the package with specified name");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + CTSCommand.ADD + " " + pkgStr
                + " root: add packages from root to repository");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + CTSCommand.REMOVE + " " + pkgStr + " package_name/all: "
                + "remove a package or all packages from repository");
    }
    private void showPlanCmdHelp() {
        final String lsPlanStr = CTSCommand.LIST + " " + CTSCommand.OPTION_PLAN;
        final String addPlanStr = CTSCommand.ADD + " " + CTSCommand.OPTION_PLAN;
        final String rmPlanStr = CTSCommand.REMOVE + " " + CTSCommand.OPTION_PLAN;
        final String addDerivedPlanStr = CTSCommand.ADD + " " + CTSCommand.OPTION_DERIVED_PLAN;
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "Plan:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + lsPlanStr + ": list available plans");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + lsPlanStr + " plan_name: list contents of the plan with specified name");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + addPlanStr + " plan_name: add a new plan with specified name");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + addDerivedPlanStr + " plan_name "
                + CTSCommand.OPTION_S + "/" + CTSCommand.OPTION_SESSION + " session_id "
                + CTSCommand.OPTION_R + "/" + CTSCommand.OPTION_RESULT + " result_type"
                + ": derive a plan from the given session");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + rmPlanStr + " plan_name/all: remove a plan or all plans from repository");
        showStartSessionHelp();
    }
    private void showStartSessionHelp() {
        final String cmdStr = CTSCommand.START + " " + CTSCommand.OPTION_PLAN;
        final String testStr = CTSCommand.OPTION_T + "/" + CTSCommand.OPTION_TEST;
        final String deviceStr = CTSCommand.OPTION_D + "/" + CTSCommand.OPTION_DEVICE;
        final String pkgStr = CTSCommand.OPTION_P + "/" + CTSCommand.OPTION_PACKAGE;
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " test_plan_name: run a test plan");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " test_plan_name " + deviceStr + " device_ID: "
                + "run a test plan using the specified device");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
            + cmdStr + " test_plan_name " + testStr + " test_name: run a specific test");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " test_plan_name " + pkgStr + " java_package_name: "
                + "run a specific java package");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " test_plan_name " + testStr + " test_name "
                + deviceStr + " device_ID: run a specific test using the specified device");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + cmdStr + " test_plan_name " + pkgStr + " java_package_name "
                + deviceStr + " device_ID: "
                + "run a specific java package using the specified device");
    }
    private void showHostCmdHelp() {
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "Host:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + CTSCommand.HELP + ": show this message");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + CTSCommand.EXIT + ": exit cts command line");
    }
    private void showDeviceCmdHelp() {
        final String deviceStr = CTSCommand.OPTION_D + "/" + CTSCommand.OPTION_DEVICE;
        CUIOutputStream.println(CMD_TYPE_LEADING_SPACE + "Device:");
        CUIOutputStream.println(CMD_OPT_LEADING_SPACE
                + CTSCommand.LIST + " " + deviceStr + ": list available devices");
    }
    public void processCommand(final CommandParser cp) throws Exception {
        String action = cp.getAction();
        if (action.equals(CTSCommand.EXIT)) {
            if (cp.getArgSize() != 1) {
                showHelp();
                return;
            }
            Log.d("exit cts host");
            mKeepRunning = false;
            mHost.tearDown();
        } else if (action.equals(CTSCommand.HELP)) {
            showHelp();
        } else if (mCommandHistory.isHistoryCommand(action)) {
            processHistoryCommands(cp);
        } else if (action.equals(CTSCommand.ADD)) {
            processAddCommand(cp);
        } else if (action.equals(CTSCommand.START)) {
            processStartCommand(cp);
        } else if (action.equals(CTSCommand.REMOVE)) {
            processRmCommand(cp);
        } else if (action.equals(CTSCommand.LIST)) {
            processListCommand(cp);
        } else {
            showHelp();
        }
    }
    private void processStartCommand(CommandParser cp) throws SAXException,
            ParserConfigurationException {
        if (cp.containsKey(CTSCommand.OPTION_PLAN)) {
            processStartSessionCommand(cp);
        } else if (cp.containsKey(CTSCommand.OPTION_P)
                || cp.containsKey(CTSCommand.OPTION_PACKAGE)) {
            processStartPackageCommand(cp);
        } else {
            showHelp();
        }
    }
    private void processStartPackageCommand(CommandParser cp) {
        try {
            String pathName = cp.getValue(CTSCommand.OPTION_PACKAGE);
            mHost.startZippedPackage(pathName);
        } catch (DeviceDisconnectedException e) {
          Log.e("Device " + e.getMessage() + " disconnected", e);
        } catch (Exception e) {
            Log.e("Met exception during running zipped package.", e);
        }
    }
    public boolean validateCommandParams(CommandParser cp) {
        if (cp == null) {
            return false;
        }
        if (cp.getAction() == null) {
            return true;
        } else if (isValidCommandOption(cp, CTSCommand.START,
                CTSCommand.OPTION_PLAN)) {
            return true;
        } else if (isValidCommandOption(cp, CTSCommand.START,
                CTSCommand.OPTION_PACKAGE)) {
            return true;
        } else if (isValidCommandOption(cp, CTSCommand.START,
                CTSCommand.OPTION_P)) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isValidCommandOption(CommandParser cp,
            String command, String option) {
        return (cp.getAction().equals(command)) && (cp.containsKey(option))
                && (cp.getValue(option) != null)
                && (cp.getValue(option).length() != 0);
    }
    private void processStartSessionCommand(CommandParser cp)
            throws SAXException, ParserConfigurationException {
        if (mHost.getDeviceList().length == 0) {
            Log.e("No device connected", null);
            return;
        }
        String testPlanPath = null;
        String deviceId = null;
        String testName = null;
        String javaPkgName = null;
        String testPlanName = mHost.getPlanName(cp.getValue(CTSCommand.OPTION_PLAN));
        try {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() < 1
                    || cp.getOptionSize() > 3) {
                showStartSessionHelp();
                return;
            }
            testPlanName = mHost.getPlanName(cp
                    .getValue(CTSCommand.OPTION_PLAN));
            testPlanPath = HostConfig.getInstance().getPlanRepository()
                    .getPlanPath(testPlanName);
            if (testPlanPath == null) {
                CUIOutputStream.println("Plan " + testPlanName
                        + " is not in repository, please create it!");
                return;
            }
            if (cp.containsKey(CTSCommand.OPTION_DEVICE)) {
                deviceId = cp.getValue(CTSCommand.OPTION_DEVICE);
                String[] deviceIdList = deviceId.trim().split(",");
                if (deviceIdList.length > 1) {
                    Log.e("Just allow choosing one device ID.", null);
                    return;
                }
            }
            ActionType actionType = ActionType.START_NEW_SESSION;
            if (cp.containsKey(CTSCommand.OPTION_TEST)) {
                testName = cp.getValue(CTSCommand.OPTION_TEST);
                if (-1 == testName.indexOf(Test.METHOD_SEPARATOR)) {
                    Log.e("Test full name must be in the form of:"
                            + " java_package_name.class_name#method_name.", null);
                    return;
                }
                actionType = ActionType.RUN_SINGLE_TEST;
            } else if (cp.containsKey(CTSCommand.OPTION_PACKAGE)) {
                javaPkgName = cp.getValue(CTSCommand.OPTION_PACKAGE);
                actionType = ActionType.RUN_SINGLE_JAVA_PACKAGE;
            }
            TestSession ts = null;
            ArrayList<TestSession> sessionList = mHost.getSessionList(testPlanName);
            if ((sessionList != null) && (sessionList.size() > 0)) {
                if ((testName == null) || (testName.length() == 0)) {
                    String mode = chooseMode(sessionList);
                    if (CREATE_SESSION.equals(mode)) {
                        ts = TestHost.createSession(testPlanName);
                    }
                }
                if (ts == null) {
                    ts = chooseTestSession(sessionList);
                    deviceId = ts.getDeviceId();
                    if ((actionType != ActionType.RUN_SINGLE_TEST)
                            && (actionType != ActionType.RUN_SINGLE_JAVA_PACKAGE)) {
                        actionType = ActionType.RESUME_SESSION;
                    }
                }
            }
            if (deviceId == null) {
                TestDevice td = mHost.getFirstAvailableDevice();
                if (td == null) {
                    CUIOutputStream.println("No idle devices found.");
                    return;
                }
                deviceId = td.getSerialNumber();
            }
            if (!checkDeviceExists(mHost.getDeviceList(), deviceId)) {
                CUIOutputStream.println("Can't find specified device id.  Is it attached?");
                return;
            }
            if (ts == null) {
                ts = TestHost.createSession(testPlanName);
            }
            mHost.startSession(ts, deviceId, testName, javaPkgName, actionType);
        } catch (IOException e) {
            Log.e("Can't create test session", e);
        } catch (DeviceNotAvailableException e) {
            CUIOutputStream.println("Test plan(" + testPlanName + ") "
                    + e.getMessage());
            showStartSessionHelp();
        } catch (TestNotFoundException e) {
            CUIOutputStream.println(e.getMessage());
        } catch (TestPlanNotFoundException e) {
            CUIOutputStream.println("Can't find test plan " + testPlanName);
        } catch (IllegalTestNameException e) {
            CUIOutputStream.println("Illegal case name: " + testName);
        } catch (DeviceDisconnectedException e) {
            Log.e("Device " + e.getMessage() + " disconnected ", null);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Fail to initialise SHA-1 algorithm", e);
        } catch (InvalidApkPathException e) {
            Log.e(e.getMessage(), null);
        } catch (InvalidNameSpaceException e) {
            Log.e(e.getMessage(), null);
        }
    }
    private TestSession chooseTestSession(ArrayList<TestSession> sessionList) throws IOException {
        if ((sessionList == null) || (sessionList.size() == 0)) {
            return null;
        }
        if (sessionList.size() == 1) {
            return sessionList.get(0);
        }
        int index = 0;
        String notification = "Please choose a session from the existed session(s):\n";
        for (TestSession session : sessionList) {
            notification += "  " + session.getId() + "  [" + index + "] \n";
            index ++;
        }
        return sessionList.get(getUserInputId(notification, 0, index));
    }
    private String chooseMode(ArrayList<TestSession> sessionList) throws IOException {
        if (TestHost.sMode == MODE.RUN || (sessionList == null) || (sessionList.size() == 0)) {
            return CREATE_SESSION;
        }
        String planName = sessionList.get(0).getSessionLog().getTestPlanName();
        String notification = "There are " + sessionList.size()
            + " existing session(s) for plan " + planName + ".\n"
            + "Create a new session or choose an existing one?\n"
            + "  Create a new session [0]\n"
            + "  Choose a session     [1]\n";
        int indexSelected = getUserInputId(notification, 0, 2);
        if (indexSelected == 0) {
            return CREATE_SESSION;
        } else {
            return CHOOSE_SESSION;
        }
    }
    public boolean checkDeviceExists(TestDevice[] availableDevices, String specifiedId) {
        for (TestDevice dev : availableDevices) {
            if (specifiedId.equals(dev.getSerialNumber())) {
                return true;
            }
        }
        return false;
    }
    public int getDeviceId(TestDevice[] availableDevices, String idStr) {
        for (int i = 0; i < availableDevices.length; i++) {
            TestDevice dev = availableDevices[i];
            if (idStr.equals(dev.getSerialNumber())) {
                return i;
            }
        }
        return -1;
    }
    private int getUserInputId(String notification, int startIndex, int endIndex)
                throws IOException {
        int indexSelected = 0;
        boolean success = false;
        while (!success) {
            String answer = readLine(notification);
            try {
                indexSelected = Integer.parseInt(answer);
                if ((indexSelected >= 0) && (indexSelected < endIndex)) {
                    success = true;
                } else {
                    CUIOutputStream.println("" + indexSelected
                            + " is out of range [0," + (endIndex -1 ) + "].");
                }
            } catch (NumberFormatException e) {
                CUIOutputStream.println("Invalid nuber is typed in.");
            }
        }
        return indexSelected;
    }
    public boolean isValidDeviceId(int numOfAvailableDevices, int specifiedId) {
        if (specifiedId < 0 || specifiedId >= numOfAvailableDevices) {
            return false;
        }
        return true;
    }
    private void processListCommand(CommandParser cp) throws SAXException,
            IOException, ParserConfigurationException {
        if (cp.containsKey(CTSCommand.OPTION_DEVICE)) {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
                showDeviceCmdHelp();
                return;
            }
            if (cp.getValue(CTSCommand.OPTION_DEVICE).equals("")) {
                listDevices();
            } else {
                showDeviceCmdHelp();
            }
        } else if (cp.containsKey(CTSCommand.OPTION_PLAN)) {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
                showPlanCmdHelp();
                return;
            }
            String planValue = cp.getValue(CTSCommand.OPTION_PLAN);
            if (planValue.equals("")) {
                listPlans();
            } else {
                listSinglePlan(mHost.getPlanName(planValue));
            }
        } else if (cp.containsKey(CTSCommand.OPTION_RESULT)) {
            if (cp.getActionValues().size() != 0
                    || (cp.getOptionSize() < 1 || cp.getOptionSize() > 2)) {
                showResultCmdHelp();
                return;
            }
            String resultValue = cp.getValue(CTSCommand.OPTION_RESULT);
            String sessionId = cp.getValue(CTSCommand.OPTION_SESSION);
            Integer resultCode = null;
            if (sessionId != null) {
                if (resultValue.length() != 0
                        && !mResultCodeMap.containsKey(resultValue)) {
                    showResultCmdHelp();
                } else {
                    resultCode = mResultCodeMap.get(resultValue);
                    listSessionResult(sessionId, resultCode);
                }
            } else if (resultValue.length() == 0) {
                listResults();
            } else {
                showHelp();
            }
        } else if (cp.containsKey(CTSCommand.OPTION_PACKAGE)) {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
                showPackageCmdHelp();
                return;
            }
            listPackages(cp);
        } else {
            showHelp();
        }
    }
    private void processRmCommand(CommandParser cp) throws IOException {
        if (cp.containsKey(CTSCommand.OPTION_PLAN)) {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
                showPlanCmdHelp();
                return;
            }
            String planName = mHost.getPlanName(cp.getValue(CTSCommand.OPTION_PLAN));
            if (HostConfig.ALL.equals(planName)) {
                String prompt = "Remove all of the plans?([y/N])";
                String answer = readLine(prompt).trim();
                if (!isConfirmation(answer, false)) {
                    return;
                }
            }
            mHost.removePlans(planName);
        } else if (cp.containsKey(CTSCommand.OPTION_PACKAGE)) {
            if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
                showPackageCmdHelp();
                return;
            }
            String packageName = cp.getValue(CTSCommand.OPTION_PACKAGE);
            if (HostConfig.ALL.equals(packageName)) {
                String prompt = "Remove all of the packages?([y/N])";
                String answer = readLine(prompt).trim();
                if (!isConfirmation(answer, false)) {
                    return;
                }
            }
            mHost.removePackages(packageName);
        } else {
            showHelp();
        }
    }
    public static boolean isConfirmation(String answer, boolean defaultResult) {
        if ("".equals(answer)) {
            return defaultResult;
        }
        return ("y".equals(answer.toLowerCase()) || "yes".equals(answer.toLowerCase()));
    }
    private void processAddCommand(CommandParser cp) {
        if (cp.containsKey(CTSCommand.OPTION_PLAN)) {
            if (isValidAddPlanArguments(cp)) {
                createPlan(cp, CTSCommand.OPTION_PLAN);
            } else {
                showPlanCmdHelp();
            }
        } else if (cp.containsKey(CTSCommand.OPTION_DERIVED_PLAN)) {
            if (isValidDerivedPlanArguments(cp)) {
                createPlan(cp, CTSCommand.OPTION_DERIVED_PLAN);
            } else {
                showPlanCmdHelp();
            }
        } else if (cp.containsKey(CTSCommand.OPTION_PACKAGE)) {
            try {
                addPackage(cp);
            } catch (IOException e) {
                Log.e("Can't add package", e);
            } catch (IndexOutOfBoundsException e) {
                Log.e("Can't add package", e);
            } catch (NoSuchAlgorithmException e) {
                Log.e("Can't add package", e);
            }
        } else {
            showHelp();
        }
    }
    private boolean isValidAddPlanArguments(CommandParser cp) {
        return (cp.getArgSize() == 3) && (cp.getActionValues().size() == 0)
                && (cp.getOptionSize() == 1);
    }
    private boolean isValidDerivedPlanArguments(CommandParser cp) {
        return (cp.getArgSize() >= 3) && (cp.getActionValues().size() == 0)
                && (cp.getOptionSize() >= 1);
    }
    private void processHistoryCommands(final CommandParser cp)
            throws Exception {
        try {
            if ((cp.getOptionSize() == 0) && (cp.getActionValues().size() == 0)) {
                mCommandHistory.show(mCommandHistory.size());
            } else if (cp.containsKey(CTSCommand.OPTION_E)
                    && (cp.getActionValues().size() == 0)) {
                int cmdNum = 0;
                cmdNum = Integer.parseInt(cp.getValue(CTSCommand.OPTION_E));
                if (cmdNum >= 0 && cmdNum < mCommandHistory.size()) {
                    String cmdLine = mCommandHistory.get(cmdNum);
                    CommandParser cpH = CommandParser.parse(cmdLine);
                    CUIOutputStream.printPrompt();
                    CUIOutputStream.println(cmdLine);
                    processCommand(cpH);
                    mCommandHistory.addCommand(cpH, cmdLine);
                } else {
                    if (mCommandHistory.size() > 0) {
                        Log.e("Command index " + cmdNum
                                + " is out of command history range [0,"
                                + (mCommandHistory.size() - 1) + "].", null);
                    } else {
                        Log.e("No command exists in command history.", null);
                    }
                }
            } else if ((cp.getOptionSize() == 0)
                    && (cp.getActionValues().size() == 1)) {
                int cmdCount = Integer.parseInt(cp.getActionValues().iterator()
                        .next());
                if (cmdCount < 0 || cmdCount > mCommandHistory.size()) {
                    cmdCount = mCommandHistory.size();
                }
                mCommandHistory.show(cmdCount);
            } else {
                showHistoryCmdHelp();
            }
        } catch (NumberFormatException e) {
            showHistoryCmdHelp();
        }
    }
    private void listSinglePlan(String name) throws SAXException, IOException,
            ParserConfigurationException {
        String planName = null;
        for (String str : mHost.getPlanRepository().getAllPlanNames()) {
            if (str.startsWith(name)) {
                planName = str;
                break;
            }
        }
        if (planName == null) {
            Log.e("No plan named " + name + " in repository!", null);
            return;
        }
        String planPath = mHost.getPlanRepository().getPlanPath(planName);
        ArrayList<String> removedPkgList = new ArrayList<String>();
        Collection<String> pkgNames = TestPlan.getEntries(planPath, removedPkgList);
        if (removedPkgList.size() != 0) {
            CUIOutputStream.println("The following package(s) contained in plan "
                    + planName + " have been removed:");
            for (String pkgName : removedPkgList) {
                CUIOutputStream.println("    " + pkgName);
            }
        }
        if (pkgNames.size() > 0) {
            CUIOutputStream.println("Packages of plan " + planName
                    + " (" + pkgNames.size() + " in total):");
            CUIOutputStream.println(LS_PLAN_SEPARATOR);
            for (String pkgName : pkgNames) {
                CUIOutputStream.println(pkgName);
            }
        }
    }
    private void createPlanFromSession(final String name, TestSession ts, final String resultType)
            throws FileNotFoundException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        HashMap<String, ArrayList<String>> selectedResult =
            new HashMap<String, ArrayList<String>>();
        ArrayList<String> packageNames = new ArrayList<String>();
        for (TestPackage pkg : ts.getSessionLog().getTestPackages()) {
            String pkgName = pkg.getAppPackageName();
            ArrayList<String> excludedList = pkg.getExcludedList(resultType);
            if (excludedList != null) {
                packageNames.add(pkgName);
                selectedResult.put(pkgName, excludedList);
            }
        }
        if ((selectedResult != null) && (selectedResult.size() > 0)) {
            TestSessionBuilder.getInstance().serialize(name, packageNames, selectedResult);
        } else {
            if (resultType == null) {
                Log.i("All tests of session " + ts.getId()
                        + " have passed execution. The plan is not created!");
            } else {
                Log.i("No " + resultType +  " tests of session " + ts.getId()
                        + ". The plan is not created!");
            }
        }
    }
    private void addDerivedPlan(final CommandParser cp, final String name,
            ArrayList<String> packageNames) {
        try {
            String sessionId = null;
            String resultType = null;
            int id = TestSession.getLastSessionId();
            if (cp.containsKey(CTSCommand.OPTION_SESSION)) {
                sessionId = cp.getValue(CTSCommand.OPTION_SESSION);
                id = Integer.parseInt(sessionId);
            }
            TestSession ts = mHost.getSession(id);
            if (ts == null) {
                Log.e("The session ID of " + id + " doesn't exist.", null);
                return;
            }
            if (cp.containsKey(CTSCommand.OPTION_RESULT)) {
                resultType = cp.getValue(CTSCommand.OPTION_RESULT);
                if (!CtsTestResult.isValidResultType(resultType)) {
                    Log.e("The following result type is invalid: " + resultType, null);
                    return;
                }
            }
            createPlanFromSession(name, ts, resultType);
        } catch (Exception e) {
            Log.e("Got exception while trying to add a plan!", e);
            return;
        }
    }
    private void addPlan(final CommandParser cp, final String name,
            ArrayList<String> packageNames) {
        try {
            PlanBuilder planBuilder = new PlanBuilder(packageNames);
            if (mOsName.equals(OS_NAME_LINUX)) {
            } else {
                planBuilder.setInputStream(mCommandInput);
            }
            HashMap<String, ArrayList<String>> selectedResult = planBuilder.doSelect();
            if (selectedResult != null) {
                TestSessionBuilder.getInstance().serialize(name, packageNames, selectedResult);
            } else {
                Log.i("Selected nothing for the plan of " + name + ". The plan is not created!");
            }
        } catch (Exception e) {
            Log.e("Got exception while trying to add a plan!", e);
            return;
        }
    }
    private void createPlan(final CommandParser cp, final String type) {
        String name = null;
        if (CTSCommand.OPTION_PLAN.equals(type)) {
            name = cp.getValue(CTSCommand.OPTION_PLAN);
        } else if (CTSCommand.OPTION_DERIVED_PLAN.equals(type)) {
            name = cp.getValue(CTSCommand.OPTION_DERIVED_PLAN);
        } else {
            return;
        }
        if (HostUtils.isFileExist(HostConfig.getInstance().getPlanRepository()
                .getPlanPath(name)) == true) {
            Log.e("Plan " + name + " already exist, please use another name!", null);
            return;
        }
        try {
            if ((name != null) && (!name.matches("\\w+"))) {
                CUIOutputStream.println("Only letter of the alphabet, number and '_'"
                        + " are available for test plan name");
                return;
            }
            ArrayList<String> packageNames =
                HostConfig.getInstance().getCaseRepository().getPackageNames();
            Collection<TestPackage> testPackages = HostConfig.getInstance().getTestPackages();
            if (testPackages.size() == 0) {
                CUIOutputStream.println("No package found in repository, please add package first!");
                return;
            }
            if (CTSCommand.OPTION_PLAN.equals(type)) {
                addPlan(cp, name, packageNames);
            } else if (CTSCommand.OPTION_DERIVED_PLAN.equals(type)) {
                addDerivedPlan(cp, name, packageNames);
            }
        } catch (Exception e) {
            Log.e("Got exception while trying to add a plan!", e);
            return;
        }
    }
    private void listPlans() {
        ArrayList<String> plans = mHost.getPlanRepository().getAllPlanNames();
        if (plans.size() == 0) {
            CUIOutputStream.println("No plan created!");
        } else {
            CUIOutputStream.println("List of plans (" + plans.size() + " in total):");
            for (String name : plans) {
                CUIOutputStream.println(name);
            }
        }
    }
    private void listSessionResult(final String idStr, final Integer resultType) {
        if (!idStr.matches("\\d+")) {
            showResultCmdHelp();
            return;
        }
        int sessionId = Integer.parseInt(idStr);
        TestSession ts = mHost.getSession(sessionId);
        if (null == ts) {
            Log.e("Can't find specified session", null);
            return;
        }
        TestSessionLog log = ts.getSessionLog();
        CUIOutputStream.println("Result of session " + ts.getId());
        CUIOutputStream.println("Result\t\tCase name");
        CUIOutputStream
                .println("==============================================================");
        for (Test test : log.getAllResults()) {
            CtsTestResult result = test.getResult();
            if ((resultType != null) && (result.getResultCode() != resultType.intValue())) {
                continue;
            }
            CUIOutputStream.println(result.getResultString() + "\t\t"
                    + test.getFullName());
        }
    }
    private void listResults() {
        Collection<TestSession> sessions = mHost.getSessions();
        if (sessions.isEmpty()) {
            CUIOutputStream.println("There isn't any test result!");
        } else {
            CUIOutputStream.println("List of all results: ");
            CUIOutputStream.println(
                    "Session\t\tTest result\t\t\t\tStart time\t\tEnd time\t\tTest plan name");
            CUIOutputStream.println("\t\tPass\tFail\tTimeout\tNotExecuted");
            for (TestSession session : sessions) {
                TestSessionLog log = session.getSessionLog();
                int passNum = log.getTestList(
                        CtsTestResult.CODE_PASS).size();
                int failNum = log.getTestList(
                        CtsTestResult.CODE_FAIL).size();
                int notExecutedNum = log.getTestList(
                        CtsTestResult.CODE_NOT_EXECUTED).size();
                int timeOutNum = log.getTestList(
                        CtsTestResult.CODE_TIMEOUT).size();
                String resStr = Long.toString(passNum) + "\t" + failNum;
                resStr += "\t" + timeOutNum;
                resStr += "\t" + notExecutedNum;
                String startTimeStr =
                    HostUtils.getFormattedTimeString(log.getStartTime().getTime(), " ", ".", ":");
                String endTimeStr =
                    HostUtils.getFormattedTimeString(log.getEndTime().getTime(), " ", ".", ":");
                CUIOutputStream.println(Long.toString(session.getId()) + "\t\t"
                        + resStr + "\t\t" + startTimeStr
                        + "\t" + endTimeStr + "\t" + log.getTestPlanName());
            }
        }
    }
    private void addPackage(final CommandParser cp) throws IOException,
            IndexOutOfBoundsException, NoSuchAlgorithmException {
        if (cp.getActionValues().size() != 0 || cp.getOptionSize() != 1) {
            showPackageCmdHelp();
            return;
        }
        String pathName = cp.getValue(CTSCommand.OPTION_PACKAGE);
        mHost.addPackage(pathName);
    }
    private void listPackages(final CommandParser cp) {
        String expectPackage = cp.getValue(CTSCommand.OPTION_PACKAGE);
        String caseRoot = mHost.getCaseRepository().getRoot();
        if (caseRoot == null) {
            Log.e("Case repository is null", null);
            return;
        }
        File root = new File(caseRoot);
        if (!root.isDirectory()) {
            Log.e("Case repository must be a directory!", null);
            return;
        }
        Collection<TestPackage> testPackages = HostConfig.getInstance().getTestPackages();
        if (testPackages.size() == 0) {
            CUIOutputStream
                    .println("No package available under case repository!");
        } else {
            if (expectPackage.equals("")) {
                CUIOutputStream.println("Available packages ("
                        + testPackages.size() + " in total):");
                for (TestPackage pkg : testPackages) {
                    CUIOutputStream.println(pkg.getAppPackageName());
                }
            } else {
                List<ArrayList<String>> list = mHost.getCaseRepository()
                        .listAvailablePackage(expectPackage);
                ArrayList<String> packageList = list.get(0);
                ArrayList<String> suiteList = list.get(1);
                ArrayList<String> caseList = list.get(2);
                ArrayList<String> testList = list.get(3);
                if ((packageList.size() == 0) && (suiteList.size() == 0)
                        && (caseList.size() == 0) && (testList.size() == 0)) {
                    CUIOutputStream
                            .println("Not available test package, suite, cases or tests: "
                                    + expectPackage);
                } else {
                    if (packageList.size() != 0) {
                        CUIOutputStream.println(
                                "Test packages (" + packageList.size() + " in total):");
                        for (String packageName : packageList) {
                            CUIOutputStream.println(packageName);
                        }
                    }
                    if (suiteList.size() != 0) {
                        CUIOutputStream.println(
                                "Test suites (" + suiteList.size() + " in total):");
                        for (String suiteName : suiteList) {
                            CUIOutputStream.println(suiteName);
                        }
                    }
                    if (caseList.size() != 0) {
                        CUIOutputStream.println("Test cases (" + caseList.size() + " in total):");
                        for (String caseName : caseList) {
                            CUIOutputStream.println(caseName);
                        }
                    }
                    if (testList.size() != 0) {
                        CUIOutputStream.println("Tests (" + testList.size() + " in total):");
                        for (String testName : testList) {
                            CUIOutputStream.println(testName);
                        }
                    }
                }
            }
        }
    }
    private void listDevices() {
        String[] deviceNames = mHost.listDevices();
        if (deviceNames.length == 0) {
            CUIOutputStream.println("No device connected.");
            return;
        }
        CUIOutputStream.println("Id\t\tDevice Name\t\tStatus");
        for (int i = 0; i < deviceNames.length; i++) {
            CUIOutputStream.println(i + "\t\t" + deviceNames[i]);
        }
    }
}
