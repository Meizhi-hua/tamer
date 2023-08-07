public class MProjectTask extends X_C_ProjectTask {
    public MProjectTask(Properties ctx, int C_ProjectTask_ID, String trxName) {
        super(ctx, C_ProjectTask_ID, trxName);
        if (C_ProjectTask_ID == 0) {
            setSeqNo(0);
            setQty(Env.ZERO);
        }
    }
    public MProjectTask(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
    public MProjectTask(MProjectPhase phase) {
        this(phase.getCtx(), 0, phase.get_TrxName());
        setClientOrg(phase);
        setC_ProjectPhase_ID(phase.getC_ProjectPhase_ID());
    }
    public MProjectTask(MProjectPhase phase, MProjectTypeTask task) {
        this(phase);
        setC_Task_ID(task.getC_Task_ID());
        setSeqNo(task.getSeqNo());
        setName(task.getName());
        setDescription(task.getDescription());
        setHelp(task.getHelp());
        if (task.getM_Product_ID() != 0) {
            setM_Product_ID(task.getM_Product_ID());
        }
        setQty(task.getStandardQty());
    }
    public MProjectProduct[] getProduct() {
        ArrayList list = new ArrayList();
        String sql = "SELECT p.* FROM C_ProjectProduct p INNER JOIN c_projecttask t ON (p.C_ProjectTask_ID=t.C_ProjectTask_ID)WHERE p.C_ProjectTask_ID=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.setInt(1, getC_ProjectTask_ID());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new MProjectProduct(getCtx(), rs, get_TrxName()));
            }
            rs.close();
            pstmt.close();
            pstmt = null;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "getTasks", ex);
        }
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ex1) {
        }
        pstmt = null;
        MProjectProduct[] retValue = new MProjectProduct[list.size()];
        list.toArray(retValue);
        return retValue;
    }
}
