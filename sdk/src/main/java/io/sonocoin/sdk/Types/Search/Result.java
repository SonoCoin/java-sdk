package io.sonocoin.sdk.Types.Search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tim on 10.01.2017.
 */

public class Result implements Parcelable {

    public Float amount;

    public String hash;

    public int index;

    protected Result(Parcel in) {
        index = in.readInt();
        hash = in.readString();
        amount = in.readFloat();
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeString(hash);
        dest.writeFloat(amount);
    }
}
