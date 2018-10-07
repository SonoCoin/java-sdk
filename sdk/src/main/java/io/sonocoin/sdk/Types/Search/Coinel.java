package io.sonocoin.sdk.Types.Search;

/**
 * Created by tim on 10.01.2017.
 */

public class Coinel {

    public String token_id;

    public String hash;
    public int index;

    public Coinel(String token_id, String hash, int index) {
        this.token_id = token_id;
        this.hash = hash;
        this.index = index;
    }
}
