class ReceiverList extends ArrayList<BroadcastFilter>
        implements IBinder.DeathRecipient {
    final ActivityManagerService owner;
    public final IIntentReceiver receiver;
    public final ProcessRecord app;
    public final int pid;
    public final int uid;
    BroadcastRecord curBroadcast = null;
    boolean linkedToDeath = false;
    String stringName;
    ReceiverList(ActivityManagerService _owner, ProcessRecord _app,
            int _pid, int _uid, IIntentReceiver _receiver) {
        owner = _owner;
        receiver = _receiver;
        app = _app;
        pid = _pid;
        uid = _uid;
    }
    public boolean equals(Object o) {
        return this == o;
    }
    public int hashCode() {
        return System.identityHashCode(this);
    }
    public void binderDied() {
        linkedToDeath = false;
        owner.unregisterReceiver(receiver);
    }
    void dumpLocal(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("app="); pw.print(app);
            pw.print(" pid="); pw.print(pid); pw.print(" uid="); pw.println(uid);
        if (curBroadcast != null || linkedToDeath) {
            pw.print(prefix); pw.print("curBroadcast="); pw.print(curBroadcast);
                pw.print(" linkedToDeath="); pw.println(linkedToDeath);
        }
    }
    void dump(PrintWriter pw, String prefix) {
        Printer pr = new PrintWriterPrinter(pw);
        dumpLocal(pw, prefix);
        String p2 = prefix + "  ";
        final int N = size();
        for (int i=0; i<N; i++) {
            BroadcastFilter bf = get(i);
            pw.print(prefix); pw.print("Filter #"); pw.print(i);
                    pw.print(": BroadcastFilter{");
                    pw.print(Integer.toHexString(System.identityHashCode(bf)));
                    pw.println('}');
            bf.dumpInReceiverList(pw, pr, p2);
        }
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ReceiverList{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(pid);
        sb.append(' ');
        sb.append((app != null ? app.processName : "(unknown name)"));
        sb.append('/');
        sb.append(uid);
        sb.append((receiver.asBinder() instanceof Binder) ? " local:" : " remote:");
        sb.append(Integer.toHexString(System.identityHashCode(receiver.asBinder())));
        sb.append('}');
        return stringName = sb.toString();
    }
}
