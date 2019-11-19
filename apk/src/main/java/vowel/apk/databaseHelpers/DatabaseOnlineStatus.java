package vowel.apk.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseOnlineStatus extends SQLiteOpenHelper {
  private final static  String DATABASE_NAME ="online.db";
  private final static String TABLE_NAME ="status_table";
  private static final String COL_1 = "EC_NUMBER";
  private static final String COL_2 = "IP";

  public DatabaseOnlineStatus(Context context) {
    super(context , DATABASE_NAME , null, 1);

  }

  @Override
  public void onCreate(SQLiteDatabase db){
    db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(EC_NUMBER TEXT UNIQUE,IP TEXT UNIQUE)");
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    onCreate(db);
  }



  // add to database
  public void insertStatus(String ec, String ip){
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(COL_1, ec);
    cv.put(COL_2, ip);
    db.insert(TABLE_NAME, null, cv);

  }


  //delete all the rows
  public void deleteData(){
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME, null, null);

  }

  //get all the rows
  public ArrayList<String> getAllOnlineUsers(){
    String ec;
    ArrayList<String> onlineUsers = new ArrayList<>();
    SQLiteDatabase db =  this.getWritableDatabase();
    String query="SELECT * FROM "+TABLE_NAME;
    Cursor cursor = db.rawQuery(query,null);


    while (cursor.moveToNext()) {

            ec = cursor.getString(0);
            onlineUsers.add(ec);
        }


    cursor.close();
    return onlineUsers;
  }

}