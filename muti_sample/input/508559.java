class LiveFolderAdapter extends CursorAdapter {
    private boolean mIsList;
    private LayoutInflater mInflater;
    private final HashMap<String, Drawable> mIcons = new HashMap<String, Drawable>();
    private final HashMap<Long, SoftReference<Drawable>> mCustomIcons =
            new HashMap<Long, SoftReference<Drawable>>();
    private final Launcher mLauncher;
    LiveFolderAdapter(Launcher launcher, LiveFolderInfo info, Cursor cursor) {
        super(launcher, cursor, true);
        mIsList = info.displayMode == LiveFolders.DISPLAY_MODE_LIST;
        mInflater = LayoutInflater.from(launcher);
        mLauncher = launcher;
        mLauncher.startManagingCursor(getCursor());
    }
    static Cursor query(Context context, LiveFolderInfo info) {
        return context.getContentResolver().query(info.uri, null, null,
                null, LiveFolders.NAME + " ASC");
    }
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        final ViewHolder holder = new ViewHolder();
        if (!mIsList) {
            view = mInflater.inflate(R.layout.application_boxed, parent, false);
        } else {
            view = mInflater.inflate(R.layout.application_list, parent, false);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.icon = (ImageView) view.findViewById(R.id.icon);
        }
        holder.name = (TextView) view.findViewById(R.id.name);
        holder.idIndex = cursor.getColumnIndexOrThrow(LiveFolders._ID);
        holder.nameIndex = cursor.getColumnIndexOrThrow(LiveFolders.NAME);
        holder.descriptionIndex = cursor.getColumnIndex(LiveFolders.DESCRIPTION);
        holder.intentIndex = cursor.getColumnIndex(LiveFolders.INTENT);
        holder.iconBitmapIndex = cursor.getColumnIndex(LiveFolders.ICON_BITMAP);
        holder.iconResourceIndex = cursor.getColumnIndex(LiveFolders.ICON_RESOURCE);
        holder.iconPackageIndex = cursor.getColumnIndex(LiveFolders.ICON_PACKAGE);
        view.setTag(holder);
        return view;
    }
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.id = cursor.getLong(holder.idIndex);
        final Drawable icon = loadIcon(context, cursor, holder);
        holder.name.setText(cursor.getString(holder.nameIndex));
        if (!mIsList) {
            holder.name.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        } else {
            final boolean hasIcon = icon != null;
            holder.icon.setVisibility(hasIcon ? View.VISIBLE : View.GONE);
            if (hasIcon) holder.icon.setImageDrawable(icon);
            if (holder.descriptionIndex != -1) {
                final String description = cursor.getString(holder.descriptionIndex);
                if (description != null) {
                    holder.description.setText(description);
                    holder.description.setVisibility(View.VISIBLE);
                } else {
                    holder.description.setVisibility(View.GONE);                    
                }
            } else {
                holder.description.setVisibility(View.GONE);                
            }
        }
        if (holder.intentIndex != -1) {
            try {
                holder.intent = Intent.parseUri(cursor.getString(holder.intentIndex), 0);
            } catch (URISyntaxException e) {
            }
        } else {
            holder.useBaseIntent = true;
        }
    }
    private Drawable loadIcon(Context context, Cursor cursor, ViewHolder holder) {
        Drawable icon = null;
        byte[] data = null;
        if (holder.iconBitmapIndex != -1) {
            data = cursor.getBlob(holder.iconBitmapIndex);
        }
        if (data != null) {
            final SoftReference<Drawable> reference = mCustomIcons.get(holder.id);
            if (reference != null) {
                icon = reference.get();
            }
            if (icon == null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                final Bitmap resampled = Utilities.resampleIconBitmap(bitmap, mContext);
                if (bitmap != resampled) {
                    bitmap.recycle();
                }
                icon = new FastBitmapDrawable(resampled);
                mCustomIcons.put(holder.id, new SoftReference<Drawable>(icon));
            }
        } else if (holder.iconResourceIndex != -1 && holder.iconPackageIndex != -1) {
            final String resource = cursor.getString(holder.iconResourceIndex);
            icon = mIcons.get(resource);
            if (icon == null) {
                try {
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            cursor.getString(holder.iconPackageIndex));
                    final int id = resources.getIdentifier(resource,
                            null, null);
                    icon = new FastBitmapDrawable(
                            Utilities.createIconBitmap(resources.getDrawable(id), mContext));
                    mIcons.put(resource, icon);
                } catch (Exception e) {
                }
            }
        }
        return icon;
    }
    void cleanup() {
        for (Drawable icon : mIcons.values()) {
            icon.setCallback(null);
        }
        mIcons.clear();
        for (SoftReference<Drawable> icon : mCustomIcons.values()) {
            final Drawable drawable = icon.get();
            if (drawable != null) {
                drawable.setCallback(null);
            }
        }
        mCustomIcons.clear();
        final Cursor cursor = getCursor();
        if (cursor != null) {
            try {
                cursor.close();
            } finally {
                mLauncher.stopManagingCursor(cursor);
            }
        }
    }
    static class ViewHolder {
        TextView name;
        TextView description;
        ImageView icon;
        Intent intent;
        long id;
        boolean useBaseIntent;
        int idIndex;
        int nameIndex;
        int descriptionIndex = -1;
        int intentIndex = -1;
        int iconBitmapIndex = -1;
        int iconResourceIndex = -1;
        int iconPackageIndex = -1;
    }
}
