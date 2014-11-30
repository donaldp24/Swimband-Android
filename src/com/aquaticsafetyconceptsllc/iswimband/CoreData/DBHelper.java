package com.aquaticsafetyconceptsllc.iswimband.CoreData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

/**
 * Created by donaldpae on 11/26/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wahooo.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sql_swimbanddata = "CREATE TABLE tbl_swimbanddata(bandId TEXT PRIMARY KEY, warningTime INTEGER, alarmTime INTEGER, alarmType INTEGER, authKey TEXT, name TEXT, firstTime INTEGER, disconnectTime REAL);";
            String sql_serialno = "CREATE TABLE tbl_serialno(address TEXT PRIMARY KEY, serialno TEXT)";
            db.execSQL(sql_swimbanddata);
            db.execSQL(sql_serialno);
        } catch (Exception e) {
            Logger.log("exception in db creating : " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //if (oldVersion == 1 && newVersion == 1.1) {
        //    db.execSQL("DROP TABLE IF EXISTS tbl_job");
        //    db.execSQL("DROP TABLE IF EXISTS tbl_location");
        //    db.execSQL("DROP TABLE IF EXISTS tbl_product");
        //    db.execSQL("DROP TABLE IF EXISTS tbl_locproduct");
        //    db.execSQL("DROP TABLE IF EXISTS tbl_reading");
        //    onCreate(db);
        //}
    }
}
