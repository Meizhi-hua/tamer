public class SoftSincResampler extends SoftAbstractResampler {
    float[][][] sinc_table;
    int sinc_scale_size = 100;
    int sinc_table_fsize = 800;
    int sinc_table_size = 30;
    int sinc_table_center = sinc_table_size / 2;
    public SoftSincResampler() {
        super();
        sinc_table = new float[sinc_scale_size][sinc_table_fsize][];
        for (int s = 0; s < sinc_scale_size; s++) {
            float scale = (float) (1.0 / (1.0 + Math.pow(s, 1.1) / 10.0));
            for (int i = 0; i < sinc_table_fsize; i++) {
                sinc_table[s][i] = sincTable(sinc_table_size,
                        -i / ((float)sinc_table_fsize), scale);
            }
        }
    }
    public static double sinc(double x) {
        return (x == 0.0) ? 1.0 : Math.sin(Math.PI * x) / (Math.PI * x);
    }
    public static float[] wHanning(int size, float offset) {
        float[] window_table = new float[size];
        for (int k = 0; k < size; k++) {
            window_table[k] = (float)(-0.5
                    * Math.cos(2.0 * Math.PI * (double)(k + offset)
                        / (double) size) + 0.5);
        }
        return window_table;
    }
    public static float[] sincTable(int size, float offset, float scale) {
        int center = size / 2;
        float[] w = wHanning(size, offset);
        for (int k = 0; k < size; k++)
            w[k] *= sinc((-center + k + offset) * scale) * scale;
        return w;
    }
    public int getPadding() 
    {
        return sinc_table_size / 2 + 2;
    }
    public void interpolate(float[] in, float[] in_offset, float in_end,
            float[] startpitch, float pitchstep, float[] out, int[] out_offset,
            int out_end) {
        float pitch = startpitch[0];
        float ix = in_offset[0];
        int ox = out_offset[0];
        float ix_end = in_end;
        int ox_end = out_end;
        int max_p = sinc_scale_size - 1;
        if (pitchstep == 0) {
            int p = (int) ((pitch - 1) * 10.0f);
            if (p < 0)
                p = 0;
            else if (p > max_p)
                p = max_p;
            float[][] sinc_table_f = this.sinc_table[p];
            while (ix < ix_end && ox < ox_end) {
                int iix = (int) ix;
                float[] sinc_table =
                        sinc_table_f[(int)((ix - iix) * sinc_table_fsize)];
                int xx = iix - sinc_table_center;
                float y = 0;
                for (int i = 0; i < sinc_table_size; i++, xx++)
                    y += in[xx] * sinc_table[i];
                out[ox++] = y;
                ix += pitch;
            }
        } else {
            while (ix < ix_end && ox < ox_end) {
                int iix = (int) ix;
                int p = (int) ((pitch - 1) * 10.0f);
                if (p < 0)
                    p = 0;
                else if (p > max_p)
                    p = max_p;
                float[][] sinc_table_f = this.sinc_table[p];
                float[] sinc_table =
                        sinc_table_f[(int)((ix - iix) * sinc_table_fsize)];
                int xx = iix - sinc_table_center;
                float y = 0;
                for (int i = 0; i < sinc_table_size; i++, xx++)
                    y += in[xx] * sinc_table[i];
                out[ox++] = y;
                ix += pitch;
                pitch += pitchstep;
            }
        }
        in_offset[0] = ix;
        out_offset[0] = ox;
        startpitch[0] = pitch;
    }
}
