public final class DataBufferFloat extends DataBuffer {
    float data[][];
    public DataBufferFloat(float dataArrays[][], int size, int offsets[]) {
        super(TYPE_FLOAT, size, dataArrays.length, offsets);
        data = dataArrays.clone();
    }
    public DataBufferFloat(float dataArrays[][], int size) {
        super(TYPE_FLOAT, size, dataArrays.length);
        data = dataArrays.clone();
    }
    public DataBufferFloat(float dataArray[], int size, int offset) {
        super(TYPE_FLOAT, size, 1, offset);
        data = new float[1][];
        data[0] = dataArray;
    }
    public DataBufferFloat(float dataArray[], int size) {
        super(TYPE_FLOAT, size);
        data = new float[1][];
        data[0] = dataArray;
    }
    public DataBufferFloat(int size, int numBanks) {
        super(TYPE_FLOAT, size, numBanks);
        data = new float[numBanks][];
        int i = 0;
        while (i < numBanks) {
            data[i++] = new float[size];
        }
    }
    public DataBufferFloat(int size) {
        super(TYPE_FLOAT, size);
        data = new float[1][];
        data[0] = new float[size];
    }
    @Override
    public void setElem(int bank, int i, int val) {
        data[bank][offsets[bank] + i] = val;
        notifyChanged();
    }
    @Override
    public void setElemFloat(int bank, int i, float val) {
        data[bank][offsets[bank] + i] = val;
        notifyChanged();
    }
    @Override
    public void setElemDouble(int bank, int i, double val) {
        data[bank][offsets[bank] + i] = (float)val;
        notifyChanged();
    }
    @Override
    public void setElem(int i, int val) {
        data[0][offset + i] = val;
        notifyChanged();
    }
    @Override
    public int getElem(int bank, int i) {
        return (int)(data[bank][offsets[bank] + i]);
    }
    @Override
    public float getElemFloat(int bank, int i) {
        return data[bank][offsets[bank] + i];
    }
    @Override
    public double getElemDouble(int bank, int i) {
        return data[bank][offsets[bank] + i];
    }
    @Override
    public void setElemFloat(int i, float val) {
        data[0][offset + i] = val;
        notifyChanged();
    }
    @Override
    public void setElemDouble(int i, double val) {
        data[0][offset + i] = (float)val;
        notifyChanged();
    }
    public float[] getData(int bank) {
        notifyTaken();
        return data[bank];
    }
    @Override
    public int getElem(int i) {
        return (int)(data[0][offset + i]);
    }
    @Override
    public float getElemFloat(int i) {
        return data[0][offset + i];
    }
    @Override
    public double getElemDouble(int i) {
        return data[0][offset + i];
    }
    public float[][] getBankData() {
        notifyTaken();
        return data.clone();
    }
    public float[] getData() {
        notifyTaken();
        return data[0];
    }
}
