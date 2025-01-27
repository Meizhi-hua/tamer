public class RenderScriptGL extends RenderScript {
    private Surface mSurface;
    int mWidth;
    int mHeight;
    public RenderScriptGL(boolean useDepth, boolean forceSW) {
        mSurface = null;
        mWidth = 0;
        mHeight = 0;
        mDev = nDeviceCreate();
        if(forceSW) {
            nDeviceSetConfig(mDev, 0, 1);
        }
        mContext = nContextCreateGL(mDev, 0, useDepth);
        mMessageThread = new MessageThread(this);
        mMessageThread.start();
        Element.initPredefined(this);
    }
    public void contextSetSurface(int w, int h, Surface sur) {
        mSurface = sur;
        mWidth = w;
        mHeight = h;
        validate();
        nContextSetSurface(w, h, mSurface);
    }
    void pause() {
        validate();
        nContextPause();
    }
    void resume() {
        validate();
        nContextResume();
    }
    public void contextBindRootScript(Script s) {
        validate();
        nContextBindRootScript(safeID(s));
    }
    public void contextBindProgramFragmentStore(ProgramStore p) {
        validate();
        nContextBindProgramFragmentStore(safeID(p));
    }
    public void contextBindProgramFragment(ProgramFragment p) {
        validate();
        nContextBindProgramFragment(safeID(p));
    }
    public void contextBindProgramRaster(ProgramRaster p) {
        validate();
        nContextBindProgramRaster(safeID(p));
    }
    public void contextBindProgramVertex(ProgramVertex p) {
        validate();
        nContextBindProgramVertex(safeID(p));
    }
    public class File extends BaseObj {
        File(int id) {
            super(RenderScriptGL.this);
            mID = id;
        }
    }
    public File fileOpen(String s) throws IllegalStateException, IllegalArgumentException
    {
        if(s.length() < 1) {
            throw new IllegalArgumentException("fileOpen does not accept a zero length string.");
        }
        try {
            byte[] bytes = s.getBytes("UTF-8");
            int id = nFileOpen(bytes);
            return new File(id);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
