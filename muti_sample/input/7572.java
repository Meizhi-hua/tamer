public class MetalSliderUI extends BasicSliderUI {
    protected final int TICK_BUFFER = 4;
    protected boolean filledSlider = false;
    protected static Color thumbColor;
    protected static Color highlightColor;
    protected static Color darkShadowColor;
    protected static int trackWidth;
    protected static int tickLength;
    private int safeLength;
    protected static Icon horizThumbIcon;
    protected static Icon vertThumbIcon;
    private static Icon SAFE_HORIZ_THUMB_ICON;
    private static Icon SAFE_VERT_THUMB_ICON;
    protected final String SLIDER_FILL = "JSlider.isFilled";
    public static ComponentUI createUI(JComponent c)    {
        return new MetalSliderUI();
    }
    public MetalSliderUI() {
        super( null );
    }
    private static Icon getHorizThumbIcon() {
        if (System.getSecurityManager() != null) {
            return SAFE_HORIZ_THUMB_ICON;
        } else {
            return horizThumbIcon;
        }
    }
    private static Icon getVertThumbIcon() {
        if (System.getSecurityManager() != null) {
            return SAFE_VERT_THUMB_ICON;
        } else {
            return vertThumbIcon;
        }
    }
    public void installUI( JComponent c ) {
        trackWidth = ((Integer)UIManager.get( "Slider.trackWidth" )).intValue();
        tickLength = safeLength = ((Integer)UIManager.get( "Slider.majorTickLength" )).intValue();
        horizThumbIcon = SAFE_HORIZ_THUMB_ICON =
                UIManager.getIcon( "Slider.horizontalThumbIcon" );
        vertThumbIcon = SAFE_VERT_THUMB_ICON =
                UIManager.getIcon( "Slider.verticalThumbIcon" );
        super.installUI( c );
        thumbColor = UIManager.getColor("Slider.thumb");
        highlightColor = UIManager.getColor("Slider.highlight");
        darkShadowColor = UIManager.getColor("Slider.darkShadow");
        scrollListener.setScrollByBlock( false );
        prepareFilledSliderField();
    }
    protected PropertyChangeListener createPropertyChangeListener( JSlider slider ) {
        return new MetalPropertyListener();
    }
    protected class MetalPropertyListener extends BasicSliderUI.PropertyChangeHandler {
        public void propertyChange( PropertyChangeEvent e ) {  
            super.propertyChange( e );
            if (e.getPropertyName().equals(SLIDER_FILL)) {
                prepareFilledSliderField();
            }
        }
    }
    private void prepareFilledSliderField() {
        filledSlider = MetalLookAndFeel.usingOcean();
        Object sliderFillProp = slider.getClientProperty(SLIDER_FILL);
        if (sliderFillProp != null) {
            filledSlider = ((Boolean) sliderFillProp).booleanValue();
        }
    }
    public void paintThumb(Graphics g)  {
        Rectangle knobBounds = thumbRect;
        g.translate( knobBounds.x, knobBounds.y );
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            getHorizThumbIcon().paintIcon( slider, g, 0, 0 );
        }
        else {
            getVertThumbIcon().paintIcon( slider, g, 0, 0 );
        }
        g.translate( -knobBounds.x, -knobBounds.y );
    }
    private Rectangle getPaintTrackRect() {
        int trackLeft = 0, trackRight, trackTop = 0, trackBottom;
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            trackBottom = (trackRect.height - 1) - getThumbOverhang();
            trackTop = trackBottom - (getTrackWidth() - 1);
            trackRight = trackRect.width - 1;
        }
        else {
            if (MetalUtils.isLeftToRight(slider)) {
                trackLeft = (trackRect.width - getThumbOverhang()) -
                                                         getTrackWidth();
                trackRight = (trackRect.width - getThumbOverhang()) - 1;
            }
            else {
                trackLeft = getThumbOverhang();
                trackRight = getThumbOverhang() + getTrackWidth() - 1;
            }
            trackBottom = trackRect.height - 1;
        }
        return new Rectangle(trackRect.x + trackLeft, trackRect.y + trackTop,
                             trackRight - trackLeft, trackBottom - trackTop);
    }
    public void paintTrack(Graphics g)  {
        if (MetalLookAndFeel.usingOcean()) {
            oceanPaintTrack(g);
            return;
        }
        Color trackColor = !slider.isEnabled() ? MetalLookAndFeel.getControlShadow() :
                           slider.getForeground();
        boolean leftToRight = MetalUtils.isLeftToRight(slider);
        g.translate( trackRect.x, trackRect.y );
        int trackLeft = 0;
        int trackTop = 0;
        int trackRight;
        int trackBottom;
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            trackBottom = (trackRect.height - 1) - getThumbOverhang();
            trackTop = trackBottom - (getTrackWidth() - 1);
            trackRight = trackRect.width - 1;
        }
        else {
            if (leftToRight) {
                trackLeft = (trackRect.width - getThumbOverhang()) -
                                                         getTrackWidth();
                trackRight = (trackRect.width - getThumbOverhang()) - 1;
            }
            else {
                trackLeft = getThumbOverhang();
                trackRight = getThumbOverhang() + getTrackWidth() - 1;
            }
            trackBottom = trackRect.height - 1;
        }
        if ( slider.isEnabled() ) {
            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawRect( trackLeft, trackTop,
                        (trackRight - trackLeft) - 1, (trackBottom - trackTop) - 1 );
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine( trackLeft + 1, trackBottom, trackRight, trackBottom );
            g.drawLine( trackRight, trackTop + 1, trackRight, trackBottom );
            g.setColor( MetalLookAndFeel.getControlShadow() );
            g.drawLine( trackLeft + 1, trackTop + 1, trackRight - 2, trackTop + 1 );
            g.drawLine( trackLeft + 1, trackTop + 1, trackLeft + 1, trackBottom - 2 );
        }
        else {
            g.setColor( MetalLookAndFeel.getControlShadow() );
            g.drawRect( trackLeft, trackTop,
                        (trackRight - trackLeft) - 1, (trackBottom - trackTop) - 1 );
        }
        if ( filledSlider ) {
            int middleOfThumb;
            int fillTop;
            int fillLeft;
            int fillBottom;
            int fillRight;
            if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                middleOfThumb = thumbRect.x + (thumbRect.width / 2);
                middleOfThumb -= trackRect.x; 
                fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
                fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;
                if ( !drawInverted() ) {
                    fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                    fillRight = middleOfThumb;
                }
                else {
                    fillLeft = middleOfThumb;
                    fillRight = !slider.isEnabled() ? trackRight - 1 : trackRight - 2;
                }
            }
            else {
                middleOfThumb = thumbRect.y + (thumbRect.height / 2);
                middleOfThumb -= trackRect.y; 
                fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                fillRight = !slider.isEnabled() ? trackRight - 1 : trackRight - 2;
                if ( !drawInverted() ) {
                    fillTop = middleOfThumb;
                    fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;
                }
                else {
                    fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
                    fillBottom = middleOfThumb;
                }
            }
            if ( slider.isEnabled() ) {
                g.setColor( slider.getBackground() );
                g.drawLine( fillLeft, fillTop, fillRight, fillTop );
                g.drawLine( fillLeft, fillTop, fillLeft, fillBottom );
                g.setColor( MetalLookAndFeel.getControlShadow() );
                g.fillRect( fillLeft + 1, fillTop + 1,
                            fillRight - fillLeft, fillBottom - fillTop );
            }
            else {
                g.setColor( MetalLookAndFeel.getControlShadow() );
                g.fillRect(fillLeft, fillTop, fillRight - fillLeft, fillBottom - fillTop);
            }
        }
        g.translate( -trackRect.x, -trackRect.y );
    }
    private void oceanPaintTrack(Graphics g)  {
        boolean leftToRight = MetalUtils.isLeftToRight(slider);
        boolean drawInverted = drawInverted();
        Color sliderAltTrackColor = (Color)UIManager.get(
                                    "Slider.altTrackColor");
        Rectangle paintRect = getPaintTrackRect();
        g.translate(paintRect.x, paintRect.y);
        int w = paintRect.width;
        int h = paintRect.height;
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int middleOfThumb = thumbRect.x + thumbRect.width / 2 - paintRect.x;
            if (slider.isEnabled()) {
                int fillMinX;
                int fillMaxX;
                if (middleOfThumb > 0) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getControlDarkShadow() :
                            MetalLookAndFeel.getPrimaryControlDarkShadow());
                    g.drawRect(0, 0, middleOfThumb - 1, h - 1);
                }
                if (middleOfThumb < w) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getPrimaryControlDarkShadow() :
                            MetalLookAndFeel.getControlDarkShadow());
                    g.drawRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                }
                if (filledSlider) {
                    g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
                    if (drawInverted) {
                        fillMinX = middleOfThumb;
                        fillMaxX = w - 2;
                        g.drawLine(1, 1, middleOfThumb, 1);
                    } else {
                        fillMinX = 1;
                        fillMaxX = middleOfThumb;
                        g.drawLine(middleOfThumb, 1, w - 1, 1);
                    }
                    if (h == 6) {
                        g.setColor(MetalLookAndFeel.getWhite());
                        g.drawLine(fillMinX, 1, fillMaxX, 1);
                        g.setColor(sliderAltTrackColor);
                        g.drawLine(fillMinX, 2, fillMaxX, 2);
                        g.setColor(MetalLookAndFeel.getControlShadow());
                        g.drawLine(fillMinX, 3, fillMaxX, 3);
                        g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
                        g.drawLine(fillMinX, 4, fillMaxX, 4);
                    }
                }
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                if (middleOfThumb > 0) {
                    if (!drawInverted && filledSlider) {
                        g.fillRect(0, 0, middleOfThumb - 1, h - 1);
                    } else {
                        g.drawRect(0, 0, middleOfThumb - 1, h - 1);
                    }
                }
                if (middleOfThumb < w) {
                    if (drawInverted && filledSlider) {
                        g.fillRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                    } else {
                        g.drawRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                    }
                }
            }
        } else {
            int middleOfThumb = thumbRect.y + (thumbRect.height / 2) - paintRect.y;
            if (slider.isEnabled()) {
                int fillMinY;
                int fillMaxY;
                if (middleOfThumb > 0) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getPrimaryControlDarkShadow() :
                            MetalLookAndFeel.getControlDarkShadow());
                    g.drawRect(0, 0, w - 1, middleOfThumb - 1);
                }
                if (middleOfThumb < h) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getControlDarkShadow() :
                            MetalLookAndFeel.getPrimaryControlDarkShadow());
                    g.drawRect(0, middleOfThumb, w - 1, h - middleOfThumb - 1);
                }
                if (filledSlider) {
                    g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
                    if (drawInverted()) {
                        fillMinY = 1;
                        fillMaxY = middleOfThumb;
                        if (leftToRight) {
                            g.drawLine(1, middleOfThumb, 1, h - 1);
                        } else {
                            g.drawLine(w - 2, middleOfThumb, w - 2, h - 1);
                        }
                    } else {
                        fillMinY = middleOfThumb;
                        fillMaxY = h - 2;
                        if (leftToRight) {
                            g.drawLine(1, 1, 1, middleOfThumb);
                        } else {
                            g.drawLine(w - 2, 1, w - 2, middleOfThumb);
                        }
                    }
                    if (w == 6) {
                        g.setColor(leftToRight ? MetalLookAndFeel.getWhite() : MetalLookAndFeel.getPrimaryControlShadow());
                        g.drawLine(1, fillMinY, 1, fillMaxY);
                        g.setColor(leftToRight ? sliderAltTrackColor : MetalLookAndFeel.getControlShadow());
                        g.drawLine(2, fillMinY, 2, fillMaxY);
                        g.setColor(leftToRight ? MetalLookAndFeel.getControlShadow() : sliderAltTrackColor);
                        g.drawLine(3, fillMinY, 3, fillMaxY);
                        g.setColor(leftToRight ? MetalLookAndFeel.getPrimaryControlShadow() : MetalLookAndFeel.getWhite());
                        g.drawLine(4, fillMinY, 4, fillMaxY);
                    }
                }
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                if (middleOfThumb > 0) {
                    if (drawInverted && filledSlider) {
                        g.fillRect(0, 0, w - 1, middleOfThumb - 1);
                    } else {
                        g.drawRect(0, 0, w - 1, middleOfThumb - 1);
                    }
                }
                if (middleOfThumb < h) {
                    if (!drawInverted && filledSlider) {
                        g.fillRect(0, middleOfThumb, w - 1, h - middleOfThumb - 1);
                    } else {
                        g.drawRect(0, middleOfThumb, w - 1, h - middleOfThumb - 1);
                    }
                }
            }
        }
        g.translate(-paintRect.x, -paintRect.y);
    }
    public void paintFocus(Graphics g)  {
    }
    protected Dimension getThumbSize() {
        Dimension size = new Dimension();
        if ( slider.getOrientation() == JSlider.VERTICAL ) {
            size.width = getVertThumbIcon().getIconWidth();
            size.height = getVertThumbIcon().getIconHeight();
        }
        else {
            size.width = getHorizThumbIcon().getIconWidth();
            size.height = getHorizThumbIcon().getIconHeight();
        }
        return size;
    }
    public int getTickLength() {
        return slider.getOrientation() == JSlider.HORIZONTAL ? safeLength + TICK_BUFFER + 1 :
        safeLength + TICK_BUFFER + 3;
    }
    protected int getTrackWidth() {
        final double kIdealTrackWidth = 7.0;
        final double kIdealThumbHeight = 16.0;
        final double kWidthScalar = kIdealTrackWidth / kIdealThumbHeight;
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            return (int)(kWidthScalar * thumbRect.height);
        }
        else {
            return (int)(kWidthScalar * thumbRect.width);
        }
    }
    protected int getTrackLength() {
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            return trackRect.width;
        }
        return trackRect.height;
    }
    protected int getThumbOverhang() {
        return (int)(getThumbSize().getHeight()-getTrackWidth())/2;
    }
    protected void scrollDueToClickInTrack( int dir ) {
        scrollByUnit( dir );
    }
    protected void paintMinorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        g.setColor( slider.isEnabled() ? slider.getForeground() : MetalLookAndFeel.getControlShadow() );
        g.drawLine( x, TICK_BUFFER, x, TICK_BUFFER + (safeLength / 2) );
    }
    protected void paintMajorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        g.setColor( slider.isEnabled() ? slider.getForeground() : MetalLookAndFeel.getControlShadow() );
        g.drawLine( x, TICK_BUFFER , x, TICK_BUFFER + (safeLength - 1) );
    }
    protected void paintMinorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.setColor( slider.isEnabled() ? slider.getForeground() : MetalLookAndFeel.getControlShadow() );
        if (MetalUtils.isLeftToRight(slider)) {
            g.drawLine( TICK_BUFFER, y, TICK_BUFFER + (safeLength / 2), y );
        }
        else {
            g.drawLine( 0, y, safeLength/2, y );
        }
    }
    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.setColor( slider.isEnabled() ? slider.getForeground() : MetalLookAndFeel.getControlShadow() );
        if (MetalUtils.isLeftToRight(slider)) {
            g.drawLine( TICK_BUFFER, y, TICK_BUFFER + safeLength, y );
        }
        else {
            g.drawLine( 0, y, safeLength, y );
        }
    }
}