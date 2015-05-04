package de.hftl_projekt.ict.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import de.hftl_projekt.ict.R;

/**
 * provides some methods to handle SharedPrefs
 * @author Carsten
 */
public class SharedPrefsHandler {
	private static final String TAG = "SharedPrefsHandler";

    /** SharedPrefsHandler instance */
    private static SharedPrefsHandler mInstance = null;

	private SharedPreferences prefs;

    /**
     * returns an instance of the handler
     * @param pContext context to open new handler
     * @return instance
     */
    public static SharedPrefsHandler getInstance(Context pContext){
        Log.d(TAG, "get SharedPrefsHandler instance!");
        if(mInstance == null){
            mInstance = new SharedPrefsHandler(pContext);
        }
        return mInstance;
    }

    /**
     * constructor of this class
     * @param pContext context
     */
	private SharedPrefsHandler(Context pContext) {
        Log.d(TAG, "new SharedPrefsHandler");
		prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
		//load the default values
		PreferenceManager.setDefaultValues(pContext, R.xml.settings, false);
	}
	
	/**
	 * Empty the SharedPreference at given key
	 * @param key key of the preference to empty
	 */
	public void emptySharedPreference(String key) {
		try {
			SharedPreferences.Editor editor = prefs.edit(); //create editor
			editor.remove(key); //remove preference key
			editor.apply();
			Log.d(TAG, "removed key \""+key+"\" from preferences");
		} catch(Exception e) { 
			Log.e(TAG, "removing failed- " + e);
		}
	}
	/**
	 * Load a value of a given key from SharedPreferences
	 * @param	key key of SharedPreferences
	 * @return	String	value of key - with exception when an error appeared
	 */
	public String loadStringSettings(String key){ //load a string from sharedPreferences
		try {
			return prefs.getString(key, ""); //return nothing by default
		} catch (Exception e) {
			return "empty - " + e;
		}
	}
  	
  	/**
	 * Save a key with given string-value to SharedPreferences
	 * @param	key key of SharedPreferences
	 * @param	value value of SharedPreferences
	 */
	public void saveStringSettings(String key, String value) { //saves a String resource
		try {
			SharedPreferences.Editor editor = prefs.edit(); //create editor
			editor.putString(key, value); //put parameters
			editor.apply();
			Log.d(TAG, "saved \""+value+"\" in key \""+key+"\"");
		} catch(Exception e) { 
			Log.e(TAG, "saving failed- " + e);
		}
	}
	
	/**
	 * Load a value of a given key from SharedPreferences
	 * @param	key	key of SharedPreferences
	 * @return	int	value of key - with exception when an error appeared
	 */
	public int loadIntSettings(String key){ //load a string from sharedPreferences
		try {
			return prefs.getInt(key, 0); //return nothing by default
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Save a key with given string-value to SharedPreferences
	 * @param	key	key of SharedPreferences
	 * @param	value	value of SharedPreferences
	 */
	public void saveIntSettings(String key, int value) { //saves a String resource
		try {
			SharedPreferences.Editor editor = prefs.edit(); //create editor
			editor.putInt(key, value); //put parameters
			editor.apply();
			Log.d(TAG, "saved \""+value+"\" in key \""+key+"\"");
		} catch(Exception e) { 
			Log.e(TAG, "saving failed- " + e);
		}
	}
	
	/**
	 * Load a value of a given key from SharedPreferences
	 * @param	key	key of SharedPreferences
	 * @return	boolean	value of key - with exception when an error appeared
	 */
	public boolean loadBooleanSettings(String key) { //loads a boolean resource
		try {
			return prefs.getBoolean(key, false); //return false by default
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Save a key with given boolean-value to SharedPreferences
	 * @param	key	key of SharedPreferences
	 * @param	value value of SharedPreferences
	 */
	public void saveBooleanSettings(String key, boolean value){ //saves a boolean resource
		try {
			SharedPreferences.Editor editor = prefs.edit(); //create editor
			editor.putBoolean(key, value);  //put parameters
			editor.apply();
			Log.d(TAG, "saved \""+value+"\" in key \""+key+"\"");
		} catch(Exception e) {
			Log.e(TAG, "saving failed- " + e);
		}
	}
}