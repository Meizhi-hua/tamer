public final class Normalizer {
    public static enum Form {
        NFD,
        NFC,
        NFKD,
        NFKC;
    }
    public static boolean isNormalized(CharSequence src, Form form) {
        return NativeNormalizer.isNormalized(src, form);
    }
    public static String normalize(CharSequence src, Form form) {
        return NativeNormalizer.normalize(src, form);
    }
    private Normalizer() {}
}
