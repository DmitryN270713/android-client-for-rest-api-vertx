package news.bbc.bbc_reader;

import java.util.List;

import workinginfoclasses.NewsGroup;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsHeadersExpandableAdapter extends BaseExpandableListAdapter
{
    private final List<NewsGroup> lsGroup;
    private Context context;

    public NewsHeadersExpandableAdapter(final List<NewsGroup> lsGroup, Context context)
    {
        this.lsGroup = lsGroup;
        this.context = context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return this.lsGroup.get(groupPosition).getChildrenCollection().get(childPosition);
    }

    @Override
    public long getChildId(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {
        View view = null;
        LayoutInflater inflater;

        if(view == null)
        {
            inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.news_headlines_left, null);
            TextView tvTitle = (TextView) view.findViewById(R.id.headlineLeft);
            tvTitle.setText(this.lsGroup.get(groupPosition)
                    .getChildrenCollection().get(childPosition).getHeader());
            TextView tvDescription = (TextView) view.findViewById(R.id.shortDescLeft);
            tvDescription.setText(this.lsGroup.get(groupPosition)
                    .getChildrenCollection().get(childPosition).getPreviewTxt());
            ImageView imgView = (ImageView)view.findViewById(R.id.newsPhotoLeft);
            imgView.setImageBitmap(this.lsGroup.get(groupPosition)
                    .getChildrenCollection().get(childPosition).getBitmap());
        }

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return this.lsGroup.get(groupPosition).getChildrenCollection().size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return this.lsGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount()
    {
        return this.lsGroup.size();
    }

    @Override
    public long getGroupId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent)
    {
        LayoutInflater inflater;

        View view = null;

        if(view == null)			//Remove if -statement later
        {
            inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(!isExpanded)
            {
                view = inflater.inflate(R.layout.expandable_group_normal, null);
                TextView tvHeader = (TextView)view.findViewById(R.id.newsGroupHeadlineNormal);
                tvHeader.setText(this.lsGroup.get(groupPosition).getGroupTitle());
            }
            else
            {
                view = inflater.inflate(R.layout.expandable_group_expanded, null);
                TextView tvHeader = (TextView)view.findViewById(R.id.newsGroupHeadlineExpanded);
                tvHeader.setText(this.lsGroup.get(groupPosition).getGroupTitle());
            }
        }

        return view;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

}
