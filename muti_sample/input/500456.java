public class Clock {
    static final int MILLISECONDS_PER_MINUTE = 60 * 1000;
    static final int MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
    private City mCity = null;
    private long mCitySwitchTime;
    private long mTime;
    private float mColorRed = 1.0f;
    private float mColorGreen = 1.0f;
    private float mColorBlue = 1.0f;
    private long mOldOffset;
    private Interpolator mClockHandInterpolator =
        new AccelerateDecelerateInterpolator();
    public Clock() {
    }
    private static void drawLine(Path path,
        float radius, float pos, float cx, float cy, float r0, float r1) {
        float theta = pos * Shape.TWO_PI - Shape.PI_OVER_TWO;
        float dx = (float) Math.cos(theta);
        float dy = (float) Math.sin(theta);
        float p0x = cx + dx * r0;
        float p0y = cy + dy * r0;
        float p1x = cx + dx * r1;
        float p1y = cy + dy * r1;
        float ox =  (p1y - p0y);
        float oy = -(p1x - p0x);
        float norm = (radius / 2.0f) / (float) Math.sqrt(ox * ox + oy * oy);
        ox *= norm;
        oy *= norm;
        path.moveTo(p0x - ox, p0y - oy);
        path.lineTo(p1x - ox, p1y - oy);
        path.lineTo(p1x + ox, p1y + oy);
        path.lineTo(p0x + ox, p0y + oy);
        path.close();
    }
    private static void drawVArrow(Path path,
        float cx, float cy, float width, float height) {
        path.moveTo(cx - width / 2.0f, cy);
        path.lineTo(cx, cy + height);
        path.lineTo(cx + width / 2.0f, cy);
        path.close();
    }
    private static void drawHArrow(Path path,
        float cx, float cy, float width, float height) {
        path.moveTo(cx, cy - height / 2.0f);
        path.lineTo(cx + width, cy);
        path.lineTo(cx, cy + height / 2.0f);
        path.close();
    }
    private long getOffset(float lerp) {
        long doffset = (long) (mCity.getOffset() *
            (float) MILLISECONDS_PER_HOUR - mOldOffset);
        int sign;
        if (doffset < 0) {
            doffset = -doffset;
            sign = -1;
        } else {
            sign = 1;
        }
        while (doffset > 12L * MILLISECONDS_PER_HOUR) {
            doffset -= 12L * MILLISECONDS_PER_HOUR;
        }
        if (doffset > 6L * MILLISECONDS_PER_HOUR) {
            doffset = 12L * MILLISECONDS_PER_HOUR - doffset;
            sign = -sign;
        }
        doffset = (long)((1.0f - lerp)*doffset);
        long dh = doffset / (MILLISECONDS_PER_HOUR);
        doffset -= dh * MILLISECONDS_PER_HOUR;
        long dm = doffset / MILLISECONDS_PER_MINUTE;
        doffset = sign * (60 * dh + dm) * MILLISECONDS_PER_MINUTE;
        return doffset;
    }
    public void setCity(City city) {
        if (mCity != city) {
            if (mCity != null) {
                mOldOffset =
                    (long) (mCity.getOffset() * (float) MILLISECONDS_PER_HOUR);
            } else if (city != null) {
                mOldOffset =
                    (long) (city.getOffset() * (float) MILLISECONDS_PER_HOUR);
            } else {
                mOldOffset = 0L; 
            }
            this.mCitySwitchTime = System.currentTimeMillis();
            this.mCity = city;
        }
    }
    public void setTime(long time) {
        this.mTime = time;
    }
    public void drawClock(Canvas canvas,
        float cx, float cy, float radius, float alpha, float textAlpha,
        boolean showCityName, boolean showTime,
        boolean showUpArrow,  boolean showDownArrow, boolean showLeftRightArrows,
        int prefixChars) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int iradius = (int)radius;
        TimeZone tz = mCity.getTimeZone();
        float lerp = Math.min(1.0f,
            (System.currentTimeMillis() - mCitySwitchTime) / 500.0f);
        lerp = mClockHandInterpolator.getInterpolation(lerp);
        long doffset = lerp < 1.0f ? getOffset(lerp) : 0L;
        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(mTime - doffset);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int milli = cal.get(Calendar.MILLISECOND);
        float offset = tz.getRawOffset() / (float) MILLISECONDS_PER_HOUR;
        float daylightOffset = tz.inDaylightTime(new Date(mTime)) ?
            tz.getDSTSavings() / (float) MILLISECONDS_PER_HOUR : 0.0f;
        float absOffset = offset < 0 ? -offset : offset;
        int offsetH = (int) absOffset;
        int offsetM = (int) (60.0f * (absOffset - offsetH));
        hour %= 12;
        String cityName = mCity.getName();
        cal.setTimeInMillis(mTime);
        String time = DateUtils.timeString(cal.getTimeInMillis()) + " "  +
            DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK),
                    DateUtils.LENGTH_SHORT) + " " +
            " (UTC" +
            (offset >= 0 ? "+" : "-") +
            offsetH +
            (offsetM == 0 ? "" : ":" + offsetM) +
            (daylightOffset == 0 ? "" : "+" + daylightOffset) +
            ")";
        float th = paint.getTextSize();
        float tw;
        paint.setARGB((int) (textAlpha * 255.0f),
                      (int) (mColorRed * 255.0f),
                      (int) (mColorGreen * 255.0f),
                      (int) (mColorBlue * 255.0f));
        tw = paint.measureText(cityName);
        if (showCityName) {
            for (int i = 0; i < prefixChars; i++) {
                if (cityName.charAt(i) == ' ') {
                    ++prefixChars;
                }
            }
            canvas.drawText(cityName, cx - tw / 2, cy - radius - th, paint);
            canvas.drawText(cityName.substring(0, prefixChars),
                            cx - tw / 2 + 1, cy - radius - th, paint);
        }
        tw = paint.measureText(time);
        if (showTime) {
            canvas.drawText(time, cx - tw / 2, cy + radius + th + 5, paint);
        }
        paint.setARGB((int)(alpha * 255.0f),
                      (int)(mColorRed * 255.0f),
                      (int)(mColorGreen * 255.0f),
                      (int)(mColorBlue * 255.0f));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(cx - 2, cy - 2, cx + 2, cy + 2), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.12f);
        canvas.drawOval(new RectF(cx - iradius, cy - iradius,
                                  cx + iradius, cy + iradius),
                        paint);
        float r0 = radius * 0.1f;
        float r1 = radius * 0.4f;
        float r2 = radius * 0.6f;
        float r3 = radius * 0.65f;
        float r4 = radius * 0.7f;
        float r5 = radius * 0.9f;
        Path path = new Path();
        float ss = second + milli / 1000.0f;
        float mm = minute + ss / 60.0f;
        float hh = hour + mm / 60.0f;
        for (int i = 0; i < 12; i++) {
            drawLine(path, radius * 0.12f, i / 12.0f, cx, cy, r4, r5);
        }
        drawLine(path, radius * 0.12f, hh / 12.0f, cx, cy, r0, r1); 
        drawLine(path, radius * 0.12f, mm / 60.0f, cx, cy, r0, r2); 
        drawLine(path, radius * 0.036f, ss / 60.0f, cx, cy, r0, r3); 
        if (showUpArrow) {
            drawVArrow(path, cx + radius * 1.13f, cy - radius,
                radius * 0.15f, -radius * 0.1f);
        }
        if (showDownArrow) {
            drawVArrow(path, cx + radius * 1.13f, cy + radius,
                radius * 0.15f, radius * 0.1f);
        }
        if (showLeftRightArrows) {
            drawHArrow(path, cx - radius * 1.3f, cy, -radius * 0.1f,
                radius * 0.15f);
            drawHArrow(path, cx + radius * 1.3f, cy,  radius * 0.1f,
                radius * 0.15f);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
    }
}
