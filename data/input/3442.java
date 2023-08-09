class WindbgCDebugInfoBuilder
  implements DebugVC50SubsectionTypes, DebugVC50TypeLeafIndices, DebugVC50TypeEnums, DebugVC50SymbolTypes, DebugVC50MemberAttributes, CVAttributes, AccessControl {
  private WindbgDebugger dbg;
  private Address       base;
  private DebugVC50 vc50;
  private BasicCDebugInfoDataBase db;
  private DebugVC50TypeIterator iter;
  private DebugVC50SymbolIterator symIter;
  private COFFFile file;
  private DebugVC50SSSegMap segMap;
  private Map primIndexToTypeMap;
  private BasicEnumType unnamedEnum;
  private Stack blockStack;
  private int   endsToSkip;
  private static final int POINTER_SIZE = 4;
  WindbgCDebugInfoBuilder(WindbgDebugger dbg) {
    this.dbg = dbg;
  }
  CDebugInfoDataBase buildDataBase(String dllName, Address base) {
    this.base = base;
    file = COFFFileParser.getParser().parse(dllName);
    vc50 = getDebugVC50(file);
    if (vc50 == null) return null;
    segMap = getSegMap();
    primIndexToTypeMap = new HashMap();
    blockStack = new Stack();
    endsToSkip = 0;
    db = new BasicCDebugInfoDataBase();
    db.beginConstruction();
    DebugVC50SSGlobalTypes types = getGlobalTypes();
    for (iter = types.getTypeIterator(); !iter.done(); iter.next()) {
      while (!iter.typeStringDone()) {
        switch (iter.typeStringLeaf()) {
        case LF_MODIFIER: {
          int idx = iter.getModifierIndex();
          BasicType target = getTypeByIndex(idx);
          short windowsMods = iter.getModifierAttribute();
          short mods = 0;
          if ((windowsMods & MODIFIER_CONST_MASK)    != 0) mods |= CONST;
          if ((windowsMods & MODIFIER_VOLATILE_MASK) != 0) mods |= VOLATILE;
          putType(target.getCVVariant(mods));
          break;
        }
        case LF_POINTER: {
          int idx = iter.getPointerType();
          BasicType target = getTypeByIndex(idx);
          short windowsMods = iter.getModifierAttribute();
          short mods = 0;
          if ((windowsMods & POINTER_CONST_MASK)    != 0) mods |= CONST;
          if ((windowsMods & POINTER_VOLATILE_MASK) != 0) mods |= VOLATILE;
          BasicPointerType ptrType = new BasicPointerType(POINTER_SIZE, target);
          if (mods != 0) {
            ptrType = (BasicPointerType) ptrType.getCVVariant(mods);
          }
          putType(ptrType);
          break;
        }
        case LF_ARRAY: {
          BasicType elemType = getTypeByIndex(iter.getArrayElementType());
          putType(new BasicArrayType(iter.getArrayName(), elemType, iter.getArrayLength()));
          break;
        }
        case LF_CLASS:
        case LF_STRUCTURE: {
          CompoundTypeKind kind = ((iter.typeStringLeaf() == LF_CLASS) ? CompoundTypeKind.CLASS
                                                                       : CompoundTypeKind.STRUCT);
          BasicCompoundType type = new BasicCompoundType(iter.getClassName(),
                                                         iter.getClassSize(),
                                                         kind);
          if ((iter.getClassProperty() & PROPERTY_FWDREF) == 0) {
            DebugVC50TypeIterator fieldIter = iter.getClassFieldListIterator();
            if (Assert.ASSERTS_ENABLED) {
              Assert.that(fieldIter.typeStringLeaf() == LF_FIELDLIST, "Expected field list");
            }
            boolean advance = false;
            while (!fieldIter.typeStringDone()) {
              advance = true;
              switch (fieldIter.typeStringLeaf()) {
              case LF_FIELDLIST: break;
              case LF_BCLASS: {
                int accessControl = memberAttributeToAccessControl(fieldIter.getBClassAttribute());
                Type baseType = getTypeByIndex(fieldIter.getBClassType());
                type.addBaseClass(new BasicBaseClass(accessControl, false, baseType));
                break;
              }
              case LF_VBCLASS: {
                int accessControl = memberAttributeToAccessControl(fieldIter.getVBClassAttribute());
                Type baseType = getTypeByIndex(fieldIter.getVBClassBaseClassType());
                type.addBaseClass(new BasicBaseClass(accessControl, true, baseType));
                break;
              }
              case LF_IVBCLASS: break;
              case LF_INDEX: {
                fieldIter = fieldIter.getIndexIterator();
                advance = false;
                break;
              }
              case LF_MEMBER: {
                BasicField field = new BasicField(fieldIter.getMemberName(),
                                                  getTypeByIndex(fieldIter.getMemberType()),
                                                  memberAttributeToAccessControl(fieldIter.getMemberAttribute()),
                                                  false);
                field.setOffset(fieldIter.getMemberOffset());
                type.addField(field);
                break;
              }
              case LF_STMEMBER: {
                BasicField field = new BasicField(fieldIter.getStaticName(),
                                                  getTypeByIndex(fieldIter.getStaticType()),
                                                  memberAttributeToAccessControl(fieldIter.getStaticAttribute()),
                                                  true);
                type.addField(field);
                break;
              }
              case LF_METHOD: break;
              case LF_ONEMETHOD: break;
              case LF_NESTTYPE: break;
              case LF_NESTTYPEEX: break;
              case LF_VFUNCTAB: break;
              case LF_FRIENDCLS: break;
              case LF_VFUNCOFF: break;
              case LF_MEMBERMODIFY: break;
              case LF_PAD0:  case LF_PAD1:  case LF_PAD2:  case LF_PAD3:
              case LF_PAD4:  case LF_PAD5:  case LF_PAD6:  case LF_PAD7:
              case LF_PAD8:  case LF_PAD9:  case LF_PAD10: case LF_PAD11:
              case LF_PAD12: case LF_PAD13: case LF_PAD14: case LF_PAD15: break;
              default: System.err.println("WARNING: unexpected leaf index " +
                                          fieldIter.typeStringLeaf() +
                                          " in field list for type " + iter.getTypeIndex());
              }
              if (advance) {
                fieldIter.typeStringNext();
              }
            }
          }
          putType(type);
          break;
        }
        case LF_UNION: {
          BasicCompoundType type = new BasicCompoundType(iter.getUnionName(),
                                                         iter.getUnionSize(),
                                                         CompoundTypeKind.UNION);
          if ((iter.getClassProperty() & PROPERTY_FWDREF) == 0) {
            DebugVC50TypeIterator fieldIter = iter.getUnionFieldListIterator();
            if (Assert.ASSERTS_ENABLED) {
              Assert.that(fieldIter.typeStringLeaf() == LF_FIELDLIST, "Expected field list");
            }
            boolean advance = false;
            while (!fieldIter.typeStringDone()) {
              advance = true;
              switch (fieldIter.typeStringLeaf()) {
              case LF_FIELDLIST: break;
              case LF_BCLASS:    break;
              case LF_VBCLASS:   break;
              case LF_IVBCLASS:  break;
              case LF_INDEX: {
                fieldIter = fieldIter.getIndexIterator();
                advance = false;
                break;
              }
              case LF_MEMBER: {
                BasicField field = new BasicField(fieldIter.getMemberName(),
                                                  getTypeByIndex(fieldIter.getMemberType()),
                                                  memberAttributeToAccessControl(fieldIter.getMemberAttribute()),
                                                  false);
                field.setOffset(fieldIter.getMemberOffset());
                type.addField(field);
                break;
              }
              case LF_STMEMBER: {
                System.err.println("WARNING: I didn't think unions could contain static fields...");
                BasicField field = new BasicField(fieldIter.getStaticName(),
                                                  getTypeByIndex(fieldIter.getStaticType()),
                                                  memberAttributeToAccessControl(fieldIter.getStaticAttribute()),
                                                  true);
                type.addField(field);
                break;
              }
              case LF_METHOD: break;
              case LF_ONEMETHOD: break;
              case LF_NESTTYPE: break;
              case LF_NESTTYPEEX: break;
              case LF_VFUNCTAB: break;
              case LF_FRIENDCLS: break;
              case LF_VFUNCOFF: break;
              case LF_MEMBERMODIFY: break;
              case LF_PAD0:  case LF_PAD1:  case LF_PAD2:  case LF_PAD3:
              case LF_PAD4:  case LF_PAD5:  case LF_PAD6:  case LF_PAD7:
              case LF_PAD8:  case LF_PAD9:  case LF_PAD10: case LF_PAD11:
              case LF_PAD12: case LF_PAD13: case LF_PAD14: case LF_PAD15: break;
              default: System.err.println("WARNING: unexpected leaf index " +
                                          fieldIter.typeStringLeaf() +
                                          " in field list for union of type " + iter.getTypeIndex());
              }
              if (advance) {
                fieldIter.typeStringNext();
              }
            }
          }
          putType(type);
          break;
        }
        case LF_ENUM: {
          String name = iter.getEnumName();
          BasicEnumType enumType = null;
          if ((name == null) || (name.equals(""))) {
            if (unnamedEnum == null) {
              unnamedEnum = new BasicEnumType(null, getTypeByIndex(iter.getEnumType()));
            }
            enumType = unnamedEnum;
          } else {
            enumType = new BasicEnumType(name, getTypeByIndex(iter.getEnumType()));
          }
          DebugVC50TypeIterator fieldIter = iter.getEnumFieldListIterator();
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(fieldIter.typeStringLeaf() == LF_FIELDLIST, "Expected field list");
          }
          boolean advance = false;
          while (!fieldIter.typeStringDone()) {
            advance = true;
            switch (fieldIter.typeStringLeaf()) {
            case LF_FIELDLIST: break;
            case LF_ENUMERATE: {
              String enumName = fieldIter.getEnumerateName();
              long   enumVal  = fieldIter.getEnumerateValue();
              enumType.addEnum(enumName, enumVal);
              break;
            }
            case LF_INDEX: {
              fieldIter = fieldIter.getIndexIterator();
              advance = false;
              break;
            }
            case LF_PAD0:  case LF_PAD1:  case LF_PAD2:  case LF_PAD3:
            case LF_PAD4:  case LF_PAD5:  case LF_PAD6:  case LF_PAD7:
            case LF_PAD8:  case LF_PAD9:  case LF_PAD10: case LF_PAD11:
            case LF_PAD12: case LF_PAD13: case LF_PAD14: case LF_PAD15: break;
            default: System.err.println("WARNING: unexpected leaf index " +
                                        fieldIter.typeStringLeaf() +
                                        " in field list for enum of type " + iter.getTypeIndex());
            }
            if (advance) {
              fieldIter.typeStringNext();
            }
          }
          putType(enumType);
          break;
        }
        case LF_PROCEDURE: {
          Type retType = getTypeByIndex(iter.getProcedureReturnType());
          BasicFunctionType func = new BasicFunctionType(null, POINTER_SIZE, retType);
          DebugVC50TypeIterator argIter = iter.getProcedureArgumentListIterator();
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(argIter.typeStringLeaf() == LF_ARGLIST, "Expected argument list");
          }
          for (int i = 0; i < argIter.getArgListCount(); i++) {
            func.addArgumentType(getTypeByIndex(argIter.getArgListType(i)));
          }
          putType(func);
          break;
        }
        case LF_MFUNCTION: {
          Type retType   = getTypeByIndex(iter.getMFunctionReturnType());
          Type container = getTypeByIndex(iter.getMFunctionContainingClass());
          Type thisType  = getTypeByIndex(iter.getMFunctionThis());
          long thisAdjust = iter.getMFunctionThisAdjust();
          BasicMemberFunctionType func = new BasicMemberFunctionType(null,
                                                                     POINTER_SIZE,
                                                                     retType,
                                                                     container,
                                                                     thisType,
                                                                     thisAdjust);
          DebugVC50TypeIterator argIter = iter.getMFunctionArgumentListIterator();
          for (int i = 0; i < argIter.getArgListCount(); i++) {
            func.addArgumentType(getTypeByIndex(argIter.getArgListType(i)));
          }
          putType(func);
          break;
        }
        case LF_VTSHAPE: break;
        case LF_BARRAY: System.err.println("FIXME: don't know what to do with LF_BARRAY leaves (convert to pointers?"); break;
        case LF_LABEL: break;
        case LF_NULL: break; 
        case LF_DIMARRAY: System.err.println("FIXME: don't know what to do with LF_DIMARRAY leaves yet"); break;
        case LF_VFTPATH: break;
        case LF_PRECOMP: break;
        case LF_ENDPRECOMP: break;
        case LF_OEM: break;
        case LF_TYPESERVER: break;
        case LF_SKIP: break;
        case LF_ARGLIST: skipTypeRecord(); break;
        case LF_DEFARG: System.err.println("FIXME: handle default arguments (dereference the type)"); break;
        case LF_FIELDLIST: skipTypeRecord(); break;
        case LF_DERIVED: break;
        case LF_BITFIELD: {
          Type underlyingType = getTypeByIndex(iter.getBitfieldFieldType());
          BasicBitType bit = new BasicBitType(underlyingType,
                                              (iter.getBitfieldLength() & 0xFF),
                                              (iter.getBitfieldPosition() & 0xFF));
          putType(bit);
          break;
        }
        case LF_METHODLIST: break;
        case LF_DIMCONU:
        case LF_DIMCONLU:
        case LF_DIMVARU:
        case LF_DIMVARLU: break;
        case LF_REFSYM: break;
        case LF_PAD0:  case LF_PAD1:  case LF_PAD2:  case LF_PAD3:
        case LF_PAD4:  case LF_PAD5:  case LF_PAD6:  case LF_PAD7:
        case LF_PAD8:  case LF_PAD9:  case LF_PAD10: case LF_PAD11:
        case LF_PAD12: case LF_PAD13: case LF_PAD14: case LF_PAD15: break;
        default: {
          System.err.println("Unexpected leaf index " +
                             iter.typeStringLeaf() + " at offset 0x" +
                             Integer.toHexString(iter.typeStringOffset()));
          break;
        }
        }
        if (!iter.typeStringDone()) {
          iter.typeStringNext();
        }
      }
    }
    DebugVC50SubsectionDirectory dir = vc50.getSubsectionDirectory();
    int moduleNumber = 0; 
    for (int i = 0; i < dir.getNumEntries(); i++) {
      DebugVC50Subsection ss = dir.getSubsection(i);
      int ssType = ss.getSubsectionType();
      boolean process = false;
      if ((ssType == SST_GLOBAL_SYM) ||
          (ssType == SST_GLOBAL_PUB) ||
          (ssType == SST_STATIC_SYM)) {
        DebugVC50SSSymbolBase syms = (DebugVC50SSSymbolBase) ss;
        symIter = syms.getSymbolIterator();
        process = true;
      }
      if (ssType == SST_ALIGN_SYM) {
        DebugVC50SSAlignSym syms = (DebugVC50SSAlignSym) ss;
        symIter = syms.getSymbolIterator();
        process = true;
      }
      if (process) {
        for (; !symIter.done(); symIter.next()) {
          switch (symIter.getType()) {
          case S_COMPILE: break;
          case S_SSEARCH: break; 
          case S_END: {
            try {
              if (endsToSkip == 0) {
                blockStack.pop();
              } else {
                --endsToSkip;
              }
            } catch (EmptyStackException e) {
              System.err.println("WARNING: mismatched block begins/ends in debug information");
            }
            break;
          }
          case S_SKIP: break;
          case S_CVRESERVE: break;
          case S_OBJNAME: break; 
          case S_ENDARG: break;
          case S_COBOLUDT: break;
          case S_MANYREG: break; 
          case S_RETURN: break;  
          case S_ENTRYTHIS: break; 
          case S_REGISTER: break; 
          case S_CONSTANT: break; 
          case S_UDT: break; 
          case S_COBOLUDT2: break;
          case S_MANYREG2: break;
          case S_BPREL32: {
            LocalSym sym = new BasicLocalSym(symIter.getBPRelName(),
                                             getTypeByIndex(symIter.getBPRelType()),
                                             symIter.getBPRelOffset());
            addLocalToCurBlock(sym);
            break;
          }
          case S_LDATA32:
          case S_GDATA32: {
            boolean isModuleLocal = (symIter.getType() == S_LDATA32);
            GlobalSym sym = new BasicGlobalSym(symIter.getLGDataName(),
                                               getTypeByIndex(symIter.getLGDataType()),
                                               newAddress(symIter.getLGDataOffset(), symIter.getLGDataSegment()),
                                               isModuleLocal);
            addGlobalSym(sym);
            break;
          }
          case S_PUB32: break; 
          case S_LPROC32:
          case S_GPROC32: {
            BasicFunctionSym sym = new BasicFunctionSym(newLazyBlockSym(symIter.getLGProcParentOffset()),
                                                        symIter.getLGProcLength(),
                                                        newAddress(symIter.getLGProcOffset(), symIter.getLGProcSegment()),
                                                        symIter.getLGProcName(),
                                                        getTypeByIndex(symIter.getLGProcType()),
                                                        (symIter.getType() == S_LPROC32));
            addBlock(sym);
            break;
          }
          case S_THUNK32: {
            skipEnd();
            break;
          }
          case S_BLOCK32: {
            BasicBlockSym sym = new BasicBlockSym(newLazyBlockSym(symIter.getBlockParentOffset()),
                                                  symIter.getBlockLength(),
                                                  newAddress(symIter.getBlockOffset(), symIter.getBlockSegment()),
                                                  symIter.getBlockName());
            addBlock(sym);
            break;
          }
          case S_WITH32: break;
          case S_LABEL32: break;
          case S_CEXMODEL32: break;
          case S_VFTTABLE32: break; 
          case S_REGREL32: break;   
          case S_LTHREAD32: break;
          case S_GTHREAD32: break;  
          case S_PROCREF: break;
          case S_DATAREF: break;
          case S_ALIGN: break;
          default:
            if ((symIter.getType() != 0) && (symIter.getType() != 4115)) {
              System.err.println("  NOTE: Unexpected symbol of type " +
                                 symIter.getType() + " at offset 0x" +
                                 Integer.toHexString(symIter.getOffset()));
            }
            break;
          }
        }
      }
    }
    for (int i = 0; i < dir.getNumEntries(); i++) {
      DebugVC50Subsection ss = dir.getSubsection(i);
      if (ss.getSubsectionType() == SST_SRC_MODULE) {
        DebugVC50SSSrcModule srcMod = (DebugVC50SSSrcModule) ss;
        for (int sf = 0; sf < srcMod.getNumSourceFiles(); sf++) {
          DebugVC50SrcModFileDesc desc = srcMod.getSourceFileDesc(sf);
          String name = desc.getSourceFileName().intern();
          for (int cs = 0; cs < desc.getNumCodeSegments(); cs++) {
            DebugVC50SrcModLineNumberMap map = desc.getLineNumberMap(cs);
            SectionHeader seg = file.getHeader().getSectionHeader(map.getSegment());
            for (int lp = 0; lp < map.getNumSourceLinePairs(); lp++) {
              Address startPC = base.addOffsetTo(seg.getVirtualAddress() + map.getCodeOffset(lp));
              Address endPC = base.addOffsetTo(seg.getSize());
              db.addLineNumberInfo(new BasicLineNumberInfo(name, map.getLineNumber(lp), startPC, endPC));
            }
          }
        }
      }
    }
    db.resolve(new ResolveListener() {
        public void resolveFailed(Type containingType, LazyType failedResolve, String detail) {
          System.err.println("WARNING: failed to resolve type of index " +
                             ((Integer) failedResolve.getKey()).intValue() +
                             " in type " + containingType.getName() + " (class " +
                             containingType.getClass().getName() + ") while " + detail);
        }
        public void resolveFailed(Type containingType, String staticFieldName) {
          System.err.println("WARNING: failed to resolve address of static field \"" +
                             staticFieldName + "\" in type " + containingType.getName());
        }
        public void resolveFailed(Sym containingSymbol, LazyType failedResolve, String detail) {
          System.err.println("WARNING: failed to resolve type of index " +
                             ((Integer) failedResolve.getKey()).intValue() +
                             " in symbol of type " + containingSymbol.getClass().getName() +
                             " while " + detail);
        }
        public void resolveFailed(Sym containingSymbol, LazyBlockSym failedResolve, String detail) {
          System.err.println("WARNING: failed to resolve block at offset 0x" +
                             Integer.toHexString(((Integer) failedResolve.getKey()).intValue()) +
                             " in symbol of type " + containingSymbol.getClass().getName() +
                             " while " + detail);
        }
      });
    db.endConstruction();
    return db;
  }
  private static DebugVC50 getDebugVC50(COFFFile file) {
    COFFHeader header = file.getHeader();
    OptionalHeader opt = header.getOptionalHeader();
    if (opt == null) {
      return null;
    }
    OptionalHeaderDataDirectories dd = opt.getDataDirectories();
    if (dd == null) {
      return null;
    }
    DebugDirectory debug = dd.getDebugDirectory();
    if (debug == null) {
      return null;
    }
    for (int i = 0; i < debug.getNumEntries(); i++) {
      DebugDirectoryEntry entry = debug.getEntry(i);
      if (entry.getType() == DebugTypes.IMAGE_DEBUG_TYPE_CODEVIEW) {
        return entry.getDebugVC50();
      }
    }
    return null;
  }
  private DebugVC50SSSegMap getSegMap() {
    return (DebugVC50SSSegMap) findSubsection(SST_SEG_MAP);
  }
  private DebugVC50SSGlobalTypes getGlobalTypes() {
    return (DebugVC50SSGlobalTypes) findSubsection(SST_GLOBAL_TYPES);
  }
  private DebugVC50SSGlobalSym getGlobalSymbols() {
    return (DebugVC50SSGlobalSym) findSubsection(SST_GLOBAL_SYM);
  }
  private DebugVC50Subsection findSubsection(short ssType) {
    DebugVC50SubsectionDirectory dir = vc50.getSubsectionDirectory();
    for (int i = 0; i < dir.getNumEntries(); i++) {
      DebugVC50Subsection ss = dir.getSubsection(i);
      if (ss.getSubsectionType() == ssType) {
        return ss;
      }
    }
    throw new DebuggerException("Unable to find subsection of type " + ssType);
  }
  private void putType(Type t) {
    db.addType(new Integer(iter.getTypeIndex()), t);
  }
  private Address newAddress(int offset, short segment) {
    int seg = segment & 0xFFFF;
    SectionHeader section = file.getHeader().getSectionHeader(seg);
    return base.addOffsetTo(section.getVirtualAddress() + offset);
  }
  private BasicType getTypeByIndex(int intIndex) {
    Integer index = new Integer(intIndex);
    if (intIndex <= 0x0FFF) {
      BasicType type = (BasicType) primIndexToTypeMap.get(index);
      if (type != null) {
        return type;
      }
      int primMode = intIndex & RESERVED_MODE_MASK;
      if (primMode == RESERVED_MODE_DIRECT) {
        int primType = intIndex & RESERVED_TYPE_MASK;
        switch (primType) {
        case RESERVED_TYPE_SIGNED_INT:
        case RESERVED_TYPE_UNSIGNED_INT: {
          boolean unsigned = (primType == RESERVED_TYPE_UNSIGNED_INT);
          int size = 0;
          String name = null;
          switch (intIndex & RESERVED_SIZE_MASK) {
          case RESERVED_SIZE_INT_1_BYTE: size = 1; name = "char";    break;
          case RESERVED_SIZE_INT_2_BYTE: size = 2; name = "short";   break;
          case RESERVED_SIZE_INT_4_BYTE: size = 4; name = "int";     break;
          case RESERVED_SIZE_INT_8_BYTE: size = 8; name = "__int64"; break;
          default: throw new DebuggerException("Illegal size of integer type " + intIndex);
          }
          type = new BasicIntType(name, size, unsigned);
          break;
        }
        case RESERVED_TYPE_BOOLEAN: {
          int size = 0;
          switch (intIndex & RESERVED_SIZE_MASK) {
          case RESERVED_SIZE_INT_1_BYTE: size = 1; break;
          case RESERVED_SIZE_INT_2_BYTE: size = 2; break;
          case RESERVED_SIZE_INT_4_BYTE: size = 4; break;
          case RESERVED_SIZE_INT_8_BYTE: size = 8; break;
          default: throw new DebuggerException("Illegal size of boolean type " + intIndex);
          }
          type = new BasicIntType("bool", size, false);
          break;
        }
        case RESERVED_TYPE_REAL: {
          switch (intIndex & RESERVED_SIZE_MASK) {
          case RESERVED_SIZE_REAL_32_BIT:
            type = new BasicFloatType("float", 4);
            break;
          case RESERVED_SIZE_REAL_64_BIT:
            type = new BasicDoubleType("double", 8);
            break;
          default:
            throw new DebuggerException("Unsupported floating-point size in type " + intIndex);
          }
          break;
        }
        case RESERVED_TYPE_REALLY_INT: {
          switch (intIndex & RESERVED_SIZE_MASK) {
          case RESERVED_SIZE_REALLY_INT_CHAR:     type = new BasicIntType("char",    1, false); break;
          case RESERVED_SIZE_REALLY_INT_WCHAR:    type = new BasicIntType("wchar",   2, false); break;
          case RESERVED_SIZE_REALLY_INT_2_BYTE:   type = new BasicIntType("short",   2, false); break;
          case RESERVED_SIZE_REALLY_INT_2_BYTE_U: type = new BasicIntType("short",   2, true);  break;
          case RESERVED_SIZE_REALLY_INT_4_BYTE:   type = new BasicIntType("int",     4, false); break;
          case RESERVED_SIZE_REALLY_INT_4_BYTE_U: type = new BasicIntType("int",     4, true);  break;
          case RESERVED_SIZE_REALLY_INT_8_BYTE:   type = new BasicIntType("__int64", 8, false); break;
          case RESERVED_SIZE_REALLY_INT_8_BYTE_U: type = new BasicIntType("__int64", 8, true);  break;
          default: throw new DebuggerException("Illegal REALLY_INT size in type " + intIndex);
          }
          break;
        }
        case RESERVED_TYPE_SPECIAL: {
          switch (intIndex & RESERVED_SIZE_MASK) {
          case RESERVED_SIZE_SPECIAL_NO_TYPE:
          case RESERVED_SIZE_SPECIAL_VOID: type = new BasicVoidType(); break;
          default: throw new DebuggerException("Don't know how to handle reserved special type " + intIndex);
          }
          break;
        }
        default:
          throw new DebuggerException("Don't know how to handle reserved type " + intIndex);
        }
      } else {
        Type targetType = getTypeByIndex(intIndex & (~RESERVED_MODE_MASK));
        type = new BasicPointerType(POINTER_SIZE, targetType);
      }
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(type != null, "Got null Type for primitive type " + intIndex);
      }
      primIndexToTypeMap.put(index, type);
      return type;
    }
    return new LazyType(index);
  }
  private void addBlock(BlockSym block) {
    db.addBlock(new Integer(symIter.getOffset()), block);
    blockStack.push(block);
  }
  private void skipEnd() {
    ++endsToSkip;
  }
  private BlockSym newLazyBlockSym(int offset) {
    if (offset == 0) {
      return null;
    }
    return new LazyBlockSym(new Integer(offset));
  }
  private int memberAttributeToAccessControl(short memberAttribute) {
    int acc = memberAttribute & MEMATTR_ACCESS_MASK;
    switch (acc) {
    case MEMATTR_ACCESS_NO_PROTECTION: return NO_PROTECTION;
    case MEMATTR_ACCESS_PRIVATE:       return PRIVATE;
    case MEMATTR_ACCESS_PROTECTED:     return PROTECTED;
    case MEMATTR_ACCESS_PUBLIC:        return PUBLIC;
    default: throw new RuntimeException("Should not reach here");
    }
  }
  private void addLocalToCurBlock(LocalSym local) {
    ((BasicBlockSym) blockStack.peek()).addLocal(local);
  }
  private void addGlobalSym(GlobalSym sym) {
    db.addGlobalSym(sym);
  }
  private void skipTypeRecord() {
    while (!iter.typeStringDone()) {
      iter.typeStringNext();
    }
  }
}
