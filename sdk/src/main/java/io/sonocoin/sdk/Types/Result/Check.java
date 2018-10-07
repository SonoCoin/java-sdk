package io.sonocoin.sdk.Types.Result;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

/**
* Created by aantonov on 31.08.2017.
*/
public class Check {

    @SerializedName("id")
    public int id;

    @SerializedName("is_new")
    public boolean is_new;

    @SerializedName("amount_format")
    public String amount_format;

    @SerializedName("block")
    public String block;

    @SerializedName("confirimed_timestamp")
    public int confirimed_timestamp;

    @SerializedName("confirmed")
    public int confirmed;

    @SerializedName("error")
    public String error;

    @SerializedName("hash")
    public String hash;

    @SerializedName("secret_key")
    public String secret_key;

    @SerializedName("index")
    public int index;

    @SerializedName("lock_time")
    public int lock_time;

    @SerializedName("pk_script")
    public String pk_script;

    @SerializedName("status")
    public int status;

    @SerializedName("value")
    public BigInteger value;

    @SerializedName("version")
    public int version;

    // @SerializedName("encrypted_method")
    public int encrypted_method;

    // @SerializedName("coin_version")
    public int coin_version;

    @Override
    public String toString() {
        String resp = "";
        resp = "hash = " + this.hash +
               "; index = " + this.index +
               "; pk_script = " + this.pk_script +
               "; error = " + this.error +
               "; value = " + this.value;
        return resp;
    }

}

