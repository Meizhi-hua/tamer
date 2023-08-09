public class GLU {
    public static String gluErrorString(int error) {
        switch (error) {
        case GL10.GL_NO_ERROR:
            return "no error";
        case GL10.GL_INVALID_ENUM:
            return "invalid enum";
        case GL10.GL_INVALID_VALUE:
            return "invalid value";
        case GL10.GL_INVALID_OPERATION:
            return "invalid operation";
        case GL10.GL_STACK_OVERFLOW:
            return "stack overflow";
        case GL10.GL_STACK_UNDERFLOW:
            return "stack underflow";
        case GL10.GL_OUT_OF_MEMORY:
            return "out of memory";
        default:
            return null;
        }
    }
    public static void gluLookAt(GL10 gl, float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ, float upX, float upY,
            float upZ) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            Matrix.setLookAtM(scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ,
                    upX, upY, upZ);
            gl.glMultMatrixf(scratch, 0);
        }
    }
    public static void gluOrtho2D(GL10 gl, float left, float right,
            float bottom, float top) {
        gl.glOrthof(left, right, bottom, top, -1.0f, 1.0f);
    }
    public static void gluPerspective(GL10 gl, float fovy, float aspect,
            float zNear, float zFar) {
        float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
        gl.glFrustumf(left, right, bottom, top, zNear, zFar);
    }
    public static int gluProject(float objX, float objY, float objZ,
            float[] model, int modelOffset, float[] project, int projectOffset,
            int[] view, int viewOffset, float[] win, int winOffset) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            final int M_OFFSET = 0; 
            final int V_OFFSET = 16; 
            final int V2_OFFSET = 20; 
            Matrix.multiplyMM(scratch, M_OFFSET, project, projectOffset,
                    model, modelOffset);
            scratch[V_OFFSET + 0] = objX;
            scratch[V_OFFSET + 1] = objY;
            scratch[V_OFFSET + 2] = objZ;
            scratch[V_OFFSET + 3] = 1.0f;
            Matrix.multiplyMV(scratch, V2_OFFSET,
                    scratch, M_OFFSET, scratch, V_OFFSET);
            float w = scratch[V2_OFFSET + 3];
            if (w == 0.0f) {
                return GL10.GL_FALSE;
            }
            float rw = 1.0f / w;
            win[winOffset] =
                    view[viewOffset] + view[viewOffset + 2]
                            * (scratch[V2_OFFSET + 0] * rw + 1.0f)
                            * 0.5f;
            win[winOffset + 1] =
                    view[viewOffset + 1] + view[viewOffset + 3]
                            * (scratch[V2_OFFSET + 1] * rw + 1.0f) * 0.5f;
            win[winOffset + 2] = (scratch[V2_OFFSET + 2] * rw + 1.0f) * 0.5f;
        }
        return GL10.GL_TRUE;
    }
    public static int gluUnProject(float winX, float winY, float winZ,
            float[] model, int modelOffset, float[] project, int projectOffset,
            int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            final int PM_OFFSET = 0; 
            final int INVPM_OFFSET = 16; 
               final int V_OFFSET = 0; 
            Matrix.multiplyMM(scratch, PM_OFFSET, project, projectOffset,
                    model, modelOffset);
            if (!Matrix.invertM(scratch, INVPM_OFFSET, scratch, PM_OFFSET)) {
                return GL10.GL_FALSE;
            }
            scratch[V_OFFSET + 0] =
                    2.0f * (winX - view[viewOffset + 0]) / view[viewOffset + 2]
                            - 1.0f;
            scratch[V_OFFSET + 1] =
                    2.0f * (winY - view[viewOffset + 1]) / view[viewOffset + 3]
                            - 1.0f;
            scratch[V_OFFSET + 2] = 2.0f * winZ - 1.0f;
            scratch[V_OFFSET + 3] = 1.0f;
            Matrix.multiplyMV(obj, objOffset, scratch, INVPM_OFFSET,
                    scratch, V_OFFSET);
        }
        return GL10.GL_TRUE;
    }
    private static final float[] sScratch = new float[32];
 }
