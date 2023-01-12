package com.example.mapwidgetdemo.utils

import com.example.mapwidgetdemo.ui.activity.MainApplication.Companion.applicationPreference
import com.example.mapwidgetdemo.ui.activity.MainApplication.Companion.applicationPreferenceEditor
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type
import kotlin.collections.ArrayList

/**
 * Application level preference work.
 */
object SharedPreferenceUtils {
    fun preferencePutInteger(key: String?, value: Int) {
        applicationPreferenceEditor!!.putInt(key, value)
        applicationPreferenceEditor!!.commit()
    }

    @JvmStatic
    fun preferenceGetInteger(key: String?, defaultValue: Int): Int {
        return applicationPreference!!.getInt(key, defaultValue)
    }

    fun preferencePutBoolean(key: String?, value: Boolean) {
        applicationPreferenceEditor!!.putBoolean(key, value)
        applicationPreferenceEditor!!.commit()
    }

    @JvmStatic
    fun preferenceGetBoolean(key: String?, defaultValue: Boolean): Boolean {
        return applicationPreference!!.getBoolean(key, defaultValue)
    }

    fun preferencePutString(key: String?, value: String?) {
        applicationPreferenceEditor!!.putString(key, value)
        applicationPreferenceEditor!!.commit()
    }

    @JvmStatic
    fun preferenceGetString(key: String?): String? {
        return applicationPreference!!.getString(key, "")
    }

    fun preferencePutLong(key: String?, value: Long) {
        applicationPreferenceEditor!!.putLong(key, value)
        applicationPreferenceEditor!!.commit()
    }

    fun preferenceGetLong(key: String?, defaultValue: Long): Long {
        return applicationPreference!!.getLong(key, defaultValue)
    }

    fun preferencePutFloat(key: String?, value: Float) {
        applicationPreferenceEditor!!.putFloat(key, value)
        applicationPreferenceEditor!!.commit()
    }

    fun preferenceGetFloat(key: String?, defaultValue: Float): Float {
        return applicationPreference!!.getFloat(key, defaultValue)
    }

    fun hasPreferenceKey(key: String?): Boolean {
        return applicationPreference!!.contains(key)
    }


    /*remove Keys*/
    fun preferenceRemoveKey(key: String?) {
        applicationPreferenceEditor!!.remove(key)
        applicationPreferenceEditor!!.commit()
    }


    fun saveArrayList(list: ArrayList<MarkerModel>, key: String?) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        applicationPreferenceEditor?.putString(key, json)
        applicationPreferenceEditor?.apply()
    }

    fun getArrayList(key: String?): ArrayList<MarkerModel?>? {
        val gson = Gson()
        val json: String? = applicationPreference?.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<MarkerModel?>?>() {}.getType()
        return gson.fromJson(json, type)
    }


}