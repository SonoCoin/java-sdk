package io.sonocoin.sdk.Types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
* Created by sciner on 20.10.2017.
*/
public class CoinExport {

    @SerializedName("index")
    public int index;

    @SerializedName("hash")
    public String hash;

    @SerializedName("secretkey")
    public String secretkey;

    @SerializedName("encrypted_method")
    public Integer encrypted_method;

    @SerializedName("coin_version")
    public Integer coin_version;

    public CoinExport() {
    }

    @JsonCreator
    public CoinExport(@JsonProperty("index") int index,
                      @JsonProperty("hash") String hash,
                      @JsonProperty("secretkey") String secretkey,
                      @JsonProperty("encrypted_method") int encrypted_method,
                      @JsonProperty("coin_version") int coin_version
                      ) {
        this.index = index;
        this.hash = hash;
        this.secretkey = secretkey;
        this.encrypted_method = encrypted_method;
        this.coin_version = coin_version;
    }

    public CoinExport(String hash, int index, String secretkey, int encrypted_method, int coin_version) {
        this.hash = hash;
        this.index = index;
        this.secretkey = secretkey;
        this.encrypted_method = encrypted_method;
        this.coin_version = coin_version;
    }

}
