package news.bbc.bbc_reader;

import java.util.ArrayList;

import android.content.Context;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ViewFlipper;


public class NewsFragmentPagerAdapter extends FragmentPagerAdapter
        implements ActionBar.TabListener, ViewPager.OnPageChangeListener
{
    private final ActionBar actionBar;
    private final ViewPager viewPager;
    private ViewFlipper vf;
    private OnTextViewSelectedTab onTextViewSelectedTab;
    private final Context context;
    private final ArrayList<TabInfo> lsTabs = new ArrayList<TabInfo>();


    private final class TabInfo
    {
        private final Class<?> cls;
        private final Bundle args;

        public TabInfo(Class<?> cls, Bundle args)
        {
            this.cls = cls;
            this.args = args;
        }

        public Bundle getArgs()
        {
            return this.args;
        }

        public Class<?> getClassInf()
        {
            return this.cls;
        }
    }

    public NewsFragmentPagerAdapter(FragmentActivity activity, ViewPager viewPager, ViewFlipper vf)
    {
        super(activity.getSupportFragmentManager());
        this.vf = vf;
        this.onTextViewSelectedTab = (OnTextViewSelectedTab) activity;
        this.context = activity.getBaseContext();
        this.actionBar = activity.getActionBar();
        this.viewPager = viewPager;
        this.viewPager.setAdapter(this);
        this.viewPager.setOnPageChangeListener(this);
    }

    public void addNewTab(ActionBar.Tab tab, Class<?> cls, Bundle args)
    {
        TabInfo info = new TabInfo(cls, args);
        tab.setTag(info);
        tab.setTabListener(this);
        this.lsTabs.add(info);
        this.actionBar.addTab(tab);
        notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int arg0)
    {
        this.actionBar.setSelectedNavigationItem(arg0);				//arg0 is position in the list
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        boolean isPagerView = (this.vf.getCurrentView() == this.viewPager ? true : false);
        Object tag = tab.getTag();

        for(int i = 0; i < this.lsTabs.size(); i++)
        {
            if(lsTabs.get(i) == tag)
            {
                if(isPagerView)
                {
                    this.viewPager.setCurrentItem(i, true);
                }
                else
                {
                    this.viewPager.setCurrentItem(i, true);
                    this.onTextViewSelectedTab.onTextViewSelectedTab(i);
                }
                break;
            }
        }
        this.HideVirtualKeyBoard();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public Fragment getItem(int arg0) {
        return Fragment.instantiate(this.context, this.lsTabs.get(arg0).getClassInf().getName(),
                this.lsTabs.get(arg0).getArgs());
    }

    @Override
    public int getCount()
    {
        return this.lsTabs.size();
    }

    public interface OnTextViewSelectedTab
    {
        public void onTextViewSelectedTab(final int position);
    }

    private void HideVirtualKeyBoard()
    {
        InputMethodManager manager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(this.viewPager.getWindowToken(), 0);
    }
}
