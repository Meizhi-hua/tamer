final class CardImpl extends Card {
    private static enum State { OK, REMOVED, DISCONNECTED };
    private final TerminalImpl terminal;
    final long cardId;
    private final ATR atr;
    final int protocol;
    private final ChannelImpl basicChannel;
    private volatile State state;
    private volatile Thread exclusiveThread;
    CardImpl(TerminalImpl terminal, String protocol) throws PCSCException {
        this.terminal = terminal;
        int sharingMode = SCARD_SHARE_SHARED;
        int connectProtocol;
        if (protocol.equals("*")) {
            connectProtocol = SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1;
        } else if (protocol.equalsIgnoreCase("T=0")) {
            connectProtocol = SCARD_PROTOCOL_T0;
        } else if (protocol.equalsIgnoreCase("T=1")) {
            connectProtocol = SCARD_PROTOCOL_T1;
        } else if (protocol.equalsIgnoreCase("direct")) {
            connectProtocol = 0;
            sharingMode = SCARD_SHARE_DIRECT;
        } else {
            throw new IllegalArgumentException("Unsupported protocol " + protocol);
        }
        cardId = SCardConnect(terminal.contextId, terminal.name,
                    sharingMode, connectProtocol);
        byte[] status = new byte[2];
        byte[] atrBytes = SCardStatus(cardId, status);
        atr = new ATR(atrBytes);
        this.protocol = status[1] & 0xff;
        basicChannel = new ChannelImpl(this, 0);
        state = State.OK;
    }
    void checkState()  {
        State s = state;
        if (s == State.DISCONNECTED) {
            throw new IllegalStateException("Card has been disconnected");
        } else if (s == State.REMOVED) {
            throw new IllegalStateException("Card has been removed");
        }
    }
    boolean isValid() {
        if (state != State.OK) {
            return false;
        }
        try {
            SCardStatus(cardId, new byte[2]);
            return true;
        } catch (PCSCException e) {
            state = State.REMOVED;
            return false;
        }
    }
    private void checkSecurity(String action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new CardPermission(terminal.name, action));
        }
    }
    void handleError(PCSCException e) {
        if (e.code == SCARD_W_REMOVED_CARD) {
            state = State.REMOVED;
        }
    }
    public ATR getATR() {
        return atr;
    }
    public String getProtocol() {
        switch (protocol) {
        case SCARD_PROTOCOL_T0:
            return "T=0";
        case SCARD_PROTOCOL_T1:
            return "T=1";
        default:
            return "Unknown protocol " + protocol;
        }
    }
    public CardChannel getBasicChannel() {
        checkSecurity("getBasicChannel");
        checkState();
        return basicChannel;
    }
    private static int getSW(byte[] b) {
        if (b.length < 2) {
            return -1;
        }
        int sw1 = b[b.length - 2] & 0xff;
        int sw2 = b[b.length - 1] & 0xff;
        return (sw1 << 8) | sw2;
    }
    private static byte[] commandOpenChannel = new byte[] {0, 0x70, 0, 0, 1};
    public CardChannel openLogicalChannel() throws CardException {
        checkSecurity("openLogicalChannel");
        checkState();
        checkExclusive();
        try {
            byte[] response = SCardTransmit
                (cardId, protocol, commandOpenChannel, 0, commandOpenChannel.length);
            if ((response.length != 3) || (getSW(response) != 0x9000)) {
                throw new CardException
                        ("openLogicalChannel() failed, card response: "
                        + PCSC.toString(response));
            }
            return new ChannelImpl(this, response[0]);
        } catch (PCSCException e) {
            handleError(e);
            throw new CardException("openLogicalChannel() failed", e);
        }
    }
    void checkExclusive() throws CardException {
        Thread t = exclusiveThread;
        if (t == null) {
            return;
        }
        if (t != Thread.currentThread()) {
            throw new CardException("Exclusive access established by another Thread");
        }
    }
    public synchronized void beginExclusive() throws CardException {
        checkSecurity("exclusive");
        checkState();
        if (exclusiveThread != null) {
            throw new CardException
                    ("Exclusive access has already been assigned to Thread "
                    + exclusiveThread.getName());
        }
        try {
            SCardBeginTransaction(cardId);
        } catch (PCSCException e) {
            handleError(e);
            throw new CardException("beginExclusive() failed", e);
        }
        exclusiveThread = Thread.currentThread();
    }
    public synchronized void endExclusive() throws CardException {
        checkState();
        if (exclusiveThread != Thread.currentThread()) {
            throw new IllegalStateException
                    ("Exclusive access not assigned to current Thread");
        }
        try {
            SCardEndTransaction(cardId, SCARD_LEAVE_CARD);
        } catch (PCSCException e) {
            handleError(e);
            throw new CardException("endExclusive() failed", e);
        } finally {
            exclusiveThread = null;
        }
    }
    public byte[] transmitControlCommand(int controlCode, byte[] command)
            throws CardException {
        checkSecurity("transmitControl");
        checkState();
        checkExclusive();
        if (command == null) {
            throw new NullPointerException();
        }
        try {
            byte[] r = SCardControl(cardId, controlCode, command);
            return r;
        } catch (PCSCException e) {
            handleError(e);
            throw new CardException("transmitControlCommand() failed", e);
        }
    }
    public void disconnect(boolean reset) throws CardException {
        if (reset) {
            checkSecurity("reset");
        }
        if (state != State.OK) {
            return;
        }
        checkExclusive();
        try {
            SCardDisconnect(cardId, (reset ? SCARD_LEAVE_CARD : SCARD_RESET_CARD));
        } catch (PCSCException e) {
            throw new CardException("disconnect() failed", e);
        } finally {
            state = State.DISCONNECTED;
            exclusiveThread = null;
        }
    }
    public String toString() {
        return "PC/SC card in " + terminal.getName()
            + ", protocol " + getProtocol() + ", state " + state;
    }
    protected void finalize() throws Throwable {
        try {
            if (state == State.OK) {
                SCardDisconnect(cardId, SCARD_LEAVE_CARD);
            }
        } finally {
            super.finalize();
        }
    }
}
