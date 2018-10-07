package io.sonocoin.sdk.Core;

import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Connection;
import io.sonocoin.sdk.Types.Faqitem;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.GsonBuilder;

/**
 * Created by Tim on 17.10.2016.
 */
public class Config {

    public static final int DATABASE_VERSION = 1;
    public static final int CMD_ERROR = 7;
    public static final int CMD_COIN = 17;
    public static final int VERSION = 1;
    public static final int COUNT_DECIMALS = 100000000;
    public static final int SIZE_HASH = 32;
    public static final int SIZE_INDEX = 32;
    public static final int PRIVATE_KEY_LEN = 32; // @todo правильное значение 64!
    public static final int PUBLIC_KEY_LEN = 32;
    public static final int COIN_ENCRYPTED_METHOD_DEFAULT = 0;
    public static final int COIN_ENCRYPTED_METHOD_SHA2561000 = 1;
    public static final int COIN_DEFAULT_VERSION = 1;
    public static final double MIN_PART = 0.05;
    public static final String SERVER_ADDR_API = "http://78.140.162.219:8082/api/";
    public static final String MAIN_SERVER = "https://api.sono.money";
    // public static final String MAIN_SERVER = "https://api.notfoolen.ru";
    public static final String ACTION_TX_CHECK = "/api/v1/txs/out";
    public static final String ACTION_TX_PUBLISH = "/api/v1/txs/publish";
    public static final String SONOCOIN = "";
    public static final String BACKUP_SIGN = "SCB";
    public static final String DEBUG_TAG = "SonoDebug";

    public static int ID;
    public static String IOS_VERSION;
    public static String ANDROID_VERSION;
    public static BigDecimal COURSE;
    public static int FEE_TYPE;
    public static BigDecimal FEE_VALUE;
    public static String SESSION_ID;
    public static String AUTH_TOKEN;
    public static String LOGIN;
    public static String PWD;
    public static List<io.sonocoin.sdk.Types.Connection> CONNECTIONS = new ArrayList<>();
    public static Boolean ConnectionsIsRead = false;

    public static List<Coin> Coins = new ArrayList<Coin>();
    public static List<Coin> CoinList = new ArrayList<Coin>();
    public static List<Faqitem> FaqList = new ArrayList<Faqitem>();

    public static void InitRemoteNodesConnections() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        String url = Config.MAIN_SERVER + "/v1/nodes";
        AndroidNetworking.get(url)
                .setPriority(com.androidnetworking.common.Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Config.CONNECTIONS = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JsonParser parser = new JsonParser();
                                JsonElement mJson = parser.parse(jsonArray.getString(i));
                                JsonObject m = mJson.getAsJsonObject();
                                JsonObject s = m.get("api").getAsJsonObject();
                                String addr = s.get("addr").getAsString();
                                String[] addrs = addr.split("/");
                                boolean ssl = s.get("ssl").getAsBoolean();
                                int port = s.get("port").getAsInt();
                                String protocol = ssl ? "https://" : "http://";
                                String url = protocol + addrs[0];
                                Connection con = new Connection(1, addr, port, ssl, url);
                                Config.CONNECTIONS.add(con);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Config.ConnectionsIsRead = true;
                        Log.d("SonoCoin Config", "Success");
                        latch.countDown();
                    }

                    @Override
                    public void onError(ANError error) {
                        Config.ConnectionsIsRead = true;
                        Log.d("SonoCoin Config", "there was an error: " + error.getMessage());
                        latch.countDown();
                    }
                });
        latch.await();
    }
}
