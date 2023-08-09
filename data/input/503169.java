public class ReasonFlags {
    static final String[] REASONS = {
        "unused", 
        "keyCompromise", 
        "cACompromise", 
        "affiliationChanged", 
        "superseded", 
        "cessationOfOperation", 
        "certificateHold", 
        "privilegeWithdrawn", 
        "aACompromise" 
    };
    private boolean[] flags;
    public ReasonFlags(boolean[] flags) {
        this.flags = flags;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix);
        buffer.append("ReasonFlags [\n"); 
        for (int i=0; i<flags.length; i++) {
            if (flags[i]) {
                buffer.append(prefix).append("  ") 
                    .append(REASONS[i]).append('\n');
            }
        }
        buffer.append(prefix);
        buffer.append("]\n"); 
    }
    public static final ASN1BitString ASN1 = 
                            new ASN1BitString.ASN1NamedBitList(REASONS.length) {
        public Object getDecodedObject(BerInputStream in) throws IOException {
            return new ReasonFlags((boolean[]) super.getDecodedObject(in));
        }
        public void setEncodingContent(BerOutputStream out) {
            out.content = ((ReasonFlags) out.content).flags;
            super.setEncodingContent(out);
        }
    };
}
