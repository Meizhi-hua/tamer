public class GLWorld {
	public void addShape(GLShape shape) {
		mShapeList.add(shape);
		mIndexCount += shape.getIndexCount();
	}
	public void generate() {		
	    ByteBuffer bb = ByteBuffer.allocateDirect(mVertexList.size()*4*4);
	    bb.order(ByteOrder.nativeOrder());
		mColorBuffer = bb.asIntBuffer();
	    bb = ByteBuffer.allocateDirect(mVertexList.size()*4*3);
	    bb.order(ByteOrder.nativeOrder());
	    mVertexBuffer = bb.asIntBuffer();
	    bb = ByteBuffer.allocateDirect(mIndexCount*2);
	    bb.order(ByteOrder.nativeOrder());
	    mIndexBuffer = bb.asShortBuffer();
		Iterator<GLVertex> iter2 = mVertexList.iterator();
		while (iter2.hasNext()) {
			GLVertex vertex = iter2.next();
			vertex.put(mVertexBuffer, mColorBuffer);
		}
		Iterator<GLShape> iter3 = mShapeList.iterator();
		while (iter3.hasNext()) {
			GLShape shape = iter3.next();
			shape.putIndices(mIndexBuffer);
		}
	}
	public GLVertex addVertex(float x, float y, float z) {
		GLVertex vertex = new GLVertex(x, y, z, mVertexList.size());
		mVertexList.add(vertex);
		return vertex;
	}
	public void transformVertex(GLVertex vertex, M4 transform) {
		vertex.update(mVertexBuffer, transform);
	}
	int count = 0;
    public void draw(GL10 gl)
    {
		mColorBuffer.position(0);
		mVertexBuffer.position(0);
		mIndexBuffer.position(0);
		gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        count++;
    }
    static public float toFloat(int x) {
    	return x/65536.0f;
    }
	private ArrayList<GLShape>	mShapeList = new ArrayList<GLShape>();	
	private ArrayList<GLVertex>	mVertexList = new ArrayList<GLVertex>();
	private int mIndexCount = 0;
    private IntBuffer   mVertexBuffer;
    private IntBuffer   mColorBuffer;
    private ShortBuffer mIndexBuffer;
}
