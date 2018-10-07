package io.sonocoin.sdktestapplication;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import io.sonocoin.sdk.Core.Coinservice;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CoinServiceTest {
    @Test
    public void createSingleton() {
        Coinservice c = Coinservice.getInstance();
        assertNotNull(c);

        // Singleton test
        Coinservice c1 = Coinservice.getInstance();
        assertNotNull(c1);
        assertSame(c, c1);
    }

    @Test
    public void SodiumNaCl() {
        org.libsodium.jni.NaCl.sodium();
        assertTrue(true);
    }
}
