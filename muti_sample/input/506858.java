final class HTMLTable extends IElement.Factory.ElementImpl
{
    public static interface ICell extends IElement
    {
        ICell setColspan (final int span);
    } 
    public static interface IRow extends IElement
    {
        ICell newCell ();
    } 
    public HTMLTable (final String width, final String border, final String cellpadding, final String cellspacing)
    {
        super (Tag.TABLE, AttributeSet.create ());
        final AttributeSet attrs = getAttributes ();
        if (width != null) attrs.set (Attribute.WIDTH, width);
        if (border != null) attrs.set (Attribute.BORDER, border);
        if (cellpadding != null) attrs.set (Attribute.CELLPADDING, cellpadding);
        if (cellspacing != null) attrs.set (Attribute.CELLSPACING, cellspacing);
    }
    public void setCaption (final String align, final String text, final boolean nbsp)
    {
        m_caption = IElement.Factory.create (Tag.CAPTION);
        m_caption.getAttributes ().set (Attribute.ALIGN, align);
        m_caption.setText (text, nbsp);
    }
    public IRow newTitleRow ()
    {
        final Row row = new Row (true);
        add (row);
        return row;
    }
    public IRow newRow ()
    {
        final Row row = new Row (false);
        add (row);
        return row;
    }
    public void emit (final HTMLWriter out)
    {
        if (m_caption != null)
        {
            add (0, m_caption);
        }
        super.emit(out);
    }
    private static class Cell extends IElement.Factory.ElementImpl
                              implements ICell
    {
        public ICell setColspan (final int span)
        {
            getAttributes ().set (Attribute.COLSPAN, span);
            return this;
        }
        Cell (Tag tag)
        {
            super (tag, AttributeSet.create ());
        }
    } 
    private static class Row extends IElement.Factory.ElementImpl
                             implements IRow
    {
        public ICell newCell ()
        {
            final ICell cell = new Cell (m_th ? Tag.TH : Tag.TD);
            add (cell);
            return cell;
        }
        Row (final boolean th)
        {
            super (Tag.TR, AttributeSet.create ());
            m_th = th;
        }
        private final boolean m_th;
    } 
    private IElement m_caption;
} 
