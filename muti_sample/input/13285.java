public class Flags {
    private Flags() {} 
    public static String toString(long flags) {
        StringBuilder buf = new StringBuilder();
        String sep = "";
        for (Flag s : asFlagSet(flags)) {
            buf.append(sep);
            buf.append(s);
            sep = " ";
        }
        return buf.toString();
    }
    public static EnumSet<Flag> asFlagSet(long mask) {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if ((mask&PUBLIC) != 0) flags.add(Flag.PUBLIC);
        if ((mask&PRIVATE) != 0) flags.add(Flag.PRIVATE);
        if ((mask&PROTECTED) != 0) flags.add(Flag.PROTECTED);
        if ((mask&STATIC) != 0) flags.add(Flag.STATIC);
        if ((mask&FINAL) != 0) flags.add(Flag.FINAL);
        if ((mask&SYNCHRONIZED) != 0) flags.add(Flag.SYNCHRONIZED);
        if ((mask&VOLATILE) != 0) flags.add(Flag.VOLATILE);
        if ((mask&TRANSIENT) != 0) flags.add(Flag.TRANSIENT);
        if ((mask&NATIVE) != 0) flags.add(Flag.NATIVE);
        if ((mask&INTERFACE) != 0) flags.add(Flag.INTERFACE);
        if ((mask&ABSTRACT) != 0) flags.add(Flag.ABSTRACT);
        if ((mask&STRICTFP) != 0) flags.add(Flag.STRICTFP);
        if ((mask&BRIDGE) != 0) flags.add(Flag.BRIDGE);
        if ((mask&SYNTHETIC) != 0) flags.add(Flag.SYNTHETIC);
        if ((mask&DEPRECATED) != 0) flags.add(Flag.DEPRECATED);
        if ((mask&HASINIT) != 0) flags.add(Flag.HASINIT);
        if ((mask&ENUM) != 0) flags.add(Flag.ENUM);
        if ((mask&IPROXY) != 0) flags.add(Flag.IPROXY);
        if ((mask&NOOUTERTHIS) != 0) flags.add(Flag.NOOUTERTHIS);
        if ((mask&EXISTS) != 0) flags.add(Flag.EXISTS);
        if ((mask&COMPOUND) != 0) flags.add(Flag.COMPOUND);
        if ((mask&CLASS_SEEN) != 0) flags.add(Flag.CLASS_SEEN);
        if ((mask&SOURCE_SEEN) != 0) flags.add(Flag.SOURCE_SEEN);
        if ((mask&LOCKED) != 0) flags.add(Flag.LOCKED);
        if ((mask&UNATTRIBUTED) != 0) flags.add(Flag.UNATTRIBUTED);
        if ((mask&ANONCONSTR) != 0) flags.add(Flag.ANONCONSTR);
        if ((mask&ACYCLIC) != 0) flags.add(Flag.ACYCLIC);
        if ((mask&PARAMETER) != 0) flags.add(Flag.PARAMETER);
        if ((mask&VARARGS) != 0) flags.add(Flag.VARARGS);
        return flags;
    }
    public static final int PUBLIC       = 1<<0;
    public static final int PRIVATE      = 1<<1;
    public static final int PROTECTED    = 1<<2;
    public static final int STATIC       = 1<<3;
    public static final int FINAL        = 1<<4;
    public static final int SYNCHRONIZED = 1<<5;
    public static final int VOLATILE     = 1<<6;
    public static final int TRANSIENT    = 1<<7;
    public static final int NATIVE       = 1<<8;
    public static final int INTERFACE    = 1<<9;
    public static final int ABSTRACT     = 1<<10;
    public static final int STRICTFP     = 1<<11;
    public static final int SYNTHETIC    = 1<<12;
    public static final int ANNOTATION   = 1<<13;
    public static final int ENUM         = 1<<14;
    public static final int StandardFlags = 0x0fff;
    public static final int ModifierFlags = StandardFlags & ~INTERFACE;
    public static final int ACC_SUPER    = 0x0020;
    public static final int ACC_BRIDGE   = 0x0040;
    public static final int ACC_VARARGS  = 0x0080;
    public static final int DEPRECATED   = 1<<17;
    public static final int HASINIT          = 1<<18;
    public static final int BLOCK            = 1<<20;
    public static final int IPROXY           = 1<<21;
    public static final int NOOUTERTHIS  = 1<<22;
    public static final int EXISTS           = 1<<23;
    public static final int COMPOUND     = 1<<24;
    public static final int CLASS_SEEN   = 1<<25;
    public static final int SOURCE_SEEN  = 1<<26;
    public static final int LOCKED           = 1<<27;
    public static final int UNATTRIBUTED = 1<<28;
    public static final int ANONCONSTR   = 1<<29;
    public static final int ACYCLIC          = 1<<30;
    public static final long BRIDGE          = 1L<<31;
    public static final long PARAMETER   = 1L<<33;
    public static final long VARARGS   = 1L<<34;
    public static final long ACYCLIC_ANN      = 1L<<35;
    public static final long GENERATEDCONSTR   = 1L<<36;
    public static final long HYPOTHETICAL   = 1L<<37;
    public static final long PROPRIETARY = 1L<<38;
    public static final long UNION = 1L<<39;
    public static final long POLYMORPHIC_SIGNATURE = 1L<<40;
    public static final long OVERRIDE_BRIDGE = 1L<<41;
    public static final long EFFECTIVELY_FINAL = 1L<<42;
    public static final long CLASH = 1L<<43;
    public static final int
        AccessFlags           = PUBLIC | PROTECTED | PRIVATE,
        LocalClassFlags       = FINAL | ABSTRACT | STRICTFP | ENUM | SYNTHETIC,
        MemberClassFlags      = LocalClassFlags | INTERFACE | AccessFlags,
        ClassFlags            = LocalClassFlags | INTERFACE | PUBLIC | ANNOTATION,
        InterfaceVarFlags     = FINAL | STATIC | PUBLIC,
        VarFlags              = AccessFlags | FINAL | STATIC |
                                VOLATILE | TRANSIENT | ENUM,
        ConstructorFlags      = AccessFlags,
        InterfaceMethodFlags  = ABSTRACT | PUBLIC,
        MethodFlags           = AccessFlags | ABSTRACT | STATIC | NATIVE |
                                SYNCHRONIZED | FINAL | STRICTFP;
    public static final long
        LocalVarFlags         = FINAL | PARAMETER;
    public static Set<Modifier> asModifierSet(long flags) {
        Set<Modifier> modifiers = modifierSets.get(flags);
        if (modifiers == null) {
            modifiers = java.util.EnumSet.noneOf(Modifier.class);
            if (0 != (flags & PUBLIC))    modifiers.add(Modifier.PUBLIC);
            if (0 != (flags & PROTECTED)) modifiers.add(Modifier.PROTECTED);
            if (0 != (flags & PRIVATE))   modifiers.add(Modifier.PRIVATE);
            if (0 != (flags & ABSTRACT))  modifiers.add(Modifier.ABSTRACT);
            if (0 != (flags & STATIC))    modifiers.add(Modifier.STATIC);
            if (0 != (flags & FINAL))     modifiers.add(Modifier.FINAL);
            if (0 != (flags & TRANSIENT)) modifiers.add(Modifier.TRANSIENT);
            if (0 != (flags & VOLATILE))  modifiers.add(Modifier.VOLATILE);
            if (0 != (flags & SYNCHRONIZED))
                                          modifiers.add(Modifier.SYNCHRONIZED);
            if (0 != (flags & NATIVE))    modifiers.add(Modifier.NATIVE);
            if (0 != (flags & STRICTFP))  modifiers.add(Modifier.STRICTFP);
            modifiers = Collections.unmodifiableSet(modifiers);
            modifierSets.put(flags, modifiers);
        }
        return modifiers;
    }
    private static Map<Long, Set<Modifier>> modifierSets =
        new java.util.concurrent.ConcurrentHashMap<Long, Set<Modifier>>(64);
    public static boolean isStatic(Symbol symbol) {
        return (symbol.flags() & STATIC) != 0;
    }
    public static boolean isEnum(Symbol symbol) {
        return (symbol.flags() & ENUM) != 0;
    }
    public static boolean isConstant(Symbol.VarSymbol symbol) {
        return symbol.getConstValue() != null;
    }
    public enum Flag {
        PUBLIC("public"),
        PRIVATE("private"),
        PROTECTED("protected"),
        STATIC("static"),
        FINAL("final"),
        SYNCHRONIZED("synchronized"),
        VOLATILE("volatile"),
        TRANSIENT("transient"),
        NATIVE("native"),
        INTERFACE("interface"),
        ABSTRACT("abstract"),
        STRICTFP("strictfp"),
        BRIDGE("bridge"),
        SYNTHETIC("synthetic"),
        DEPRECATED("deprecated"),
        HASINIT("hasinit"),
        ENUM("enum"),
        IPROXY("iproxy"),
        NOOUTERTHIS("noouterthis"),
        EXISTS("exists"),
        COMPOUND("compound"),
        CLASS_SEEN("class_seen"),
        SOURCE_SEEN("source_seen"),
        LOCKED("locked"),
        UNATTRIBUTED("unattributed"),
        ANONCONSTR("anonconstr"),
        ACYCLIC("acyclic"),
        PARAMETER("parameter"),
        VARARGS("varargs"),
        PACKAGE("package");
        String name;
        Flag(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
}
