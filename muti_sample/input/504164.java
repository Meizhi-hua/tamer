public class FullBackupAgent extends BackupAgent {
    private static final String TAG = "FullBackupAgent";
    private static final boolean DEBUG = true;
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) {
        LinkedList<File> dirsToScan = new LinkedList<File>();
        ArrayList<String> allFiles = new ArrayList<String>();
        dirsToScan.add(getFilesDir());
        if (DEBUG) Log.v(TAG, "Backing up dir tree @ " + getFilesDir().getAbsolutePath() + " :");
        while (dirsToScan.size() > 0) {
            File dir = dirsToScan.removeFirst();
            File[] contents = dir.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (f.isDirectory()) {
                        dirsToScan.add(f);
                    } else if (f.isFile()) {
                        if (DEBUG) Log.v(TAG, "    " + f.getAbsolutePath());
                        allFiles.add(f.getAbsolutePath());
                    }
                }
            }
        }
        FileBackupHelper helper = new FileBackupHelper(this,
                allFiles.toArray(new String[allFiles.size()]));
        helper.performBackup(oldState, data, newState);
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) {
    }
}
