public class ResultInfo implements Parcelable {
    public final String mResultWho;
    public final int mRequestCode;
    public final int mResultCode;
    public final Intent mData;
    public ResultInfo(String resultWho, int requestCode, int resultCode,
            Intent data) {
        mResultWho = resultWho;
        mRequestCode = requestCode;
        mResultCode = resultCode;
        mData = data;
    }
    public String toString() {
        return "ResultInfo{who=" + mResultWho + ", request=" + mRequestCode
            + ", result=" + mResultCode + ", data=" + mData + "}";
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mResultWho);
        out.writeInt(mRequestCode);
        out.writeInt(mResultCode);
        if (mData != null) {
            out.writeInt(1);
            mData.writeToParcel(out, 0);
        } else {
            out.writeInt(0);
        }
    }
    public static final Parcelable.Creator<ResultInfo> CREATOR
            = new Parcelable.Creator<ResultInfo>() {
        public ResultInfo createFromParcel(Parcel in) {
            return new ResultInfo(in);
        }
        public ResultInfo[] newArray(int size) {
            return new ResultInfo[size];
        }
    };
    public ResultInfo(Parcel in) {
        mResultWho = in.readString();
        mRequestCode = in.readInt();
        mResultCode = in.readInt();
        if (in.readInt() != 0) {
            mData = Intent.CREATOR.createFromParcel(in);
        } else {
            mData = null;
        }
    }
}
