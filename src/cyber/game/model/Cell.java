package cyber.game.model;

public class Cell {
	public static enum CellState {Blank, X, O};
	private CellState state;
	private int wrongTime;
	public Cell() {
		super();
		this.state = CellState.Blank;
		this.wrongTime=0;
	}
	public CellState getState() {
		return state;
	}
	public void setState(CellState state) {
		this.state = state;
	}
	
	public int getWrongTime() {
		return wrongTime;
	}
	public void setWrongTime(int wrongTime) {
		this.wrongTime = wrongTime;
	}
	public void riseWrongTime(){
		this.wrongTime++;
	}
	@Override
	public String toString() {
		return "Cell [state=" + state + ", wrongTime=" + wrongTime + "]";
	}
	
}
