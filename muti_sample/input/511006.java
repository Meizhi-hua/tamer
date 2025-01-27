public class MediaRecorderStressTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {    
    private String TAG = "MediaRecorderStressTest";
    private MediaRecorder mRecorder;
    private Camera mCamera;
    private static final int NUMBER_OF_CAMERA_STRESS_LOOPS = 100;
    private static final int NUMBER_OF_RECORDER_STRESS_LOOPS = 100;
    private static final int NUMBER_OF_RECORDERANDPLAY_STRESS_LOOPS = 50;
    private static final int NUMBER_OF_SWTICHING_LOOPS_BW_CAMERA_AND_RECORDER = 200;
    private static final long WAIT_TIME_CAMERA_TEST = 3000;  
    private static final long WAIT_TIME_RECORDER_TEST = 6000;  
    private static final long WAIT_TIME_RECORD = 10000;  
    private static final long WAIT_TIME_PLAYBACK = 6000;  
    private static final String OUTPUT_FILE = "/sdcard/temp";
    private static final String OUTPUT_FILE_EXT = ".3gp";
    private static final String MEDIA_STRESS_OUTPUT =
        "/sdcard/mediaStressOutput.txt";
    private Looper mCameraLooper = null;
    private Looper mRecorderLooper = null;
    private final Object lock = new Object();
    private final Object recorderlock = new Object();
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 10000;  
    private final CameraErrorCallback mCameraErrorCallback = new CameraErrorCallback();
    private final RecorderErrorCallback mRecorderErrorCallback = new RecorderErrorCallback();
    public MediaRecorderStressTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    protected void setUp() throws Exception {
        getActivity();
        super.setUp();      
    }
    private final class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
        public void onError(int error, android.hardware.Camera camera) {
            if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
                assertTrue("Camera test mediaserver died", false);
            }
        }
    }
    private final class RecorderErrorCallback implements MediaRecorder.OnErrorListener {
        public void onError(MediaRecorder mr, int what, int extra) {
            assertTrue("mediaRecorder error", false);
        }
    }
    private void initializeCameraMessageLooper() {
        Log.v(TAG, "start looper");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Log.v(TAG, "start loopRun");
                mCameraLooper = Looper.myLooper();
                mCamera = Camera.open();
                synchronized (lock) {
                    lock.notify();
                }
                Looper.loop();
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
    }
    private void initializeRecorderMessageLooper() {
        Log.v(TAG, "start looper");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Log.v(TAG, "start loopRun");
                mRecorderLooper = Looper.myLooper();
                mRecorder = new MediaRecorder();
                synchronized (recorderlock) {
                    recorderlock.notify();
                }
                Looper.loop();  
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
    }
    private void terminateCameraMessageLooper() {
        mCameraLooper.quit();
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            Log.v(TAG, e.toString());
        }
        mCamera.release();
    }
    private void terminateRecorderMessageLooper() {
        mRecorderLooper.quit();
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            Log.v(TAG, e.toString());
        }
        mRecorder.release();
    }
    @LargeTest
    public void testStressCamera() throws Exception {
        SurfaceHolder mSurfaceHolder;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        File stressOutFile = new File(MEDIA_STRESS_OUTPUT);
        Writer output = new BufferedWriter(new FileWriter(stressOutFile, true));
        output.write("Camera start preview stress:\n");
        output.write("Total number of loops:" +
                NUMBER_OF_CAMERA_STRESS_LOOPS + "\n");
        try {
            Log.v(TAG, "Start preview");
            output.write("No of loop: ");
            for (int i = 0; i< NUMBER_OF_CAMERA_STRESS_LOOPS; i++){
                synchronized (lock) {
                    initializeCameraMessageLooper();
                    try {
                        lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                    } catch(Exception e) {
                        Log.v(TAG, "wait was interrupted.");
                    }
                }
                mCamera.setErrorCallback(mCameraErrorCallback);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                Thread.sleep(WAIT_TIME_CAMERA_TEST);
                mCamera.stopPreview();
                terminateCameraMessageLooper();
                output.write(" ," + i);
            }
        } catch (Exception e) {
            assertTrue("CameraStressTest", false);
            Log.v(TAG, e.toString());
        }
        output.write("\n\n");
        output.close();
    }
    @LargeTest
    public void testStressRecorder() throws Exception {
        String filename;
        SurfaceHolder mSurfaceHolder;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        File stressOutFile = new File(MEDIA_STRESS_OUTPUT);
        Writer output = new BufferedWriter(new FileWriter(stressOutFile, true));
        output.write("H263 video record- reset after prepare Stress test\n");
        output.write("Total number of loops:" +
                NUMBER_OF_RECORDER_STRESS_LOOPS + "\n");
        try {
            output.write("No of loop: ");
            Log.v(TAG, "Start preview");
            for (int i = 0; i < NUMBER_OF_RECORDER_STRESS_LOOPS; i++){
                synchronized (recorderlock) {
                    initializeRecorderMessageLooper();
                    try {
                        recorderlock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                    } catch(Exception e) {
                        Log.v(TAG, "wait was interrupted.");
                    }
                }
                Log.v(TAG, "counter = " + i);
                filename = OUTPUT_FILE + i + OUTPUT_FILE_EXT;
                Log.v(TAG, filename);
                mRecorder.setOnErrorListener(mRecorderErrorCallback);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(filename);
                mRecorder.setVideoFrameRate(20);
                mRecorder.setVideoSize(176,144);
                Log.v(TAG, "setEncoder");
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
                mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
                Log.v(TAG, "setPreview");
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                Log.v(TAG, "prepare");
                mRecorder.prepare();
                Log.v(TAG, "before release");
                Thread.sleep(WAIT_TIME_RECORDER_TEST);
                mRecorder.reset();
                terminateRecorderMessageLooper();
                output.write(", " + i);
            }
        } catch (Exception e) {
            assertTrue("Recorder Stress test", false);
            Log.v(TAG, e.toString());
        }
        output.write("\n\n");
        output.close();
    }
    @LargeTest
    public void testStressCameraSwitchRecorder() throws Exception {
        String filename;
        SurfaceHolder mSurfaceHolder;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        File stressOutFile = new File(MEDIA_STRESS_OUTPUT);
        Writer output = new BufferedWriter(new FileWriter(stressOutFile, true));
        output.write("Camera and video recorder preview switching\n");
        output.write("Total number of loops:"
                + NUMBER_OF_SWTICHING_LOOPS_BW_CAMERA_AND_RECORDER + "\n");
        try {
            Log.v(TAG, "Start preview");
            output.write("No of loop: ");
            for (int i = 0; i < NUMBER_OF_SWTICHING_LOOPS_BW_CAMERA_AND_RECORDER; i++){
                synchronized (lock) {
                    initializeCameraMessageLooper();
                    try {
                        lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                    } catch(Exception e) {
                        Log.v(TAG, "wait was interrupted.");
                    }
                }
                mCamera.setErrorCallback(mCameraErrorCallback);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                Thread.sleep(WAIT_TIME_CAMERA_TEST);
                mCamera.stopPreview();
                terminateCameraMessageLooper();
                mCamera = null;
                Log.v(TAG, "release camera");
                filename = OUTPUT_FILE + i + OUTPUT_FILE_EXT;
                Log.v(TAG, filename);
                synchronized (recorderlock) {
                    initializeRecorderMessageLooper();
                    try {
                        recorderlock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                    } catch(Exception e) {
                        Log.v(TAG, "wait was interrupted.");
                    }
                }
                mRecorder.setOnErrorListener(mRecorderErrorCallback);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(filename);
                mRecorder.setVideoFrameRate(20);
                mRecorder.setVideoSize(176,144);
                Log.v(TAG, "Media recorder setEncoder");
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
                Log.v(TAG, "mediaRecorder setPreview");
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                Log.v(TAG, "prepare");
                mRecorder.prepare();
                Log.v(TAG, "before release");
                Thread.sleep(WAIT_TIME_CAMERA_TEST);
                terminateRecorderMessageLooper();
                Log.v(TAG, "release video recorder");
                output.write(", " + i);
            }
        } catch (Exception e) {
            assertTrue("Camer and recorder switch mode", false);
                Log.v(TAG, e.toString());
        }
        output.write("\n\n");
        output.close();
    }
    public void validateRecordedVideo(String recorded_file) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(recorded_file);
            mp.prepare();
            int duration = mp.getDuration();
            if (duration <= 0){
                assertTrue("stressRecordAndPlayback", false);
            }
        } catch (Exception e) {
            assertTrue("stressRecordAndPlayback", false);
        }
    }
    public void removeRecodedVideo(String filename){
        File video = new File(filename);
        Log.v(TAG, "remove recorded video " + filename);
        video.delete();
    }
    @LargeTest
    public void testStressRecordVideoAndPlayback() throws Exception {
        int iterations = MediaRecorderStressTestRunner.mIterations;
        int video_encoder = MediaRecorderStressTestRunner.mVideoEncoder;
        int audio_encoder = MediaRecorderStressTestRunner.mAudioEncdoer;
        int frame_rate = MediaRecorderStressTestRunner.mFrameRate;
        int video_width = MediaRecorderStressTestRunner.mVideoWidth;
        int video_height = MediaRecorderStressTestRunner.mVideoHeight;
        int bit_rate = MediaRecorderStressTestRunner.mBitRate;
        boolean remove_video = MediaRecorderStressTestRunner.mRemoveVideo;
        int record_duration = MediaRecorderStressTestRunner.mDuration;
        String filename;
        SurfaceHolder mSurfaceHolder;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        File stressOutFile = new File(MEDIA_STRESS_OUTPUT);
        Writer output = new BufferedWriter(
                new FileWriter(stressOutFile, true));
        output.write("Video record and play back stress test:\n");
        output.write("Total number of loops:"
                + NUMBER_OF_RECORDERANDPLAY_STRESS_LOOPS + "\n");
        try {
            output.write("No of loop: ");
            for (int i = 0; i < iterations; i++){
                filename = OUTPUT_FILE + i + OUTPUT_FILE_EXT;
                Log.v(TAG, filename);
                synchronized (recorderlock) {
                    initializeRecorderMessageLooper();
                    try {
                        recorderlock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                    } catch(Exception e) {
                        Log.v(TAG, "wait was interrupted.");
                    }
                }
                Log.v(TAG, "iterations : " + iterations);
                Log.v(TAG, "video_encoder : " + video_encoder);
                Log.v(TAG, "audio_encoder : " + audio_encoder);
                Log.v(TAG, "frame_rate : " + frame_rate);
                Log.v(TAG, "video_width : " + video_width);
                Log.v(TAG, "video_height : " + video_height);
                Log.v(TAG, "bit rate : " + bit_rate);
                Log.v(TAG, "record_duration : " + record_duration);
                mRecorder.setOnErrorListener(mRecorderErrorCallback);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(filename);
                mRecorder.setVideoFrameRate(frame_rate);
                mRecorder.setVideoSize(video_width, video_height);
                mRecorder.setVideoEncoder(video_encoder);
                mRecorder.setAudioEncoder(audio_encoder);
                Log.v(TAG, "mediaRecorder setPreview");
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                mRecorder.prepare();
                mRecorder.start();
                Thread.sleep(record_duration);
                Log.v(TAG, "Before stop");
                mRecorder.stop();
                terminateRecorderMessageLooper();
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(filename);
                mp.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
                mp.prepare();
                mp.start();
                Thread.sleep(record_duration);
                mp.release();
                validateRecordedVideo(filename);
                if (remove_video) {
                    removeRecodedVideo(filename);
                }
                output.write(", " + i);
            }
        } catch (Exception e) {
            assertTrue("record and playback", false);
                Log.v(TAG, e.toString());
        }
        output.write("\n\n");
        output.close();
    }
}
