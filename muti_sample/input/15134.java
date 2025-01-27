public class RangeSliderModel implements ChangedEventProvider<RangeSliderModel> {
    private ChangedEvent<RangeSliderModel> changedEvent;
    private ChangedEvent<RangeSliderModel> colorChangedEvent;
    private List<String> positions;
    private int firstPosition;
    private int secondPosition;
    private List<Color> colors;
    public void setData(RangeSliderModel model) {
        boolean changed = false;
        changed |= (positions != model.positions);
        positions = model.positions;
        changed |= (firstPosition != model.firstPosition);
        firstPosition = model.firstPosition;
        changed |= (secondPosition != model.secondPosition);
        secondPosition = model.secondPosition;
        boolean colorChanged = (colors != model.colors);
        colors = model.colors;
        if (changed) {
            changedEvent.fire();
        }
        if (colorChanged) {
            colorChangedEvent.fire();
        }
    }
    public RangeSliderModel(List<String> positions) {
        assert positions.size() > 0;
        this.changedEvent = new ChangedEvent<RangeSliderModel>(this);
        this.colorChangedEvent = new ChangedEvent<RangeSliderModel>(this);
        setPositions(positions);
    }
    protected void setPositions(List<String> positions) {
        this.positions = positions;
        colors = new ArrayList<Color>();
        for (int i = 0; i < positions.size(); i++) {
            colors.add(Color.black);
        }
        changedEvent.fire();
        colorChangedEvent.fire();
    }
    public void setColors(List<Color> colors) {
        this.colors = colors;
        colorChangedEvent.fire();
    }
    public List<Color> getColors() {
        return colors;
    }
    public RangeSliderModel copy() {
        RangeSliderModel newModel = new RangeSliderModel(positions);
        newModel.firstPosition = firstPosition;
        newModel.secondPosition = secondPosition;
        newModel.colors = colors;
        return newModel;
    }
    public List<String> getPositions() {
        return Collections.unmodifiableList(positions);
    }
    public int getFirstPosition() {
        return firstPosition;
    }
    public int getSecondPosition() {
        return secondPosition;
    }
    public void setPositions(int fp, int sp) {
        assert fp >= 0 && fp < positions.size();
        assert sp >= 0 && sp < positions.size();
        firstPosition = fp;
        secondPosition = sp;
        ensureOrder();
        changedEvent.fire();
    }
    private void ensureOrder() {
        if (secondPosition < firstPosition) {
            int tmp = secondPosition;
            secondPosition = firstPosition;
            firstPosition = tmp;
        }
    }
    public ChangedEvent<RangeSliderModel> getColorChangedEvent() {
        return colorChangedEvent;
    }
    public ChangedEvent<RangeSliderModel> getChangedEvent() {
        return changedEvent;
    }
}
