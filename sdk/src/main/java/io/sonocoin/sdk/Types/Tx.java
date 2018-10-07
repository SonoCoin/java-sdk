package io.sonocoin.sdk.Types;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
* Created by aantonov on 31.08.2017.
*/
public class Tx {

    @SerializedName("version")
    public int version;

    @SerializedName("lock_time")
    public int lock_time;

    @SerializedName("hash")
    public String hash;

    @SerializedName("tx_in")
    public ArrayList<Txin> tx_in;

    @SerializedName("tx_out")
    public ArrayList<Txout> tx_out;

}

