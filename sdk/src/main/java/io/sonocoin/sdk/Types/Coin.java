package io.sonocoin.sdk.Types;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.math.BigInteger;

/**
 * Created by aantonov on 31.08.2017.
 */
public class Coin implements Comparable<Coin>, Cloneable {

    public static final int TYPE_SOUND = 1;
    public static final int TYPE_DIGITAL = 2;

    public int id;
    public int index;
    public String hash;
    public BigInteger amount;
    public BigInteger amount_without_commission;
    public String amount_format;
    public Integer cointype;
    public Integer status;
    public String secretkey;
    public Integer rel; // Status depends on operation №x
    public String pk_script;
    public Integer pk_script_length;
    public String dt;
    public Integer encrypted_method;
    public Integer coin_version;

    public Coin(int id, BigInteger amount, String hash, Integer index, Integer cointype,
                Integer status, String dt, String amount_format, String secretkey, String pk_script,
                Integer rel, Integer encrypted_method, Integer coin_version) {
        this.id = id;
        this.amount = amount;
        this.amount_format = amount_format;
        this.secretkey = secretkey;
        this.pk_script = pk_script;
        this.hash = hash;
        this.index = index;
        this.cointype = cointype;
        this.status = status;
        this.rel = rel;
        this.dt = dt;
        this.encrypted_method = encrypted_method;
        this.coin_version = coin_version;
    }

    public Coin clone() throws CloneNotSupportedException {
        Coin obj = (Coin) super.clone();
        return obj;
    }

    @Override
    public int compareTo(@NonNull Coin o) {
        return Integer.compare(Integer.parseInt(dt), Integer.parseInt(o.dt));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coin coin = (Coin) o;

        if (id != coin.id) return false;
        if (index != coin.index) return false;
        if (!hash.equals(coin.hash)) return false;
        if (!amount.equals(coin.amount)) return false;
        if (amount_without_commission != null ? !amount_without_commission.equals(coin.amount_without_commission) : coin.amount_without_commission != null)
            return false;
        return amount_format != null ? amount_format.equals(coin.amount_format) : coin.amount_format == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + index;
        result = 31 * result + hash.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (amount_without_commission != null ? amount_without_commission.hashCode() : 0);
        result = 31 * result + (amount_format != null ? amount_format.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Coin{" +
                "id=" + id +
                ", index=" + index +
                ", hash='" + hash + '\'' +
                ", amount=" + amount +
                ", amount_without_commission=" + amount_without_commission +
                ", amount_format='" + amount_format + '\'' +
                ", cointype=" + cointype +
                ", status=" + status +
                ", secretkey='" + secretkey + '\'' +
                ", rel=" + rel +
                ", pk_script='" + pk_script + '\'' +
                ", pk_script_length=" + pk_script_length +
                ", dt='" + dt + '\'' +
                ", encrypted_method=" + encrypted_method +
                ", coin_version=" + coin_version +
                '}';
    }

    /**
     * Table description (DDL) for sqlite
     */
    public static abstract class CoinEntry implements BaseColumns {
        public static final String TABLE_NAME = "coins";
        public static final String COLUMN_NAME_NULLABLE = null;
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_HASH = "hash";
        public static final String COLUMN_NAME_INDEX = "cindex";
        public static final String COLUMN_NAME_COINTYPE = "cointype";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_REL = "rel";
        public static final String COLUMN_NAME_SECRETKEY = "secretkey";
        public static final String COLUMN_NAME_PUBLICKEY = "publickey";
        public static final String COLUMN_NAME_DT = "dt";
        public static final String COLUMN_NAME_ENCRYPTED_METHOD = "encrypted_method";
        public static final String COLUMN_NAME_COIN_VERSION = "coin_version";
        public static final int STATUS_READY = 1; // A coin loaded
        public static final int STATUS_FORISSUE = 2; // A coin awaits for issue
        public static final int STATUS_WAIT = 3; // A coin is in process of confirmation
        public static final int STATUS_OLD = 4; // A coin has been used (obsolete)
        public static final int STATUS_DEL = 5; // A coin has been deleted
        public static final int TYPE_SOUND = 1; // Sound file
        public static final int TYPE_DIGITAL = 2; // json
    }
}
