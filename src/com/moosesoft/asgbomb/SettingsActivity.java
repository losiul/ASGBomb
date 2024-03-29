package com.moosesoft.asgbomb;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;

public class SettingsActivity extends Activity {
	
	private TimePicker gameDurationPicker;
	private TimePicker bombTimerPicker;
	private EditText sensitivityText;
	private TextView bombsitesCountTextView;
	private SeekBar bombsitesCountSeekBar;
	private ASGBombPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);

		this.preferences = new ASGBombPreferences(this);
		
		this.bombsitesCountTextView = (TextView)findViewById(R.id.settings_bombsites_count_value);
		this.bombsitesCountSeekBar = (SeekBar)findViewById(R.id.settings_bombsites_count_seekbar);
		this.bombsitesCountSeekBar.setMax(4);
		this.bombsitesCountSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				bombsitesCountTextView.setText(String.valueOf(progress + 1));
			}
		});
		this.bombsitesCountSeekBar.setProgress(preferences.getCodesCount() - 1);

		long gameDuration = preferences.getGameDuration();
		long bombDuration = preferences.getBombDuration();
		this.gameDurationPicker = (TimePicker) findViewById(R.id.settings_game_duration_picker);
		this.gameDurationPicker.setIs24HourView(true);
		this.gameDurationPicker.setCurrentHour((int)(gameDuration/1000/3600));
		this.gameDurationPicker.setCurrentMinute((int)(gameDuration/1000/60%60));
		
		this.bombTimerPicker = (TimePicker) findViewById(R.id.settings_bomb_timer_picker);
		this.bombTimerPicker.setIs24HourView(true);
		this.bombTimerPicker.setCurrentHour((int)(bombDuration/1000/3600));
		this.bombTimerPicker.setCurrentMinute((int)(bombDuration/1000/60%60));
		
		this.sensitivityText = (EditText) findViewById(R.id.settings_sensitivity);
		this.sensitivityText.setText(String.valueOf(preferences.getSensitivity()));
		
		findViewById(R.id.settings_save_settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long minute = 60*1000;
				long bombTime = bombTimerPicker.getCurrentHour()*60*minute + bombTimerPicker.getCurrentMinute()*minute;
				long gameTime = gameDurationPicker.getCurrentHour()*60*minute + gameDurationPicker.getCurrentMinute()*minute;
				float sensitivity = Float.parseFloat(sensitivityText.getText().toString());
				int bombsitesCount = bombsitesCountSeekBar.getProgress() + 1;
				preferences.updateSettings(gameTime, bombTime, sensitivity, bombsitesCount);
				finish();
			}
		});
	}
}
