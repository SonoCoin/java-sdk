package io.sonocoin.sdk.Storage;

import android.content.Context;

/**
 * Created by tim on 16.01.2017.
 */

public class Repository {

    protected DbHelper _dbHelper;

    public Repository(Context context) {
        this._dbHelper = DbHelper.getInstance(context);
    }

    public Repository(DbHelper dbHelper) {
        this._dbHelper = dbHelper;
    }

}
