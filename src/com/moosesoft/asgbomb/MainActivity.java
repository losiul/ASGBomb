package com.moosesoft.asgbomb;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableRow;

public class MainActivity extends Activity {

	private static int MAX_BOMB_SITES = 5;
	
	private EditText gameCodeText;
	private EditText[] bombCodeTexts;
	private TableRow[] bombCodeRows;
	private EditText bombDefuseText;
	private ASGBombPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

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
		this.preferences.createDefaultSettingsIfNecessary();
		
		this.bombCodeTexts = new EditText[MAX_BOMB_SITES];
		this.bombCodeTexts[0] = (EditText)findViewById(R.id.main_bomb_code1);
		this.bombCodeTexts[1] = (EditText)findViewById(R.id.main_bomb_code2);
		this.bombCodeTexts[2] = (EditText)findViewById(R.id.main_bomb_code3);
		this.bombCodeTexts[3] = (EditText)findViewById(R.id.main_bomb_code4);
		this.bombCodeTexts[4] = (EditText)findViewById(R.id.main_bomb_code5);
		this.bombCodeRows = new TableRow[MAX_BOMB_SITES];
		this.bombCodeRows[0] = (TableRow)findViewById(R.id.main_table_row_1);
		this.bombCodeRows[1] = (TableRow)findViewById(R.id.main_table_row_2);
		this.bombCodeRows[2] = (TableRow)findViewById(R.id.main_table_row_3);
		this.bombCodeRows[3] = (TableRow)findViewById(R.id.main_table_row_4);
		this.bombCodeRows[4] = (TableRow)findViewById(R.id.main_table_row_5);
		
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
				BombCodeChecker checker = new BombCodeChecker(gameCode, preferences.getCodesCount());
				for(int i = 0; i < preferences.getCodesCount(); ++i)
					bombCodeTexts[i].setText(checker.getBombCode(i));
				bombDefuseText.setText(checker.getDefuseCode());
				preferences.setGameCode(gameCode);
			}
		});		

//		String gameCode = "gra ";
//		for(int i = 0; i < 20; ++i) {
//			String currentGameCode = gameCode + (i + 1);
//			BombCodeChecker checker = new BombCodeChecker(currentGameCode, preferences.getCodesCount());
//			String codes = "";
//			for(int j = 0; j < preferences.getCodesCount(); ++j)
//				codes += "Bombsite " + (j + 1) + ":\t" + checker.getBombCode(j) + "\t";
//			Log.d("Main", "Nazwa gry:\t" + currentGameCode + codes + "Defuse:\t" + checker.getDefuseCode());
//		}
		
		//debug code:
		//preferences.updateSettings(60*1000, 15*1000, 0.8f, 2);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		for(int i = 0; i < preferences.getCodesCount(); ++i) {
			bombCodeRows[i].setVisibility(View.VISIBLE);
		}
		for(int i = preferences.getCodesCount(); i < MAX_BOMB_SITES; ++i) {
			bombCodeRows[i].setVisibility(View.GONE);			
		}
		this.gameCodeText.setText(preferences.getGameCode());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
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
