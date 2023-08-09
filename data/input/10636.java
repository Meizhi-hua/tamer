public class GenerateOopMap {
  interface JumpClosure {
    public void process(GenerateOopMap c, int bcpDelta, int[] data);
  }
  private static final boolean DEBUG = false;
  private static final int MAXARGSIZE     =   256;      
  private static final int MAX_LOCAL_VARS = 65536;      
  private static final boolean TraceMonitorMismatch = true;
  private static final boolean TraceOopMapRewrites = true;
  static CellTypeState[] epsilonCTS = { CellTypeState.bottom };
  static CellTypeState   refCTS     = CellTypeState.ref;
  static CellTypeState   valCTS     = CellTypeState.value;
  static CellTypeState[]    vCTS    = { CellTypeState.value, CellTypeState.bottom };
  static CellTypeState[]    rCTS    = { CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[]   rrCTS    = { CellTypeState.ref,   CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[]   vrCTS    = { CellTypeState.value, CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[]   vvCTS    = { CellTypeState.value, CellTypeState.value, CellTypeState.bottom };
  static CellTypeState[]  rvrCTS    = { CellTypeState.ref,   CellTypeState.value, CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[]  vvrCTS    = { CellTypeState.value, CellTypeState.value, CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[]  vvvCTS    = { CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.bottom };
  static CellTypeState[] vvvrCTS    = { CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.ref,   CellTypeState.bottom };
  static CellTypeState[] vvvvCTS    = { CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.bottom };
  static class ComputeCallStack extends SignatureIterator {
    CellTypeStateList _effect;
    int _idx;
    void set(CellTypeState state)         { _effect.get(_idx++).set(state); }
    int  length()                         { return _idx; };
    public void doBool  ()              { set(CellTypeState.value); }
    public void doChar  ()              { set(CellTypeState.value); }
    public void doFloat ()              { set(CellTypeState.value); }
    public void doByte  ()              { set(CellTypeState.value); }
    public void doShort ()              { set(CellTypeState.value); }
    public void doInt   ()              { set(CellTypeState.value); }
    public void doVoid  ()              { set(CellTypeState.bottom);}
    public void doObject(int begin, int end) { set(CellTypeState.ref); }
    public void doArray (int begin, int end) { set(CellTypeState.ref); }
    public void doDouble()              { set(CellTypeState.value);
                                          set(CellTypeState.value); }
    public void doLong  ()              { set(CellTypeState.value);
                                          set(CellTypeState.value); }
    ComputeCallStack(Symbol signature) {
      super(signature);
    }
    int computeForParameters(boolean is_static, CellTypeStateList effect) {
      _idx    = 0;
      _effect = effect;
      if (!is_static) {
        effect.get(_idx++).set(CellTypeState.ref);
      }
      iterateParameters();
      return length();
    };
    int computeForReturntype(CellTypeStateList effect) {
      _idx    = 0;
      _effect = effect;
      iterateReturntype();
      set(CellTypeState.bottom);  
      return length();
    }
  }
  static class ComputeEntryStack extends SignatureIterator {
    CellTypeStateList _effect;
    int _idx;
    void set(CellTypeState state)         { _effect.get(_idx++).set(state); }
    int  length()                         { return _idx; };
    public void doBool  ()              { set(CellTypeState.value); }
    public void doChar  ()              { set(CellTypeState.value); }
    public void doFloat ()              { set(CellTypeState.value); }
    public void doByte  ()              { set(CellTypeState.value); }
    public void doShort ()              { set(CellTypeState.value); }
    public void doInt   ()              { set(CellTypeState.value); }
    public void doVoid  ()              { set(CellTypeState.bottom);}
    public void doObject(int begin, int end) { set(CellTypeState.makeSlotRef(_idx)); }
    public void doArray (int begin, int end) { set(CellTypeState.makeSlotRef(_idx)); }
    public void doDouble()              { set(CellTypeState.value);
                                          set(CellTypeState.value); }
    public void doLong  ()              { set(CellTypeState.value);
                                          set(CellTypeState.value); }
    ComputeEntryStack(Symbol signature) {
      super(signature);
    }
    int computeForParameters(boolean is_static, CellTypeStateList effect) {
      _idx    = 0;
      _effect = effect;
      if (!is_static) {
        effect.get(_idx++).set(CellTypeState.makeSlotRef(0));
      }
      iterateParameters();
      return length();
    };
    int computeForReturntype(CellTypeStateList effect) {
      _idx    = 0;
      _effect = effect;
      iterateReturntype();
      set(CellTypeState.bottom);  
      return length();
    }
  }
  static class RetTableEntry {
    private static int _init_nof_jsrs; 
    private int _target_bci;           
    private List _jsrs;       
    private RetTableEntry _next;       
    RetTableEntry(int target, RetTableEntry next) {
      _target_bci = target;
      _jsrs = new ArrayList(_init_nof_jsrs);
      _next = next;
    }
    int targetBci()  { return _target_bci; }
    int nofJsrs()    { return _jsrs.size(); }
    int jsrs(int i)  { return ((Integer) _jsrs.get(i)).intValue(); }
    void addJsr  (int return_bci)     { _jsrs.add(new Integer(return_bci)); }
    void addDelta(int bci, int delta) {
      if (_target_bci > bci) {
        _target_bci += delta;
      }
      for (int k = 0; k < nofJsrs(); k++) {
        int jsr = jsrs(k);
        if (jsr > bci) {
          _jsrs.set(k, new Integer(jsr+delta));
        }
      }
    }
    RetTableEntry next()               { return _next; }
  }
  static class RetTable {
    private RetTableEntry _first;
    private static int _init_nof_entries;
    private void addJsr(int return_bci, int target_bci) {
      RetTableEntry entry = _first;
      for (;(entry != null) && (entry.targetBci() != target_bci); entry = entry.next());
      if (entry == null) {
        entry = new RetTableEntry(target_bci, _first);
        _first = entry;
      }
      entry.addJsr(return_bci);
    }
    RetTable() {}
    void computeRetTable(Method method) {
      BytecodeStream i = new BytecodeStream(method);
      int bytecode;
      while( (bytecode = i.next()) >= 0) {
        switch (bytecode) {
        case Bytecodes._jsr:
          addJsr(i.nextBCI(), i.dest());
          break;
        case Bytecodes._jsr_w:
          addJsr(i.nextBCI(), i.dest_w());
          break;
        }
      }
    }
    void updateRetTable(int bci, int delta) {
      RetTableEntry cur = _first;
      while(cur != null) {
        cur.addDelta(bci, delta);
        cur = cur.next();
      }
    }
    RetTableEntry findJsrsForTarget(int targBci) {
      RetTableEntry cur = _first;
      while(cur != null) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(cur.targetBci() != -1, "sanity check");
        }
        if (cur.targetBci() == targBci) {
          return cur;
        }
        cur = cur.next();
      }
      throw new RuntimeException("Should not reach here");
    }
  }
  static class BasicBlock {
    private boolean _changed;              
    static final int _dead_basic_block = -2;
    static final int _unreached        = -1;
    int                     _bci;          
    int                     _end_bci;      
    int                     _max_locals;   
    int                     _max_stack;    
    CellTypeStateList       _state;        
    int                     _stack_top;    
    int                     _monitor_top;  
    CellTypeStateList       vars()  { return _state; }
    CellTypeStateList       stack() { return _state.subList(_max_locals, _state.size()); }
    boolean changed()               { return _changed; }
    void    setChanged(boolean s)   { _changed = s; }
    boolean isReachable()           { return _stack_top >= 0; }
    boolean isDead()                { return _stack_top == _dead_basic_block; }
    boolean isAlive()               { return _stack_top != _dead_basic_block; }
    void    markAsAlive()           {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(isDead(), "must be dead");
        _stack_top = _unreached;
      }
    }
  }
  protected static final int bad_monitors = -1;
  Method   _method;         
  RetTable _rt;             
  int      _max_locals;     
  int      _max_stack;      
  int      _max_monitors;   
  boolean  _has_exceptions; 
  boolean  _got_error;      
  String   _error_msg;      
  boolean  _monitor_safe;   
  int               _state_len;     
  CellTypeStateList _state;         
  char[]            _state_vec_buf; 
  int               _stack_top;
  int               _monitor_top;
  int _report_for_exit_bci;
  int _matching_enter_bci;
  void            initState() {
    _state_len = _max_locals + _max_stack + _max_monitors;
    _state     = new CellTypeStateList(_state_len);
    _state_vec_buf = new char[Math.max(_max_locals, Math.max(_max_stack, Math.max(_max_monitors, 1)))];
  }
  void            makeContextUninitialized () {
    CellTypeStateList vs = vars();
    for (int i = 0; i < _max_locals; i++)
      vs.get(i).set(CellTypeState.uninit);
    _stack_top = 0;
    _monitor_top = 0;
  }
  int             methodsigToEffect          (Symbol signature, boolean isStatic, CellTypeStateList effect) {
    ComputeEntryStack ces = new ComputeEntryStack(signature);
    return ces.computeForParameters(isStatic, effect);
  }
  boolean         mergeStateVectors          (CellTypeStateList cts, CellTypeStateList bbts) {
    int i;
    int len = _max_locals + _stack_top;
    boolean change = false;
    for (i = len - 1; i >= 0; i--) {
      CellTypeState v = cts.get(i).merge(bbts.get(i), i);
      change = change || !v.equal(bbts.get(i));
      bbts.get(i).set(v);
    }
    if (_max_monitors > 0 && _monitor_top != bad_monitors) {
      int base = _max_locals + _max_stack;
      len = base + _monitor_top;
      for (i = len - 1; i >= base; i--) {
        CellTypeState v = cts.get(i).merge(bbts.get(i), i);
        change = change || !v.equal(bbts.get(i));
        bbts.get(i).set(v);
      }
    }
    return change;
  }
  void            copyState                  (CellTypeStateList dst, CellTypeStateList src) {
    int len = _max_locals + _stack_top;
    for (int i = 0; i < len; i++) {
      if (src.get(i).isNonlockReference()) {
        dst.get(i).set(CellTypeState.makeSlotRef(i));
      } else {
        dst.get(i).set(src.get(i));
      }
    }
    if (_max_monitors > 0 && _monitor_top != bad_monitors) {
      int base = _max_locals + _max_stack;
      len = base + _monitor_top;
      for (int i = base; i < len; i++) {
        dst.get(i).set(src.get(i));
      }
    }
  }
  void            mergeStateIntoBB           (BasicBlock bb) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bb.isAlive(), "merging state into a dead basicblock");
    }
    if (_stack_top == bb._stack_top) {
      if (_monitor_top == bb._monitor_top) {
        if (mergeStateVectors(_state, bb._state)) {
          bb.setChanged(true);
        }
      } else {
        if (TraceMonitorMismatch) {
          reportMonitorMismatch("monitor stack height merge conflict");
        }
        bb._monitor_top = bad_monitors;
        bb.setChanged(true);
        _monitor_safe = false;
      }
    } else if (!bb.isReachable()) {
      copyState(bb._state, _state);
      bb._stack_top = _stack_top;
      bb._monitor_top = _monitor_top;
      bb.setChanged(true);
    } else {
      throw new RuntimeException("stack height conflict: " +
                                 _stack_top + " vs. " + bb._stack_top);
    }
  }
  void            mergeState                 (int bci, int[] data) {
    mergeStateIntoBB(getBasicBlockAt(bci));
  }
  void            setVar                     (int localNo, CellTypeState cts) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(cts.isReference() || cts.isValue() || cts.isAddress(),
                  "wrong celltypestate");
    }
    if (localNo < 0 || localNo > _max_locals) {
      throw new RuntimeException("variable write error: r" + localNo);
    }
    vars().get(localNo).set(cts);
  }
  CellTypeState   getVar                     (int localNo) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(localNo < _max_locals + _nof_refval_conflicts, "variable read error");
    }
    if (localNo < 0 || localNo > _max_locals) {
      throw new RuntimeException("variable read error: r" + localNo);
    }
    return vars().get(localNo).copy();
  }
  CellTypeState   pop                        () {
    if ( _stack_top <= 0) {
      throw new RuntimeException("stack underflow");
    }
    return  stack().get(--_stack_top).copy();
  }
  void            push                       (CellTypeState cts) {
    if ( _stack_top >= _max_stack) {
      if (DEBUG) {
        System.err.println("Method: " + method().getName().asString() + method().getSignature().asString() +
                           " _stack_top: " + _stack_top + " _max_stack: " + _max_stack);
      }
      throw new RuntimeException("stack overflow");
    }
    stack().get(_stack_top++).set(cts);
    if (DEBUG) {
      System.err.println("After push: _stack_top: " + _stack_top +
                         " _max_stack: " + _max_stack +
                         " just pushed: " + cts.toChar());
    }
  }
  CellTypeState   monitorPop                 () {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(_monitor_top != bad_monitors, "monitorPop called on error monitor stack");
    }
    if (_monitor_top == 0) {
      _monitor_safe = false;
      _monitor_top = bad_monitors;
      if (TraceMonitorMismatch) {
        reportMonitorMismatch("monitor stack underflow");
      }
      return CellTypeState.ref; 
    }
    return  monitors().get(--_monitor_top).copy();
  }
  void            monitorPush                (CellTypeState cts) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(_monitor_top != bad_monitors, "monitorPush called on error monitor stack");
    }
    if (_monitor_top >= _max_monitors) {
      _monitor_safe = false;
      _monitor_top = bad_monitors;
      if (TraceMonitorMismatch) {
        reportMonitorMismatch("monitor stack overflow");
      }
      return;
    }
    monitors().get(_monitor_top++).set(cts);
  }
  CellTypeStateList vars    ()     { return _state; }
  CellTypeStateList stack   ()     { return _state.subList(_max_locals, _state.size()); }
  CellTypeStateList monitors()     { return _state.subList(_max_locals+_max_stack, _state.size()); }
  void            replaceAllCTSMatches       (CellTypeState match,
                                              CellTypeState replace) {
    int i;
    int len = _max_locals + _stack_top;
    boolean change = false;
    for (i = len - 1; i >= 0; i--) {
      if (match.equal(_state.get(i))) {
        _state.get(i).set(replace);
      }
    }
    if (_monitor_top > 0) {
      int base = _max_locals + _max_stack;
      len = base + _monitor_top;
      for (i = len - 1; i >= base; i--) {
        if (match.equal(_state.get(i))) {
          _state.get(i).set(replace);
        }
      }
    }
  }
  void            printStates                (PrintStream tty, CellTypeStateList vector, int num) {
    for (int i = 0; i < num; i++) {
      vector.get(i).print(tty);
    }
  }
  void            printCurrentState          (PrintStream tty,
                                              BytecodeStream currentBC,
                                              boolean        detailed) {
    if (detailed) {
      tty.print("     " + currentBC.bci() + " vars     = ");
      printStates(tty, vars(), _max_locals);
      tty.print("    " + Bytecodes.name(currentBC.code()));
      switch(currentBC.code()) {
      case Bytecodes._invokevirtual:
      case Bytecodes._invokespecial:
      case Bytecodes._invokestatic:
      case Bytecodes._invokeinterface:
      case Bytecodes._invokedynamic:
        int idx = currentBC.getIndexBig();
        tty.print(" idx " + idx);
      }
      tty.println();
      tty.print("          stack    = ");
      printStates(tty, stack(), _stack_top);
      tty.println();
      if (_monitor_top != bad_monitors) {
        tty.print("          monitors = ");
        printStates(tty, monitors(), _monitor_top);
      } else {
        tty.print("          [bad monitor stack]");
      }
      tty.println();
    } else {
      tty.print("    " + currentBC.bci() + "  vars = '" +
                stateVecToString(vars(), _max_locals) + "' ");
      tty.print("     stack = '" + stateVecToString(stack(), _stack_top) + "' ");
      if (_monitor_top != bad_monitors) {
        tty.print("  monitors = '" + stateVecToString(monitors(), _monitor_top) + "'  \t" +
                  Bytecodes.name(currentBC.code()));
      } else {
        tty.print("  [bad monitor stack]");
      }
      switch(currentBC.code()) {
      case Bytecodes._invokevirtual:
      case Bytecodes._invokespecial:
      case Bytecodes._invokestatic:
      case Bytecodes._invokeinterface:
      case Bytecodes._invokedynamic:
        int idx = currentBC.getIndexBig();
        tty.print(" idx " + idx);
      }
      tty.println();
    }
  }
  void            reportMonitorMismatch      (String msg) {
    if (Assert.ASSERTS_ENABLED) {
      System.err.print("    Monitor mismatch in method ");
      method().printValueOn(System.err);
      System.err.println(": " + msg);
    }
  }
  BasicBlock[]    _basic_blocks;             
  int             _gc_points;
  int             _bb_count;
  BitMap          _bb_hdr_bits;
  void          initializeBB               () {
    _gc_points = 0;
    _bb_count  = 0;
    _bb_hdr_bits = new BitMap((int) _method.getCodeSize());
  }
  void          markBBHeadersAndCountGCPoints() {
    initializeBB();
    boolean fellThrough = false;  
    TypeArray excps = method().getExceptionTable();
    for(int i = 0; i < excps.getLength(); i += 4) {
      int handler_pc_idx = i+2;
      markBB(excps.getIntAt(handler_pc_idx), null);
    }
    BytecodeStream bcs = new BytecodeStream(_method);
    int bytecode;
    while( (bytecode = bcs.next()) >= 0) {
      int bci = bcs.bci();
      if (!fellThrough)
        markBB(bci, null);
      fellThrough = jumpTargetsDo(bcs,
                                  new JumpClosure() {
                                      public void process(GenerateOopMap c, int bcpDelta, int[] data) {
                                        c.markBB(bcpDelta, data);
                                      }
                                    },
                                  null);
      switch (bytecode) {
      case Bytecodes._jsr:
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(!fellThrough, "should not happen");
        }
        markBB(bci + Bytecodes.lengthFor(bytecode), null);
        break;
      case Bytecodes._jsr_w:
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(!fellThrough, "should not happen");
        }
        markBB(bci + Bytecodes.lengthFor(bytecode), null);
        break;
      }
      if (possibleGCPoint(bcs))
        _gc_points++;
    }
  }
  boolean       isBBHeader                  (int bci) {
    return _bb_hdr_bits.at(bci);
  }
  int           gcPoints                    () {
    return _gc_points;
  }
  int           bbCount                     () {
    return _bb_count;
  }
  void          setBBMarkBit                (int bci) {
    _bb_hdr_bits.atPut(bci, true);
  }
  void          clear_bbmark_bit            (int bci) {
    _bb_hdr_bits.atPut(bci, false);
  }
  BasicBlock    getBasicBlockAt             (int bci) {
    BasicBlock bb = getBasicBlockContaining(bci);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bb._bci == bci, "should have found BB");
    }
    return bb;
  }
  BasicBlock    getBasicBlockContaining     (int bci) {
    BasicBlock[] bbs = _basic_blocks;
    int lo = 0, hi = _bb_count - 1;
    while (lo <= hi) {
      int m = (lo + hi) / 2;
      int mbci = bbs[m]._bci;
      int nbci;
      if ( m == _bb_count-1) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that( bci >= mbci && bci < method().getCodeSize(), "sanity check failed");
        }
        return bbs[m];
      } else {
        nbci = bbs[m+1]._bci;
      }
      if ( mbci <= bci && bci < nbci) {
        return bbs[m];
      } else if (mbci < bci) {
        lo = m + 1;
      } else {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(mbci > bci, "sanity check");
        }
        hi = m - 1;
      }
    }
    throw new RuntimeException("should have found BB");
  }
  void          interpBB                    (BasicBlock bb) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bb.isReachable(), "should be reachable or deadcode exist");
    }
    restoreState(bb);
    BytecodeStream itr = new BytecodeStream(_method);
    int lim_bci = nextBBStartPC(bb);
    itr.setInterval(bb._bci, lim_bci);
    if (DEBUG) {
      System.err.println("interpBB: method = " + method().getName().asString() +
                         method().getSignature().asString() +
                         ", BCI interval [" + bb._bci + ", " + lim_bci + ")");
      {
        System.err.print("Bytecodes:");
        for (int i = bb._bci; i < lim_bci; i++) {
          System.err.print(" 0x" + Long.toHexString(method().getBytecodeOrBPAt(i)));
        }
        System.err.println();
      }
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(lim_bci != bb._bci, "must be at least one instruction in a basicblock");
    }
    itr.next(); 
    while(itr.nextBCI() < lim_bci && !_got_error) {
      if (_has_exceptions || (_monitor_top != 0)) {
        doExceptionEdge(itr);
      }
      interp1(itr);
      itr.next();
    }
    if (!_got_error) {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(itr.nextBCI() == lim_bci, "must point to end");
      }
      if (_has_exceptions || (_monitor_top != 0)) {
        doExceptionEdge(itr);
      }
      interp1(itr);
      boolean fall_through = jumpTargetsDo(itr, new JumpClosure() {
          public void process(GenerateOopMap c, int bcpDelta, int[] data) {
            c.mergeState(bcpDelta, data);
          }
        }, null);
      if (_got_error)  return;
      if (itr.code() == Bytecodes._ret) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(!fall_through, "cannot be set if ret instruction");
        }
        retJumpTargetsDo(itr, new JumpClosure() {
            public void process(GenerateOopMap c, int bcpDelta, int[] data) {
              c.mergeState(bcpDelta, data);
            }
          }, itr.getIndex(), null);
      } else if (fall_through) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(lim_bci == _basic_blocks[bbIndex(bb) + 1]._bci, "there must be another bb");
        }
        mergeStateIntoBB(_basic_blocks[bbIndex(bb) + 1]);
      }
    }
  }
  void          restoreState                (BasicBlock bb) {
    for (int i = 0; i < _state_len; i++) {
      _state.get(i).set(bb._state.get(i));
    }
    _stack_top   = bb._stack_top;
    _monitor_top = bb._monitor_top;
  }
  int           nextBBStartPC               (BasicBlock bb) {
    int bbNum = bbIndex(bb) + 1;
    if (bbNum == _bb_count)
      return (int) method().getCodeSize();
    return _basic_blocks[bbNum]._bci;
  }
  void          updateBasicBlocks           (int bci, int delta) {
    BitMap bbBits = new BitMap((int) (_method.getCodeSize() + delta));
    for(int k = 0; k < _bb_count; k++) {
      if (_basic_blocks[k]._bci > bci) {
        _basic_blocks[k]._bci     += delta;
        _basic_blocks[k]._end_bci += delta;
      }
      bbBits.atPut(_basic_blocks[k]._bci, true);
    }
    _bb_hdr_bits = bbBits;
  }
  void markBB(int bci, int[] data) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bci>= 0 && bci < method().getCodeSize(), "index out of bounds");
    }
    if (isBBHeader(bci))
      return;
    setBBMarkBit(bci);
    _bb_count++;
  }
  void          markReachableCode() {
    final int[] change = new int[1];
    change[0] = 1;
    _basic_blocks[0].markAsAlive();
    TypeArray excps = method().getExceptionTable();
    for(int i = 0; i < excps.getLength(); i += 4) {
      int handler_pc_idx = i+2;
      BasicBlock bb = getBasicBlockAt(excps.getIntAt(handler_pc_idx));
      if (bb.isDead())
        bb.markAsAlive();
    }
    BytecodeStream bcs = new BytecodeStream(_method);
    while (change[0] != 0) {
      change[0] = 0;
      for (int i = 0; i < _bb_count; i++) {
        BasicBlock bb = _basic_blocks[i];
        if (bb.isAlive()) {
          bcs.setStart(bb._end_bci);
          bcs.next();
          int bytecode = bcs.code();
          int bci = bcs.bci();
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(bci == bb._end_bci, "wrong bci");
          }
          boolean fell_through = jumpTargetsDo(bcs, new JumpClosure() {
              public void process(GenerateOopMap c, int bciDelta, int[] change) {
                c.reachableBasicblock(bciDelta, change);
              }
            }, change);
          switch (bytecode) {
          case Bytecodes._jsr:
          case Bytecodes._jsr_w:
            if (Assert.ASSERTS_ENABLED) {
              Assert.that(!fell_through, "should not happen");
            }
            reachableBasicblock(bci + Bytecodes.lengthFor(bytecode), change);
            break;
          }
          if (fell_through) {
            if (_basic_blocks[i+1].isDead()) {
              _basic_blocks[i+1].markAsAlive();
              change[0] = 1;
            }
          }
        }
      }
    }
  }
  void  reachableBasicblock        (int bci, int[] data) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bci>= 0 && bci < method().getCodeSize(), "index out of bounds");
    }
    BasicBlock bb = getBasicBlockAt(bci);
    if (bb.isDead()) {
      bb.markAsAlive();
      data[0] = 1; 
    }
  }
  void  doInterpretation                    () {
    int i = 0;
    do {
      _conflict = false;
      _monitor_safe = true;
      if (!_got_error) initBasicBlocks();
      if (!_got_error) setupMethodEntryState();
      if (!_got_error) interpAll();
      if (!_got_error) rewriteRefvalConflicts();
      i++;
    } while (_conflict && !_got_error);
  }
  void  initBasicBlocks                     () {
    _basic_blocks = new BasicBlock[_bb_count];
    for (int i = 0; i < _bb_count; i++) {
      _basic_blocks[i] = new BasicBlock();
    }
    BytecodeStream j = new BytecodeStream(_method);
    int bytecode;
    int bbNo = 0;
    int monitor_count = 0;
    int prev_bci = -1;
    while( (bytecode = j.next()) >= 0) {
      if (j.code() == Bytecodes._monitorenter) {
        monitor_count++;
      }
      int bci = j.bci();
      if (isBBHeader(bci)) {
        BasicBlock bb    = _basic_blocks[bbNo];
        bb._bci          = bci;
        bb._max_locals   = _max_locals;
        bb._max_stack    = _max_stack;
        bb.setChanged(false);
        bb._stack_top    = BasicBlock._dead_basic_block; 
        bb._monitor_top  = bad_monitors;
        if (bbNo > 0) {
          _basic_blocks[bbNo - 1]._end_bci = prev_bci;
        }
        bbNo++;
      }
      prev_bci = bci;
    }
    _basic_blocks[bbNo-1]._end_bci = prev_bci;
    _max_monitors = monitor_count;
    initState();
    CellTypeStateList basicBlockState = new CellTypeStateList(bbNo * _state_len);
    for (int blockNum=0; blockNum < bbNo; blockNum++) {
      BasicBlock bb = _basic_blocks[blockNum];
      bb._state = basicBlockState.subList(blockNum * _state_len, (blockNum + 1) * _state_len);
      if (Assert.ASSERTS_ENABLED) {
        if (blockNum + 1 < bbNo) {
          int bc_len = Bytecodes.javaLengthAt(_method, bb._end_bci);
          Assert.that(bb._end_bci + bc_len == _basic_blocks[blockNum + 1]._bci,
                      "unmatched bci info in basicblock");
        }
      }
    }
    if (Assert.ASSERTS_ENABLED) {
      BasicBlock bb = _basic_blocks[bbNo-1];
      int bc_len = Bytecodes.javaLengthAt(_method, bb._end_bci);
      Assert.that(bb._end_bci + bc_len == _method.getCodeSize(), "wrong end bci");
    }
    if (bbNo !=_bb_count) {
      if (bbNo < _bb_count) {
        throw new RuntimeException("jump into the middle of instruction?");
      } else {
        throw new RuntimeException("extra basic blocks - should not happen?");
      }
    }
    markReachableCode();
  }
  void  setupMethodEntryState               () {
    makeContextUninitialized();
    methodsigToEffect(method().getSignature(), method().isStatic(), vars());
    initializeVars();
    mergeStateIntoBB(_basic_blocks[0]);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(_basic_blocks[0].changed(), "we are not getting off the ground");
    }
  }
  void  interpAll                           () {
    boolean change = true;
    while (change && !_got_error) {
      change = false;
      for (int i = 0; i < _bb_count && !_got_error; i++) {
        BasicBlock bb = _basic_blocks[i];
        if (bb.changed()) {
          if (_got_error) return;
          change = true;
          bb.setChanged(false);
          interpBB(bb);
        }
      }
    }
  }
  void  interp1                             (BytecodeStream itr) {
    if (DEBUG) {
      System.err.println(" - bci " + itr.bci());
    }
    if (_report_result == true) {
      switch(itr.code()) {
      case Bytecodes._invokevirtual:
      case Bytecodes._invokespecial:
      case Bytecodes._invokestatic:
      case Bytecodes._invokeinterface:
      case Bytecodes._invokedynamic:
        _itr_send = itr;
        _report_result_for_send = true;
        break;
      default:
        fillStackmapForOpcodes(itr, vars(), stack(), _stack_top);
        break;
      }
    }
    switch(itr.code()) {
    case Bytecodes._nop:               break;
    case Bytecodes._goto:              break;
    case Bytecodes._goto_w:            break;
    case Bytecodes._iinc:              break;
    case Bytecodes._return:            doReturnMonitorCheck();
      break;
    case Bytecodes._aconst_null:
    case Bytecodes._new:               ppush1(CellTypeState.makeLineRef(itr.bci()));
      break;
    case Bytecodes._iconst_m1:
    case Bytecodes._iconst_0:
    case Bytecodes._iconst_1:
    case Bytecodes._iconst_2:
    case Bytecodes._iconst_3:
    case Bytecodes._iconst_4:
    case Bytecodes._iconst_5:
    case Bytecodes._fconst_0:
    case Bytecodes._fconst_1:
    case Bytecodes._fconst_2:
    case Bytecodes._bipush:
    case Bytecodes._sipush:            ppush1(valCTS);             break;
    case Bytecodes._lconst_0:
    case Bytecodes._lconst_1:
    case Bytecodes._dconst_0:
    case Bytecodes._dconst_1:          ppush(vvCTS);               break;
    case Bytecodes._ldc2_w:            ppush(vvCTS);               break;
    case Bytecodes._ldc:               doLdc(itr.getIndex(), itr.bci());    break;
    case Bytecodes._ldc_w:             doLdc(itr.getIndexBig(), itr.bci());break;
    case Bytecodes._iload:
    case Bytecodes._fload:             ppload(vCTS, itr.getIndex()); break;
    case Bytecodes._lload:
    case Bytecodes._dload:             ppload(vvCTS,itr.getIndex()); break;
    case Bytecodes._aload:             ppload(rCTS, itr.getIndex()); break;
    case Bytecodes._iload_0:
    case Bytecodes._fload_0:           ppload(vCTS, 0);            break;
    case Bytecodes._iload_1:
    case Bytecodes._fload_1:           ppload(vCTS, 1);            break;
    case Bytecodes._iload_2:
    case Bytecodes._fload_2:           ppload(vCTS, 2);            break;
    case Bytecodes._iload_3:
    case Bytecodes._fload_3:           ppload(vCTS, 3);            break;
    case Bytecodes._lload_0:
    case Bytecodes._dload_0:           ppload(vvCTS, 0);           break;
    case Bytecodes._lload_1:
    case Bytecodes._dload_1:           ppload(vvCTS, 1);           break;
    case Bytecodes._lload_2:
    case Bytecodes._dload_2:           ppload(vvCTS, 2);           break;
    case Bytecodes._lload_3:
    case Bytecodes._dload_3:           ppload(vvCTS, 3);           break;
    case Bytecodes._aload_0:           ppload(rCTS, 0);            break;
    case Bytecodes._aload_1:           ppload(rCTS, 1);            break;
    case Bytecodes._aload_2:           ppload(rCTS, 2);            break;
    case Bytecodes._aload_3:           ppload(rCTS, 3);            break;
    case Bytecodes._iaload:
    case Bytecodes._faload:
    case Bytecodes._baload:
    case Bytecodes._caload:
    case Bytecodes._saload:            pp(vrCTS, vCTS); break;
    case Bytecodes._laload:            pp(vrCTS, vvCTS);  break;
    case Bytecodes._daload:            pp(vrCTS, vvCTS); break;
    case Bytecodes._aaload:            ppNewRef(vrCTS, itr.bci()); break;
    case Bytecodes._istore:
    case Bytecodes._fstore:            ppstore(vCTS, itr.getIndex()); break;
    case Bytecodes._lstore:
    case Bytecodes._dstore:            ppstore(vvCTS, itr.getIndex()); break;
    case Bytecodes._astore:            doAstore(itr.getIndex());     break;
    case Bytecodes._istore_0:
    case Bytecodes._fstore_0:          ppstore(vCTS, 0);           break;
    case Bytecodes._istore_1:
    case Bytecodes._fstore_1:          ppstore(vCTS, 1);           break;
    case Bytecodes._istore_2:
    case Bytecodes._fstore_2:          ppstore(vCTS, 2);           break;
    case Bytecodes._istore_3:
    case Bytecodes._fstore_3:          ppstore(vCTS, 3);           break;
    case Bytecodes._lstore_0:
    case Bytecodes._dstore_0:          ppstore(vvCTS, 0);          break;
    case Bytecodes._lstore_1:
    case Bytecodes._dstore_1:          ppstore(vvCTS, 1);          break;
    case Bytecodes._lstore_2:
    case Bytecodes._dstore_2:          ppstore(vvCTS, 2);          break;
    case Bytecodes._lstore_3:
    case Bytecodes._dstore_3:          ppstore(vvCTS, 3);          break;
    case Bytecodes._astore_0:          doAstore(0);                break;
    case Bytecodes._astore_1:          doAstore(1);                break;
    case Bytecodes._astore_2:          doAstore(2);                break;
    case Bytecodes._astore_3:          doAstore(3);                break;
    case Bytecodes._iastore:
    case Bytecodes._fastore:
    case Bytecodes._bastore:
    case Bytecodes._castore:
    case Bytecodes._sastore:           ppop(vvrCTS);               break;
    case Bytecodes._lastore:
    case Bytecodes._dastore:           ppop(vvvrCTS);              break;
    case Bytecodes._aastore:           ppop(rvrCTS);               break;
    case Bytecodes._pop:               ppopAny(1);                 break;
    case Bytecodes._pop2:              ppopAny(2);                 break;
    case Bytecodes._dup:               ppdupswap(1, "11");         break;
    case Bytecodes._dup_x1:            ppdupswap(2, "121");        break;
    case Bytecodes._dup_x2:            ppdupswap(3, "1321");       break;
    case Bytecodes._dup2:              ppdupswap(2, "2121");       break;
    case Bytecodes._dup2_x1:           ppdupswap(3, "21321");      break;
    case Bytecodes._dup2_x2:           ppdupswap(4, "214321");     break;
    case Bytecodes._swap:              ppdupswap(2, "12");         break;
    case Bytecodes._iadd:
    case Bytecodes._fadd:
    case Bytecodes._isub:
    case Bytecodes._fsub:
    case Bytecodes._imul:
    case Bytecodes._fmul:
    case Bytecodes._idiv:
    case Bytecodes._fdiv:
    case Bytecodes._irem:
    case Bytecodes._frem:
    case Bytecodes._ishl:
    case Bytecodes._ishr:
    case Bytecodes._iushr:
    case Bytecodes._iand:
    case Bytecodes._ior:
    case Bytecodes._ixor:
    case Bytecodes._l2f:
    case Bytecodes._l2i:
    case Bytecodes._d2f:
    case Bytecodes._d2i:
    case Bytecodes._fcmpl:
    case Bytecodes._fcmpg:             pp(vvCTS, vCTS); break;
    case Bytecodes._ladd:
    case Bytecodes._dadd:
    case Bytecodes._lsub:
    case Bytecodes._dsub:
    case Bytecodes._lmul:
    case Bytecodes._dmul:
    case Bytecodes._ldiv:
    case Bytecodes._ddiv:
    case Bytecodes._lrem:
    case Bytecodes._drem:
    case Bytecodes._land:
    case Bytecodes._lor:
    case Bytecodes._lxor:              pp(vvvvCTS, vvCTS); break;
    case Bytecodes._ineg:
    case Bytecodes._fneg:
    case Bytecodes._i2f:
    case Bytecodes._f2i:
    case Bytecodes._i2c:
    case Bytecodes._i2s:
    case Bytecodes._i2b:               pp(vCTS, vCTS); break;
    case Bytecodes._lneg:
    case Bytecodes._dneg:
    case Bytecodes._l2d:
    case Bytecodes._d2l:               pp(vvCTS, vvCTS); break;
    case Bytecodes._lshl:
    case Bytecodes._lshr:
    case Bytecodes._lushr:             pp(vvvCTS, vvCTS); break;
    case Bytecodes._i2l:
    case Bytecodes._i2d:
    case Bytecodes._f2l:
    case Bytecodes._f2d:               pp(vCTS, vvCTS); break;
    case Bytecodes._lcmp:              pp(vvvvCTS, vCTS); break;
    case Bytecodes._dcmpl:
    case Bytecodes._dcmpg:             pp(vvvvCTS, vCTS); break;
    case Bytecodes._ifeq:
    case Bytecodes._ifne:
    case Bytecodes._iflt:
    case Bytecodes._ifge:
    case Bytecodes._ifgt:
    case Bytecodes._ifle:
    case Bytecodes._tableswitch:       ppop1(valCTS);
      break;
    case Bytecodes._ireturn:
    case Bytecodes._freturn:           doReturnMonitorCheck();
      ppop1(valCTS);
      break;
    case Bytecodes._if_icmpeq:
    case Bytecodes._if_icmpne:
    case Bytecodes._if_icmplt:
    case Bytecodes._if_icmpge:
    case Bytecodes._if_icmpgt:
    case Bytecodes._if_icmple:         ppop(vvCTS);
      break;
    case Bytecodes._lreturn:           doReturnMonitorCheck();
      ppop(vvCTS);
      break;
    case Bytecodes._dreturn:           doReturnMonitorCheck();
      ppop(vvCTS);
      break;
    case Bytecodes._if_acmpeq:
    case Bytecodes._if_acmpne:         ppop(rrCTS);                 break;
    case Bytecodes._jsr:               doJsr(itr.dest());          break;
    case Bytecodes._jsr_w:             doJsr(itr.dest_w());        break;
    case Bytecodes._getstatic:         doField(true,  true,
                                               itr.getIndexBig(),
                                               itr.bci()); break;
    case Bytecodes._putstatic:         doField(false, true,  itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._getfield:          doField(true,  false, itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._putfield:          doField(false, false, itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._invokevirtual:
    case Bytecodes._invokespecial:     doMethod(false, false, itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._invokestatic:      doMethod(true,  false, itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._invokedynamic:     doMethod(false, true,  itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._invokeinterface:   doMethod(false, true,  itr.getIndexBig(), itr.bci()); break;
    case Bytecodes._newarray:
    case Bytecodes._anewarray:         ppNewRef(vCTS, itr.bci()); break;
    case Bytecodes._checkcast:         doCheckcast(); break;
    case Bytecodes._arraylength:
    case Bytecodes._instanceof:        pp(rCTS, vCTS); break;
    case Bytecodes._monitorenter:      doMonitorenter(itr.bci()); break;
    case Bytecodes._monitorexit:       doMonitorexit(itr.bci()); break;
    case Bytecodes._athrow:            
      if ((!_has_exceptions) && (_monitor_top > 0)) {
        _monitor_safe = false;
      }
      break;
    case Bytecodes._areturn:           doReturnMonitorCheck();
      ppop1(refCTS);
      break;
    case Bytecodes._ifnull:
    case Bytecodes._ifnonnull:         ppop1(refCTS); break;
    case Bytecodes._multianewarray:    doMultianewarray(itr.codeAt(itr.bci() + 3), itr.bci()); break;
    case Bytecodes._wide:              throw new RuntimeException("Iterator should skip this bytecode");
    case Bytecodes._ret:                                           break;
    case Bytecodes._fast_aaccess_0:     ppNewRef(rCTS, itr.bci()); break; 
    case Bytecodes._fast_iaccess_0:     ppush1(valCTS);            break; 
    case Bytecodes._fast_igetfield:     pp(rCTS, vCTS);            break;
    case Bytecodes._fast_agetfield:     ppNewRef(rCTS, itr.bci()); break;
    case Bytecodes._fast_aload_0:       ppload(rCTS,  0);          break;
    case Bytecodes._lookupswitch:
    case Bytecodes._fast_linearswitch:
    case Bytecodes._fast_binaryswitch:  ppop1(valCTS);             break;
    default:
      throw new RuntimeException("unexpected opcode: " + itr.code());
    }
  }
  void  doExceptionEdge                     (BytecodeStream itr) {
    if (!Bytecodes.canTrap(itr.code())) return;
    switch (itr.code()) {
    case Bytecodes._aload_0:
    case Bytecodes._fast_aload_0:
      return;
    case Bytecodes._ireturn:
    case Bytecodes._lreturn:
    case Bytecodes._freturn:
    case Bytecodes._dreturn:
    case Bytecodes._areturn:
    case Bytecodes._return:
      if (_monitor_top == 0) {
        return;
      }
      break;
    case Bytecodes._monitorexit:
      if (_monitor_top != bad_monitors && _monitor_top != 0) {
        return;
      }
      break;
    }
    if (_has_exceptions) {
      int bci = itr.bci();
      TypeArray exct   = method().getExceptionTable();
      for(int i = 0; i< exct.getLength(); i+=4) {
        int start_pc   = exct.getIntAt(i);
        int end_pc     = exct.getIntAt(i+1);
        int handler_pc = exct.getIntAt(i+2);
        int catch_type = exct.getIntAt(i+3);
        if (start_pc <= bci && bci < end_pc) {
          BasicBlock excBB = getBasicBlockAt(handler_pc);
          CellTypeStateList excStk  = excBB.stack();
          CellTypeStateList cOpStck = stack();
          CellTypeState cOpStck_0 = cOpStck.get(0).copy();
          int cOpStackTop = _stack_top;
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(method().getMaxStack() > 0, "sanity check");
          }
          cOpStck.get(0).set(CellTypeState.makeSlotRef(_max_locals));
          _stack_top = 1;
          mergeStateIntoBB(excBB);
          cOpStck.get(0).set(cOpStck_0);
          _stack_top = cOpStackTop;
          if (catch_type == 0) {
            return;
          }
        }
      }
    }
    if (_monitor_top == 0) {
      return;
    }
    if (TraceMonitorMismatch && _monitor_safe) {
      reportMonitorMismatch("non-empty monitor stack at exceptional exit");
    }
    _monitor_safe = false;
  }
  void  checkType                           (CellTypeState expected, CellTypeState actual) {
    if (!expected.equalKind(actual)) {
      throw new RuntimeException("wrong type on stack (found: " +
                                 actual.toChar() + " expected: " +
                                 expected.toChar() + ")");
    }
  }
  void  ppstore                             (CellTypeState[] in,  int loc_no) {
    for (int i = 0; i < in.length && !in[i].equal(CellTypeState.bottom); i++) {
      CellTypeState expected = in[i];
      CellTypeState actual   = pop();
      checkType(expected, actual);
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(loc_no >= 0, "sanity check");
      }
      setVar(loc_no++, actual);
    }
  }
  void  ppload                              (CellTypeState[] out, int loc_no) {
    for (int i = 0; i < out.length && !out[i].equal(CellTypeState.bottom); i++) {
      CellTypeState out1 = out[i];
      CellTypeState vcts = getVar(loc_no);
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(out1.canBeReference() || out1.canBeValue(),
                    "can only load refs. and values.");
      }
      if (out1.isReference()) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(loc_no>=0, "sanity check");
        }
        if (!vcts.isReference()) {
          _conflict = true;
          if (vcts.canBeUninit()) {
            addToRefInitSet(loc_no);
            vcts = out1;
          } else {
            recordRefvalConflict(loc_no);
            vcts = out1;
          }
          push(out1); 
        } else {
          push(vcts); 
        }
      } else {
        push(out1); 
      }
      loc_no++;
    }
  }
  void  ppush1                              (CellTypeState in) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(in.isReference() | in.isValue(), "sanity check");
    }
    if (DEBUG) {
      System.err.println("   - pushing " + in.toChar());
    }
    push(in);
  }
  void  ppush                               (CellTypeState[] in) {
    for (int i = 0; i < in.length && !in[i].equal(CellTypeState.bottom); i++) {
      ppush1(in[i]);
    }
  }
  void  ppush                               (CellTypeStateList in) {
    for (int i = 0; i < in.size() && !in.get(i).equal(CellTypeState.bottom); i++) {
      ppush1(in.get(i));
    }
  }
  void  ppop1                               (CellTypeState out) {
    CellTypeState actual = pop();
    if (DEBUG) {
      System.err.println("   - popping " + actual.toChar() + ", expecting " + out.toChar());
    }
    checkType(out, actual);
  }
  void  ppop                                (CellTypeState[] out) {
    for (int i = 0; i < out.length && !out[i].equal(CellTypeState.bottom); i++) {
      ppop1(out[i]);
    }
  }
  void  ppopAny                             (int poplen) {
    if (_stack_top >= poplen) {
      _stack_top -= poplen;
    } else {
      throw new RuntimeException("stack underflow");
    }
  }
  void  pp                                  (CellTypeState[] in, CellTypeState[] out) {
    ppop(in);
    ppush(out);
  }
  void  ppNewRef                            (CellTypeState[] in, int bci) {
    ppop(in);
    ppush1(CellTypeState.makeLineRef(bci));
  }
  void  ppdupswap                           (int poplen, String out) {
    CellTypeState[] actual = new CellTypeState[5];
    Assert.that(poplen < 5, "this must be less than length of actual vector");
    for(int i = 0; i < poplen; i++) actual[i] = pop();
    for (int i = 0; i < out.length(); i++) {
      char push_ch = out.charAt(i);
      int idx = push_ch - '1';
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(idx >= 0 && idx < poplen, "wrong arguments");
      }
      push(actual[idx]);
    }
  }
  void  doLdc                               (int idx, int bci) {
    ConstantPool  cp  = method().getConstants();
    ConstantTag   tag = cp.getTagAt(idx);
    CellTypeState cts = (tag.isString() || tag.isUnresolvedString() ||
                         tag.isKlass() || tag.isUnresolvedKlass())
                          ? CellTypeState.makeLineRef(bci)
                          : valCTS;
    ppush1(cts);
  }
  void  doAstore                            (int idx) {
    CellTypeState r_or_p = pop();
    if (!r_or_p.isAddress() && !r_or_p.isReference()) {
      throw new RuntimeException("wrong type on stack (found: " +
                                 r_or_p.toChar() + ", expected: {pr})");
    }
    setVar(idx, r_or_p);
  }
  void  doJsr                               (int targBCI) {
    push(CellTypeState.makeAddr(targBCI));
  }
  void  doField                             (boolean is_get, boolean is_static, int idx, int bci) {
    ConstantPool cp        = method().getConstants();
    int nameAndTypeIdx     = cp.getNameAndTypeRefIndexAt(idx);
    int signatureIdx       = cp.getSignatureRefIndexAt(nameAndTypeIdx);
    Symbol signature       = cp.getSymbolAt(signatureIdx);
    if (DEBUG) {
      System.err.println("doField: signature = " + signature.asString() + ", idx = " + idx +
                         ", nameAndTypeIdx = " + nameAndTypeIdx + ", signatureIdx = " + signatureIdx + ", bci = " + bci);
    }
    char sigch = (char) signature.getByteAt(0);
    CellTypeState[] temp = new CellTypeState[4];
    CellTypeState[] eff  = sigcharToEffect(sigch, bci, temp);
    CellTypeState[] in = new CellTypeState[4];
    CellTypeState[] out;
    int i =  0;
    if (is_get) {
      out = eff;
    } else {
      out = epsilonCTS;
      i   = copyCTS(in, eff);
    }
    if (!is_static) in[i++] = CellTypeState.ref;
    in[i] = CellTypeState.bottom;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(i<=3, "sanity check");
    }
    pp(in, out);
  }
  void  doMethod                            (boolean is_static, boolean is_interface, int idx, int bci) {
    ConstantPool cp       = _method.getConstants();
    int nameAndTypeIdx    = cp.getTagAt(idx).isNameAndType() ? idx : cp.getNameAndTypeRefIndexAt(idx);
    int signatureIdx      = cp.getSignatureRefIndexAt(nameAndTypeIdx);
    Symbol signature      = cp.getSymbolAt(signatureIdx);
    if (DEBUG) {
      System.err.println("doMethod: signature = " + signature.asString() + ", idx = " + idx +
                         ", nameAndTypeIdx = " + nameAndTypeIdx + ", signatureIdx = " + signatureIdx +
                         ", bci = " + bci);
    }
    CellTypeStateList out = new CellTypeStateList(4);
    CellTypeStateList in  = new CellTypeStateList(MAXARGSIZE+1);   
    ComputeCallStack cse  = new ComputeCallStack(signature);
    int res_length = cse.computeForReturntype(out);
    if (out.get(0).equal(CellTypeState.ref) && out.get(1).equal(CellTypeState.bottom)) {
      out.get(0).set(CellTypeState.makeLineRef(bci));
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(res_length<=4, "max value should be vv");
    }
    int arg_length = cse.computeForParameters(is_static, in);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(arg_length<=MAXARGSIZE, "too many locals");
    }
    for (int i = arg_length - 1; i >= 0; i--) ppop1(in.get(i));
    if (_report_result_for_send == true) {
      fillStackmapForOpcodes(_itr_send, vars(), stack(), _stack_top);
      _report_result_for_send = false;
    }
    ppush(out);
  }
  void  doMultianewarray                    (int dims, int bci) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(dims >= 1, "sanity check");
    }
    for(int i = dims -1; i >=0; i--) {
      ppop1(valCTS);
    }
    ppush1(CellTypeState.makeLineRef(bci));
  }
  void  doMonitorenter                      (int bci) {
    CellTypeState actual = pop();
    if (_monitor_top == bad_monitors) {
      return;
    }
    if (actual.isLockReference()) {
      _monitor_top = bad_monitors;
      _monitor_safe = false;
      if (TraceMonitorMismatch) {
        reportMonitorMismatch("nested redundant lock -- bailout...");
      }
      return;
    }
    CellTypeState lock = CellTypeState.makeLockRef(bci);
    checkType(refCTS, actual);
    if (!actual.isInfoTop()) {
      replaceAllCTSMatches(actual, lock);
      monitorPush(lock);
    }
  }
  void  doMonitorexit                       (int bci) {
    CellTypeState actual = pop();
    if (_monitor_top == bad_monitors) {
      return;
    }
    checkType(refCTS, actual);
    CellTypeState expected = monitorPop();
    if (!actual.isLockReference() || !expected.equal(actual)) {
      _monitor_top = bad_monitors;
      _monitor_safe = false;
      BasicBlock bb = getBasicBlockContaining(bci);
      bb.setChanged(true);
      bb._monitor_top = bad_monitors;
      if (TraceMonitorMismatch) {
        reportMonitorMismatch("improper monitor pair");
      }
    } else {
      replaceAllCTSMatches(actual, CellTypeState.makeLineRef(bci));
    }
    if (_report_for_exit_bci == bci) {
      _matching_enter_bci = expected.getMonitorSource();
    }
  }
  void  doReturnMonitorCheck                () {
    if (_monitor_top > 0) {
      _monitor_safe = false;
      if (TraceMonitorMismatch) {
        reportMonitorMismatch("non-empty monitor stack at return");
      }
    }
  }
  void  doCheckcast                         () {
    CellTypeState actual = pop();
    checkType(refCTS, actual);
    push(actual);
  }
  CellTypeState[] sigcharToEffect           (char sigch, int bci, CellTypeState[] out) {
    if (sigch=='L' || sigch=='[') {
      out[0] = CellTypeState.makeLineRef(bci);
      out[1] = CellTypeState.bottom;
      return out;
    }
    if (sigch == 'J' || sigch == 'D' ) return vvCTS;  
    if (sigch == 'V' ) return epsilonCTS;             
    return vCTS;                                      
  }
  int copyCTS                               (CellTypeState[] dst, CellTypeState[] src) {
    int idx = 0;
    for (; idx < src.length && !src[idx].isBottom(); idx++) {
      dst[idx] = src[idx];
    }
    return idx;
  }
  boolean  _report_result;
  boolean  _report_result_for_send;         
  BytecodeStream _itr_send;                 
  void  reportResult                        () {
    _report_result = true;
    fillStackmapProlog(_gc_points);
    for (int i = 0; i<_bb_count; i++) {
      if (_basic_blocks[i].isReachable()) {
        _basic_blocks[i].setChanged(true);
        interpBB(_basic_blocks[i]);
      }
    }
    fillStackmapEpilog();
    fillInitVars(_init_vars);
    _report_result = false;
  }
  List _init_vars;
  void  initializeVars                      () {
    for (int k = 0; k < _init_vars.size(); k++)
      _state.get(((Integer) _init_vars.get(k)).intValue()).set(CellTypeState.makeSlotRef(k));
  }
  void  addToRefInitSet                     (int localNo) {
    Integer local = new Integer(localNo);
    if (_init_vars.contains(local))
      return;
    _init_vars.add(local);
  }
  boolean   _conflict;                      
  int       _nof_refval_conflicts;          
  int[]     _new_var_map;
  void recordRefvalConflict                 (int varNo) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(varNo>=0 && varNo< _max_locals, "index out of range");
    }
    if (TraceOopMapRewrites) {
      System.err.println("### Conflict detected (local no: " + varNo + ")");
    }
    if (_new_var_map == null) {
      _new_var_map = new int[_max_locals];
      for (int k = 0; k < _max_locals; k++)  _new_var_map[k] = k;
    }
    if ( _new_var_map[varNo] == varNo) {
      if (_max_locals + _nof_refval_conflicts >= MAX_LOCAL_VARS) {
        throw new RuntimeException("Rewriting exceeded local variable limit");
      }
      _new_var_map[varNo] = _max_locals + _nof_refval_conflicts;
      _nof_refval_conflicts++;
    }
  }
  void rewriteRefvalConflicts               () {
    if (_nof_refval_conflicts > 0) {
      if (VM.getVM().isDebugging()) {
        throw new RuntimeException("Should not reach here (method rewriting should have been done by the VM already)");
      } else {
        throw new RuntimeException("Method rewriting not yet implemented in Java");
      }
    }
  }
  String stateVecToString                   (CellTypeStateList vec, int len) {
    for (int i = 0; i < len; i++) {
      _state_vec_buf[i] = vec.get(i).toChar();
    }
    return new String(_state_vec_buf, 0, len);
  }
  void  retJumpTargetsDo                    (BytecodeStream bcs, JumpClosure closure, int varNo, int[] data) {
    CellTypeState ra = vars().get(varNo);
    if (!ra.isGoodAddress()) {
      throw new RuntimeException("ret returns from two jsr subroutines?");
    }
    int target = ra.getInfo();
    RetTableEntry rtEnt = _rt.findJsrsForTarget(target);
    int bci = bcs.bci();
    for (int i = 0; i < rtEnt.nofJsrs(); i++) {
      int target_bci = rtEnt.jsrs(i);
      BasicBlock jsr_bb    = getBasicBlockContaining(target_bci - 1);
      if (Assert.ASSERTS_ENABLED) {
        BasicBlock target_bb = _basic_blocks[1 + bbIndex(jsr_bb)];
        Assert.that(target_bb  == getBasicBlockAt(target_bci), "wrong calc. of successor basicblock");
      }
      boolean alive = jsr_bb.isAlive();
      if (alive) {
        closure.process(this, target_bci, data);
      }
    }
  }
  boolean jumpTargetsDo                     (BytecodeStream bcs, JumpClosure closure, int[] data) {
    int bci = bcs.bci();
    switch (bcs.code()) {
    case Bytecodes._ifeq:
    case Bytecodes._ifne:
    case Bytecodes._iflt:
    case Bytecodes._ifge:
    case Bytecodes._ifgt:
    case Bytecodes._ifle:
    case Bytecodes._if_icmpeq:
    case Bytecodes._if_icmpne:
    case Bytecodes._if_icmplt:
    case Bytecodes._if_icmpge:
    case Bytecodes._if_icmpgt:
    case Bytecodes._if_icmple:
    case Bytecodes._if_acmpeq:
    case Bytecodes._if_acmpne:
    case Bytecodes._ifnull:
    case Bytecodes._ifnonnull:
      closure.process(this, bcs.dest(), data);
      closure.process(this, bci + 3, data);
      break;
    case Bytecodes._goto:
      closure.process(this, bcs.dest(), data);
      break;
    case Bytecodes._goto_w:
      closure.process(this, bcs.dest_w(), data);
      break;
    case Bytecodes._tableswitch:
      {
        BytecodeTableswitch tableswitch = BytecodeTableswitch.at(bcs);
        int len = tableswitch.length();
        closure.process(this, bci + tableswitch.defaultOffset(), data); 
        while (--len >= 0) {
          closure.process(this, bci + tableswitch.destOffsetAt(len), data);
        }
        break;
      }
    case Bytecodes._fast_linearswitch:     
    case Bytecodes._fast_binaryswitch:     
    case Bytecodes._lookupswitch:
      {
        BytecodeLookupswitch lookupswitch = BytecodeLookupswitch.at(bcs);
        int npairs = lookupswitch.numberOfPairs();
        closure.process(this, bci + lookupswitch.defaultOffset(), data); 
        while(--npairs >= 0) {
          LookupswitchPair pair = lookupswitch.pairAt(npairs);
          closure.process(this, bci + pair.offset(), data);
        }
        break;
      }
    case Bytecodes._jsr:
      Assert.that(bcs.isWide()==false, "sanity check");
      closure.process(this, bcs.dest(), data);
      break;
    case Bytecodes._jsr_w:
      closure.process(this, bcs.dest_w(), data);
      break;
    case Bytecodes._wide:
      throw new RuntimeException("Should not reach here");
    case Bytecodes._athrow:
    case Bytecodes._ireturn:
    case Bytecodes._lreturn:
    case Bytecodes._freturn:
    case Bytecodes._dreturn:
    case Bytecodes._areturn:
    case Bytecodes._return:
    case Bytecodes._ret:
      break;
    default:
      return true;
    }
    return false;
  }
  public GenerateOopMap(Method method) {
    _method = method;
    _max_locals=0;
    _init_vars = null;
    _rt = new RetTable();
  }
  public void computeMap() {
    if (DEBUG) {
      System.err.println("*** GenerateOopMap: computing for " +
                         method().getMethodHolder().getName().asString() + "." +
                         method().getName().asString() +
                         method().getSignature().asString());
    }
    _got_error      = false;
    _conflict       = false;
    _max_locals     = (int) method().getMaxLocals();
    _max_stack      = (int) method().getMaxStack();
    _has_exceptions = (method().getExceptionTable().getLength() > 0);
    _nof_refval_conflicts = 0;
    _init_vars      = new ArrayList(5);  
    _report_result  = false;
    _report_result_for_send = false;
    _report_for_exit_bci = -1;
    _new_var_map    = null;
    if (method().getCodeSize() == 0 || _max_locals + method().getMaxStack() == 0) {
      fillStackmapProlog(0);
      fillStackmapEpilog();
      return;
    }
    if (!_got_error)
      _rt.computeRetTable(_method);
    if (!_got_error)
      markBBHeadersAndCountGCPoints();
    if (!_got_error)
      doInterpretation();
    if (!_got_error && reportResults())
      reportResult();
    if (_got_error) {
      throw new RuntimeException("Illegal bytecode sequence encountered while generating interpreter pointer maps - method should be rejected by verifier.");
    }
  }
  public void resultForBasicblock(int bci) {
    _report_result = true;
    BasicBlock bb = getBasicBlockContaining(bci);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bb.isReachable(), "getting result from unreachable basicblock");
    }
    bb.setChanged(true);
    interpBB(bb);
  }
  public int maxLocals()                                  { return _max_locals; }
  public Method method()                                  { return _method; }
  public boolean monitorSafe()                            { return _monitor_safe; }
  public int  getMonitorMatch(int bci) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(_monitor_safe, "Attempt to match monitor in broken code.");
    }
    _report_for_exit_bci = bci;
    _matching_enter_bci = -1;
    BasicBlock bb = getBasicBlockContaining(bci);
    if (bb.isReachable()) {
      bb.setChanged(true);
      interpBB(bb);
      _report_for_exit_bci = -1;
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(_matching_enter_bci != -1, "monitor matching invariant");
      }
    }
    return _matching_enter_bci;
  }
  private int bbIndex(BasicBlock bb) {
    for (int i = 0; i < _basic_blocks.length; i++) {
      if (_basic_blocks[i] == bb) {
        return i;
      }
    }
    throw new RuntimeException("Should have found block");
  }
  public boolean allowRewrites            ()                              { return false; }
  public boolean reportResults            ()                              { return true;  }
  public boolean reportInitVars           ()                              { return true;  }
  public boolean possibleGCPoint          (BytecodeStream bcs)            { throw new RuntimeException("ShouldNotReachHere"); }
  public void fillStackmapProlog          (int nofGCPoints)               { throw new RuntimeException("ShouldNotReachHere"); }
  public void fillStackmapEpilog          ()                              { throw new RuntimeException("ShouldNotReachHere"); }
  public void fillStackmapForOpcodes      (BytecodeStream bcs,
                                           CellTypeStateList vars,
                                           CellTypeStateList stack,
                                           int stackTop)                  { throw new RuntimeException("ShouldNotReachHere"); }
  public void fillInitVars                (List init_vars)   { throw new RuntimeException("ShouldNotReachHere"); }
}
