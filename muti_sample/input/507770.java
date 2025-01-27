public class CodecTest {    
    private static String TAG = "MediaPlayerApiTest";
    private static MediaPlayer mMediaPlayer;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 60000;  
    private static boolean mInitialized = false;
    private static boolean mPrepareReset = false;
    private static Looper mLooper = null;
    private static final Object lock = new Object();
    private static final Object prepareDone = new Object();
    private static final Object videoSizeChanged = new Object();
    private static final Object onCompletion = new Object();
    private static boolean onPrepareSuccess = false;
    private static boolean onCompleteSuccess = false;
    public static String printCpuInfo(){      
        String cm = "dumpsys cpuinfo";
        String cpuinfo =null;
        int ch;
        try{
            Process  p = Runtime.getRuntime().exec(cm);
            InputStream in = p.getInputStream();        
            StringBuffer sb = new StringBuffer(512);
            while ( ( ch = in.read() ) != -1 ){  
                sb.append((char) ch); 
            }
            cpuinfo = sb.toString();      
        }catch (IOException e){
            Log.v(TAG, e.toString());
        }
        return cpuinfo;
    }
    public static int getDuration(String filePath) {
        Log.v(TAG, "getDuration - " + filePath);
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            mp.prepare(); 
        }catch (Exception e){
            Log.v(TAG, e.toString());
        }
        int duration = mp.getDuration();
        Log.v(TAG, "Duration " + duration);
        mp.release();
        Log.v(TAG, "release");      
        return duration;
    }
    public static boolean getCurrentPosition(String filePath){
        Log.v(TAG, "GetCurrentPosition - " + filePath);
        int currentPosition = 0;
        long t1=0;
        long t2 =0;
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            Log.v(TAG, "start playback");
            mp.prepare();
            mp.start();
            t1=SystemClock.uptimeMillis();
            Thread.sleep(10000);
            mp.pause();
            Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
            t2=SystemClock.uptimeMillis();
        }catch (Exception e){
            Log.v(TAG, e.toString());
        }
        currentPosition = mp.getCurrentPosition();
        mp.stop();
        mp.release();   
        Log.v(TAG, "mp currentPositon = " + currentPosition + " play duration = " + (t2-t1));
        if (filePath.equals(MediaNames.SHORTMP3)){
            if (currentPosition < 1000 )
                return true;
        }
        if ((currentPosition < ((t2-t1) *1.2)) && (currentPosition > 0)) 
            return true;
        else
            return false;
    }  
    public static boolean seekTo(String filePath){
        Log.v(TAG, "seekTo " + filePath);
        int currentPosition = 0;
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            mp.seekTo(MediaNames.SEEK_TIME);
            Thread.sleep(MediaNames.WAIT_TIME);
            currentPosition = mp.getCurrentPosition();
        }catch (Exception e){
            Log.v(TAG, e.getMessage());
        }      
        mp.stop();
        mp.release();
        Log.v(TAG, "CurrentPosition = " + currentPosition);
        if ((currentPosition > MediaNames.SEEK_TIME *0.8)) 
            return true;
        else
            return false;
    }
    public static boolean setLooping(String filePath){
        int currentPosition = 0;
        int duration = 0;
        long t1 =0;
        long t2 =0;
        Log.v (TAG, "SetLooping - " + filePath);
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            mp.prepare();
            duration = mp.getDuration(); 
            Log.v(TAG, "setLooping duration " + duration);
            mp.setLooping(true);
            mp.start();
            Thread.sleep(5000);
            mp.seekTo(duration - 5000);
            t1=SystemClock.uptimeMillis();
            Thread.sleep(20000);
            t2=SystemClock.uptimeMillis();
            Log.v(TAG, "pause");
            currentPosition = mp.getCurrentPosition();
            Log.v(TAG, "looping position " + currentPosition + "duration = " + (t2-t1));
        }catch (Exception e){
            Log.v(TAG, "Exception : " + e.toString());
        }      
        mp.stop();
        mp.release();
        if ((currentPosition < ((t2-t1-5000)*1.2)) && currentPosition > 0)
            return true;
        else
            return false;
    }  
    public static boolean pause(String filePath) throws Exception {
        Log.v(TAG, "pause - " + filePath);
        boolean misPlaying = true;
        boolean pauseResult = false;
        long t1=0;
        long t2=0;
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(filePath);
        mp.prepare();    
        int duration = mp.getDuration();
        mp.start();
        t1=SystemClock.uptimeMillis();
        Thread.sleep(5000);
        mp.pause();
        Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
        t2=SystemClock.uptimeMillis();
        misPlaying = mp.isPlaying();
        int curPosition = mp.getCurrentPosition();
        Log.v(TAG, filePath + " pause currentPositon " + curPosition);
        Log.v(TAG, "isPlaying "+ misPlaying + " wait time " + (t2 - t1) );
        String cpuinfo = printCpuInfo();
        Log.v(TAG, cpuinfo);
        if ((curPosition>0) && (curPosition < ((t2-t1) * 1.3)) && (misPlaying == false))
            pauseResult = true;
        mp.stop();
        mp.release();
        return pauseResult;
    }
    public static void prepareStopRelease(String filePath) throws Exception {
        Log.v(TAG, "prepareStopRelease" + filePath);
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(filePath);
        mp.prepare();
        mp.stop();
        mp.release();
    }
    public static void preparePauseRelease(String filePath) throws Exception {
        Log.v(TAG, "preparePauseRelease" + filePath);
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(filePath);
        mp.prepare();
        mp.pause();
        mp.release();
    }
    static MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener =
        new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                synchronized (videoSizeChanged) {
                    Log.v(TAG, "sizechanged notification received ...");
                    videoSizeChanged.notify();
                }
            }
    };
    public static int videoHeight(String filePath) throws Exception {
        Log.v(TAG, "videoHeight - " + filePath);
        int videoHeight = 0;    
        synchronized (lock) {
            initializeMessageLooper();
            try {
                lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
            } catch(Exception e) {
                Log.v(TAG, "looper was interrupted.");
                return 0;
            }
        }
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            synchronized (videoSizeChanged) {
                try {
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    videoSizeChanged.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch (Exception e) {
                    Log.v(TAG, "wait was interrupted");
                }
            }
            videoHeight = mMediaPlayer.getVideoHeight();
            terminateMessageLooper();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return videoHeight;
    }
    public static int videoWidth(String filePath) throws Exception {
        Log.v(TAG, "videoWidth - " + filePath);
        int videoWidth = 0;
        synchronized (lock) {
            initializeMessageLooper();
            try {
                lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
            } catch(Exception e) {
                Log.v(TAG, "looper was interrupted.");
                return 0;
            }
        }
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            synchronized (videoSizeChanged) {
                try {
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    videoSizeChanged.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch (Exception e) {
                    Log.v(TAG, "wait was interrupted");
                }
            }
            videoWidth = mMediaPlayer.getVideoWidth();
            terminateMessageLooper();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }        
        return videoWidth;
    }
    public static boolean videoSeekTo(String filePath) throws Exception {
        Log.v(TAG, "videoSeekTo - " + filePath);
        int currentPosition = 0;
        int duration = 0;
        boolean videoResult = false;
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(filePath);
        mp.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
        mp.prepare();
        mp.start();
        if (filePath.equals(MediaNames.VIDEO_SHORT_3GP)){
            mp.pause();
            Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
            mp.seekTo(0);
            mp.start();
            Thread.sleep(1000);
            currentPosition = mp.getCurrentPosition();
            Log.v(TAG,"short position " + currentPosition);
            if (currentPosition > 100 )
                return true;
            else
                return false;
        }
        Thread.sleep(5000);
        duration = mp.getDuration();
        Log.v(TAG, "video duration " + duration);
        mp.pause();
        Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
        mp.seekTo(duration - 20000 );
        mp.start();
        Thread.sleep(1000);
        mp.pause();
        Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
        mp.seekTo(duration/2);
        mp.start();
        Thread.sleep(10000);
        currentPosition = mp.getCurrentPosition();
        Log.v(TAG, "video currentPosition " + currentPosition);
        mp.release();
        if (currentPosition > (duration /2 )*0.9)
            return true;
        else
            return false;
    }
    public static boolean seekToEnd(String filePath){
        Log.v(TAG, "seekToEnd - " + filePath);
        int duration = 0;
        int currentPosition = 0;
        boolean isPlaying = false;
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            Log.v(TAG, "start playback");
            mp.prepare();
            duration = mp.getDuration();
            mp.seekTo(duration - 3000);
            mp.start();
            Thread.sleep(6000);
        }catch (Exception e){}
        isPlaying = mp.isPlaying();
        currentPosition = mp.getCurrentPosition();
        Log.v(TAG, "seekToEnd currentPosition= " + currentPosition + " isPlaying = " + isPlaying);
        mp.stop();
        mp.release();   
        Log.v(TAG, "duration = " + duration);
        if (currentPosition < 0.9 * duration || isPlaying)
            return false;
        else
            return true;        
    }
    public static boolean shortMediaStop(String filePath){
        Log.v(TAG, "shortMediaStop - " + filePath);
        int duration = 0;
        int currentPosition = 0;
        boolean isPlaying = false;
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            Log.v(TAG, "start playback");
            mp.prepare();
            duration = mp.getDuration();
            mp.start();
            Thread.sleep(10000);
        }catch (Exception e){}
        isPlaying = mp.isPlaying();
        currentPosition = mp.getCurrentPosition();
        Log.v(TAG, "seekToEnd currentPosition= " + currentPosition + " isPlaying = " + isPlaying);
        mp.stop();
        mp.release();   
        Log.v(TAG, "duration = " + duration);
        if (currentPosition > duration || isPlaying)
            return false;
        else
            return true;        
    }
    public static boolean playToEnd(String filePath){
        Log.v(TAG, "shortMediaStop - " + filePath);
        int duration = 200000;
        int updateDuration = 0;
        int currentPosition = 0;
        boolean isPlaying = false;
        MediaPlayer mp = new MediaPlayer();
        try{
            Thread.sleep(5000);
            mp.setDataSource(filePath);
            Log.v(TAG, "start playback");
            mp.prepare();
            mp.start();
            Thread.sleep(50000);
        }catch (Exception e){}
        isPlaying = mp.isPlaying();
        currentPosition = mp.getCurrentPosition();
        Log.v(TAG, "seekToEnd currentPosition= " + currentPosition + " isPlaying = " + isPlaying);
        mp.stop();
        mp.release();   
        if (currentPosition > duration || isPlaying)
            return false;
        else
            return true;        
    }
    public static boolean seektoBeforeStart(String filePath){
        Log.v(TAG, "seektoBeforeStart - " + filePath);
        int duration = 0;
        int currentPosition = 0;
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            mp.prepare();
            duration = mp.getDuration();
            mp.seekTo(duration - 10000);
            mp.start();
            currentPosition=mp.getCurrentPosition();
            mp.stop();
            mp.release();
        }catch (Exception e){}
        if (currentPosition < duration/2)
            return false;
        else
            return true;        
    }
    public static boolean mediaRecorderRecord(String filePath){
        Log.v(TAG, "SoundRecording - " + filePath);
        int duration = 0;
        try{
            MediaRecorder mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(filePath);
            mRecorder.prepare();
            mRecorder.start();
            Thread.sleep(500);
            mRecorder.stop();
            Log.v(TAG, "sound recorded");
            mRecorder.release();
        }catch (Exception e){
            Log.v(TAG, e.toString());
        }  
        MediaPlayer mp = new MediaPlayer();
        try{
            mp.setDataSource(filePath);
            mp.prepare();
            duration = mp.getDuration();
            Log.v(TAG,"Duration " + duration);
            mp.release();
        }catch (Exception e){}
        if (duration > 0)
            return true;
        else
            return false;
    }
    public static boolean getThumbnail(String filePath, String goldenPath){
        Log.v(TAG, "getThumbnail - " + filePath);
        int goldenHeight = 0;
        int goldenWidth = 0;
        int outputWidth = 0;
        int outputHeight = 0;
        try{
            BitmapFactory mBitmapFactory = new BitmapFactory();
            MediaMetadataRetriever mMediaMetadataRetriever = new MediaMetadataRetriever();
            try {
                mMediaMetadataRetriever.setDataSource(filePath);
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
            Bitmap outThumbnail = mMediaMetadataRetriever.captureFrame();
            Bitmap goldenBitmap = mBitmapFactory.decodeFile(goldenPath);
            outputWidth = outThumbnail.getWidth();
            outputHeight = outThumbnail.getHeight();
            goldenHeight = goldenBitmap.getHeight();
            goldenWidth = goldenBitmap.getWidth();
            if ((outputWidth != goldenWidth) || (outputHeight != goldenHeight))
                return false;
            int x = goldenHeight / 2;
            for (int j = 1; j < goldenWidth / 2; j++) {
                if (goldenBitmap.getPixel(x, j) != outThumbnail.getPixel(x, j)) {
                    Log.v(TAG, "pixel = " + goldenBitmap.getPixel(x, j));
                    return false;
                }
           }
        }catch (Exception e){
            Log.v(TAG, e.toString());
            return false;
        }
        return true;
    }
    public static boolean resourcesPlayback(AssetFileDescriptor afd, int expectedDuration){
        int duration = 0;
        try{
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
            duration = mp.getDuration();
            Thread.sleep(5000);
            mp.release();
        }catch (Exception e){
            Log.v(TAG,e.getMessage());
        }
        if (duration > expectedDuration)
            return true;
        else
            return false;
    }
    public static boolean prepareAsyncReset(String filePath){    
        try{
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(filePath);
            mp.prepareAsync();
            mp.reset();
            mp.release();
        }catch (Exception e){
            Log.v(TAG,e.getMessage());
            return false;
        }
        return true;
    }
    public static boolean isLooping(String filePath) {        
        MediaPlayer mp = null;
        try {
            mp = new MediaPlayer();
            if (mp.isLooping()) {
                Log.v(TAG, "MediaPlayer.isLooping() returned true after ctor");
                return false;
            }
            mp.setDataSource(filePath);
            mp.prepare();
            mp.setLooping(true);
            if (!mp.isLooping()) {
                Log.v(TAG, "MediaPlayer.isLooping() returned false after setLooping(true)");
                return false;
            }
            mp.setLooping(false);
            if (mp.isLooping()) {
                Log.v(TAG, "MediaPlayer.isLooping() returned true after setLooping(false)");
                return false;
            }
        }catch (Exception e){
            Log.v(TAG, "Exception : " + e.toString());
            return false;
        } finally {
            if (mp != null)
                mp.release();
        }
        return true;
    }
    public static boolean isLoopingAfterReset(String filePath) {
        MediaPlayer mp = null;
        try {
            mp = new MediaPlayer();
            mp.setDataSource(filePath);
            mp.prepare();
            mp.setLooping(true);
            mp.reset();
            if (mp.isLooping()) {
                Log.v(TAG, "MediaPlayer.isLooping() returned true after reset()");
                return false;
            }
        }catch (Exception e){
            Log.v(TAG, "Exception : " + e.toString());
            return false;
        } finally {
            if (mp != null)
                mp.release();
        }
        return true;
    }
    private static void initializeMessageLooper() {
        Log.v(TAG, "start looper");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Log.v(TAG, "start loopRun");
                mLooper = Looper.myLooper();                
                mMediaPlayer = new MediaPlayer();                                
                synchronized (lock) {
                    mInitialized = true;
                    lock.notify();
                }
                Looper.loop();  
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
    }
    private static void terminateMessageLooper() {
        mLooper.quit();
        mMediaPlayer.release();
    }
    static MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            synchronized (prepareDone) {
                if(mPrepareReset){
                    Log.v(TAG, "call Reset");
                    mMediaPlayer.reset();
                }
                Log.v(TAG, "notify the prepare callback");
                prepareDone.notify();
                onPrepareSuccess = true;
            }
        }
    };
    public static boolean prepareAsyncCallback(String filePath, boolean reset) throws Exception {
        if (reset){
            mPrepareReset = true;
        }
        synchronized (lock) {
            initializeMessageLooper();
            try {
                lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
            } catch(Exception e) {
                Log.v(TAG, "looper was interrupted.");
                return false;
            }
        }
        try{
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync(); 
            synchronized (prepareDone) {
                try {
                    prepareDone.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch (Exception e) {
                    Log.v(TAG, "wait was interrupted.");
                }
            }         
            terminateMessageLooper();
        }catch (Exception e){
            Log.v(TAG,e.getMessage());
        }      
       return onPrepareSuccess;
    }
    static MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            synchronized (onCompletion) {
                Log.v(TAG, "notify the completion callback");
                onCompletion.notify();
                onCompleteSuccess = true;
            }
        }
    };
    public static boolean playMediaSamples(String filePath) throws Exception {
        int duration = 0;
        int curPosition = 0;
        int nextPosition = 0;
        int waittime = 0;
        Random r = new Random();
        initializeMessageLooper();
        synchronized (lock) {
            try {
                lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
            } catch(Exception e) {
                Log.v(TAG, "looper was interrupted.");
                return false;
            }
        }
        try {
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            Log.v(TAG, "playMediaSamples: sample file name " + filePath);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
            mMediaPlayer.prepare();
            duration = mMediaPlayer.getDuration();
            Log.v(TAG, "playMediaSamples: duration = " + duration);
            mMediaPlayer.start();
            Thread.sleep(r.nextInt(duration/3));
            mMediaPlayer.pause();
            Log.v(TAG, "playMediaSamples: current position after pause: "
                        + mMediaPlayer.getCurrentPosition());
            nextPosition = mMediaPlayer.getCurrentPosition() + r.nextInt(duration/3);
            mMediaPlayer.seekTo(nextPosition);
            Log.v(TAG, "playMediaSamples: current position after the first seek:"
                        + mMediaPlayer.getCurrentPosition());
            mMediaPlayer.start();
            Thread.sleep(r.nextInt(duration/6));
            Log.v(TAG, "playMediaSamples: position after the second play:"
                        + mMediaPlayer.getCurrentPosition());
            mMediaPlayer.seekTo(r.nextInt(duration));
            Log.v(TAG, "playMediaSamples: current position after the second seek:"
                        + mMediaPlayer.getCurrentPosition());
            waittime = duration - mMediaPlayer.getCurrentPosition();
            synchronized(onCompletion){
                try {
                    onCompletion.wait(waittime + 30000);
                }catch (Exception e) {
                    Log.v(TAG, "playMediaSamples are interrupted");
                    return false;
                }
            }
            terminateMessageLooper();
        }catch (Exception e) {
            Log.v(TAG, "playMediaSamples:" + e.getMessage());
        }
        return onCompleteSuccess;
    }
}
