package com.moosesoft.asgbomb;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class BombActivity extends Activity {

	private static final String BOMB_PLANTED_TIME = "plant_time";
	private static final String GAME_START_TIME = "game_start_time";
	private static final long[] SLOW_VIBRATION_SCHEME = new long[]{0, 100, 700};
	private static final long[] FAST_VIBRATION_SCHEME = new long[]{0, 100, 233, 100, 233, 100, 233};
	
	private SensorManager sensorManager;
		
	private long bombPlantedTime = 0;
	private long gameStartTime = 0;
	private boolean gameFinished = false;
	
	private long lastTimeToEndGame = 0;
	private long lastTimeToExplode = 0;
	private ASGBombPreferences preferences;
	private MediaPlayer mediaPlayer;
	private Thread timerThread;
	
	private Dialog keyboardDialog = null;
	private boolean keyboardSetPosition = true;
	private float[] keyboardStartPosition = new float[3];
	private float keyboardStartPositionLength;
	
	private TextView gameStatus;
	private TextView bombStatus;
	private Button plantDefuseButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_bomb);
		this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.sensorManager.registerListener(new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
					
					if(keyboardSetPosition) {
						keyboardStartPosition[0] = event.values[0];
						keyboardStartPosition[1] = event.values[1];
						keyboardStartPosition[2] = event.values[2];
						keyboardStartPositionLength = vectorLength(keyboardStartPosition);
						keyboardSetPosition = false;
					}
					if(keyboardDialog != null && keyboardDialog.isShowing()) {
						float sensitivity = preferences.getSensitivity();
						if(Math.abs(keyboardStartPositionLength - 9.83f) > sensitivity*10) {
							closeKeyboardDialog();
							return;
						}
						
						float currentLen = vectorLength(event.values);

						if(Math.abs(currentLen - 9.83f) > sensitivity*10) {
							closeKeyboardDialog();
							return;
						}
						
						float angle = 	keyboardStartPosition[0]*event.values[0] + 
										keyboardStartPosition[1]*event.values[1] + 
										keyboardStartPosition[2]*event.values[2];
						angle /= keyboardStartPositionLength * currentLen;
						Log.d("Bomb", "Acc pos: " + event.values[0] + "; "+ event.values[1] + "; " + event.values[2]);
						Log.d("Bomb", "Acc angle: " + angle + " lenStart: " + keyboardStartPositionLength + " lenCur: " + currentLen);
						
						if(1.0f - angle > sensitivity) {
							closeKeyboardDialog();
							return;
						}
					}
				}
			}

			private float vectorLength(float[] values) {
				return (float)Math.sqrt( values[0]*values[0] +
										 values[1]*values[1] +
									 	 values[2]*values[2]);
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		}, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		
		this.preferences = new ASGBombPreferences(this);
		this.mediaPlayer = new MediaPlayer();
	    this.mediaPlayer.setLooping(false);

	    this.gameStatus = (TextView)findViewById(R.id.bomb_game_status);
	    this.bombStatus = (TextView)findViewById(R.id.bomb_bomb_status);
		
		if( savedInstanceState != null) {
			if(savedInstanceState.containsKey(BOMB_PLANTED_TIME))
				bombPlantedTime = savedInstanceState.getLong(BOMB_PLANTED_TIME);
			
			if(savedInstanceState.containsKey(GAME_START_TIME)) 
				gameStartTime = savedInstanceState.getLong(GAME_START_TIME);
		}
		
		if(gameStartTime != 0) {
			startGame();
		}
		
		this.plantDefuseButton = (Button)findViewById(R.id.bomb_button_bomb_set_defuse);
		this.plantDefuseButton.setText("Start game!");
		this.plantDefuseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isGameOngoing())
					startGame();	
				else
					showBombKeyboard();
			}
		});
		((Button)findViewById(R.id.bomb_button_end_game)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showShouldEndGameDialog();
			}
		});
	}
	
	private void showShouldEndGameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(BombActivity.this);
	    builder
		    .setTitle("End game")
		    .setMessage("Are you sure you want to end game now?")
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        {
		        	cancelVibration();
		        	gameStartTime = 0;
		        	bombPlantedTime = 0;
		        	gameFinished = true;
		        	finish();
		        	dialog.dismiss();
		        }
		    });  
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        {   
		        	dialog.dismiss();
		        }
		    });         
	    AlertDialog alert = builder.create();
        alert.show();
	}

	private void startGame() {
		gameFinished = false;
		plantDefuseButton.setText("Plant bomb");
		bombStatus.setText("");
		gameStartTime = System.currentTimeMillis();
		timerThread = new Thread() {
		    public void run () {
		        while (isGameOngoing()) {
		            try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            uiCallback.sendEmptyMessage(0);
		        }
		    }
		};
		this.timerThread.start();
	}
	
	private Handler uiCallback = new Handler () {
	    public void handleMessage (Message msg) {
	        updateBombExplosionTime();
	        updateGameTime();
	    }
	};
	
	private void updateBombExplosionTime() {
		if(bombPlantedTime != 0){
	    	long currentTime = System.currentTimeMillis();
        	long explosionTime = bombPlantedTime + preferences.getBombDuration();
        	if(explosionTime < currentTime) {
        		bombExplodes();
        	}
        	else {
        		long timeToExplode = explosionTime - currentTime;
        		SimpleDateFormat format = new SimpleDateFormat(":mm:ss.SSS");
        		bombStatus.setText(timeToExplode/(3600*1000) + format.format(new Date(timeToExplode)));
				
				if(timeToExplode < 10*1000 && lastTimeToExplode/500 > timeToExplode/500) {
					playSound("time_short.mp3");
				}
				else if(timeToExplode < 60*1000 && lastTimeToExplode/1000 > timeToExplode/1000) {
					playSound("time_short.mp3");
				}
				else if(lastTimeToExplode/1000 > timeToExplode/1000) {
					playSound("time_long.mp3");
				}
				lastTimeToExplode = timeToExplode;
        	}
        }
	}
	
	private void updateGameTime() {
		if(gameStartTime != 0) {
			long currentTime = System.currentTimeMillis();
			long timeToEndGame = gameStartTime + preferences.getGameDuration() - currentTime;
			if(timeToEndGame <= 0)
				gameTimeEnd();
			else {
				SimpleDateFormat format = new SimpleDateFormat(":mm:ss.SSS");
				gameStatus.setText(timeToEndGame/(3600*1000) + format.format(new Date(timeToEndGame)));
								
				if(timeToEndGame < 30*1000 && lastTimeToEndGame/1000 > timeToEndGame/1000) {
					vibrate(FAST_VIBRATION_SCHEME);
				}
				else if(timeToEndGame < 120*1000 && lastTimeToEndGame/1000 > timeToEndGame/1000) {
					vibrate(SLOW_VIBRATION_SCHEME);
				}
				lastTimeToEndGame = timeToEndGame;
			}    		
		}
	}

	private void playSound(String sound) {
		try {
			AssetFileDescriptor descriptor = getAssets().openFd(sound);
			mediaPlayer.reset();
		    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
		    descriptor.close();
		    
		    mediaPlayer.prepare();
		    mediaPlayer.start();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	private void vibrate(final long[] vibrationScheme) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		//if(vibrator.hasVibrator()) {
			vibrator.cancel();
			vibrator.vibrate(vibrationScheme, 1);
		//}
	}
	
	private void cancelVibration() {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.cancel();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bombPlantedTime != 0) {
			outState.putLong(BOMB_PLANTED_TIME, bombPlantedTime);
		}
		if(gameStartTime != 0) {
			outState.putLong(GAME_START_TIME, gameStartTime);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("Bomb", "OnDestroy");
		gameStartTime = 0;
		bombPlantedTime = 0;
		if(timerThread != null)
			try {
				timerThread.join();
			} catch (InterruptedException e) {
			}
	}
	
	@Override
	public void onBackPressed() {
		if(isGameFinished())
			finish();
	}
	
	class KeyboardKeyListener implements OnClickListener {

		private char key;
		private TextView statusView;
		
		public KeyboardKeyListener(char key, TextView statusView) {
			this.key = key;
			this.statusView = statusView;
		}
		
		@Override
		public void onClick(View v) {
			playSound("bomb_key.mp3");
			String currentText = statusView.getText().toString();
			currentText += key;
			statusView.setText(currentText);
		}		
	}
	
	private void showBombKeyboard() {
		keyboardSetPosition = true;
		keyboardDialog = new Dialog(this);
		keyboardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		keyboardDialog.setContentView(R.layout.activity_bomb_planting);
		
		final TextView bombKeyboardStatus = (TextView)keyboardDialog.findViewById(R.id.bomb_planting_display);
		
		Button key0 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_0);
		Button key1 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_1);
		Button key2 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_2);
		Button key3 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_3);
		Button key4 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_4);
		Button key5 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_5);
		Button key6 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_6);
		Button key7 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_7);
		Button key8 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_8);
		Button key9 = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_9);
		Button keyStar = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_star);
		Button keyHash = (Button)keyboardDialog.findViewById(R.id.bomb_planting_button_hash);

		key0.setOnClickListener(new KeyboardKeyListener('0', bombKeyboardStatus));
		key1.setOnClickListener(new KeyboardKeyListener('1', bombKeyboardStatus));
		key2.setOnClickListener(new KeyboardKeyListener('2', bombKeyboardStatus));
		key3.setOnClickListener(new KeyboardKeyListener('3', bombKeyboardStatus));
		key4.setOnClickListener(new KeyboardKeyListener('4', bombKeyboardStatus));
		key5.setOnClickListener(new KeyboardKeyListener('5', bombKeyboardStatus));
		key6.setOnClickListener(new KeyboardKeyListener('6', bombKeyboardStatus));
		key7.setOnClickListener(new KeyboardKeyListener('7', bombKeyboardStatus));
		key8.setOnClickListener(new KeyboardKeyListener('8', bombKeyboardStatus));
		key9.setOnClickListener(new KeyboardKeyListener('9', bombKeyboardStatus));
		
		keyStar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playSound("bomb_key.mp3");
				String statusText = bombKeyboardStatus.getText().toString();
				if(statusText.length() > 0) {
					statusText = statusText.substring(0, statusText.length() - 1);
					bombKeyboardStatus.setText(statusText);
				}
				else {
					closeKeyboardDialog();
				}
			}
		});
		keyHash.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBombCombination(bombKeyboardStatus.getText().toString());
			}
		});
		
		keyboardDialog.show();
	}

	private void closeKeyboardDialog() {
		if(keyboardDialog != null && keyboardDialog.isShowing()) {
			keyboardDialog.dismiss();
			keyboardDialog = null;
			keyboardSetPosition = false;
		}
	}
	
	
	private void checkBombCombination(String combination) {
		BombCodeChecker checker = new BombCodeChecker(preferences.getGameCode(), preferences.getCodesCount());
		if(isBombPlanted()) {
			if(checker.checkDefuseCode(combination)) {
				bombHasBeenDefused();
				closeKeyboardDialog();
			}
			else
				playSound("bomb_wrong.mp3");
		}
		else if(isGameOngoing()) {
			if(checker.checkBombCode(combination)) {
				bombHasBeenPlanted();
				closeKeyboardDialog();
			}
			else
				playSound("bomb_wrong.mp3");
		}
	}

	
	private void bombHasBeenPlanted() {
		plantDefuseButton.setText("Defuse bomb");
		bombPlantedTime = System.currentTimeMillis();
		gameStartTime = 0;
    	playSound("planted.mp3");
    	cancelVibration();
	}
	
	private void bombHasBeenDefused() {

		plantDefuseButton.setText("Start game");
		bombPlantedTime = 0;
		gameStartTime = 0;
    	gameFinished = true;
    	gameStatus.setText("Counter terrorists win!");
    	bombStatus.setText("Defused!");
    	
    	playSound("defused.mp3");
    	cancelVibration();
    	Thread thread = new Thread() {
    		public void run() {
				try {
					Thread.sleep(4*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
		    	    	playSound("counters_win.mp3");					
					}
				});
    		}
    	};
    	thread.start();
	}
	
	private void bombExplodes() {
		plantDefuseButton.setText("Start game");
    	playSound("explosion.mp3");
    	cancelVibration();
    	Thread thread = new Thread() {
    		public void run() {
				try {
					Thread.sleep(4*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
		    	    	playSound("terrorists_win.mp3");					
					}
				});
    		}
    	};
    	thread.start();
    	
    	gameStatus.setText("Terrorists win!");
    	bombPlantedTime = 0;
    	gameFinished = true;
    	bombStatus.setText("Detonated!");		
	}
	
	private void gameTimeEnd() {
		plantDefuseButton.setText("Start game");
    	playSound("counters_win.mp3");
    	cancelVibration();
    	gameStartTime = 0;
    	gameFinished = true;
    	gameStatus.setText("Counter terrorists win!");
    	closeKeyboardDialog();
	}
	
	private boolean isGameFinished() {
		return gameFinished;
	}

	private boolean isGameOngoing() {
		return (gameStartTime != 0 || bombPlantedTime != 0);
	}

	private boolean isBombPlanted() {
		return bombPlantedTime != 0;
	}
	
	
}
