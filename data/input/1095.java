public abstract class SignatureIterator {
  protected Symbol _signature;       
  protected int    _index;           
  protected int    _parameter_index; 
  protected void expect(char c) {
    if (_signature.getByteAt(_index) != (byte) c) {
      throw new RuntimeException("expecting '" + c + "'");
    }
    _index++;
  }
  protected void skipOptionalSize() {
    byte c = _signature.getByteAt(_index);
    while ('0' <= c && c <= '9') {
      c = _signature.getByteAt(++_index);
    }
  }
  protected int parseType() {
    switch(_signature.getByteAt(_index)) {
    case 'B': doByte  (); _index++; return BasicTypeSize.getTByteSize();
    case 'C': doChar  (); _index++; return BasicTypeSize.getTCharSize();
    case 'D': doDouble(); _index++; return BasicTypeSize.getTDoubleSize();
    case 'F': doFloat (); _index++; return BasicTypeSize.getTFloatSize();
    case 'I': doInt   (); _index++; return BasicTypeSize.getTIntSize();
    case 'J': doLong  (); _index++; return BasicTypeSize.getTLongSize();
    case 'S': doShort (); _index++; return BasicTypeSize.getTShortSize();
    case 'Z': doBool  (); _index++; return BasicTypeSize.getTBooleanSize();
    case 'V':
      {
        if (!isReturnType()) {
          throw new RuntimeException("illegal parameter type V (void)");
        }
        doVoid(); _index++;
        return BasicTypeSize.getTVoidSize();
      }
    case 'L':
      {
        int begin = ++_index;
        while (_signature.getByteAt(_index++) != ';') ;
        doObject(begin, _index);
        return BasicTypeSize.getTObjectSize();
      }
    case '[':
      {
        int begin = ++_index;
        skipOptionalSize();
        while (_signature.getByteAt(_index) == '[') {
          _index++;
          skipOptionalSize();
        }
        if (_signature.getByteAt(_index) == 'L') {
          while (_signature.getByteAt(_index++) != ';') ;
        } else {
          _index++;
        }
        doArray(begin, _index);
        return BasicTypeSize.getTArraySize();
      }
    }
    throw new RuntimeException("Should not reach here: char " + (char)_signature.getByteAt(_index) + " @ " + _index + " in " + _signature.asString());
  }
  protected void checkSignatureEnd() {
    if (_index < _signature.getLength()) {
      System.err.println("too many chars in signature");
      _signature.printValueOn(System.err);
      System.err.println(" @ " + _index);
    }
  }
  public SignatureIterator(Symbol signature) {
    _signature       = signature;
    _parameter_index = 0;
  }
  public void dispatchField() {
    _index = 0;
    _parameter_index = 0;
    parseType();
    checkSignatureEnd();
  }
  public void iterateParameters() {
    _index = 0;
    _parameter_index = 0;
    expect('(');
    while (_signature.getByteAt(_index) != ')') {
      _parameter_index += parseType();
    }
    expect(')');
    _parameter_index = 0; 
  }
  public void iterateReturntype() {
    _index = 0;
    expect('(');
    while (_signature.getByteAt(_index) != ')') {
      _index++;
    }
    expect(')');
    _parameter_index = -1;
    parseType();
    checkSignatureEnd();
    _parameter_index = 0; 
  }
  public void iterate() {
    _index = 0;
    _parameter_index = 0;
    expect('(');
    while (_signature.getByteAt(_index) != ')') {
      _parameter_index += parseType();
    }
    expect(')');
    _parameter_index = -1;
    parseType();
    checkSignatureEnd();
    _parameter_index = 0; 
  }
  public int  parameterIndex()               { return _parameter_index; }
  public boolean isReturnType()              { return (parameterIndex() < 0); }
  public abstract void doBool  ();
  public abstract void doChar  ();
  public abstract void doFloat ();
  public abstract void doDouble();
  public abstract void doByte  ();
  public abstract void doShort ();
  public abstract void doInt   ();
  public abstract void doLong  ();
  public abstract void doVoid  ();
  public abstract void doObject(int begin, int end);
  public abstract void doArray (int begin, int end);
}
