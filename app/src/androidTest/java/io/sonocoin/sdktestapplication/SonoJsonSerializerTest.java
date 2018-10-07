package io.sonocoin.sdktestapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.ContextCompat;

import org.json.JSONException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;

import io.sonocoin.sdk.Sound.SonoJsonSerializer;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Item;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SonoJsonSerializerTest {
    protected String hash;
    protected String key;
    protected int index;
    protected Item item;

    public SonoJsonSerializerTest(String hash, String key, int index) {
        this.hash = hash;
        this.key = key;
        this.index = index;

        item = new Item(hash, key, index, 0L, 0, 1);
    }

    @Parameterized.Parameters()
    public static Object[] DecodeAndEncodeParams() {
        //noinspection UnnecessaryLocalVariable
        Object[] ret = new Object[]{
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "55066010e308d7db19a906d08e8cfad73205fc67ab1286668454bb6a565aee7a",
                        "6da988b3e7e3614922e14e699d16051f05d203ac4d742e541bb0d57ace62609a",
                        1,
                },
                new Object[]{
                        "601e08357a729f780edace6e654d76b52f39bd2513424f3c11236891cfeabe8f",
                        "f35d175e27016f5aec4bf178a75fa5b5a9fefb6fd03d68b61eda087ec7a165a8",
                        1,
                },
        };

        return ret;
    }

    @Test
    public void serialize() throws JSONException, InterruptedException, IOException {
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE));
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.INTERNET));
        {
            // Serialize Item

            // Serialize
            String serialized = SonoJsonSerializer.serializeItem(item);
            assertNotNull(serialized);
            assertNotEquals(serialized, "");

            // Deserialize
            Item newItem = SonoJsonSerializer.deserializeItem(serialized);
            assertEquals(this.hash, newItem.hash);
            assertEquals(this.key, newItem.key);
            assertEquals(this.index, newItem.index);
        }
        Coin coin = item.GetCoinFromRemoteNode();
        assertNotNull(coin);

        {
            // Serialize Coin to local string

            // Serialize
            String serialized = SonoJsonSerializer.serializeCoin(coin);
            assertNotNull(serialized);
            assertNotEquals(serialized, "");

            // Deserialize
            Item newItem = SonoJsonSerializer.deserializeItem(serialized);
            assertEquals(this.hash, newItem.hash);
            assertEquals(this.key, newItem.key);
            assertEquals(this.index, newItem.index);
        }

        {
            // Serialize Coin to file

            String temporaryFilename = String.format("%s/sonocoin-sdk-test.json.tmp",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            );

            // Serialize
            SonoJsonSerializer.saveCoinToFile(coin, temporaryFilename);

            File temporaryFile = new File(temporaryFilename);
            assertTrue(temporaryFile.exists());
            assertTrue(temporaryFile.canRead());
            temporaryFile.deleteOnExit();

            // Deserialize
            Item newItem = SonoJsonSerializer.loadItemFromFile(temporaryFilename);
            assertEquals(this.hash, newItem.hash);
            assertEquals(this.key, newItem.key);
            assertEquals(this.index, newItem.index);
        }
    }
}
