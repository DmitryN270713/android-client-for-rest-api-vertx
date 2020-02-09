package workinginfoclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableGroupInfo implements Parcelable {
    private final String groupRef;
    private final String groupTitle;

    public ParcelableGroupInfo(final String groupTitle, final String groupRef) {
        super();
        this.groupTitle = groupTitle;
        this.groupRef = groupRef;
    }

    private ParcelableGroupInfo(Parcel in) {
        this.groupTitle = in.readString();
        this.groupRef = in.readString();
    }

    public final String getGroupTitle() {
        return this.groupTitle;
    }

    public final String getGroupRef() {
        return this.groupRef;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.groupTitle);
        parcel.writeString(this.groupRef);
    }

    public static final Parcelable.Creator<ParcelableGroupInfo> CREATOR =
            new Creator<ParcelableGroupInfo>() {

                @Override
                public ParcelableGroupInfo[] newArray(int size) {
                    return new ParcelableGroupInfo[size];
                }

                @Override
                public ParcelableGroupInfo createFromParcel(Parcel parcel) {
                    return new ParcelableGroupInfo(parcel);
                }
            };
}
