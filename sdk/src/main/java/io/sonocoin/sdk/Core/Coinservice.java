package io.sonocoin.sdk.Core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.encoders.Encoder;
import org.libsodium.jni.keys.PublicKey;

import okhttp3.RequestBody;
import io.sonocoin.sdk.Libs.FileUtils;
import io.sonocoin.sdk.R;
import io.sonocoin.sdk.Storage.CoinRepository;
import io.sonocoin.sdk.Storage.DbHelper;
import io.sonocoin.sdk.Types.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Coinservice {

    private static final String DEBUG_TAG = "SonoDebug";
    private static final String TAG = DEBUG_TAG;

    private static Coinservice coinservice;
    private Context context;
    private DbHelper dbHelper;
    private static Sodium SodiumInstance;

    public static Coinservice getInstance() {
        if (coinservice == null) {
            coinservice = new Coinservice();
        }
        return coinservice;
    }

    public static BigInteger stringToBigint(String item) {
        return new BigInteger(item);
    }

    public static Coin generateFee(BigInteger sum, String dt, boolean generateKeys) {
        long new_val;
        if (Config.FEE_TYPE == 1) {
            BigDecimal val = Config.FEE_VALUE;
            BigDecimal dec = new BigDecimal(Config.COUNT_DECIMALS);
            val = val.multiply(dec);
            new_val = val.longValue();
        } else {
            BigDecimal val = Config.FEE_VALUE;
            BigDecimal sumamount = new BigDecimal(sum);
            val = val.multiply(sumamount);
            BigDecimal dec = new BigDecimal(Config.COUNT_DECIMALS);
            val = val.multiply(dec);
            new_val = val.longValue();
        }
        BigInteger amount = BigInteger.valueOf(new_val);
        String amount_format = Coinservice.bigintToString(amount);
        Coin coinout = new Coin(0, amount, generateHash(), 0, Coin.CoinEntry.TYPE_SOUND, Coin.CoinEntry.STATUS_WAIT, dt, amount_format, "", "", 0, Config.COIN_ENCRYPTED_METHOD_DEFAULT, Config.COIN_DEFAULT_VERSION);
        try {
            if (generateKeys) {
                coinout = generateKeys(coinout);
            }
            return coinout;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Coin generateFee(BigInteger sum, String dt) {
        return generateFee(sum, dt, false);
    }

    public static String bigintToString(BigInteger item) {
        return item.toString();
    }

    public static String bigdecToString(BigDecimal item) {
        return item.toString();
    }

    /**
     * Combine / Divide coins
     *
     * @param coinsList        List of old coins, which will be deleted after operation
     * @param bigOuts          Dividing sums
     * @param dividingCallback Promise executed BEFORE publishing on the Net
     * @param publishCallback  Promise executed after publishing on the Net
     * @param isDivide         This is DIVIDE method, not combine
     * @throws JSONException
     */
    public void complex(
            final List<Coin> coinsList,
            final BigInteger[] bigOuts,
            final boolean isDivide,
            final DownloadCallback dividingCallback,
            final DownloadCallback publishCallback
    ) throws JSONException {
        final ArrayList<Out> outsInput = new ArrayList<Out>();
        final List<Coin> old_coins = new ArrayList<>();

        for (Coin c : coinsList) {
            Out out = new Out();
            out.hash = c.hash;
            out.index = c.index;
            out.key = c.secretkey; // We MUST set this field here, because we don't use repository (DB)
            outsInput.add(out);
        }
        // update status of coins from server
        JsonGetSQLiteStatus.getStatus(null, outsInput, context, new DownloadCallback() {
            @Override
            public void onActionDone(List<Coin> coins) {
                //update coins (array for update in SQLite)
                BigInteger inputCoinSum = BigInteger.ZERO;

                coins = new ArrayList<Coin>();
                ArrayList<Txout> txOutList = new ArrayList<Txout>();

                // first coin
                Coin first = null;
                for (Coin coin : coinsList) {
                    if (coin == null ||
                            !(coin.status == Coin.CoinEntry.STATUS_READY || coin.status == Coin.CoinEntry.STATUS_WAIT ||
                                    coin.status == Coin.CoinEntry.STATUS_FORISSUE)) {
                        FileUtils.dump("coin not found");
                        dividingCallback.onError("coin not found");
                        return;
                    }
                    inputCoinSum = inputCoinSum.add(coin.amount);
                    coin.status = Coin.CoinEntry.STATUS_WAIT;// OLD;
                    coin.rel = 1;
                    coins.add(coin);
                    old_coins.add(coin);
                    if (first == null) {
                        first = coin;
                    }
                }

                Tx tx = new Tx();
                Date date = new Date();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dt = simpleDate.format(date);
                tx.lock_time = 0;
                tx.version = Config.VERSION;

                // generate fee
                Coin transactionFee = generateFee(inputCoinSum, dt);
                try {
                    txOutList.add(generateTxOut(transactionFee, context));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // check fee
                BigInteger changeAmount = inputCoinSum.subtract(transactionFee.amount);
                BigInteger inputCoinSumCheck = BigInteger.ZERO;

                if (changeAmount.compareTo(BigInteger.ZERO) <= 0) {
                    FileUtils.dump("We don't have enough amount of money. complex sum < 0: " + changeAmount.toString());
                    dividingCallback.onError(context.getResources().getString(R.string.commission_error));
                    return;
                }

                // generate txout
                int iterator = 0;
                for (BigInteger bigOut : bigOuts) {
                    if (bigOut.compareTo(BigInteger.ZERO) <= 0) {
                        dividingCallback.onError("One or more outputs less or equal to 0");
                        return;
                    }

                    iterator++;
                    Coin coinOut;
                    try {
                        coinOut = first.clone();
                    } catch (CloneNotSupportedException e) {
                        FileUtils.dump("divide clone err: " + e.getMessage());
                        e.printStackTrace();
                        dividingCallback.onError("divide clone err: " + e.getMessage());
                        return;
                    }
                    coinOut.id = 0;
                    coinOut.rel = 0;
                    coinOut.status = Coin.CoinEntry.STATUS_WAIT;
                    BigInteger ins_value = bigOut;
                    if (iterator == 1 && !isDivide) {
                        ins_value = ins_value.subtract(transactionFee.amount);
                    }
                    coinOut.amount = ins_value;
                    coinOut.amount_format = Coinservice.bigintToString(ins_value);
                    coinOut.index = iterator;
                    coinOut.dt = dt;
                    coinOut.hash = "";

                    try {
                        coinOut = generateKeys(coinOut);
                    } catch (Exception e) {
                        FileUtils.dump("complex generateKeys err: " + e.getMessage());
                        e.printStackTrace();
                        dividingCallback.onError("complex generateKeys err: " + e.getMessage());
                        return;
                    }
                    txOutList.add(generateTxOut(coinOut, context));
                    coins.add(coinOut);
                    inputCoinSumCheck = inputCoinSumCheck.add(ins_value);
                }

                if (!inputCoinSumCheck.equals(changeAmount)) {
                    dividingCallback.onError(null);
                    return;
                }
                tx.tx_out = txOutList;

                // generate txin
                ArrayList<Txin> txInList = new ArrayList<Txin>();
                for (Coin old_coin : old_coins) {
                    try {
                        txInList.add(generateTxIn(old_coin, txOutList));
                    } catch (IOException e) {
                        FileUtils.dump("merge generateTxIn err: " + e.getMessage());
                        e.printStackTrace();
                        dividingCallback.onError(null);
                        return;
                    }
                }
                tx.tx_in = txInList;

                // generate hash
                String newHash;
                try {
                    newHash = generateTxHash(txInList, txOutList);
                } catch (IOException | NoSuchAlgorithmException e) {
                    FileUtils.dump("complex generateTxHash err: " + e.getMessage());
                    dividingCallback.onError(null);
                    return;
                }
                tx.hash = newHash;
                for (Coin co : coins) {
                    if (co.hash.isEmpty()) {
                        co.hash = newHash;
                    }
                }

                dividingCallback.onActionDone(coins);
                Publish(coins, tx, publishCallback);
            }

            @Override
            public void onError(String msg) {
                publishCallback.onError(msg);
                FileUtils.dump("merge err: " + msg);
            }
        }, false);
    }

    public static String generateHash() {
        return "";
    }

    private static void InitSodium() {
        if (SodiumInstance == null) {
            Log.d(TAG, "InitSodium()");
            SodiumInstance = NaCl.sodium();
        }
    }

    public static Coin generateKeys(Coin coin) {
        InitSodium();

        byte[] publicKey = new byte[com.jackwink.libsodium.jni.SodiumConstants.CRYPTO_SIGN_PUBLICKEYBYTES];
        byte[] secretKey = new byte[com.jackwink.libsodium.jni.SodiumConstants.CRYPTO_SIGN_SECRETKEYBYTES];
        Sodium.crypto_sign_keypair(publicKey, secretKey);

        secretKey = Arrays.copyOfRange(secretKey, 0, 32);
        coin.pk_script = Encoder.HEX.encode(publicKey);
        coin.secretkey = Encoder.HEX.encode(secretKey);
        coin.pk_script_length = coin.pk_script.length();
        return coin;
    }

    public static Txout generateTxOut(Coin coin, Context context) {
        Txout txout = new Txout();
        txout.index = coin.index;
        txout.value = coin.amount;
        txout.pk_script = coin.pk_script;
        txout.pk_script_length = txout.pk_script.length() / 2; // because hex

        return txout;
    }

    public static Txin generateTxIn(Coin coin, ArrayList<Txout> txOutList) throws IOException {
        Txin txin = new Txin();
        txin.sequence = Config.VERSION;

        /* generate sign */
        byte[] hash = Encoder.HEX.decode(coin.hash);

        int version = Config.VERSION;
        int locktime = 0;
        byte[][] txOutSlices = new byte[txOutList.size()][];
        int i = 0;
        for (Txout out : txOutList) {
            byte[] outindex = intToByteArray(out.index);
            byte[] out_value = bigintToByteArray(out.value);
            PublicKey pk = new PublicKey(out.pk_script);
            byte[] slis = ConcatByteArray(new byte[][]{
                    outindex, // 4
                    out_value, // 8
                    pk.toBytes(), // ScriptLength
            });
            txOutSlices[i] = slis;
            i++;
        }
        byte[] txOutSlice = ConcatByteArray(txOutSlices);
        String tx = bytesToHex(txOutSlice);
        byte[] l = intToByteArray(locktime);
        byte[][] txSlices = new byte[][]{
                hash, // 32
                intToByteArray(coin.index), // 4
                intToByteArray(version), // 4
                intToByteArray(locktime), // 4
                txOutSlice,
        };
        byte[] msg = ConcatByteArray(txSlices);
        byte[] sign = new byte[SodiumConstants.SIGNATURE_BYTES];
        if (coin.secretkey.length() > 64) {
            coin.secretkey = coin.secretkey.substring(0, coin.secretkey.length() / 2);
        }

        byte[] secretKey = Encoder.HEX.decode(coin.secretkey + coin.pk_script);
        int[] len = new int[0];
        if (Sodium.crypto_sign_detached(sign, len, msg, msg.length, secretKey) != 0) {
            return null;
        }
        txin.signature_script = Encoder.HEX.encode(sign);
        txin.script_length = txin.signature_script.length() / 2; // because hex
        Output out = new Output(coin.index, coin.amount, coin.hash);
        txin.previous_output = out;
        return txin;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String generateTxHash(ArrayList<Txin> txinlist, ArrayList<Txout> txoutlist) throws IOException, NoSuchAlgorithmException {
        byte[][] txInSlices = new byte[txinlist.size()][];
        int i = 0;
        for (Txin in : txinlist) {
            byte[] pohash = Encoder.HEX.decode(in.previous_output.hash);
            byte[] pout_value = bigintToByteArray(in.previous_output.value);
            byte[][] slices = new byte[][]{
                    pohash, // 32
                    intToByteArray(in.previous_output.index), // 4
                    pout_value, // 8
            };
            byte[] slice = ConcatByteArray(slices);
            txInSlices[i] = ConcatByteArray(new byte[][]{
                    slice, // 44
                    intToByteArray(in.sequence), // 4
                    // intToByteArray8(in.script_length), // 8
                    Encoder.HEX.decode(in.signature_script), // ScriptLength
            });
            i++;
        }
        byte[] txInSlice = ConcatByteArray(txInSlices);
        byte[][] txOutSlices = new byte[txoutlist.size()][];
        i = 0;
        for (Txout out : txoutlist) {
            byte[] out_value = bigintToByteArray(out.value);
            PublicKey pk = new PublicKey(out.pk_script);
            txOutSlices[i] = ConcatByteArray(new byte[][]{
                    intToByteArray(out.index), // 4
                    out_value, // 8
                    pk.toBytes(), // ScriptLength
            });
            i++;
        }
        byte[] txOutSlice = ConcatByteArray(txOutSlices);

        byte[][] txSlices = new byte[][]{
                intToByteArray(Config.VERSION),// 4
                intToByteArray(0),// 4
                txInSlice,
                txOutSlice,
        };
        byte[] msg = ConcatByteArray(txSlices);
        //noinspection UnnecessaryLocalVariable
        String dhash = Dhash(msg);
        return dhash; // 32
    }

    /**
     * Publish transaction to remote server
     *
     * @param coinsToReturn
     * @param tx
     * @param downloadCallback
     */
    public void Publish(final List<Coin> coinsToReturn, Tx tx, final DownloadCallback downloadCallback) {
        for (Connection connection : Config.CONNECTIONS) {
            String url = connection.getFullUrl() + Config.ACTION_TX_PUBLISH;
            JSONObject object;
            String json_string;
            try {
                GsonBuilder builder = new GsonBuilder();
                json_string = builder.create().toJson(tx, Tx.class);
                object = new JSONObject(json_string);
            } catch (JSONException e) {
                FileUtils.dump("publish: Error: " + e.getMessage());
                e.printStackTrace();
                downloadCallback.onError(null);
                return;
            }
            String obj_string = object.toString();
            Log.d(DEBUG_TAG, obj_string);
            AndroidNetworking.post(url)
                    .addStringBody(json_string)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            downloadCallback.onActionDone(coinsToReturn);
                        }

                        @Override
                        public void onError(ANError anError) {
                            RequestBody body = anError.getResponse().request().body();
                            FileUtils.dump("publish: POST: BODY ERROR: " + body.toString());
                            downloadCallback.onError(body.toString());
                        }
                    });
            break;
        }
    }

    public static byte[] ConcatByteArray(byte[][] slices) {
        int totalLen = 0;
        for (byte[] b : slices) {
            totalLen += b.length;
        }
        byte[] tmp = new byte[totalLen];
        int pos = 0;
        for (byte[] b : slices) {
            System.arraycopy(b, 0, tmp, pos, b.length);
            pos += b.length;
        }
        return tmp;
    }

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        ret[2] = (byte) ((a >> 16) & 0xFF);
        ret[3] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static byte[] int16ToByteArray(int a) {
        byte[] ret = new byte[2];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        return ret;
    }

    public static byte[] bigintToByteArray(BigInteger a) {
        Integer size = 8;
        byte[] b = new byte[size];
        long l = a.longValue();
        for (int i = 0; i < size; ++i) {
            b[i] = (byte) (l >> (size - i - 1 << 3));
        }
        byte[] b_new = new byte[size];
        for (int i = 0; i < size; ++i) {
            b_new[i] = b[size - i - 1];
        }
        return b_new;
    }

    public static String Dhash(byte[] slice) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(slice);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
        }
        String hashstr = sb.toString();
        MessageDigest digest2 = MessageDigest.getInstance("SHA-256");
        byte[] slice2 = Encoder.HEX.decode(hashstr);
        byte[] hash2 = digest2.digest(slice2);
        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < hash2.length; i++) {
            sb2.append(Integer.toString((hash2[i] & 0xff) + 0x100, 16).substring(1));
        }
        String hashstr2 = sb2.toString();
        return hashstr2;
    }

    public void Delete(final Coin c, final DownloadCallback downloadCallback) {
        final CoinRepository repo = new CoinRepository(dbHelper);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        Coin coin = repo.getByHash(db, c.hash, c.index); // void Delete
        if (coin == null) {
            downloadCallback.onError(null);
            return;
        }
        if (coin.status == Coin.CoinEntry.STATUS_OLD) {
            List<Coin> coins = new ArrayList<Coin>();
            coin.status = Coin.CoinEntry.STATUS_DEL;// OLD;
            coin.rel = 0;
            coins.add(coin);
            repo.updateList(db, coins, context, downloadCallback);
        }
    }

    public void deleteCoins(final List<Coin> coins, final DownloadCallback downloadCallback) {
        final CoinRepository repo = new CoinRepository(dbHelper);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Coin> coinsResult = new ArrayList<>();

        for (Coin c : coins) {
            Coin coin = repo.getByHash(db, c.hash, c.index); // deleteCoins
            if (coin == null) {
                downloadCallback.onError(null);
                return;
            }
            if (coin.status == Coin.CoinEntry.STATUS_OLD) {
                coin.status = Coin.CoinEntry.STATUS_DEL;// OLD;
                coin.rel = 0;
                coinsResult.add(coin);
            }
        }
        repo.updateList(db, coinsResult, context, downloadCallback);
    }

    public void getList(DownloadCallback downloadCallback) {
        CoinRepository coinRepo = new CoinRepository(dbHelper);
        coinRepo.checkTableExists();
        // get current list and check status from server
        coinRepo.getList(downloadCallback, true, null, null);
    }

    public void init(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }
}