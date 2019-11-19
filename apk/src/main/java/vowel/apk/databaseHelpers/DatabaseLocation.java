package vowel.apk.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseLocation extends SQLiteOpenHelper {

    private final static  String DATABASE_NAME ="location.db";
    private final static String TABLE_NAME ="location_table";
    private static final String COL_1 = "EC_NUMBER";
    private static final String COL_2 = "LONGITUDE";
    private static final String COL_3 = "LATITUDE";

    public DatabaseLocation(Context context) {
        super(context , DATABASE_NAME , null, 4);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(EC_NUMBER TEXT UNIQUE, LONGITUDE TEXT, LATITUDE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }


    //add to database
    public void insertLocation(String ec, double longitude, double latitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_1, ec);
        cv.put(COL_2,longitude);
        cv.put(COL_3, latitude);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }


    //check if the row is already present
    public boolean isInDatabase(String ec) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_1 + " = ?;";
        c = db.rawQuery(query, new String[]{ec});
        if (c.getCount() <= 0) {
            c.close();
            return false;
        }
        c.close();
        return true;
    }


    //get all the rows
    public ArrayList<String> getAllLocations(){
        String ecNum,longitude,latitude,fullMessage;
        ArrayList<String> messageList = new ArrayList<>();
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){

            ecNum = cursor.getString(0);
            longitude = cursor.getString(1);
            latitude = cursor.getString(2);
            fullMessage = ecNum+"_~"+longitude+"<->"+latitude;
            messageList.add(fullMessage);
        }
        cursor.close();
        return messageList;
    }


    //update a location detail
    public void updateLocation(String ec, double longitude, double latitude){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, longitude);
        contentValues.put(COL_3, latitude);
        db.update(TABLE_NAME, contentValues,"EC_NUMBER=?",new String[]{ec});
    }

    //delete a row
    public void deleteData(String ec){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "EC_NUMBER = ?" ,new String[]{ec});

    }

    //get the location row
    public String getLocation(String ec){
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_1+" = ?;";
        Cursor cursor = db.rawQuery(query,new  String[] {ec});
        String longitude = null;
        String latitude = null;
        if (cursor.moveToFirst()) {
            longitude = cursor.getString(1);
            latitude = cursor.getString(2);}
        cursor.close();
        return longitude + " "+ latitude;
    }


}
