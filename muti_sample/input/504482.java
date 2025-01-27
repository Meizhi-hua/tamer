public class DTMNodeList extends DTMNodeListBase {
    private DTMIterator m_iter;
    private DTMNodeList() {
    }
    public DTMNodeList(DTMIterator dtmIterator) {
        if (dtmIterator != null) {
            int pos = dtmIterator.getCurrentPos();
            try {
                m_iter=(DTMIterator)dtmIterator.cloneWithReset();
            } catch(CloneNotSupportedException cnse) {
                m_iter = dtmIterator;
            }
            m_iter.setShouldCacheNodes(true);
            m_iter.runTo(-1);
            m_iter.setCurrentPos(pos);
        }
    }
    public DTMIterator getDTMIterator() {
        return m_iter;
    }
    public Node item(int index)
    {
        if (m_iter != null) {
            int handle=m_iter.item(index);
            if (handle == DTM.NULL) {
                return null;
            }
            return m_iter.getDTM(handle).getNode(handle);
        } else {
            return null;
        }
    }
    public int getLength() {
        return (m_iter != null) ? m_iter.getLength() : 0;
    }
}
