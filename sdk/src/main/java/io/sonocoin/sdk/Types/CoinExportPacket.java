package io.sonocoin.sdk.Types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
* Created by sciner on 21.10.2017.
*/
public class CoinExportPacket {

    @SerializedName("list")
    public ArrayList<CoinExport> list = new ArrayList<CoinExport>();

    public CoinExportPacket() {
    }

    @JsonCreator
    public CoinExportPacket(@JsonProperty("list") ArrayList<CoinExport> list) {
        this.list = list;
    }

}
