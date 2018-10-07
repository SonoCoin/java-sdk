package io.sonocoin.sdktestapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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

import static org.junit.Assert.*;

import io.sonocoin.sdk.Core.Coinservice;

import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DivideTest {
    private io.sonocoin.sdk.Types.Coin originalCoin;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        JsonRequestData.requestData().await();

        if (Config.CONNECTIONS.size() == 0) {
            Config.InitRemoteNodesConnections();
        }
    }

    public DivideTest(String hex, String key, int index, Integer amount)
            throws JSONException, InterruptedException {
        Item firstDecodedItem = new Item(hex, key, index, Long.valueOf(amount), 0, 1);
        originalCoin = firstDecodedItem.GetCoinFromRemoteNode();
    }

    @Parameterized.Parameters()
    public static Object[] DecodeAndEncodeParams() {
        //noinspection UnnecessaryLocalVariable
        Object[] ret = new Object[]{
                new Object[]{
                        "601e08357a729f780edace6e654d76b52f39bd2513424f3c11236891cfeabe8f",
                        "f35d175e27016f5aec4bf178a75fa5b5a9fefb6fd03d68b61eda087ec7a165a8",
                        1,
                        19900000,
                },
        };

        return ret;
    }

    @Test
    public void divide() throws JSONException, InterruptedException {
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE));
        assertEquals(PackageManager.PERMISSION_GRANTED,
                ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                        Manifest.permission.INTERNET));
        Coinservice instance = Coinservice.getInstance();
        ArrayList<Coin> coinList = new ArrayList<Coin>();
        assertNotNull(originalCoin);
        coinList.add(originalCoin);
        BigInteger[] bigOut;
        {
            ArrayList<BigInteger> bigOutsL = new ArrayList<BigInteger>();
            BigInteger fee;
            {
                BigDecimal dec = new BigDecimal(Config.COUNT_DECIMALS);
                fee = BigInteger.valueOf(Config.FEE_VALUE.multiply(dec).longValue());
            }

            BigInteger amountWithoutFee = originalCoin.amount.subtract(fee);
            BigInteger v1 = amountWithoutFee.divide(BigInteger.valueOf(2));
            bigOutsL.add(v1);
            {
                BigInteger d = amountWithoutFee.subtract(v1);
                bigOutsL.add(d);
            }

            bigOut = new BigInteger[bigOutsL.size()];
            for (int i = 0; i < bigOutsL.size(); i++) {
                bigOut[i] = bigOutsL.get(i);
            }
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final int outCount = bigOut.length;
        final int[] dividingCallbackCount = {0};
        instance.complex(
                coinList,
                bigOut,
                true,
                new DownloadCallback() {
                    @Override
                    public void onActionDone(List<Coin> coins) {
                        // At this step we MAY save all our coins to some database
                        Log.d("DivideTest", "DividingCallback.onActionDone");
                        assertNotNull(coins);

                        // Let's save them to local storage
                        int coinOutputCount = 0;
                        for (Coin coin : coins) {
                            if (coin.rel == 1) {
                                Log.d("DivideTest", String.format("Old coin `%s:%d`",
                                        coin.hash, coin.index));
                                continue;
                            }
                            String temporaryFilename = String.format("%s/divided-coin-%s_%d.json",
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
                            Log.d("DivideTest", String.format("Coin `%s:%d` saved to %s. Key: %s",
                                    coin.hash, coin.index, temporaryFilename, coin.secretkey));
                            coinOutputCount++;
                        }
                        assertEquals(outCount, coinOutputCount);
                        dividingCallbackCount[0]++;
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("DivideTest", "DividingCallback.onError");
                        latch.countDown();
                    }
                },
                new DownloadCallback() {
                    @Override
                    public void onActionDone(List<Coin> coins) {
                        // At this step we SHOULD save new state of our new coins
                        // These coins are the same as previous, on "DividingCallback.onActionDone"
                        Log.d("DivideTest", "PublishingCallback.onActionDone");
                        assertNotNull(coins);

                        for (Coin coin : coins) {
                            Log.d("DivideTest", String.format("Coin `%s:%d` has been updated from the Net",
                                    coin.hash, coin.index));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(String msg) {
                        Log.d("DivideTest", "PublishingCallback.onError");
                        latch.countDown();
                    }
                }
        );
        latch.await();
        assertEquals(1, dividingCallbackCount[0]);
    }


}
