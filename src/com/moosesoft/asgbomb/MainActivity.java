package com.moosesoft.asgbomb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	private EditText gameCodeText;
	private EditText bombCode1Text;
	private EditText bombCode2Text;
	private EditText bombDefuseText;
	private ASGBombPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		String gameCode = "gra";
//		for(int i = 0; i < 20; ++i) {
//			String currentGameCode = gameCode + (i + 1);
//			BombCodeChecker checker = new BombCodeChecker(currentGameCode);
//			Log.d("Main", "Kod gry:\t" + currentGameCode + " BS1:\t" + checker.getBombCode1() + 
//					" BS2:\t" + checker.getBombCode2() + " Def:\t" + checker.getDefuseCode());
//		}

		setContentView(R.layout.activity_main);
		
		findViewById(R.id.main_button_settings).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleSettingsButton();
			}
		});
		
		findViewById(R.id.main_button_start).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleStartButton();			
			}
		});
		
		this.preferences = new ASGBombPreferences(this);
		this.bombCode1Text = (EditText)findViewById(R.id.main_bomb_code1);
		this.bombCode2Text = (EditText)findViewById(R.id.main_bomb_code2);
		this.bombDefuseText = (EditText)findViewById(R.id.main_defuse_code);
		this.gameCodeText = (EditText)findViewById(R.id.main_game_code);
		this.gameCodeText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String gameCode = s.toString();
				BombCodeChecker checker = new BombCodeChecker(gameCode);
				bombCode1Text.setText(checker.getBombCode1());
				bombCode2Text.setText(checker.getBombCode2());
				bombDefuseText.setText(checker.getDefuseCode());
				preferences.setGameCode(gameCode);
			}
		});
		this.gameCodeText.setText(preferences.getGameCode());
				
		ASGBombPreferences preferences = new ASGBombPreferences(this);
		preferences.createDefaultSettingsIfNecessary();
		//debug code:
		//preferences.updateSettings(60*1000, 15*1000, 0.01f);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		preferences.setGameCode(gameCodeText.getText().toString());		
	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void handleSettingsButton() {		
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	private void handleStartButton() {		
		Intent intent = new Intent(this, BombActivity.class);
		startActivity(intent);
	}
	

}
