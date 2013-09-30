package cyber.game.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLDataException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper{
	private static final String DATABASE_PATH = "/data/data/cyber.game.flaggame/databases/";
	private static final String DATABASE_NAME = "flagGame.sqlite";
	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase myDataBase;
	private final Context myContext;

	// Questions table
	public static final String TABLE_NAME = "Questions";
	public static final String QID = "qid";
	public static final String QUESTION = "question";
	public static final String ANSWER_A = "answer1"; 
	public static final String ANSWER_B = "answer2";
	public static final String ANSWER_C = "answer3";
	public static final String ANSWER_D = "answer4";
	public static final String TRUE_ANSWER = "trueAnswer";
	public static final String DIFFICULT= "difficult";

	public static final String EASY = "easy";
	public static final String MEDIUM = "medium";
	public static final String HARD = "hard";
	
	public static final int MAX_QUESTION = 200;
	
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		myContext= context;
	}

	public void openDatabase() throws SQLDataException{
		String myPath = DATABASE_PATH + DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	@Override
	public synchronized void close() {
		if(myDataBase != null)
			myDataBase.close();
		super.close();
	}

	private boolean checkDataBase(){ 
		SQLiteDatabase checkDB = null;

		try{
			String myPath = DATABASE_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}catch(SQLiteException e){
			//database chua ton tai
		}

		if(checkDB != null)
			checkDB.close();
		return checkDB != null ? true : false;

	}

	private void copyDataBase() throws IOException{
		InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

		String outFileName = DATABASE_PATH + DATABASE_NAME;

		OutputStream myOutput = new FileOutputStream(outFileName);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0)
			myOutput.write(buffer, 0, length);

		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void createDataBase() {
		boolean dbExist = checkDataBase(); 

		if(!dbExist){
			this.getReadableDatabase();
			try {
				copyDataBase(); 
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  
	
	public Question getRandomQuestion(String difficult, ArrayList<Integer> exclude){
		SQLiteDatabase sd = getReadableDatabase();
		
		String query = "";
		query = makeQuery(difficult, exclude);
		Cursor cursor = sd.rawQuery(query, null);
		cursor.moveToNext();
		
		Question question = new Question();
		question.setQid(cursor.getInt(cursor.getColumnIndex(QID)));
		question.setQuestion(cursor.getString(cursor.getColumnIndex(QUESTION)));
		question.setAnswerID(cursor.getInt(cursor.getColumnIndex(TRUE_ANSWER)));
		String[] listChoice = new String[4];
		listChoice[0] = cursor.getString(cursor.getColumnIndex(ANSWER_A));
		listChoice[1] = cursor.getString(cursor.getColumnIndex(ANSWER_B));
		listChoice[2] = cursor.getString(cursor.getColumnIndex(ANSWER_C));
		listChoice[3] = cursor.getString(cursor.getColumnIndex(ANSWER_D));
		question.setChoiceList(listChoice);
		question.setRankScore(cursor.getString(cursor.getColumnIndex(DIFFICULT)));
		
		return question;
	}

	private String makeQuery(String difficult, ArrayList<Integer> exclude) {
		String query;
		// begin part of query 
		query = "SELECT * FROM "+TABLE_NAME
				+" WHERE "+QID+">=random() % (SELECT max("+QID+") FROM "+TABLE_NAME+")"
				+" AND "+DIFFICULT+"=\""+difficult+"\"";
		// middle part of query : for exclude 
		for (Integer id : exclude) {
			query += " AND "+QID+"<>"+id;
		}
		// end part
		query += " LIMIT 1;";
		return query;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}