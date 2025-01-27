public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";
    private final int[] mCoordinates = new int[2];
    public void onReceive(Context context, Intent data) {
        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }
        int screen = Launcher.getScreen();
        if (!installShortcut(context, data, screen)) {
            for (int i = 0; i < Launcher.SCREEN_COUNT; i++) {
                if (i != screen && installShortcut(context, data, i)) break;
            }
        }
    }
    private boolean installShortcut(Context context, Intent data, int screen) {
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        if (findEmptyCell(context, mCoordinates, screen)) {
            CellLayout.CellInfo cell = new CellLayout.CellInfo();
            cell.cellX = mCoordinates[0];
            cell.cellY = mCoordinates[1];
            cell.screen = screen;
            Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            if (intent.getAction() == null) {
                intent.setAction(Intent.ACTION_VIEW);
            }
            boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
            if (duplicate || !LauncherModel.shortcutExists(context, name, intent)) {
                ((LauncherApplication)context.getApplicationContext()).getModel()
                        .addShortcut(context, data, cell, true);
                Toast.makeText(context, context.getString(R.string.shortcut_installed, name),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.shortcut_duplicate, name),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            Toast.makeText(context, context.getString(R.string.out_of_space),
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    private static boolean findEmptyCell(Context context, int[] xy, int screen) {
        final int xCount = Launcher.NUMBER_CELLS_X;
        final int yCount = Launcher.NUMBER_CELLS_Y;
        boolean[][] occupied = new boolean[xCount][yCount];
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
            new String[] { LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
                    LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY },
            LauncherSettings.Favorites.SCREEN + "=?",
            new String[] { String.valueOf(screen) }, null);
        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
        try {
            while (c.moveToNext()) {
                int cellX = c.getInt(cellXIndex);
                int cellY = c.getInt(cellYIndex);
                int spanX = c.getInt(spanXIndex);
                int spanY = c.getInt(spanYIndex);
                for (int x = cellX; x < cellX + spanX && x < xCount; x++) {
                    for (int y = cellY; y < cellY + spanY && y < yCount; y++) {
                        occupied[x][y] = true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            c.close();
        }
        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
}
