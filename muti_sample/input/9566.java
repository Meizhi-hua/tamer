public class CodeCache {
  private static AddressField       heapField;
  private static AddressField       scavengeRootNMethodsField;
  private static VirtualConstructor virtualConstructor;
  private CodeHeap heap;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("CodeCache");
    heapField = type.getAddressField("_heap");
    scavengeRootNMethodsField = type.getAddressField("_scavenge_root_nmethods");
    virtualConstructor = new VirtualConstructor(db);
    virtualConstructor.addMapping("BufferBlob", BufferBlob.class);
    virtualConstructor.addMapping("nmethod", NMethod.class);
    virtualConstructor.addMapping("RuntimeStub", RuntimeStub.class);
    virtualConstructor.addMapping("RicochetBlob", RicochetBlob.class);
    virtualConstructor.addMapping("AdapterBlob", AdapterBlob.class);
    virtualConstructor.addMapping("SafepointBlob", SafepointBlob.class);
    virtualConstructor.addMapping("DeoptimizationBlob", DeoptimizationBlob.class);
    if (VM.getVM().isServerCompiler()) {
      virtualConstructor.addMapping("ExceptionBlob", ExceptionBlob.class);
      virtualConstructor.addMapping("UncommonTrapBlob", UncommonTrapBlob.class);
    }
  }
  public CodeCache() {
    heap = (CodeHeap) VMObjectFactory.newObject(CodeHeap.class, heapField.getValue());
  }
  public NMethod scavengeRootMethods() {
    return (NMethod) VMObjectFactory.newObject(NMethod.class, scavengeRootNMethodsField.getValue());
  }
  public boolean contains(Address p) {
    return getHeap().contains(p);
  }
  public CodeBlob findBlob(Address start) {
    CodeBlob result = findBlobUnsafe(start);
    if (result == null) return null;
    if (VM.getVM().isDebugging()) {
      return result;
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!(result.isZombie() || result.isLockedByVM()), "unsafe access to zombie method");
    }
    return result;
  }
  public CodeBlob findBlobUnsafe(Address start) {
    CodeBlob result = null;
    try {
      result = (CodeBlob) virtualConstructor.instantiateWrapperFor(getHeap().findStart(start));
    }
    catch (WrongTypeException wte) {
      Address cbAddr = null;
      try {
        cbAddr = getHeap().findStart(start);
      }
      catch (Exception findEx) {
        findEx.printStackTrace();
      }
      String message = "Couldn't deduce type of CodeBlob ";
      if (cbAddr != null) {
        message = message + "@" + cbAddr + " ";
      }
      message = message + "for PC=" + start;
      throw new RuntimeException(message, wte);
    }
    if (result == null) return null;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(result.blobContains(start) || result.blobContains(start.addOffsetTo(8)),
                                                                    "found wrong CodeBlob");
    }
    return result;
  }
  public NMethod findNMethod(Address start) {
    CodeBlob cb = findBlob(start);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(cb == null || cb.isNMethod(), "did not find an nmethod");
    }
    return (NMethod) cb;
  }
  public NMethod findNMethodUnsafe(Address start) {
    CodeBlob cb = findBlobUnsafe(start);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(cb == null || cb.isNMethod(), "did not find an nmethod");
    }
    return (NMethod) cb;
  }
  public CodeBlob createCodeBlobWrapper(Address codeBlobAddr) {
    try {
      return (CodeBlob) virtualConstructor.instantiateWrapperFor(codeBlobAddr);
    }
    catch (Exception e) {
      String message = "Unable to deduce type of CodeBlob from address " + codeBlobAddr +
                       " (expected type nmethod, RuntimeStub, ";
      if (VM.getVM().isClientCompiler()) {
        message = message + " or ";
      }
      message = message + "SafepointBlob";
      if (VM.getVM().isServerCompiler()) {
        message = message + ", DeoptimizationBlob, or ExceptionBlob";
      }
      message = message + ")";
      throw new RuntimeException(message);
    }
  }
  public void iterate(CodeCacheVisitor visitor) {
    CodeHeap heap = getHeap();
    Address ptr = heap.begin();
    Address end = heap.end();
    visitor.prologue(ptr, end);
    CodeBlob lastBlob = null;
    while (ptr != null && ptr.lessThan(end)) {
      try {
        CodeBlob blob = findBlobUnsafe(heap.findStart(ptr));
        if (blob != null) {
          visitor.visit(blob);
          if (blob == lastBlob) {
            throw new InternalError("saw same blob twice");
          }
          lastBlob = blob;
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
      Address next = heap.nextBlock(ptr);
      if (next != null && next.lessThan(ptr)) {
        throw new InternalError("pointer moved backwards");
      }
      ptr = next;
    }
    visitor.epilogue();
  }
  private CodeHeap getHeap() {
    return heap;
  }
}
