public class AcceptExtraMouseButtons extends Frame {
    static String tk = Toolkit.getDefaultToolkit().getClass().getName();
    static Robot robot;
    static int [] standardButtonMasks = {InputEvent.BUTTON1_MASK,
                                         InputEvent.BUTTON2_MASK,
                                         InputEvent.BUTTON3_MASK};
    static int [] buttonsPressed;
    static int [] buttonsReleased;
    static int [] buttonsClicked;
    static int buttonsNum = MouseInfo.getNumberOfButtons();
    public static void main(String []s){
        if (tk.equals("sun.awt.X11.XToolkit") || tk.equals("sun.awt.motif.MToolkit")) {
            buttonsNum = buttonsNum - 2;
        }
        System.out.println("Number Of Buttons = "+ buttonsNum);
        if (buttonsNum < 3) {
            System.out.println("Linux and Windows systems should emulate three buttons if even there are only 1 or 2 are phsically available. Setting number of buttons to 3.");
            buttonsNum = 3;
        }
        buttonsPressed = new int [buttonsNum];
        buttonsReleased = new int [buttonsNum];
        buttonsClicked = new int [buttonsNum];
        AcceptExtraMouseButtons frame = new AcceptExtraMouseButtons();
        MouseAdapter ma1 = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    buttonsPressed[e.getButton() - 1] += 1;
                    System.out.println("PRESSED "+e);
                }
                public void mouseReleased(MouseEvent e) {
                    buttonsReleased[e.getButton() - 1] += 1;
                    System.out.println("RELEASED "+e);
                }
                public void mouseClicked(MouseEvent e) {
                    buttonsClicked[e.getButton() - 1] += 1;
                    System.out.println("CLICKED "+e);
                }
            };
        frame.addMouseListener(ma1);
        frame.setSize(300, 300);
        frame.setVisible(true);
        Util.waitForIdle(robot);  
        try {
            robot = new Robot();
            robot.delay(1000);
            robot.mouseMove(frame.getLocationOnScreen().x + frame.getWidth()/2,
                            frame.getLocationOnScreen().y + frame.getHeight()/2);
            for (int i = 0; i < buttonsNum; i++){
                int buttonMask = InputEvent.getMaskForButton(i+1);
                System.out.println("button to press = " +(i+1) + " : value passed to robot = " +buttonMask);
                robot.mousePress(buttonMask);
                robot.delay(30);
                robot.mouseRelease(buttonMask);
                Util.waitForIdle(robot);
            }
            for (int i = 0; i < buttonsNum; i++){
                if (buttonsPressed[i] != 1 || buttonsReleased[i] != 1 || buttonsClicked[i] !=1 ) {
                    throw new RuntimeException("TESTCASE 1 FAILED : button " + (i+1) + " wasn't single pressed|released|clicked : "+ buttonsPressed[i] +" : "+ buttonsReleased[i] +" : "+ buttonsClicked[i]);
                }
            }
            java.util.Arrays.fill(buttonsPressed, 0);
            java.util.Arrays.fill(buttonsReleased, 0);
            java.util.Arrays.fill(buttonsClicked, 0);
            for (int i = 0; i < standardButtonMasks.length; i++){
                int buttonMask = standardButtonMasks[i];
                System.out.println("button to press = " +(i+1) + " : value passed to robot = " +buttonMask);
                robot.mousePress(buttonMask);
                robot.delay(30);
                robot.mouseRelease(buttonMask);
                Util.waitForIdle(robot);
            }
            for (int i = 0; i < standardButtonMasks.length; i++){
                if (buttonsPressed[i] != 1 || buttonsReleased[i] != 1 || buttonsClicked[i] !=1 ) {
                    throw new RuntimeException("TESTCASE 2 FAILED : button " + (i+1) + " wasn't single pressed|released|clicked : "+ buttonsPressed[i] +" : "+ buttonsReleased[i] +" : "+ buttonsClicked[i]);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
