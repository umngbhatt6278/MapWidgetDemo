package com.example.mapwidgetdemo.custom_camera;

import android.app.Application;

import com.example.mapwidgetdemo.custom_camera.constants.Constants;
import com.example.mapwidgetdemo.ui.activity.MainApplication;

import java.io.Serializable;

/**
 * Created by koushick on 10-Nov-17.
 */

public class ControlVisbilityPreference extends MainApplication implements Serializable{

    private boolean hideControl;
    private int mediaSelectedPosition;
    private int brightnessLevel = Constants.NORMAL_BRIGHTNESS;
    private float brightnessProgress = Constants.NORMAL_BRIGHTNESS_PROGRESS;
    private boolean fromGallery = false;

    public boolean isFromGallery() {
        return fromGallery;
    }

    public void setFromGallery(boolean fromGallery) {
        this.fromGallery = fromGallery;
    }

    public float getBrightnessProgress() {
        return brightnessProgress;
    }

    public void setBrightnessProgress(float brightnessProgress) {
        this.brightnessProgress = brightnessProgress;
    }

    public int getBrightnessLevel() {
        return brightnessLevel;
    }

    public void setBrightnessLevel(int brightnessLevel) {
        this.brightnessLevel = brightnessLevel;
    }

    public int getMediaSelectedPosition() {
        return mediaSelectedPosition;
    }

    public void setMediaSelectedPosition(int mediaSelectedPosition) {
        this.mediaSelectedPosition = mediaSelectedPosition;
    }

    public boolean isHideControl() {
        return hideControl;
    }

    public void setHideControl(boolean hideControl) {
        this.hideControl = hideControl;
    }
}
