package cyber.game.model;

import cyber.game.model.Cell.CellState;

public class Board4x4 {
	public static final int DIMENSON = 4;

	private static Cell[][] gridCell;

	public Board4x4() {
		super();
		gridCell = new Cell[DIMENSON][DIMENSON];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				gridCell[i][j]= new Cell();
			}
		}
	}

	public Cell getCell(int x, int y) {
		if(x<=4 && y<=4)
			return gridCell[x][y];
		return null;
	}

	public String getCellDifficult(int x, int y) {
		if((x==0 && y==3) || (x==3 && y==0))
			return SQLiteHelper.HARD;
		else if ((x==1 && (y==1 || y==2)) || (x==2 && (y==1 || y==2)))
			return SQLiteHelper.EASY;
		return SQLiteHelper.MEDIUM;
	}

	public boolean checkForWin(int x, int y, CellState state) {
		// check for a horizontal line
		for (int i = 0; i < DIMENSON; i++) {
			if (gridCell[x][i].getState() != state) {
				break;
			}
			if(i == DIMENSON-1){
				return true;
			}
		}

		// check for a vertical line
		for (int i = 0; i < DIMENSON; i++) {
			if (gridCell[i][y].getState() != state) {
				break;
			}
			if(i == DIMENSON-1){
				return true;
			}
		}

		// check for a diagonal
		if(x==y){
			for (int i = 0; i < DIMENSON; i++) {
				if (gridCell[i][i].getState() != state) {
					break;
				}
				if(i == DIMENSON-1){
					return true;
				}
			}
		}

		// check for a anti-diagonal
		for (int i = 0; i < DIMENSON; i++) {
			if (gridCell[i][(DIMENSON-1)-i].getState() != state) {
				break;
			}
			if(i == DIMENSON-1){
				return true;
			}
		}

		return false;
	}

}

