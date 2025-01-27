public class CollectedHeapName {
  private String name;
  private CollectedHeapName(String name) { this.name = name; }
  public static final CollectedHeapName ABSTRACT = new CollectedHeapName("abstract");
  public static final CollectedHeapName SHARED_HEAP = new CollectedHeapName("SharedHeap");
  public static final CollectedHeapName GEN_COLLECTED_HEAP = new CollectedHeapName("GenCollectedHeap");
  public static final CollectedHeapName PARALLEL_SCAVENGE_HEAP = new CollectedHeapName("ParallelScavengeHeap");
  public String toString() {
    return name;
  }
}
