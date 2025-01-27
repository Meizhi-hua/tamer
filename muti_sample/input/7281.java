public class CLSFractal
        extends java.applet.Applet
        implements Runnable, MouseListener {
    Thread kicker;
    ContextLSystem cls;
    int fractLevel = 1;
    int repaintDelay = 50;
    boolean incrementalUpdates;
    float startAngle = 0;
    float rotAngle = 45;
    float Xmin;
    float Xmax;
    float Ymin;
    float Ymax;
    int border;
    boolean normalizescaling;
    @Override
    public void init() {
        String s;
        cls = new ContextLSystem(this);
        s = getParameter("level");
        if (s != null) {
            fractLevel = Integer.parseInt(s);
        }
        s = getParameter("incremental");
        if (s != null) {
            incrementalUpdates = s.equalsIgnoreCase("true");
        }
        s = getParameter("delay");
        if (s != null) {
            repaintDelay = Integer.parseInt(s);
        }
        s = getParameter("startAngle");
        if (s != null) {
            startAngle = Float.valueOf(s).floatValue();
        }
        s = getParameter("rotAngle");
        if (s != null) {
            rotAngle = Float.valueOf(s).floatValue();
        }
        rotAngle = rotAngle / 360 * 2 * 3.14159265358f;
        s = getParameter("border");
        if (s != null) {
            border = Integer.parseInt(s);
        }
        s = getParameter("normalizescale");
        if (s != null) {
            normalizescaling = s.equalsIgnoreCase("true");
        }
        addMouseListener(this);
    }
    @Override
    public void destroy() {
        removeMouseListener(this);
    }
    @Override
    public void run() {
        Thread me = Thread.currentThread();
        boolean needsRepaint = false;
        while (kicker == me && cls.getLevel() < fractLevel) {
            cls.generate();
            if (kicker == me && incrementalUpdates) {
                repaint();
                try {
                    Thread.sleep(repaintDelay);
                } catch (InterruptedException ignored) {
                }
            } else {
                needsRepaint = true;
            }
        }
        if (kicker == me) {
            kicker = null;
            if (needsRepaint) {
                repaint();
            }
        }
    }
    @Override
    public void start() {
        kicker = new Thread(this);
        kicker.start();
    }
    @Override
    public void stop() {
        kicker = null;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        cls = new ContextLSystem(this);
        savedPath = null;
        start();
        e.consume();
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    String savedPath;
    @Override
    public void paint(Graphics g) {
        String fractalPath = cls.getPath();
        if (fractalPath == null) {
            super.paint(g);
            return;
        }
        if (savedPath == null || !savedPath.equals(fractalPath)) {
            savedPath = fractalPath;
            render(null, fractalPath);
        }
        for (int i = 0; i < border; i++) {
            g.draw3DRect(i, i, getSize().width - i * 2, getSize().height - i * 2,
                    false);
        }
        render(g, fractalPath);
    }
    void render(Graphics g, String path) {
        Stack<CLSTurtle> turtleStack = new Stack<CLSTurtle>();
        CLSTurtle turtle;
        if (g == null) {
            Xmin = 1E20f;
            Ymin = 1E20f;
            Xmax = -1E20f;
            Ymax = -1E20f;
            turtle = new CLSTurtle(startAngle, 0, 0, 0, 0, 1, 1);
        } else {
            float frwidth = Xmax - Xmin;
            if (frwidth == 0) {
                frwidth = 1;
            }
            float frheight = Ymax - Ymin;
            if (frheight == 0) {
                frheight = 1;
            }
            float xscale = (getSize().width - border * 2 - 1) / frwidth;
            float yscale = (getSize().height - border * 2 - 1) / frheight;
            int xoff = border;
            int yoff = border;
            if (normalizescaling) {
                if (xscale < yscale) {
                    yoff += ((getSize().height - border * 2)
                            - ((Ymax - Ymin) * xscale)) / 2;
                    yscale = xscale;
                } else if (yscale < xscale) {
                    xoff += ((getSize().width - border * 2)
                            - ((Xmax - Xmin) * yscale)) / 2;
                    xscale = yscale;
                }
            }
            turtle = new CLSTurtle(startAngle, 0 - Xmin, 0 - Ymin,
                    xoff, yoff, xscale, yscale);
        }
        for (int pos = 0; pos < path.length(); pos++) {
            switch (path.charAt(pos)) {
                case '+':
                    turtle.rotate(rotAngle);
                    break;
                case '-':
                    turtle.rotate(-rotAngle);
                    break;
                case '[':
                    turtleStack.push(turtle);
                    turtle = new CLSTurtle(turtle);
                    break;
                case ']':
                    turtle = turtleStack.pop();
                    break;
                case 'f':
                    turtle.jump();
                    break;
                case 'F':
                    if (g == null) {
                        includePt(turtle.X, turtle.Y);
                        turtle.jump();
                        includePt(turtle.X, turtle.Y);
                    } else {
                        turtle.draw(g);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    void includePt(float x, float y) {
        if (x < Xmin) {
            Xmin = x;
        }
        if (x > Xmax) {
            Xmax = x;
        }
        if (y < Ymin) {
            Ymin = y;
        }
        if (y > Ymax) {
            Ymax = y;
        }
    }
    @Override
    public String getAppletInfo() {
        return "Title: CLSFractal 1.1f, 27 Mar 1995 \nAuthor: Jim Graham \nA "
                + "(not yet) Context Sensitive L-System production rule. \n"
                + "This class encapsulates a production rule for a Context "
                + "Sensitive\n L-System \n(pred, succ, lContext, rContext)."
                + "  The matches() method, however, does not \n(yet) verify "
                + "the lContext and rContext parts of the rule.";
    }
    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            { "level", "int", "Maximum number of recursions.  Default is 1." },
            { "incremental", "boolean", "Whether or not to repaint between "
                + "recursions.  Default is true." },
            { "delay", "integer", "Sets delay between repaints.  Default is 50." },
            { "startAngle", "float", "Sets the starting angle.  Default is 0." },
            { "rotAngle", "float", "Sets the rotation angle.  Default is 45." },
            { "border", "integer", "Width of border.  Default is 2." },
            { "normalizeScale", "boolean", "Whether or not to normalize "
                + "the scaling.  Default is true." },
            { "pred", "String",
                "Initializes the rules for Context Sensitive L-Systems." },
            { "succ", "String",
                "Initializes the rules for Context Sensitive L-Systems." },
            { "lContext", "String",
                "Initializes the rules for Context Sensitive L-Systems." },
            { "rContext", "String",
                "Initializes the rules for Context Sensitive L-Systems." }
        };
        return info;
    }
}
class CLSTurtle {
    float angle;
    float X;
    float Y;
    float scaleX;
    float scaleY;
    int xoff;
    int yoff;
    public CLSTurtle(float ang, float x, float y,
            int xorg, int yorg, float sx, float sy) {
        angle = ang;
        scaleX = sx;
        scaleY = sy;
        X = x * sx;
        Y = y * sy;
        xoff = xorg;
        yoff = yorg;
    }
    public CLSTurtle(CLSTurtle turtle) {
        angle = turtle.angle;
        X = turtle.X;
        Y = turtle.Y;
        scaleX = turtle.scaleX;
        scaleY = turtle.scaleY;
        xoff = turtle.xoff;
        yoff = turtle.yoff;
    }
    public void rotate(float theta) {
        angle += theta;
    }
    public void jump() {
        X += (float) Math.cos(angle) * scaleX;
        Y += (float) Math.sin(angle) * scaleY;
    }
    public void draw(Graphics g) {
        float x = X + (float) Math.cos(angle) * scaleX;
        float y = Y + (float) Math.sin(angle) * scaleY;
        g.drawLine((int) X + xoff, (int) Y + yoff,
                (int) x + xoff, (int) y + yoff);
        X = x;
        Y = y;
    }
}
class ContextLSystem {
    String axiom;
    List<CLSRule> rules = new ArrayList<CLSRule>();
    int level;
    public ContextLSystem(java.applet.Applet app) {
        axiom = app.getParameter("axiom");
        int num = 1;
        while (true) {
            String pred = app.getParameter("pred" + num);
            String succ = app.getParameter("succ" + num);
            if (pred == null || succ == null) {
                break;
            }
            rules.add(new CLSRule(pred, succ,
                    app.getParameter("lContext" + num),
                    app.getParameter("rContext" + num)));
            num++;
        }
        currentPath = new StringBuffer(axiom);
        level = 0;
    }
    public int getLevel() {
        return level;
    }
    StringBuffer currentPath;
    public synchronized String getPath() {
        return ((currentPath == null) ? null : currentPath.toString());
    }
    private synchronized void setPath(StringBuffer path) {
        currentPath = path;
        level++;
    }
    public void generate() {
        StringBuffer newPath = new StringBuffer();
        int pos = 0;
        while (pos < currentPath.length()) {
            CLSRule rule = findRule(pos);
            if (rule == null) {
                newPath.append(currentPath.charAt(pos));
                pos++;
            } else {
                newPath.append(rule.succ);
                pos += rule.pred.length();
            }
        }
        setPath(newPath);
    }
    public CLSRule findRule(int pos) {
        for (int i = 0; i < rules.size(); i++) {
            CLSRule rule = rules.get(i);
            if (rule.matches(currentPath, pos)) {
                return rule;
            }
        }
        return null;
    }
}
class CLSRule {
    String pred;
    String succ;
    String lContext;
    String rContext;
    public CLSRule(String p, String d, String l, String r) {
        pred = p;
        succ = d;
        lContext = l;
        rContext = r;
    }
    public boolean matches(StringBuffer sb, int pos) {
        if (pos + pred.length() > sb.length()) {
            return false;
        }
        char cb[] = new char[pred.length()];
        sb.getChars(pos, pos + pred.length(), cb, 0);
        return pred.equals(new String(cb));
    }
}
