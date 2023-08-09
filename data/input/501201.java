public class NotificationData {
    public String pkg;
    public String tag;
    public int id;
    public CharSequence tickerText;
    public long when;
    public boolean ongoingEvent;
    public boolean clearable;
    public RemoteViews contentView;
    public PendingIntent contentIntent;
    public PendingIntent deleteIntent;
    public String toString() {
        return "NotificationData(package=" + pkg + " id=" + id + " tickerText=" + tickerText
                + " ongoingEvent=" + ongoingEvent + " contentIntent=" + contentIntent
                + " deleteIntent=" + deleteIntent
                + " clearable=" + clearable
                + " contentView=" + contentView + " when=" + when + ")";
    }
}
