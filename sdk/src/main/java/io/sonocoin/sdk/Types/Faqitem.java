package io.sonocoin.sdk.Types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import io.sonocoin.sdk.Types.Faq.FaqItem;

public class Faqitem implements Parcelable {

    public FaqItem ru;
    public FaqItem en;

    public Faqitem(FaqItem ru, FaqItem en) {
        this.ru = ru;
        this.en = en;
    }

    protected Faqitem(Parcel in) {
        ru = in.readParcelable(FaqItem.class.getClassLoader());
        en = in.readParcelable(FaqItem.class.getClassLoader());
    }

    public static final Creator<Faqitem> CREATOR = new Creator<Faqitem>() {
        @Override
        public Faqitem createFromParcel(Parcel in) {
            return new Faqitem(in);
        }

        @Override
        public Faqitem[] newArray(int size) {
            return new Faqitem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faqitem faqitem = (Faqitem) o;
        return Objects.equals(ru, faqitem.ru) &&
                Objects.equals(en, faqitem.en);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ru, en);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ru, flags);
        dest.writeParcelable(en, flags);
    }
}

