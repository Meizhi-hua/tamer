public class TestTibetan {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TestTibetan().run();
            }
        });
    }
    public static boolean AUTOMATIC_TEST=true;  
    private void run()  {
        Font ourFont = null;
        try {
            ourFont = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(new java.io.File(System.getProperty("user.home"),"fonts"), "Jomolhari-alpha3c-0605331.ttf"));
            ourFont = ourFont.deriveFont((float)24.0);
        } catch(Throwable t) {
            t.printStackTrace();
            System.err.println("Fail: " + t);
            return;
        }
        JFrame frame = new JFrame(System.getProperty("java.version"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        final JTextArea label = new JTextArea("(empty)");
        label.setSize(400, 300);
        label.setBorder(new LineBorder(Color.black));
        label.setFont(ourFont);
        final String str = "\u0F04\u0F05\u0F0D\u0F0D\u0020\u0F4F\u0F72\u0F53\u0F0B\u0F4F\u0F72\u0F53\u0F0B\u0F42\u0FB1\u0F72\u0F0B\u0F51\u0F54\u0F60\u0F0B\u0F62\u0FA9\u0F63";  
        if(AUTOMATIC_TEST) {  
            label.setText(str);
        } else {
        JButton button = new JButton("Set Char x0DDD");
        button.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                label.setText(str);
            }
        });
        panel.add(button);
        }
        panel.add(label);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
