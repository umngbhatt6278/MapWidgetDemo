package com.example.mapwidgetdemo.custom_camera;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.mapwidgetdemo.R;
import com.example.mapwidgetdemo.custom_camera.constants.Constants;
import com.example.mapwidgetdemo.custom_camera.util.GLUtil;
import com.example.mapwidgetdemo.custom_camera.view.PinchZoomGestureListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements VideoFragment.PermissionInterface, PhotoFragment.PhotoPermission,VideoFragment.SwitchInterface,
PhotoFragment.SwitchPhoto, VideoFragment.LowestThresholdCheckForVideoInterface, PhotoFragment.LowestThresholdCheckForPictureInterface
{

    private static final String TAG = "CameraActivity";
    VideoFragment videoFragment = null;
    PhotoFragment photoFragment = null;
    View warningMsgRoot;
    Dialog warningMsg;
    Button okButton;
    LayoutInflater layoutInflater;
    SharedPreferences sharedPreferences;
    boolean VERBOSE = false;
    View settingsRootView;
    Dialog settingsDialog;
    ImageView brightness;
    ControlVisbilityPreference controlVisbilityPreference;
    boolean fromGallery = false;
    PinchZoomGestureListener pinchZoomGestureListener;
    ScaleGestureDetector scaleGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(VERBOSE)Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_camera);
        brightness = (ImageView)findViewById(R.id.brightness);
        controlVisbilityPreference = (ControlVisbilityPreference)getApplicationContext();
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(savedInstanceState == null) {
            //Start with video fragment
            showVideoFragment();
            controlVisbilityPreference.setBrightnessLevel(Constants.NORMAL_BRIGHTNESS);
            controlVisbilityPreference.setBrightnessProgress(0.0f);
        }
        layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        warningMsgRoot = layoutInflater.inflate(R.layout.warning_message,null);
        warningMsg = new Dialog(this);
        settingsRootView = layoutInflater.inflate(R.layout.brightness_settings, null);
        settingsDialog = new Dialog(this);
        sharedPreferences = getSharedPreferences(Constants.FC_SETTINGS, Context.MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            fromGallery = bundle.getBoolean("fromGallery");
        }
        setPinchZoomScaleListener(videoFragment!=null ? videoFragment : null, photoFragment!=null ? photoFragment : null);
    }

    private void setPinchZoomScaleListener(VideoFragment videoFragment, PhotoFragment photoFragment){
        if(pinchZoomGestureListener != null){
            pinchZoomGestureListener = null;
        }
        pinchZoomGestureListener = new PinchZoomGestureListener(getApplicationContext(), videoFragment,
                photoFragment);
        scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), pinchZoomGestureListener);
    }

    public PinchZoomGestureListener getPinchZoomGestureListener(){
        return pinchZoomGestureListener;
    }

    void displaySDCardNotDetectMessage(){
        if(VERBOSE)Log.d(TAG, "displaySDCardNotDetectMessage");
        //The below variable is needed to check if there was SD Card removed in MediaActivity which caused the control
        // to come here.
        if(fromGallery) {
            //Show SD Card not detected, please insert sd card to try again.
            TextView warningTitle = (TextView) warningMsgRoot.findViewById(R.id.warningTitle);
            warningTitle.setText(getResources().getString(R.string.sdCardNotDetectTitle));
            TextView warningText = (TextView) warningMsgRoot.findViewById(R.id.warningText);
            warningText.setText(getResources().getString(R.string.sdCardNotDetectMessage));
            okButton = (Button) warningMsgRoot.findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    warningMsg.dismiss();
                    videoFragment.getLatestFileIfExists();
                }
            });
            warningMsg.setContentView(warningMsgRoot);
            warningMsg.setCancelable(false);
            warningMsg.show();
            if(VERBOSE)Log.d(TAG, "MESSAGE SHOWN");
        }
    }

    public String doesSDCardExist(){
        String sdcardpath = sharedPreferences.getString(Constants.SD_CARD_PATH, "");
        try {
            String filename = "/doesSDCardExist_"+String.valueOf(System.currentTimeMillis()).substring(0,5);
            sdcardpath += filename;
            final String sdCardFilePath = sdcardpath;
            final FileOutputStream createTestFile = new FileOutputStream(sdcardpath);
            if(VERBOSE)Log.d(TAG, "Able to create file... SD Card exists");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File testfile = new File(sdCardFilePath);
                    try {
                        createTestFile.close();
                        testfile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (FileNotFoundException e) {
            if(VERBOSE)Log.d(TAG, "Unable to create file... SD Card NOT exists..... "+e.getMessage());
            return null;
        }
        return sharedPreferences.getString(Constants.SD_CARD_PATH, "");
    }

    public void goToSettings(View view){
       /* Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);*/
    }

    public void openBrightnessPopup(View view){
        TextView header = (TextView)settingsRootView.findViewById(R.id.timerText);
        header.setText(getResources().getString(R.string.brightnessHeading));
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        settingsDialog.setContentView(settingsRootView);
        settingsDialog.setCancelable(true);
        WindowManager.LayoutParams lp = settingsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        lp.width = (int)(size.x * 0.8);
        final SeekBar brightnessBar = (SeekBar)settingsRootView.findViewById(R.id.brightnessBar);
        brightnessBar.setMax(10);
        brightnessBar.setProgress(controlVisbilityPreference.getBrightnessLevel());
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(controlVisbilityPreference.getBrightnessLevel());
            }
        });
        Button increaseBrightness = (Button)settingsRootView.findViewById(R.id.increaseBrightness);
        increaseBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isVideo()) {
                    if(GLUtil.colorVal < 0.25f) {
                        GLUtil.colorVal += 0.05f;
                        brightnessBar.incrementProgressBy(1);
                        controlVisbilityPreference.setBrightnessLevel(brightnessBar.getProgress());
                    }
                    else{
                        GLUtil.colorVal = 0.25f;
                    }
                    controlVisbilityPreference.setBrightnessProgress(GLUtil.colorVal);
                }
            }
        });
        Button decreaseBrightness = (Button)settingsRootView.findViewById(R.id.setTimer);
        decreaseBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isVideo()){
                    if(GLUtil.colorVal > -0.25f) {
                        GLUtil.colorVal -= 0.05f;
                        brightnessBar.incrementProgressBy(-1);
                        controlVisbilityPreference.setBrightnessLevel(brightnessBar.getProgress());
                    }
                    else{
                        GLUtil.colorVal = -0.25f;
                    }
                    controlVisbilityPreference.setBrightnessProgress(GLUtil.colorVal);
                }
            }
        });
        settingsDialog.getWindow().setBackgroundDrawableResource(R.color.backColorSettingPopup);
        settingsDialog.show();
    }

    private boolean isVideo(){
        return videoFragment!=null;
    }

    @Override
    public void switchToPhoto() {
        showPhotoFragment();
    }

    @Override
    public void switchToVideo() {
        showVideoFragment();
    }

    public void showVideoFragment()
    {
        if(videoFragment == null) {
            if(VERBOSE)Log.d(TAG,"creating videofragment");
            videoFragment = VideoFragment.newInstance();
            videoFragment.setApplicationContext(getApplicationContext());
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.cameraPreview, videoFragment).commit();
        if(VERBOSE)Log.d(TAG, "brightnessLevel SET to = "+controlVisbilityPreference.getBrightnessLevel());
        brightness.setVisibility(View.VISIBLE);
        setPinchZoomScaleListener(videoFragment, null);
    }

    public void showPhotoFragment()
    {
        brightness.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(photoFragment == null) {
            if(VERBOSE)Log.d(TAG,"creating photofragment");
            photoFragment = PhotoFragment.newInstance();
            photoFragment.setApplicationContext(getApplicationContext());
        }
        setPinchZoomScaleListener(null, photoFragment);
        fragmentTransaction.replace(R.id.cameraPreview, photoFragment).commit();
        if(VERBOSE)Log.d(TAG,"photofragment added");
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        if(!sharedPreferences.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)){
            //Check if SD Card exists
            if(doesSDCardExist() == null){
                settingsEditor.putBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true);
                settingsEditor.commit();
                TextView warningTitle = (TextView)warningMsgRoot.findViewById(R.id.warningTitle);
                warningTitle.setText(getResources().getString(R.string.sdCardRemovedTitle));
                TextView warningText = (TextView)warningMsgRoot.findViewById(R.id.warningText);
                warningText.setText(getResources().getString(R.string.sdCardNotPresentForRecord));
                okButton = (Button)warningMsgRoot.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        warningMsg.dismiss();
                        photoFragment.getLatestFileIfExists();
                    }
                });
                warningMsg.setContentView(warningMsgRoot);
                warningMsg.setCancelable(false);
                warningMsg.show();
            }
        }
        else if(!checkIfPhoneMemoryIsBelowLowThreshold() && !sharedPreferences.getBoolean(Constants.PHONE_MEMORY_DISABLE, true)) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            int memoryThreshold = Integer.parseInt(sharedPreferences.getString(Constants.PHONE_MEMORY_LIMIT, ""));
            String memoryMetric = sharedPreferences.getString(Constants.PHONE_MEMORY_METRIC, "");
            StatFs storageStat = new StatFs(Environment.getDataDirectory().getPath());
            long memoryValue = 0;
            String metric = "";
            switch (memoryMetric) {
                case Constants.METRIC_MB:
                    memoryValue = (memoryThreshold * (long) Constants.MEGA_BYTE);
                    metric = Constants.METRIC_MB;
                    break;
                case Constants.METRIC_GB:
                    memoryValue = (memoryThreshold * (long) Constants.GIGA_BYTE);
                    metric = Constants.METRIC_GB;
                    break;
            }
            if(VERBOSE)Log.d(TAG, "memory value = " + memoryValue);
            if(VERBOSE)Log.d(TAG, "Avail mem = " + storageStat.getAvailableBytes());
            if (storageStat.getAvailableBytes() < memoryValue) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View thresholdExceededRoot = layoutInflater.inflate(R.layout.threshold_exceeded, null);
                final Dialog thresholdDialog = new Dialog(this);
                TextView memoryLimitMsg = (TextView) thresholdExceededRoot.findViewById(R.id.memoryLimitMsg);
                final CheckBox disableThreshold = (CheckBox) thresholdExceededRoot.findViewById(R.id.disableThreshold);
                Button okButton = (Button) thresholdExceededRoot.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(VERBOSE)Log.d(TAG, "disableThreshold.isChecked = " + disableThreshold.isChecked());
                        if (disableThreshold.isChecked()) {
                            editor.remove(Constants.PHONE_MEMORY_LIMIT);
                            editor.remove(Constants.PHONE_MEMORY_METRIC);
                            editor.putBoolean(Constants.PHONE_MEMORY_DISABLE, true);
                            editor.commit();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.minimumThresholdDisabled), Toast.LENGTH_LONG).show();
                        }
                        thresholdDialog.dismiss();
                    }
                });
                StringBuilder memThreshold = new StringBuilder(memoryThreshold + "");
                memThreshold.append(" ");
                memThreshold.append(metric);
                if(VERBOSE)Log.d(TAG, "memory threshold for display = " + memThreshold);
                memoryLimitMsg.setText(getResources().getString(R.string.thresholdLimitExceededMsg, memThreshold.toString()));
                thresholdDialog.setContentView(thresholdExceededRoot);
                thresholdDialog.setCancelable(false);
                thresholdDialog.show();
            }
        }
    }

    public boolean checkIfPhoneMemoryIsBelowLowThreshold(){
        if(sharedPreferences.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)){
            StatFs storageStat = new StatFs(Environment.getDataDirectory().getPath());
            int lowestThreshold = getResources().getInteger(R.integer.minimumMemoryWarning);
            long lowestMemory = lowestThreshold * (long)Constants.MEGA_BYTE;
            if(VERBOSE)Log.d(TAG, "lowestMemory = "+lowestMemory);
            if(VERBOSE)Log.d(TAG, "avail mem = "+storageStat.getAvailableBytes());
            if(storageStat.getAvailableBytes() < lowestMemory){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public boolean checkIfPhoneMemoryIsBelowLowestThresholdForPicture() {
        return checkIfPhoneMemoryIsBelowLowThreshold();
    }

    @Override
    public boolean checkIfPhoneMemoryIsBelowLowestThresholdForVideo() {
        return checkIfPhoneMemoryIsBelowLowThreshold();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(VERBOSE)Log.d(TAG,"onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(VERBOSE)Log.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(VERBOSE)Log.d(TAG,"onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(VERBOSE)Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(VERBOSE)Log.d(TAG,"onPause");
        if(this.videoFragment!=null) {
            this.videoFragment.getZoomBar().setProgress(0);
        }
        else if(this.photoFragment!=null) {
            this.photoFragment.getZoomBar().setProgress(0);
        }
        pinchZoomGestureListener.setProgress(0);
    }

    @Override
    public void askPermission() {
        askCameraPermission();
    }

    @Override
    public void askPhotoPermission() {
        askCameraPermission();
    }

    public void askCameraPermission(){
        if(VERBOSE)Log.d(TAG,"start permission act to get permissions");
        Intent permission = new Intent(this,PermissionActivity.class);
        permission.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(permission);
        finish();
    }
}
