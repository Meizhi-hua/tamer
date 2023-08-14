public class bug6987844 {
    static JMenu menu1;
    static JMenu menu2;
    public static void main(String... args) throws Exception {
        SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        Robot robot = new Robot();
        robot.setAutoDelay(200);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JMenuBar bar = new JMenuBar();
                menu1 = new JMenu("Menu1");
                menu1.add(new JMenuItem("item"));
                bar.add(menu1);
                menu2 = new JMenu("Menu2");
                menu2.add(new JMenuItem("item"));
                menu2.add(new JMenuItem("item"));
                bar.add(menu2);
                frame.setJMenuBar(bar);
                frame.pack();
                frame.setVisible(true);
            }
        });
        toolkit.realSync();
        Point point1 = menu1.getLocationOnScreen();
        Point point2 = menu2.getLocationOnScreen();
        robot.mouseMove(point1.x + 1, point1.y + 1);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mouseMove(point2.x + 1, point2.y + 1);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mouseMove(point1.x + 1, point1.y + 1);
        toolkit.realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Dimension popupSize1 = menu1.getPopupMenu().getSize();
                Dimension popupSize2 = menu2.getPopupMenu().getSize();
                if (popupSize1.equals(popupSize2)) {
                    throw new RuntimeException("First popup unexpedetly changed its size");
                }
            }
        });
    }
}