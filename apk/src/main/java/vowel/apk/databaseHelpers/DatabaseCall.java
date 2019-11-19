package vowel.apk.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseCall extends SQLiteOpenHelper {

  private final static  String DATABASE_NAME ="missed_calls.db";
  private final static String TABLE_NAME ="calls_table";
  private static final String COL_1 = "TIMESTAMP";
  private static final String COL_2 = "EC_NUMBER";

  public DatabaseCall(Context context) {
    super(context , DATABASE_NAME , null, 1);

  }

  @Override
  public void onCreate(SQLiteDatabase db){
    db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(TIMESTAMP TEXT ,EC_NUMBER TEXT)");
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    onCreate(db);
  }


  //add a  row to the database
  public void insertCall(String timestamp, String ec){
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(COL_1, timestamp);
    cv.put(COL_2, ec);
    db.insert(TABLE_NAME, null, cv);

  }
  //delete a row
  public void deleteMissedCall(String timestamp){
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME, "TIMESTAMP = ?" ,new String[]{timestamp});

  }

  //get a list of all missed calls
  public ArrayList<String> getAllCalls(){
    String timestamp,ec,fullDetail;
    ArrayList<String> callList = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME;
    Cursor cursor = db.rawQuery(query,null);

    while (cursor.moveToNext()){

            timestamp = cursor.getString(0);
            ec = cursor.getString(1);
            fullDetail = ec+"_~break "+timestamp;
            callList.add(fullDetail);
        }


    cursor.close();
    return callList;
  }

  //get a timestamp column
  public ArrayList<String> getAllTimeStamp(){
    String timestamp;
    ArrayList<String> missedCallList = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME;
    Cursor cursor = db.rawQuery(query,null);

    while (cursor.moveToNext()){
      timestamp = cursor.getString(0);
      missedCallList.add(timestamp);
    }
    cursor.close();
    return missedCallList;
  }
}
