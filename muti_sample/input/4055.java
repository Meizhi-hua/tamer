public class Clock extends Applet implements Runnable {
    private volatile Thread timer;       
    private int lastxs, lastys, lastxm,
            lastym, lastxh, lastyh;  
    private SimpleDateFormat formatter;  
    private String lastdate;             
    private Font clockFaceFont;          
    private Date currentDate;            
    private Color handColor;             
    private Color numberColor;           
    private int xcenter = 80, ycenter = 55; 
    @Override
    public void init() {
        lastxs = lastys = lastxm = lastym = lastxh = lastyh = 0;
        formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy",
                Locale.getDefault());
        currentDate = new Date();
        lastdate = formatter.format(currentDate);
        clockFaceFont = new Font("Serif", Font.PLAIN, 14);
        handColor = Color.blue;
        numberColor = Color.darkGray;
        try {
            setBackground(new Color(Integer.parseInt(getParameter("bgcolor"),
                    16)));
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }
        try {
            handColor = new Color(Integer.parseInt(getParameter("fgcolor1"),
                    16));
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }
        try {
            numberColor = new Color(Integer.parseInt(getParameter("fgcolor2"),
                    16));
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }
        resize(300, 300);              
    }
    @Override
    public void update(Graphics g) {
        int xh, yh, xm, ym, xs, ys;
        int s = 0, m = 10, h = 10;
        String today;
        currentDate = new Date();
        formatter.applyPattern("s");
        try {
            s = Integer.parseInt(formatter.format(currentDate));
        } catch (NumberFormatException n) {
            s = 0;
        }
        formatter.applyPattern("m");
        try {
            m = Integer.parseInt(formatter.format(currentDate));
        } catch (NumberFormatException n) {
            m = 10;
        }
        formatter.applyPattern("h");
        try {
            h = Integer.parseInt(formatter.format(currentDate));
        } catch (NumberFormatException n) {
            h = 10;
        }
        xs = (int) (Math.cos(s * Math.PI / 30 - Math.PI / 2) * 45 + xcenter);
        ys = (int) (Math.sin(s * Math.PI / 30 - Math.PI / 2) * 45 + ycenter);
        xm = (int) (Math.cos(m * Math.PI / 30 - Math.PI / 2) * 40 + xcenter);
        ym = (int) (Math.sin(m * Math.PI / 30 - Math.PI / 2) * 40 + ycenter);
        xh = (int) (Math.cos((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2)
                * 30
                + xcenter);
        yh = (int) (Math.sin((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2)
                * 30
                + ycenter);
        formatter.applyPattern("EEE MMM dd HH:mm:ss yyyy");
        today = formatter.format(currentDate);
        g.setFont(clockFaceFont);
        g.setColor(getBackground());
        if (xs != lastxs || ys != lastys) {
            g.drawLine(xcenter, ycenter, lastxs, lastys);
            g.drawString(lastdate, 5, 125);
        }
        if (xm != lastxm || ym != lastym) {
            g.drawLine(xcenter, ycenter - 1, lastxm, lastym);
            g.drawLine(xcenter - 1, ycenter, lastxm, lastym);
        }
        if (xh != lastxh || yh != lastyh) {
            g.drawLine(xcenter, ycenter - 1, lastxh, lastyh);
            g.drawLine(xcenter - 1, ycenter, lastxh, lastyh);
        }
        g.setColor(numberColor);
        g.drawString(today, 5, 125);
        g.drawLine(xcenter, ycenter, xs, ys);
        g.setColor(handColor);
        g.drawLine(xcenter, ycenter - 1, xm, ym);
        g.drawLine(xcenter - 1, ycenter, xm, ym);
        g.drawLine(xcenter, ycenter - 1, xh, yh);
        g.drawLine(xcenter - 1, ycenter, xh, yh);
        lastxs = xs;
        lastys = ys;
        lastxm = xm;
        lastym = ym;
        lastxh = xh;
        lastyh = yh;
        lastdate = today;
        currentDate = null;
    }
    @Override
    public void paint(Graphics g) {
        g.setFont(clockFaceFont);
        g.setColor(handColor);
        g.drawArc(xcenter - 50, ycenter - 50, 100, 100, 0, 360);
        g.setColor(numberColor);
        g.drawString("9", xcenter - 45, ycenter + 3);
        g.drawString("3", xcenter + 40, ycenter + 3);
        g.drawString("12", xcenter - 5, ycenter - 37);
        g.drawString("6", xcenter - 3, ycenter + 45);
        g.setColor(numberColor);
        g.drawString(lastdate, 5, 125);
        g.drawLine(xcenter, ycenter, lastxs, lastys);
        g.setColor(handColor);
        g.drawLine(xcenter, ycenter - 1, lastxm, lastym);
        g.drawLine(xcenter - 1, ycenter, lastxm, lastym);
        g.drawLine(xcenter, ycenter - 1, lastxh, lastyh);
        g.drawLine(xcenter - 1, ycenter, lastxh, lastyh);
    }
    @Override
    public void start() {
        timer = new Thread(this);
        timer.start();
    }
    @Override
    public void stop() {
        timer = null;
    }
    @Override
    @SuppressWarnings("SleepWhileHoldingLock")
    public void run() {
        Thread me = Thread.currentThread();
        while (timer == me) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            repaint();
        }
    }
    @Override
    public String getAppletInfo() {
        return "Title: A Clock \n"
                + "Author: Rachel Gollub, 1995 \n"
                + "An analog clock.";
    }
    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            { "bgcolor", "hexadecimal RGB number",
                "The background color. Default is the color of your browser." },
            { "fgcolor1", "hexadecimal RGB number",
                "The color of the hands and dial. Default is blue." },
            { "fgcolor2", "hexadecimal RGB number",
                "The color of the second hand and numbers. Default is dark gray." }
        };
        return info;
    }
}