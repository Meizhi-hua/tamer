public class Send_ResetAllControllers {
    public static boolean[] dontResetControls = new boolean[128];
    static {
        for (int i = 0; i < dontResetControls.length; i++)
            dontResetControls[i] = false;
        dontResetControls[0] = true;   
        dontResetControls[32] = true;  
        dontResetControls[7] = true;   
        dontResetControls[8] = true;   
        dontResetControls[10] = true;  
        dontResetControls[11] = true;  
        dontResetControls[91] = true;  
        dontResetControls[92] = true;  
        dontResetControls[93] = true;  
        dontResetControls[94] = true;  
        dontResetControls[95] = true;  
        dontResetControls[70] = true;  
        dontResetControls[71] = true;  
        dontResetControls[72] = true;  
        dontResetControls[73] = true;  
        dontResetControls[74] = true;  
        dontResetControls[75] = true;  
        dontResetControls[76] = true;  
        dontResetControls[77] = true;  
        dontResetControls[78] = true;  
        dontResetControls[79] = true;  
        dontResetControls[120] = true; 
        dontResetControls[121] = true; 
        dontResetControls[122] = true; 
        dontResetControls[123] = true; 
        dontResetControls[124] = true; 
        dontResetControls[125] = true; 
        dontResetControls[126] = true; 
        dontResetControls[127] = true; 
        dontResetControls[6] = true;   
        dontResetControls[38] = true;  
        dontResetControls[96] = true;  
        dontResetControls[97] = true;  
        dontResetControls[98] = true;  
        dontResetControls[99] = true;  
        dontResetControls[100] = true; 
        dontResetControls[101] = true; 
    }
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
        SoftTestUtils soft = new SoftTestUtils();
        MidiChannel channel = soft.synth.getChannels()[0];
        Receiver receiver = soft.synth.getReceiver();
        for (int i = 0; i < 128; i++)
            channel.setPolyPressure(i, 10);
        channel.setChannelPressure(10);
        channel.setPitchBend(2192);
        for (int i = 0; i < 120; i++)
            channel.controlChange(i, 1);
        ShortMessage smsg = new ShortMessage();
        smsg.setMessage(ShortMessage.CONTROL_CHANGE,0, 121,0);
        receiver.send(smsg, -1);
        for (int i = 0; i < 128; i++)
            assertEquals(channel.getPolyPressure(i), 0);
        assertEquals(channel.getChannelPressure(), 0);
        assertEquals(channel.getPitchBend(),8192);
        for (int i = 0; i < 120; i++)
            if(!dontResetControls[i])
                assertEquals(channel.getController(i), 0);
        assertEquals(channel.getController(71), 64); 
        assertEquals(channel.getController(72), 64); 
        assertEquals(channel.getController(73), 64); 
        assertEquals(channel.getController(74), 64); 
        assertEquals(channel.getController(75), 64); 
        assertEquals(channel.getController(76), 64); 
        assertEquals(channel.getController(77), 64); 
        assertEquals(channel.getController(78), 64); 
        assertEquals(channel.getController(8), 64); 
        assertEquals(channel.getController(11), 127); 
        assertEquals(channel.getController(98), 127); 
        assertEquals(channel.getController(99), 127); 
        assertEquals(channel.getController(100), 127); 
        assertEquals(channel.getController(101), 127); 
        soft.close();
    }
}
