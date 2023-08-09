public class ContactsUtils {
    private static final String TAG = "ContactsUtils";
    private static final String WAIT_SYMBOL_AS_STRING = String.valueOf(PhoneNumberUtils.WAIT);
    public static final CharSequence getDisplayLabel(Context context,
            String mimeType, Cursor cursor) {
        int colType;
        int colLabel;
        if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)
                || Constants.MIME_SMS_ADDRESS.equals(mimeType)) {
            mimeType = Phone.CONTENT_ITEM_TYPE;
            colType = cursor.getColumnIndex(Phone.TYPE);
            colLabel = cursor.getColumnIndex(Phone.LABEL);
        } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(Email.TYPE);
            colLabel = cursor.getColumnIndex(Email.LABEL);
        } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(StructuredPostal.TYPE);
            colLabel = cursor.getColumnIndex(StructuredPostal.LABEL);
        } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(Organization.TYPE);
            colLabel = cursor.getColumnIndex(Organization.LABEL);
        } else {
            return null;
        }
        final int type = cursor.getInt(colType);
        final CharSequence label = cursor.getString(colLabel);
        return getDisplayLabel(context, mimeType, type, label);
    }
    public static final CharSequence getDisplayLabel(Context context, String mimetype, int type,
            CharSequence label) {
        CharSequence display = "";
        final int customType;
        final int defaultType;
        final int arrayResId;
        if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Phone.TYPE_HOME;
            customType = Phone.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.phoneTypes;
        } else if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Email.TYPE_HOME;
            customType = Email.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.emailAddressTypes;
        } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = StructuredPostal.TYPE_HOME;
            customType = StructuredPostal.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.postalAddressTypes;
        } else if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Organization.TYPE_WORK;
            customType = Organization.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.organizationTypes;
        } else {
            return display;
        }
        if (type != customType) {
            CharSequence[] labels = context.getResources().getTextArray(arrayResId);
            try {
                display = labels[type - 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                display = labels[defaultType - 1];
            }
        } else {
            if (!TextUtils.isEmpty(label)) {
                display = label;
            }
        }
        return display;
    }
    public static Bitmap loadContactPhoto(Cursor cursor, int bitmapColumnIndex,
            BitmapFactory.Options options) {
        if (cursor == null) {
            return null;
        }
        byte[] data = cursor.getBlob(bitmapColumnIndex);
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
    public static Bitmap loadPlaceholderPhoto(int placeholderImageResource, Context context,
            BitmapFactory.Options options) {
        if (placeholderImageResource == 0) {
            return null;
        }
        return BitmapFactory.decodeResource(context.getResources(),
                placeholderImageResource, options);
    }
    public static Bitmap loadContactPhoto(Context context, long photoId,
            BitmapFactory.Options options) {
        Cursor photoCursor = null;
        Bitmap photoBm = null;
        try {
            photoCursor = context.getContentResolver().query(
                    ContentUris.withAppendedId(Data.CONTENT_URI, photoId),
                    new String[] { Photo.PHOTO },
                    null, null, null);
            if (photoCursor.moveToFirst() && !photoCursor.isNull(0)) {
                byte[] photoData = photoCursor.getBlob(0);
                photoBm = BitmapFactory.decodeByteArray(photoData, 0,
                        photoData.length, options);
            }
        } finally {
            if (photoCursor != null) {
                photoCursor.close();
            }
        }
        return photoBm;
    }
    public interface ProviderNames {
        String YAHOO = "Yahoo";
        String GTALK = "GTalk";
        String MSN = "MSN";
        String ICQ = "ICQ";
        String AIM = "AIM";
        String XMPP = "XMPP";
        String JABBER = "JABBER";
        String SKYPE = "SKYPE";
        String QQ = "QQ";
    }
    public static String lookupProviderNameFromId(int protocol) {
        switch (protocol) {
            case Im.PROTOCOL_GOOGLE_TALK:
                return ProviderNames.GTALK;
            case Im.PROTOCOL_AIM:
                return ProviderNames.AIM;
            case Im.PROTOCOL_MSN:
                return ProviderNames.MSN;
            case Im.PROTOCOL_YAHOO:
                return ProviderNames.YAHOO;
            case Im.PROTOCOL_ICQ:
                return ProviderNames.ICQ;
            case Im.PROTOCOL_JABBER:
                return ProviderNames.JABBER;
            case Im.PROTOCOL_SKYPE:
                return ProviderNames.SKYPE;
            case Im.PROTOCOL_QQ:
                return ProviderNames.QQ;
        }
        return null;
    }
    public static Intent buildImIntent(ContentValues values) {
        final boolean isEmail = Email.CONTENT_ITEM_TYPE.equals(values.getAsString(Data.MIMETYPE));
        if (!isEmail && !isProtocolValid(values)) {
            return null;
        }
        final int protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : values.getAsInteger(Im.PROTOCOL);
        String host = values.getAsString(Im.CUSTOM_PROTOCOL);
        String data = values.getAsString(isEmail ? Email.DATA : Im.DATA);
        if (protocol != Im.PROTOCOL_CUSTOM) {
            host = ContactsUtils.lookupProviderNameFromId(protocol);
        }
        if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(data)) {
            final String authority = host.toLowerCase();
            final Uri imUri = new Uri.Builder().scheme(Constants.SCHEME_IMTO).authority(
                    authority).appendPath(data).build();
            return new Intent(Intent.ACTION_SENDTO, imUri);
        } else {
            return null;
        }
    }
    private static boolean isProtocolValid(ContentValues values) {
        String protocolString = values.getAsString(Im.PROTOCOL);
        if (protocolString == null) {
            return false;
        }
        try {
            Integer.valueOf(protocolString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    public static long queryForContactId(ContentResolver cr, long rawContactId) {
        Cursor contactIdCursor = null;
        long contactId = -1;
        try {
            contactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts.CONTACT_ID},
                    RawContacts._ID + "=" + rawContactId, null, null);
            if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
                contactId = contactIdCursor.getLong(0);
            }
        } finally {
            if (contactIdCursor != null) {
                contactIdCursor.close();
            }
        }
        return contactId;
    }
    public static String querySuperPrimaryPhone(ContentResolver cr, long contactId) {
        Cursor c = null;
        String phone = null;
        try {
            Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);
            c = cr.query(dataUri,
                    new String[] {Phone.NUMBER},
                    Data.MIMETYPE + "=" + Phone.MIMETYPE +
                        " AND " + Data.IS_SUPER_PRIMARY + "=1",
                    null, null);
            if (c != null && c.moveToFirst()) {
                phone = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return phone;
    }
    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    public static ArrayList<Long> queryForAllRawContactIds(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        ArrayList<Long> rawContactIds = new ArrayList<Long>();
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null) {
                while (rawContactIdCursor.moveToNext()) {
                    rawContactIds.add(rawContactIdCursor.getLong(0));
                }
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactIds;
    }
    public static View createTabIndicatorView(ViewGroup parent, CharSequence label, Drawable icon) {
        final LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View tabIndicator = inflater.inflate(R.layout.tab_indicator, parent, false);
        tabIndicator.getBackground().setDither(true);
        final TextView tv = (TextView) tabIndicator.findViewById(R.id.tab_title);
        tv.setText(label);
        final ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.tab_icon);
        iconView.setImageDrawable(icon);
        return tabIndicator;
    }
    public static View createTabIndicatorView(ViewGroup parent, ContactsSource source) {
        Drawable icon = null;
        if (source != null) {
            icon = source.getDisplayIcon(parent.getContext());
        }
        return createTabIndicatorView(parent, null, icon);
    }
    public static void initiateCall(Context context, CharSequence phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("tel", phoneNumber.toString(), null));
        context.startActivity(intent);
    }
    public static void initiateSms(Context context, CharSequence phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("sms", phoneNumber.toString(), null));
        context.startActivity(intent);
    }
    public static boolean isGraphic(CharSequence str) {
        return !TextUtils.isEmpty(str) && TextUtils.isGraphic(str);
    }
    public static boolean areObjectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
    public static final boolean shouldCollapse(Context context, CharSequence mimetype1,
            CharSequence data1, CharSequence mimetype2, CharSequence data2) {
        if (TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimetype1)
                && TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimetype2)) {
            if (data1 == data2) {
                return true;
            }
            if (data1 == null || data2 == null) {
                return false;
            }
            String[] dataParts1 = data1.toString().split(WAIT_SYMBOL_AS_STRING);
            String[] dataParts2 = data2.toString().split(WAIT_SYMBOL_AS_STRING);
            if (dataParts1.length != dataParts2.length) {
                return false;
            }
            for (int i = 0; i < dataParts1.length; i++) {
                if (!PhoneNumberUtils.compare(context, dataParts1[i], dataParts2[i])) {
                    return false;
                }
            }
            return true;
        } else {
            if (mimetype1 == mimetype2 && data1 == data2) {
                return true;
            }
            return TextUtils.equals(mimetype1, mimetype2) && TextUtils.equals(data1, data2);
        }
    }
    public static final boolean areIntentActionEqual(Intent a, Intent b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return TextUtils.equals(a.getAction(), b.getAction());
    }
}
