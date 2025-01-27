public class ContactView extends LinearLayout {
    static final String[] CONTACT_PROJECTION = {
        Imps.Contacts._ID,
        Imps.Contacts.PROVIDER,
        Imps.Contacts.ACCOUNT,
        Imps.Contacts.USERNAME,
        Imps.Contacts.NICKNAME,
        Imps.Contacts.TYPE,
        Imps.Contacts.SUBSCRIPTION_TYPE,
        Imps.Contacts.SUBSCRIPTION_STATUS,
        Imps.Presence.PRESENCE_STATUS,
        Imps.Presence.PRESENCE_CUSTOM_STATUS,
        Imps.Chats.LAST_MESSAGE_DATE,
        Imps.Chats.LAST_UNREAD_MESSAGE,
    };
    static final int COLUMN_CONTACT_ID = 0;
    static final int COLUMN_CONTACT_PROVIDER = 1;
    static final int COLUMN_CONTACT_ACCOUNT = 2;
    static final int COLUMN_CONTACT_USERNAME = 3;
    static final int COLUMN_CONTACT_NICKNAME = 4;
    static final int COLUMN_CONTACT_TYPE = 5;
    static final int COLUMN_SUBSCRIPTION_TYPE = 6;
    static final int COLUMN_SUBSCRIPTION_STATUS = 7;
    static final int COLUMN_CONTACT_PRESENCE_STATUS = 8;
    static final int COLUMN_CONTACT_CUSTOM_STATUS = 9;
    static final int COLUMN_LAST_MESSAGE_DATE = 10;
    static final int COLUMN_LAST_MESSAGE = 11;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mTimeStamp;
    public ContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLine1 = (TextView) findViewById(R.id.line1);
        mLine2 = (TextView) findViewById(R.id.line2);
        mLine2.setCompoundDrawablePadding(5);
        mTimeStamp = (TextView)findViewById(R.id.timestamp);
    }
    public void bind(Cursor cursor, String underLineText, boolean scrolling) {
        bind(cursor, underLineText, true, scrolling);
    }
    public void bind(Cursor cursor, String underLineText, boolean showChatMsg, boolean scrolling) {
        Resources r = getResources();
        long providerId = cursor.getLong(COLUMN_CONTACT_PROVIDER);
        String username = cursor.getString(COLUMN_CONTACT_USERNAME);
        String nickname = cursor.getString(COLUMN_CONTACT_NICKNAME);
        int type = cursor.getInt(COLUMN_CONTACT_TYPE);
        String statusText = cursor.getString(COLUMN_CONTACT_CUSTOM_STATUS);
        String lastMsg = cursor.getString(COLUMN_LAST_MESSAGE);
        boolean hasChat = !cursor.isNull(COLUMN_LAST_MESSAGE_DATE);
        ImApp app = ImApp.getApplication((Activity)mContext);
        BrandingResources brandingRes = app.getBrandingResource(providerId);
        int presence = cursor.getInt(COLUMN_CONTACT_PRESENCE_STATUS);
        int iconId = 0;
        if (Imps.Contacts.TYPE_GROUP == type) {
            iconId = lastMsg == null ? R.drawable.group_chat : R.drawable.group_chat_new;
        } else if (hasChat) {
            iconId = lastMsg == null ? BrandingResourceIDs.DRAWABLE_READ_CHAT
                    : BrandingResourceIDs.DRAWABLE_UNREAD_CHAT;
        } else {
            iconId = PresenceUtils.getStatusIconId(presence);
        }
        Drawable presenceIcon = brandingRes.getDrawable(iconId);
        CharSequence line1;
        if (Imps.Contacts.TYPE_GROUP == type) {
            ContentResolver resolver = getContext().getContentResolver();
            long id = cursor.getLong(ContactView.COLUMN_CONTACT_ID);
            line1 = queryGroupMembers(resolver, id);
        } else {
            line1 = TextUtils.isEmpty(nickname) ?
                    ImpsAddressUtils.getDisplayableAddress(username) : nickname;
            if (!TextUtils.isEmpty(underLineText)) {
                String lowercase = line1.toString().toLowerCase();
                int start = lowercase.indexOf(underLineText.toLowerCase());
                if (start >= 0) {
                    int end = start + underLineText.length();
                    SpannableString str = new SpannableString(line1);
                    str.setSpan(new UnderlineSpan(), start, end,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    line1 = str;
                }
            }
            if (Imps.Contacts.TYPE_TEMPORARY == type) {
                SpannableStringBuilder str = new SpannableStringBuilder(
                        r.getText(R.string.unknown_contact));
                str.setSpan(new RelativeSizeSpan(0.8f), 0, str.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                str.append(line1);
                line1 = str;
            }
        }
        mLine1.setText(line1);
        if (showChatMsg && hasChat) {
            mTimeStamp.setVisibility(VISIBLE);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(COLUMN_LAST_MESSAGE_DATE));
            DateFormat formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
            mTimeStamp.setText(formatter.format(cal.getTime()));
        } else {
            mTimeStamp.setVisibility(GONE);
        }
        CharSequence line2 = null;
        if (showChatMsg) {
            line2 = lastMsg;
        }
        if (TextUtils.isEmpty(line2)){
            if (Imps.Contacts.TYPE_GROUP == type) {
                line2 = null;
            } else {
                line2 = statusText;
            }
        }
        if (TextUtils.isEmpty(line2)) {
            line2 = brandingRes.getString(PresenceUtils.getStatusStringRes(presence));
        }
        mLine2.setText(line2);
        mLine2.setCompoundDrawablesWithIntrinsicBounds(null, null, presenceIcon, null);
        View contactInfoPanel = findViewById(R.id.contactInfo);
        if (hasChat && showChatMsg) {
            contactInfoPanel.setBackgroundResource(R.drawable.bubble);
            mLine1.setTextColor(r.getColor(R.color.chat_contact));
        } else {
            contactInfoPanel.setBackgroundDrawable(null);
            contactInfoPanel.setPadding(4, 0, 0, 0);
            mLine1.setTextColor(r.getColor(R.color.nonchat_contact));
        }
    }
    private String queryGroupMembers(ContentResolver resolver, long groupId) {
        String[] projection = { Imps.GroupMembers.NICKNAME };
        Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
        Cursor c = resolver.query(uri, projection, null, null, null);
        StringBuilder buf = new StringBuilder();
        if(c != null) {
            while(c.moveToNext()) {
                buf.append(c.getString(0));
                if(!c.isLast()) {
                    buf.append(',');
                }
            }
            c.close();
        }
        return buf.toString();
    }
}
