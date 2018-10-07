package io.sonocoin.sdk.Core;

import io.sonocoin.sdk.Types.Coin;

import java.io.IOException;
import java.util.List;

public interface DownloadCallback {

    void onActionDone(List<Coin> coins);

    void onError(String msg);

}
