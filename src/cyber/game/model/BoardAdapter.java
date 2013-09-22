package cyber.game.model;



import cyber.game.flaggame.InGame;
import cyber.game.flaggame.R;
import cyber.game.model.Cell.CellState;

import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter.LengthFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

public class BoardAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private int mViewResId;
	private Board4x4 boardGame;
	private InGame mainAct;
	private boolean turn=true;
	private boolean gameover = false;
	private int moveCount=0;

	public BoardAdapter(InGame act, Context context, int viewResourceId,
			Board4x4 board) {
		super();
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainAct = act;
		mViewResId = viewResourceId;
		boardGame = board;

	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(mViewResId, null);

		final ImageButton iv = (ImageButton) convertView.findViewById(R.id.imgCell);
		final String difficult = boardGame.getCellDifficult(position/4, position%4);
		if (difficult.equals(SQLiteHelper.HARD)) {
			iv.setBackgroundColor(Color.DKGRAY);
		} else if (difficult.equals(SQLiteHelper.EASY)){
			iv.setBackgroundColor(Color.LTGRAY);
		} else {
			iv.setBackgroundColor(Color.GRAY);
		}
		
		switch (getItem(position).getWrongTime()) {
		case 2:
			iv.setBackgroundColor(Color.RED);
			break;
		case 1:
			iv.setBackgroundColor(Color.YELLOW);
			break;
		}
		
		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*int currentWT = getItem(position).getWrongTime();
				if (currentWT >= 2) {
					getItem(position).setWrongTime(0);
				}else {
					getItem(position).setWrongTime(currentWT+1);
				}
				notifyDataSetChanged();*/
				
				// check state of cell clicked and game-over flag
				Cell currentCell = getItem(position);
				if(currentCell.getState()==CellState.Blank && !gameover){
					CellState currentState = turn ? CellState.X : CellState.O;
					int symbolID = turn ? R.drawable.x : R.drawable.o;
					iv.setImageResource(symbolID);
					currentCell.setState(currentState);
					moveCount++;
					
					// game is draw when not exist cell can be click
					if(moveCount==Board4x4.DIMENSON*Board4x4.DIMENSON){
						gameover = true;
						mainAct.showDrawMessage();
					}
					
					// check who is winner or game will continue
					if(boardGame.checkForWin(position/4, position%4, currentState)){
						gameover = true;
						String winner  = 
								currentState==CellState.X? mainAct.getString(R.string.player1):mainAct.getString(R.string.player2);
						mainAct.showWinMessage(winner);
					}else{
						// change player turn
						turn=!turn;
						mainAct.updateUI(turn);
					}
					
					Question testQuestion = new SQLiteHelper(mainAct).getRandomQuestion(difficult, null);
					
					Toast.makeText(mainAct, testQuestion.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});

		return convertView;
	}

	public void updateAdapter() {
		// this function will be build later
	}
	
	@Override
	public int getCount() {
		return 16;
	}

	@Override
	public Cell getItem(int position) {
		return boardGame.getCell(position/4, position%4);
	}

	
	@Override
	public long getItemId(int position) {
		return 0;
	}


}
