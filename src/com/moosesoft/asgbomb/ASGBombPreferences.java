package com.moosesoft.asgbomb;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ASGBombPreferences {
	
	private static final String PREFERENCES_FILE = "preferences";
	private static final String GAME_DURATION = "game_duration";
	private static final String BOMB_DURATION = "bomb_duration";
	private static final String GAME_CODE = "game_code";
	private static final String SENSITIVITY = "sensitivity";

	private static final long DEFAULT_GAME_DURATION = 10*60*1000;
	private static final long DEFAULT_BOMB_DURATION = 10*60*1000;
	private static final String DEFAULT_GAME_CODE = "Defuse 1";
	private static final float DEFAULT_SENSITIVITY = 0.1f;
	
	private Context context;
	
	public ASGBombPreferences(Context context) {
		this.context = context;
	}
	
	public void createDefaultSettingsIfNecessary() {
		SharedPreferences preferences = getPreferences();

		if(!preferences.contains(GAME_DURATION)) {
			
			SharedPreferences.Editor editor = preferences.edit();
			editor.putLong(GAME_DURATION, DEFAULT_GAME_DURATION);
			editor.putLong(BOMB_DURATION, DEFAULT_BOMB_DURATION);
			editor.putString(GAME_CODE, DEFAULT_GAME_CODE);
			editor.putFloat(SENSITIVITY, DEFAULT_SENSITIVITY);
			
			editor.commit();
		}

	}
	
	/**
	 * @return game duration in milliseconds
	 */
	public long getGameDuration() {
		return getLong(GAME_DURATION, DEFAULT_GAME_DURATION);
	}

	/**
	 * @return game duration in milliseconds
	 */	
	public long getBombDuration() {
			return getLong(BOMB_DURATION, DEFAULT_BOMB_DURATION);
	}

	private long getLong(String key, long defaultValue) {
		return getPreferences().getLong(key, defaultValue);
	}

	private SharedPreferences getPreferences() {
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
		return preferences;
	}
	
	public String getGameCode() {
		return getPreferences().getString(GAME_CODE, DEFAULT_GAME_CODE);
	}
	
	public float getSensitivity() {
		return getPreferences().getFloat(SENSITIVITY, DEFAULT_SENSITIVITY);		
	}
	
	public void setGameCode(String newGameCode) {
		SharedPreferences preferences = getPreferences();
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString(GAME_CODE, newGameCode);
		
		editor.apply();		
	}
	
	public void updateSettings(long gameDuration, long bombDuration, float sensitivity) {
		SharedPreferences preferences = getPreferences();

		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putLong(GAME_DURATION, gameDuration);
		editor.putLong(BOMB_DURATION, bombDuration);
		editor.putFloat(SENSITIVITY, sensitivity);
		
		editor.apply();
	}
}
