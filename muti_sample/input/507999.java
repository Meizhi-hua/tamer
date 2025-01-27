public class DOMTestIncompatibleException
    extends Exception {
  private final String msg;
  private DOMTestIncompatibleException(String msg) {
    this.msg = msg;
  }
  public DOMTestIncompatibleException(Throwable ex,
                                      DocumentBuilderSetting setting) {
    if (ex != null) {
      msg = ex.toString();
    }
    else {
      if (setting != null) {
        msg = setting.toString();
      }
      else {
        msg = super.toString();
      }
    }
  }
  public static DOMTestIncompatibleException incompatibleFeature(String feature,
      String version) {
    StringBuffer buf = new StringBuffer(
        "Implementation does not support feature \"");
    buf.append(feature);
    buf.append("\" version=\"");
    buf.append(version);
    buf.append("\".");
    return new DOMTestIncompatibleException(buf.toString());
  }
  public static DOMTestIncompatibleException incompatibleLoad(String href,
      String contentType) {
    StringBuffer buf = new StringBuffer(
        "Document is incompatible with content type, \"");
    buf.append(href);
    buf.append("\" not available for =\"");
    buf.append(contentType);
    buf.append("\".");
    return new DOMTestIncompatibleException(buf.toString());
  }
  public String toString() {
    return msg;
  }
}
