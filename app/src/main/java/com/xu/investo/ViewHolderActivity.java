package com.xu.investo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

public class ViewHolderActivity extends AppCompatActivity implements CommunicateToActivity,CommunicateToFragment{
    //Job of ViewHolderActivity is to allow swiping between list and MainFragment/Fragment
    TabLayout tablayout;
    ViewPager viewPager;
    List<HistoricalQuote> historicaldata;
    Bundle bundle;
    Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_holder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        viewPager =(ViewPager)findViewById(R.id.viewpager);
        tablayout= (TabLayout)findViewById(R.id.tabs);
        tablayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    //----------------------- Interface code-----------
    @Override
    public void communicateup(){
        communicatedown();
        //call communicate down STRAIGHT, since we call it from mainfrag
    }

    @Override
    public void communicatedown(){
        //This line works
        ListFragment currentFragment =(ListFragment)adapter.instantiateItem(viewPager,1);
        currentFragment.refreshUI();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new MainFragment(), "Add Stock");
        adapter.addFragment(new ListFragment(), "Portfolio");

        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
