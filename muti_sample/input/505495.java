public class ChoiceFormat extends NumberFormat {
    private static final long serialVersionUID = 1795184449645032964L;
    private double[] choiceLimits;
    private String[] choiceFormats;
    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }
    public ChoiceFormat(String template) {
        applyPattern(template);
    }
    public void applyPattern(String template) {
        double[] limits = new double[5];
        List<String> formats = new ArrayList<String>();
        int length = template.length(), limitCount = 0, index = 0;
        StringBuffer buffer = new StringBuffer();
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        ParsePosition position = new ParsePosition(0);
        while (true) {
            index = skipWhitespace(template, index);
            if (index >= length) {
                if (limitCount == limits.length) {
                    choiceLimits = limits;
                } else {
                    choiceLimits = new double[limitCount];
                    System.arraycopy(limits, 0, choiceLimits, 0, limitCount);
                }
                choiceFormats = new String[formats.size()];
                for (int i = 0; i < formats.size(); i++) {
                    choiceFormats[i] = formats.get(i);
                }
                return;
            }
            position.setIndex(index);
            Number value = format.parse(template, position);
            index = skipWhitespace(template, position.getIndex());
            if (position.getErrorIndex() != -1 || index >= length) {
                choiceLimits = new double[0];
                choiceFormats = new String[0];
                return;
            }
            char ch = template.charAt(index++);
            if (limitCount == limits.length) {
                double[] newLimits = new double[limitCount * 2];
                System.arraycopy(limits, 0, newLimits, 0, limitCount);
                limits = newLimits;
            }
            double next;
            switch (ch) {
                case '#':
                case '\u2264':
                    next = value.doubleValue();
                    break;
                case '<':
                    next = nextDouble(value.doubleValue());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            if (limitCount > 0 && next <= limits[limitCount - 1]) {
                throw new IllegalArgumentException();
            }
            buffer.setLength(0);
            position.setIndex(index);
            upTo(template, position, buffer, '|');
            index = position.getIndex();
            limits[limitCount++] = next;
            formats.add(buffer.toString());
        }
    }
    @Override
    public Object clone() {
        ChoiceFormat clone = (ChoiceFormat) super.clone();
        clone.choiceLimits = choiceLimits.clone();
        clone.choiceFormats = choiceFormats.clone();
        return clone;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChoiceFormat)) {
            return false;
        }
        ChoiceFormat choice = (ChoiceFormat) object;
        return Arrays.equals(choiceLimits, choice.choiceLimits)
                && Arrays.equals(choiceFormats, choice.choiceFormats);
    }
    @Override
    public StringBuffer format(double value, StringBuffer buffer,
            FieldPosition field) {
        for (int i = choiceLimits.length - 1; i >= 0; i--) {
            if (choiceLimits[i] <= value) {
                return buffer.append(choiceFormats[i]);
            }
        }
        return choiceFormats.length == 0 ? buffer : buffer
                .append(choiceFormats[0]);
    }
    @Override
    public StringBuffer format(long value, StringBuffer buffer,
            FieldPosition field) {
        return format((double) value, buffer, field);
    }
    public Object[] getFormats() {
        return choiceFormats;
    }
    public double[] getLimits() {
        return choiceLimits;
    }
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < choiceLimits.length; i++) {
            long v = Double.doubleToLongBits(choiceLimits[i]);
            hashCode += (int) (v ^ (v >>> 32)) + choiceFormats[i].hashCode();
        }
        return hashCode;
    }
    public static final double nextDouble(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return value;
        }
        long bits;
        if (value == 0) {
            bits = 0;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value < 0 ? bits - 1 : bits + 1);
    }
    public static double nextDouble(double value, boolean increment) {
        return increment ? nextDouble(value) : previousDouble(value);
    }
    @Override
    public Number parse(String string, ParsePosition position) {
        int offset = position.getIndex();
        for (int i = 0; i < choiceFormats.length; i++) {
            if (string.startsWith(choiceFormats[i], offset)) {
                position.setIndex(offset + choiceFormats[i].length());
                return new Double(choiceLimits[i]);
            }
        }
        position.setErrorIndex(offset);
        return new Double(Double.NaN);
    }
    public static final double previousDouble(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return value;
        }
        long bits;
        if (value == 0) {
            bits = 0x8000000000000000L;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value <= 0 ? bits + 1 : bits - 1);
    }
    public void setChoices(double[] limits, String[] formats) {
        if (limits.length != formats.length) {
            throw new IllegalArgumentException();
        }
        choiceLimits = limits;
        choiceFormats = formats;
    }
    private int skipWhitespace(String string, int index) {
        int length = string.length();
        while (index < length && Character.isWhitespace(string.charAt(index))) {
            index++;
        }
        return index;
    }
    public String toPattern() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < choiceLimits.length; i++) {
            if (i != 0) {
                buffer.append('|');
            }
            String previous = String.valueOf(previousDouble(choiceLimits[i]));
            String limit = String.valueOf(choiceLimits[i]);
            if (previous.length() < limit.length()) {
                buffer.append(previous);
                buffer.append('<');
            } else {
                buffer.append(limit);
                buffer.append('#');
            }
            boolean quote = (choiceFormats[i].indexOf('|') != -1);
            if (quote) {
                buffer.append('\'');
            }
            buffer.append(choiceFormats[i]);
            if (quote) {
                buffer.append('\'');
            }
        }
        return buffer.toString();
    }
}
