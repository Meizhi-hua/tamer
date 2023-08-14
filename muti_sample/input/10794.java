public class SelectionAutoscrollTest extends Applet {
    TextArea textArea;
    Robot robot;
    final int desiredSelectionEnd = ('z'-'a'+1)*2;  
    final static int SCROLL_DELAY = 10; 
    public void start () {
        createObjects();
        manipulateMouse();
        checkResults();
    }
    void createObjects() {
        textArea = new TextArea( bigString() );
        robot = Util.createRobot();
        Panel panel = new Panel();
        panel.setLayout( new GridLayout(3,3) );
        for( int y=0; y<3; ++y ) {
            for( int x=0; x<3; ++x ) {
                if( x==1 && y==1 ) {
                    panel.add( textArea );
                } else {
                    panel.add( new Panel() );
                }
            }
        }
        Frame frame = new Frame( "TextArea cursor icon test" );
        frame.setSize( 300, 300 );
        frame.add( panel );
        frame.setVisible( true );
    }
    static String bigString() {
        String s = "";
        for( char c='a'; c<='z'; ++c ) {
            s += c+"\n";
        }
        return s;
    }
    void manipulateMouse() {
        moveMouseToCenterOfTextArea();
        Util.waitForIdle( robot );
        robot.mousePress( MouseEvent.BUTTON1_MASK );
        Util.waitForIdle( robot );
        for( int tremble=0; tremble < desiredSelectionEnd; ++tremble ) {
            moveMouseBelowTextArea( tremble%2!=0 );
            Util.waitForIdle( robot );
            waitUntilScrollIsPerformed(robot);
        }
        robot.mouseRelease( MouseEvent.BUTTON1_MASK );
        Util.waitForIdle( robot );
    }
    void moveMouseToCenterOfTextArea() {
        Dimension d = textArea.getSize();
        Point l = textArea.getLocationOnScreen();
        robot.mouseMove( (int)(l.x+d.width*.5), (int)(l.y+d.height*.5) );
    }
    void moveMouseBelowTextArea( boolean shift ) {
        Dimension d = textArea.getSize();
        Point l = textArea.getLocationOnScreen();
        int x = (int)(l.x+d.width*.5);
        int y = (int)(l.y+d.height*1.5);
        if( shift ) y+=15;
        robot.mouseMove( x, y );
    }
    void waitUntilScrollIsPerformed(Robot robot) {
        try {
            Thread.sleep( SCROLL_DELAY );
        }
        catch( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    void checkResults() {
        final int currentSelectionEnd = textArea.getSelectionEnd();
        System.out.println(
            "TEST: Selection range after test is: ( "
            + textArea.getSelectionStart() + ", "
            + currentSelectionEnd + " )"
        );
        boolean resultOk = ( currentSelectionEnd == desiredSelectionEnd );
        String desiredSelectionEndString = "" + desiredSelectionEnd;
        String toolkitName = Toolkit.getDefaultToolkit().getClass().getName();
        if( toolkitName.equals("sun.awt.windows.WToolkit") ) {
            final int desiredSelectionEnd2 = desiredSelectionEnd-1;  
            resultOk |= ( currentSelectionEnd == desiredSelectionEnd2 );
            desiredSelectionEndString += " or " + desiredSelectionEnd2;
        }
        if( resultOk ) {
            System.out.println(
                "TEST: passed: Text is selected to the end"
                + " (expected selection range end is "
                + desiredSelectionEndString + ")."
            );
        } else {
            System.out.println(
                "TEST: FAILED: Text should be selected to the end"
                + " (selection range end should be "
                + desiredSelectionEndString + ")."
            );
            throw new RuntimeException(
                "TEST: FAILED: Text should be selected to the end, but it is not."
            );
        }
    }
}
