 class TableView extends BoxView implements ViewFactory {
    public TableView(Element elem) {
        super(elem, View.Y_AXIS);
        rows = new Vector<RowView>();
        gridValid = false;
        captionIndex = -1;
        totalColumnRequirements = new SizeRequirements();
    }
    protected RowView createTableRow(Element elem) {
        Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (o == HTML.Tag.TR) {
            return new RowView(elem);
        }
        return null;
    }
    public int getColumnCount() {
        return columnSpans.length;
    }
    public int getColumnSpan(int col) {
        if (col < columnSpans.length) {
            return columnSpans[col];
        }
        return 0;
    }
    public int getRowCount() {
        return rows.size();
    }
    public int getMultiRowSpan(int row0, int row1) {
        RowView rv0 = getRow(row0);
        RowView rv1 = getRow(row1);
        if ((rv0 != null) && (rv1 != null)) {
            int index0 = rv0.viewIndex;
            int index1 = rv1.viewIndex;
            int span = getOffset(Y_AXIS, index1) - getOffset(Y_AXIS, index0) +
                getSpan(Y_AXIS, index1);
            return span;
        }
        return 0;
    }
    public int getRowSpan(int row) {
        RowView rv = getRow(row);
        if (rv != null) {
            return getSpan(Y_AXIS, rv.viewIndex);
        }
        return 0;
    }
    RowView getRow(int row) {
        if (row < rows.size()) {
            return rows.elementAt(row);
        }
        return null;
    }
    protected View getViewAtPoint(int x, int y, Rectangle alloc) {
        int n = getViewCount();
        View v;
        Rectangle allocation = new Rectangle();
        for (int i = 0; i < n; i++) {
            allocation.setBounds(alloc);
            childAllocation(i, allocation);
            v = getView(i);
            if (v instanceof RowView) {
                v = ((RowView)v).findViewAtPoint(x, y, allocation);
                if (v != null) {
                    alloc.setBounds(allocation);
                    return v;
                }
            }
        }
        return super.getViewAtPoint(x, y, alloc);
    }
    protected int getColumnsOccupied(View v) {
        AttributeSet a = v.getElement().getAttributes();
        if (a.isDefined(HTML.Attribute.COLSPAN)) {
            String s = (String) a.getAttribute(HTML.Attribute.COLSPAN);
            if (s != null) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                }
            }
        }
        return 1;
    }
    protected int getRowsOccupied(View v) {
        AttributeSet a = v.getElement().getAttributes();
        if (a.isDefined(HTML.Attribute.ROWSPAN)) {
            String s = (String) a.getAttribute(HTML.Attribute.ROWSPAN);
            if (s != null) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                }
            }
        }
        return 1;
    }
    protected void invalidateGrid() {
        gridValid = false;
    }
    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument) getDocument();
        return doc.getStyleSheet();
    }
    void updateInsets() {
        short top = (short) painter.getInset(TOP, this);
        short bottom = (short) painter.getInset(BOTTOM, this);
        if (captionIndex != -1) {
            View caption = getView(captionIndex);
            short h = (short) caption.getPreferredSpan(Y_AXIS);
            AttributeSet a = caption.getAttributes();
            Object align = a.getAttribute(CSS.Attribute.CAPTION_SIDE);
            if ((align != null) && (align.equals("bottom"))) {
                bottom += h;
            } else {
                top += h;
            }
        }
        setInsets(top, (short) painter.getInset(LEFT, this),
                  bottom, (short) painter.getInset(RIGHT, this));
    }
    protected void setPropertiesFromAttributes() {
        StyleSheet sheet = getStyleSheet();
        attr = sheet.getViewAttributes(this);
        painter = sheet.getBoxPainter(attr);
        if (attr != null) {
            setInsets((short) painter.getInset(TOP, this),
                      (short) painter.getInset(LEFT, this),
                          (short) painter.getInset(BOTTOM, this),
                      (short) painter.getInset(RIGHT, this));
            CSS.LengthValue lv = (CSS.LengthValue)
                attr.getAttribute(CSS.Attribute.BORDER_SPACING);
            if (lv != null) {
                cellSpacing = (int) lv.getValue();
            } else {
                cellSpacing = 0;
            }
            lv = (CSS.LengthValue)
                    attr.getAttribute(CSS.Attribute.BORDER_TOP_WIDTH);
            if (lv != null) {
                    borderWidth = (int) lv.getValue();
            } else {
                    borderWidth = 0;
            }
                }
    }
    void updateGrid() {
        if (! gridValid) {
            relativeCells = false;
            multiRowCells = false;
            captionIndex = -1;
            rows.removeAllElements();
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                if (v instanceof RowView) {
                    rows.addElement((RowView) v);
                    RowView rv = (RowView) v;
                    rv.clearFilledColumns();
                    rv.rowIndex = rows.size() - 1;
                    rv.viewIndex = i;
                } else {
                    Object o = v.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
                    if (o instanceof HTML.Tag) {
                        HTML.Tag kind = (HTML.Tag) o;
                        if (kind == HTML.Tag.CAPTION) {
                            captionIndex = i;
                        }
                    }
                }
            }
            int maxColumns = 0;
            int nrows = rows.size();
            for (int row = 0; row < nrows; row++) {
                RowView rv = getRow(row);
                int col = 0;
                for (int cell = 0; cell < rv.getViewCount(); cell++, col++) {
                    View cv = rv.getView(cell);
                    if (! relativeCells) {
                        AttributeSet a = cv.getAttributes();
                        CSS.LengthValue lv = (CSS.LengthValue)
                            a.getAttribute(CSS.Attribute.WIDTH);
                        if ((lv != null) && (lv.isPercentage())) {
                            relativeCells = true;
                        }
                    }
                    for (; rv.isFilled(col); col++);
                    int rowSpan = getRowsOccupied(cv);
                    if (rowSpan > 1) {
                        multiRowCells = true;
                    }
                    int colSpan = getColumnsOccupied(cv);
                    if ((colSpan > 1) || (rowSpan > 1)) {
                        int rowLimit = row + rowSpan;
                        int colLimit = col + colSpan;
                        for (int i = row; i < rowLimit; i++) {
                            for (int j = col; j < colLimit; j++) {
                                if (i != row || j != col) {
                                    addFill(i, j);
                                }
                            }
                        }
                        if (colSpan > 1) {
                            col += colSpan - 1;
                        }
                    }
                }
                maxColumns = Math.max(maxColumns, col);
            }
            columnSpans = new int[maxColumns];
            columnOffsets = new int[maxColumns];
            columnRequirements = new SizeRequirements[maxColumns];
            for (int i = 0; i < maxColumns; i++) {
                columnRequirements[i] = new SizeRequirements();
                columnRequirements[i].maximum = Integer.MAX_VALUE;
            }
            gridValid = true;
        }
    }
    void addFill(int row, int col) {
        RowView rv = getRow(row);
        if (rv != null) {
            rv.fillColumn(col);
        }
    }
    protected void layoutColumns(int targetSpan, int[] offsets, int[] spans,
                                 SizeRequirements[] reqs) {
        Arrays.fill(offsets, 0);
        Arrays.fill(spans, 0);
        colIterator.setLayoutArrays(offsets, spans, targetSpan);
        CSS.calculateTiledLayout(colIterator, targetSpan);
    }
    void calculateColumnRequirements(int axis) {
        for (SizeRequirements req : columnRequirements) {
            req.minimum = 0;
            req.preferred = 0;
            req.maximum = Integer.MAX_VALUE;
        }
        Container host = getContainer();
        if (host != null) {
            if (host instanceof JTextComponent) {
                skipComments = !((JTextComponent)host).isEditable();
            } else {
                skipComments = true;
            }
        }
        boolean hasMultiColumn = false;
        int nrows = getRowCount();
        for (int i = 0; i < nrows; i++) {
            RowView row = getRow(i);
            int col = 0;
            int ncells = row.getViewCount();
            for (int cell = 0; cell < ncells; cell++) {
                View cv = row.getView(cell);
                if (skipComments && !(cv instanceof CellView)) {
                    continue;
                }
                for (; row.isFilled(col); col++); 
                int rowSpan = getRowsOccupied(cv);
                int colSpan = getColumnsOccupied(cv);
                if (colSpan == 1) {
                    checkSingleColumnCell(axis, col, cv);
                } else {
                    hasMultiColumn = true;
                    col += colSpan - 1;
                }
                col++;
            }
        }
        if (hasMultiColumn) {
            for (int i = 0; i < nrows; i++) {
                RowView row = getRow(i);
                int col = 0;
                int ncells = row.getViewCount();
                for (int cell = 0; cell < ncells; cell++) {
                    View cv = row.getView(cell);
                    if (skipComments && !(cv instanceof CellView)) {
                        continue;
                    }
                    for (; row.isFilled(col); col++); 
                    int colSpan = getColumnsOccupied(cv);
                    if (colSpan > 1) {
                        checkMultiColumnCell(axis, col, colSpan, cv);
                        col += colSpan - 1;
                    }
                    col++;
                }
            }
        }
    }
    void checkSingleColumnCell(int axis, int col, View v) {
        SizeRequirements req = columnRequirements[col];
        req.minimum = Math.max((int) v.getMinimumSpan(axis), req.minimum);
        req.preferred = Math.max((int) v.getPreferredSpan(axis), req.preferred);
    }
    void checkMultiColumnCell(int axis, int col, int ncols, View v) {
        long min = 0;
        long pref = 0;
        long max = 0;
        for (int i = 0; i < ncols; i++) {
            SizeRequirements req = columnRequirements[col + i];
            min += req.minimum;
            pref += req.preferred;
            max += req.maximum;
        }
        int cmin = (int) v.getMinimumSpan(axis);
        if (cmin > min) {
            SizeRequirements[] reqs = new SizeRequirements[ncols];
            for (int i = 0; i < ncols; i++) {
                reqs[i] = columnRequirements[col + i];
            }
            int[] spans = new int[ncols];
            int[] offsets = new int[ncols];
            SizeRequirements.calculateTiledPositions(cmin, null, reqs,
                                                     offsets, spans);
            for (int i = 0; i < ncols; i++) {
                SizeRequirements req = reqs[i];
                req.minimum = Math.max(spans[i], req.minimum);
                req.preferred = Math.max(req.minimum, req.preferred);
                req.maximum = Math.max(req.preferred, req.maximum);
            }
        }
        int cpref = (int) v.getPreferredSpan(axis);
        if (cpref > pref) {
            SizeRequirements[] reqs = new SizeRequirements[ncols];
            for (int i = 0; i < ncols; i++) {
                reqs[i] = columnRequirements[col + i];
            }
            int[] spans = new int[ncols];
            int[] offsets = new int[ncols];
            SizeRequirements.calculateTiledPositions(cpref, null, reqs,
                                                     offsets, spans);
            for (int i = 0; i < ncols; i++) {
                SizeRequirements req = reqs[i];
                req.preferred = Math.max(spans[i], req.preferred);
                req.maximum = Math.max(req.preferred, req.maximum);
            }
        }
    }
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        updateGrid();
        calculateColumnRequirements(axis);
        if (r == null) {
            r = new SizeRequirements();
        }
        long min = 0;
        long pref = 0;
        int n = columnRequirements.length;
        for (int i = 0; i < n; i++) {
            SizeRequirements req = columnRequirements[i];
            min += req.minimum;
            pref += req.preferred;
        }
        int adjust = (n + 1) * cellSpacing + 2 * borderWidth;
        min += adjust;
        pref += adjust;
        r.minimum = (int) min;
        r.preferred = (int) pref;
        r.maximum = (int) pref;
        AttributeSet attr = getAttributes();
        CSS.LengthValue cssWidth = (CSS.LengthValue)attr.getAttribute(
                                                    CSS.Attribute.WIDTH);
        if (BlockView.spanSetFromAttributes(axis, r, cssWidth, null)) {
            if (r.minimum < (int)min) {
                r.maximum = r.minimum = r.preferred = (int) min;
            }
        }
        totalColumnRequirements.minimum = r.minimum;
        totalColumnRequirements.preferred = r.preferred;
        totalColumnRequirements.maximum = r.maximum;
        Object o = attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
        if (o != null) {
            String ta = o.toString();
            if (ta.equals("left")) {
                r.alignment = 0;
            } else if (ta.equals("center")) {
                r.alignment = 0.5f;
            } else if (ta.equals("right")) {
                r.alignment = 1;
            } else {
                r.alignment = 0;
            }
        } else {
            r.alignment = 0;
        }
        return r;
    }
    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
        updateInsets();
        rowIterator.updateAdjustments();
        r = CSS.calculateTiledRequirements(rowIterator, r);
        r.maximum = r.preferred;
        return r;
    }
    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        updateGrid();
        int n = getRowCount();
        for (int i = 0; i < n; i++) {
            RowView row = getRow(i);
            row.layoutChanged(axis);
        }
        layoutColumns(targetSpan, columnOffsets, columnSpans, columnRequirements);
        super.layoutMinorAxis(targetSpan, axis, offsets, spans);
    }
    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        rowIterator.setLayoutArrays(offsets, spans);
        CSS.calculateTiledLayout(rowIterator, targetSpan);
        if (captionIndex != -1) {
            View caption = getView(captionIndex);
            int h = (int) caption.getPreferredSpan(Y_AXIS);
            spans[captionIndex] = h;
            short boxBottom = (short) painter.getInset(BOTTOM, this);
            if (boxBottom != getBottomInset()) {
                offsets[captionIndex] = targetSpan + boxBottom;
            } else {
                offsets[captionIndex] = - getTopInset();
            }
        }
    }
    protected View getViewAtPosition(int pos, Rectangle a) {
        int n = getViewCount();
        for (int i = 0; i < n; i++) {
            View v = getView(i);
            int p0 = v.getStartOffset();
            int p1 = v.getEndOffset();
            if ((pos >= p0) && (pos < p1)) {
                if (a != null) {
                    childAllocation(i, a);
                }
                return v;
            }
        }
        if (pos == getEndOffset()) {
            View v = getView(n - 1);
            if (a != null) {
                this.childAllocation(n - 1, a);
            }
            return v;
        }
        return null;
    }
    public AttributeSet getAttributes() {
        if (attr == null) {
            StyleSheet sheet = getStyleSheet();
            attr = sheet.getViewAttributes(this);
        }
        return attr;
    }
    public void paint(Graphics g, Shape allocation) {
        Rectangle a = allocation.getBounds();
        setSize(a.width, a.height);
        if (captionIndex != -1) {
            short top = (short) painter.getInset(TOP, this);
            short bottom = (short) painter.getInset(BOTTOM, this);
            if (top != getTopInset()) {
                int h = getTopInset() - top;
                a.y += h;
                a.height -= h;
            } else {
                a.height -= getBottomInset() - bottom;
            }
        }
        painter.paint(g, a.x, a.y, a.width, a.height, this);
        int n = getViewCount();
        for (int i = 0; i < n; i++) {
            View v = getView(i);
            v.paint(g, getChildAllocation(i, allocation));
        }
    }
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent != null) {
            setPropertiesFromAttributes();
        }
    }
    public ViewFactory getViewFactory() {
        return this;
    }
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, this);
    }
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, this);
    }
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, this);
    }
    protected void forwardUpdate(DocumentEvent.ElementChange ec,
                                 DocumentEvent e, Shape a, ViewFactory f) {
        super.forwardUpdate(ec, e, a, f);
        if (a != null) {
            Component c = getContainer();
            if (c != null) {
                Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a :
                                   a.getBounds();
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }
    }
    public void replace(int offset, int length, View[] views) {
        super.replace(offset, length, views);
        invalidateGrid();
    }
    public View create(Element elem) {
        Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag kind = (HTML.Tag) o;
            if (kind == HTML.Tag.TR) {
                return createTableRow(elem);
            } else if ((kind == HTML.Tag.TD) || (kind == HTML.Tag.TH)) {
                return new CellView(elem);
            } else if (kind == HTML.Tag.CAPTION) {
                return new javax.swing.text.html.ParagraphView(elem);
            }
        }
        View p = getParent();
        if (p != null) {
            ViewFactory f = p.getViewFactory();
            if (f != null) {
                return f.create(elem);
            }
        }
        return null;
    }
    private AttributeSet attr;
    private StyleSheet.BoxPainter painter;
    private int cellSpacing;
    private int borderWidth;
    private int captionIndex;
    private boolean relativeCells;
    private boolean multiRowCells;
    int[] columnSpans;
    int[] columnOffsets;
    SizeRequirements totalColumnRequirements;
    SizeRequirements[] columnRequirements;
    RowIterator rowIterator = new RowIterator();
    ColumnIterator colIterator = new ColumnIterator();
    Vector<RowView> rows;
    boolean skipComments = false;
    boolean gridValid;
    static final private BitSet EMPTY = new BitSet();
    class ColumnIterator implements CSS.LayoutIterator {
        void disablePercentages() {
            percentages = null;
        }
        private void updatePercentagesAndAdjustmentWeights(int span) {
            adjustmentWeights = new int[columnRequirements.length];
            for (int i = 0; i < columnRequirements.length; i++) {
                adjustmentWeights[i] = 0;
            }
            if (relativeCells) {
                percentages = new int[columnRequirements.length];
            } else {
                percentages = null;
            }
            int nrows = getRowCount();
            for (int rowIndex = 0; rowIndex < nrows; rowIndex++) {
                RowView row = getRow(rowIndex);
                int col = 0;
                int ncells = row.getViewCount();
                for (int cell = 0; cell < ncells; cell++, col++) {
                    View cv = row.getView(cell);
                    for (; row.isFilled(col); col++); 
                    int rowSpan = getRowsOccupied(cv);
                    int colSpan = getColumnsOccupied(cv);
                    AttributeSet a = cv.getAttributes();
                    CSS.LengthValue lv = (CSS.LengthValue)
                        a.getAttribute(CSS.Attribute.WIDTH);
                    if ( lv != null ) {
                        int len = (int) (lv.getValue(span) / colSpan + 0.5f);
                        for (int i = 0; i < colSpan; i++) {
                            if (lv.isPercentage()) {
                                percentages[col+i] = Math.max(percentages[col+i], len);
                                adjustmentWeights[col + i] = Math.max(adjustmentWeights[col + i], WorstAdjustmentWeight);
                            } else {
                                adjustmentWeights[col + i] = Math.max(adjustmentWeights[col + i], WorstAdjustmentWeight - 1);
                            }
                        }
                    }
                    col += colSpan - 1;
                }
            }
        }
        public void setLayoutArrays(int offsets[], int spans[], int targetSpan) {
            this.offsets = offsets;
            this.spans = spans;
            updatePercentagesAndAdjustmentWeights(targetSpan);
        }
        public int getCount() {
            return columnRequirements.length;
        }
        public void setIndex(int i) {
            col = i;
        }
        public void setOffset(int offs) {
            offsets[col] = offs;
        }
        public int getOffset() {
            return offsets[col];
        }
        public void setSpan(int span) {
            spans[col] = span;
        }
        public int getSpan() {
            return spans[col];
        }
        public float getMinimumSpan(float parentSpan) {
            return columnRequirements[col].minimum;
        }
        public float getPreferredSpan(float parentSpan) {
            if ((percentages != null) && (percentages[col] != 0)) {
                return Math.max(percentages[col], columnRequirements[col].minimum);
            }
            return columnRequirements[col].preferred;
        }
        public float getMaximumSpan(float parentSpan) {
            return columnRequirements[col].maximum;
        }
        public float getBorderWidth() {
            return borderWidth;
        }
        public float getLeadingCollapseSpan() {
            return cellSpacing;
        }
        public float getTrailingCollapseSpan() {
            return cellSpacing;
        }
        public int getAdjustmentWeight() {
            return adjustmentWeights[col];
        }
        private int col;
        private int[] percentages;
        private int[] adjustmentWeights;
        private int[] offsets;
        private int[] spans;
    }
    class RowIterator implements CSS.LayoutIterator {
        RowIterator() {
        }
        void updateAdjustments() {
            int axis = Y_AXIS;
            if (multiRowCells) {
                int n = getRowCount();
                adjustments = new int[n];
                for (int i = 0; i < n; i++) {
                    RowView rv = getRow(i);
                    if (rv.multiRowCells == true) {
                        int ncells = rv.getViewCount();
                        for (int j = 0; j < ncells; j++) {
                            View v = rv.getView(j);
                            int nrows = getRowsOccupied(v);
                            if (nrows > 1) {
                                int spanNeeded = (int) v.getPreferredSpan(axis);
                                adjustMultiRowSpan(spanNeeded, nrows, i);
                            }
                        }
                    }
                }
            } else {
                adjustments = null;
            }
        }
        void adjustMultiRowSpan(int spanNeeded, int nrows, int rowIndex) {
            if ((rowIndex + nrows) > getCount()) {
                nrows = getCount() - rowIndex;
                if (nrows < 1) {
                    return;
                }
            }
            int span = 0;
            for (int i = 0; i < nrows; i++) {
                RowView rv = getRow(rowIndex + i);
                span += rv.getPreferredSpan(Y_AXIS);
            }
            if (spanNeeded > span) {
                int adjust = (spanNeeded - span);
                int rowAdjust = adjust / nrows;
                int firstAdjust = rowAdjust + (adjust - (rowAdjust * nrows));
                RowView rv = getRow(rowIndex);
                adjustments[rowIndex] = Math.max(adjustments[rowIndex],
                                                 firstAdjust);
                for (int i = 1; i < nrows; i++) {
                    adjustments[rowIndex + i] = Math.max(
                        adjustments[rowIndex + i], rowAdjust);
                }
            }
        }
        void setLayoutArrays(int[] offsets, int[] spans) {
            this.offsets = offsets;
            this.spans = spans;
        }
        public void setOffset(int offs) {
            RowView rv = getRow(row);
            if (rv != null) {
                offsets[rv.viewIndex] = offs;
            }
        }
        public int getOffset() {
            RowView rv = getRow(row);
            if (rv != null) {
                return offsets[rv.viewIndex];
            }
            return 0;
        }
        public void setSpan(int span) {
            RowView rv = getRow(row);
            if (rv != null) {
                spans[rv.viewIndex] = span;
            }
        }
        public int getSpan() {
            RowView rv = getRow(row);
            if (rv != null) {
                return spans[rv.viewIndex];
            }
            return 0;
        }
        public int getCount() {
            return rows.size();
        }
        public void setIndex(int i) {
            row = i;
        }
        public float getMinimumSpan(float parentSpan) {
            return getPreferredSpan(parentSpan);
        }
        public float getPreferredSpan(float parentSpan) {
            RowView rv = getRow(row);
            if (rv != null) {
                int adjust = (adjustments != null) ? adjustments[row] : 0;
                return rv.getPreferredSpan(TableView.this.getAxis()) + adjust;
            }
            return 0;
        }
        public float getMaximumSpan(float parentSpan) {
            return getPreferredSpan(parentSpan);
        }
        public float getBorderWidth() {
            return borderWidth;
        }
        public float getLeadingCollapseSpan() {
            return cellSpacing;
        }
        public float getTrailingCollapseSpan() {
            return cellSpacing;
        }
        public int getAdjustmentWeight() {
            return 0;
        }
        private int row;
        private int[] adjustments;
        private int[] offsets;
        private int[] spans;
    }
    public class RowView extends BoxView {
        public RowView(Element elem) {
            super(elem, View.X_AXIS);
            fillColumns = new BitSet();
            RowView.this.setPropertiesFromAttributes();
        }
        void clearFilledColumns() {
            fillColumns.and(EMPTY);
        }
        void fillColumn(int col) {
            fillColumns.set(col);
        }
        boolean isFilled(int col) {
            return fillColumns.get(col);
        }
        int getColumnCount() {
            int nfill = 0;
            int n = fillColumns.size();
            for (int i = 0; i < n; i++) {
                if (fillColumns.get(i)) {
                    nfill ++;
                }
            }
            return getViewCount() + nfill;
        }
        public AttributeSet getAttributes() {
            return attr;
        }
        View findViewAtPoint(int x, int y, Rectangle alloc) {
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                if (getChildAllocation(i, alloc).contains(x, y)) {
                    childAllocation(i, alloc);
                    return getView(i);
                }
            }
            return null;
        }
        protected StyleSheet getStyleSheet() {
            HTMLDocument doc = (HTMLDocument) getDocument();
            return doc.getStyleSheet();
        }
        public void preferenceChanged(View child, boolean width, boolean height) {
            super.preferenceChanged(child, width, height);
            if (TableView.this.multiRowCells && height) {
                for (int i = rowIndex  - 1; i >= 0; i--) {
                    RowView rv = TableView.this.getRow(i);
                    if (rv.multiRowCells) {
                        rv.preferenceChanged(null, false, true);
                        break;
                    }
                }
            }
        }
        protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
            SizeRequirements req = new SizeRequirements();
            req.minimum = totalColumnRequirements.minimum;
            req.maximum = totalColumnRequirements.maximum;
            req.preferred = totalColumnRequirements.preferred;
            req.alignment = 0f;
            return req;
        }
        public float getMinimumSpan(int axis) {
            float value;
            if (axis == View.X_AXIS) {
                value = totalColumnRequirements.minimum + getLeftInset() +
                        getRightInset();
            }
            else {
                value = super.getMinimumSpan(axis);
            }
            return value;
        }
        public float getMaximumSpan(int axis) {
            float value;
            if (axis == View.X_AXIS) {
                value = (float)Integer.MAX_VALUE;
            }
            else {
                value = super.getMaximumSpan(axis);
            }
            return value;
        }
        public float getPreferredSpan(int axis) {
            float value;
            if (axis == View.X_AXIS) {
                value = totalColumnRequirements.preferred + getLeftInset() +
                        getRightInset();
            }
            else {
                value = super.getPreferredSpan(axis);
            }
            return value;
        }
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            super.changedUpdate(e, a, f);
            int pos = e.getOffset();
            if (pos <= getStartOffset() && (pos + e.getLength()) >=
                getEndOffset()) {
                RowView.this.setPropertiesFromAttributes();
            }
        }
        public void paint(Graphics g, Shape allocation) {
            Rectangle a = (Rectangle) allocation;
            painter.paint(g, a.x, a.y, a.width, a.height, this);
            super.paint(g, a);
        }
        public void replace(int offset, int length, View[] views) {
            super.replace(offset, length, views);
            invalidateGrid();
        }
        protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            long min = 0;
            long pref = 0;
            long max = 0;
            multiRowCells = false;
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                if (getRowsOccupied(v) > 1) {
                    multiRowCells = true;
                    max = Math.max((int) v.getMaximumSpan(axis), max);
                } else {
                    min = Math.max((int) v.getMinimumSpan(axis), min);
                    pref = Math.max((int) v.getPreferredSpan(axis), pref);
                    max = Math.max((int) v.getMaximumSpan(axis), max);
                }
            }
            if (r == null) {
                r = new SizeRequirements();
                r.alignment = 0.5f;
            }
            r.preferred = (int) pref;
            r.minimum = (int) min;
            r.maximum = (int) max;
            return r;
        }
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            int col = 0;
            int ncells = getViewCount();
            for (int cell = 0; cell < ncells; cell++) {
                View cv = getView(cell);
                if (skipComments && !(cv instanceof CellView)) {
                    continue;
                }
                for (; isFilled(col); col++); 
                int colSpan = getColumnsOccupied(cv);
                spans[cell] = columnSpans[col];
                offsets[cell] = columnOffsets[col];
                if (colSpan > 1) {
                    int n = columnSpans.length;
                    for (int j = 1; j < colSpan; j++) {
                        if ((col+j) < n) {
                            spans[cell] += columnSpans[col+j];
                            spans[cell] += cellSpacing;
                        }
                    }
                    col += colSpan - 1;
                }
                col++;
            }
        }
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            super.layoutMinorAxis(targetSpan, axis, offsets, spans);
            int col = 0;
            int ncells = getViewCount();
            for (int cell = 0; cell < ncells; cell++, col++) {
                View cv = getView(cell);
                for (; isFilled(col); col++); 
                int colSpan = getColumnsOccupied(cv);
                int rowSpan = getRowsOccupied(cv);
                if (rowSpan > 1) {
                    int row0 = rowIndex;
                    int row1 = Math.min(rowIndex + rowSpan - 1, getRowCount()-1);
                    spans[cell] = getMultiRowSpan(row0, row1);
                }
                if (colSpan > 1) {
                    col += colSpan - 1;
                }
            }
        }
        public int getResizeWeight(int axis) {
            return 1;
        }
        protected View getViewAtPosition(int pos, Rectangle a) {
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                int p0 = v.getStartOffset();
                int p1 = v.getEndOffset();
                if ((pos >= p0) && (pos < p1)) {
                    if (a != null) {
                        childAllocation(i, a);
                    }
                    return v;
                }
            }
            if (pos == getEndOffset()) {
                View v = getView(n - 1);
                if (a != null) {
                    this.childAllocation(n - 1, a);
                }
                return v;
            }
            return null;
        }
        void setPropertiesFromAttributes() {
            StyleSheet sheet = getStyleSheet();
            attr = sheet.getViewAttributes(this);
            painter = sheet.getBoxPainter(attr);
        }
        private StyleSheet.BoxPainter painter;
        private AttributeSet attr;
        BitSet fillColumns;
        int rowIndex;
        int viewIndex;
        boolean multiRowCells;
    }
    class CellView extends BlockView {
        public CellView(Element elem) {
            super(elem, Y_AXIS);
        }
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            super.layoutMajorAxis(targetSpan, axis, offsets, spans);
            int used = 0;
            int n = spans.length;
            for (int i = 0; i < n; i++) {
                used += spans[i];
            }
            int adjust = 0;
            if (used < targetSpan) {
                String valign = (String) getElement().getAttributes().getAttribute(
                    HTML.Attribute.VALIGN);
                if (valign == null) {
                    AttributeSet rowAttr = getElement().getParentElement().getAttributes();
                    valign = (String) rowAttr.getAttribute(HTML.Attribute.VALIGN);
                }
                if ((valign == null) || valign.equals("middle")) {
                    adjust = (targetSpan - used) / 2;
                } else if (valign.equals("bottom")) {
                    adjust = targetSpan - used;
                }
            }
            if (adjust != 0) {
                for (int i = 0; i < n; i++) {
                    offsets[i] += adjust;
                }
            }
        }
        protected SizeRequirements calculateMajorAxisRequirements(int axis,
                                                                  SizeRequirements r) {
            SizeRequirements req = super.calculateMajorAxisRequirements(axis, r);
            req.maximum = Integer.MAX_VALUE;
            return req;
        }
        @Override
        protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            SizeRequirements rv = super.calculateMinorAxisRequirements(axis, r);
            int n = getViewCount();
            int min = 0;
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                min = Math.max((int) v.getMinimumSpan(axis), min);
            }
            rv.minimum = Math.min(rv.minimum, min);
            return rv;
        }
    }
}
