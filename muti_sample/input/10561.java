public class CellTypeStateList {
  public CellTypeStateList(int size) {
    list = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      list.add(i, CellTypeState.makeBottom());
    }
  }
  public int size() {
    return list.size();
  }
  public CellTypeState get(int i) {
    return (CellTypeState) list.get(i);
  }
  public CellTypeStateList subList(int fromIndex, int toIndex) {
    return new CellTypeStateList(list.subList(fromIndex, toIndex));
  }
  private List list;
  private CellTypeStateList(List list) {
    this.list = list;
  }
}
