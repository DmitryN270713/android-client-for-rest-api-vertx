package news.bbc.bbc_reader;

import java.util.ArrayList;
import java.util.List;

import workinginfoclasses.NewsHeadLine;
import workinginfoclasses.ParcelableGroupInfo;
import workinginfoclasses.ParcelableNewsHeadLine;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.ViewFlipper;


public class NewsHeadlinesFragment extends ListFragment
{
    private ViewFlipper vf;
    private PublishingInfoonRecieve publisher;
    private SingleNewsReceiver singleNewsReceiver;
    private List<NewsHeadLine> lsheadLines;
    private String introduction;
    private String globalNewsHeader;
    private boolean receiversRegistered = false;
    public static final String PUBLISHER_ACTION = "PUBLISH_TOP_NEWS";
    public static final String TOP_NEWS_KEY_ARRAY = "TOP_NEWS_KEY_ARRAY";
    public static final String PUBLISHER_ACTION_NUMBER = "PUBLISHER_ACTION_NUMBER";
    public static final String TOP_NEWS_NUMBER = "TOP_NEWS_NUMBER";
    public static final String SHOW_SINGLE_NEWS_ACTION = "SHOW_SINGLE_NEWS_ACTION";
    public static final String SHOW_SINGLE_NEWS_ACTION_KEY = "SHOW_SINGLE_NEWS_ACTION_KEY";
    public static final String GROUP_INTRODUCTION_AND_GLOBAL = "GROUP_INTRODUCTION_AND_GLOBAL";
    public static final String GROUP_INTRODUCTION_AND_GLOBAL_KEY_1 = "GROUP_INTRODUCTION_AND_GLOBAL_KEY_1";
    public static final String GROUP_INTRODUCTION_AND_GLOBAL_KEY_2 = "GROUP_INTRODUCTION_AND_GLOBAL_KEY2";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(!this.receiversRegistered)
        {
            this.publisher = new PublishingInfoonRecieve();
            IntentFilter filter = new IntentFilter(PUBLISHER_ACTION_NUMBER);
            filter.addAction(PUBLISHER_ACTION);
            this.getActivity().registerReceiver(this.publisher, filter);
            this.singleNewsReceiver = new SingleNewsReceiver();
            IntentFilter singleNewsFilter = new IntentFilter(SHOW_SINGLE_NEWS_ACTION);
            singleNewsFilter.addAction(GROUP_INTRODUCTION_AND_GLOBAL);
            this.getActivity().registerReceiver(this.singleNewsReceiver, singleNewsFilter);
            this.receiversRegistered = true;
        }

        this.vf = (ViewFlipper)this.getActivity().findViewById(R.id.viewFlipper);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class PublishingInfoonRecieve extends BroadcastReceiver {
        private int newsAvailable = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PUBLISHER_ACTION_NUMBER)) {
                newsAvailable = Integer.valueOf(intent.getStringExtra(TOP_NEWS_NUMBER));
                NewsHeadlinesFragment.this.lsheadLines = new ArrayList<NewsHeadLine>();
            } else if(action.equals(PUBLISHER_ACTION)) {
                ParcelableNewsHeadLine parcerable = intent.getParcelableExtra(TOP_NEWS_KEY_ARRAY);
                NewsHeadlinesFragment.this.lsheadLines.add(parcerable.getHeadline());
                newsAvailable--;
                if (newsAvailable == 0) {
                    news.bbc.bbc_reader.NewsAdapter adapter =
                            new news.bbc.bbc_reader.NewsAdapter(NewsHeadlinesFragment.this.getActivity().getBaseContext(),
                                    android.R.layout.simple_list_item_activated_1, NewsHeadlinesFragment.this.lsheadLines);

                    NewsHeadlinesFragment.this.setListAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.getListView().setSelector(R.drawable.list_selector_red);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.introduction = this.lsheadLines.get(position).getPreviewTxt();
        this.globalNewsHeader = this.lsheadLines.get(position).getHeader();
        Intent intent = new Intent(MainBBCNewsReader.ITEM_ACTION_CLICKED);
        intent.putExtra(MainBBCNewsReader.ITEM_ACTION_CLICKED_KEY,
                new ParcelableGroupInfo(this.lsheadLines.get(position).getHeader(),
                        this.lsheadLines.get(position).getReference()));
        this.getActivity().sendBroadcast(intent);
    }

    private class SingleNewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(NewsHeadlinesFragment.SHOW_SINGLE_NEWS_ACTION)) {
                String url = intent.getStringExtra(NewsHeadlinesFragment.SHOW_SINGLE_NEWS_ACTION_KEY);
                //NOTE: commented out for back compatibilty and as a reminder to myself
                //TextView headertv = (TextView)NewsHeadlinesFragment.this.vf.findViewById(R.id.headlineView);
                //TextView newsBody = (TextView)NewsHeadlinesFragment.this.vf.findViewById(R.id.shortDescView);
                WebView newsBody = (WebView) NewsHeadlinesFragment.this.vf.findViewById(R.id.web_view);
                //Well, the user will be able to read a news from the view.
                //But she will not be able to navigate away from it
                //Pages are loaded to sd card
                newsBody.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return true;
                    }
                });
                newsBody.getSettings().setJavaScriptEnabled(true);
                newsBody.getSettings().setSupportMultipleWindows(false);
                newsBody.loadUrl(url);

                NewsHeadlinesFragment.this.vf.showNext();
            } else if(action.equals(GROUP_INTRODUCTION_AND_GLOBAL)) {
                NewsHeadlinesFragment.this.introduction = intent.getStringExtra(GROUP_INTRODUCTION_AND_GLOBAL_KEY_1);
                NewsHeadlinesFragment.this.globalNewsHeader = intent.getStringExtra(GROUP_INTRODUCTION_AND_GLOBAL_KEY_2);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        this.getActivity().unregisterReceiver(this.publisher);
        this.getActivity().unregisterReceiver(this.singleNewsReceiver);
        super.onDestroy();
    }
}
