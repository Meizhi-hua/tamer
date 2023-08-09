public class AnimationUtils {
    public static long currentAnimationTimeMillis() {
        return SystemClock.uptimeMillis();
    }
    public static Animation loadAnimation(Context context, int id)
            throws NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createAnimationFromXml(context, parser);
        } catch (XmlPullParserException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } finally {
            if (parser != null) parser.close();
        }
    }
    private static Animation createAnimationFromXml(Context c, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
    }
    private static Animation createAnimationFromXml(Context c, XmlPullParser parser,
            AnimationSet parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        Animation anim = null;
        int type;
        int depth = parser.getDepth();
        while (((type=parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
               && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            String  name = parser.getName();
            if (name.equals("set")) {
                anim = new AnimationSet(c, attrs);
                createAnimationFromXml(c, parser, (AnimationSet)anim, attrs);
            } else if (name.equals("alpha")) {
                anim = new AlphaAnimation(c, attrs);
            } else if (name.equals("scale")) {
                anim = new ScaleAnimation(c, attrs);
            }  else if (name.equals("rotate")) {
                anim = new RotateAnimation(c, attrs);
            }  else if (name.equals("translate")) {
                anim = new TranslateAnimation(c, attrs);
            } else {
                throw new RuntimeException("Unknown animation name: " + parser.getName());
            }
            if (parent != null) {
                parent.addAnimation(anim);
            }
        }
        return anim;
    }
    public static LayoutAnimationController loadLayoutAnimation(Context context, int id)
            throws NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createLayoutAnimationFromXml(context, parser);
        } catch (XmlPullParserException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } finally {
            if (parser != null) parser.close();
        }
    }
    private static LayoutAnimationController createLayoutAnimationFromXml(Context c,
            XmlPullParser parser) throws XmlPullParserException, IOException {
        return createLayoutAnimationFromXml(c, parser, Xml.asAttributeSet(parser));
    }
    private static LayoutAnimationController createLayoutAnimationFromXml(Context c,
            XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        LayoutAnimationController controller = null;
        int type;
        int depth = parser.getDepth();
        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if ("layoutAnimation".equals(name)) {
                controller = new LayoutAnimationController(c, attrs);
            } else if ("gridLayoutAnimation".equals(name)) {
                controller = new GridLayoutAnimationController(c, attrs);
            } else {
                throw new RuntimeException("Unknown layout animation name: " + name);
            }
        }
        return controller;
    }
    public static Animation makeInAnimation(Context c, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_left);
        } else {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_right);
        }
        a.setInterpolator(new DecelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }
    public static Animation makeOutAnimation(Context c, boolean toRight) {
        Animation a;
        if (toRight) {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_out_right);
        } else {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_out_left);
        }
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }
    public static Animation makeInChildBottomAnimation(Context c) {
        Animation a;
        a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_child_bottom);
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }
    public static Interpolator loadInterpolator(Context context, int id) throws NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createInterpolatorFromXml(context, parser);
        } catch (XmlPullParserException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex) {
            NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } finally {
            if (parser != null) parser.close();
        }
    }
    private static Interpolator createInterpolatorFromXml(Context c, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Interpolator interpolator = null;
        int type;
        int depth = parser.getDepth();
        while (((type=parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
               && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            AttributeSet attrs = Xml.asAttributeSet(parser);
            String  name = parser.getName();
            if (name.equals("linearInterpolator")) {
                interpolator = new LinearInterpolator(c, attrs);
            } else if (name.equals("accelerateInterpolator")) {
                interpolator = new AccelerateInterpolator(c, attrs);
            } else if (name.equals("decelerateInterpolator")) {
                interpolator = new DecelerateInterpolator(c, attrs);
            }  else if (name.equals("accelerateDecelerateInterpolator")) {
                interpolator = new AccelerateDecelerateInterpolator(c, attrs);
            }  else if (name.equals("cycleInterpolator")) {
                interpolator = new CycleInterpolator(c, attrs);
            } else if (name.equals("anticipateInterpolator")) {
                interpolator = new AnticipateInterpolator(c, attrs);
            } else if (name.equals("overshootInterpolator")) {
                interpolator = new OvershootInterpolator(c, attrs);
            } else if (name.equals("anticipateOvershootInterpolator")) {
                interpolator = new AnticipateOvershootInterpolator(c, attrs);
            } else if (name.equals("bounceInterpolator")) {
                interpolator = new BounceInterpolator(c, attrs);
            } else {
                throw new RuntimeException("Unknown interpolator name: " + parser.getName());
            }
        }
        return interpolator;
    }
}
