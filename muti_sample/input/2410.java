public class SuccessorSelector implements Selector {
    private Selector innerSelector;
    public SuccessorSelector(Selector innerSelector) {
        this.innerSelector = innerSelector;
    }
    public List<Figure> selected(Diagram d) {
        List<Figure> inner = innerSelector.selected(d);
        List<Figure> result = new ArrayList<Figure>();
        for (Figure f : d.getFigures()) {
            boolean saved = false;
            for (Figure f2 : f.getPredecessors()) {
                if (inner.contains(f2)) {
                    saved = true;
                }
            }
            if (saved) {
                result.add(f);
            }
        }
        return result;
    }
}
