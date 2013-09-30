package cyber.game.model;

import cyber.game.flaggame.InGame;
import cyber.game.flaggame.QuestionActivity;
import cyber.game.flaggame.R;
import cyber.game.model.Cell.CellState;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

public class BoardAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private int mViewResId;
	private Board4x4 boardGame;
	private InGame mainAct;
	private int currentPosition;

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

		// get current cell state
		final String difficult = boardGame.getCellDifficult(position/4, position%4);
		final Cell cell = getItem(position);

		// check & show difficult rank (color) of current cell
		if (difficult.equals(SQLiteHelper.HARD)) {
			iv.setBackgroundColor(Color.DKGRAY);
		} else if (difficult.equals(SQLiteHelper.EASY)){
			iv.setBackgroundColor(Color.LTGRAY);
		} else {
			iv.setBackgroundColor(Color.GRAY);
		}

		// check & show wrong time (color) of current cell
		switch (cell.getWrongTime()) {
		case 2:
			iv.setBackgroundColor(Color.RED);
			break;
		case 1:
			iv.setBackgroundColor(Color.YELLOW);
			break;
		}

		// check & show flag state of current cell
		switch (cell.getState()) {
		case X:
			iv.setImageResource(R.drawable.x);
			break;
		case O:
			iv.setImageResource(R.drawable.o);
			break;
		}

		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				currentPosition = position;
				Cell currentCell = getItem(currentPosition);
				if (currentCell.getState()==CellState.Blank && !mainAct.isGameOver()) {
					if (currentCell.getWrongTime() >= 2) {
						mainAct.showWarningDialog();
					} else {
						clearLockedCell();
						// start Activity for player solve question
						Intent intent = new Intent(mainAct,
								QuestionActivity.class);
						intent.putExtra(InGame.DIFFICULT, difficult);
						mainAct.startActivityForResult(intent,
								InGame.REQUEST_CODE);
					}
				}
			}

			private void clearLockedCell() {
				for (int i = 0; i < getCount(); i++) {
					Cell cell = getItem(i);
					if(cell.getWrongTime()>=2)
						getItem(i).setWrongTime(0);
				}
				boardGame.setHaveAlockedCell(false);
			}
		});

		return convertView;
	}

	public void updateAdapter() {
		// this function will be build later
	}

	@Override
	public int getCount() {
		return Board4x4.DIMENSON*Board4x4.DIMENSON;
	}

	@Override
	public Cell getItem(int position) {
		return boardGame.getCell(position/4, position%4);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public int getCurrentPosition(){
		return currentPosition;
	}
}
