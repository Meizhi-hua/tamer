class OopMapForCacheEntry extends GenerateOopMap {
  private OopMapCacheEntry entry;
  private int              bci;
  private int              stackTop;
  OopMapForCacheEntry(Method method, int bci, OopMapCacheEntry entry) {
    super(method);
    this.entry = entry;
    this.bci = bci;
    this.stackTop = -1;
  }
  public boolean reportResults() { return false; }
  public boolean possibleGCPoint(BytecodeStream bcs) {
    return false; 
  }
  public void fillStackmapProlog(int nof_gc_points) {
  }
  public void fillStackmapEpilog() {
  }
  public void fillStackmapForOpcodes(BytecodeStream bcs,
                                     CellTypeStateList vars,
                                     CellTypeStateList stack,
                                     int stackTop) {
    if (bcs.bci() == bci) {
      entry.setMask(vars, stack, stackTop);
      this.stackTop = stackTop;
    }
  }
  public void fillInitVars(List initVars) {
  }
  public void computeMap() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!method().isNative(), "cannot compute oop map for native methods");
    }
    if (method().getCodeSize() == 0 || method().getMaxLocals() + method().getMaxStack() == 0) {
      entry.setEmptyMask();
    } else {
      super.computeMap();
      resultForBasicblock(bci);
    }
  }
  public int size() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(stackTop != -1, "computeMap must be called first");
    }
    return (int) ((method().isStatic() ? 0 : 1) + method().getMaxLocals() + stackTop);
  }
}
