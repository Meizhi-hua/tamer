public class test {
    public void requestPatchDump(int bankNum, int patchNum) {
        send(SYS_REQ.toSysexMessage(getChannel(), new SysexHandler.NameValue("channel", getChannel())));
    }
}
