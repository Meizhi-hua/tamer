public class IntervalNode extends RBNode {
  private Interval   interval;
  private Comparator endpointComparator;
  private Object     minEndpoint;
  private Object     maxEndpoint;
  public IntervalNode(Interval interval, Comparator endpointComparator, Object data) {
    super(data);
    this.interval = interval;
    this.endpointComparator = endpointComparator;
  }
  public void copyFrom(RBNode arg) {
    IntervalNode argNode = (IntervalNode) arg;
    this.interval = argNode.interval;
  }
  public Interval getInterval() {
    return interval;
  }
  public Object getMinEndpoint() {
    return minEndpoint;
  }
  public Object getMaxEndpoint() {
    return maxEndpoint;
  }
  public boolean update() {
    Object newMaxEndpoint = computeMaxEndpoint();
    Object newMinEndpoint = computeMinEndpoint();
    if ((maxEndpoint != newMaxEndpoint) || (minEndpoint != newMinEndpoint)) {
      maxEndpoint = newMaxEndpoint;
      minEndpoint = newMinEndpoint;
      return true;
    }
    return false;
  }
  public Object computeMinEndpoint() {
    IntervalNode left = (IntervalNode) getLeft();
    if (left != null) {
      return left.getMinEndpoint();
    }
    return interval.getLowEndpoint();
  }
  public Object computeMaxEndpoint() {
    Object curMax = interval.getHighEndpoint();
    if (getLeft() != null) {
      IntervalNode left = (IntervalNode) getLeft();
      if (endpointComparator.compare(left.getMaxEndpoint(), curMax) > 0) {
        curMax = left.getMaxEndpoint();
      }
    }
    if (getRight() != null) {
      IntervalNode right = (IntervalNode) getRight();
      if (endpointComparator.compare(right.getMaxEndpoint(), curMax) > 0) {
        curMax = right.getMaxEndpoint();
      }
    }
    return curMax;
  }
  public String toString() {
    String res = interval.toString();
    Object d = getData();
    if (d != null) {
      res += " " + d;
    }
    return res;
  }
}
