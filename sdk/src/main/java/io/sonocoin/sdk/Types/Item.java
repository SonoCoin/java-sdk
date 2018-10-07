package io.sonocoin.sdk.Types;

import com.google.gson.GsonBuilder;
import io.sonocoin.sdk.Core.Coinservice;
import io.sonocoin.sdk.Core.Config;
import io.sonocoin.sdk.Libs.ArrayUtils;
import io.sonocoin.sdk.Types.Result.Check;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.GsonBuilder;

/**
 * Created by sciner on 02.09.2017.
 */
public class Item {

    public String key;
    public String hash;
    public int index;
    public Long amount;
    // extended fields
    public int encrypted_method;
    public int coin_version;

    private Item() {
        this.coin_version = Config.COIN_DEFAULT_VERSION;
    }

    public Item(String hash, String key, int index, Long amount, int encrypted_method, int coin_version) {
        this.hash = hash;
        this.key = key;
        this.index = index;
        this.amount = amount;
        this.encrypted_method = encrypted_method;
        this.coin_version = coin_version;
    }

    /**
     * Parse a string to an Item object
     *
     * @param msg
     * @return Item
     */
    public static Item parse(String msg) {
        int len = msg.length();
        byte[] msg_bytes = new byte[len];
        for (int i = 0; i < len; ++i) {
            char c = msg.charAt(i);
            msg_bytes[i] = (byte) c;
        }
        return Item.parse(msg_bytes);
    }

    public static Item parse(byte[] bytes) {
        int minSize = 68; // minimal coin size (without header)
        int headerSize = 5; // Header size
        int skSize = 32; // Size of secret key
        int txSize = 32;
        int indexSize = 4; // index size
        int offset = 0; // the beggining of the body of Coin object

        if (bytes.length < minSize) {
            return null;
        }

        byte[] sign = new byte[2]; // Two bytes with the abbreviatura of SonoCoin: SC
        byte[] version = new byte[2]; // Number of version of this coin object
        byte[] encrypted_method = new byte[1]; // Method of encryption. 0 = without encryption
        byte[] sk;  // Secret key
        byte[] tx = new byte[txSize];
        byte[] index = new byte[indexSize];

        Item item = new Item();

        if (bytes.length >= minSize + headerSize) {
            System.arraycopy(bytes, 0, sign, 0, 2);
            System.arraycopy(bytes, 2, version, 0, 2);
            System.arraycopy(bytes, 4, encrypted_method, 0, 1);
            // SC
            if ((sign[0] != 83) || (sign[1] != 57)) {
                return null;
            }
            item.encrypted_method = (int) encrypted_method[0];
            item.coin_version = (version[0] & 0xff) | ((version[1] & 0xff) << 8);
            if (item.coin_version < 1) {
                item.coin_version = Config.COIN_DEFAULT_VERSION;
            }
            skSize = bytes.length - headerSize - txSize - indexSize;
            offset = headerSize;
        }

        sk = new byte[skSize];

        //noinspection PointlessArithmeticExpression
        System.arraycopy(bytes, offset + 0, sk, 0, skSize);
        System.arraycopy(bytes, offset + skSize, tx, 0, txSize);
        System.arraycopy(bytes, offset + skSize + txSize, index, 0, indexSize);

        item.key = ArrayUtils.bytesToHex(sk);
        item.hash = ArrayUtils.bytesToHex(tx);
        item.index = java.nio.ByteBuffer.wrap(index).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

        return item;

    }

    /**
     * Get Coin object from Item object from remote server, which has been declared in Config.CONNECTIONS
     *
     * @return
     * @throws JSONException
     * @throws InterruptedException
     */
    public Coin GetCoinFromRemoteNode() throws JSONException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        if (Config.CONNECTIONS.size() == 0) {
            Config.InitRemoteNodesConnections();
        }

        JSONObject jo = new JSONObject();
        Collection<JSONObject> items = new ArrayList<>();
        final HashMap<String, Item> item_map = new HashMap<>();
        {
            JSONObject item1 = new JSONObject();
            item1.put("hash", this.hash);
            item1.put("index", this.index);
            items.add(item1);
            item_map.put(this.hash + ":" + this.index, this);
        }
        jo.put("outs", new JSONArray(items));
        final ArrayList<ArrayList<Check>> checks = new ArrayList<>();
        //noinspection LoopStatementThatDoesntLoop
        for (Connection con : Config.CONNECTIONS) {
            String url = con.getFullUrl() + Config.ACTION_TX_CHECK;
            AndroidNetworking
                    .post(url)
                    .addJSONObjectBody(jo)
                    .setPriority(com.androidnetworking.common.Priority.MEDIUM)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        public void onResponse(JSONArray response) {
                            ArrayList<Check> oChecks = new ArrayList<>();
                            checks.add(oChecks);
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject o = (JSONObject) response.get(i);
                                    GsonBuilder builder = new GsonBuilder();
                                    Check check = builder.create().fromJson(o.toString(), Check.class);
                                    if (check != null) {
                                        Item item = item_map.get(check.hash + ":" + check.index);
                                        if (item != null) {
                                            check.encrypted_method = item.encrypted_method;
                                            check.coin_version = item.coin_version;
                                            check.secret_key = item.key;
                                        }
                                    }
                                    oChecks.add(check);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // checks
                            latch.countDown();
                        }

                        @Override
                        public void onError(ANError error) {
                            latch.countDown();
                        }
                    });
            break;
        }

        latch.await();

        for (Check elem : checks.get(0)) {
            if ((elem.error == null || elem.error.equals("") || elem.error.equals("Transaction out did spend")) &&
                    (elem.pk_script != null && !elem.pk_script.equals(""))) {
                Date date = new Date();
                java.text.SimpleDateFormat simpleDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dt = simpleDate.format(date);
                java.math.BigDecimal bigDecY = new java.math.BigDecimal(String.valueOf(Config.COUNT_DECIMALS));
                java.math.BigDecimal sum = new java.math.BigDecimal(elem.value);
                //noinspection BigDecimalMethodWithoutRoundingCalled
                java.math.BigDecimal result = sum.divide(bigDecY);
                elem.amount_format = Coinservice.bigdecToString(result);
                //noinspection UnnecessaryLocalVariable
                Coin newCoin = new Coin(0, elem.value, elem.hash, elem.index, 1,
                        Coin.CoinEntry.STATUS_FORISSUE, dt, elem.amount_format,
                        elem.secret_key, elem.pk_script, 0, elem.encrypted_method, elem.coin_version);
                return newCoin;
            }
        }

        return null;
    }
}
