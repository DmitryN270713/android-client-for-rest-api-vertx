package news.bbc.bbc_reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workinginfoclasses.NewsGroup;
import workinginfoclasses.NewsHeadLine;
import workinginfoclasses.ParcelableGroupInfo;
import workinginfoclasses.ParcelableNewsHeadLine;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;



public class ExpandableNewsListFragment extends Fragment
{
    public final static String GET_COLUMNS_HEADERS = "GET_COLUMNS_HEADERS";
    public final static String COLUMNS_INCOME = "COLUMNS_INCOME";
    private ColumnsonReceive receiver;
    private ArrayList<ParcelableGroupInfo> columns;
    private OnGroupNewsExpanded grCallback;
    private Map<String, ParcelableGroupInfo> existGroupsMap;
    private ExpandableListView listViewExpandable;
    private NewsGroupReceiveronReceive receiverGroupNews;
    private List<NewsHeadLine> lsheadLines;
    private List<NewsGroup> lsNewsGroup;
    private DisplayMetrics metrics;
    private boolean receiverRegistered = false;

    public static final String ON_NEWS_HEADERS_RECEIVE = "ON_NEWS_HEADERS_RECEIVE";
    public static final String ON_NEWS_HEADERS_RECEIVE_KEY_1 = "ON_NEWS_HEADERS_RECEIVE_KEY_1";
    public static final String ON_NEWS_HEADERS_RECEIVE_KEY_2 = "ON_NEWS_HEADERS_RECEIVE_KEY_2";
    public static final String ON_NEWS_HEADERS_RECEIVE_NUMBER = "ON_NEWS_HEADERS_RECEIVE_NUMBER";
    public static final String ON_NEWS_HEADERS_RECEIVE_NEWS_NUMBER = "ON_NEWS_HEADERS_RECEIVE_NEWS_NUMBER";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.grCallback = (OnGroupNewsExpanded)this.getActivity();
        this.existGroupsMap = new HashMap<String, ParcelableGroupInfo>();
        this.lsNewsGroup = new ArrayList<NewsGroup>();

        if(!this.receiverRegistered) {
            this.receiver = new ColumnsonReceive();
            IntentFilter filter = new IntentFilter();
            filter.addAction(GET_COLUMNS_HEADERS);
            this.getActivity().registerReceiver(receiver, filter);

            this.receiverGroupNews = new NewsGroupReceiveronReceive();
            IntentFilter grFilter = new IntentFilter(ON_NEWS_HEADERS_RECEIVE);
            grFilter.addAction(ON_NEWS_HEADERS_RECEIVE_NUMBER);
            this.getActivity().registerReceiver(receiverGroupNews, grFilter);

            this.receiverRegistered = true;
        }

        View view = inflater.inflate(R.layout.expandable_list_fragment, null);
        this.listViewExpandable = (ExpandableListView) view.findViewById(R.id.expandableListView);

        this.listViewExpandable.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                String grName = ExpandableNewsListFragment.this.columns.get(groupPosition).getGroupTitle();
                ParcelableGroupInfo grInfo = ExpandableNewsListFragment.this.existGroupsMap.get(grName);

                if(grInfo == null) {
                    grInfo = ExpandableNewsListFragment.this.columns.get(groupPosition);
                    ExpandableNewsListFragment.this.grCallback.onGroupExpanded(new String[]{grInfo.getGroupRef(), grInfo.getGroupTitle()});
                }
            }

        });

        this.listViewExpandable.setOnChildClickListener(new OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id)
            {
                String introduction = ExpandableNewsListFragment.this.lsNewsGroup.get(groupPosition).
                        getChildrenCollection().get(childPosition).getPreviewTxt();
                String globalNewsHeader = ExpandableNewsListFragment.this.lsNewsGroup.get(groupPosition).
                        getChildrenCollection().get(childPosition).getHeader();
                Intent preRequestIntent = new Intent(NewsHeadlinesFragment.GROUP_INTRODUCTION_AND_GLOBAL);
                preRequestIntent.putExtra(NewsHeadlinesFragment.GROUP_INTRODUCTION_AND_GLOBAL_KEY_1, introduction);
                preRequestIntent.putExtra(NewsHeadlinesFragment.GROUP_INTRODUCTION_AND_GLOBAL_KEY_2, globalNewsHeader);
                ExpandableNewsListFragment.this.getActivity().sendBroadcast(preRequestIntent);

                Intent intent = new Intent(MainBBCNewsReader.ITEM_ACTION_CLICKED);
                intent.putExtra(MainBBCNewsReader.ITEM_ACTION_CLICKED_KEY,
                        new ParcelableGroupInfo(ExpandableNewsListFragment.this.lsNewsGroup.get(groupPosition).
                                getChildrenCollection().get(childPosition).getHeader(),
                                ExpandableNewsListFragment.this.lsNewsGroup.get(groupPosition).
                                        getChildrenCollection().get(childPosition).getReference()));
                ExpandableNewsListFragment.this.getActivity().sendBroadcast(intent);
                return true;
            }

        });

        return view;
    }

    public interface OnGroupNewsExpanded {
        public void onGroupExpanded(String... params);
    }

    private class ColumnsonReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GET_COLUMNS_HEADERS)) {
                ExpandableNewsListFragment.this.columns =
                        intent.getParcelableArrayListExtra(COLUMNS_INCOME);
                ExpandableNewsListFragment.this.lsNewsGroup = new ArrayList<NewsGroup>();
                for(ParcelableGroupInfo grInfo : ExpandableNewsListFragment.this.columns) {
                    ExpandableNewsListFragment.this.lsNewsGroup.add(new NewsGroup(new ArrayList<NewsHeadLine>(), grInfo.getGroupTitle()));
                }
                NewsHeadersExpandableAdapter adapter = new NewsHeadersExpandableAdapter(ExpandableNewsListFragment.this.lsNewsGroup,
                        ExpandableNewsListFragment.this.getActivity());

                ExpandableNewsListFragment.this.listViewExpandable.setAdapter(adapter);
            }
        }

    }

    private class NewsGroupReceiveronReceive extends BroadcastReceiver {
        private int newsAvailable = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ON_NEWS_HEADERS_RECEIVE_NUMBER)) {
                ExpandableNewsListFragment.this.lsheadLines = new ArrayList<NewsHeadLine>();
                newsAvailable = Integer.valueOf(intent.getStringExtra(ON_NEWS_HEADERS_RECEIVE_NEWS_NUMBER));
            } else if (action.equals(ON_NEWS_HEADERS_RECEIVE)) {
                ParcelableNewsHeadLine parcerable = intent.getParcelableExtra(ON_NEWS_HEADERS_RECEIVE_KEY_1);
                ExpandableNewsListFragment.this.lsheadLines.add(parcerable.getHeadline());
                newsAvailable--;
                if (newsAvailable == 0) {
                    String groupName = ExpandableNewsListFragment.this.lsheadLines.get(0).getCategory();
                    int groupToBeExpanded = 0;

                    for (int i = 0; i < ExpandableNewsListFragment.this.columns.size(); i++) {
                        if (ExpandableNewsListFragment.this.columns.get(i).getGroupTitle().equals(groupName)) {
                            ExpandableNewsListFragment.this.existGroupsMap.put(groupName, ExpandableNewsListFragment.this.columns.get(i));
                            break;
                        }
                    }
                    for (int i = 0; i < ExpandableNewsListFragment.this.lsNewsGroup.size(); i++) {
                        if (ExpandableNewsListFragment.this.lsNewsGroup.get(i).getGroupTitle().equals(groupName)) {
                            ExpandableNewsListFragment.this.lsNewsGroup.remove(i);
                            ExpandableNewsListFragment.this.lsNewsGroup.add(i, new NewsGroup(ExpandableNewsListFragment.this.lsheadLines, groupName));
                            groupToBeExpanded = i;
                        }
                    }
                    NewsHeadersExpandableAdapter adapter = new NewsHeadersExpandableAdapter(ExpandableNewsListFragment.this.lsNewsGroup,
                            ExpandableNewsListFragment.this.getActivity());

                    ExpandableNewsListFragment.this.listViewExpandable.setAdapter(adapter);
                    ExpandableNewsListFragment.this.listViewExpandable.expandGroup(groupToBeExpanded);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager manager = (WindowManager)this.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        this.metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int displayWidth = this.metrics.widthPixels;
        this.listViewExpandable.setIndicatorBounds(displayWidth - this.DPtoPixels(68),
                displayWidth - this.DPtoPixels(10));
    }

    private int DPtoPixels(final int dp) {
        final float scale = this.getResources().getDisplayMetrics().density;
        int pixels = (int)(dp * scale + 0.5f);
        return pixels;
    }

    @Override
    public void onDestroy() {
        this.getActivity().unregisterReceiver(this.receiver);
        this.getActivity().unregisterReceiver(this.receiverGroupNews);
        super.onDestroy();
    }
}
