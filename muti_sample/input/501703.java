    public class TonesAutoTest {
        private static String TAG = "TonesAutoTest";
    public static boolean tonesDtmfTest() throws Exception {
        Log.v(TAG, "DTMF tones test");
        ToneGenerator toneGen;
        int type;
        boolean result = true;
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        for (type = ToneGenerator.TONE_DTMF_0; type <= ToneGenerator.TONE_DTMF_D; type++) {
            if (toneGen.startTone(type)) {
                Thread.sleep(200);
                toneGen.stopTone();
                Thread.sleep(100);
            } else {
                result = false;
                break;
            }
        }
        toneGen.release();
        return result;
    }
    public static boolean tonesSupervisoryTest() throws Exception {
      Log.v(TAG, "Supervisory tones test");
      ToneGenerator toneGen;
      int type;
      boolean result = true;
      toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
      for (type = ToneGenerator.TONE_SUP_DIAL;
      type <= ToneGenerator.TONE_SUP_RINGTONE; type++) {
          if (toneGen.startTone(type)) {
              Thread.sleep(2000);
              toneGen.stopTone();
              Thread.sleep(200);
          } else {
              result = false;
              break;
          }
      }
      for (type = ToneGenerator.TONE_SUP_INTERCEPT;
      type <= ToneGenerator.TONE_SUP_PIP; type++) {
          if (toneGen.startTone(type)) {
              Thread.sleep(5000);
              toneGen.stopTone();
              Thread.sleep(200);
          } else {
              result = false;
              break;
          }
      }
      toneGen.release();
      return result;
    }
    public static boolean tonesProprietaryTest() throws Exception {
        Log.v(TAG, "Proprietary tones test");
        ToneGenerator toneGen;
        int type;
        boolean result = true;
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        for (type = ToneGenerator.TONE_PROP_BEEP; type <= ToneGenerator.TONE_PROP_BEEP2; type++) {
            if (toneGen.startTone(type)) {
                Thread.sleep(1000);
                toneGen.stopTone();
                Thread.sleep(100);
            } else {
                result = false;
                break;
            }
        }
        toneGen.release();
        return result;
    }
    public static boolean tonesSimultaneousTest() throws Exception {
        Log.v(TAG, "Simultaneous tones test");
        ToneGenerator toneGen1;
        ToneGenerator toneGen2;
        int type;
        boolean result = true;
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
        if (toneGen1.startTone(ToneGenerator.TONE_DTMF_1)) {
            Thread.sleep(100);
            if (toneGen2.startTone(ToneGenerator.TONE_DTMF_2)) {
                Thread.sleep(500);
                toneGen1.stopTone();
                Thread.sleep(100);
                toneGen2.stopTone();
            } else {
                toneGen1.stopTone();
                result = false;
            }
        } else {
            result = false;
        }
        toneGen1.release();
        toneGen2.release();
        return result;
    }
    public static boolean tonesStressTest() throws Exception {
        Log.v(TAG, "Stress tones test");
        ToneGenerator toneGen;
        int type;
        boolean result = true;
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        for (type = ToneGenerator.TONE_DTMF_1; type <= ToneGenerator.TONE_DTMF_9; type++) {
            if (toneGen.startTone(type)) {
                Thread.sleep(200);
            } else {
                result = false;
                break;
            }
        }
        toneGen.release();
        return result;
    }
    public static boolean tonesAllTest() throws Exception {
        Log.v(TAG, "All tones tests");
        if (!tonesDtmfTest()) {
            return false;
        }
        if (!tonesSupervisoryTest()) {
            return false;
        }
        if (!tonesProprietaryTest()) {
            return false;
        }
        if (!tonesSimultaneousTest()) {
            return false;
        }
        if (!tonesStressTest()) {
            return false;
        }
        return true;
    }
}
