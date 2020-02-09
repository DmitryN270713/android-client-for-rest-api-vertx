package workinginfoclasses;

import java.util.List;

public final class NewsGroup
{
    private List<NewsHeadLine> lsChildren;
    private String title;

    public NewsGroup(final List<NewsHeadLine> lsChildren, final String title)
    {
        this.lsChildren = lsChildren;
        this.title = title;
    }

    public final List<NewsHeadLine> getChildrenCollection()
    {
        return this.lsChildren;
    }

    public final String getGroupTitle()
    {
        return this.title;
    }
}
