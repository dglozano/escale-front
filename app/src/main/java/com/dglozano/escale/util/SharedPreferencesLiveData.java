package com.dglozano.escale.util;


import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;

import java.util.Set;

public abstract class SharedPreferencesLiveData<T> extends LiveData<T> {

    SharedPreferences sharedPrefs;
    String key;
    T defValue;

    public SharedPreferencesLiveData(SharedPreferences prefs, String key, T defValue) {
        this.sharedPrefs = prefs;
        this.key = key;
        this.defValue = defValue;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (SharedPreferencesLiveData.this.key.equals(key)) {
                setValue(getValueFromPreferences(key, defValue));
            }
        }
    };

    abstract T getValueFromPreferences(String key, T defValue);

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getValueFromPreferences(key, defValue));
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onInactive();
    }


    public static class SharedPreferenceBooleanLiveData extends SharedPreferencesLiveData<Boolean> {

        public SharedPreferenceBooleanLiveData(SharedPreferences prefs, String key, Boolean defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Boolean getValueFromPreferences(String key, Boolean defValue) {
            return sharedPrefs.getBoolean(key, defValue);
        }
    }

    public static class SharedPreferenceIntLiveData extends SharedPreferencesLiveData<Integer> {

        public SharedPreferenceIntLiveData(SharedPreferences prefs, String key, Integer defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Integer getValueFromPreferences(String key, Integer defValue) {
            return sharedPrefs.getInt(key, defValue);
        }
    }

    public static class SharedPreferenceStringLiveData extends SharedPreferencesLiveData<String> {

        public SharedPreferenceStringLiveData(SharedPreferences prefs, String key, String defValue) {
            super(prefs, key, defValue);
        }

        @Override
        String getValueFromPreferences(String key, String defValue) {
            return sharedPrefs.getString(key, defValue);
        }
    }

    public class SharedPreferenceFloatLiveData extends SharedPreferencesLiveData<Float> {

        public SharedPreferenceFloatLiveData(SharedPreferences prefs, String key, Float defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Float getValueFromPreferences(String key, Float defValue) {
            return sharedPrefs.getFloat(key, defValue);
        }
    }

    public static class SharedPreferenceLongLiveData extends SharedPreferencesLiveData<Long> {

        public SharedPreferenceLongLiveData(SharedPreferences prefs, String key, Long defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Long getValueFromPreferences(String key, Long defValue) {
            return sharedPrefs.getLong(key, defValue);
        }
    }

    public class SharedPreferenceStringSetLiveData extends SharedPreferencesLiveData<Set<String>> {

        public SharedPreferenceStringSetLiveData(SharedPreferences prefs, String key, Set<String> defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Set<String> getValueFromPreferences(String key, Set<String> defValue) {
            return sharedPrefs.getStringSet(key, defValue);
        }
    }
}
