

package vowel.apk.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;



public class DatabaseHelper extends SQLiteOpenHelper {

    private final static  String DATABASE_NAME ="credentials.db";
    private final static String TABLE_NAME ="user_table";
    private static final String COL_1 = "FIRST_NAME";
    private static final String COL_2 = "SURNAME";
    private static final String COL_3 = "EC_NUMBER";
    private static final String COL_4 = "PASSWORD";
    private static final String COL_5 = "IP";


    public DatabaseHelper(Context context) {
        super(context , DATABASE_NAME , null, 1);

    }
@Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(FIRST_NAME TEXT ,SURNAME TEXT, EC_NUMBER TEXT UNIQUE, PASSWORD TEXT, IP TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }


    // add a row to the database
    public void insertData(String name, String surname, String ec, String password, String ip){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_1, name);
        cv.put(COL_2, surname);
        cv.put(COL_3, ec);
        cv.put(COL_4, password);
        cv.put(COL_5,ip);
        db.insert(TABLE_NAME, null, cv);

    }


//update the password for a row
    public void updatePassword(String ec, String password){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, password);
        db.update(TABLE_NAME, contentValues,"EC_NUMBER=?",new String[]{ec});

    }

    //update the ip address for a row
    public void updateIP(String ec,String ip){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_5, ip);
        db.update(TABLE_NAME, contentValues,"EC_NUMBER=?",new String[]{ec});
    }

//delete a row
    public void deleteData(String ec){
        SQLiteDatabase db = this.getWritableDatabase();
         db.delete(TABLE_NAME, "EC_NUMBER = ?" ,new String[]{ec});

    }

    //get the name and surname for a row
    public ArrayList<String> getContactNames() {
        String name,surname,myNameF;
        ArrayList<String> contactNames = new ArrayList<>();
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+ TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);


            while (cursor.moveToNext()) {

                        name = cursor.getString(0);
                        surname = cursor.getString(1);
                        myNameF = name+" "+surname;
                        contactNames.add(myNameF);


        }
        cursor.close();
        return contactNames;
    }

    //get the list of ec numbers(Column)
    public ArrayList<String> getEcList(){
        String ec;
        ArrayList<String> ecNums = new ArrayList<>();
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);

            while (cursor.moveToNext()) {
                        ec = cursor.getString(2);
                        ecNums.add(ec);

                }


        cursor.close();
        return ecNums;

    }

    //get the column for ip addresses
    public String getIP(String ec){
      SQLiteDatabase db =  this.getWritableDatabase();
      String ip = "127.0.0.1";
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_3+" = ?;";
        Cursor cs = db.rawQuery(query,new  String[] {ec});
        if (cs.moveToFirst()) {
            ip = cs.getString(4);
            }
        cs.close();
        return ip;
    }

    //get a row from database cursor
    public Cursor getRow(String ec){
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_3+" = ?;";
        return db.rawQuery(query,new  String[] {ec});
    }

//get username (row) cursor
    public Cursor getUsername(String ec) {
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_3+" = ?;";
        return db.rawQuery(query,new  String[] {ec});
    }

//get a username detail string
    public String getUsernameDetail(String ec) {
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_3+" = ?;";
        Cursor cursor = db.rawQuery(query,new  String[] {ec});
        String name = null;
        String surname = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            surname = cursor.getString(1);}
            cursor.close();
        return name + " "+ surname;
    }

    //get username from ip address

    public String ipToUsername(String ip) {
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_5+" = ?;";
        Cursor cursor = db.rawQuery(query,new  String[] {ip});
        String name = null;
        String surname = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            surname = cursor.getString(1);}
        cursor.close();
        return name + " "+ surname;
    }

    //get EC number from ip address
    public String ipToEC(String ip) {
        SQLiteDatabase db =  this.getWritableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE " +COL_5+" = ?;";
        Cursor cursor = db.rawQuery(query,new  String[] {ip});
        String ec = null;
        if (cursor.moveToFirst()) {
            ec = cursor.getString(2); }
        cursor.close();
        return ec;
    }
}
