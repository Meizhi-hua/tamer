public final class CoreMetrics {
    public CoreMetrics(float ascent,
                       float descent,
                       float leading,
                       float height,
                       int baselineIndex,
                       float[] baselineOffsets,
                       float strikethroughOffset,
                       float strikethroughThickness,
                       float underlineOffset,
                       float underlineThickness,
                       float ssOffset,
                       float italicAngle) {
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.height = height;
        this.baselineIndex = baselineIndex;
        this.baselineOffsets = baselineOffsets;
        this.strikethroughOffset = strikethroughOffset;
        this.strikethroughThickness = strikethroughThickness;
        this.underlineOffset = underlineOffset;
        this.underlineThickness = underlineThickness;
        this.ssOffset = ssOffset;
        this.italicAngle = italicAngle;
    }
    public static CoreMetrics get(LineMetrics lm) {
        return ((FontLineMetrics)lm).cm;
    }
    public final int hashCode() {
        return Float.floatToIntBits(ascent + ssOffset);
    }
    public final boolean equals(Object rhs) {
        try {
            return equals((CoreMetrics)rhs);
        }
        catch(ClassCastException e) {
            return false;
        }
    }
    public final boolean equals(CoreMetrics rhs) {
        if (rhs != null) {
            if (this == rhs) {
                return true;
            }
            return ascent == rhs.ascent
                && descent == rhs.descent
                && leading == rhs.leading
                && baselineIndex == rhs.baselineIndex
                && baselineOffsets[0] == rhs.baselineOffsets[0]
                && baselineOffsets[1] == rhs.baselineOffsets[1]
                && baselineOffsets[2] == rhs.baselineOffsets[2]
                && strikethroughOffset == rhs.strikethroughOffset
                && strikethroughThickness == rhs.strikethroughThickness
                && underlineOffset == rhs.underlineOffset
                && underlineThickness == rhs.underlineThickness
                && ssOffset == rhs.ssOffset
                && italicAngle == rhs.italicAngle;
        }
        return false;
    }
    public final float effectiveBaselineOffset(float[] fullOffsets) {
        switch (baselineIndex) {
        case GraphicAttribute.TOP_ALIGNMENT:
            return fullOffsets[4] + ascent;
        case GraphicAttribute.BOTTOM_ALIGNMENT:
            return fullOffsets[3] - descent;
        default:
            return fullOffsets[baselineIndex];
        }
    }
    public final float   ascent;
    public final float   descent;
    public final float   leading;
    public final float   height;
    public final int     baselineIndex;
    public final float[] baselineOffsets; 
    public final float   strikethroughOffset;
    public final float   strikethroughThickness;
    public final float   underlineOffset;
    public final float   underlineThickness;
    public final float   ssOffset;
    public final float   italicAngle;
}
