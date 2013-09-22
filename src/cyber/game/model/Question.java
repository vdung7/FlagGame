package cyber.game.model;

import java.util.Arrays;

public class Question {
	private String question;
	private String[] choiceList = new String[4];
	private int answerID;
	private int rankScore;

	private static final int EASY_SCORE = 5;
	private static final int MEDIUM_SCORE = 10;
	private static final int HARD_SCORE = 15;

	public Question(String question, String[] choiceList, int answerID,
			int rankScore) {
		super();
		this.question = question;
		this.choiceList = choiceList;
		this.answerID = answerID;
		this.rankScore = rankScore;
	}
	public Question() {
		this("", new String[4], 0, 0);
	}

	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String[] getChoiceList() {
		return choiceList;
	}
	public void setChoiceList(String[] choiceList) {
		this.choiceList = choiceList;
	}
	public int getAnswerID() {
		return answerID;
	}
	public void setAnswerID(int answerID) {
		this.answerID = answerID;
	}
	public int getRankScore() {
		return rankScore;
	}
	public void setRankScore(String difficult) {
		if (difficult.equals(SQLiteHelper.EASY)) {
			rankScore = EASY_SCORE;
		} else if (difficult.equals(SQLiteHelper.MEDIUM	)){
			rankScore = MEDIUM_SCORE;
		} else {
			rankScore = HARD_SCORE;
		}
	}
	@Override
	public String toString() {
		return "Question [question=" + question + ", choiceList="
				+ Arrays.toString(choiceList) + ", answerID=" + answerID
				+ ", rankScore=" + rankScore + "]";
	}

}