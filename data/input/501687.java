    public static class Standard
    implements AlignmentSpan, ParcelableSpan {
        public Standard(Layout.Alignment align) {
            mAlignment = align;
        }
        public Standard(Parcel src) {
            mAlignment = Layout.Alignment.valueOf(src.readString());
        }
        public int getSpanTypeId() {
            return TextUtils.ALIGNMENT_SPAN;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mAlignment.name());
        }
        public Layout.Alignment getAlignment() {
            return mAlignment;
        }
        private final Layout.Alignment mAlignment;
    }
}
