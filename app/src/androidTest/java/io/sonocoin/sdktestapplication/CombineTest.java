package io.sonocoin.sdktestapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import io.sonocoin.sdk.Core.Coinservice;
import io.sonocoin.sdk.Core.Config;
import io.sonocoin.sdk.Core.DownloadCallback;
import io.sonocoin.sdk.Core.JsonRequestData;
import io.sonocoin.sdk.Sound.SonoJsonSerializer;
import io.sonocoin.sdk.Types.Coin;
import io.sonocoin.sdk.Types.Item;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CombineTest {
    private ArrayList<Coin> originalCoins;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        JsonRequestData.requestData().await();

        if (Config.CONNECTIONS.size() == 0) {
            Config.InitRemoteNodesConnections();
        }
    }

    public CombineTest(ArrayList<Item> items) throws JSONException, InterruptedException {
        originalCoins = new ArrayList<>();
        for (Item item : items) {
            originalCoins.add(item.GetCoinFromRemoteNode());
        }
    }

    @Parameterized.Parameters()
    public static Object[] DecodeAndEncodeParams() {
        ArrayList<ArrayList<Item>> retRaw = new ArrayList<ArrayList<Item>>();
        {
            ArrayList<Item> set = new ArrayList<>();
            Item item = new Item("8a9b8d1f08ef774e43da9e0ec6bd05e81ea64c715163550dd7c641d9b430ed3e",
                    "db758d1349b676e1723884bae554c337dfd4792c997cbee8bf8fa4d1d8b9fe81", 1, 9450000L, 0, 1);
            set.add(item);

            item = new Item("8a9b8d1f08ef774e43da9e0ec6bd05e81ea64c715163550dd7c641d9b430ed3e",
                    "51756becd6519f7eaa3ca707b16d901a9a61e4736d4b625f87eb78eca8812ca4", 2, 9450000L, 0, 1);
            set.add(item);
            retRaw.add(set);
        }

        Object[] ret = new Object[retRaw.size()];
        for (int i = 0; i < retRaw.size(); i++) {
            ret[i] = retRaw.get(i);
        }

        return ret;
    }

    @Test
    public void combine() throws JSONException, InterruptedException {
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE));
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.INTERNET));
        Coinservice instance = Coinservice.getInstance();
        BigInteger[] bigOut;
        {
            BigInteger fee;
            {
                BigDecimal dec = new BigDecimal(Config.COUNT_DECIMALS);
                fee = BigInteger.valueOf(Config.FEE_VALUE.multiply(dec).longValue());
            }

            BigInteger amountWithoutFee = BigInteger.ZERO.subtract(fee);
            for (Coin coin : originalCoins) {
                assertNotNull(coin);
                amountWithoutFee = amountWithoutFee.add(coin.amount);
            }

            bigOut = new BigInteger[]{amountWithoutFee};
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final int[] dividingCallbackCount = {0};
        instance.complex(
                originalCoins,
                bigOut,
                true,
                new DownloadCallback() {
                    @Override
                    public void onActionDone(List<Coin> coins) {
                        // At this step we MAY save all our coins to some database
                        Log.d("CombineTest", "DividingCallback.onActionDone");
                        assertNotNull(coins);

                        // Let's save them to local storage
                        int coinOutputCount = 0;
                        for (Coin coin : coins) {
                            if (coin.rel == 1) {
                                Log.d("CombineTest", String.format("Old coin `%s:%d`",
                                        coin.hash, coin.index));
                                continue;
                            }
                            String temporaryFilename = String.format("%s/combined-coin-%s_%d.json",
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    coin.hash, coin.index
                            );
                            try {
                                SonoJsonSerializer.saveCoinToFile(coin, temporaryFilename);
                            } catch (IOException e) {
                                e.printStackTrace();
                                assertNull(e);
                            }
                            File temporaryFile = new File(temporaryFilename);
                            assertTrue(temporaryFile.exists());
                            assertTrue(temporaryFile.canRead());
                            Log.d("CombineTest", String.format("Coin `%s:%d` saved to %s. Key: %s",
                                    coin.hash, coin.index, temporaryFilename, coin.secretkey));
                            coinOutputCount++;
                        }
                        assertEquals(1, coinOutputCount);
                        dividingCallbackCount[0]++;
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("CombineTest", "DividingCallback.onError");
                        latch.countDown();
                    }
                },
                new DownloadCallback() {
                    @Override
                    public void onActionDone(List<Coin> coins) {
                        // At this step we SHOULD save new state of our new coins
                        // These coins are the same as previous, on "DividingCallback.onActionDone"
                        Log.d("CombineTest", "PublishingCallback.onActionDone");
                        assertNotNull(coins);

                        for (Coin coin : coins) {
                            Log.d("CombineTest", String.format("Coin `%s:%d` has been updated from the Net",
                                    coin.hash, coin.index));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("CombineTest", "PublishingCallback.onError");
                        latch.countDown();
                    }
                }
        );
        latch.await();
        assertEquals(1, dividingCallbackCount[0]);
    }


}
