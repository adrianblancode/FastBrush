package co.adrianblan.fastbrush.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Class which manages settings.
 */
public class SettingsManager {
    private static String SETTINGS_NAME = "settings";
    private static String PREFS_KEY = "settingsData";
    private static SettingsManager instance;

    private Gson gson;
    private SharedPreferences sharedPreferences;
    private SettingsData settingsData;

    public static SettingsManager createInstance(Context context) {
        if(instance == null){
            instance = new SettingsManager(context);
        }

        return instance;
    }

    public static SettingsManager getInstance(Context context) {
        return instance;
    }

    public static boolean hasInstance(){
        return (instance != null);
    }

    private SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        load();
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public void save(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_KEY, gson.toJson(settingsData));
        editor.commit();
    }

    private void load() {
        String settingsDataString = sharedPreferences.getString(PREFS_KEY, null);

        if(settingsDataString == null) {
            settingsData = new SettingsData();
        } else {
            settingsData = gson.fromJson(settingsDataString, SettingsData.class);
        }
    }
}
