package io.sonocoin.sdk.Storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.sonocoin.sdk.Core.Config;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Setting;

/**
 * Created by tim on 15.01.2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper instance;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = Config.DATABASE_VERSION;
    public static final String DATABASE_NAME = "sono16.db";

    private static final String INT_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String FLOAT_TYPE = " NUMERIC";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_COINS =
            "CREATE TABLE " + Coin.CoinEntry.TABLE_NAME + " (" +
                    Coin.CoinEntry._ID + " " + INT_TYPE + " PRIMARY KEY, " +
                    Coin.CoinEntry.COLUMN_NAME_AMOUNT + FLOAT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_HASH + TEXT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_INDEX + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_COINTYPE + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_STATUS + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_REL + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_SECRETKEY + TEXT_TYPE  + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_PUBLICKEY + TEXT_TYPE  + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_COIN_VERSION + INT_TYPE + COMMA_SEP +
                    Coin.CoinEntry.COLUMN_NAME_DT + " " + TEXT_TYPE + ")";

    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE " + Setting.SettingEntry.TABLE_NAME + " (" +
                    Setting.SettingEntry._ID + " " + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    Setting.SettingEntry.COLUMN_NAME_COLOR + TEXT_TYPE + COMMA_SEP +
                    Setting.SettingEntry.COLUMN_NAME_LANGUAGE + TEXT_TYPE + COMMA_SEP +
                    Setting.SettingEntry.COLUMN_NAME_SECURITY + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_COINS = "DROP TABLE IF EXISTS " + Coin.CoinEntry.TABLE_NAME;
    private static final String SQL_DELETE_SETTINGS = "DROP TABLE IF EXISTS " + Setting.SettingEntry.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static public synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context);
        }
        return instance;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_COINS);
        db.execSQL(SQL_CREATE_SETTINGS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_COINS);
        db.execSQL(SQL_DELETE_SETTINGS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean isTableExists(SQLiteDatabase db, String tableName, boolean openDb) {
        if(openDb) {
            if(db == null || !db.isOpen()) {
                db = getReadableDatabase();
            }

            if(!db.isReadOnly()) {
                db.close();
                db = getReadableDatabase();
            }
        }

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}
