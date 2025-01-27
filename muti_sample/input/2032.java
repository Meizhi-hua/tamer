public class AndSelector implements Selector {
    private Selector selector1;
    private Selector selector2;
    public AndSelector(Selector s1, Selector s2) {
        this.selector1 = s1;
        this.selector2 = s2;
    }
    public List<Figure> selected(Diagram d) {
        List<Figure> l1 = selector1.selected(d);
        List<Figure> l2 = selector2.selected(d);
        List<Figure> result = new ArrayList<Figure>();
        for (Figure f : l2) {
            if (l1.contains(f)) {
                result.add(f);
            }
        }
        return result;
    }
}
