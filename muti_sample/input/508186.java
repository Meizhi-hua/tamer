public class ProxyDrawable extends Drawable {
    private Drawable mProxy;
    private boolean mMutated;
    public ProxyDrawable(Drawable target) {
        mProxy = target;
    }
    public Drawable getProxy() {
        return mProxy;
    }
    public void setProxy(Drawable proxy) {
        if (proxy != this) {
            mProxy = proxy;
        }
    }
    @Override
    public void draw(Canvas canvas) {
        if (mProxy != null) {
            mProxy.draw(canvas);
        }
    }
    @Override
    public int getIntrinsicWidth() {
        return mProxy != null ? mProxy.getIntrinsicWidth() : -1;
    }
    @Override
    public int getIntrinsicHeight() {
        return mProxy != null ? mProxy.getIntrinsicHeight() : -1;
    }
    @Override
    public int getOpacity() {
        return mProxy != null ? mProxy.getOpacity() : PixelFormat.TRANSPARENT;
    }
    @Override
    public void setFilterBitmap(boolean filter) {
        if (mProxy != null) {
            mProxy.setFilterBitmap(filter);
        }
    }
    @Override
    public void setDither(boolean dither) {
        if (mProxy != null) {
            mProxy.setDither(dither);
        }
    }
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mProxy != null) {
            mProxy.setColorFilter(colorFilter);
        }
    }
    @Override
    public void setAlpha(int alpha) {
        if (mProxy != null) {
            mProxy.setAlpha(alpha);
        }
    }
    @Override
    public Drawable mutate() {
        if (mProxy != null && !mMutated && super.mutate() == this) {
            mProxy.mutate();
            mMutated = true;
        }
        return this;
    }
}
