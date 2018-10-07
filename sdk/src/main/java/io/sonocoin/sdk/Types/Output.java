package io.sonocoin.sdk.Types;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

/**
 * Created by aantonov on 31.08.2017.
 */
public class Output {

    @SerializedName("index")
    public int index;

    @SerializedName("hash")
    public String hash;

    @SerializedName("value")
    public BigInteger value;

    public Output(int index, BigInteger value, String hash) {
        this.value = value;
        this.hash = hash;
        this.index = index;
    }

}

