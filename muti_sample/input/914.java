public class CallSite {
    private int bci;
    private Method method;
    private int count;
    private String receiver;
    private int receiver_count;
    private String reason;
    private List<CallSite> calls;
    CallSite() {
    }
    CallSite(int bci, Method m) {
        this.bci = bci;
        this.method = m;
    }
    void add(CallSite site) {
        if (getCalls() == null) {
            setCalls(new ArrayList<CallSite>());
        }
        getCalls().add(site);
    }
    CallSite last() {
        return last(-1);
    }
    CallSite last(int fromEnd) {
        return getCalls().get(getCalls().size() + fromEnd);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getReason() == null) {
            sb.append("  @ " + getBci() + " " + getMethod());
        } else {
            sb.append("- @ " + getBci() + " " + getMethod() + " " + getReason());
        }
        sb.append("\n");
        if (getCalls() != null) {
            for (CallSite site : getCalls()) {
                sb.append(site);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    public void print(PrintStream stream) {
        print(stream, 0);
    }
    void emit(PrintStream stream, int indent) {
        for (int i = 0; i < indent; i++) {
            stream.print(' ');
        }
    }
    private static boolean compat = true;
    public void print(PrintStream stream, int indent) {
        emit(stream, indent);
        String m = getMethod().getHolder().replace('/', '.') + "::" + getMethod().getName();
        if (getReason() == null) {
            stream.println("  @ " + getBci() + " " + m + " (" + getMethod().getBytes() + " bytes)");
        } else {
            if (isCompat()) {
                stream.println("  @ " + getBci() + " " + m + " " + getReason());
            } else {
                stream.println("- @ " + getBci() + " " + m +
                        " (" + getMethod().getBytes() + " bytes) " + getReason());
            }
        }
        if (getReceiver() != null) {
            emit(stream, indent + 3);
            stream.println("type profile " + getMethod().getHolder() + " -> " + getReceiver() + " (" +
                    (getReceiverCount() * 100 / getCount()) + "%)");
        }
        if (getCalls() != null) {
            for (CallSite site : getCalls()) {
                site.print(stream, indent + 2);
            }
        }
    }
    public int getBci() {
        return bci;
    }
    public void setBci(int bci) {
        this.bci = bci;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public int getReceiverCount() {
        return receiver_count;
    }
    public void setReceiver_count(int receiver_count) {
        this.receiver_count = receiver_count;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public List<CallSite> getCalls() {
        return calls;
    }
    public void setCalls(List<CallSite> calls) {
        this.calls = calls;
    }
    public static boolean isCompat() {
        return compat;
    }
    public static void setCompat(boolean aCompat) {
        compat = aCompat;
    }
}
