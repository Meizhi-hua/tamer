public class Broadcaster
{
    public Broadcaster()
    {
    }
    public void request(int senderWhat, Handler target, int targetWhat)
    {
        synchronized (this) {
            Registration r = null;
            if (mReg == null) {
                r = new Registration();
                r.senderWhat = senderWhat;
                r.targets = new Handler[1];
                r.targetWhats = new int[1];
                r.targets[0] = target;
                r.targetWhats[0] = targetWhat;
                mReg = r;
                r.next = r;
                r.prev = r;
            } else {
                Registration start = mReg;
                r = start;
                do {
                    if (r.senderWhat >= senderWhat) {
                        break;
                    }
                    r = r.next;
                } while (r != start);
                int n;
                if (r.senderWhat != senderWhat) {
                    Registration reg = new Registration();
                    reg.senderWhat = senderWhat;
                    reg.targets = new Handler[1];
                    reg.targetWhats = new int[1];
                    reg.next = r;
                    reg.prev = r.prev;
                    r.prev.next = reg;
                    r.prev = reg;
                    if (r == mReg && r.senderWhat > reg.senderWhat) {
                        mReg = reg;
                    }
                    r = reg;
                    n = 0;
                } else {
                    n = r.targets.length;
                    Handler[] oldTargets = r.targets;
                    int[] oldWhats = r.targetWhats;
                    for (int i=0; i<n; i++) {
                        if (oldTargets[i] == target && oldWhats[i] == targetWhat) {
                            return;
                        }
                    }
                    r.targets = new Handler[n+1];
                    System.arraycopy(oldTargets, 0, r.targets, 0, n);
                    r.targetWhats = new int[n+1];
                    System.arraycopy(oldWhats, 0, r.targetWhats, 0, n);
                }
                r.targets[n] = target;
                r.targetWhats[n] = targetWhat;
            }
        }
    }
    public void cancelRequest(int senderWhat, Handler target, int targetWhat)
    {
        synchronized (this) {
            Registration start = mReg;
            Registration r = start;
            if (r == null) {
                return;
            }
            do {
                if (r.senderWhat >= senderWhat) {
                    break;
                }
                r = r.next;
            } while (r != start);
            if (r.senderWhat == senderWhat) {
                Handler[] targets = r.targets;
                int[] whats = r.targetWhats;
                int oldLen = targets.length;
                for (int i=0; i<oldLen; i++) {
                    if (targets[i] == target && whats[i] == targetWhat) {
                        r.targets = new Handler[oldLen-1];
                        r.targetWhats = new int[oldLen-1];
                        if (i > 0) {
                            System.arraycopy(targets, 0, r.targets, 0, i);
                            System.arraycopy(whats, 0, r.targetWhats, 0, i);
                        }
                        int remainingLen = oldLen-i-1;
                        if (remainingLen != 0) {
                            System.arraycopy(targets, i+1, r.targets, i,
                                    remainingLen);
                            System.arraycopy(whats, i+1, r.targetWhats, i,
                                    remainingLen);
                        }
                        break;
                    }
                }
            }
        }
    }
    public void dumpRegistrations()
    {
        synchronized (this) {
            Registration start = mReg;
            System.out.println("Broadcaster " + this + " {");
            if (start != null) {
                Registration r = start;
                do {
                    System.out.println("    senderWhat=" + r.senderWhat);
                    int n = r.targets.length;
                    for (int i=0; i<n; i++) {
                        System.out.println("        [" + r.targetWhats[i]
                                        + "] " + r.targets[i]);
                    }
                    r = r.next;
                } while (r != start);
            }
            System.out.println("}");
        }
    }
    public void broadcast(Message msg)
    {
        synchronized (this) {
        	if (mReg == null) {
        		return;
        	}
            int senderWhat = msg.what;
            Registration start = mReg;
            Registration r = start;
            do {
                if (r.senderWhat >= senderWhat) {
                    break;
                }
                r = r.next;
            } while (r != start);
            if (r.senderWhat == senderWhat) {
                Handler[] targets = r.targets;
                int[] whats = r.targetWhats;
                int n = targets.length;
                for (int i=0; i<n; i++) {
                    Handler target = targets[i];
                    Message m = Message.obtain();
                    m.copyFrom(msg);
                    m.what = whats[i];
                    target.sendMessage(m);
                }
            }
        }
    }
    private class Registration
    {
        Registration next;
        Registration prev;
        int senderWhat;
        Handler[] targets;
        int[] targetWhats;
    }
    private Registration mReg;
}
