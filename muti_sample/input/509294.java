public class Helpers {
    public static Random sRandom = new Random(SystemClock.uptimeMillis());
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");
    private Helpers() {
    }
    private static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(1);
            }
        } catch (IllegalStateException ex) {
        }
        return null;
    }
    public static DownloadFileInfo generateSaveFile(
            Context context,
            String url,
            String hint,
            String contentDisposition,
            String contentLocation,
            String mimeType,
            int destination,
            int contentLength) throws FileNotFoundException {
        if (destination == Downloads.Impl.DESTINATION_EXTERNAL
                || destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE) {
            if (mimeType == null) {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "external download with no mime type not allowed");
                }
                return new DownloadFileInfo(null, null, Downloads.Impl.STATUS_NOT_ACCEPTABLE);
            }
            if (!DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING.equalsIgnoreCase(mimeType)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                PackageManager pm = context.getPackageManager();
                intent.setDataAndType(Uri.fromParts("file", "", null), mimeType);
                ResolveInfo ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (ri == null) {
                    if (Config.LOGD) {
                        Log.d(Constants.TAG, "no handler found for type " + mimeType);
                    }
                    return new DownloadFileInfo(null, null, Downloads.Impl.STATUS_NOT_ACCEPTABLE);
                }
            }
        }
        String filename = chooseFilename(
                url, hint, contentDisposition, contentLocation, destination);
        String extension = null;
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            extension = chooseExtensionFromMimeType(mimeType, true);
        } else {
            extension = chooseExtensionFromFilename(
                    mimeType, destination, filename, dotIndex);
            filename = filename.substring(0, dotIndex);
        }
        File base = null;
        StatFs stat = null;
        if (destination == Downloads.Impl.DESTINATION_CACHE_PARTITION
                || destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE
                || destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING
                || DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING.equalsIgnoreCase(mimeType)) {
            base = Environment.getDownloadCacheDirectory();
            stat = new StatFs(base.getPath());
            int blockSize = stat.getBlockSize();
            long bytesAvailable = blockSize * ((long) stat.getAvailableBlocks() - 4);
            while (bytesAvailable < contentLength) {
                if (!discardPurgeableFiles(context, contentLength - bytesAvailable)) {
                    if (Config.LOGD) {
                        Log.d(Constants.TAG,
                                "download aborted - not enough free space in internal storage");
                    }
                    return new DownloadFileInfo(null, null,
                            Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR);
                } else {
                    stat.restat(base.getPath());
                    bytesAvailable = blockSize * ((long) stat.getAvailableBlocks() - 4);
                }
            }
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String root = Environment.getExternalStorageDirectory().getPath();
            stat = new StatFs(root);
            if (stat.getBlockSize() * ((long) stat.getAvailableBlocks() - 4) < contentLength) {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "download aborted - not enough free space");
                }
                return new DownloadFileInfo(null, null,
                        Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR);
            }
            base = new File(root + Constants.DEFAULT_DL_SUBDIR);
            if (!base.isDirectory() && !base.mkdir()) {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "download aborted - can't create base directory "
                            + base.getPath());
                }
                return new DownloadFileInfo(null, null, Downloads.Impl.STATUS_FILE_ERROR);
            }
        } else {
            if (Config.LOGD) {
                Log.d(Constants.TAG, "download aborted - no external storage");
            }
            return new DownloadFileInfo(null, null,
                    Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR);
        }
        boolean recoveryDir = Constants.RECOVERY_DIRECTORY.equalsIgnoreCase(filename + extension);
        filename = base.getPath() + File.separator + filename;
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "target file: " + filename + extension);
        }
        String fullFilename = chooseUniqueFilename(
                destination, filename, extension, recoveryDir);
        if (fullFilename != null) {
            return new DownloadFileInfo(fullFilename, new FileOutputStream(fullFilename), 0);
        } else {
            return new DownloadFileInfo(null, null, Downloads.Impl.STATUS_FILE_ERROR);
        }
    }
    private static String chooseFilename(String url, String hint, String contentDisposition,
            String contentLocation, int destination) {
        String filename = null;
        if (filename == null && hint != null && !hint.endsWith("/")) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "getting filename from hint");
            }
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "getting filename from content-disposition");
                }
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }
        if (filename == null && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null
                    && !decodedContentLocation.endsWith("/")
                    && decodedContentLocation.indexOf('?') < 0) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "getting filename from content-location");
                }
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
            }
        }
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null
                    && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "getting filename from uri");
                    }
                    filename = decodedUrl.substring(index);
                }
            }
        }
        if (filename == null) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "using default filename");
            }
            filename = Constants.DEFAULT_DL_FILENAME;
        }
        filename = filename.replaceAll("[^a-zA-Z0-9\\.\\-_]+", "_");
        return filename;
    }
    private static String chooseExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "adding extension from type");
                }
                extension = "." + extension;
            } else {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "couldn't find extension for " + mimeType);
                }
            }
        }
        if (extension == null) {
            if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "adding default html extension");
                    }
                    extension = Constants.DEFAULT_DL_HTML_EXTENSION;
                } else if (useDefaults) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "adding default text extension");
                    }
                    extension = Constants.DEFAULT_DL_TEXT_EXTENSION;
                }
            } else if (useDefaults) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "adding default binary extension");
                }
                extension = Constants.DEFAULT_DL_BINARY_EXTENSION;
            }
        }
        return extension;
    }
    private static String chooseExtensionFromFilename(String mimeType, int destination,
            String filename, int dotIndex) {
        String extension = null;
        if (mimeType != null) {
            int lastDotIndex = filename.lastIndexOf('.');
            String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    filename.substring(lastDotIndex + 1));
            if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = chooseExtensionFromMimeType(mimeType, false);
                if (extension != null) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "substituting extension from type");
                    }
                } else {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "couldn't find extension for " + mimeType);
                    }
                }
            }
        }
        if (extension == null) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "keeping extension");
            }
            extension = filename.substring(dotIndex);
        }
        return extension;
    }
    private static String chooseUniqueFilename(int destination, String filename,
            String extension, boolean recoveryDir) {
        String fullFilename = filename + extension;
        if (!new File(fullFilename).exists()
                && (!recoveryDir ||
                (destination != Downloads.Impl.DESTINATION_CACHE_PARTITION &&
                        destination != Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE &&
                        destination != Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING))) {
            return fullFilename;
        }
        filename = filename + Constants.FILENAME_SEQUENCE_SEPARATOR;
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "file with sequence number " + sequence + " exists");
                }
                sequence += sRandom.nextInt(magnitude) + 1;
            }
        }
        return null;
    }
    public static final boolean discardPurgeableFiles(Context context, long targetBytes) {
        Cursor cursor = context.getContentResolver().query(
                Downloads.Impl.CONTENT_URI,
                null,
                "( " +
                Downloads.Impl.COLUMN_STATUS + " = '" + Downloads.Impl.STATUS_SUCCESS + "' AND " +
                Downloads.Impl.COLUMN_DESTINATION +
                        " = '" + Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE + "' )",
                null,
                Downloads.Impl.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            return false;
        }
        long totalFreed = 0;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && totalFreed < targetBytes) {
                File file = new File(cursor.getString(cursor.getColumnIndex(Downloads.Impl._DATA)));
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "purging " + file.getAbsolutePath() + " for " +
                            file.length() + " bytes");
                }
                totalFreed += file.length();
                file.delete();
                long id = cursor.getLong(cursor.getColumnIndex(Downloads.Impl._ID));
                context.getContentResolver().delete(
                        ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, id), null, null);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        if (Constants.LOGV) {
            if (totalFreed > 0) {
                Log.v(Constants.TAG, "Purged files, freed " + totalFreed + " for " +
                        targetBytes + " requested");
            }
        }
        return totalFreed > 0;
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (Constants.LOGVV) {
                            Log.v(Constants.TAG, "network is available");
                        }
                        return true;
                    }
                }
            }
        }
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "network is not available");
        }
        return false;
    }
    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (TelephonyManager.getDefault().isNetworkRoaming()) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "network is roaming");
                    }
                    return true;
                } else {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "network is not roaming");
                    }
                }
            } else {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "not using mobile network");
                }
            }
        }
        return false;
    }
    public static boolean isFilenameValid(String filename) {
        File dir = new File(filename).getParentFile();
        return dir.equals(Environment.getDownloadCacheDirectory())
                || dir.equals(new File(Environment.getExternalStorageDirectory()
                        + Constants.DEFAULT_DL_SUBDIR));
    }
    public static void validateSelection(String selection, Set<String> allowedColumns) {
        try {
            if (selection == null) {
                return;
            }
            Lexer lexer = new Lexer(selection, allowedColumns);
            parseExpression(lexer);
            if (lexer.currentToken() != Lexer.TOKEN_END) {
                throw new IllegalArgumentException("syntax error");
            }
        } catch (RuntimeException ex) {
            if (Constants.LOGV) {
                Log.d(Constants.TAG, "invalid selection [" + selection + "] triggered " + ex);
            } else if (Config.LOGD) {
                Log.d(Constants.TAG, "invalid selection triggered " + ex);
            }
            throw ex;
        }
    }
    private static void parseExpression(Lexer lexer) {
        for (;;) {
            if (lexer.currentToken() == Lexer.TOKEN_OPEN_PAREN) {
                lexer.advance();
                parseExpression(lexer);
                if (lexer.currentToken() != Lexer.TOKEN_CLOSE_PAREN) {
                    throw new IllegalArgumentException("syntax error, unmatched parenthese");
                }
                lexer.advance();
            } else {
                parseStatement(lexer);
            }
            if (lexer.currentToken() != Lexer.TOKEN_AND_OR) {
                break;
            }
            lexer.advance();
        }
    }
    private static void parseStatement(Lexer lexer) {
        if (lexer.currentToken() != Lexer.TOKEN_COLUMN) {
            throw new IllegalArgumentException("syntax error, expected column name");
        }
        lexer.advance();
        if (lexer.currentToken() == Lexer.TOKEN_COMPARE) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_VALUE) {
                throw new IllegalArgumentException("syntax error, expected quoted string");
            }
            lexer.advance();
            return;
        }
        if (lexer.currentToken() == Lexer.TOKEN_IS) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_NULL) {
                throw new IllegalArgumentException("syntax error, expected NULL");
            }
            lexer.advance();
            return;
        }
        throw new IllegalArgumentException("syntax error after column name");
    }
    private static class Lexer {
        public static final int TOKEN_START = 0;
        public static final int TOKEN_OPEN_PAREN = 1;
        public static final int TOKEN_CLOSE_PAREN = 2;
        public static final int TOKEN_AND_OR = 3;
        public static final int TOKEN_COLUMN = 4;
        public static final int TOKEN_COMPARE = 5;
        public static final int TOKEN_VALUE = 6;
        public static final int TOKEN_IS = 7;
        public static final int TOKEN_NULL = 8;
        public static final int TOKEN_END = 9;
        private final String mSelection;
        private final Set<String> mAllowedColumns;
        private int mOffset = 0;
        private int mCurrentToken = TOKEN_START;
        private final char[] mChars;
        public Lexer(String selection, Set<String> allowedColumns) {
            mSelection = selection;
            mAllowedColumns = allowedColumns;
            mChars = new char[mSelection.length()];
            mSelection.getChars(0, mChars.length, mChars, 0);
            advance();
        }
        public int currentToken() {
            return mCurrentToken;
        }
        public void advance() {
            char[] chars = mChars;
            while (mOffset < chars.length && chars[mOffset] == ' ') {
                ++mOffset;
            }
            if (mOffset == chars.length) {
                mCurrentToken = TOKEN_END;
                return;
            }
            if (chars[mOffset] == '(') {
                ++mOffset;
                mCurrentToken = TOKEN_OPEN_PAREN;
                return;
            }
            if (chars[mOffset] == ')') {
                ++mOffset;
                mCurrentToken = TOKEN_CLOSE_PAREN;
                return;
            }
            if (chars[mOffset] == '?') {
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }
            if (chars[mOffset] == '=') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }
            if (chars[mOffset] == '>') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }
            if (chars[mOffset] == '<') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && (chars[mOffset] == '=' || chars[mOffset] == '>')) {
                    ++mOffset;
                }
                return;
            }
            if (chars[mOffset] == '!') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                    return;
                }
                throw new IllegalArgumentException("Unexpected character after !");
            }
            if (isIdentifierStart(chars[mOffset])) {
                int startOffset = mOffset;
                ++mOffset;
                while (mOffset < chars.length && isIdentifierChar(chars[mOffset])) {
                    ++mOffset;
                }
                String word = mSelection.substring(startOffset, mOffset);
                if (mOffset - startOffset <= 4) {
                    if (word.equals("IS")) {
                        mCurrentToken = TOKEN_IS;
                        return;
                    }
                    if (word.equals("OR") || word.equals("AND")) {
                        mCurrentToken = TOKEN_AND_OR;
                        return;
                    }
                    if (word.equals("NULL")) {
                        mCurrentToken = TOKEN_NULL;
                        return;
                    }
                }
                if (mAllowedColumns.contains(word)) {
                    mCurrentToken = TOKEN_COLUMN;
                    return;
                }
                throw new IllegalArgumentException("unrecognized column or keyword");
            }
            if (chars[mOffset] == '\'') {
                ++mOffset;
                while (mOffset < chars.length) {
                    if (chars[mOffset] == '\'') {
                        if (mOffset + 1 < chars.length && chars[mOffset + 1] == '\'') {
                            ++mOffset;
                        } else {
                            break;
                        }
                    }
                    ++mOffset;
                }
                if (mOffset == chars.length) {
                    throw new IllegalArgumentException("unterminated string");
                }
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }
            throw new IllegalArgumentException("illegal character");
        }
        private static final boolean isIdentifierStart(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z');
        }
        private static final boolean isIdentifierChar(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9');
        }
    }
}
