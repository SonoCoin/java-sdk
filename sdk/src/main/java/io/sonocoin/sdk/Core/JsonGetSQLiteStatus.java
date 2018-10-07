package io.sonocoin.sdk.Core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.sonocoin.sdk.Storage.CoinRepository;
import io.sonocoin.sdk.Storage.DbHelper;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Connection;
import io.sonocoin.sdk.Types.Out;
import io.sonocoin.sdk.Types.Result.Check;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JsonGetSQLiteStatus {
    public static Boolean getStatus(
            final SQLiteDatabase db,
            final ArrayList<Out> outs,
            final Context context,
            final DownloadCallback downloadCallback,
            final boolean needCallbackAfterRepoUpdate
    ) throws JSONException {
        Log.d("JsonGetSQLiteStatus", "getStatus(final SQLiteDatabase db, ArrayList<Out> outs, final Context context...");
        if (Config.CONNECTIONS.size() < 1) {
            downloadCallback.onError("No connections to nodes");
            return false;
        }

        for (Connection con : Config.CONNECTIONS) {
            String url = con.getFullUrl() + Config.ACTION_TX_CHECK;
            JSONObject jo = new JSONObject();
            Collection<JSONObject> items = new ArrayList<JSONObject>();
            for (Out o : outs) {
                JSONObject item1 = new JSONObject();
                item1.put("hash", o.hash);
                item1.put("index", o.index);
                items.add(item1);
            }
            jo.put("outs", new JSONArray(items));
            AndroidNetworking.post(url)
                    .addJSONObjectBody(jo)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            List<Coin> list = new ArrayList<Coin>();
                            CoinRepository repo = null;
                            if (db != null) {
                                DbHelper dbHelper = DbHelper.getInstance(context);
                                repo = new CoinRepository(dbHelper);
                            }
                            // Log.d("SonoCoin", Integer.toString(response.length()));
                            for (int i = 0; i < response.length(); i++) {
                                Check elem = new Check();
                                try {
                                    JSONObject o = (JSONObject) response.get(i);
                                    GsonBuilder builder = new GsonBuilder();
                                    elem = builder.create().fromJson(o.toString(), Check.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if ((db != null) && !Objects.equals(elem.hash, "") && !Objects.equals(elem.index, "")) {
                                    Coin c = repo.getByHash(db, elem.hash, elem.index);
                                    if (c != null) {
                                        switch (elem.status) {
                                            case 0:
                                                // if current status is forissue then not update status
                                                if (c.status == Coin.CoinEntry.STATUS_WAIT && c.rel > 1) {
                                                } else {
                                                    if (c.status != Coin.CoinEntry.STATUS_FORISSUE) {
                                                        c.status = Coin.CoinEntry.STATUS_READY;
                                                    }
                                                }
                                                break;
                                            case 1:
                                            case 2:
                                            case 3:
                                            case 5:
                                            case 6:
                                                c.status = Coin.CoinEntry.STATUS_OLD;
                                                break;
                                            case 4:
                                                c.status = Coin.CoinEntry.STATUS_WAIT;
                                                break;
                                        }
                                        list.add(c);
                                    }
                                }
                                if (db == null) {
                                    int CoinType = 0;
                                    String key = "";
                                    for (Out o : outs) {
                                        if ((o.hash.equals(elem.hash)) && (o.index == elem.index)) {
                                            key = o.key;
                                            break;
                                        }
                                    }
                                    Coin c = new Coin(
                                            0,
                                            elem.value,
                                            elem.hash,
                                            elem.index,
                                            CoinType,
                                            elem.status,
                                            "",
                                            elem.amount_format,
                                            key,
                                            elem.pk_script,
                                            0,// @todo rel
                                            elem.encrypted_method,
                                            elem.coin_version
                                    );
                                    list.add(c);
                                }
                            }
                            // Log.d("SonoCoin", Boolean.toString(need_update));
                            if (db != null) {
                                repo.updateList(db, list, context, needCallbackAfterRepoUpdate ? downloadCallback : null);
                            }
                            if (!needCallbackAfterRepoUpdate) {
                                downloadCallback.onActionDone(list);
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            Log.e("SonoDebug", "API-Server error: " + error.getErrorBody());
                            if ((downloadCallback != null) && (db != null)) {
                                CoinRepository coinRepo = new CoinRepository(context);
                                coinRepo.getList(downloadCallback, false, db, context);
                                // downloadCallback.onError("Node error: " + error.getErrorBody());
                            }
                        }
                    });
            break;
        }
        return true;
    }

}
