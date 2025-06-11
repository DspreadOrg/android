package com.dspread.pos.ui.base.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by goldze on 2017/7/17.
 * FragmentPager adapter
 */

public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> list;//ViewPager List of fragment to be filled in
    private List<String> title;//List of title text in tab

    //Using constructor to pass data in
    public BaseFragmentPagerAdapter(FragmentManager fm, List<Fragment> list, List<String> title) {
        super(fm);
        this.list = list;
        this.title = title;
    }

    @Override
    public Fragment getItem(int position) {//获得position中的fragment来填充
        return list.get(position);
    }

    @Override
    public int getCount() {//返回FragmentPager的个数
        return list.size();
    }

    //FragmentPager's title, if this method is rewritten, the tab's title content will not be displayed
    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }
}
