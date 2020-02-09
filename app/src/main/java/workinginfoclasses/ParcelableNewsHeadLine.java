package workinginfoclasses;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNewsHeadLine implements Parcelable
{
    private NewsHeadLine headLine;

    public NewsHeadLine getHeadline()
    {
        return this.headLine;
    }

    public ParcelableNewsHeadLine(NewsHeadLine headline)
    {
        super();
        this.headLine = headline;
    }

    private ParcelableNewsHeadLine(Parcel in)
    {
        this.headLine = new NewsHeadLine((Bitmap)in.readParcelable(Bitmap.class.getClassLoader()),
                in.readString(), in.readString(), in.readString());
        this.headLine.setCategory(in.readString());
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeParcelable(this.headLine.getBitmap(), PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeString(this.headLine.getHeader());
        parcel.writeString(this.headLine.getPreviewTxt());
        parcel.writeString(this.headLine.getReference());
        parcel.writeString(this.headLine.getCategory());
    }

    public static final Parcelable.Creator<ParcelableNewsHeadLine> CREATOR =
            new Creator<ParcelableNewsHeadLine>() {

                @Override
                public ParcelableNewsHeadLine[] newArray(int size) {
                    // TODO Auto-generated method stub
                    return new ParcelableNewsHeadLine[size];
                }

                @Override
                public ParcelableNewsHeadLine createFromParcel(Parcel source) {
                    // TODO Auto-generated method stub
                    return new ParcelableNewsHeadLine(source);
                }
            };
}
