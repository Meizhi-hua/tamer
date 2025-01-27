public class InputSlot extends Slot {
    protected InputSlot(Figure figure, int wantedIndex) {
        super(figure, wantedIndex);
    }
    public int getPosition() {
        return getFigure().getInputSlots().indexOf(this);
    }
    public void setPosition(int position) {
        List<InputSlot> inputSlots = getFigure().inputSlots;
        InputSlot s = inputSlots.remove(position);
        inputSlots.add(position, s);
    }
    public Point getRelativePosition() {
        return new Point(getFigure().getWidth() * (getPosition() + 1) / (getFigure().getInputSlots().size() + 1), Figure.SLOT_WIDTH - Figure.SLOT_START);
    }
    @Override
    public String toString() {
        return "InputSlot[figure=" + this.getFigure().toString() + ", position=" + getPosition() + "]";
    }
}
