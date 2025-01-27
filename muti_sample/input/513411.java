public class OtaUtils {
    private static final String LOG_TAG = "OtaUtils";
    private static final String UNACTIVATED_MIN2_VALUE = "000000";
    private static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 1);
    public static final int OTA_SHOW_ACTIVATION_SCREEN_OFF = 0;
    public static final int OTA_SHOW_ACTIVATION_SCREEN_ON = 1;
    public static final int OTA_SHOW_LISTENING_SCREEN_OFF =0;
    public static final int OTA_SHOW_LISTENING_SCREEN_ON =1;
    public static final int OTA_SHOW_ACTIVATE_FAIL_COUNT_OFF = 0;
    public static final int OTA_SHOW_ACTIVATE_FAIL_COUNT_THREE = 3;
    public static final int OTA_PLAY_SUCCESS_FAILURE_TONE_OFF = 0;
    public static final int OTA_PLAY_SUCCESS_FAILURE_TONE_ON = 1;
    public final int OTA_SPC_TIMEOUT = 60;
    public final int OTA_FAILURE_DIALOG_TIMEOUT = 2;
    private InCallScreen mInCallScreen;
    private Context mContext;
    private PhoneApp mApplication;
    private OtaWidgetData mOtaWidgetData;
    private ViewGroup mInCallPanel;
    private CallCard mCallCard;
    private DTMFTwelveKeyDialer mDialer;
    private DTMFTwelveKeyDialer mOtaCallCardDtmfDialer;
    private static boolean mIsWizardMode = true;
    private class OtaWidgetData {
        public Button otaEndButton;
        public Button otaActivateButton;
        public Button otaSkipButton;
        public Button otaNextButton;
        public ToggleButton otaSpeakerButton;
        public View otaCallCardBase;
        public View callCardOtaButtonsFailSuccess;
        public ProgressBar otaTextProgressBar;
        public TextView otaTextSuccessFail;
        public View callCardOtaButtonsActivate;
        public View callCardOtaButtonsListenProgress;
        public TextView otaTextActivate;
        public TextView otaTextListenProgress;
        public ScrollView otaTextListenProgressContainer;
        public AlertDialog spcErrorDialog;
        public AlertDialog otaFailureDialog;
        public AlertDialog otaSkipConfirmationDialog;
        public TextView otaTitle;
        public DTMFTwelveKeyDialerView otaDtmfDialerView;
        public Button otaTryAgainButton;
    }
    public OtaUtils(Context context,
                    InCallScreen inCallScreen,
                    ViewGroup inCallPanel,
                    CallCard callCard,
                    DTMFTwelveKeyDialer dialer) {
        if (DBG) log("Enter OtaUtil constructor");
        mInCallScreen = inCallScreen;
        mContext = context;
        mInCallPanel = inCallPanel;
        mCallCard = callCard;
        mDialer = dialer;
        mApplication = PhoneApp.getInstance();
        mOtaWidgetData = new OtaWidgetData();
        ViewStub otaCallCardStub = (ViewStub) mInCallScreen.findViewById(R.id.otaCallCardStub);
        otaCallCardStub.inflate();
        readXmlSettings();
        initOtaInCallScreen();
    }
    public static boolean needsActivation(String minString) throws IllegalArgumentException {
        if (minString == null || (minString.length() < 6)) {
            throw new IllegalArgumentException();
        }
        return (minString.equals(UNACTIVATED_MIN_VALUE)
                || minString.substring(0,6).equals(UNACTIVATED_MIN2_VALUE))
                || SystemProperties.getBoolean("test_cdma_setup", false);
    }
    public static boolean maybeDoOtaCall(Context context, Handler handler, int request) {
        PhoneApp app = PhoneApp.getInstance();
        Phone phone = app.phone;
        if (!isCdmaPhone()) {
            if (DBG) Log.v("OtaUtils", "Can't run provisioning on a non-CDMA phone");
            return true; 
        }
        if (!phone.isMinInfoReady()) {
            if (DBG) log("MIN is not ready. Registering to receive notification.");
            phone.registerForSubscriptionInfoReady(handler, request, null);
            return false;
        }
        phone.unregisterForSubscriptionInfoReady(handler);
        String min = phone.getCdmaMin();
        if (DBG) log("min_string: " + min);
        boolean phoneNeedsActivation = false;
        try {
            phoneNeedsActivation = needsActivation(min);
        } catch (IllegalArgumentException e) {
            if (DBG) log("invalid MIN string, exit");
            return true; 
        }
        if (DBG) log("phoneNeedsActivation is set to " + phoneNeedsActivation);
        int otaShowActivationScreen = context.getResources().getInteger(
                R.integer.OtaShowActivationScreen);
        if (DBG) log("otaShowActivationScreen: " + otaShowActivationScreen);
        if (phoneNeedsActivation && (otaShowActivationScreen == OTA_SHOW_ACTIVATION_SCREEN_ON)) {
            app.cdmaOtaProvisionData.isOtaCallIntentProcessed = false;
            Intent newIntent = new Intent(InCallScreen.ACTION_SHOW_ACTIVATION);
            newIntent.setClass(context, InCallScreen.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIsWizardMode = false;
            context.startActivity(newIntent);
            if (DBG) log("activation intent sent.");
        } else {
            if (DBG) log("activation intent NOT sent.");
        }
        return true;
    }
    private void setSpeaker(boolean state) {
        if (DBG) log("setSpeaker : " + state );
        if (state == PhoneUtils.isSpeakerOn(mContext)) {
            if (DBG) log("no change. returning");
            return;
        }
        if (state && mInCallScreen.isBluetoothAvailable()
                && mInCallScreen.isBluetoothAudioConnected()) {
            mInCallScreen.disconnectBluetoothAudio();
        }
        PhoneUtils.turnOnSpeaker(mContext, state, true);
    }
    public void onOtaProvisionStatusChanged(AsyncResult r) {
        int OtaStatus[] = (int[]) r.result;
        if (DBG) log("onOtaProvisionStatusChanged(): OtaStatus[0]" + OtaStatus[0]);
        switch(OtaStatus[0]) {
            case Phone.CDMA_OTA_PROVISION_STATUS_SPC_RETRIES_EXCEEDED:
                otaShowInProgressScreen();
                mApplication.cdmaOtaProvisionData.otaSpcUptime = SystemClock.elapsedRealtime();
                otaShowSpcErrorNotice(OTA_SPC_TIMEOUT);
                if (DBG) log("onOtaProvisionStatusChanged(): RETRIES EXCEEDED");
                break;
            case Phone.CDMA_OTA_PROVISION_STATUS_COMMITTED:
                otaShowInProgressScreen();
                mApplication.cdmaOtaProvisionData.isOtaCallCommitted = true;
                if (DBG) log("onOtaProvisionStatusChanged(): DONE, isOtaCallCommitted set to true");
                break;
            case Phone.CDMA_OTA_PROVISION_STATUS_SPL_UNLOCKED:
            case Phone.CDMA_OTA_PROVISION_STATUS_A_KEY_EXCHANGED:
            case Phone.CDMA_OTA_PROVISION_STATUS_SSD_UPDATED:
            case Phone.CDMA_OTA_PROVISION_STATUS_NAM_DOWNLOADED:
            case Phone.CDMA_OTA_PROVISION_STATUS_MDN_DOWNLOADED:
            case Phone.CDMA_OTA_PROVISION_STATUS_IMSI_DOWNLOADED:
            case Phone.CDMA_OTA_PROVISION_STATUS_PRL_DOWNLOADED:
            case Phone.CDMA_OTA_PROVISION_STATUS_OTAPA_STARTED:
            case Phone.CDMA_OTA_PROVISION_STATUS_OTAPA_STOPPED:
            case Phone.CDMA_OTA_PROVISION_STATUS_OTAPA_ABORTED:
                if (DBG) log("onOtaProvisionStatusChanged(): change to ProgressScreen");
                otaShowInProgressScreen();
                break;
            default:
                if (DBG) log("onOtaProvisionStatusChanged(): Ignoring OtaStatus " + OtaStatus[0]);
                break;
        }
    }
    private void otaShowHome() {
        if (DBG) log("OtaShowHome()...");
        mApplication.cdmaOtaScreenState.otaScreenState =
                CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED;
        mInCallScreen.endInCallScreenSession();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory (Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return;
    }
    private void otaSkipActivation() {
        if (DBG) log("otaSkipActivation()...");
        PhoneApp app = PhoneApp.getInstance();
        if (app != null && app.cdmaOtaInCallScreenUiState.reportSkipPendingIntent != null) {
            try {
                app.cdmaOtaInCallScreenUiState.reportSkipPendingIntent.send();
            } catch (CanceledException e) {
            }
        }
        mInCallScreen.finish();
        return;
    }
    private void otaPerformActivation() {
        if (DBG) log("otaPerformActivation()...");
        if (!mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            Intent newIntent = new Intent(Intent.ACTION_CALL);
            newIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, InCallScreen.OTA_NUMBER);
            mInCallScreen.internalResolveIntent(newIntent);
            otaShowListeningScreen();
        }
        return;
    }
    public void otaShowActivateScreen() {
        if (DBG) log("OtaShowActivationScreen()...");
        if (mApplication.cdmaOtaConfigData.otaShowActivationScreen
                == OTA_SHOW_ACTIVATION_SCREEN_ON) {
            if (DBG) log("OtaShowActivationScreen(): show activation screen");
            if (!isDialerOpened()) {
                otaScreenInitialize();
                mOtaWidgetData.otaSkipButton.setVisibility(mIsWizardMode ?
                        View.VISIBLE : View.INVISIBLE);
                mOtaWidgetData.otaTextActivate.setVisibility(View.VISIBLE);
                mOtaWidgetData.callCardOtaButtonsActivate.setVisibility(View.VISIBLE);
            } else {
                mDialer.setHandleVisible(true);
            }
            mApplication.cdmaOtaScreenState.otaScreenState =
                    CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION;
        } else {
            if (DBG) log("OtaShowActivationScreen(): show home screen");
            otaShowHome();
        }
     }
    private void otaShowListeningScreen() {
        if (DBG) log("OtaShowListeningScreen()...");
        if (mApplication.cdmaOtaConfigData.otaShowListeningScreen
                == OTA_SHOW_LISTENING_SCREEN_ON) {
            if (DBG) log("OtaShowListeningScreen(): show listening screen");
            if (!isDialerOpened()) {
                otaScreenInitialize();
                mOtaWidgetData.otaTextListenProgressContainer.setVisibility(View.VISIBLE);
                mOtaWidgetData.otaTextListenProgress.setText(R.string.ota_listen);
                mOtaWidgetData.otaDtmfDialerView.setVisibility(View.VISIBLE);
                mOtaWidgetData.callCardOtaButtonsListenProgress.setVisibility(View.VISIBLE);
                mOtaWidgetData.otaSpeakerButton.setVisibility(View.VISIBLE);
                boolean speakerOn = PhoneUtils.isSpeakerOn(mContext);
                mOtaWidgetData.otaSpeakerButton.setChecked(speakerOn);
            } else {
                mDialer.setHandleVisible(true);
            }
            mApplication.cdmaOtaScreenState.otaScreenState =
                    CdmaOtaScreenState.OtaScreenState.OTA_STATUS_LISTENING;
            mInCallScreen.updateMenuItems();
        } else {
            if (DBG) log("OtaShowListeningScreen(): show progress screen");
            otaShowInProgressScreen();
        }
    }
    private void otaShowInProgressScreen() {
        if (DBG) log("OtaShowInProgressScreen()...");
        if (!isDialerOpened()) {
            otaScreenInitialize();
            mOtaWidgetData.otaTextListenProgressContainer.setVisibility(View.VISIBLE);
            mOtaWidgetData.otaTextListenProgress.setText(R.string.ota_progress);
            mOtaWidgetData.otaTextProgressBar.setVisibility(View.VISIBLE);
            mOtaWidgetData.callCardOtaButtonsListenProgress.setVisibility(View.VISIBLE);
            mOtaWidgetData.otaSpeakerButton.setVisibility(View.VISIBLE);
            boolean speakerOn = PhoneUtils.isSpeakerOn(mContext);
            mOtaWidgetData.otaSpeakerButton.setChecked(speakerOn);
        } else {
            mDialer.setHandleVisible(true);
        }
        mApplication.cdmaOtaScreenState.otaScreenState =
            CdmaOtaScreenState.OtaScreenState.OTA_STATUS_PROGRESS;
        mInCallScreen.updateMenuItems();
    }
    private void otaShowProgramFailure(int length) {
        if (DBG) log("OtaShowProgramFailure()...");
        mApplication.cdmaOtaProvisionData.activationCount++;
        if ((mApplication.cdmaOtaProvisionData.activationCount <
                mApplication.cdmaOtaConfigData.otaShowActivateFailTimes)
                && (mApplication.cdmaOtaConfigData.otaShowActivationScreen ==
                OTA_SHOW_ACTIVATION_SCREEN_ON)) {
            if (DBG) log("OtaShowProgramFailure(): activationCount"
                    + mApplication.cdmaOtaProvisionData.activationCount);
            if (DBG) log("OtaShowProgramFailure(): show failure notice");
            otaShowProgramFailureNotice(length);
        } else {
            if (DBG) log("OtaShowProgramFailure(): show failure dialog");
            otaShowProgramFailureDialog();
        }
    }
    public void otaShowSuccessFailure() {
        if (DBG) log("OtaShowSuccessFailure()...");
        otaScreenInitialize();
        if (DBG) log("OtaShowSuccessFailure(): isOtaCallCommitted"
                + mApplication.cdmaOtaProvisionData.isOtaCallCommitted);
        if (mApplication.cdmaOtaProvisionData.isOtaCallCommitted) {
            if (DBG) log("OtaShowSuccessFailure(), show success dialog");
            otaShowProgramSuccessDialog();
        } else {
            if (DBG) log("OtaShowSuccessFailure(), show failure dialog");
            otaShowProgramFailure(OTA_FAILURE_DIALOG_TIMEOUT);
        }
        return;
    }
    private void otaShowProgramFailureDialog() {
        if (DBG) log("OtaShowProgramFailureDialog()...");
        mApplication.cdmaOtaScreenState.otaScreenState =
                CdmaOtaScreenState.OtaScreenState.OTA_STATUS_SUCCESS_FAILURE_DLG;
        mOtaWidgetData.otaTitle.setText(R.string.ota_title_problem_with_activation);
        mOtaWidgetData.otaTextSuccessFail.setVisibility(View.VISIBLE);
        mOtaWidgetData.otaTextSuccessFail.setText(R.string.ota_unsuccessful);
        mOtaWidgetData.callCardOtaButtonsFailSuccess.setVisibility(View.VISIBLE);
        mOtaWidgetData.otaTryAgainButton.setVisibility(View.VISIBLE);
        if (isDialerOpened()) {
            mDialer.closeDialer(false);
        }
    }
    private void otaShowProgramSuccessDialog() {
        if (DBG) log("OtaShowProgramSuccessDialog()...");
        mApplication.cdmaOtaScreenState.otaScreenState =
                CdmaOtaScreenState.OtaScreenState.OTA_STATUS_SUCCESS_FAILURE_DLG;
        mOtaWidgetData.otaTitle.setText(R.string.ota_title_activate_success);
        mOtaWidgetData.otaTextSuccessFail.setVisibility(View.VISIBLE);
        mOtaWidgetData.otaTextSuccessFail.setText(R.string.ota_successful);
        mOtaWidgetData.callCardOtaButtonsFailSuccess.setVisibility(View.VISIBLE);
        mOtaWidgetData.otaNextButton.setVisibility(View.VISIBLE);
        if (isDialerOpened()) {
            mDialer.closeDialer(false);
        }
    }
    private void otaShowSpcErrorNotice(int length) {
        if (DBG) log("OtaShowSpcErrorNotice()...");
        if (mOtaWidgetData.spcErrorDialog == null) {
            mApplication.cdmaOtaProvisionData.inOtaSpcState = true;
            DialogInterface.OnKeyListener keyListener;
            keyListener = new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    log("Ignoring key events...");
                    return true;
                }};
            mOtaWidgetData.spcErrorDialog = new AlertDialog.Builder(mInCallScreen)
                    .setMessage(R.string.ota_spc_failure)
                    .setOnKeyListener(keyListener)
                    .create();
            mOtaWidgetData.spcErrorDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mOtaWidgetData.spcErrorDialog.show();
            if (isDialerOpened()) {
                mDialer.closeDialer(false);
            }
            long noticeTime = length*1000;
            if (DBG) log("OtaShowSpcErrorNotice(), remaining SPC noticeTime" + noticeTime);
            mInCallScreen.requestCloseSpcErrorNotice(noticeTime);
        }
    }
    public void onOtaCloseSpcNotice() {
        if (DBG) log("onOtaCloseSpcNotice(), send shutdown intent");
        Intent shutdown = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        shutdown.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(shutdown);
    }
    private void otaShowProgramFailureNotice(int length) {
        if (DBG) log("OtaShowProgramFailureNotice()...");
        if (mOtaWidgetData.otaFailureDialog == null) {
            mOtaWidgetData.otaFailureDialog = new AlertDialog.Builder(mInCallScreen)
                    .setMessage(R.string.ota_failure)
                    .create();
            mOtaWidgetData.otaFailureDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mOtaWidgetData.otaFailureDialog.show();
            long noticeTime = length*1000;
            mInCallScreen.requestCloseOtaFailureNotice(noticeTime);
        }
    }
    public void onOtaCloseFailureNotice() {
        if (DBG) log("onOtaCloseFailureNotice()...");
        if (mOtaWidgetData.otaFailureDialog != null) {
            mOtaWidgetData.otaFailureDialog.dismiss();
            mOtaWidgetData.otaFailureDialog = null;
        }
        otaShowActivateScreen();
    }
    private void otaScreenInitialize() {
        if (DBG) log("OtaScreenInitialize()...");
        if (mInCallPanel != null) mInCallPanel.setVisibility(View.GONE);
        if (mCallCard != null) mCallCard.hideCallCardElements();
        mDialer.setHandleVisible(false);
        mOtaWidgetData.otaTitle.setText(R.string.ota_title_activate);
        mOtaWidgetData.otaTextActivate.setVisibility(View.GONE);
        mOtaWidgetData.otaTextListenProgressContainer.setVisibility(View.GONE);
        mOtaWidgetData.otaTextProgressBar.setVisibility(View.GONE);
        mOtaWidgetData.otaTextSuccessFail.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsActivate.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsListenProgress.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsFailSuccess.setVisibility(View.GONE);
        mOtaWidgetData.otaDtmfDialerView.setVisibility(View.GONE);
        mOtaWidgetData.otaSpeakerButton.setVisibility(View.GONE);
        mOtaWidgetData.otaTryAgainButton.setVisibility(View.GONE);
        mOtaWidgetData.otaNextButton.setVisibility(View.GONE);
        mOtaWidgetData.otaCallCardBase.setVisibility(View.VISIBLE);
        mOtaWidgetData.otaSkipButton.setVisibility(View.VISIBLE);
    }
    public void hideOtaScreen() {
        if (DBG) log("hideOtaScreen()...");
        mOtaWidgetData.callCardOtaButtonsActivate.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsListenProgress.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsFailSuccess.setVisibility(View.GONE);
        mOtaWidgetData.otaCallCardBase.setVisibility(View.GONE);
    }
    public boolean isDialerOpened() {
        return (mDialer != null && mDialer.isOpened());
    }
    public void otaShowProperScreen() {
        if (DBG) log("otaShowProperScreen()...");
        if (mInCallScreen.isForegroundActivity()) {
            if (DBG) log("otaShowProperScreen(), OTA is foreground activity, currentstate ="
                    + mApplication.cdmaOtaScreenState.otaScreenState);
            if (mInCallPanel != null) {
                mInCallPanel.setVisibility(View.GONE);
            }
            if (mApplication.cdmaOtaScreenState.otaScreenState
                    == CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION) {
                otaShowActivateScreen();
            } else if (mApplication.cdmaOtaScreenState.otaScreenState
                    == CdmaOtaScreenState.OtaScreenState.OTA_STATUS_LISTENING) {
                otaShowListeningScreen();
            } else if (mApplication.cdmaOtaScreenState.otaScreenState
                    == CdmaOtaScreenState.OtaScreenState.OTA_STATUS_PROGRESS) {
                otaShowInProgressScreen();
            }
            if (mApplication.cdmaOtaProvisionData.inOtaSpcState) {
                otaShowSpcErrorNotice(getOtaSpcDisplayTime());
            }
        }
    }
    private void readXmlSettings() {
        if (DBG) log("readXmlSettings()...");
        if (mApplication.cdmaOtaConfigData.configComplete) {
            return;
        }
        mApplication.cdmaOtaConfigData.configComplete = true;
        int tmpOtaShowActivationScreen =
                mContext.getResources().getInteger(R.integer.OtaShowActivationScreen);
        mApplication.cdmaOtaConfigData.otaShowActivationScreen = tmpOtaShowActivationScreen;
        if (DBG) log("readXmlSettings(), otaShowActivationScreen"
                + mApplication.cdmaOtaConfigData.otaShowActivationScreen);
        int tmpOtaShowListeningScreen =
                mContext.getResources().getInteger(R.integer.OtaShowListeningScreen);
        mApplication.cdmaOtaConfigData.otaShowListeningScreen = tmpOtaShowListeningScreen;
        if (DBG) log("readXmlSettings(), otaShowListeningScreen"
                + mApplication.cdmaOtaConfigData.otaShowListeningScreen);
        int tmpOtaShowActivateFailTimes =
                mContext.getResources().getInteger(R.integer.OtaShowActivateFailTimes);
        mApplication.cdmaOtaConfigData.otaShowActivateFailTimes = tmpOtaShowActivateFailTimes;
        if (DBG) log("readXmlSettings(), otaShowActivateFailTimes"
                + mApplication.cdmaOtaConfigData.otaShowActivateFailTimes);
        int tmpOtaPlaySuccessFailureTone =
                mContext.getResources().getInteger(R.integer.OtaPlaySuccessFailureTone);
        mApplication.cdmaOtaConfigData.otaPlaySuccessFailureTone = tmpOtaPlaySuccessFailureTone;
        if (DBG) log("readXmlSettings(), otaPlaySuccessFailureTone"
                + mApplication.cdmaOtaConfigData.otaPlaySuccessFailureTone);
    }
    public void onClickHandler(int id) {
        switch (id) {
            case R.id.otaEndButton:
                onClickOtaEndButton();
                break;
            case R.id.otaSpeakerButton:
                onClickOtaSpeakerButton();
                break;
            case R.id.otaActivateButton:
                onClickOtaActivateButton();
                break;
            case R.id.otaSkipButton:
                onClickOtaActivateSkipButton();
                break;
            case R.id.otaNextButton:
                onClickOtaActivateNextButton();
                break;
            case R.id.otaTryAgainButton:
                onClickOtaTryAgainButton();
                break;
            default:
                if (DBG) log ("onClickHandler: received a click event for unrecognized id");
                break;
        }
    }
    private void onClickOtaTryAgainButton() {
        if (DBG) log("Activation Try Again Clicked!");
        if (!mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            otaShowActivateScreen();
        }
    }
    private void onClickOtaEndButton() {
        if (DBG) log("Activation End Call Button Clicked!");
        if (!mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            if (PhoneUtils.hangup(mApplication.phone) == false) {
                setSpeaker(false);
                mInCallScreen.handleOtaCallEnd();
            }
        }
    }
    private void onClickOtaSpeakerButton() {
        if (DBG) log("OTA Speaker button Clicked!");
        if (!mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            boolean isChecked = !PhoneUtils.isSpeakerOn(mContext);
            setSpeaker(isChecked);
        }
    }
    private void onClickOtaActivateButton() {
        if (DBG) log("Call Activation Clicked!");
        otaPerformActivation();
    }
    private void onClickOtaActivateSkipButton() {
        if (DBG) log("Activation Skip Clicked!");
        DialogInterface.OnKeyListener keyListener;
        keyListener = new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (DBG) log("Ignoring key events...");
                return true;
            }
        };
        mOtaWidgetData.otaSkipConfirmationDialog = new AlertDialog.Builder(mInCallScreen)
                .setTitle(R.string.ota_skip_activation_dialog_title)
                .setMessage(R.string.ota_skip_activation_dialog_message)
                .setPositiveButton(
                    R.string.ota_skip_activation_dialog_skip_label,
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            otaSkipActivation();
                        }
                    })
                .setNegativeButton(
                    R.string.ota_skip_activation_dialog_continue_label,
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            otaPerformActivation();
                        }
                    })
                .setOnKeyListener(keyListener)
                .create();
        mOtaWidgetData.otaSkipConfirmationDialog.show();
    }
    private void onClickOtaActivateNextButton() {
        if (DBG) log("Dialog Next Clicked!");
        if (!mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            mApplication.cdmaOtaScreenState.otaScreenState =
                    CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED;
            otaShowHome();
        }
    }
    public void dismissAllOtaDialogs() {
        if (mOtaWidgetData.spcErrorDialog != null) {
            if (DBG) log("- DISMISSING mSpcErrorDialog.");
            mOtaWidgetData.spcErrorDialog.dismiss();
            mOtaWidgetData.spcErrorDialog = null;
        }
        if (mOtaWidgetData.otaFailureDialog != null) {
            if (DBG) log("- DISMISSING mOtaFailureDialog.");
            mOtaWidgetData.otaFailureDialog.dismiss();
            mOtaWidgetData.otaFailureDialog = null;
        }
    }
    private int getOtaSpcDisplayTime() {
        if (DBG) log("getOtaSpcDisplayTime()...");
        int tmpSpcTime = 1;
        if (mApplication.cdmaOtaProvisionData.inOtaSpcState) {
            long tmpOtaSpcRunningTime = 0;
            long tmpOtaSpcLeftTime = 0;
            tmpOtaSpcRunningTime = SystemClock.elapsedRealtime();
            tmpOtaSpcLeftTime =
                tmpOtaSpcRunningTime - mApplication.cdmaOtaProvisionData.otaSpcUptime;
            if (tmpOtaSpcLeftTime >= OTA_SPC_TIMEOUT*1000) {
                tmpSpcTime = 1;
            } else {
                tmpSpcTime = OTA_SPC_TIMEOUT - (int)tmpOtaSpcLeftTime/1000;
            }
        }
        if (DBG) log("getOtaSpcDisplayTime(), time for SPC error notice: " + tmpSpcTime);
        return tmpSpcTime;
    }
    private void initOtaInCallScreen() {
        if (DBG) log("initOtaInCallScreen()...");
        mOtaWidgetData.otaTitle = (TextView) mInCallScreen.findViewById(R.id.otaTitle);
        mOtaWidgetData.otaTextActivate = (TextView) mInCallScreen.findViewById(R.id.otaActivate);
        mOtaWidgetData.otaTextActivate.setVisibility(View.GONE);
        mOtaWidgetData.otaTextListenProgressContainer =
                (ScrollView) mInCallScreen.findViewById(R.id.otaListenProgressContainer);
        mOtaWidgetData.otaTextListenProgress =
                (TextView) mInCallScreen.findViewById(R.id.otaListenProgress);
        mOtaWidgetData.otaTextProgressBar =
                (ProgressBar) mInCallScreen.findViewById(R.id.progress_large);
        mOtaWidgetData.otaTextProgressBar.setIndeterminate(true);
        mOtaWidgetData.otaTextSuccessFail =
                (TextView) mInCallScreen.findViewById(R.id.otaSuccessFailStatus);
        mOtaWidgetData.otaCallCardBase = (View) mInCallScreen.findViewById(R.id.otaBase);
        mOtaWidgetData.callCardOtaButtonsListenProgress =
                (View) mInCallScreen.findViewById(R.id.callCardOtaListenProgress);
        mOtaWidgetData.callCardOtaButtonsActivate =
                (View) mInCallScreen.findViewById(R.id.callCardOtaActivate);
        mOtaWidgetData.callCardOtaButtonsFailSuccess =
                (View) mInCallScreen.findViewById(R.id.callCardOtaFailOrSuccessful);
        mOtaWidgetData.otaEndButton = (Button) mInCallScreen.findViewById(R.id.otaEndButton);
        mOtaWidgetData.otaEndButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaSpeakerButton =
                (ToggleButton) mInCallScreen.findViewById(R.id.otaSpeakerButton);
        mOtaWidgetData.otaSpeakerButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaActivateButton =
                (Button) mInCallScreen.findViewById(R.id.otaActivateButton);
        mOtaWidgetData.otaActivateButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaSkipButton = (Button) mInCallScreen.findViewById(R.id.otaSkipButton);
        mOtaWidgetData.otaSkipButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaNextButton = (Button) mInCallScreen.findViewById(R.id.otaNextButton);
        mOtaWidgetData.otaNextButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaTryAgainButton =
                (Button) mInCallScreen.findViewById(R.id.otaTryAgainButton);
        mOtaWidgetData.otaTryAgainButton.setOnClickListener(mInCallScreen);
        mOtaWidgetData.otaDtmfDialerView =
                (DTMFTwelveKeyDialerView) mInCallScreen.findViewById(R.id.otaDtmfDialer);
        if (mOtaWidgetData.otaDtmfDialerView == null) {
            Log.e(LOG_TAG, "onCreate: couldn't find otaDtmfDialer", new IllegalStateException());
        }
        mOtaCallCardDtmfDialer = new DTMFTwelveKeyDialer(mInCallScreen,
                                                         mOtaWidgetData.otaDtmfDialerView,
                                                         null );
        mOtaCallCardDtmfDialer.startDialerSession();
        mOtaWidgetData.otaDtmfDialerView.setDialer(mOtaCallCardDtmfDialer);
    }
    public void cleanOtaScreen(boolean disableSpeaker) {
        if (DBG) log("OTA ends, cleanOtaScreen!");
        mApplication.cdmaOtaScreenState.otaScreenState =
                CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED;
        mApplication.cdmaOtaProvisionData.isOtaCallCommitted = false;
        mApplication.cdmaOtaProvisionData.isOtaCallIntentProcessed = false;
        mApplication.cdmaOtaProvisionData.inOtaSpcState = false;
        mApplication.cdmaOtaProvisionData.activationCount = 0;
        mApplication.cdmaOtaProvisionData.otaSpcUptime = 0;
        mApplication.cdmaOtaInCallScreenUiState.state = State.UNDEFINED;
        if (mInCallPanel != null) mInCallPanel.setVisibility(View.VISIBLE);
        if (mCallCard != null) mCallCard.hideCallCardElements();
        mDialer.setHandleVisible(true);
        if (mOtaCallCardDtmfDialer != null) {
            mOtaCallCardDtmfDialer.stopDialerSession();
        }
        mOtaWidgetData.otaTextActivate.setVisibility(View.GONE);
        mOtaWidgetData.otaTextListenProgressContainer.setVisibility(View.GONE);
        mOtaWidgetData.otaTextProgressBar.setVisibility(View.GONE);
        mOtaWidgetData.otaTextSuccessFail.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsActivate.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsListenProgress.setVisibility(View.GONE);
        mOtaWidgetData.callCardOtaButtonsFailSuccess.setVisibility(View.GONE);
        mOtaWidgetData.otaCallCardBase.setVisibility(View.GONE);
        mOtaWidgetData.otaDtmfDialerView.setVisibility(View.GONE);
        mOtaWidgetData.otaNextButton.setVisibility(View.GONE);
        mOtaWidgetData.otaTryAgainButton.setVisibility(View.GONE);
        if (disableSpeaker) {
            setSpeaker(false);
        }
    }
    public static class CdmaOtaProvisionData {
        public boolean isOtaCallCommitted;
        public boolean isOtaCallIntentProcessed;
        public boolean inOtaSpcState;
        public int activationCount;
        public long otaSpcUptime;
    }
    public static class CdmaOtaConfigData {
        public int otaShowActivationScreen;
        public int otaShowListeningScreen;
        public int otaShowActivateFailTimes;
        public int otaPlaySuccessFailureTone;
        public boolean configComplete;
        public CdmaOtaConfigData() {
            if (DBG) log("CdmaOtaConfigData constructor!");
            otaShowActivationScreen = OTA_SHOW_ACTIVATION_SCREEN_OFF;
            otaShowListeningScreen = OTA_SHOW_LISTENING_SCREEN_OFF;
            otaShowActivateFailTimes = OTA_SHOW_ACTIVATE_FAIL_COUNT_OFF;
            otaPlaySuccessFailureTone = OTA_PLAY_SUCCESS_FAILURE_TONE_OFF;
        }
    }
    public static class CdmaOtaInCallScreenUiState {
        public enum State {
            UNDEFINED,
            NORMAL,
            ENDED
        }
        public State state;
        public CdmaOtaInCallScreenUiState() {
            if (DBG) log("CdmaOtaInCallScreenState: constructor init to UNDEFINED");
            state = CdmaOtaInCallScreenUiState.State.UNDEFINED;
        }
        public PendingIntent reportSkipPendingIntent;
    }
    public void setCdmaOtaInCallScreenUiState(CdmaOtaInCallScreenUiState.State state) {
        if (DBG) log("setCdmaOtaInCallScreenState: " + state);
        mApplication.cdmaOtaInCallScreenUiState.state = state;
    }
    public CdmaOtaInCallScreenUiState.State getCdmaOtaInCallScreenUiState() {
        if (DBG) log("getCdmaOtaInCallScreenState: " + mApplication.cdmaOtaInCallScreenUiState.state);
        return mApplication.cdmaOtaInCallScreenUiState.state;
    }
    public static class CdmaOtaScreenState {
        public enum OtaScreenState {
            OTA_STATUS_UNDEFINED,
            OTA_STATUS_ACTIVATION,
            OTA_STATUS_LISTENING,
            OTA_STATUS_PROGRESS,
            OTA_STATUS_SUCCESS_FAILURE_DLG
        }
        public OtaScreenState otaScreenState;
        public CdmaOtaScreenState() {
            otaScreenState = OtaScreenState.OTA_STATUS_UNDEFINED;
        }
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    public static boolean isCdmaPhone() {
        return (PhoneApp.getInstance().phone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
    }
}
