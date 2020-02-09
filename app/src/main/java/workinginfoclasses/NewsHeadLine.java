package workinginfoclasses;

import android.graphics.Bitmap;

public final class NewsHeadLine
{
    private Bitmap img;
    private String header;
    private String previewTxt;
    private String ref;
    private String category;

    public NewsHeadLine(final Bitmap img, final String header, final String previewTxt, final String ref)
    {
        this.img = img;
        this.header = header;
        this.previewTxt = previewTxt;
        this.ref = ref;
    }

    public void setReference(final String ref)
    {
        this.ref = ref;
    }

    public final String getReference()
    {
        return this.ref;
    }

    public void setBitmap(final Bitmap img)
    {
        this.img = img;
    }

    public void setHeader(final String header)
    {
        this.header = header;
    }

    public void setPreviewTxt(final String previewTxt)
    {
        this.previewTxt = previewTxt;
    }

    public final Bitmap getBitmap()
    {
        return this.img;
    }

    public final String getHeader()
    {
        return this.header;
    }

    public final String getPreviewTxt()
    {
        return this.previewTxt;
    }

    public void setCategory(final String category) { this.category = category; }

    public final String getCategory() { return this.category; }
}
