package workinginfoclasses;

import android.os.Parcel;
import android.os.Parcelable;

public final class ParcelableRate implements Parcelable
{
    private String currency;
    private float rate;

    public final String getCurrency()
    {
        return this.currency;
    }

    public final float getRate()
    {
        return this.rate;
    }

    public ParcelableRate(final String currency, final float rate)
    {
        super();
        this.currency = currency;
        this.rate = rate;
    }

    private ParcelableRate(Parcel in)
    {
        this.currency = in.readString();
        this.rate = in.readFloat();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1)
    {
        arg0.writeString(this.currency);
        arg0.writeFloat(this.rate);
    }

    public static final Parcelable.Creator<ParcelableRate> CREATOR =
            new Creator<ParcelableRate>() {

                @Override
                public ParcelableRate[] newArray(int size) {
                    return new ParcelableRate[size];
                }

                @Override
                public ParcelableRate createFromParcel(Parcel parcel) {
                    return new ParcelableRate(parcel);
                }
            };
}
