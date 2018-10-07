package io.sonocoin.sdk.Storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.sonocoin.sdk.Core.Config;
import io.sonocoin.sdk.Core.JsonStorage;
import io.sonocoin.sdk.Libs.FileUtils;
import io.sonocoin.sdk.Types.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tim on 15.01.2017.
 */

public class ConnectionRepository extends Repository {

    private static final String SQL_CREATE_CONNECTION =
            "CREATE TABLE " + Connection.ConnectionEntry.TABLE_NAME + " (" +
                    Connection.ConnectionEntry._ID + " INTEGER PRIMARY KEY, " +
                    Connection.ConnectionEntry.COLUMN_NAME_ADDR + " TEXT, " +
                    Connection.ConnectionEntry.COLUMN_NAME_SSL + " BOOLEAN, " +
                    Connection.ConnectionEntry.COLUMN_NAME_PORT + " INTEGER, " +
                    Connection.ConnectionEntry.COLUMN_NAME_URL + " TEXT)";

    // constructors
    public ConnectionRepository(Context context) {
        super(context);
    }

    public ConnectionRepository(DbHelper dbHelper) {
        super(dbHelper);
    }

    // methods
    public Connection get(String addr, int port) {
        SQLiteDatabase _db = _dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + Connection.ConnectionEntry.TABLE_NAME + " WHERE addr = ? and port = '" + port + "'";
        Cursor cursor = _db.rawQuery(sql, new String[]{addr});
        Connection elem = null;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                elem = new Connection(1,
                    cursor.getString(cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_ADDR)),
                    cursor.getInt(cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_PORT)),
                    cursor.getString(cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_SSL)).equals("true"),
                    cursor.getString(cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_URL))
                );
            } while (cursor.moveToNext());
        }
        return elem;
    }

    public List<Connection> getList() {
        String[] projection = {
                Connection.ConnectionEntry._ID,
                Connection.ConnectionEntry.COLUMN_NAME_ADDR,
                Connection.ConnectionEntry.COLUMN_NAME_PORT,
                Connection.ConnectionEntry.COLUMN_NAME_SSL,
                Connection.ConnectionEntry.COLUMN_NAME_URL,
        };
        String sortOrder = Connection.ConnectionEntry._ID + " DESC";
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    Connection.ConnectionEntry.TABLE_NAME,  // The table to query
                    projection,                       // The columns to return
                    null, //"_id = ?",                // The columns for the WHERE clause
                    null, // new String[] {Integer.toString(channelId)},                             // The values for the WHERE clause
                    null,                             // don't group the rows
                    null,                             // don't filter by row groups
                    sortOrder                         // The sort order
                    // limit 10,2
            );
            List<Connection> list = new ArrayList<>();
            int idIndex = cursor.getColumnIndex(Connection.ConnectionEntry._ID);
            int ipIndex = cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_ADDR);
            int portIndex = cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_PORT);
            int sslIndex = cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_SSL);
            int urlIndex = cursor.getColumnIndex(Connection.ConnectionEntry.COLUMN_NAME_URL);
        try {
            while (cursor.moveToNext()) {
                list.add(new Connection(
                    cursor.getInt(idIndex),
                    cursor.getString(ipIndex),
                    cursor.getInt(portIndex),
                    cursor.getString(sslIndex).equals("true"),
                    cursor.getString(urlIndex)
                ));
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    public void update(Context ctx) throws IOException {
        String url = Config.MAIN_SERVER;
        String method = "/v1/nodes";
        try {
            JsonStorage.requestNodes(url + method, ctx);
        } catch (Exception e) {
            FileUtils.dump("Json.requestNodes, error: " + e.getMessage());
            e.printStackTrace();
        }
        /*JsonObjectRequest myReq = new JsonObjectRequest(url,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<String> Test = JsonUtils.ToList(response);
                            Gson gson = new Gson();
                            //String response_obj = response.getString("one");
                            //CoinResult item = gson.fromJson(response_obj, CoinResult.class);
                            // textView.setText(response);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.d("response", error.toString());
                    }
                }
        );*/
        // queue.add(myReq);
        /*SQLiteDatabase db = _dbHelper.getWritableDatabase();
        try {
            for (Connection con : items) {
                ContentValues values = new ContentValues();
                values.put(Connection.ConnectionEntry.COLUMN_NAME_ADDR, con.ip);
                values.put(Connection.ConnectionEntry.COLUMN_NAME_PORT, con.port);
                Connection elem = get(con.ip, con.port);
                // insert
                if (elem.id == -1) {
                    values.put(Connection.ConnectionEntry._ID, Connection.ConnectionEntry.COLUMN_NAME_NULLABLE);
                    db.insert(
                            Connection.ConnectionEntry.TABLE_NAME,
                            Connection.ConnectionEntry.COLUMN_NAME_NULLABLE,
                            values);
                }
            }
        } finally {
            db.close();
        }*/
    }

    public void insert(List<Connection> connections) {
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        try {
            for (Connection con : connections) {
                ContentValues values = new ContentValues();
                values.put(Connection.ConnectionEntry.COLUMN_NAME_ADDR, con.getAddr());
                values.put(Connection.ConnectionEntry.COLUMN_NAME_PORT, con.getPort());
                values.put(Connection.ConnectionEntry.COLUMN_NAME_SSL, con.getSSL());
                values.put(Connection.ConnectionEntry.COLUMN_NAME_URL, con.getUrl());
                Connection elem = get(con.getFullUrl(), con.getPort());
                // insert
                if (elem == null) {
                    values.put(Connection.ConnectionEntry._ID, Connection.ConnectionEntry.COLUMN_NAME_NULLABLE);
                    db.insert(Connection.ConnectionEntry.TABLE_NAME, Connection.ConnectionEntry.COLUMN_NAME_NULLABLE, values);
                }
            }
        } finally {
            db.close();
        }
    }

    public void create() {
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
    }

    public void clear() {
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        try {
            db.execSQL("delete from "+ Connection.ConnectionEntry.TABLE_NAME);
        } finally {
            db.close();
        }
    }

    public boolean checkTableExists() {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+Connection.ConnectionEntry.TABLE_NAME+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        db.execSQL(SQL_CREATE_CONNECTION);
        return false;
    }

}
