public class UserHappinessSignals {
    public static void userAcceptedImeText(Context context) {
        Intent i = new Intent(LoggingEvents.ACTION_LOG_EVENT);
        i.putExtra(LoggingEvents.EXTRA_APP_NAME, LoggingEvents.VoiceIme.APP_NAME);
        i.putExtra(LoggingEvents.EXTRA_EVENT, LoggingEvents.VoiceIme.IME_TEXT_ACCEPTED);
        i.putExtra(LoggingEvents.EXTRA_CALLING_APP_NAME, context.getPackageName());
        i.putExtra(LoggingEvents.EXTRA_TIMESTAMP, System.currentTimeMillis());
        context.sendBroadcast(i);
    }
}
