public class ComposingText {
    public static final int LAYER0  = 0;
    public static final int LAYER1  = 1;
    public static final int LAYER2  = 2;
    public static final int MAX_LAYER = 3;
    protected ArrayList<StrSegment>[] mStringLayer;
    protected int[] mCursor;
    public ComposingText() {
        mStringLayer = new ArrayList[MAX_LAYER];
        mCursor = new int[MAX_LAYER];
        for (int i = 0; i < MAX_LAYER; i++) {
            mStringLayer[i] = new ArrayList<StrSegment>();
            mCursor[i] = 0;
        }
    }
    public void debugout() {
        for (int i = 0; i < MAX_LAYER; i++) {
            Log.d("OpenWnn", "ComposingText["+i+"]");
            Log.d("OpenWnn", "  cur = " + mCursor[i]);
            String tmp = "";
            for (Iterator<StrSegment> it = mStringLayer[i].iterator(); it.hasNext();) {
                StrSegment ss = it.next();
                tmp += "(" + ss.string + "," + ss.from + "," + ss.to + ")";
            }
            Log.d("OpenWnn", "  str = "+tmp);
        }
    }
    public StrSegment getStrSegment(int layer, int pos) {
        try {
            ArrayList<StrSegment> strLayer = mStringLayer[layer];
            if (pos < 0) {
                pos = strLayer.size() - 1;
            }
            if (pos >= strLayer.size() || pos < 0) {
                return null;
            }
            return strLayer.get(pos);
        } catch (Exception ex) {
            return null;
        }
    } 
    public String toString(int layer, int from, int to) {
        try {
            StringBuffer buf = new StringBuffer();
            ArrayList<StrSegment> strLayer = mStringLayer[layer];
            for (int i = from; i <= to; i++) {
                StrSegment ss = strLayer.get(i);
                buf.append(ss.string);
            }
            return buf.toString();
        } catch (Exception ex) {
            return null;
        }
    }
    public String toString(int layer) {
        return this.toString(layer, 0, mStringLayer[layer].size() - 1);
    }
    private void modifyUpper(int layer, int mod_from, int mod_len, int org_len) {
        if (layer >= MAX_LAYER - 1) {
            return;
        }
        int uplayer = layer + 1;
        ArrayList<StrSegment> strUplayer = mStringLayer[uplayer];
        if (strUplayer.size() <= 0) {
            strUplayer.add(new StrSegment(toString(layer), 0, mStringLayer[layer].size() - 1));
            modifyUpper(uplayer, 0, 1, 0);
            return;
        }
        int mod_to = mod_from + ((mod_len == 0)? 0 : (mod_len - 1));
        int org_to = mod_from + ((org_len == 0)? 0 : (org_len - 1));
        StrSegment last = strUplayer.get(strUplayer.size() - 1);
        if (last.to < mod_from) {
            last.to = mod_to;
            last.string = toString(layer, last.from, last.to);
            modifyUpper(uplayer, strUplayer.size()-1, 1, 1);
            return;
        }
        int uplayer_mod_from = -1;
        int uplayer_org_to = -1;
        for (int i = 0; i < strUplayer.size(); i++) {
            StrSegment ss = strUplayer.get(i);
            if (ss.from > mod_from) {
                if (ss.to <= org_to) {
                    if (uplayer_mod_from < 0) {
                        uplayer_mod_from = i;
                    }
                    uplayer_org_to = i;
                } else {
                    uplayer_org_to = i;
                    break;
                }
            } else {
                if (org_len == 0 && ss.from == mod_from) {
                    uplayer_mod_from = i - 1;
                    uplayer_org_to   = i - 1;
                    break;
                } else {
                    uplayer_mod_from = i;
                    uplayer_org_to = i;
                    if (ss.to >= org_to) {
                        break;
                    }
                }
            }
        }
        int diff = mod_len - org_len;
        if (uplayer_mod_from >= 0) {
            StrSegment ss = strUplayer.get(uplayer_mod_from);
            int last_to = ss.to;
            int next = uplayer_mod_from + 1;
            for (int i = next; i <= uplayer_org_to; i++) {
                ss = strUplayer.get(next);
                if (last_to > ss.to) {
                    last_to = ss.to;
                }
                strUplayer.remove(next);
            }
            ss.to = (last_to < mod_to)? mod_to : (last_to + diff);
            ss.string = toString(layer, ss.from, ss.to);
            for (int i = next; i < strUplayer.size(); i++) {
                ss = strUplayer.get(i);
                ss.from += diff;
                ss.to   += diff;
            }
            modifyUpper(uplayer, uplayer_mod_from, 1, uplayer_org_to - uplayer_mod_from + 1);
        } else {
            StrSegment ss = new StrSegment(toString(layer, mod_from, mod_to),
                                           mod_from, mod_to); 
            strUplayer.add(0, ss);
            for (int i = 1; i < strUplayer.size(); i++) {
                ss = strUplayer.get(i);
                ss.from += diff;
                ss.to   += diff;
            }
            modifyUpper(uplayer, 0, 1, 0);
        }
        return;
    }
    public void insertStrSegment(int layer, StrSegment str) {
        int cursor = mCursor[layer];
        mStringLayer[layer].add(cursor, str);
        modifyUpper(layer, cursor, 1, 0);
        setCursor(layer, cursor + 1);
    }
    public void insertStrSegment(int layer1, int layer2, StrSegment str) {
        mStringLayer[layer1].add(mCursor[layer1], str);
        mCursor[layer1]++;
        for (int i = layer1 + 1; i <= layer2; i++) {
            int pos = mCursor[i-1] - 1;
            StrSegment tmp = new StrSegment(str.string, pos, pos);
            ArrayList<StrSegment> strLayer = mStringLayer[i];
            strLayer.add(mCursor[i], tmp);
            mCursor[i]++;
            for (int j = mCursor[i]; j < strLayer.size(); j++) {
                StrSegment ss = strLayer.get(j);
                ss.from++;
                ss.to++;
            }
        }
        int cursor = mCursor[layer2];
        modifyUpper(layer2, cursor - 1, 1, 0);
        setCursor(layer2, cursor);
    }
    protected void replaceStrSegment0(int layer, StrSegment[] str, int from, int to) {
        ArrayList<StrSegment> strLayer = mStringLayer[layer];
        if (from < 0 || from > strLayer.size()) {
            from = strLayer.size();
        }
        if (to < 0 || to > strLayer.size()) {
            to = strLayer.size();
        }
        for (int i = from; i <= to; i++) {
            strLayer.remove(from);
        }
        for (int i = str.length - 1; i >= 0; i--) {
            strLayer.add(from, str[i]);
        }
        modifyUpper(layer, from, str.length, to - from + 1);
    }
    public void replaceStrSegment(int layer, StrSegment[] str, int num) {
        int cursor = mCursor[layer];
        replaceStrSegment0(layer, str, cursor - num, cursor - 1);
        setCursor(layer, cursor + str.length - num);
    }
    public void replaceStrSegment(int layer, StrSegment[] str) {
        int cursor = mCursor[layer];
        replaceStrSegment0(layer, str, cursor - 1, cursor - 1);
        setCursor(layer, cursor + str.length - 1);
    }
    public void deleteStrSegment(int layer, int from, int to) {
        int[] fromL = new int[] {-1, -1, -1};
        int[] toL   = new int[] {-1, -1, -1};
        ArrayList<StrSegment> strLayer2 = mStringLayer[2];
        ArrayList<StrSegment> strLayer1 = mStringLayer[1];
        if (layer == 2) {
            fromL[2] = from;
            toL[2]   = to;
            fromL[1] = strLayer2.get(from).from;
            toL[1]   = strLayer2.get(to).to;
            fromL[0] = strLayer1.get(fromL[1]).from;
            toL[0]   = strLayer1.get(toL[1]).to;
        } else if (layer == 1) {
            fromL[1] = from;
            toL[1]   = to;
            fromL[0] = strLayer1.get(from).from;
            toL[0]   = strLayer1.get(to).to;
        } else {
            fromL[0] = from;
            toL[0]   = to;
        }
        int diff = to - from + 1;
        for (int lv = 0; lv < MAX_LAYER; lv++) {
            if (fromL[lv] >= 0) {
                deleteStrSegment0(lv, fromL[lv], toL[lv], diff);
            } else {
                int boundary_from = -1;
                int boundary_to   = -1;
                ArrayList<StrSegment> strLayer = mStringLayer[lv];
                for (int i = 0; i < strLayer.size(); i++) {
                    StrSegment ss = (StrSegment)strLayer.get(i);
                    if ((ss.from >= fromL[lv-1] && ss.from <= toL[lv-1]) ||
                        (ss.to >= fromL[lv-1] && ss.to <= toL[lv-1]) ) {
                        if (fromL[lv] < 0) {
                            fromL[lv] = i;
                            boundary_from = ss.from;
                        }
                        toL[lv] = i;
                        boundary_to = ss.to;
                    } else if (ss.from <= fromL[lv-1] && ss.to >= toL[lv-1]) {
                        boundary_from = ss.from;
                        boundary_to   = ss.to;
                        fromL[lv] = i;
                        toL[lv] = i;
                        break;
                    } else if (ss.from > toL[lv-1]) {
                        break;
                    }
                }
                if (boundary_from != fromL[lv-1] || boundary_to != toL[lv-1]) {
                    deleteStrSegment0(lv, fromL[lv] + 1, toL[lv], diff);
                    boundary_to -= diff;
                    StrSegment[] tmp = new StrSegment[] {
                        (new StrSegment(toString(lv-1), boundary_from, boundary_to))
                    };
                    replaceStrSegment0(lv, tmp, fromL[lv], fromL[lv]);
                    return;
                } else {
                    deleteStrSegment0(lv, fromL[lv], toL[lv], diff);
                }
            }
            diff = toL[lv] - fromL[lv] + 1;
        }
    }
    private void deleteStrSegment0(int layer, int from, int to, int diff) {
        ArrayList<StrSegment> strLayer = mStringLayer[layer];
        if (diff != 0) {
            for (int i = to + 1; i < strLayer.size(); i++) {
                StrSegment ss = strLayer.get(i);
                ss.from -= diff;
                ss.to   -= diff;
            }
        }
        for (int i = from; i <= to; i++) {
            strLayer.remove(from);
        }
    }
    public int delete(int layer, boolean rightside) {
        int cursor = mCursor[layer];
        ArrayList<StrSegment> strLayer = mStringLayer[layer];
        if (!rightside && cursor > 0) {
            deleteStrSegment(layer, cursor-1, cursor-1);
            setCursor(layer, cursor - 1);
        } else if (rightside && cursor < strLayer.size()) {
            deleteStrSegment(layer, cursor, cursor);
            setCursor(layer, cursor);
        }
        return strLayer.size();
    }
    public ArrayList<StrSegment> getStringLayer(int layer) {
        try {
            return mStringLayer[layer];
        } catch (Exception ex) {
            return null;
        }
    }
    private int included(int layer, int pos) {
        if (pos == 0) {
            return 0;
        }
        int uplayer = layer + 1;
        int i;
        ArrayList<StrSegment> strLayer = mStringLayer[uplayer];
        for (i = 0; i < strLayer.size(); i++) {
            StrSegment ss = strLayer.get(i);
            if (ss.from <= pos && pos <= ss.to) {
                break;
            }
        }
        return i;
    }
    public int setCursor(int layer, int pos) {
        if (pos > mStringLayer[layer].size()) {
            pos = mStringLayer[layer].size();
        }
        if (pos < 0) {
            pos = 0;
        }
        if (layer == 0) {
            mCursor[0] = pos;
            mCursor[1] = included(0, pos);
            mCursor[2] = included(1, mCursor[1]);
        } else if (layer == 1) {
            mCursor[2] = included(1, pos);
            mCursor[1] = pos;
            mCursor[0] = (pos > 0)? mStringLayer[1].get(pos - 1).to+1 : 0;
        } else {
            mCursor[2] = pos;
            mCursor[1] = (pos > 0)? mStringLayer[2].get(pos - 1).to+1 : 0;
            mCursor[0] = (mCursor[1] > 0)? mStringLayer[1].get(mCursor[1] - 1).to+1 : 0;
        }
        return pos;
    }
    public int moveCursor(int layer, int diff) {
        int c = mCursor[layer] + diff;
        return setCursor(layer, c);
    }
    public int getCursor(int layer) {
        return mCursor[layer];
    }
    public int size(int layer) {
        return mStringLayer[layer].size();
    }
    public void clear() {
        for (int i = 0; i < MAX_LAYER; i++) {
            mStringLayer[i].clear();
            mCursor[i] = 0;
        }
    }
}
