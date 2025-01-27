public class MediaPlayerApiTest extends ActivityInstrumentationTestCase<MediaFrameworkTest> {    
   private boolean duratoinWithinTolerence = false;
   private String TAG = "MediaPlayerApiTest";
   private boolean isWMAEnable = false;
   private boolean isWMVEnable = false;
   Context mContext;
   public MediaPlayerApiTest() {
     super("com.android.mediaframeworktest", MediaFrameworkTest.class);
     isWMAEnable = MediaProfileReader.getWMAEnable();
     isWMVEnable = MediaProfileReader.getWMVEnable();
   }
    protected void setUp() throws Exception {
      super.setUp();
  }
    public boolean verifyDuration(int duration, int expectedDuration){
      if ((duration > expectedDuration * 1.1) || (duration < expectedDuration * 0.9))
         return false;
      else
        return true;
    }   
    @MediumTest
    public void testMP3CBRGetDuration() throws Exception {
      int duration = CodecTest.getDuration(MediaNames.MP3CBR);
      duratoinWithinTolerence = verifyDuration(duration, MediaNames.MP3CBR_LENGTH);
      assertTrue("MP3CBR getDuration", duratoinWithinTolerence);  
    }
    @MediumTest
    public void testMP3VBRGetDuration() throws Exception {
      int duration = CodecTest.getDuration(MediaNames.MP3VBR);
      Log.v(TAG, "getDuration");
      duratoinWithinTolerence = verifyDuration(duration, MediaNames.MP3VBR_LENGTH);
      assertTrue("MP3VBR getDuration", duratoinWithinTolerence);  
    }
    @MediumTest
    public void testMIDIGetDuration() throws Exception {
      int duration = CodecTest.getDuration(MediaNames.MIDI);  
      duratoinWithinTolerence = verifyDuration(duration, MediaNames.MIDI_LENGTH);
      assertTrue("MIDI getDuration", duratoinWithinTolerence);  
    }
    @MediumTest
    public void testWMA9GetDuration() throws Exception {
      if (isWMAEnable) {
            int duration = CodecTest.getDuration(MediaNames.WMA9);
            duratoinWithinTolerence = verifyDuration(duration, MediaNames.WMA9_LENGTH);
            assertTrue("WMA9 getDuration", duratoinWithinTolerence);
        }
    }
    @MediumTest
    public void testAMRGetDuration() throws Exception {
      int duration = CodecTest.getDuration(MediaNames.AMR);  
      duratoinWithinTolerence = verifyDuration(duration, MediaNames.AMR_LENGTH);
      assertTrue("AMR getDuration", duratoinWithinTolerence);  
    }
    @LargeTest
    public void testMP3CBRGetCurrentPosition() throws Exception {
      boolean currentPosition = CodecTest.getCurrentPosition(MediaNames.MP3CBR);       
      assertTrue("MP3CBR GetCurrentPosition", currentPosition);  
    }
    @LargeTest
    public void testMP3VBRGetCurrentPosition() throws Exception {
      boolean currentPosition = CodecTest.getCurrentPosition(MediaNames.MP3VBR);  
      assertTrue("MP3VBR GetCurrentPosition", currentPosition);  
    }
    @LargeTest
    public void testMIDIGetCurrentPosition() throws Exception {
      boolean currentPosition = CodecTest.getCurrentPosition(MediaNames.MIDI);  
      assertTrue("MIDI GetCurrentPosition", currentPosition);  
    }
    @LargeTest
    public void testWMA9GetCurrentPosition() throws Exception {
        if (isWMAEnable) {
            boolean currentPosition = CodecTest.getCurrentPosition(MediaNames.WMA9);
            assertTrue("WMA9 GetCurrentPosition", currentPosition);
        }
    }
    @LargeTest
    public void testAMRGetCurrentPosition() throws Exception {
      boolean currentPosition = CodecTest.getCurrentPosition(MediaNames.AMR);  
      assertTrue("AMR GetCurrentPosition", currentPosition);  
    }  
    @LargeTest
    public void testMP3CBRPause() throws Exception {
      boolean isPaused = CodecTest.pause(MediaNames.MP3CBR);       
      assertTrue("MP3CBR Pause", isPaused);  
    }
    @LargeTest
    public void testMP3VBRPause() throws Exception {
      boolean isPaused = CodecTest.pause(MediaNames.MP3VBR);  
      assertTrue("MP3VBR Pause", isPaused);  
    }
    @LargeTest
    public void testMIDIPause() throws Exception {
      boolean isPaused = CodecTest.pause(MediaNames.MIDI);  
      assertTrue("MIDI Pause", isPaused);  
    }
    @LargeTest
    public void testWMA9Pause() throws Exception {
        if (isWMAEnable) {
            boolean isPaused = CodecTest.pause(MediaNames.WMA9);
            assertTrue("WMA9 Pause", isPaused);
        }
    }
    @LargeTest
    public void testAMRPause() throws Exception {
      boolean isPaused = CodecTest.pause(MediaNames.AMR);  
      assertTrue("AMR Pause", isPaused);  
    }
    @MediumTest
    public void testMP3CBRPrepareStopRelease() throws Exception {
      CodecTest.prepareStopRelease(MediaNames.MP3CBR);       
      assertTrue("MP3CBR prepareStopRelease", true);  
    }
    @MediumTest
    public void testMIDIPrepareStopRelease() throws Exception {
      CodecTest.prepareStopRelease(MediaNames.MIDI);  
      assertTrue("MIDI prepareStopRelease", true);  
    }
    @MediumTest
    public void testMP3CBRSeekBeforeStart() throws Exception {
      boolean seekBeforePlay = CodecTest.seektoBeforeStart(MediaNames.MP3CBR);       
      assertTrue("MP3CBR SeekBeforePlay", seekBeforePlay);  
    }
    @LargeTest
    public void testMP3CBRSetLooping() throws Exception {
      boolean isLoop = CodecTest.setLooping(MediaNames.MP3CBR);     
      assertTrue("MP3CBR setLooping", isLoop);  
    }
    @LargeTest
    public void testMP3VBRSetLooping() throws Exception {
      boolean isLoop = CodecTest.setLooping(MediaNames.MP3VBR);
      Log.v(TAG, "setLooping");
      assertTrue("MP3VBR setLooping", isLoop);  
    }
    @LargeTest
    public void testMIDISetLooping() throws Exception {
      boolean isLoop = CodecTest.setLooping(MediaNames.MIDI);  
      assertTrue("MIDI setLooping", isLoop);  
    }
    @LargeTest
    public void testWMA9SetLooping() throws Exception {
      if (isWMAEnable) {
        boolean isLoop = CodecTest.setLooping(MediaNames.WMA9);
        assertTrue("WMA9 setLooping", isLoop);
      }
    }
    @LargeTest
    public void testAMRSetLooping() throws Exception {
      boolean isLoop = CodecTest.setLooping(MediaNames.AMR);  
      assertTrue("AMR setLooping", isLoop);  
    }
    @LargeTest
    public void testMP3CBRSeekTo() throws Exception {
      boolean isLoop = CodecTest.seekTo(MediaNames.MP3CBR);     
      assertTrue("MP3CBR seekTo", isLoop);  
    }
    @LargeTest
    public void testMP3VBRSeekTo() throws Exception {
      boolean isLoop = CodecTest.seekTo(MediaNames.MP3VBR);
      Log.v(TAG, "seekTo");
      assertTrue("MP3VBR seekTo", isLoop);  
    }
    @LargeTest
    public void testMIDISeekTo() throws Exception {
      boolean isLoop = CodecTest.seekTo(MediaNames.MIDI);  
      assertTrue("MIDI seekTo", isLoop);  
    }
    @LargeTest
    public void testWMA9SeekTo() throws Exception {
        if (isWMAEnable) {
            boolean isLoop = CodecTest.seekTo(MediaNames.WMA9);
            assertTrue("WMA9 seekTo", isLoop);
        }
    }
    @LargeTest
    public void testAMRSeekTo() throws Exception {
      boolean isLoop = CodecTest.seekTo(MediaNames.AMR);  
      assertTrue("AMR seekTo", isLoop);  
    }
    @LargeTest
    public void testMP3CBRSeekToEnd() throws Exception {
      boolean isEnd = CodecTest.seekToEnd(MediaNames.MP3CBR);     
      assertTrue("MP3CBR seekToEnd", isEnd);  
    }
    @LargeTest
    public void testMP3VBRSeekToEnd() throws Exception {
      boolean isEnd = CodecTest.seekToEnd(MediaNames.MP3VBR);
      Log.v(TAG, "seekTo");
      assertTrue("MP3VBR seekToEnd", isEnd);  
    }
    @LargeTest
    public void testMIDISeekToEnd() throws Exception {
      boolean isEnd = CodecTest.seekToEnd(MediaNames.MIDI);  
      assertTrue("MIDI seekToEnd", isEnd);  
    }
    @Suppress
    @LargeTest
    public void testWMA9SeekToEnd() throws Exception {
        if (isWMAEnable) {
            boolean isEnd = CodecTest.seekToEnd(MediaNames.WMA9);
            assertTrue("WMA9 seekToEnd", isEnd);
        }
    }
    @LargeTest
    public void testAMRSeekToEnd() throws Exception {
      boolean isEnd = CodecTest.seekToEnd(MediaNames.AMR);  
      assertTrue("AMR seekToEnd", isEnd);  
    }
    @LargeTest
    public void testWAVSeekToEnd() throws Exception {
        if (isWMVEnable) {
            boolean isEnd = CodecTest.seekToEnd(MediaNames.WAV);
            assertTrue("WAV seekToEnd", isEnd);
        }
    }  
    @MediumTest
    public void testLargeVideoHeigth() throws Exception {
      int height = 0;
      height = CodecTest.videoHeight(MediaNames.VIDEO_LARGE_SIZE_3GP);
      Log.v(TAG, "Video height = " +  height);
      assertEquals("streaming video height", 240, height);           
    }
    @MediumTest
    public void testLargeVideoWidth() throws Exception {
      int width = 0;
      width = CodecTest.videoWidth(MediaNames.VIDEO_LARGE_SIZE_3GP);
      Log.v(TAG, "Video width = " +  width);
      assertEquals("streaming video width", 320, width);           
    }
    @LargeTest
    public void testVideoMP4SeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_MP4);
      assertTrue("Local MP4 SeekTo", isSeek);          
    }
    @LargeTest
    public void testVideoLong3gpSeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_LONG_3GP);
      assertTrue("Local 3gp SeekTo", isSeek);         
    }
    @LargeTest
    public void testVideoH263AACSeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_H263_AAC);
      assertTrue("H263AAC SeekTo", isSeek);         
    }
    @LargeTest
    public void testVideoH263AMRSeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_H263_AMR);
      assertTrue("H263AMR SeekTo", isSeek);         
    }
    @LargeTest
    public void testVideoH264AACSeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_H264_AAC);
      assertTrue("H264AAC SeekTo", isSeek);         
    }
    @LargeTest
    public void testVideoH264AMRSeekTo() throws Exception {
      boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_H264_AMR);
      assertTrue("H264AMR SeekTo", isSeek);         
    }
    @LargeTest
    public void testVideoWMVSeekTo() throws Exception {
        Log.v(TAG, "wmv not enable");
        if (isWMVEnable) {
            Log.v(TAG, "wmv enable");
            boolean isSeek = CodecTest.videoSeekTo(MediaNames.VIDEO_WMV);
            assertTrue("WMV SeekTo", isSeek);
        }
    }
    @LargeTest
    public void testSoundRecord() throws Exception {
      boolean isRecordered = CodecTest.mediaRecorderRecord(MediaNames.RECORDER_OUTPUT);
      assertTrue("Recorder", isRecordered);         
    }
    @LargeTest
    public void testGetThumbnail() throws Exception {
      boolean getThumbnail = CodecTest.getThumbnail(MediaNames.VIDEO_H264_AAC, MediaNames.GOLDEN_THUMBNAIL_OUTPUT_2);
      assertTrue("Get Thumbnail", getThumbnail);         
    }
    @LargeTest
    public void testMidiResources() throws Exception {
      boolean midiResources = CodecTest.resourcesPlayback(MediaFrameworkTest.midiafd,16000);
      assertTrue("Play midi from resources", midiResources);         
    }
    @LargeTest
    public void testMp3Resources() throws Exception {
      boolean mp3Resources = CodecTest.resourcesPlayback(MediaFrameworkTest.mp3afd,25000);
      assertTrue("Play mp3 from resources", mp3Resources);         
    }
    @MediumTest
    public void testPrepareAsyncReset() throws Exception {
      boolean isReset = CodecTest.prepareAsyncReset(MediaNames.STREAM_MP3);
      assertTrue("PrepareAsync Reset", isReset);         
    }
    @MediumTest
    public void testIsLooping() throws Exception {
        boolean isLooping = CodecTest.isLooping(MediaNames.AMR);
        assertTrue("isLooping", isLooping);
    }
    @MediumTest
    public void testIsLoopingAfterReset() throws Exception {
        boolean isLooping = CodecTest.isLoopingAfterReset(MediaNames.AMR);
        assertTrue("isLooping after reset", isLooping);
    }
    @LargeTest
    public void testLocalMp3PrepareAsyncCallback() throws Exception {
        boolean onPrepareSuccess = 
            CodecTest.prepareAsyncCallback(MediaNames.MP3CBR, false);
        assertTrue("LocalMp3prepareAsyncCallback", onPrepareSuccess);
    }
    @LargeTest
    public void testLocalH263AMRPrepareAsyncCallback() throws Exception {
        boolean onPrepareSuccess =
            CodecTest.prepareAsyncCallback(MediaNames.VIDEO_H263_AMR, false);
        assertTrue("testLocalH263AMRPrepareAsyncCallback", onPrepareSuccess);
    }
    @LargeTest
    public void testStreamPrepareAsyncCallback() throws Exception {
        boolean onPrepareSuccess = 
            CodecTest.prepareAsyncCallback(MediaNames.STREAM_H264_480_360_1411k, false);
        assertTrue("StreamH264PrepareAsyncCallback", onPrepareSuccess);
    }
    @LargeTest
    public void testStreamPrepareAsyncCallbackReset() throws Exception {
        boolean onPrepareSuccess = 
            CodecTest.prepareAsyncCallback(MediaNames.STREAM_H264_480_360_1411k, true);
        assertTrue("StreamH264PrepareAsyncCallback", onPrepareSuccess);
    }
    @Suppress
    @LargeTest
    public void testMediaSamples() throws Exception {
        boolean onCompleteSuccess = false;
        File dir = new File(MediaNames.MEDIA_SAMPLE_POOL);
        String[] children = dir.list();
        if (children == null) {
            Log.v("MediaPlayerApiTest:testMediaSamples", "dir is empty");
            return;
        } else {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i];
                Log.v("MediaPlayerApiTest",
                    "testMediaSamples: file to be played: "
                    + dir + "/" + filename);
                onCompleteSuccess =
                    CodecTest.playMediaSamples(dir + "/" + filename);
                assertTrue("testMediaSamples", onCompleteSuccess);
            }
       }
    }
}
