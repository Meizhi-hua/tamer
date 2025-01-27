public class XAWTFormatter extends java.util.logging.Formatter {
    Date dat = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;
    private Object args[] = new Object[1];
    private String lineSeparator = (String) java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("line.separator"));
    boolean displayFullRecord = false;
    boolean useANSI = false;
    boolean showDate = true;
    boolean showLevel = true;
    boolean swapMethodClass = false;
    public XAWTFormatter() {
        displayFullRecord = "true".equals(LogManager.getLogManager().getProperty("XAWTFormatter.displayFullRecord"));
        useANSI = "true".equals(LogManager.getLogManager().getProperty("XAWTFormatter.useANSI"));
        showDate = !"false".equals(LogManager.getLogManager().getProperty("XAWTFormatter.showDate"));
        showLevel = !"false".equals(LogManager.getLogManager().getProperty("XAWTFormatter.showLevel"));
        swapMethodClass = "true".equals(LogManager.getLogManager().getProperty("XAWTFormatter.swapMethodClass"));
    }
    public synchronized String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        if (useANSI) {
            Level lev = record.getLevel();
            if (Level.FINEST.equals(lev)) {
                sb.append("[36m");
            } else if (Level.FINER.equals(lev)) {
                sb.append("[32m");
            } else if (Level.FINE.equals(lev)) {
                sb.append("[34m");
            }
        }
        if (displayFullRecord) {
            if (showDate) {
                dat.setTime(record.getMillis());
                args[0] = dat;
                StringBuffer text = new StringBuffer();
                if (formatter == null) {
                    formatter = new MessageFormat(format);
                }
                formatter.format(args, text, null);
                sb.append(text);
                sb.append(" ");
            } else {
                sb.append("    ");
            }
            if (swapMethodClass) {
                if (record.getSourceMethodName() != null) {
                    sb.append(" [35m");
                    sb.append(record.getSourceMethodName());
                    sb.append("[30m ");
                }
                if (record.getSourceClassName() != null) {
                    sb.append(record.getSourceClassName());
                } else {
                    sb.append(record.getLoggerName());
                }
            } else {
                if (record.getSourceClassName() != null) {
                    sb.append(record.getSourceClassName());
                } else {
                    sb.append(record.getLoggerName());
                }
                if (record.getSourceMethodName() != null) {
                    sb.append(" [35m");
                    sb.append(record.getSourceMethodName());
                    sb.append("[30m");
                }
            }
            sb.append(lineSeparator);
        }
        if (useANSI) {
            Level lev = record.getLevel();
            if (Level.FINEST.equals(lev)) {
                sb.append("[36m");
            } else if (Level.FINER.equals(lev)) {
                sb.append("[32m");
            } else if (Level.FINE.equals(lev)) {
                sb.append("[34m");
            }
        }
        if (showLevel) {
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
        }
        String message = formatMessage(record);
        sb.append(message);
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }
        if (useANSI) {
            sb.append("[30m");
        }
        return sb.toString();
    }
}
