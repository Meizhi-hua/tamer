public class DTMChildIterNodeList extends DTMNodeListBase {
    private int m_firstChild;
    private DTM m_parentDTM;
    private DTMChildIterNodeList() {
    }
    public DTMChildIterNodeList(DTM parentDTM,int parentHandle) {
        m_parentDTM=parentDTM;
        m_firstChild=parentDTM.getFirstChild(parentHandle);
    }
    public Node item(int index) {
        int handle=m_firstChild;
        while(--index>=0 && handle!=DTM.NULL) {
            handle=m_parentDTM.getNextSibling(handle);
        }
        if (handle == DTM.NULL) {
            return null;
        }
        return m_parentDTM.getNode(handle);
    }
    public int getLength() {
        int count=0;
        for (int handle=m_firstChild;
             handle!=DTM.NULL;
             handle=m_parentDTM.getNextSibling(handle)) {
            ++count;
        }
        return count;
    }
}
