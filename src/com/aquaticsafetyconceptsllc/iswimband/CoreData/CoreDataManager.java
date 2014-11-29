package com.aquaticsafetyconceptsllc.iswimband.CoreData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

/**
 * Created by donaldpae on 11/26/14.
 */
public class CoreDataManager {
    public static CoreDataManager _instance = null;

    private Context mContext;
    private DBManager dbManager;

    public static CoreDataManager initialize(Context context) {
        if (_instance == null)
            _instance = new CoreDataManager(context);
        return _instance;
    }

    public static CoreDataManager sharedInstance() {
        return _instance;
    }

    private CoreDataManager(Context context) {
        mContext = context;
        dbManager = new DBManager(mContext);

        if (!dbManager.open()) {
            Logger.log("database open failed");
        }
    }

    public SwimbandData getSwimbandData(String bandId) {
        Cursor results = dbManager.executeQuery("SELECT * FROM tbl_swimbanddata WHERE bandId = '%s'", bandId);
        if (results.moveToFirst())
        {
            /*
            bandId TEXT PRIMARY KEY,
            warningTime INTEGER,
            alarmTime INTEGER,
            alarmType INTEGER,
            authKey TEXT,
            name TEXT,
            firstTime INTEGER,
            disconnectTime REAL
             */
            do {
                int i = 0;
                SwimbandData swimbandData  = new SwimbandData();

                swimbandData.bandId       = results.getString(i++);
                swimbandData.warningTime     = results.getInt(i++);
                swimbandData.alarmTime = results.getInt(i++);
                swimbandData.alarmType = results.getInt(i++);
                swimbandData.authKey = results.getString(i++);
                swimbandData.name = results.getString(i++);
                swimbandData.firstTime = (results.getInt(i++) == 0) ? false : true;
                swimbandData.disconnectTime = results.getDouble(i++);

                return swimbandData;
            } while (false);
        }
        return null;
    }

    public void saveSwimbandData(SwimbandData swimbandData) {
        Logger.log("saveSwimbandData - %s", swimbandData.bandId);
        if (isExistSwimbandData(swimbandData.bandId)) {
            String sql = String.format("UPDATE tbl_swimbanddata SET warningTime = %d , alarmTime = %d, alarmType = %d, authKey = '%s', name = '%s', firstTime = %d, disconnectTime = %f WHERE bandId = '%s'",
                    swimbandData.warningTime,
                    swimbandData.alarmTime,
                    swimbandData.alarmType,
                    swimbandData.authKey,
                    swimbandData.name,
                    (swimbandData.firstTime) ? 1 : 0,
                    swimbandData.disconnectTime,
                    swimbandData.bandId);
            dbManager.executeUpdate(sql);
        }
        else {
            ContentValues values = new ContentValues();
            values.put("bandId", swimbandData.bandId);
            values.put("warningTime", swimbandData.warningTime);
            values.put("alarmTime", swimbandData.alarmTime);
            values.put("alarmType", swimbandData.alarmType);
            values.put("authKey", swimbandData.authKey);
            values.put("name", swimbandData.name);
            values.put("firstTime", swimbandData.firstTime?1:0);
            values.put("disconnectTime", swimbandData.disconnectTime);

            long ret = dbManager.insert("tbl_swimbanddata", null, values);
            if (ret < 0) {
                //return 0;
                Logger.log("added swimbanddata failed");
            }
            else {
                //return ret;
                Logger.log("added swimbanddata success");
            }
        }
    }

    public boolean isExistSwimbandData(String bandId) {
        String sql = String.format("SELECT COUNT(*) AS samecount FROM tbl_swimbanddata WHERE bandId = '%s'", bandId);
        Cursor results = dbManager.executeCommand(sql);
        if (results.moveToFirst())
        {
            do  {
                int count       = results.getInt(0); //[results intForColumn:@"samecount"];
                if (count > 0)
                    return true;
                return false;
            } while (false);
        }
        return false;
    }
}
