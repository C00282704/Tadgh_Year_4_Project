package com.example.TheiaNewsAggregator;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class SettingsManager {

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
    }

    public void saveSetting(Set<String> values) {
        prefs.edit().putStringSet("my_setting", values).apply();
    }

    public Set<String> getSetting() {
        return prefs.getStringSet("my_setting", new HashSet<>());
    }
}