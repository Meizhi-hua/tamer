public final class ByteArrayAnnotatedOutput
        implements AnnotatedOutput {
    private static final int DEFAULT_SIZE = 1000;
    private final boolean stretchy;
    private byte[] data;
    private int cursor;
    private boolean verbose;
    private ArrayList<Annotation> annotations;
    private int annotationWidth;
    private int hexCols;
    public ByteArrayAnnotatedOutput(byte[] data) {
        this(data, false);
    }
    public ByteArrayAnnotatedOutput() {
        this(new byte[DEFAULT_SIZE], true);
    }
    private ByteArrayAnnotatedOutput(byte[] data, boolean stretchy) {
        if (data == null) {
            throw new NullPointerException("data == null");
        }
        this.stretchy = stretchy;
        this.data = data;
        this.cursor = 0;
        this.verbose = false;
        this.annotations = null;
        this.annotationWidth = 0;
        this.hexCols = 0;
    }
    public byte[] getArray() {
        return data;
    }
    public byte[] toByteArray() {
        byte[] result = new byte[cursor];
        System.arraycopy(data, 0, result, 0, cursor);
        return result;
    }
    public int getCursor() {
        return cursor;
    }
    public void assertCursor(int expectedCursor) {
        if (cursor != expectedCursor) {
            throw new ExceptionWithContext("expected cursor " +
                    expectedCursor + "; actual value: " + cursor);
        }
    }
    public void writeByte(int value) {
        int writeAt = cursor;
        int end = writeAt + 1;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        data[writeAt] = (byte) value;
        cursor = end;
    }
    public void writeShort(int value) {
        int writeAt = cursor;
        int end = writeAt + 2;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        data[writeAt] = (byte) value;
        data[writeAt + 1] = (byte) (value >> 8);
        cursor = end;
    }
    public void writeInt(int value) {
        int writeAt = cursor;
        int end = writeAt + 4;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        data[writeAt] = (byte) value;
        data[writeAt + 1] = (byte) (value >> 8);
        data[writeAt + 2] = (byte) (value >> 16);
        data[writeAt + 3] = (byte) (value >> 24);
        cursor = end;
    }
    public void writeLong(long value) {
        int writeAt = cursor;
        int end = writeAt + 8;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        int half = (int) value;
        data[writeAt] = (byte) half;
        data[writeAt + 1] = (byte) (half >> 8);
        data[writeAt + 2] = (byte) (half >> 16);
        data[writeAt + 3] = (byte) (half >> 24);
        half = (int) (value >> 32);
        data[writeAt + 4] = (byte) half;
        data[writeAt + 5] = (byte) (half >> 8);
        data[writeAt + 6] = (byte) (half >> 16);
        data[writeAt + 7] = (byte) (half >> 24);
        cursor = end;
    }
    public int writeUnsignedLeb128(int value) {
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            writeByte((value & 0x7f) | 0x80);
            value = remaining;
            remaining >>= 7;
            count++;
        }
        writeByte(value & 0x7f);
        return count + 1;
    }
    public int writeSignedLeb128(int value) {
        int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                || ((remaining & 1) != ((value >> 6) & 1));
            writeByte((value & 0x7f) | (hasMore ? 0x80 : 0));
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }
    public void write(ByteArray bytes) {
        int blen = bytes.size();
        int writeAt = cursor;
        int end = writeAt + blen;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        bytes.getBytes(data, writeAt);
        cursor = end;
    }
    public void write(byte[] bytes, int offset, int length) {
        int writeAt = cursor;
        int end = writeAt + length;
        int bytesEnd = offset + length;
        if (((offset | length | end) < 0) || (bytesEnd > bytes.length)) {
            throw new IndexOutOfBoundsException("bytes.length " +
                                                bytes.length + "; " + 
                                                offset + "..!" + end);
        }
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        System.arraycopy(bytes, offset, data, writeAt, length);
        cursor = end;
    }
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }
    public void writeZeroes(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        int end = cursor + count;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        cursor = end;
    }
    public void alignTo(int alignment) {
        int mask = alignment - 1;
        if ((alignment < 0) || ((mask & alignment) != 0)) {
            throw new IllegalArgumentException("bogus alignment");
        }
        int end = (cursor + mask) & ~mask;
        if (stretchy) {
            ensureCapacity(end);
        } else if (end > data.length) {
            throwBounds();
            return;
        }
        cursor = end;
    }
    public boolean annotates() {
        return (annotations != null);
    }
    public boolean isVerbose() {
        return verbose;
    }
    public void annotate(String msg) {
        if (annotations == null) {
            return;
        }
        endAnnotation();
        annotations.add(new Annotation(cursor, msg));
    }
    public void annotate(int amt, String msg) {
        if (annotations == null) {
            return;
        }
        endAnnotation();
        int asz = annotations.size();
        int lastEnd = (asz == 0) ? 0 : annotations.get(asz - 1).getEnd();
        int startAt;
        if (lastEnd <= cursor) {
            startAt = cursor;
        } else {
            startAt = lastEnd;
        }
        annotations.add(new Annotation(startAt, startAt + amt, msg));
    }
    public void endAnnotation() {
        if (annotations == null) {
            return;
        }
        int sz = annotations.size();
        if (sz != 0) {
            annotations.get(sz - 1).setEndIfUnset(cursor);
        }
    }
    public int getAnnotationWidth() {
        int leftWidth = 8 + (hexCols * 2) + (hexCols / 2);
        return annotationWidth - leftWidth;
    }
    public void enableAnnotations(int annotationWidth, boolean verbose) {
        if ((annotations != null) || (cursor != 0)) {
            throw new RuntimeException("cannot enable annotations");
        }
        if (annotationWidth < 40) {
            throw new IllegalArgumentException("annotationWidth < 40");
        }
        int hexCols = (((annotationWidth - 7) / 15) + 1) & ~1;
        if (hexCols < 6) {
            hexCols = 6;
        } else if (hexCols > 10) {
            hexCols = 10;
        }
        this.annotations = new ArrayList<Annotation>(1000);
        this.annotationWidth = annotationWidth;
        this.hexCols = hexCols;
        this.verbose = verbose;
    }
    public void finishAnnotating() {
        endAnnotation();
        if (annotations != null) {
            int asz = annotations.size();
            while (asz > 0) {
                Annotation last = annotations.get(asz - 1);
                if (last.getStart() > cursor) {
                    annotations.remove(asz - 1);
                    asz--;
                } else if (last.getEnd() > cursor) {
                    last.setEnd(cursor);
                    break;
                } else {
                    break;
                }
            }
        }
    }
    public void writeAnnotationsTo(Writer out) throws IOException {
        int width2 = getAnnotationWidth();
        int width1 = annotationWidth - width2 - 1;
        TwoColumnOutput twoc = new TwoColumnOutput(out, width1, width2, "|");
        Writer left = twoc.getLeft();
        Writer right = twoc.getRight();
        int leftAt = 0; 
        int rightAt = 0; 
        int rightSz = annotations.size();
        while ((leftAt < cursor) && (rightAt < rightSz)) {
            Annotation a = annotations.get(rightAt);
            int start = a.getStart();
            int end;
            String text;
            if (leftAt < start) {
                end = start;
                start = leftAt;
                text = "";
            } else {
                end = a.getEnd();
                text = a.getText();
                rightAt++;
            }
            left.write(Hex.dump(data, start, end - start, start, hexCols, 6));
            right.write(text);
            twoc.flush();
            leftAt = end;
        }
        if (leftAt < cursor) {
            left.write(Hex.dump(data, leftAt, cursor - leftAt, leftAt,
                                hexCols, 6));
        }
        while (rightAt < rightSz) {
            right.write(annotations.get(rightAt).getText());
            rightAt++;
        }
        twoc.flush();
    }
    private static void throwBounds() {
        throw new IndexOutOfBoundsException("attempt to write past the end");
    }
    private void ensureCapacity(int desiredSize) {
        if (data.length < desiredSize) {
            byte[] newData = new byte[desiredSize * 2 + 1000];
            System.arraycopy(data, 0, newData, 0, cursor);
            data = newData;
        }
    }
    private static class Annotation {
        private final int start;
        private int end;
        private final String text;
        public Annotation(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
        public Annotation(int start, String text) {
            this(start, Integer.MAX_VALUE, text);
        }
        public void setEndIfUnset(int end) {
            if (this.end == Integer.MAX_VALUE) {
                this.end = end;
            }
        }
        public void setEnd(int end) {
            this.end = end;
        }
        public int getStart() {
            return start;
        }
        public int getEnd() {
            return end;
        }
        public String getText() {
            return text;
        }
    }
}
