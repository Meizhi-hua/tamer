final class GridQuadFrame {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;
    private FloatBuffer mSecTexCoordBuffer;
    private CharBuffer mIndexBuffer;
    private int mW;
    private int mH;
    public static final int INDEX_COUNT = 25;
    private int mVertBufferIndex;
    private int mIndexBufferIndex;
    private int mTextureCoordBufferIndex;
    private int mSecTextureCoordBufferIndex;
    public static GridQuadFrame createFrame(float width, float height, int itemWidth, int itemHeight) {
        GridQuadFrame frame = new GridQuadFrame();
        final float textureSize = 64.0f;
        final float numPixelsYOriginShift = 7;
        final float inset = 6;
        final float ratio = 1.0f / (float) itemHeight;
        final float frameXThickness = 0.5f * textureSize * ratio;
        final float frameYThickness = 0.5f * textureSize * ratio;
        final float frameX = width * 0.5f + frameXThickness * 0.5f - inset * ratio;
        float frameY = height * 0.5f + frameYThickness * 0.5f + (inset - 1) * ratio;
        final float originX = 0.0f;
        final float originY = numPixelsYOriginShift * ratio;
        frame.set(0, 0, -frameX + originX, -frameY + originY, 0, 1.0f, 1.0f);
        frame.set(1, 0, -frameX + originX + frameXThickness, -frameY + originY, 0, 0.5f, 1.0f);
        frame.set(2, 0, frameX - frameXThickness + originX, -frameY + originY, 0, 0.5f, 1.0f);
        frame.set(3, 0, frameX + originX, -frameY + originY, 0, 0.0f, 1.0f);
        frameY -= frameYThickness;
        frame.set(0, 1, -frameX + originX, -frameY + originY, 0, 1.0f, 0.5f);
        frame.set(1, 1, -frameX + frameXThickness + originX, -frameY + originY, 0, 0.5f, 0.5f);
        frame.set(2, 1, frameX - frameXThickness + originX, -frameY + originY, 0, 0.5f, 0.5f);
        frame.set(3, 1, frameX + originX, -frameY + originY, 0, 0.0f, 0.5f);
        frameY = height * 0.5f - frameYThickness;
        frame.set(0, 2, -frameX + originX, frameY + originY, 0, 1.0f, 0.5f);
        frame.set(1, 2, -frameX + frameXThickness + originX, frameY + originY, 0, 0.5f, 0.5f);
        frame.set(2, 2, frameX - frameXThickness + originX, frameY + originY, 0, 0.5f, 0.5f);
        frame.set(3, 2, frameX + originX, frameY + originY, 0, 0.0f, 0.5f);
        frameY += frameYThickness;
        frame.set(0, 3, -frameX + originX, frameY + originY, 0, 1.0f, 0.0f);
        frame.set(1, 3, -frameX + frameXThickness + originX, frameY + originY, 0, 0.5f, 0.0f);
        frame.set(2, 3, frameX - frameXThickness + originX, frameY + originY, 0, 0.5f, 0.0f);
        frame.set(3, 3, frameX + originX, frameY + originY, 0, 0.0f, 0.0f);
        return frame;
    }
    public GridQuadFrame() {
        int vertsAcross = 4;
        int vertsDown = 4;
        mW = vertsAcross;
        mH = vertsDown;
        int size = vertsAcross * vertsDown;
        final int FLOAT_SIZE = 4;
        final int CHAR_SIZE = 2;
        mVertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 3).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSecTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        int indexCount = INDEX_COUNT; 
        mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(ByteOrder.nativeOrder()).asCharBuffer();
        CharBuffer buffer = mIndexBuffer;
        buffer.put(0, (char) 0);
        buffer.put(1, (char) 4);
        buffer.put(2, (char) 1);
        buffer.put(3, (char) 5);
        buffer.put(4, (char) 2);
        buffer.put(5, (char) 6);
        buffer.put(6, (char) 3);
        buffer.put(7, (char) 7);
        buffer.put(8, (char) 11);
        buffer.put(9, (char) 6);
        buffer.put(10, (char) 10);
        buffer.put(11, (char) 14);
        buffer.put(12, (char) 11);
        buffer.put(13, (char) 15);
        buffer.put(14, (char) 15);
        buffer.put(15, (char) 14);
        buffer.put(16, (char) 14);
        buffer.put(17, (char) 10);
        buffer.put(18, (char) 13);
        buffer.put(19, (char) 9);
        buffer.put(20, (char) 12);
        buffer.put(21, (char) 8);
        buffer.put(22, (char) 4);
        buffer.put(23, (char) 9);
        buffer.put(24, (char) 5);
        mVertBufferIndex = 0;
    }
    void set(int i, int j, float x, float y, float z, float u, float v) {
        if (i < 0 || i >= mW) {
            throw new IllegalArgumentException("i");
        }
        if (j < 0 || j >= mH) {
            throw new IllegalArgumentException("j");
        }
        int index = mW * j + i;
        int posIndex = index * 3;
        mVertexBuffer.put(posIndex, x);
        mVertexBuffer.put(posIndex + 1, y);
        mVertexBuffer.put(posIndex + 2, z);
        int texIndex = index * 2;
        mTexCoordBuffer.put(texIndex, u);
        mTexCoordBuffer.put(texIndex + 1, v);
        int secTexIndex = index * 2;
        mSecTexCoordBuffer.put(secTexIndex, u);
        mSecTexCoordBuffer.put(secTexIndex + 1, v);
    }
    public void bindArrays(GL10 gl) {
        GL11 gl11 = (GL11) gl;
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
        gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
        gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
        gl11.glClientActiveTexture(GL11.GL_TEXTURE1);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mSecTextureCoordBufferIndex);
        gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
        gl11.glClientActiveTexture(GL11.GL_TEXTURE0);
        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
    }
    public static final void draw(GL11 gl11) {
        gl11.glDrawElements(GL11.GL_TRIANGLE_STRIP, INDEX_COUNT, GL11.GL_UNSIGNED_SHORT, 0);
    }
    public void unbindArrays(GL10 gl) {
        GL11 gl11 = (GL11) gl;
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    public boolean usingHardwareBuffers() {
        return mVertBufferIndex != 0;
    }
    public void forgetHardwareBuffers() {
        mVertBufferIndex = 0;
        mIndexBufferIndex = 0;
        mTextureCoordBufferIndex = 0;
        mSecTextureCoordBufferIndex = 0;
    }
    public void freeHardwareBuffers(GL10 gl) {
        if (mVertBufferIndex != 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11) gl;
                int[] buffer = new int[1];
                buffer[0] = mVertBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                buffer[0] = mTextureCoordBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                buffer[0] = mSecTextureCoordBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                buffer[0] = mIndexBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
            }
            forgetHardwareBuffers();
        }
    }
    public void generateHardwareBuffers(GL10 gl) {
        if (mVertBufferIndex == 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11) gl;
                int[] buffer = new int[1];
                gl11.glGenBuffers(1, buffer, 0);
                mVertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                mVertexBuffer.position(0);
                final int vertexSize = mVertexBuffer.capacity() * 4;
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, mVertexBuffer, GL11.GL_STATIC_DRAW);
                gl11.glGenBuffers(1, buffer, 0);
                mTextureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
                final int texCoordSize = mTexCoordBuffer.capacity() * 4;
                mTexCoordBuffer.position(0);
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texCoordSize, mTexCoordBuffer, GL11.GL_STATIC_DRAW);
                gl11.glGenBuffers(1, buffer, 0);
                mSecTextureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mSecTextureCoordBufferIndex);
                final int secTexCoordSize = mSecTexCoordBuffer.capacity() * 4;
                mSecTexCoordBuffer.position(0);
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, secTexCoordSize, mSecTexCoordBuffer, GL11.GL_STATIC_DRAW);
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                gl11.glGenBuffers(1, buffer, 0);
                mIndexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
                final int indexSize = mIndexBuffer.capacity() * 2;
                mIndexBuffer.position(0);
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, mIndexBuffer, GL11.GL_STATIC_DRAW);
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }
    }
}
