package co.adrianblan.fastbrush.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Class which manages settings.
 */
public class SettingsManager {
    private static String SETTINGS_DATA_KEY = "settingsData";
    private static SettingsManager instance;

    private SettingsData settingsData;
    private Gson gson;
    private SharedPreferences sharedPreferences;

    private boolean hasChanges;

    public static SettingsManager getInstance(Context context) {
        if(instance == null){
            instance = new SettingsManager(context);
        }

        return instance;
    }

    public static boolean hasInstance(){
        return (instance != null);
    }

    private SettingsManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
        loadSettingsData();
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public void saveSettingsData(SettingsData settingsData){

        this.settingsData = settingsData;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SETTINGS_DATA_KEY, gson.toJson(settingsData));
        editor.apply();

        hasChanges = true;
    }

    private void loadSettingsData() {
        String settingsDataString = sharedPreferences.getString(SETTINGS_DATA_KEY, null);

        if(settingsDataString == null) {
            System.out.println("No settings data, creating new");
            settingsData = new SettingsData();
        } else {
            settingsData = gson.fromJson(settingsDataString, SettingsData.class);
        }
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    public void setChangesRead() {
        hasChanges = false;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
