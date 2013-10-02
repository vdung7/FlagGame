package cyber.game.flaggame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.text.BoringLayout;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import cyber.game.flaggame.util.SystemUiHider;
import cyber.game.model.Board4x4;
import cyber.game.model.BoardAdapter;
import cyber.game.model.Cell;
import cyber.game.model.Cell.CellState;
import cyber.game.model.Question;
import cyber.game.model.SQLiteHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class InGame extends Activity {
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

	public static final String DIFFICULT = "difficult";
	public static final String ANSWER = "receive";
	public static final int REQUEST_CODE = 1;
	public static final int RESULT_CODE = 11;

	private GridView boardGameView;
	private Board4x4 boardGame;
	private BoardAdapter boardAdapter;
	private boolean turn = true;
	private boolean gameover = false;
	private int moveCount = 0;
	private int player1score = 0;
	private int player2score = 0;
	
	private ImageView ivTurn;
	private TextView txtTurn;
	private TextView txtScore1;
	private TextView txtScore2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_in_game);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		ivTurn = (ImageView) findViewById(R.id.ivTurn);
		txtTurn = (TextView) findViewById(R.id.txtTurn);
		
		// Set up a 4x4 Board Game
		boardGame = new Board4x4();
		boardAdapter = new BoardAdapter(this, getApplicationContext(), R.layout.grid_cell, boardGame);
		boardGameView = (GridView)findViewById(R.id.boardGame);
		boardGameView.setAdapter(boardAdapter);

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

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		// interact with UI
		txtScore1 = (TextView) findViewById(R.id.txtScore1);
		txtScore2 = (TextView) findViewById(R.id.txtScore2);
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
	
	public void showWinMessage(String winner) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.winMessageDialogTitle)
		.setMessage(winner+" "+getString(R.string.winMessage))
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				InGame.this.finish();
			}
		}).create();
		alertDialog.show();
	}

	public void showScoringMessage() {
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialogScoringTitle)
		.setMessage(R.string.player1+" : "+player1score+"\n"+
					R.string.player2+" : "+player2score)
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (player1score==player2score) {
					showDrawMessage();
				} else {
					String winner = 
							player1score>player2score? getString(R.string.player1):getString(R.string.player2);
					showWinMessage(winner);
				}
			}
		}).create();
		alertDialog.show();
	}

	public void showDrawMessage() {
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.winMessageDialogTitle)
		.setMessage(getString(R.string.drawMessage))
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				InGame.this.finish();
			}
		}).create();
		alertDialog.show();
	}

	public void showWarningDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.dialogWarning_Title)
		.setMessage(getString(R.string.dialogWrongtimeWarning_Msg))
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// nothing here ^_^
			}
		}).create();
		alertDialog.show();
	}

	public boolean isGameOver(){
		return gameover;
	}

	public void updateGameState(boolean answer) {
		String currentPlayer = "";
		int currentPosition = boardAdapter.getCurrentPosition();
		Cell currentCell = boardAdapter.getItem(currentPosition);
		
		if(answer==true){
			// set flag symbol for current cell
			CellState newState = turn ? CellState.X : CellState.O;
			currentCell.setState(newState);
			moveCount++;
			currentCell.setWrongTime(0);
			
			// get Cell Score
			String diffcult = 
					boardGame.getCellDifficult(currentPosition/4, currentPosition%4);
			int cellScore;
			if (diffcult.equals(SQLiteHelper.EASY)) {
				cellScore = Question.EASY_SCORE;
			} else if(diffcult.equals(SQLiteHelper.MEDIUM)) {
				cellScore = Question.MEDIUM_SCORE;
			} else {
				cellScore = Question.HARD_SCORE;
			}
			
			// update score (add)
			if (turn) {
				player1score+=cellScore;
			}else {
				player2score+=cellScore;
			}
			
			// game is scoring when not exist cell can be click
			if(boardGame.checkNoMoreMoveState(moveCount)){
				gameover = true;
				showScoringMessage();
			}

			// check who is winner or game will be continue
			if(boardGame.checkForWin(currentPosition/4, currentPosition%4, newState)){
				gameover = true;
				String winner = 
						newState==CellState.X? getString(R.string.player1):getString(R.string.player2);
						showWinMessage(winner);
			}
		}else{
			// update score (sub)
			if (turn) {
				player1score--;
			}else {
				player2score--;
			}
			
			// update cell state
			currentCell.riseWrongTime();
			if(currentCell.getWrongTime()>=2)
				boardGame.setHaveAlockedCell(true);
		}

		// update UI (adapter)
		boardAdapter.notifyDataSetChanged();
		
		// update Score field
		txtScore1.setText(player1score+"p");
		txtScore2.setText(player2score+"p");
		
		// switch turn & update turn view
		turn = !turn;
		int symbolID = turn ? R.drawable.x : R.drawable.o;
		if(turn){
			currentPlayer = getString(R.string.player1);
		}
		else{
			currentPlayer = getString(R.string.player2);
		}
		txtTurn.setText(currentPlayer+" Turn");
		ivTurn.setImageResource(symbolID);

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE && resultCode == RESULT_CODE){
			// update UI and switch turn
			updateGameState(data.getBooleanExtra(ANSWER, false));
		}
	}
	
	@Override
	public void onBackPressed() {
		showScoringMessage();
	}
	
}
