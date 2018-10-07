package io.sonocoin.sdk.Types.Faq;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
* Created by aantonov on 31.08.2017.
*/
public class FaqItem implements Parcelable {

    public String title;
    public String url;

    public FaqItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    protected FaqItem(Parcel in) {
        title = in.readString();
        url = in.readString();
    }

    public static final Creator<FaqItem> CREATOR = new Creator<FaqItem>() {
        @Override
        public FaqItem createFromParcel(Parcel in) {
            return new FaqItem(in);
        }

        @Override
        public FaqItem[] newArray(int size) {
            return new FaqItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaqItem faqItem = (FaqItem) o;
        return Objects.equals(title, faqItem.title) &&
                Objects.equals(url, faqItem.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, url);
    }
}
