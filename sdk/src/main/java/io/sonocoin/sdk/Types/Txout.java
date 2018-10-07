package io.sonocoin.sdk.Types;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

/**
* Created by aantonov on 31.08.2017.
*/
public class Txout {

    @SerializedName("index")
    public int index;

    @SerializedName("value")
    public BigInteger value;

    @SerializedName("pk_script_length")
    public int pk_script_length;

    @SerializedName("pk_script")
    public String pk_script;
}

