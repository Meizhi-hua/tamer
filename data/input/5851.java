public class AutoRequestFocusToFrontTest extends Applet {
    static boolean haveDelays;
    static Frame auxFrame;
    static Frame frame;
    static Button frameButton;
    static Frame frame2;
    static Button frameButton2;
    static Frame frame3;
    static Button frameButton3;
    static Window window;
    static Button winButton;
    static Dialog dialog;
    static Button dlgButton;
    static Window ownedWindow;
    static Button ownWinButton;
    static Dialog ownedDialog;
    static Button ownDlgButton;
    static Dialog modalDialog;
    static Button modalDlgButton;
    static String toolkitClassName;
    static Robot robot = Util.createRobot();
    public static void main(String[] args) {
        if (args.length != 0) {
            haveDelays = "delay".equals(args[0]) ? true : false;
        }
        AutoRequestFocusToFrontTest app = new AutoRequestFocusToFrontTest();
        app.init();
        app.start();
    }
    public void init() {
        this.setLayout (new BorderLayout ());
        Sysout.createDialogWithInstructions(new String[]
            {"This is an automatic test. Simply wait until it is done."
            });
        toolkitClassName = Toolkit.getDefaultToolkit().getClass().getName();
    }
    static void recreateGUI() {
        if (auxFrame != null) {
            auxFrame.dispose();
            frame.dispose();
            frame2.dispose();
            frame3.dispose();
            window.dispose();
            dialog.dispose();
            ownedWindow.dispose();
            ownedDialog.dispose();
            modalDialog.dispose();
        }
        auxFrame = new Frame("Auxiliary Frame");
        frame = new Frame("Test Frame");
        frameButton = new Button("button");
        frame2 = new Frame("Test Frame 2");
        frameButton2 = new Button("button");
        frame3 = new Frame("Test Frame 3");
        frameButton3 = new Button("button");
        window = new Window(null);
        winButton = new Button("button");
        dialog = new Dialog((Frame)null, "Test Dialog");
        dlgButton = new Button("button");
        ownedWindow = new Window(frame);
        ownWinButton = new Button("button");
        ownedDialog = new Dialog(frame2, "Test Owned Dialog");
        ownDlgButton = new Button("button");
        modalDialog = new Dialog(frame3, "Test Modal Dialog");
        modalDlgButton = new Button("button");
        auxFrame.setBounds(100, 100, 300, 300);
        frame.setBounds(120, 120, 260, 260);
        frame.add(frameButton);
        frame2.setBounds(120, 120, 260, 260);
        frame2.add(frameButton2);
        frame3.setBounds(120, 120, 260, 260);
        frame3.add(frameButton3);
        window.setBounds(120, 120, 260, 260);
        window.add(winButton);
        dialog.setBounds(120, 120, 260, 260);
        dialog.add(dlgButton);
        ownedWindow.setBounds(140, 140, 220, 220);
        ownedWindow.add(ownWinButton);
        ownedDialog.setBounds(140, 140, 220, 220);
        ownedDialog.add(ownDlgButton);
        modalDialog.setBounds(140, 140, 220, 220);
        modalDialog.add(modalDlgButton);
        modalDialog.setModal(true);
    }
    public void start() {
        recreateGUI();
        Test.setWindows(frame, null, null);
        Test.test("Test stage 1 in progress", frameButton);
        recreateGUI();
        Test.setWindows(window, null, null);
        Test.test("Test stage 2 in progress", winButton);
        recreateGUI();
        Test.setWindows(dialog, null, null);
        Test.test("Test stage 3 in progress", dlgButton);
        recreateGUI();
        Test.setWindows(frame, null, new Window[] {ownedWindow, frame});
        Test.test("Test stage 4.1 in progress", ownWinButton);
        recreateGUI();
        Test.setWindows(ownedWindow, null, new Window[] {ownedWindow, frame});
        Test.test("Test stage 4.2 in progress", ownWinButton);
        recreateGUI();
        Test.setWindows(frame2, null, new Window[] {ownedDialog, frame2});
        Test.test("Test stage 5.1 in progress", ownDlgButton);
        recreateGUI();
        Test.setWindows(ownedDialog, null, new Window[] {ownedDialog, frame2});
        Test.test("Test stage 5.2 in progress", ownDlgButton);
        if (!"sun.awt.motif.MToolkit".equals(toolkitClassName)) {
            recreateGUI();
            auxFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            Test.setWindows(modalDialog, modalDialog, new Window[] {modalDialog, frame3});
            Test.test("Test stage 6.1 in progress", modalDlgButton);
        }
        if (!"sun.awt.motif.MToolkit".equals(toolkitClassName)) {
            recreateGUI();
            auxFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            Test.setWindows(frame3, modalDialog, new Window[] {modalDialog, frame3});
            Test.test("Test stage 6.2 in progress", modalDlgButton, true);
        }
        recreateGUI();
        Test.setWindows(frame, null, null);
        Test.setTestSetVisible();
        Test.test("Test stage 7 in progress", frameButton);
        Sysout.println("Test passed.");
    }
    static class Test {
        static Window testWindow; 
        static Window focusWindow; 
        static Window[] showWindows; 
        static boolean testSetVisible;
        static void setWindows(Window _testWindow, Window _focusWindow, Window[] _showWindows) {
            testWindow = _testWindow;
            focusWindow = _focusWindow;
            showWindows = _showWindows;
        }
        static void setTestSetVisible() {
            testSetVisible = true;
        }
        static void test(String msg, final Button testButton, boolean shouldFocusChange) {
            Sysout.println(msg);
            showWindows(testWindow, showWindows, true);
            pause(100);
            Runnable action = new Runnable() {
                    public void run() {
                        testWindow.setAutoRequestFocus(false);
                        if (testSetVisible) {
                            setVisible(testWindow, true);
                        } else {
                            toFront(testWindow);
                        }
                    }
                };
            if (shouldFocusChange) {
                action.run();
                Util.waitForIdle(robot);
                if (!focusWindow.isFocused()) {
                    throw new TestFailedException("the window must gain focus on moving to front but it didn't!");
                }
            } else if (TestHelper.trackFocusChangeFor(action, robot)) {
                throw new TestFailedException("the window shouldn't gain focus on moving to front but it did!");
            }
            pause(100);
            if (Util.getWMID() != Util.METACITY_WM) {
                boolean performed = Util.trackActionPerformed(testButton, new Runnable() {
                        public void run() {
                            Util.clickOnComp(testButton, robot);
                        }
                    }, 1000, false);
                if (!performed) {
                    Sysout.println("(ACTION_EVENT was not generated. One more attemp.)");
                    performed = Util.trackActionPerformed(testButton, new Runnable() {
                            public void run() {
                                Util.clickOnComp(testButton, robot);
                            }
                        }, 1000, false);
                    if (!performed) {
                        throw new TestFailedException("the window moved to front is not on the top!");
                    }
                }
            }
            showWindows(testWindow, showWindows, false);
            if (!testWindow.isFocusableWindow()) {
                return;
            }
            showWindows(testWindow, showWindows, true);
            pause(100);
            boolean gained = Util.trackWindowGainedFocus(testWindow, new Runnable() {
                    public void run() {
                        testWindow.setAutoRequestFocus(true);
                        if (testSetVisible) {
                            setVisible(testWindow, true);
                        } else {
                            toFront(testWindow);
                        }
                    }
                }, 1000, false);
            if (!gained && !testButton.hasFocus()) {
                throw new TestFailedException("the window should gain focus automatically but it didn't!");
            }
            showWindows(testWindow, showWindows, false);
        }
        static void test(String msg, Button testButton) {
            test(msg, testButton, false);
        }
        private static void showWindows(Window win, Window[] wins, final boolean visible) {
            pause(100);
            if (wins == null) {
                wins = new Window[] {win}; 
            }
            for (final Window w: wins) {
                if (visible) {
                    if ((w instanceof Dialog) && ((Dialog)w).isModal()) {
                        TestHelper.invokeLaterAndWait(new Runnable() {
                                public void run() {
                                    w.setVisible(true);
                                }
                            }, robot);
                    } else {
                        setVisible(w, true);
                    }
                } else {
                    w.dispose();
                }
            }
            setVisible(auxFrame, visible);
            if (visible) {
                if (!auxFrame.isFocused()) {
                    Util.clickOnTitle(auxFrame, robot);
                    Util.waitForIdle(robot);
                    if (!auxFrame.isFocused()) {
                        throw new Error("Test error: the frame couldn't be focused.");
                    }
                }
            }
        }
    }
    private static void setVisible(Window w, boolean b) {
        w.setVisible(b);
        try {
            Util.waitForIdle(robot);
        } catch (RuntimeException rte) { 
            rte.printStackTrace();
        }
        robot.delay(200);
    }
    private static void toFront(Window w) {
        w.toFront();
        Util.waitForIdle(robot);
        robot.delay(200);
    }
    private static void pause(int msec) {
        if (haveDelays) {
            robot.delay(msec);
        }
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class Sysout
{
    static TestDialog dialog;
    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        println( "Any messages for the tester will display here." );
    }
    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }
    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }
}
class TestDialog extends Dialog
{
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );
        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);
        pack();
    }
    public void printInstructions( String[] instructions )
    {
        instructionsText.setText( "" );
        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ )
        {
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 )
            {
                if( remainingStr.length() >= maxStringLength )
                {
                    int posOfSpace = remainingStr.
                        lastIndexOf( ' ', maxStringLength - 1 );
                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;
                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                else
                {
                    printStr = remainingStr;
                    remainingStr = "";
                }
                instructionsText.append( printStr + "\n" );
            }
        }
    }
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }
}
