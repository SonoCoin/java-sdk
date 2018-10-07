package io.sonocoin.sdk.Types;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

/**
 * Created by aantonov on 31.08.2017.
 */
public class Setting implements Comparable<Setting>, Parcelable {
    public int id;
    public String language;
    public String color;
    public int security;
    public String summ;
    public String summusd;

    public Setting(int id, String language, String color, int security) {
        this.id = id;
        this.language = language;
        this.color = color;
        this.security = security;
    }

    protected Setting(Parcel in) {
        language = in.readString();
        color = in.readString();
        security = in.readInt();
    }

    public static final Creator<Setting> CREATOR = new Creator<Setting>() {
        @Override
        public Setting createFromParcel(Parcel in) {
            return new Setting(in);
        }

        @Override
        public Setting[] newArray(int size) {
            return new Setting[size];
        }
    };

    @Override
    public int compareTo(Setting o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(language);
        dest.writeString(color);
        dest.writeInt(security);
    }

    public void setSumm(String summ, String summusd) {
        this.summ = summ;
        this.summusd = summusd;
    }

    /**
     * Table description (DDL) for SQLite
     */
    public static abstract class SettingEntry implements BaseColumns {
        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_NAME_NULLABLE = null;
        public static final String COLUMN_NAME_LANGUAGE = "language";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_SECURITY = "security";
    }
}
