public class GradientDrawable extends Drawable {
    public static final int RECTANGLE = 0;
    public static final int OVAL = 1; 
    public static final int LINE = 2;
    public static final int RING = 3;
    public static final int LINEAR_GRADIENT = 0;
    public static final int RADIAL_GRADIENT = 1;
    public static final int SWEEP_GRADIENT  = 2;
    private GradientState mGradientState;
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mPadding;
    private Paint mStrokePaint;   
    private ColorFilter mColorFilter;   
    private int mAlpha = 0xFF;  
    private boolean mDither;
    private final Path mPath = new Path();
    private final RectF mRect = new RectF();
    private Paint mLayerPaint;    
    private boolean mRectIsDirty;   
    private boolean mMutated;
    private Path mRingPath;
    private boolean mPathIsDirty;
    public enum Orientation {
        TOP_BOTTOM,
        TR_BL,
        RIGHT_LEFT,
        BR_TL,
        BOTTOM_TOP,
        BL_TR,
        LEFT_RIGHT,
        TL_BR,
    }
    public GradientDrawable() {
        this(new GradientState(Orientation.TOP_BOTTOM, null));
    }
    public GradientDrawable(Orientation orientation, int[] colors) {
        this(new GradientState(orientation, colors));
    }
    @Override
    public boolean getPadding(Rect padding) {
        if (mPadding != null) {
            padding.set(mPadding);
            return true;
        } else {
            return super.getPadding(padding);
        }
    }
    public void setCornerRadii(float[] radii) {
        mGradientState.setCornerRadii(radii);
    }
    public void setCornerRadius(float radius) {
        mGradientState.setCornerRadius(radius);
    }
    public void setStroke(int width, int color) {
        setStroke(width, color, 0, 0);
    }
    public void setStroke(int width, int color, float dashWidth, float dashGap) {
        mGradientState.setStroke(width, color, dashWidth, dashGap);
        if (mStrokePaint == null)  {
            mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStyle(Paint.Style.STROKE);
        }
        mStrokePaint.setStrokeWidth(width);
        mStrokePaint.setColor(color);
        DashPathEffect e = null;
        if (dashWidth > 0) {
            e = new DashPathEffect(new float[] { dashWidth, dashGap }, 0);
        }
        mStrokePaint.setPathEffect(e);
    }
    public void setSize(int width, int height) {
        mGradientState.setSize(width, height); 
    }
    public void setShape(int shape) {
        mRingPath = null;
        mGradientState.setShape(shape);
    }
    public void setGradientType(int gradient) {
        mGradientState.setGradientType(gradient);
        mRectIsDirty = true;
    }
    public void setGradientCenter(float x, float y) {
        mGradientState.setGradientCenter(x, y);
    }
    public void setGradientRadius(float gradientRadius) {
        mGradientState.setGradientRadius(gradientRadius);
    }
    public void setUseLevel(boolean useLevel) {
        mGradientState.mUseLevel = useLevel;
    }
    private int modulateAlpha(int alpha) {
        int scale = mAlpha + (mAlpha >> 7);
        return alpha * scale >> 8;
    }
    @Override
    public void draw(Canvas canvas) {
        if (!ensureValidRect()) {
            return;
        }
        final int prevFillAlpha = mFillPaint.getAlpha();
        final int prevStrokeAlpha = mStrokePaint != null ? mStrokePaint.getAlpha() : 0;
        final int currFillAlpha = modulateAlpha(prevFillAlpha);
        final int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
        final boolean haveStroke = currStrokeAlpha > 0 && mStrokePaint.getStrokeWidth() > 0;
        final boolean haveFill = currFillAlpha > 0;
        final GradientState st = mGradientState;
         final boolean useLayer = haveStroke && haveFill && st.mShape != LINE &&
                 currStrokeAlpha < 255;
        if (useLayer) {
            if (mLayerPaint == null) {
                mLayerPaint = new Paint();
            }
            mLayerPaint.setDither(mDither);
            mLayerPaint.setAlpha(mAlpha);
            mLayerPaint.setColorFilter(mColorFilter);
            float rad = mStrokePaint.getStrokeWidth();
            canvas.saveLayer(mRect.left - rad, mRect.top - rad,
                             mRect.right + rad, mRect.bottom + rad,
                             mLayerPaint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            mFillPaint.setColorFilter(null);
            mStrokePaint.setColorFilter(null);
        } else {
            mFillPaint.setAlpha(currFillAlpha);
            mFillPaint.setDither(mDither);
            mFillPaint.setColorFilter(mColorFilter);
            if (haveStroke) {
                mStrokePaint.setAlpha(currStrokeAlpha);
                mStrokePaint.setDither(mDither);
                mStrokePaint.setColorFilter(mColorFilter);
            }
        }
        switch (st.mShape) {
            case RECTANGLE:
                if (st.mRadiusArray != null) {
                    mPath.reset();
                    mPath.addRoundRect(mRect, st.mRadiusArray,
                                       Path.Direction.CW);
                    canvas.drawPath(mPath, mFillPaint);
                    if (haveStroke) {
                        canvas.drawPath(mPath, mStrokePaint);
                    }
                }
                else {
                    float rad = st.mRadius;
                    float r = Math.min(mRect.width(), mRect.height()) * 0.5f;
                    if (rad > r) {
                        rad = r;
                    }
                    canvas.drawRoundRect(mRect, rad, rad, mFillPaint);
                    if (haveStroke) {
                        canvas.drawRoundRect(mRect, rad, rad, mStrokePaint);
                    }
                }
                break;
            case OVAL:
                canvas.drawOval(mRect, mFillPaint);
                if (haveStroke) {
                    canvas.drawOval(mRect, mStrokePaint);
                }
                break;
            case LINE: {
                RectF r = mRect;
                float y = r.centerY();
                canvas.drawLine(r.left, y, r.right, y, mStrokePaint);
                break;
            }
            case RING:
                Path path = buildRing(st);
                canvas.drawPath(path, mFillPaint);
                if (haveStroke) {
                    canvas.drawPath(path, mStrokePaint);
                }
                break;
        }
        if (useLayer) {
            canvas.restore();
        } else {
            mFillPaint.setAlpha(prevFillAlpha);
            if (haveStroke) {
                mStrokePaint.setAlpha(prevStrokeAlpha);
            }
        }
    }
    private Path buildRing(GradientState st) {
        if (mRingPath != null && (!st.mUseLevelForShape || !mPathIsDirty)) return mRingPath;
        mPathIsDirty = false;
        float sweep = st.mUseLevelForShape ? (360.0f * getLevel() / 10000.0f) : 360f;
        RectF bounds = new RectF(mRect);
        float x = bounds.width() / 2.0f;
        float y = bounds.height() / 2.0f;
        float thickness = st.mThickness != -1 ?
                st.mThickness : bounds.width() / st.mThicknessRatio;
        float radius = st.mInnerRadius != -1 ?
                st.mInnerRadius : bounds.width() / st.mInnerRadiusRatio;
        RectF innerBounds = new RectF(bounds);
        innerBounds.inset(x - radius, y - radius);
        bounds = new RectF(innerBounds);
        bounds.inset(-thickness, -thickness);
        if (mRingPath == null) {
            mRingPath = new Path();
        } else {
            mRingPath.reset();            
        }
        final Path ringPath = mRingPath;
        if (sweep < 360 && sweep > -360) {
            ringPath.setFillType(Path.FillType.EVEN_ODD);
            ringPath.moveTo(x + radius, y);
            ringPath.lineTo(x + radius + thickness, y);
            ringPath.arcTo(bounds, 0.0f, sweep, false);
            ringPath.arcTo(innerBounds, sweep, -sweep, false);
            ringPath.close();
        } else {
            ringPath.addOval(bounds, Path.Direction.CW);
            ringPath.addOval(innerBounds, Path.Direction.CCW);
        }
        return ringPath;
    }
    public void setColor(int argb) {
        mGradientState.setSolidColor(argb);
        mFillPaint.setColor(argb);
    }
    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mGradientState.mChangingConfigurations;
    }
    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }
    @Override
    public void setDither(boolean dither) {
        mDither = dither;
    }
    @Override
    public void setColorFilter(ColorFilter cf) {
        mColorFilter = cf;
    }
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    protected void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        mRingPath = null;
        mPathIsDirty = true;
        mRectIsDirty = true;
    }
    @Override
    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        mRectIsDirty = true;
        mPathIsDirty = true;
        invalidateSelf();
        return true;
    }
    private boolean ensureValidRect() {
        if (mRectIsDirty) {
            mRectIsDirty = false;
            Rect bounds = getBounds();
            float inset = 0;
            if (mStrokePaint != null) {
                inset = mStrokePaint.getStrokeWidth() * 0.5f;
            }
            final GradientState st = mGradientState;
            mRect.set(bounds.left + inset, bounds.top + inset,
                      bounds.right - inset, bounds.bottom - inset);
            final int[] colors = st.mColors;
            if (colors != null) {
                RectF r = mRect;
                float x0, x1, y0, y1;
                if (st.mGradient == LINEAR_GRADIENT) {
                    final float level = st.mUseLevel ? (float) getLevel() / 10000.0f : 1.0f;                    
                    switch (st.mOrientation) {
                    case TOP_BOTTOM:
                        x0 = r.left;            y0 = r.top;
                        x1 = x0;                y1 = level * r.bottom;
                        break;
                    case TR_BL:
                        x0 = r.right;           y0 = r.top;
                        x1 = level * r.left;    y1 = level * r.bottom;
                        break;
                    case RIGHT_LEFT:
                        x0 = r.right;           y0 = r.top;
                        x1 = level * r.left;    y1 = y0;
                        break;
                    case BR_TL:
                        x0 = r.right;           y0 = r.bottom;
                        x1 = level * r.left;    y1 = level * r.top;
                        break;
                    case BOTTOM_TOP:
                        x0 = r.left;            y0 = r.bottom;
                        x1 = x0;                y1 = level * r.top;
                        break;
                    case BL_TR:
                        x0 = r.left;            y0 = r.bottom;
                        x1 = level * r.right;   y1 = level * r.top;
                        break;
                    case LEFT_RIGHT:
                        x0 = r.left;            y0 = r.top;
                        x1 = level * r.right;   y1 = y0;
                        break;
                    default:
                        x0 = r.left;            y0 = r.top;
                        x1 = level * r.right;   y1 = level * r.bottom;
                        break;
                    }
                    mFillPaint.setShader(new LinearGradient(x0, y0, x1, y1,
                            colors, st.mPositions, Shader.TileMode.CLAMP));
                } else if (st.mGradient == RADIAL_GRADIENT) {
                    x0 = r.left + (r.right - r.left) * st.mCenterX;
                    y0 = r.top + (r.bottom - r.top) * st.mCenterY;
                    final float level = st.mUseLevel ? (float) getLevel() / 10000.0f : 1.0f;
                    mFillPaint.setShader(new RadialGradient(x0, y0,
                            level * st.mGradientRadius, colors, null,
                            Shader.TileMode.CLAMP));
                } else if (st.mGradient == SWEEP_GRADIENT) {
                    x0 = r.left + (r.right - r.left) * st.mCenterX;
                    y0 = r.top + (r.bottom - r.top) * st.mCenterY;
                    int[] tempColors = colors;
                    float[] tempPositions = null;
                    if (st.mUseLevel) {
                        tempColors = st.mTempColors;
                        final int length = colors.length;
                        if (tempColors == null || tempColors.length != length + 1) {
                            tempColors = st.mTempColors = new int[length + 1];
                        }
                        System.arraycopy(colors, 0, tempColors, 0, length);
                        tempColors[length] = colors[length - 1];
                        tempPositions = st.mTempPositions;
                        final float fraction = 1.0f / (float) (length - 1);
                        if (tempPositions == null || tempPositions.length != length + 1) {
                            tempPositions = st.mTempPositions = new float[length + 1];
                        }
                        final float level = (float) getLevel() / 10000.0f;
                        for (int i = 0; i < length; i++) {
                            tempPositions[i] = i * fraction * level;
                        }
                        tempPositions[length] = 1.0f;
                    }
                    mFillPaint.setShader(new SweepGradient(x0, y0, tempColors, tempPositions));
                }
            }
        }
        return !mRect.isEmpty();
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser,
            AttributeSet attrs)
            throws XmlPullParserException, IOException {
        final GradientState st = mGradientState;
        TypedArray a = r.obtainAttributes(attrs,
                com.android.internal.R.styleable.GradientDrawable);
        super.inflateWithAttributes(r, parser, a,
                com.android.internal.R.styleable.GradientDrawable_visible);
        int shapeType = a.getInt(
                com.android.internal.R.styleable.GradientDrawable_shape, RECTANGLE);
        if (shapeType == RING) {
            st.mInnerRadius = a.getDimensionPixelSize(
                    com.android.internal.R.styleable.GradientDrawable_innerRadius, -1);
            if (st.mInnerRadius == -1) {
                st.mInnerRadiusRatio = a.getFloat(
                        com.android.internal.R.styleable.GradientDrawable_innerRadiusRatio, 3.0f);
            }
            st.mThickness = a.getDimensionPixelSize(
                    com.android.internal.R.styleable.GradientDrawable_thickness, -1);
            if (st.mThickness == -1) {
                st.mThicknessRatio = a.getFloat(
                        com.android.internal.R.styleable.GradientDrawable_thicknessRatio, 9.0f);
            }
            st.mUseLevelForShape = a.getBoolean(
                    com.android.internal.R.styleable.GradientDrawable_useLevel, true);
        }
        a.recycle();
        setShape(shapeType);
        int type;
        final int innerDepth = parser.getDepth()+1;
        int depth;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && ((depth=parser.getDepth()) >= innerDepth
                       || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            if (depth > innerDepth) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("size")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.GradientDrawableSize);
                int width = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.GradientDrawableSize_width, -1);
                int height = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.GradientDrawableSize_height, -1);
                a.recycle();
                setSize(width, height);
            } else if (name.equals("gradient")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.GradientDrawableGradient);
                int startColor = a.getColor(
                        com.android.internal.R.styleable.GradientDrawableGradient_startColor, 0);
                boolean hasCenterColor = a
                        .hasValue(com.android.internal.R.styleable.GradientDrawableGradient_centerColor);
                int centerColor = a.getColor(
                        com.android.internal.R.styleable.GradientDrawableGradient_centerColor, 0);
                int endColor = a.getColor(
                        com.android.internal.R.styleable.GradientDrawableGradient_endColor, 0);
                int gradientType = a.getInt(
                        com.android.internal.R.styleable.GradientDrawableGradient_type,
                        LINEAR_GRADIENT);
                st.mCenterX = getFloatOrFraction(
                        a,
                        com.android.internal.R.styleable.GradientDrawableGradient_centerX,
                        0.5f);
                st.mCenterY = getFloatOrFraction(
                        a,
                        com.android.internal.R.styleable.GradientDrawableGradient_centerY,
                        0.5f);
                st.mUseLevel = a.getBoolean(
                        com.android.internal.R.styleable.GradientDrawableGradient_useLevel, false);
                st.mGradient = gradientType;
                if (gradientType == LINEAR_GRADIENT) {
                    int angle = (int)a.getFloat(
                            com.android.internal.R.styleable.GradientDrawableGradient_angle, 0);
                    angle %= 360;
                    if (angle % 45 != 0) {
                        throw new XmlPullParserException(a.getPositionDescription()
                                + "<gradient> tag requires 'angle' attribute to "
                                + "be a multiple of 45");
                    }
                    switch (angle) {
                    case 0:
                        st.mOrientation = Orientation.LEFT_RIGHT;
                        break;
                    case 45:
                        st.mOrientation = Orientation.BL_TR;
                        break;
                    case 90:
                        st.mOrientation = Orientation.BOTTOM_TOP;
                        break;
                    case 135:
                        st.mOrientation = Orientation.BR_TL;
                        break;
                    case 180:
                        st.mOrientation = Orientation.RIGHT_LEFT;
                        break;
                    case 225:
                        st.mOrientation = Orientation.TR_BL;
                        break;
                    case 270:
                        st.mOrientation = Orientation.TOP_BOTTOM;
                        break;
                    case 315:
                        st.mOrientation = Orientation.TL_BR;
                        break;
                    }
                } else {
                    TypedValue tv = a.peekValue(
                            com.android.internal.R.styleable.GradientDrawableGradient_gradientRadius);
                    if (tv != null) {
                        boolean radiusRel = tv.type == TypedValue.TYPE_FRACTION;
                        st.mGradientRadius = radiusRel ?
                                tv.getFraction(1.0f, 1.0f) : tv.getFloat();
                    } else if (gradientType == RADIAL_GRADIENT) {
                        throw new XmlPullParserException(
                                a.getPositionDescription()
                                + "<gradient> tag requires 'gradientRadius' "
                                + "attribute with radial type");
                    }
                }
                a.recycle();
                if (hasCenterColor) {
                    st.mColors = new int[3];
                    st.mColors[0] = startColor;
                    st.mColors[1] = centerColor;
                    st.mColors[2] = endColor;
                    st.mPositions = new float[3];
                    st.mPositions[0] = 0.0f;
                    st.mPositions[1] = st.mCenterX != 0.5f ? st.mCenterX : st.mCenterY;
                    st.mPositions[2] = 1f;
                } else {
                    st.mColors = new int[2];
                    st.mColors[0] = startColor;
                    st.mColors[1] = endColor;
                }
            } else if (name.equals("solid")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.GradientDrawableSolid);
                int argb = a.getColor(
                        com.android.internal.R.styleable.GradientDrawableSolid_color, 0);
                a.recycle();
                setColor(argb);
            } else if (name.equals("stroke")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.GradientDrawableStroke);
                int width = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.GradientDrawableStroke_width, 0);
                int color = a.getColor(
                        com.android.internal.R.styleable.GradientDrawableStroke_color, 0);
                float dashWidth = a.getDimension(
                        com.android.internal.R.styleable.GradientDrawableStroke_dashWidth, 0);
                if (dashWidth != 0.0f) {
                    float dashGap = a.getDimension(
                            com.android.internal.R.styleable.GradientDrawableStroke_dashGap, 0);
                    setStroke(width, color, dashWidth, dashGap);
                } else {
                    setStroke(width, color);
                }
                a.recycle();
            } else if (name.equals("corners")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.DrawableCorners);
                int radius = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.DrawableCorners_radius, 0);
                setCornerRadius(radius);
                int topLeftRadius = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.DrawableCorners_topLeftRadius, radius);
                int topRightRadius = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.DrawableCorners_topRightRadius, radius);
                int bottomLeftRadius = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.DrawableCorners_bottomLeftRadius, radius);
                int bottomRightRadius = a.getDimensionPixelSize(
                        com.android.internal.R.styleable.DrawableCorners_bottomRightRadius, radius);
                if (topLeftRadius != radius || topRightRadius != radius ||
                        bottomLeftRadius != radius || bottomRightRadius != radius) {
                    setCornerRadii(new float[] {
                            topLeftRadius, topLeftRadius,
                            topRightRadius, topRightRadius,
                            bottomLeftRadius, bottomLeftRadius,
                            bottomRightRadius, bottomRightRadius
                    });
                }
                a.recycle();
            } else if (name.equals("padding")) {
                a = r.obtainAttributes(attrs,
                        com.android.internal.R.styleable.GradientDrawablePadding);
                mPadding = new Rect(
                        a.getDimensionPixelOffset(
                                com.android.internal.R.styleable.GradientDrawablePadding_left, 0),
                        a.getDimensionPixelOffset(
                                com.android.internal.R.styleable.GradientDrawablePadding_top, 0),
                        a.getDimensionPixelOffset(
                                com.android.internal.R.styleable.GradientDrawablePadding_right, 0),
                        a.getDimensionPixelOffset(
                                com.android.internal.R.styleable.GradientDrawablePadding_bottom, 0));
                a.recycle();
                mGradientState.mPadding = mPadding;
            } else {
                Log.w("drawable", "Bad element under <shape>: " + name);
            }
        }
    }
    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        float v = defaultValue;
        if (tv != null) {
            boolean vIsFraction = tv.type == TypedValue.TYPE_FRACTION;
            v = vIsFraction ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
        }
        return v;
    }
    @Override
    public int getIntrinsicWidth() {
        return mGradientState.mWidth;
    }
    @Override
    public int getIntrinsicHeight() {
        return mGradientState.mHeight;
    }
    @Override
    public ConstantState getConstantState() {
        mGradientState.mChangingConfigurations = super.getChangingConfigurations();
        return mGradientState;
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mGradientState = new GradientState(mGradientState);
            initializeWithState(mGradientState);
            mMutated = true;
        }
        return this;
    }
    final static class GradientState extends ConstantState {
        public int mChangingConfigurations;
        public int mShape = RECTANGLE;
        public int mGradient = LINEAR_GRADIENT;
        public Orientation mOrientation;
        public int[] mColors;
        public int[] mTempColors; 
        public float[] mTempPositions; 
        public float[] mPositions;
        public boolean mHasSolidColor;
        public int mSolidColor;
        public int mStrokeWidth = -1;   
        public int mStrokeColor;
        public float mStrokeDashWidth;
        public float mStrokeDashGap;
        public float mRadius;    
        public float[] mRadiusArray;
        public Rect mPadding;
        public int mWidth = -1;
        public int mHeight = -1;
        public float mInnerRadiusRatio;
        public float mThicknessRatio;
        public int mInnerRadius;
        public int mThickness;
        private float mCenterX = 0.5f;
        private float mCenterY = 0.5f;
        private float mGradientRadius = 0.5f;
        private boolean mUseLevel;
        private boolean mUseLevelForShape;
        GradientState() {
            mOrientation = Orientation.TOP_BOTTOM;
        }
        GradientState(Orientation orientation, int[] colors) {
            mOrientation = orientation;
            mColors = colors;
        }
        public GradientState(GradientState state) {
            mChangingConfigurations = state.mChangingConfigurations;
            mShape = state.mShape;
            mGradient = state.mGradient;
            mOrientation = state.mOrientation;
            if (state.mColors != null) {
                mColors = state.mColors.clone();
            }
            if (state.mPositions != null) {
                mPositions = state.mPositions.clone();
            }
            mHasSolidColor = state.mHasSolidColor;
            mStrokeWidth = state.mStrokeWidth;
            mStrokeColor = state.mStrokeColor;
            mStrokeDashWidth = state.mStrokeDashWidth;
            mStrokeDashGap = state.mStrokeDashGap;
            mRadius = state.mRadius;
            if (state.mRadiusArray != null) {
                mRadiusArray = state.mRadiusArray.clone();
            }
            if (state.mPadding != null) {
                mPadding = new Rect(state.mPadding);
            }
            mWidth = state.mWidth;
            mHeight = state.mHeight;
            mInnerRadiusRatio = state.mInnerRadiusRatio;
            mThicknessRatio = state.mThicknessRatio;
            mInnerRadius = state.mInnerRadius;
            mThickness = state.mThickness;
            mCenterX = state.mCenterX;
            mCenterY = state.mCenterY;
            mGradientRadius = state.mGradientRadius;
            mUseLevel = state.mUseLevel;
            mUseLevelForShape = state.mUseLevelForShape;
        }
        @Override
        public Drawable newDrawable() {
            return new GradientDrawable(this);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new GradientDrawable(this);
        }
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        public void setShape(int shape) {
            mShape = shape;
        }
        public void setGradientType(int gradient) {
            mGradient = gradient;
        }
        public void setGradientCenter(float x, float y) {
            mCenterX = x;
            mCenterY = y;
        }
        public void setSolidColor(int argb) {
            mHasSolidColor = true;
            mSolidColor = argb;
            mColors = null;
        }
        public void setStroke(int width, int color) {
            mStrokeWidth = width;
            mStrokeColor = color;
        }
        public void setStroke(int width, int color, float dashWidth, float dashGap) {
            mStrokeWidth = width;
            mStrokeColor = color;
            mStrokeDashWidth = dashWidth;
            mStrokeDashGap = dashGap;
        }
        public void setCornerRadius(float radius) {
            if (radius < 0) {
                radius = 0;
            }
            mRadius = radius;
            mRadiusArray = null;
        }
        public void setCornerRadii(float[] radii) {
            mRadiusArray = radii;
            if (radii == null) {
                mRadius = 0;
            }
        }
        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
        }
        public void setGradientRadius(float gradientRadius) {
            mGradientRadius = gradientRadius;
        }
    }
    private GradientDrawable(GradientState state) {
        mGradientState = state;
        initializeWithState(state);
        mRectIsDirty = true;
    }
    private void initializeWithState(GradientState state) {
        if (state.mHasSolidColor) {
            mFillPaint.setColor(state.mSolidColor);
        }
        mPadding = state.mPadding;
        if (state.mStrokeWidth >= 0) {
            mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setStrokeWidth(state.mStrokeWidth);
            mStrokePaint.setColor(state.mStrokeColor);
            if (state.mStrokeDashWidth != 0.0f) {
                DashPathEffect e = new DashPathEffect(
                        new float[] { state.mStrokeDashWidth, state.mStrokeDashGap }, 0);
                mStrokePaint.setPathEffect(e);
            }
        }
    }
}
