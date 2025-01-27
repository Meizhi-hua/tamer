public class VMVersionInfoPanel extends JPanel {
    private JEditorPane         versionPane;
    public VMVersionInfoPanel() {
        initUI();
    }
    private void initUI() {
        setLayout(new BorderLayout());
        versionPane = new JEditorPane();
        versionPane.setContentType("text/html");
        versionPane.setEditable(false);
        versionPane.setText(getVersionInfo());
        add(versionPane, BorderLayout.CENTER);
    }
    private String getVersionInfo() {
       VM vm = VM.getVM();
       StringBuffer buf = new StringBuffer();
       buf.append("<html><head><title>VM Version Info</title></head>");
       buf.append("<body><table border='1'>");
       buf.append("<tr><td><b>VM Type</b></td>");
       buf.append("<td>");
       if (vm.isCore()) {
          buf.append("<b>core</b>");
       } else if(vm.isClientCompiler()) {
          buf.append("<b>client</b>");
       } else if(vm.isServerCompiler()) {
          buf.append("<b>server</b>");
       } else {
          buf.append("<b>unknown</b>");
       }
       buf.append("</td></tr>");
       String release = vm.getVMRelease();
       if (release != null) {
          buf.append("<tr><td><b>VM Release</td><td><b>");
          buf.append(release);
          buf.append("</b></td></tr>");
       }
       String internalInfo = vm.getVMInternalInfo();
       if (internalInfo != null) {
          buf.append("<tr><td><b>VM Internal Info</td><td><b>");
          buf.append(internalInfo);
          buf.append("</b></td></tr>");
       }
       buf.append("</table></body></html>");
       return buf.toString();
    }
}
