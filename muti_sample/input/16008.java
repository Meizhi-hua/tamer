public class RowClosure implements Closure {
    private MonitoredVm vm;
    private StringBuilder row = new StringBuilder();
    public RowClosure(MonitoredVm vm) {
        this.vm = vm;
    }
    public void visit(Object o, boolean hasNext) throws MonitorException {
        if (! (o instanceof ColumnFormat)) {
            return;
        }
        ColumnFormat c = (ColumnFormat)o;
        String s = null;
        Expression e = c.getExpression();
        ExpressionEvaluator ee = new ExpressionExecuter(vm);
        Object value = ee.evaluate(e);
        if (value instanceof String) {
            s = (String)value;
        } else if (value instanceof Number) {
            double d = ((Number)value).doubleValue();
            double scaledValue = c.getScale().scale(d);
            DecimalFormat df = new DecimalFormat(c.getFormat());
            s = df.format(scaledValue);
        }
        c.setPreviousValue(value);
        s = c.getAlignment().align(s, c.getWidth());
        row.append(s);
        if (hasNext) {
            row.append(" ");
        }
    }
    public String getRow() {
        return row.toString();
    }
}
