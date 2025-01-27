public class WindowsSliderUI extends BasicSliderUI
{
    private boolean rollover = false;
    private boolean pressed = false;
    public WindowsSliderUI(JSlider b){
        super(b);
    }
    public static ComponentUI createUI(JComponent b) {
        return new WindowsSliderUI((JSlider)b);
    }
    protected TrackListener createTrackListener(JSlider slider) {
        return new WindowsTrackListener();
    }
    private class WindowsTrackListener extends TrackListener {
        public void mouseMoved(MouseEvent e) {
            updateRollover(thumbRect.contains(e.getX(), e.getY()));
            super.mouseMoved(e);
        }
        public void mouseEntered(MouseEvent e) {
            updateRollover(thumbRect.contains(e.getX(), e.getY()));
            super.mouseEntered(e);
        }
        public void mouseExited(MouseEvent e) {
            updateRollover(false);
            super.mouseExited(e);
        }
        public void mousePressed(MouseEvent e) {
            updatePressed(thumbRect.contains(e.getX(), e.getY()));
            super.mousePressed(e);
        }
        public void mouseReleased(MouseEvent e) {
            updatePressed(false);
            super.mouseReleased(e);
        }
        public void updatePressed(boolean newPressed) {
            if (!slider.isEnabled()) {
                return;
            }
            if (pressed != newPressed) {
                pressed = newPressed;
                slider.repaint(thumbRect);
            }
        }
        public void updateRollover(boolean newRollover) {
            if (!slider.isEnabled()) {
                return;
            }
            if (rollover != newRollover) {
                rollover = newRollover;
                slider.repaint(thumbRect);
            }
        }
    }
    public void paintTrack(Graphics g)  {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            boolean vertical = (slider.getOrientation() == JSlider.VERTICAL);
            Part part = vertical ? Part.TKP_TRACKVERT : Part.TKP_TRACK;
            Skin skin = xp.getSkin(slider, part);
            if (vertical) {
                int x = (trackRect.width - skin.getWidth()) / 2;
                skin.paintSkin(g, trackRect.x + x, trackRect.y,
                               skin.getWidth(), trackRect.height, null);
            } else {
                int y = (trackRect.height - skin.getHeight()) / 2;
                skin.paintSkin(g, trackRect.x, trackRect.y + y,
                               trackRect.width, skin.getHeight(), null);
            }
        } else {
            super.paintTrack(g);
        }
    }
    protected void paintMinorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            g.setColor(xp.getColor(slider, Part.TKP_TICS, null, Prop.COLOR, Color.black));
        }
        super.paintMinorTickForHorizSlider(g, tickBounds, x);
    }
    protected void paintMajorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            g.setColor(xp.getColor(slider, Part.TKP_TICS, null, Prop.COLOR, Color.black));
        }
        super.paintMajorTickForHorizSlider(g, tickBounds, x);
    }
    protected void paintMinorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            g.setColor(xp.getColor(slider, Part.TKP_TICSVERT, null, Prop.COLOR, Color.black));
        }
        super.paintMinorTickForVertSlider(g, tickBounds, y);
    }
    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            g.setColor(xp.getColor(slider, Part.TKP_TICSVERT, null, Prop.COLOR, Color.black));
        }
        super.paintMajorTickForVertSlider(g, tickBounds, y);
    }
    public void paintThumb(Graphics g)  {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            Part part = getXPThumbPart();
            State state = State.NORMAL;
            if (slider.hasFocus()) {
                state = State.FOCUSED;
            }
            if (rollover) {
                state = State.HOT;
            }
            if (pressed) {
                state = State.PRESSED;
            }
            if(!slider.isEnabled()) {
                state = State.DISABLED;
            }
            xp.getSkin(slider, part).paintSkin(g, thumbRect.x, thumbRect.y, state);
        } else {
            super.paintThumb(g);
        }
    }
    protected Dimension getThumbSize() {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            Dimension size = new Dimension();
            Skin s = xp.getSkin(slider, getXPThumbPart());
            size.width = s.getWidth();
            size.height = s.getHeight();
            return size;
        } else {
            return super.getThumbSize();
        }
    }
    private Part getXPThumbPart() {
        XPStyle xp = XPStyle.getXP();
        Part part;
        boolean vertical = (slider.getOrientation() == JSlider.VERTICAL);
        boolean leftToRight = slider.getComponentOrientation().isLeftToRight();
        Boolean paintThumbArrowShape =
                (Boolean)slider.getClientProperty("Slider.paintThumbArrowShape");
        if ((!slider.getPaintTicks() && paintThumbArrowShape == null) ||
            paintThumbArrowShape == Boolean.FALSE) {
                part = vertical ? Part.TKP_THUMBVERT
                                : Part.TKP_THUMB;
        } else {
                part = vertical ? (leftToRight ? Part.TKP_THUMBRIGHT : Part.TKP_THUMBLEFT)
                                : Part.TKP_THUMBBOTTOM;
        }
        return part;
    }
}
