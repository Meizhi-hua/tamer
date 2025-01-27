public class PointerFinder {
  public static PointerLocation find(Address a) {
    PointerLocation loc = new PointerLocation(a);
    CollectedHeap heap = VM.getVM().getUniverse().heap();
    if (heap instanceof GenCollectedHeap) {
      GenCollectedHeap genheap = (GenCollectedHeap) heap;
      if (genheap.isIn(a)) {
        for (int i = 0; i < genheap.nGens(); i++) {
          Generation g = genheap.getGen(i);
          if (g.isIn(a)) {
            loc.gen = g;
            break;
          }
        }
        if (loc.gen == null) {
          Generation permGen = genheap.permGen();
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(permGen.isIn(a), "should have been in ordinary or perm gens if it's in the heap");
          }
          loc.permGen = permGen;
        }
        if (VM.getVM().getUseTLAB()) {
          for (JavaThread t = VM.getVM().getThreads().first(); t != null; t = t.next()) {
            ThreadLocalAllocBuffer tlab = t.tlab();
            if (tlab.contains(a)) {
              loc.inTLAB = true;
              loc.tlabThread = t;
              loc.tlab = tlab;
              break;
            }
          }
        }
        return loc;
      }
    } else {
      if (heap.isIn(a)) {
        loc.heap = heap;
        return loc;
      }
    }
    Interpreter interp = VM.getVM().getInterpreter();
    if (interp.contains(a)) {
      loc.inInterpreter = true;
      loc.interpreterCodelet = interp.getCodeletContaining(a);
      return loc;
    }
    if (!VM.getVM().isCore()) {
      CodeCache c = VM.getVM().getCodeCache();
      if (c.contains(a)) {
        loc.inCodeCache = true;
        loc.blob = c.findBlobUnsafe(a);
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(loc.blob != null, "Should have found CodeBlob");
        }
        loc.inBlobCode = loc.blob.codeContains(a);
        loc.inBlobData = loc.blob.dataContains(a);
        if (loc.blob.isNMethod()) {
            NMethod nm = (NMethod) loc.blob;
            loc.inBlobOops = nm.oopsContains(a);
        }
        loc.inBlobUnknownLocation = (!(loc.inBlobCode ||
                                       loc.inBlobData ||
                                       loc.inBlobOops));
        return loc;
      }
    }
    JNIHandles handles = VM.getVM().getJNIHandles();
    JNIHandleBlock handleBlock = handles.globalHandles();
    if (handleBlock != null) {
      handleBlock = handleBlock.blockContainingHandle(a);
    }
    if (handleBlock != null) {
      loc.inStrongGlobalJNIHandleBlock = true;
      loc.handleBlock = handleBlock;
      return loc;
    } else {
      handleBlock = handles.weakGlobalHandles();
      if (handleBlock != null) {
        handleBlock = handleBlock.blockContainingHandle(a);
        if (handleBlock != null) {
          loc.inWeakGlobalJNIHandleBlock = true;
          loc.handleBlock = handleBlock;
          return loc;
        } else {
          for (JavaThread t = VM.getVM().getThreads().first(); t != null; t = t.next()) {
            handleBlock = t.activeHandles();
            if (handleBlock != null) {
              handleBlock = handleBlock.blockContainingHandle(a);
              if (handleBlock != null) {
                loc.inLocalJNIHandleBlock = true;
                loc.handleBlock = handleBlock;
                loc.handleThread = t;
                return loc;
              }
            }
          }
        }
      }
    }
    return loc;
  }
}
