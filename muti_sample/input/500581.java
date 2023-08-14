public class MediaPlayerGetVideoWidthStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(!stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleState);
        assertTrue(!stateErrors.errorInIdleStateAfterReset);
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(!stateErrors.errorInStoppedState);
        assertTrue(!stateErrors.errorInPreparedStateAfterStop);
        assertTrue(stateErrors.errorInErrorState);
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.getVideoWidth();
    }
    @LargeTest
    public void testGetVideoWidth() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "getVideoWidth()";
    }
}