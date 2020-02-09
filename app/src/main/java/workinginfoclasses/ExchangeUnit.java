package workinginfoclasses;

public final class ExchangeUnit
{
    private String shortName;
    private String longName;
    public float newCourse;

    public ExchangeUnit(final String shortName, final String longName, final float newCourse)
    {
        this.shortName = shortName;
        this.longName = longName;
        this.newCourse = newCourse;
    }

    public final String getShortName()
    {
        return this.shortName;
    }

    public final float getNewCourse()
    {
        return this.newCourse;
    }

    public final String getLongName()
    {
        return this.longName;
    }
}
