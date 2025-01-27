public abstract class Tab extends JPanel {
    private String name;
    private Worker worker;
    protected VMPanel vmPanel;
    private SwingWorker<?, ?> prevSW;
    public Tab(VMPanel vmPanel, String name) {
        this.vmPanel = vmPanel;
        this.name = name;
    }
    public SwingWorker<?, ?> newSwingWorker() {
        return null;
    }
    public void update() {
        final ProxyClient proxyClient = vmPanel.getProxyClient();
        if (!proxyClient.hasPlatformMXBeans()) {
            throw new UnsupportedOperationException(
                "Platform MXBeans not registered in MBeanServer");
        }
        SwingWorker<?,?> sw = newSwingWorker();
        if (prevSW == null || prevSW.isDone()) {
            if (sw == null || sw.getState() == SwingWorker.StateValue.PENDING) {
                prevSW = sw;
                if (sw != null) {
                    sw.execute();
                }
            }
        }
    }
    public synchronized void dispose() {
        if(worker != null)
            worker.stopWorker();
    }
    protected VMPanel getVMPanel() {
        return vmPanel;
    }
    OverviewPanel[] getOverviewPanels() {
        return null;
    }
    public synchronized void workerAdd(Runnable job) {
        if (worker == null) {
            worker = new Worker(name+"-"+vmPanel.getConnectionName());
            worker.start();
        }
        worker.add(job);
    }
    public Dimension getPreferredSize() {
        return new Dimension(700, 500);
    }
}
