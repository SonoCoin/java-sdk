package io.sonocoin.sdk.Sound;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Item;

public class SonoJsonSerializer {
    /**
     * Serialize Coin object to string
     *
     * @param coin Coin to serialize
     * @return serialized json string
     */
    public static String serializeCoin(Coin coin) {
        JsonObject object = new JsonObject();
        object.addProperty("hash", coin.hash);
        object.addProperty("secretkey", coin.secretkey);
        object.addProperty("index", coin.index);

        return object.toString();
    }

    /**
     * Serialize Item object to string
     *
     * @param item Item to serialize
     * @return serialized json string
     */
    public static String serializeItem(Item item) {
        JsonObject object = new JsonObject();
        object.addProperty("hash", item.hash);
        object.addProperty("secretkey", item.key);
        object.addProperty("index", item.index);

        return object.toString();
    }

    /**
     * @param input Serialized (to json) Coin/Item object
     * @return
     */
    public static Item deserializeItem(String input) throws JSONException {
        final JSONObject obj = new JSONObject(input);
        String hash = obj.getString("hash");
        String key = obj.getString("secretkey");
        int index = obj.getInt("index");
        Item item = new Item(hash, key, index, 0L, 0, 1);

        return item;
    }

    public static void saveStringToFile(String filename, String input) throws IOException {
        FileOutputStream stream = new FileOutputStream(filename);
        stream.write(input.getBytes());
        stream.close();
    }

    public static void saveCoinToFile(Coin coin, String filename) throws IOException {
        saveStringToFile(filename, serializeCoin(coin));
    }

    public static String loadStringFromFile(String filename) throws IOException {
        File file = new File(filename);
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        int readedBytes = in.read(bytes);
        if (readedBytes != length) {
            throw new IOException();
        }
        in.close();

        //noinspection UnnecessaryLocalVariable
        final String contents = new String(bytes);

        return contents;
    }

    public static Item loadItemFromFile(String filename) throws IOException, JSONException {
        final String input = loadStringFromFile(filename);
        return deserializeItem(input);
    }

}
