package com.xu.investo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.io.IOException;
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
    boolean infoUpToDate;
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    SharedPreferences welcomesp;
    boolean shown;
    boolean internetAccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_holder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        internetAccess= isOnline();
        if(internetAccess ==false){
            Toast.makeText(this,"No network connection detected. Please check your internet connection",Toast.LENGTH_LONG).show();
        }


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
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        welcomesp =PreferenceManager.getDefaultSharedPreferences(this);
        shown = welcomesp.getBoolean("welcomeShown",false);
        if(shown == false){
            WelcomeFragment wc= new WelcomeFragment();
            wc.show(getSupportFragmentManager(),"Getting Started");

        }


    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
    //----------------------- Interface code-----------
    @Override
    public void communicateup(){

        communicatedown();
        //call communicate down STRAIGHT to get to listfragment, since we call communicateup from mainfrag
    }

    @Override
    public void communicatedown(){
        //This line works, accesses the linefragment by its position in viewPager, then call its refreshUI
        //method in order to fetch TinyDB portfolio
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

    //During onStop we will now store boolean uptodate as false, so that we can refresh all prices
    //in listFragment when we next run it
    @Override
    public void onStop(){
        super.onStop();
        infoUpToDate = false;
        sp = getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
        edit= sp.edit();
        edit.putBoolean("infoUpToDate",infoUpToDate);
        edit.commit();

    }

}
