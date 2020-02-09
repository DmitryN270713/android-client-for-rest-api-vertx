package news.bbc.bbc_reader;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import datafetching.HttpTransaction;
import news.bbc.bbc_reader.CurrencyExchangeFragment.onUploadingRequest;
import news.bbc.bbc_reader.ExpandableNewsListFragment.OnGroupNewsExpanded;
import news.bbc.bbc_reader.NewsFragmentPagerAdapter.OnTextViewSelectedTab;
import workinginfoclasses.ParcelableGroupInfo;

public class MainBBCNewsReader extends FragmentActivity implements
        OnGroupNewsExpanded, onUploadingRequest, OnTextViewSelectedTab {
    private ActionBar actionBar;
    private static final String TAG_SAVED_POSITION = "tab_position";
    private final static String URL_ADDRESS_NEWS = "http://192.168.1.41:8080";       //Remove before commiting
    private final static String MAIN_PAGE_NEWS = "/Main_page_news";
    private final static String WORLD_NEWS = "/World_news";
    private final static String SCIENCE_NEWS = "/Science_news";
    private final static String MAIN_PAGE_NEWS_LTS = "/main_latest";
    private final static String WORLD_NEWS_LTS = "/world_latest";
    private final static String SCIENCE_NEWS_LTS = "/science_latest";
    private final static String CURRENCY_NEWS = "/Currency_rates";
    private final static String CURRENCY_LTS = "/currency_latest";
    private ViewFlipper vf;
    private HttpTransaction transaction;
    private SingleItemClicked receiverItemSingle;
    private boolean receiverRegistered = false;

    public static final String ITEM_ACTION_CLICKED_KEY = "item_clicked_action";
    public final static String ITEM_ACTION_CLICKED = "ITEM_ACTION_CLICKED";
    public static final String NEWS_PARCELABLE_INFO = "NEWS_PARCELABLE_INFO";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bbcnews);

        if(!this.receiverRegistered) {
            this.receiverItemSingle = new SingleItemClicked();
            IntentFilter filter = new IntentFilter(ITEM_ACTION_CLICKED);
            this.registerReceiver(receiverItemSingle, filter);

            this.receiverRegistered = true;
        }

        this.actionBar = this.getActionBar();
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayShowTitleEnabled(false);
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.vf = (ViewFlipper)this.findViewById(R.id.viewFlipper);

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);

        NewsFragmentPagerAdapter newsAdapter = new NewsFragmentPagerAdapter(this, pager, this.vf);
        this.transaction = new HttpTransaction(this);

        newsAdapter.addNewTab(this.actionBar.newTab().setText("Main Page"), NewsHeadlinesFragment.class, null);
        newsAdapter.addNewTab(this.actionBar.newTab().setText("All News"), ExpandableNewsListFragment.class, null);
        newsAdapter.addNewTab(this.actionBar.newTab().setText("Converter"), CurrencyExchangeFragment.class, null);

        if(savedInstanceState != null) {
            this.actionBar.setSelectedNavigationItem(savedInstanceState.getInt(TAG_SAVED_POSITION, 0));
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        transaction.LoadRequest(URL_ADDRESS_NEWS, MAIN_PAGE_NEWS, WORLD_NEWS,
                                SCIENCE_NEWS, MAIN_PAGE_NEWS_LTS, WORLD_NEWS_LTS,
                                SCIENCE_NEWS_LTS, CURRENCY_NEWS, CURRENCY_LTS);
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        state.putInt(TAG_SAVED_POSITION, this.getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_bbcnews, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(this.vf.getCurrentView() != this.findViewById(R.id.pager))
            {
                this.vf.showPrevious();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private class SingleItemClicked extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MainBBCNewsReader.ITEM_ACTION_CLICKED)) {
                ParcelableGroupInfo group = (ParcelableGroupInfo)intent.getParcelableExtra(ITEM_ACTION_CLICKED_KEY);
                MainBBCNewsReader.this.transaction.executeNewsFetching(group.getGroupTitle(), group.getGroupRef());
            }
        }
    }

    @Override
    public void onStop() {
        if(this.vf.getCurrentView() != this.findViewById(R.id.pager)) {
            this.vf.showPrevious();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        this.transaction.RemoveCacheDirectory();
        this.unregisterReceiver(this.receiverItemSingle);
        super.onDestroy();
    }

    @Override
    public void onGroupExpanded(String... params) {
        this.transaction.GroupNewsExecute(params[0], params[1], URL_ADDRESS_NEWS);
    }

    @Override
    public void onUpload()
    {
        this.transaction.ExchangeCoursesLoadExecute();
    }

    @Override
    public void onTextViewSelectedTab(final int position) {
        this.vf.showPrevious();
        this.actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(this.vf.getCurrentView() != this.findViewById(R.id.pager)) {
                    this.vf.showPrevious();
                }
                this.actionBar.setSelectedNavigationItem(0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
