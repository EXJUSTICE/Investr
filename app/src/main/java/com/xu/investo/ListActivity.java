package com.xu.investo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

//ListActivity hosts listview, job is to display list of tickers provided by user
//hosts listFragment
//TODO 2.02.2017 Codwe being replaced with ViewHolderActivity

public class ListActivity extends AppCompatActivity {

    public  ArrayList<String> stocknames;
    public  ArrayList<String>stocktickers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //includes fragment container
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        //menu.setLogo("INSERT LOGO HERE");
        menu.setDisplayUseLogoEnabled(true);
        menu.setTitle(" List of Stocks in Portfolio");

        //If this doesnt work, technically we can put a String ArrayList instead, or a hashmap if we want prices
        Intent intent =getIntent();
        Bundle args= intent.getBundleExtra("BUNDLE");
        stocknames=(ArrayList)args.getStringArrayList("stocknames");
        stocktickers = (ArrayList)args.getStringArrayList("stocktickers");

        //Setting fragment with its own bundle full of arraylists
        Bundle fragmentBund = new Bundle();
        fragmentBund.putStringArrayList("stocknames",stocknames);
        fragmentBund.putStringArrayList("stocktickers",stocktickers);


        //Find the frameLayou and replace it with the fragment itself
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new ListFragment();
            fragment.setArguments(fragmentBund);
            //we are not instantiating new CrimeFragment, just an abstract method
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)

                    .commit();


        }


    }
}
