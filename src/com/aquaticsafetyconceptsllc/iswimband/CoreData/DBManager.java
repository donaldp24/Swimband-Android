package com.aquaticsafetyconceptsllc.iswimband.CoreData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

/**
 * Created by donaldpae on 11/26/14.
 */
public class DBManager {

    private DBHelper mHelper;
    private SQLiteDatabase db;
    private SQLiteCursor openCursor;

    public DBManager(Context context) {
        mHelper = new DBHelper(context);
    }

    public boolean open() {
        Logger.log("db opening....");
        try {
            db = mHelper.getWritableDatabase();
            return true;
        } catch (Exception e) {
            Logger.log("error in open database : " + e.getMessage());
            return false;
        }
    }

    public boolean close() {
        try {
            mHelper.close();
            return true;
        } catch (Exception e) {
            Logger.log("error in close database : " + e.getMessage());
            return false;
        }
    }

    public Cursor executeQuery(String formatSql, Object ...arguments) {
        String sql = String.format(formatSql, arguments);
        Logger.log("executeQuery : %s", sql);
        return db.rawQuery(sql, null);
    }

    public Cursor executeCommand(String formattedSql)
    {
        Logger.log("executeCommand : %s", formattedSql);
        return db.rawQuery(formattedSql, null);
    }

    public boolean executeUpdate(String formatSql, Object ...arguments) {
        String sql = String.format(formatSql, arguments);
        Logger.log("executeUpdate : %s", sql);
        db.execSQL(sql);
        return true;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        return db.insert(table, nullColumnHack, values);
    }
}
