class RemoteDeniedReceiver extends BroadcastReceiver {
    public RemoteDeniedReceiver() {
    }
    public void onReceive(Context context, Intent intent) {
        try {
            IBinder caller = intent.getIBinderExtra("caller");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(LaunchpadActivity.LAUNCH);
            data.writeString(BroadcastTest.RECEIVER_REMOTE);
            caller.transact(BroadcastTest.GOT_RECEIVE_TRANSACTION, data, null, 0);
            data.recycle();
        } catch (RemoteException ex) {
        }
    }
}
