public abstract class Card {
    protected Card() {
    }
    public abstract ATR getATR();
    public abstract String getProtocol();
    public abstract CardChannel getBasicChannel();
    public abstract CardChannel openLogicalChannel() throws CardException;
    public abstract void beginExclusive() throws CardException;
    public abstract void endExclusive() throws CardException;
    public abstract byte[] transmitControlCommand(int controlCode,
            byte[] command) throws CardException;
    public abstract void disconnect(boolean reset) throws CardException;
}
