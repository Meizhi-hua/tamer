public class SF2Region {
    public final static int GENERATOR_STARTADDRSOFFSET = 0;
    public final static int GENERATOR_ENDADDRSOFFSET = 1;
    public final static int GENERATOR_STARTLOOPADDRSOFFSET = 2;
    public final static int GENERATOR_ENDLOOPADDRSOFFSET = 3;
    public final static int GENERATOR_STARTADDRSCOARSEOFFSET = 4;
    public final static int GENERATOR_MODLFOTOPITCH = 5;
    public final static int GENERATOR_VIBLFOTOPITCH = 6;
    public final static int GENERATOR_MODENVTOPITCH = 7;
    public final static int GENERATOR_INITIALFILTERFC = 8;
    public final static int GENERATOR_INITIALFILTERQ = 9;
    public final static int GENERATOR_MODLFOTOFILTERFC = 10;
    public final static int GENERATOR_MODENVTOFILTERFC = 11;
    public final static int GENERATOR_ENDADDRSCOARSEOFFSET = 12;
    public final static int GENERATOR_MODLFOTOVOLUME = 13;
    public final static int GENERATOR_UNUSED1 = 14;
    public final static int GENERATOR_CHORUSEFFECTSSEND = 15;
    public final static int GENERATOR_REVERBEFFECTSSEND = 16;
    public final static int GENERATOR_PAN = 17;
    public final static int GENERATOR_UNUSED2 = 18;
    public final static int GENERATOR_UNUSED3 = 19;
    public final static int GENERATOR_UNUSED4 = 20;
    public final static int GENERATOR_DELAYMODLFO = 21;
    public final static int GENERATOR_FREQMODLFO = 22;
    public final static int GENERATOR_DELAYVIBLFO = 23;
    public final static int GENERATOR_FREQVIBLFO = 24;
    public final static int GENERATOR_DELAYMODENV = 25;
    public final static int GENERATOR_ATTACKMODENV = 26;
    public final static int GENERATOR_HOLDMODENV = 27;
    public final static int GENERATOR_DECAYMODENV = 28;
    public final static int GENERATOR_SUSTAINMODENV = 29;
    public final static int GENERATOR_RELEASEMODENV = 30;
    public final static int GENERATOR_KEYNUMTOMODENVHOLD = 31;
    public final static int GENERATOR_KEYNUMTOMODENVDECAY = 32;
    public final static int GENERATOR_DELAYVOLENV = 33;
    public final static int GENERATOR_ATTACKVOLENV = 34;
    public final static int GENERATOR_HOLDVOLENV = 35;
    public final static int GENERATOR_DECAYVOLENV = 36;
    public final static int GENERATOR_SUSTAINVOLENV = 37;
    public final static int GENERATOR_RELEASEVOLENV = 38;
    public final static int GENERATOR_KEYNUMTOVOLENVHOLD = 39;
    public final static int GENERATOR_KEYNUMTOVOLENVDECAY = 40;
    public final static int GENERATOR_INSTRUMENT = 41;
    public final static int GENERATOR_RESERVED1 = 42;
    public final static int GENERATOR_KEYRANGE = 43;
    public final static int GENERATOR_VELRANGE = 44;
    public final static int GENERATOR_STARTLOOPADDRSCOARSEOFFSET = 45;
    public final static int GENERATOR_KEYNUM = 46;
    public final static int GENERATOR_VELOCITY = 47;
    public final static int GENERATOR_INITIALATTENUATION = 48;
    public final static int GENERATOR_RESERVED2 = 49;
    public final static int GENERATOR_ENDLOOPADDRSCOARSEOFFSET = 50;
    public final static int GENERATOR_COARSETUNE = 51;
    public final static int GENERATOR_FINETUNE = 52;
    public final static int GENERATOR_SAMPLEID = 53;
    public final static int GENERATOR_SAMPLEMODES = 54;
    public final static int GENERATOR_RESERVED3 = 55;
    public final static int GENERATOR_SCALETUNING = 56;
    public final static int GENERATOR_EXCLUSIVECLASS = 57;
    public final static int GENERATOR_OVERRIDINGROOTKEY = 58;
    public final static int GENERATOR_UNUSED5 = 59;
    public final static int GENERATOR_ENDOPR = 60;
    protected Map<Integer, Short> generators = new HashMap<Integer, Short>();
    protected List<SF2Modulator> modulators = new ArrayList<SF2Modulator>();
    public Map<Integer, Short> getGenerators() {
        return generators;
    }
    public boolean contains(int generator) {
        return generators.containsKey(generator);
    }
    static public short getDefaultValue(int generator) {
        if (generator == 8) return (short)13500;
        if (generator == 21) return (short)-12000;
        if (generator == 23) return (short)-12000;
        if (generator == 25) return (short)-12000;
        if (generator == 26) return (short)-12000;
        if (generator == 27) return (short)-12000;
        if (generator == 28) return (short)-12000;
        if (generator == 30) return (short)-12000;
        if (generator == 33) return (short)-12000;
        if (generator == 34) return (short)-12000;
        if (generator == 35) return (short)-12000;
        if (generator == 36) return (short)-12000;
        if (generator == 38) return (short)-12000;
        if (generator == 43) return (short)0x7F00;
        if (generator == 44) return (short)0x7F00;
        if (generator == 46) return (short)-1;
        if (generator == 47) return (short)-1;
        if (generator == 56) return (short)100;
        if (generator == 58) return (short)-1;
        return 0;
    }
    public short getShort(int generator) {
        if (!contains(generator))
            return getDefaultValue(generator);
        return generators.get(generator);
    }
    public void putShort(int generator, short value) {
        generators.put(generator, value);
    }
    public byte[] getBytes(int generator) {
        int val = getInteger(generator);
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (0xFF & val);
        bytes[1] = (byte) ((0xFF00 & val) >> 8);
        return bytes;
    }
    public void putBytes(int generator, byte[] bytes) {
        generators.put(generator, (short) (bytes[0] + (bytes[1] << 8)));
    }
    public int getInteger(int generator) {
        return 0xFFFF & getShort(generator);
    }
    public void putInteger(int generator, int value) {
        generators.put(generator, (short) value);
    }
    public List<SF2Modulator> getModulators() {
        return modulators;
    }
}
