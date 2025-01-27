public class ContextStack {
    public static final int TOP = 1;
    public static final int METHOD = 2;
    public static final int METHOD_RETURN = 3;
    public static final int METHOD_ARGUMENT = 4;
    public static final int METHOD_EXCEPTION = 5;
    public static final int MEMBER = 6;
    public static final int MEMBER_CONSTANT = 7;
    public static final int MEMBER_STATIC = 8;
    public static final int MEMBER_TRANSIENT = 9;
    public static final int IMPLEMENTS = 10;
    public static final int EXTENDS = 11;
    private static final String[] CODE_NAMES = {
        "UNKNOWN ",
        "Top level type ",
        "Method ",
        "Return parameter ",
        "Parameter ",
        "Exception ",
        "Member ",
        "Constant member ",
        "Static member ",
        "Transient member ",
        "Implements ",
        "Extends ",
    };
    private int currentIndex = -1;
    private int maxIndex = 100;
    private TypeContext[] stack = new TypeContext[maxIndex];
    private int newCode = TOP;
    private BatchEnvironment env = null;
    private boolean trace = false;
    private TypeContext tempContext = new TypeContext();
    private static final String TRACE_INDENT = "   ";
    public ContextStack (BatchEnvironment env) {
        this.env = env;
        env.contextStack = this;
    }
    public boolean anyErrors () {
        return env.nerrors > 0;
    }
    public void setTrace(boolean trace) {
        this.trace = trace;
    }
    public boolean isTraceOn() {
        return trace;
    }
    public BatchEnvironment getEnv() {
        return env;
    }
    public void setNewContextCode(int code) {
        newCode = code;
    }
    public int getCurrentContextCode() {
        return newCode;
    }
    final void traceCallStack () {
        if (trace) dumpCallStack();
    }
    public final static void dumpCallStack() {
        new Error().printStackTrace(System.out);
    }
    final private void tracePrint (String text, boolean line) {
        int length = text.length() + (currentIndex * TRACE_INDENT.length());
        StringBuffer buffer = new StringBuffer(length);
        for (int i = 0; i < currentIndex; i++) {
            buffer.append(TRACE_INDENT);
        }
        buffer.append(text);
        if (line) {
            buffer.append("\n");
        }
        System.out.print(buffer.toString());
    }
    final void trace (String text) {
        if (trace) {
            tracePrint(text,false);
        }
    }
    final void traceln (String text) {
        if (trace) {
            tracePrint(text,true);
        }
    }
    final void traceExistingType (Type type) {
        if (trace) {
            tempContext.set(newCode,type);
            traceln(toResultString(tempContext,true,true));
        }
    }
    public TypeContext push (ContextElement element) {
        currentIndex++;
        if (currentIndex == maxIndex) {
            int newMax = maxIndex * 2;
            TypeContext[] newStack = new TypeContext[newMax];
            System.arraycopy(stack,0,newStack,0,maxIndex);
            maxIndex = newMax;
            stack = newStack;
        }
        TypeContext it = stack[currentIndex];
        if (it == null) {
            it = new TypeContext();
            stack[currentIndex] = it;
        }
        it.set(newCode,element);
        traceln(toTrialString(it));
        return it;
    }
    public TypeContext pop (boolean wasValid) {
        if (currentIndex < 0) {
            throw new CompilerError("Nothing on stack!");
        }
        newCode = stack[currentIndex].getCode();
        traceln(toResultString(stack[currentIndex],wasValid,false));
        Type last = stack[currentIndex].getCandidateType();
        if (last != null) {
            if (wasValid) {
                last.setStatus(Constants.STATUS_VALID);
            } else {
                last.setStatus(Constants.STATUS_INVALID);
            }
        }
        currentIndex--;
        if (currentIndex < 0) {
            if (wasValid) {
                Type.updateAllInvalidTypes(this);
            }
            return null;
        } else {
            return stack[currentIndex];
        }
    }
    public int size () {
        return currentIndex + 1;
    }
    public TypeContext getContext (int index) {
        if (currentIndex < index) {
            throw new Error("Index out of range");
        }
        return stack[index];
    }
    public TypeContext getContext () {
        if (currentIndex < 0) {
            throw new Error("Nothing on stack!");
        }
        return stack[currentIndex];
    }
    public boolean isParentAValue () {
        if (currentIndex > 0) {
            return stack[currentIndex - 1].isValue();
        } else {
            return false;
        }
    }
    public TypeContext getParentContext () {
        if (currentIndex > 0) {
            return stack[currentIndex - 1];
        } else {
            return null;
        }
    }
    public String getContextCodeString () {
        if (currentIndex >= 0) {
            return CODE_NAMES[newCode];
        } else {
            return CODE_NAMES[0];
        }
    }
    public static String getContextCodeString (int contextCode) {
        return CODE_NAMES[contextCode];
    }
    private String toTrialString(TypeContext it) {
        int code = it.getCode();
        if (code != METHOD && code != MEMBER) {
            return it.toString() + " (trying " + it.getTypeDescription() + ")";
        } else {
            return it.toString();
        }
    }
    private String toResultString (TypeContext it, boolean result, boolean preExisting) {
        int code = it.getCode();
        if (code != METHOD && code != MEMBER) {
            if (result) {
                String str = it.toString() + " --> " + it.getTypeDescription();
                if (preExisting) {
                    return str + " [Previously mapped]";
                } else {
                    return str;
                }
            }
        } else {
            if (result) {
                return it.toString() + " --> [Mapped]";
            }
        }
        return it.toString() + " [Did not map]";
    }
    public void clear () {
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] != null) stack[i].destroy();
        }
    }
}
class TypeContext {
    public void set(int code, ContextElement element) {
        this.code = code;
        this.element = element;
        if (element instanceof ValueType) {
            isValue = true;
        } else {
            isValue = false;
        }
    }
    public int getCode() {
        return code;
    }
    public String getName() {
        return element.getElementName();
    }
    public Type getCandidateType() {
        if (element instanceof Type) {
            return (Type) element;
        } else {
            return null;
        }
}
public String getTypeDescription() {
    if (element instanceof Type) {
        return ((Type) element).getTypeDescription();
    } else {
        return "[unknown type]";
    }
}
public String toString () {
    if (element != null) {
        return ContextStack.getContextCodeString(code) + element.getElementName();
    } else {
        return ContextStack.getContextCodeString(code) + "null";
    }
}
public boolean isValue () {
    return isValue;
}
    public boolean isConstant () {
        return code == ContextStack.MEMBER_CONSTANT;
    }
    public void destroy() {
        if (element instanceof Type) {
            ((Type)element).destroy();
        }
        element = null;
    }
    private int code = 0;
    private ContextElement element = null;
    private boolean isValue = false;
}
