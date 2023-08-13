public final class DetailMode {
    public static CharSequence[] populateDetailModeStrings(Context context, ArrayList<MediaBucket> buckets) {
        int numBuckets = buckets.size();
        if (MediaBucketList.isSetSelection(buckets) && numBuckets == 1) {
            return populateSetViewDetailModeStrings(context, MediaBucketList.getFirstSetSelection(buckets), 1);
        } else if (MediaBucketList.isSetSelection(buckets) || MediaBucketList.isMultipleItemSelection(buckets)) {
            MediaSet selectedItemsSet = new MediaSet();
            for (int i = 0; i < numBuckets; i++) {
                MediaBucket bucket = buckets.get(i);
                ArrayList<MediaItem> currItems = null;
                int numCurrItems = 0;
                if (MediaBucketList.isSetSelection(bucket)) {
                    MediaSet currSet = bucket.mediaSet;
                    if (currSet != null) {
                        currItems = currSet.getItems();
                        numCurrItems = currSet.getNumItems();
                    }
                } else {
                    currItems = bucket.mediaItems;
                    numCurrItems = currItems.size();
                }
                if (currItems != null) {
                    for (int j = 0; j < numCurrItems; j++) {
                        selectedItemsSet.addItem(currItems.get(j));
                    }
                }
            }
            return populateSetViewDetailModeStrings(context, selectedItemsSet, numBuckets);
        } else {
            return populateItemViewDetailModeStrings(context, MediaBucketList.getFirstItemSelection(buckets));
        }
    }
    private static CharSequence[] populateSetViewDetailModeStrings(Context context, MediaSet selectedItemsSet, int numOriginalSets) {
        if (selectedItemsSet == null) {
            return null;
        }
        Resources resources = context.getResources();
        ArrayList<CharSequence> strings = new ArrayList<CharSequence>();
        if (numOriginalSets == 1) {
            strings.add("1 " + resources.getString(Res.string.album_selected));
        } else {
            strings.add(Integer.toString(numOriginalSets) + " " + resources.getString(Res.string.albums_selected));
        }
        int numItems = selectedItemsSet.mNumItemsLoaded;
        if (numItems == 1) {
            strings.add("1 " + resources.getString(Res.string.item_selected));
        } else {
            strings.add(Integer.toString(numItems) + " " + resources.getString(Res.string.items_selected));
        }
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        if (selectedItemsSet.areTimestampsAvailable()) {
            long minTimestamp = selectedItemsSet.mMinTimestamp;
            long maxTimestamp = selectedItemsSet.mMaxTimestamp;
            if (selectedItemsSet.isPicassaSet()) {
                minTimestamp -= App.CURRENT_TIME_ZONE.getOffset(minTimestamp);
                maxTimestamp -= App.CURRENT_TIME_ZONE.getOffset(maxTimestamp);
            }
            strings.add(resources.getString(Res.string.start) + ": " + dateTimeFormat.format(new Date(minTimestamp)));
            strings.add(resources.getString(Res.string.end) + ": " + dateTimeFormat.format(new Date(maxTimestamp)));
        } else if (selectedItemsSet.areAddedTimestampsAvailable()) {
            long minTimestamp = selectedItemsSet.mMinAddedTimestamp;
            long maxTimestamp = selectedItemsSet.mMaxAddedTimestamp;
            if (selectedItemsSet.isPicassaSet()) {
                minTimestamp -= App.CURRENT_TIME_ZONE.getOffset(minTimestamp);
                maxTimestamp -= App.CURRENT_TIME_ZONE.getOffset(maxTimestamp);
            }
            strings.add(resources.getString(Res.string.start) + ": " + dateTimeFormat.format(new Date(minTimestamp)));
            strings.add(resources.getString(Res.string.end) + ": " + dateTimeFormat.format(new Date(maxTimestamp)));
        } else {
            strings.add(resources.getString(Res.string.start) + ": " + resources.getString(Res.string.date_unknown));
            strings.add(resources.getString(Res.string.end) + ": " + resources.getString(Res.string.date_unknown));
        }
        String locationString = null;
        if (selectedItemsSet.mLatLongDetermined) {
            locationString = selectedItemsSet.mReverseGeocodedLocation;
            if (locationString == null) {
                ReverseGeocoder reverseGeocoder = App.get(context).getReverseGeocoder();
                locationString = reverseGeocoder.computeMostGranularCommonLocation(selectedItemsSet);
            }
        }
        if (locationString != null && locationString.length() > 0) {
            strings.add(resources.getString(Res.string.location) + ": " + locationString);
        }
        int numStrings = strings.size();
        CharSequence[] stringsArr = new CharSequence[numStrings];
        for (int i = 0; i < numStrings; ++i) {
            stringsArr[i] = strings.get(i);
        }
        return stringsArr;
    }
    private static CharSequence[] populateItemViewDetailModeStrings(Context context, MediaItem item) {
        if (item == null) {
            return null;
        }
        Resources resources = context.getResources();
        CharSequence[] strings = new CharSequence[5];
        strings[0] = resources.getString(Res.string.title) + ": " + item.mCaption;
        strings[1] = resources.getString(Res.string.type) + ": " + item.getDisplayMimeType();
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        if (item.mLocaltime == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            try {
                ExifInterface exif = new ExifInterface(item.mFilePath);
                String localtime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (localtime != null) {
                    item.mLocaltime = formatter.parse(localtime, new ParsePosition(0));
                }
            } catch (IOException ex) {
            }
            if (item.mLocaltime == null && item.mCaption != null) {
                formatter = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
                item.mLocaltime = formatter.parse(item.mCaption, new ParsePosition(4));
            }
        }
        if (item.mLocaltime != null) {
            strings[2] = resources.getString(Res.string.taken_on) + ": " + dateTimeFormat.format(item.mLocaltime);
        } else if (item.isDateTakenValid()) {
            long dateTaken = item.mDateTakenInMs;
            if (item.isPicassaItem()) {
                dateTaken -= App.CURRENT_TIME_ZONE.getOffset(dateTaken);
            }
            strings[2] = resources.getString(Res.string.taken_on) + ": " + dateTimeFormat.format(new Date(dateTaken));
        } else if (item.isDateAddedValid()) {
            long dateAdded = item.mDateAddedInSec * 1000;
            if (item.isPicassaItem()) {
                dateAdded -= App.CURRENT_TIME_ZONE.getOffset(dateAdded);
            }
            strings[2] = resources.getString(Res.string.taken_on) + ": " + dateTimeFormat.format(new Date(dateAdded));
        } else {
            strings[2] = resources.getString(Res.string.taken_on) + ": " + resources.getString(Res.string.date_unknown);
        }
        MediaSet parentMediaSet = item.mParentMediaSet;
        if (parentMediaSet == null) {
            strings[3] = resources.getString(Res.string.album) + ":";
        } else {
            strings[3] = resources.getString(Res.string.album) + ": " + parentMediaSet.mName;
        }
        ReverseGeocoder reverseGeocoder = App.get(context).getReverseGeocoder();
        String locationString = item.getReverseGeocodedLocation(reverseGeocoder);
        if (locationString == null || locationString.length() == 0) {
            locationString = context.getResources().getString(Res.string.location_unknown);
        }
        strings[4] = resources.getString(Res.string.location) + ": " + locationString;
        return strings;
    }
}