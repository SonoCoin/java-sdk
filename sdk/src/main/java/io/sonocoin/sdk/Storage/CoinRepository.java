package io.sonocoin.sdk.Storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;
import org.json.JSONException;
import io.sonocoin.sdk.Core.Coinservice;
import io.sonocoin.sdk.Core.Config;
import io.sonocoin.sdk.Core.DownloadCallback;
import io.sonocoin.sdk.Core.JsonGetSQLiteStatus;
import io.sonocoin.sdk.Core.JsonStorage;
import io.sonocoin.sdk.R;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Out;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by tim on 15.01.2017.
 */

public class CoinRepository extends io.sonocoin.sdk.Storage.Repository {

    // constructors
    public CoinRepository(Context context) {
        super(context);
    }

    public CoinRepository(DbHelper dbHelper) {
        super(dbHelper);
    }

    // methods
    private boolean exist(@Nullable SQLiteDatabase db, int id) {
        SQLiteDatabase _db = db == null ? _dbHelper.getReadableDatabase() : db;
        String sql = "SELECT _id FROM " + Coin.CoinEntry.TABLE_NAME + " WHERE _id = " + id;
        Cursor cursor = _db.rawQuery(sql, null);
        return cursor.getCount() > 0;
    }

    // methods
    public int getIdByHash(@Nullable SQLiteDatabase db, String hash, int index) {
        SQLiteDatabase _db = db == null ? _dbHelper.getReadableDatabase() : db;
        String sql = "SELECT _id FROM " + Coin.CoinEntry.TABLE_NAME + " WHERE " + Coin.CoinEntry.COLUMN_NAME_HASH + " = ? and " + Coin.CoinEntry.COLUMN_NAME_INDEX + " = " + index;
        Cursor cursor = _db.rawQuery(sql, new String[]{hash});
        Boolean exist = cursor.getCount() > 0;
        Integer id = 0;
        if (exist) {
            if (cursor.moveToFirst()) {
                id = cursor.getColumnIndex("_id");
                id = cursor.getInt(id);
            }
        }
        return id;
    }

    // methods
    public Coin getByHash(@Nullable SQLiteDatabase db, String hash, int index) {
        SQLiteDatabase _db = db == null ? _dbHelper.getReadableDatabase() : db;
        String fields = TextUtils.join(",", new String[]{
                Coin.CoinEntry.COLUMN_NAME_AMOUNT,
                Coin.CoinEntry.COLUMN_NAME_HASH,
                Coin.CoinEntry.COLUMN_NAME_INDEX,
                Coin.CoinEntry.COLUMN_NAME_REL,
                Coin.CoinEntry.COLUMN_NAME_COINTYPE,
                Coin.CoinEntry.COLUMN_NAME_STATUS,
                Coin.CoinEntry.COLUMN_NAME_DT,
                Coin.CoinEntry.COLUMN_NAME_SECRETKEY,
                Coin.CoinEntry.COLUMN_NAME_PUBLICKEY,
                Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD,
                Coin.CoinEntry.COLUMN_NAME_COIN_VERSION
        });
        String sql = "SELECT _id, " + fields + " FROM " + Coin.CoinEntry.TABLE_NAME + " WHERE " + Coin.CoinEntry.COLUMN_NAME_HASH + " = ? and " + Coin.CoinEntry.COLUMN_NAME_INDEX + " = " + index;
        Cursor cursor = _db.rawQuery(sql, new String[]{hash});
        int idIndex = cursor.getColumnIndex(Coin.CoinEntry._ID);
        int amountIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_AMOUNT);
        int hashIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_HASH);
        int indexIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_INDEX);
        int relIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_REL);
        int cointypeIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_COINTYPE);
        int statusIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_STATUS);
        int dtIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_DT);
        int skIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_SECRETKEY);
        int pkIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_PUBLICKEY);
        int emIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD);
        int cvIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_COIN_VERSION);
        Boolean exist = cursor.getCount() > 0;
        Integer id = 0;
        if (exist) {
            if (cursor.moveToFirst()) {
                BigInteger amount = Coinservice.stringToBigint(cursor.getString(amountIndex));
                BigInteger bigDecY = new BigInteger(String.valueOf(Config.COUNT_DECIMALS));
                BigInteger result = amount.divide(bigDecY);
                String f = Coinservice.bigintToString(result);
                Coin c = new Coin(
                        cursor.getInt(idIndex),
                        amount,
                        cursor.getString(hashIndex),
                        cursor.getInt(indexIndex),
                        cursor.getInt(cointypeIndex),
                        cursor.getInt(statusIndex),
                        cursor.getString(dtIndex),
                        f,
                        cursor.getString(skIndex),
                        cursor.getString(pkIndex),
                        cursor.getInt(relIndex),
                        cursor.getInt(emIndex),
                        cursor.getInt(cvIndex)
                );
                return c;
            }
        }
        return null;
    }

    public boolean checkTableExists() {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + Coin.CoinEntry.TABLE_NAME + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        db.execSQL(DbHelper.SQL_CREATE_COINS);
        return false;
    }

    public ArrayList<Coin> getList(DownloadCallback downloadCallback, boolean updateStatus, SQLiteDatabase _db, Context ctx) {
        SQLiteDatabase db = _db == null ? _dbHelper.getReadableDatabase() : _db;
        String[] projection = {
                Coin.CoinEntry._ID,
                Coin.CoinEntry.COLUMN_NAME_AMOUNT,
                Coin.CoinEntry.COLUMN_NAME_HASH,
                Coin.CoinEntry.COLUMN_NAME_INDEX,
                Coin.CoinEntry.COLUMN_NAME_COINTYPE,
                Coin.CoinEntry.COLUMN_NAME_STATUS,
                Coin.CoinEntry.COLUMN_NAME_REL,
                Coin.CoinEntry.COLUMN_NAME_DT,
                Coin.CoinEntry.COLUMN_NAME_SECRETKEY,
                Coin.CoinEntry.COLUMN_NAME_PUBLICKEY,
                Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD,
                Coin.CoinEntry.COLUMN_NAME_COIN_VERSION,
        };
        String sortOrder = Coin.CoinEntry._ID + " DESC";
        Cursor cursor = db.query(
                Coin.CoinEntry.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                null, //"_id = ?",                // The columns for the WHERE clause
                null, // new String[] {Integer.toString(channelId)},                             // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
                // limit 10,2
        );
        ArrayList<Coin> list = new ArrayList<Coin>();
        ArrayList<Out> outs = new ArrayList<Out>();
        int idIndex = cursor.getColumnIndex(Coin.CoinEntry._ID);
        int amountIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_AMOUNT);
        int hashIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_HASH);
        int indexIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_INDEX);
        int cointypeIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_COINTYPE);
        int statusIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_STATUS);
        int relIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_REL);
        int dtIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_DT);
        int skIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_SECRETKEY);
        int pkIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_PUBLICKEY);
        int emIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD);
        int cvIndex = cursor.getColumnIndex(Coin.CoinEntry.COLUMN_NAME_COIN_VERSION);
        try {
            if (cursor.moveToFirst()) {
                int cnt = 0;
                while (!cursor.isAfterLast()) {
                    cnt++;
                    BigInteger amountint = Coinservice.stringToBigint(cursor.getString(amountIndex));
                    BigDecimal amount = new BigDecimal(amountint);
                    // Log.d("SonoDebug", "amount: " + cnt + " == " + amount);
                    BigDecimal bigDecY = new BigDecimal(String.valueOf(Config.COUNT_DECIMALS));
                    BigDecimal result = amount.divide(bigDecY);
                    String f = Coinservice.bigdecToString(result);
                    String hash = cursor.getString(hashIndex);
                    int index = cursor.getInt(indexIndex);
                    int st = cursor.getInt(statusIndex);
                    if (st != Coin.CoinEntry.STATUS_DEL) {
                        list.add(new Coin(
                                cursor.getInt(idIndex),
                                amountint,
                                hash,
                                index,
                                cursor.getInt(cointypeIndex),
                                st,
                                cursor.getString(dtIndex),
                                f,
                                cursor.getString(skIndex),
                                cursor.getString(pkIndex),
                                cursor.getInt(relIndex),
                                cursor.getInt(emIndex),
                                cursor.getInt(cvIndex)
                        ));
                        Out o = new Out();
                        o.hash = hash;
                        o.index = index;
                        outs.add(o);
                    }
                    cursor.moveToNext();
                }
                // Log.d("SonoDebug", "count: " + cnt);
            }
            if (outs.size() > 0) {
                if (updateStatus) {
                    Boolean result = JsonGetSQLiteStatus.getStatus(db, outs, null, downloadCallback, true);
                    if (!result) {
                        downloadCallback.onActionDone(list);
                        String url = Config.MAIN_SERVER;
                        String method = "/v1/nodes";
                        JsonStorage.requestNodes(url + method, ctx);
                    }
                } else {
                    if (downloadCallback != null) {
                        downloadCallback.onActionDone(list);
                    }
                    return list;

                }
            } else {
                if (downloadCallback != null) {
                    downloadCallback.onActionDone(Collections.<Coin>emptyList());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // cursor.close();
        }
        return list;
    }

    public void updateList(SQLiteDatabase _db, List<Coin> items, Context context, final DownloadCallback downloadCallback) {
        SQLiteDatabase db = _db == null ? _dbHelper.getReadableDatabase() : _db;
        Integer ins_id = 0;
        Map<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        ArrayList<Coin> coinsToReturn = new ArrayList<Coin>();
        try {
            for (Coin item : items) {
                ContentValues values = new ContentValues();
                String amount_format = item.amount.toString();
                item.amount_format = amount_format;
                values.put(Coin.CoinEntry.COLUMN_NAME_AMOUNT, item.amount_format);
                values.put(Coin.CoinEntry.COLUMN_NAME_HASH, item.hash);
                values.put(Coin.CoinEntry.COLUMN_NAME_INDEX, item.index);
                values.put(Coin.CoinEntry.COLUMN_NAME_COINTYPE, item.cointype);
                values.put(Coin.CoinEntry.COLUMN_NAME_STATUS, item.status);
                values.put(Coin.CoinEntry.COLUMN_NAME_REL, item.rel);
                values.put(Coin.CoinEntry.COLUMN_NAME_DT, item.dt);
                values.put(Coin.CoinEntry.COLUMN_NAME_ENCRYPTED_METHOD, item.encrypted_method);
                values.put(Coin.CoinEntry.COLUMN_NAME_COIN_VERSION, item.coin_version);
                if (exist(db, item.id)) { // update
                    // Which row to update, based on the ID
                    String selection = Coin.CoinEntry._ID + " = ?";
                    String[] selectionArgs = {String.valueOf(item.id)};
                    // not update row which has relation
                    if (item.rel < 1 || item.rel == null || (item.rel > 1 && item.status == Coin.CoinEntry.STATUS_OLD)) {
                        db.update(
                                Coin.CoinEntry.TABLE_NAME,
                                values,
                                selection,
                                selectionArgs);
                    }
                    hashMap.put(item.id, item.status);
                } else { // insert
                    if (item.id > 0) {
                        values.put(Coin.CoinEntry._ID, item.id);
                    }
                    int new_index = getIdByHash(db, item.hash, item.index);
                    if (new_index > 0) {
                        Toast.makeText(context, R.string.has_already, Toast.LENGTH_SHORT);
                        String selection = Coin.CoinEntry._ID + " = ?";
                        String[] selectionArgs = {String.valueOf(new_index)};
                        db.update(
                                Coin.CoinEntry.TABLE_NAME,
                                values,
                                selection,
                                selectionArgs);
                        hashMap.put(item.id, item.status);
                    } else {
                        values.put(Coin.CoinEntry.COLUMN_NAME_SECRETKEY, item.secretkey);
                        values.put(Coin.CoinEntry.COLUMN_NAME_PUBLICKEY, item.pk_script);
                        long l = db.insert(
                                Coin.CoinEntry.TABLE_NAME,
                                Coin.CoinEntry.COLUMN_NAME_NULLABLE,
                                values);
                        ins_id = (int) l;
                        // Log.d("QUERY", db.toString());
                        hashMap.put(ins_id, item.status);
                    }
                }
            }
            // update row which has relation
            for (Coin item : items) {
                // all rows which have relation and have status "wait"
                if (item.rel > 1 && (item.status == Coin.CoinEntry.STATUS_WAIT || item.status == Coin.CoinEntry.STATUS_OLD)) {
                    /*Log.d("Test", String.valueOf(hashMap.containsKey(item.rel.intValue())));
                    Log.d("Test", String.valueOf(hashMap.containsKey(item.rel.toString())));
                    Log.d("Test", String.valueOf(hashMap.containsKey(item.rel)));*/
                    if (hashMap.containsKey(item.rel.intValue())) {
                        int new_status = hashMap.get(item.rel);
                        // if new row is confirmed
                        if (new_status == Coin.CoinEntry.STATUS_READY) {
                            String selection = Coin.CoinEntry._ID + " = ?";
                            String[] selectionArgs = {String.valueOf(item.id)};
                            ContentValues values = new ContentValues();
                            String amount_format = item.amount.toString();
                            item.amount_format = amount_format;
                            item.status = Coin.CoinEntry.STATUS_OLD;
                            values.put(Coin.CoinEntry.COLUMN_NAME_AMOUNT, item.amount_format);
                            values.put(Coin.CoinEntry.COLUMN_NAME_HASH, item.hash);
                            values.put(Coin.CoinEntry.COLUMN_NAME_INDEX, item.index);
                            values.put(Coin.CoinEntry.COLUMN_NAME_COINTYPE, item.cointype);
                            values.put(Coin.CoinEntry.COLUMN_NAME_STATUS, item.status);
                            values.put(Coin.CoinEntry.COLUMN_NAME_REL, item.rel);
                            values.put(Coin.CoinEntry.COLUMN_NAME_DT, item.dt);
                            db.update(
                                    Coin.CoinEntry.TABLE_NAME,
                                    values,
                                    selection,
                                    selectionArgs);
                            // if new row is not confirmed, has error
                        } else if (new_status != Coin.CoinEntry.STATUS_WAIT) {
                            String selection = Coin.CoinEntry._ID + " = ?";
                            String[] selectionArgs = {String.valueOf(item.id)};
                            ContentValues values = new ContentValues();
                            String amount_format = item.amount.toString();
                            item.amount_format = amount_format;
                            // item.status = item.encrypted_method == Config.COIN_ENCRYPTED_METHOD_DEFAULT ? Coin.CoinEntry.STATUS_READY : Coin.CoinEntry.STATUS_FORISSUE;
                            item.status = (item.status == Coin.CoinEntry.STATUS_WAIT) ? (item.encrypted_method == Config.COIN_ENCRYPTED_METHOD_DEFAULT ? Coin.CoinEntry.STATUS_READY : Coin.CoinEntry.STATUS_FORISSUE) : item.status;
                            values.put(Coin.CoinEntry.COLUMN_NAME_AMOUNT, item.amount_format);
                            values.put(Coin.CoinEntry.COLUMN_NAME_HASH, item.hash);
                            values.put(Coin.CoinEntry.COLUMN_NAME_INDEX, item.index);
                            values.put(Coin.CoinEntry.COLUMN_NAME_COINTYPE, item.cointype);
                            values.put(Coin.CoinEntry.COLUMN_NAME_STATUS, item.status);
                            values.put(Coin.CoinEntry.COLUMN_NAME_REL, item.rel);
                            values.put(Coin.CoinEntry.COLUMN_NAME_DT, item.dt);
                            db.update(
                                    Coin.CoinEntry.TABLE_NAME,
                                    values,
                                    selection,
                                    selectionArgs);
                        }
                    }
                }
                if (item.rel == 1 && item.id > 0 && ins_id > 0) {
                    item.rel = ins_id;
                    String selection = Coin.CoinEntry._ID + " = ?";
                    String[] selectionArgs = {String.valueOf(item.id)};
                    ContentValues values = new ContentValues();
                    String amount_format = item.amount.toString();
                    item.amount_format = amount_format;
                    values.put(Coin.CoinEntry.COLUMN_NAME_AMOUNT, item.amount_format);
                    values.put(Coin.CoinEntry.COLUMN_NAME_HASH, item.hash);
                    values.put(Coin.CoinEntry.COLUMN_NAME_INDEX, item.index);
                    values.put(Coin.CoinEntry.COLUMN_NAME_COINTYPE, item.cointype);
                    values.put(Coin.CoinEntry.COLUMN_NAME_STATUS, item.status);
                    values.put(Coin.CoinEntry.COLUMN_NAME_REL, item.rel);
                    values.put(Coin.CoinEntry.COLUMN_NAME_DT, item.dt);
                    values.put(Coin.CoinEntry.COLUMN_NAME_REL, item.rel);
                    db.update(
                            Coin.CoinEntry.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs);
                }
            }
            if (downloadCallback != null) {
                coinsToReturn = getList(downloadCallback, false, db, null);
                downloadCallback.onActionDone(coinsToReturn);
            }
        } finally {
        }
    }

    public void clear() {
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        try {
            db.execSQL("delete from " + Coin.CoinEntry.TABLE_NAME);
        } finally {
            db.close();
        }
    }

}
