public class MetalScrollButton extends BasicArrowButton
{
  private static Color shadowColor;
  private static Color highlightColor;
  private boolean isFreeStanding = false;
  private int buttonWidth;
        public MetalScrollButton( int direction, int width, boolean freeStanding )
        {
            super( direction );
            shadowColor = UIManager.getColor("ScrollBar.darkShadow");
            highlightColor = UIManager.getColor("ScrollBar.highlight");
            buttonWidth = width;
            isFreeStanding = freeStanding;
        }
        public void setFreeStanding( boolean freeStanding )
        {
            isFreeStanding = freeStanding;
        }
        public void paint( Graphics g )
        {
            boolean leftToRight = MetalUtils.isLeftToRight(this);
            boolean isEnabled = getParent().isEnabled();
            Color arrowColor = isEnabled ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlDisabled();
            boolean isPressed = getModel().isPressed();
            int width = getWidth();
            int height = getHeight();
            int w = width;
            int h = height;
            int arrowHeight = (height+1) / 4;
            int arrowWidth = (height+1) / 2;
            if ( isPressed )
            {
                g.setColor( MetalLookAndFeel.getControlShadow() );
            }
            else
            {
                g.setColor( getBackground() );
            }
            g.fillRect( 0, 0, width, height );
            if ( getDirection() == NORTH )
            {
                if ( !isFreeStanding ) {
                    height +=1;
                    g.translate( 0, -1 );
                    width += 2;
                    if ( !leftToRight ) {
                        g.translate( -1, 0 );
                    }
                }
                g.setColor( arrowColor );
                int startY = ((h+1) - arrowHeight) / 2;
                int startX = (w / 2);
                for (int line = 0; line < arrowHeight; line++) {
                    g.drawLine( startX-line, startY+line, startX +line+1, startY+line);
                }
                if (isEnabled) {
                    g.setColor( highlightColor );
                    if ( !isPressed )
                    {
                        g.drawLine( 1, 1, width - 3, 1 );
                        g.drawLine( 1, 1, 1, height - 1 );
                    }
                    g.drawLine( width - 1, 1, width - 1, height - 1 );
                    g.setColor( shadowColor );
                    g.drawLine( 0, 0, width - 2, 0 );
                    g.drawLine( 0, 0, 0, height - 1 );
                    g.drawLine( width - 2, 2, width - 2, height - 1 );
                } else {
                    MetalUtils.drawDisabledBorder(g, 0, 0, width, height+1);
                }
                if ( !isFreeStanding ) {
                    height -= 1;
                    g.translate( 0, 1 );
                    width -= 2;
                    if ( !leftToRight ) {
                        g.translate( 1, 0 );
                    }
                }
            }
            else if ( getDirection() == SOUTH )
            {
                if ( !isFreeStanding ) {
                    height += 1;
                    width += 2;
                    if ( !leftToRight ) {
                        g.translate( -1, 0 );
                    }
                }
                g.setColor( arrowColor );
                int startY = (((h+1) - arrowHeight) / 2)+ arrowHeight-1;
                int startX = (w / 2);
                for (int line = 0; line < arrowHeight; line++) {
                    g.drawLine( startX-line, startY-line, startX +line+1, startY-line);
                }
                if (isEnabled) {
                    g.setColor( highlightColor );
                    if ( !isPressed )
                    {
                        g.drawLine( 1, 0, width - 3, 0 );
                        g.drawLine( 1, 0, 1, height - 3 );
                    }
                    g.drawLine( 1, height - 1, width - 1, height - 1 );
                    g.drawLine( width - 1, 0, width - 1, height - 1 );
                    g.setColor( shadowColor );
                    g.drawLine( 0, 0, 0, height - 2 );
                    g.drawLine( width - 2, 0, width - 2, height - 2 );
                    g.drawLine( 2, height - 2, width - 2, height - 2 );
                } else {
                    MetalUtils.drawDisabledBorder(g, 0,-1, width, height+1);
                }
                if ( !isFreeStanding ) {
                    height -= 1;
                    width -= 2;
                    if ( !leftToRight ) {
                        g.translate( 1, 0 );
                    }
                }
            }
            else if ( getDirection() == EAST )
            {
                if ( !isFreeStanding ) {
                    height += 2;
                    width += 1;
                }
                g.setColor( arrowColor );
                int startX = (((w+1) - arrowHeight) / 2) + arrowHeight-1;
                int startY = (h / 2);
                for (int line = 0; line < arrowHeight; line++) {
                    g.drawLine( startX-line, startY-line, startX -line, startY+line+1);
                }
                if (isEnabled) {
                    g.setColor( highlightColor );
                    if ( !isPressed )
                    {
                        g.drawLine( 0, 1, width - 3, 1 );
                        g.drawLine( 0, 1, 0, height - 3 );
                    }
                    g.drawLine( width - 1, 1, width - 1, height - 1 );
                    g.drawLine( 0, height - 1, width - 1, height - 1 );
                    g.setColor( shadowColor );
                    g.drawLine( 0, 0,width - 2, 0 );
                    g.drawLine( width - 2, 2, width - 2, height - 2 );
                    g.drawLine( 0, height - 2, width - 2, height - 2 );
                } else {
                    MetalUtils.drawDisabledBorder(g,-1,0, width+1, height);
                }
                if ( !isFreeStanding ) {
                    height -= 2;
                    width -= 1;
                }
            }
            else if ( getDirection() == WEST )
            {
                if ( !isFreeStanding ) {
                    height += 2;
                    width += 1;
                    g.translate( -1, 0 );
                }
                g.setColor( arrowColor );
                int startX = (((w+1) - arrowHeight) / 2);
                int startY = (h / 2);
                for (int line = 0; line < arrowHeight; line++) {
                    g.drawLine( startX+line, startY-line, startX +line, startY+line+1);
                }
                if (isEnabled) {
                    g.setColor( highlightColor );
                    if ( !isPressed )
                    {
                        g.drawLine( 1, 1, width - 1, 1 );
                        g.drawLine( 1, 1, 1, height - 3 );
                    }
                    g.drawLine( 1, height - 1, width - 1, height - 1 );
                    g.setColor( shadowColor );
                    g.drawLine( 0, 0, width - 1, 0 );
                    g.drawLine( 0, 0, 0, height - 2 );
                    g.drawLine( 2, height - 2, width - 1, height - 2 );
                } else {
                    MetalUtils.drawDisabledBorder(g,0,0, width+1, height);
                }
                if ( !isFreeStanding ) {
                    height -= 2;
                    width -= 1;
                    g.translate( 1, 0 );
                }
            }
        }
        public Dimension getPreferredSize()
        {
            if ( getDirection() == NORTH )
            {
                return new Dimension( buttonWidth, buttonWidth - 2 );
            }
            else if ( getDirection() == SOUTH )
            {
                return new Dimension( buttonWidth, buttonWidth - (isFreeStanding ? 1 : 2) );
            }
            else if ( getDirection() == EAST )
            {
                return new Dimension( buttonWidth - (isFreeStanding ? 1 : 2), buttonWidth );
            }
            else if ( getDirection() == WEST )
            {
                return new Dimension( buttonWidth - 2, buttonWidth );
            }
            else
            {
                return new Dimension( 0, 0 );
            }
        }
        public Dimension getMinimumSize()
        {
            return getPreferredSize();
        }
        public Dimension getMaximumSize()
        {
            return new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
        }
        public int getButtonWidth() {
            return buttonWidth;
        }
}
