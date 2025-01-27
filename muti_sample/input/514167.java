public class ProtectedBroadcastsTest extends AndroidTestCase {
    private static final String BROADCASTS[] = new String[] {
        Intent.ACTION_SCREEN_OFF,
        Intent.ACTION_SCREEN_ON,
        Intent.ACTION_USER_PRESENT,
        Intent.ACTION_TIME_TICK,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_PACKAGE_INSTALL,
        Intent.ACTION_PACKAGE_ADDED,
        Intent.ACTION_PACKAGE_REPLACED,
        Intent.ACTION_PACKAGE_REMOVED,
        Intent.ACTION_PACKAGE_CHANGED,
        Intent.ACTION_PACKAGE_RESTARTED,
        Intent.ACTION_PACKAGE_DATA_CLEARED,
        Intent.ACTION_UID_REMOVED,
        Intent.ACTION_CONFIGURATION_CHANGED,
        Intent.ACTION_BATTERY_CHANGED,
        Intent.ACTION_BATTERY_LOW,
        Intent.ACTION_BATTERY_OKAY,
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Intent.ACTION_SHUTDOWN,
        Intent.ACTION_DEVICE_STORAGE_LOW,
        Intent.ACTION_DEVICE_STORAGE_OK,
        Intent.ACTION_NEW_OUTGOING_CALL,
        Intent.ACTION_REBOOT,
        "android.intent.action.SERVICE_STATE",
        "android.intent.action.RADIO_TECHNOLOGY",
        "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED",
        "android.intent.action.SIG_STR",
        "android.intent.action.ANY_DATA_STATE",
        "android.intent.action.DATA_CONNECTION_FAILED",
        "android.intent.action.SIM_STATE_CHANGED",
        "android.intent.action.NETWORK_SET_TIME",
        "android.intent.action.NETWORK_SET_TIMEZONE",
        "android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS",
        "android.intent.action.ACTION_MDN_STATE_CHANGED",
        "android.provider.Telephony.SPN_STRINGS_UPDATED"
    };
    public void testProcessOutgoingCall() {
        for (String action : BROADCASTS) {
            try {
                Intent intent = new Intent(action);
                getContext().sendBroadcast(intent);
                fail("expected security exception broadcasting action: " + action);
            } catch (SecurityException expected) {
                assertNotNull("security exception's error message.", expected.getMessage());
            }
        }
    }
}
