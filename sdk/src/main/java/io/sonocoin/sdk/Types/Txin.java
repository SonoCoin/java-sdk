package io.sonocoin.sdk.Types;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aantonov on 31.08.2017.
 */
public class Txin {

    @SerializedName("signature_script")
    public String signature_script;

    @SerializedName("previous_output")
    public Output previous_output;

    @SerializedName("script_length")
    public int script_length;

    @SerializedName("sequence")
    public int sequence;

}
