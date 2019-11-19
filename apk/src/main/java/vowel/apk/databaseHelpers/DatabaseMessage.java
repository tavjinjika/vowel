package vowel.apk.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseMessage extends SQLiteOpenHelper {

  private final static  String DATABASE_NAME ="messages.db";
  private final static String TABLE_NAME ="message_table";
  private static final String COL_1 = "TIMESTAMP";
  private static final String COL_2 = "EC_NUMBER";
  private static final String COL_3 = "MSG_CONTENT";
  private static final String COL_4 = "FLAG";

  private final static String TABLE_NAME2 ="undelivered_table";
  private static final String COL_1_EC = "EC";
  private static final String COL_2_MSG = "MESSAGE";

  public DatabaseMessage(Context context) {
    super(context , DATABASE_NAME , null, 3);

  }

  @Override
  public void onCreate(SQLiteDatabase db){
    db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(TIMESTAMP TEXT ,EC_NUMBER TEXT, MSG_CONTENT TEXT, FLAG INTEGER)");
    db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME2+"(EC TEXT, MESSAGE TEXT)");
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
    onCreate(db);
  }


  //add a row to database
  public void insertMessage(String timestamp, String ec, String content, boolean flag){
      int x = 0;
      if (flag) x = 1;
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(COL_1, timestamp);
    cv.put(COL_2, ec);
    cv.put(COL_3, content);
    cv.put(COL_4, x);
    db.insert(TABLE_NAME, null, cv);
    db.close();
  }

  //delete a row
  public void deleteMessage(String timestamp){
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME, "TIMESTAMP = ?" ,new String[]{timestamp});

  }


  //get all rows
  public ArrayList<String> getAllMessage(){
    String timestamp,content,ec,fullMessage;
    ArrayList<String> messageList = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME;
    Cursor cursor = db.rawQuery(query,null);

    while (cursor.moveToNext()){

            timestamp = cursor.getString(0);
            ec = cursor.getString(1);
            content = cursor.getString(2);
            fullMessage = ec+"_~break "+content+"_~break "+timestamp;
            messageList.add(fullMessage);
        }


    cursor.close();
    return messageList;
  }

  //get the whole column
  public ArrayList<String> getAllTimeStamp(){
    String timestamp;
    ArrayList<String> messageList = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME;
    Cursor cursor = db.rawQuery(query,null);

    while (cursor.moveToNext()){
      timestamp = cursor.getString(0);
      messageList.add(timestamp);
    }
    cursor.close();
    return messageList;
  }


  //add an undelivered message  to table
  public void insertUndelivered(String ec, String content){
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(COL_1_EC, ec);
    cv.put(COL_2_MSG, content);
    db.insert(TABLE_NAME2, null, cv);
    db.close();
  }


  //delete the acknowledged message
  public void deleteDelivered(String ec){
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME2, "EC = ?" ,new String[]{ec});

  }


  //get an undelivered message
  public String getUndelivered(String ec){
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME2+" WHERE " +COL_1_EC+" = ?;";
    Cursor cursor = db.rawQuery(query,new  String[] {ec});
    String message = null;
    if (cursor.moveToFirst()) {
      message = cursor.getString(1);
      }
    cursor.close();
    return message;
  }


  //get all the undelivered message
  public ArrayList<String> getAllUdelivered(){
    String content,ec,fullMessage;
    ArrayList<String> messageList = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME2;
    Cursor cursor = db.rawQuery(query,null);
    while (cursor.moveToNext()){

      ec = cursor.getString(0);
      content = cursor.getString(1);
      fullMessage = ec+"_~break "+content;
      messageList.add(fullMessage);
    }


    cursor.close();
    return messageList;
  }

}
