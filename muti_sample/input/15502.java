public class PrinterStateReason extends EnumSyntax implements Attribute {
    private static final long serialVersionUID = -1623720656201472593L;
    public static final PrinterStateReason OTHER = new PrinterStateReason(0);
    public static final PrinterStateReason
        MEDIA_NEEDED = new PrinterStateReason(1);
    public static final PrinterStateReason
        MEDIA_JAM = new PrinterStateReason(2);
    public static final PrinterStateReason
        MOVING_TO_PAUSED = new PrinterStateReason(3);
    public static final PrinterStateReason
        PAUSED = new PrinterStateReason(4);
    public static final PrinterStateReason
        SHUTDOWN = new PrinterStateReason(5);
    public static final PrinterStateReason
        CONNECTING_TO_DEVICE = new PrinterStateReason(6);
    public static final PrinterStateReason
        TIMED_OUT = new PrinterStateReason(7);
    public static final PrinterStateReason
        STOPPING = new PrinterStateReason(8);
    public static final PrinterStateReason
        STOPPED_PARTLY = new PrinterStateReason(9);
    public static final PrinterStateReason
        TONER_LOW = new PrinterStateReason(10);
    public static final PrinterStateReason
        TONER_EMPTY = new PrinterStateReason(11);
    public static final PrinterStateReason
        SPOOL_AREA_FULL = new PrinterStateReason(12);
    public static final PrinterStateReason
        COVER_OPEN = new PrinterStateReason(13);
    public static final PrinterStateReason
        INTERLOCK_OPEN = new PrinterStateReason(14);
    public static final PrinterStateReason
        DOOR_OPEN = new PrinterStateReason(15);
    public static final PrinterStateReason
        INPUT_TRAY_MISSING = new PrinterStateReason(16);
    public static final PrinterStateReason
        MEDIA_LOW = new PrinterStateReason(17);
    public static final PrinterStateReason
        MEDIA_EMPTY = new PrinterStateReason(18);
    public static final PrinterStateReason
        OUTPUT_TRAY_MISSING = new PrinterStateReason(19);
    public static final PrinterStateReason
        OUTPUT_AREA_ALMOST_FULL = new PrinterStateReason(20);
    public static final PrinterStateReason
        OUTPUT_AREA_FULL = new PrinterStateReason(21);
    public static final PrinterStateReason
        MARKER_SUPPLY_LOW = new PrinterStateReason(22);
    public static final PrinterStateReason
        MARKER_SUPPLY_EMPTY = new PrinterStateReason(23);
    public static final PrinterStateReason
        MARKER_WASTE_ALMOST_FULL = new PrinterStateReason(24);
    public static final PrinterStateReason
        MARKER_WASTE_FULL = new PrinterStateReason(25);
    public static final PrinterStateReason
        FUSER_OVER_TEMP = new PrinterStateReason(26);
    public static final PrinterStateReason
        FUSER_UNDER_TEMP = new PrinterStateReason(27);
    public static final PrinterStateReason
        OPC_NEAR_EOL = new PrinterStateReason(28);
    public static final PrinterStateReason
        OPC_LIFE_OVER = new PrinterStateReason(29);
    public static final PrinterStateReason
        DEVELOPER_LOW = new PrinterStateReason(30);
    public static final PrinterStateReason
        DEVELOPER_EMPTY = new PrinterStateReason(31);
    public static final PrinterStateReason
        INTERPRETER_RESOURCE_UNAVAILABLE = new PrinterStateReason(32);
    protected PrinterStateReason(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "other",
        "media-needed",
        "media-jam",
        "moving-to-paused",
        "paused",
        "shutdown",
        "connecting-to-device",
        "timed-out",
        "stopping",
        "stopped-partly",
        "toner-low",
        "toner-empty",
        "spool-area-full",
        "cover-open",
        "interlock-open",
        "door-open",
        "input-tray-missing",
        "media-low",
        "media-empty",
        "output-tray-missing",
        "output-area-almost-full",
        "output-area-full",
        "marker-supply-low",
        "marker-supply-empty",
        "marker-waste-almost-full",
        "marker-waste-full",
        "fuser-over-temp",
        "fuser-under-temp",
        "opc-near-eol",
        "opc-life-over",
        "developer-low",
        "developer-empty",
        "interpreter-resource-unavailable"
    };
    private static final PrinterStateReason[] myEnumValueTable = {
        OTHER,
        MEDIA_NEEDED,
        MEDIA_JAM,
        MOVING_TO_PAUSED,
        PAUSED,
        SHUTDOWN,
        CONNECTING_TO_DEVICE,
        TIMED_OUT,
        STOPPING,
        STOPPED_PARTLY,
        TONER_LOW,
        TONER_EMPTY,
        SPOOL_AREA_FULL,
        COVER_OPEN,
        INTERLOCK_OPEN,
        DOOR_OPEN,
        INPUT_TRAY_MISSING,
        MEDIA_LOW,
        MEDIA_EMPTY,
        OUTPUT_TRAY_MISSING,
        OUTPUT_AREA_ALMOST_FULL,
        OUTPUT_AREA_FULL,
        MARKER_SUPPLY_LOW,
        MARKER_SUPPLY_EMPTY,
        MARKER_WASTE_ALMOST_FULL,
        MARKER_WASTE_FULL,
        FUSER_OVER_TEMP,
        FUSER_UNDER_TEMP,
        OPC_NEAR_EOL,
        OPC_LIFE_OVER,
        DEVELOPER_LOW,
        DEVELOPER_EMPTY,
        INTERPRETER_RESOURCE_UNAVAILABLE
    };
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterStateReason.class;
    }
    public final String getName() {
        return "printer-state-reason";
    }
}
