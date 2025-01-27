public class RemoteReceiver extends BroadcastReceiver
{
    public RemoteReceiver()
    {
    }
    public void onReceive(Context context, Intent intent)
    {
        if (LaunchpadActivity.BROADCAST_REPEAT.equals(intent.getAction())) {
            Intent newIntent = new Intent(intent);
            newIntent.setAction(LaunchpadActivity.BROADCAST_REMOTE);
            context.sendOrderedBroadcast(newIntent, null);
        }
        try {
            IBinder caller = intent.getIBinderExtra("caller");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(LaunchpadActivity.LAUNCH);
            data.writeString(LaunchpadActivity.RECEIVER_REMOTE);
            caller.transact(LaunchpadActivity.GOT_RECEIVE_TRANSACTION, data, null, 0);
            data.recycle();
        } catch (RemoteException ex) {
        }
    }
}
