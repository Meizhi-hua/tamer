public class GetTransmitter {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.open(new DummySourceDataLine(), null);
        try
        {
            synth.getTransmitter();
            throw new Exception("MidiUnavailableException not thrown!");
        } catch (MidiUnavailableException e) {
        }
        synth.close();
    }
}
