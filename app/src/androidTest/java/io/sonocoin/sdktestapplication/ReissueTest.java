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
public class ReissueTest {
    private ArrayList<Coin> originalCoins;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        JsonRequestData.requestData().await();

        if (Config.CONNECTIONS.size() == 0) {
            Config.InitRemoteNodesConnections();
        }
    }

    public ReissueTest(ArrayList<Item> items) throws JSONException, InterruptedException {
        originalCoins = new ArrayList<>();
        for (Item item : items) {
            originalCoins.add(item.GetCoinFromRemoteNode());
        }
    }

    @Parameterized.Parameters()
    public static Object[] ReissueParams() {
        ArrayList<ArrayList<Item>> retRaw = new ArrayList<ArrayList<Item>>();
        {
            ArrayList<Item> set = new ArrayList<>();
            Item item = new Item("153d15c4ef5adb456eb71c20df0994c0853a9606c463da31c4361c2949d2e60f",
                    "33e5c44e62fdd22ddc8f86706be3f23ba6b6a3896a3a71afcd86d477e694dce5", 1, 9450000L, 0, 1);
            set.add(item);
            retRaw.add(set);
        }
        {
            ArrayList<Item> set = new ArrayList<>();
            Item item = new Item("1b748f1eda86c090c74bd303025b7516365e3a56e53b3770e728012ecafd7122",
                    "a2aa4709e2ad82f05d09b437e21dba7e1c51c40bcfd73699509b5d581ddea0ee", 1, 9450000L, 0, 1);
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
    public void reissue() throws JSONException, InterruptedException {
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
                        Log.d("ReissueTest", "DividingCallback.onActionDone");
                        assertNotNull(coins);

                        // Let's save them to local storage
                        int coinOutputCount = 0;
                        for (Coin coin : coins) {
                            if (coin.rel == 1) {
                                Log.d("ReissueTest", String.format("Old coin `%s:%d`",
                                        coin.hash, coin.index));
                                continue;
                            }
                            String temporaryFilename = String.format("%s/reissued-coin-%s_%d.json",
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
                            Log.d("ReissueTest", String.format("Coin `%s:%d` saved to %s. Key: %s",
                                    coin.hash, coin.index, temporaryFilename, coin.secretkey));
                            coinOutputCount++;
                        }
                        assertEquals(1, coinOutputCount);
                        dividingCallbackCount[0]++;
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("ReissueTest", "DividingCallback.onError");
                        latch.countDown();
                    }
                },
                new DownloadCallback() {
                    @Override
                    public void onActionDone(List<Coin> coins) {
                        // At this step we SHOULD save new state of our new coins
                        // These coins are the same as previous, on "DividingCallback.onActionDone"
                        Log.d("ReissueTest", "PublishingCallback.onActionDone");
                        assertNotNull(coins);

                        for (Coin coin : coins) {
                            Log.d("ReissueTest", String.format("Coin `%s:%d` has been updated from the Net",
                                    coin.hash, coin.index));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("ReissueTest", "PublishingCallback.onError");
                        latch.countDown();
                    }
                }
        );
        latch.await();
        assertEquals(1, dividingCallbackCount[0]);
    }


}
