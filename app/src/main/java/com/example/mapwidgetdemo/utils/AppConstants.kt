package com.example.mapwidgetdemo.utils

object AppConstants {
    const val NEAR_METER = 10
    var boolFromHome = true


    object SharedPreferenceKeys {
        const val F_TOKEN = "f_token"
        const val IS_GUEST = "IS_GUEST"
        const val NAME = "NAME"
        const val EMAIL = "EMAIL"
        const val IS_UPLOAD_SERVER = "IS_UPLOAD_SERVER"
        const val IS_REMOVE_FROM_DEVICE = "IS_REMOVE_FROM_DEVICE"
        const val PREF_MAP_VIDEO_LIST = "PREF_MAP_VIDEO_LIST"
        const val USER_CURRENT_LATITUDE = "USER_CURRENT_LATITUDE"
        const val USER_CURRENT_LONGITUDE = "USER_CURRENT_LONGITUDE"
    }


    class DialogCodes {
        companion object {
            const val DIALOG_CLAIM_REWARD = 0
            const val DIALOG_SETTING_SUB = 1
            const val DIALOG_CONTACT_US = 2
            const val DIALOG_SUB_TYPE = 3
            const val DIALOG_UPDATE_CHILD_NAME = 4
            const val DIALOG_UPDATE_REWARD_MSG = 5
            const val DIALOG_REWARD_CLAIMED = 6
            const val DIALOG_SIGN_OUT = 7
        }
    }

}