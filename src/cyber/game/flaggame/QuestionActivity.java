package cyber.game.flaggame;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cyber.game.flaggame.util.SystemUiHider;
import cyber.game.model.Question;
import cyber.game.model.SQLiteHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class QuestionActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	// exclude list of this question activity
	private static ArrayList<Integer> exclude = new ArrayList<Integer>();
	// GUI component of this question activity
	private Button choiceA;
	private Button choiceB;
	private Button choiceC;
	private Button choiceD;
	private TextView txtQuestion;
	private TextView txtAnswer;
	private TextView txtRemainingTime;
	private Button btnShowTrueAnswer;
	
	private SQLiteHelper databaseCon;

	// CountDownTimer
	private CountDownTimer countDownTimer;
	private static final long STARTTIME = 45 * 1000;
	private static final long INTERVAL = 1 * 1000;

	// Quest System
	Question quest;
	public static final int WRONG_CHOICE = 0;
	public static final int RUNOUT_OF_QUESTION = -1;
	private String difficult = "";
	private OnClickListener onClickChoice = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// convert choice into number for match with answerID
			int choiceNum=0;
			switch (v.getId()) {
			case R.id.btnChoiceA:
				choiceNum = 1;
				break;
			case R.id.btnChoiceB:
				choiceNum = 2;
				break;
			case R.id.btnChoiceC:
				choiceNum = 3;
				break;
			case R.id.btnChoiceD:
				choiceNum = 4;
				break;	
			}

			sendResultBack(choiceNum);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_question);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
		.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public void onVisibilityChange(boolean visible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = controlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(
								android.R.integer.config_shortAnimTime);
					}
					controlsView
					.animate()
					.translationY(visible ? 0 : mControlsHeight)
					.setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE
							: View.GONE);
				}

				if (visible && AUTO_HIDE) {
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// connect to database
		databaseCon = new SQLiteHelper(this);	

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		// interact with layout xml file
		txtQuestion = (TextView) findViewById(R.id.txtQuestion);
		txtAnswer = (TextView) findViewById(R.id.txtAnswer);
		txtRemainingTime = (TextView) findViewById(R.id.txtRemainingTime);
		choiceA = (Button) findViewById(R.id.btnChoiceA);
		choiceB = (Button) findViewById(R.id.btnChoiceB);
		choiceC = (Button) findViewById(R.id.btnChoiceC);
		choiceD = (Button) findViewById(R.id.btnChoiceD);
		btnShowTrueAnswer = (Button) findViewById(R.id.btnShowAnswer);
		btnShowTrueAnswer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showTrueAnswer();
			}
		});

		// get difficult from parent activity (InGame), get Question and show to UI
		difficult = getIntent().getExtras().getString(InGame.DIFFICULT);
		fillData();

		// add onClickListener for choiceButton
		choiceA.setOnClickListener(onClickChoice);
		choiceB.setOnClickListener(onClickChoice);
		choiceC.setOnClickListener(onClickChoice);
		choiceD.setOnClickListener(onClickChoice);

		countDownTimer = new MyCountDownTimer(STARTTIME, INTERVAL);
		countDownTimer.start();
		// continue here ... 
	}

	public static void initExcludeList() {
		exclude = new ArrayList<Integer>();
	}
	
	private void fillData() {
		// get question from database, and prevent this question appear again
		quest = databaseCon.getRandomQuestion(difficult, exclude);
		
		// test 
		System.out.println("qid = "+quest.getQid());
		
		if (quest.getQid()>Question.BLANK_QUESTION) {
			// prevent duplicate question
			exclude.add(quest.getQid());
			
			// show this question into UI
			txtQuestion.setText("Question:\n" + quest.getQuestion());
			String[] choices = quest.getChoiceList();
			choiceA.setText(choices[0]);
			choiceB.setText(choices[1]);
			choiceC.setText(choices[2]);
			choiceD.setText(choices[3]);
		} else {
			sendResultBack(RUNOUT_OF_QUESTION);
		}
	}

	private void showTrueAnswer() {
		txtAnswer.setText("" + quest.getAnswerID());
	}

	private void sendResultBack(int resultCode) {
		// transfer quest result to parent activity (InGame)
		Intent intent = new Intent();
		if (resultCode==WRONG_CHOICE) {
			intent.putExtra(InGame.ANSWER, false);
			intent.putExtra(InGame.QUEST, quest.getQid());
		}else if (resultCode==RUNOUT_OF_QUESTION){
			intent.putExtra(InGame.QUEST, RUNOUT_OF_QUESTION);
		}else {
			intent.putExtra(InGame.ANSWER, resultCode==quest.getAnswerID());
			intent.putExtra(InGame.QUEST, quest.getQid());
		}
		setResult(InGame.RESULT_CODE, intent);
		finish();
	}

	private void setRemainingTimeUI(long millisUntilFinished) {
		long secUntilFinished = millisUntilFinished/1000;
		long minute = secUntilFinished/60;
		long sec = secUntilFinished%60;
		
		txtRemainingTime.setTextColor(secUntilFinished<10?Color.RED:Color.WHITE);
		txtRemainingTime.setText(minute+" : "+sec);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onBackPressed() {
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialogWarning_Title)
		.setMessage(getString(R.string.dialogGiveUp_Msg))
		.setNegativeButton("Cancel", null)
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				sendResultBack(WRONG_CHOICE);
			}
		}).create();
		alertDialog.show();
	}

	@Override
	protected void onDestroy() {
		databaseCon.close();
		super.onDestroy();
	}

	// CountDownTimer for this Question
	public class MyCountDownTimer extends CountDownTimer {
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			txtRemainingTime.setText(getString(R.string.timeUpWarning));
			sendResultBack(WRONG_CHOICE);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			setRemainingTimeUI(millisUntilFinished);
		}
	}
}
