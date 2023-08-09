    public static class Standard implements LeadingMarginSpan, ParcelableSpan {
        private final int mFirst, mRest;
        public Standard(int first, int rest) {
            mFirst = first;
            mRest = rest;
        }
        public Standard(int every) {
            this(every, every);
        }
        public Standard(Parcel src) {
            mFirst = src.readInt();
            mRest = src.readInt();
        }
        public int getSpanTypeId() {
            return TextUtils.LEADING_MARGIN_SPAN;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mFirst);
            dest.writeInt(mRest);
        }
        public int getLeadingMargin(boolean first) {
            return first ? mFirst : mRest;
        }
        public void drawLeadingMargin(Canvas c, Paint p,
                                      int x, int dir,
                                      int top, int baseline, int bottom,
                                      CharSequence text, int start, int end,
                                      boolean first, Layout layout) {
            ;
        }
    }
}
