public class RobotLWTest extends Applet
{
    public void init()
    {
    }
    public void start ()
    {
        Frame frame = new Frame();
        MyLWContainer c = new MyLWContainer();
        MyLWComponent b = new MyLWComponent();
        c.add(b);
        frame.add(c);
        frame.setSize(400,400);
        frame.setVisible(true);
        try {
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(100);
            robot.waitForIdle();
            Util.waitForIdle(robot);
            Point pt = frame.getLocationOnScreen();
            Point pt1 = b.getLocationOnScreen();
            robot.mouseMove(pt1.x + b.getWidth()/2, pt1.y + b.getHeight()/2);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mousePress(InputEvent.BUTTON2_MASK);
            robot.mouseMove(pt.x + frame.getWidth()+10, pt.y + frame.getHeight()+10);
            robot.mouseRelease(InputEvent.BUTTON2_MASK);
            Util.waitForIdle(robot);
            b.last = null;
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            Util.waitForIdle(robot);
            if (b.last == null) {
                throw new RuntimeException("RobotLWTest failed. Mouse Capture failed");
            }
            b.last = null;
            robot.mouseMove(pt1.x + b.getWidth()/2, pt1.y + b.getHeight()/2);
            Util.waitForIdle(robot);
            if (b.last == null || b.last.getID() != MouseEvent.MOUSE_ENTERED) {
                throw new RuntimeException("RobotLWTest failed. Enter/Exit failed");
            }
            b.last = b.prev = null;
            robot.mousePress(InputEvent.BUTTON1_MASK);
            Util.waitForIdle(robot);
            if (b.prev != null && b.prev.getID() == MouseEvent.MOUSE_ENTERED) {
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                throw new RuntimeException("RobotLWTest failed. Enter/Exit failed");
            }
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        } catch (Exception e) {
            throw new RuntimeException("The test was not completed.", e);
        }
    }
}
class MyLWContainer extends Container {
    public MouseEvent last = null;
    public MouseEvent prev = null;
    MyLWContainer() {
        enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
    }
    public void processMouseEvent(MouseEvent e) {
        prev = last;
        last = e;
        System.out.println(e.toString());
        super.processMouseEvent(e);
    }
}
class MyLWComponent extends Component {
    public MouseEvent last = null;
    public MouseEvent prev = null;
    MyLWComponent() {
        setSize(50,30);
        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
    }
    public void processMouseEvent(MouseEvent e) {
        prev = last;
        last = e;
        System.out.println(e.toString());
        super.processMouseEvent(e);
    }
    public void paint(Graphics g) {
        Dimension d = getSize();
        setBackground(isEnabled() ? Color.red : Color.gray);
        g.clearRect(0, 0, d.width - 1, d.height -1);
    }
}
class Sysout
 {
   private static TestDialog dialog;
   public static void createDialogWithInstructions( String[] instructions )
    {
      dialog = new TestDialog( new Frame(), "Instructions" );
      dialog.printInstructions( instructions );
      dialog.show();
      println( "Any messages for the tester will display here." );
    }
   public static void createDialog( )
    {
      dialog = new TestDialog( new Frame(), "Instructions" );
      String[] defInstr = { "Instructions will appear here. ", "" } ;
      dialog.printInstructions( defInstr );
      dialog.show();
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
      add("South", messageText);
      pack();
      show();
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
    }
 }
