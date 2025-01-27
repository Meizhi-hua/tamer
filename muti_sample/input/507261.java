public class PowerCommand extends Svc.Command {
    public PowerCommand() {
        super("power");
    }
    public String shortHelp() {
        return "Control the power manager";
    }
    public String longHelp() {
        return shortHelp() + "\n"
                + "\n"
                + "usage: svc power stayon [true|false|usb|ac]\n"
                + "         Set the 'keep awake while plugged in' setting.\n";
    }
    public void run(String[] args) {
        fail: {
            if (args.length >= 2) {
                if ("stayon".equals(args[1]) && args.length == 3) {
                    int val;
                    if ("true".equals(args[2])) {
                        val = BatteryManager.BATTERY_PLUGGED_AC |
                                BatteryManager.BATTERY_PLUGGED_USB;
                    }
                    else if ("false".equals(args[2])) {
                        val = 0;
                    } else if ("usb".equals(args[2])) {
                        val = BatteryManager.BATTERY_PLUGGED_USB;
                    } else if ("ac".equals(args[2])) {
                        val = BatteryManager.BATTERY_PLUGGED_AC;
                    }
                    else {
                        break fail;
                    }
                    IPowerManager pm
                            = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                    try {
                        IBinder lock = new Binder();
                        pm.acquireWakeLock(PowerManager.FULL_WAKE_LOCK, lock, "svc power");
                        pm.setStayOnSetting(val);
                        pm.releaseWakeLock(lock, 0);
                    }
                    catch (RemoteException e) {
                        System.err.println("Faild to set setting: " + e);
                    }
                    return;
                }
            }
        }
        System.err.println(longHelp());
    }
}
