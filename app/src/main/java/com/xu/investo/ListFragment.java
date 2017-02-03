package com.xu.investo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * TODO still attempt to do the setArguments method. Dont call new instance or adding list but add pages individually
 */

public class ListFragment extends Fragment {
    private HistoricalQuote[] hisstocks;
    private Stock[] stocks;
    private RecyclerView recyclerView;
    private StockAdapter mAdapter;


    public  ArrayList<String> stocknames;
    public  ArrayList<String>stocktickers;
    TinyDB tinyDB;


    @Override
    public void onCreate(Bundle savedInstanceState){
        //exists only to set the options menu
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //fetching arraylists

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_list, container, false);
        //Convert the arraylist into an array for arrayadapter
        stocknames = new ArrayList<String>();
        stocktickers = new ArrayList<String>();

        tinyDB = new TinyDB(getContext());

            stocknames = tinyDB.getListString("names");
            stocktickers= tinyDB.getListString("tickers");

            if (!stocknames.isEmpty()){
                for (int i =0;i<stocknames.size();i++){
                    Toast toast= Toast.makeText(getActivity(),stocknames.get(i),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }


        recyclerView = (RecyclerView)view.findViewById(R.id.recylerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
        //http://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //layoutManager necessary because it positions views on screen, in this case linearly

        if (stocknames.isEmpty() ||stocknames ==null){
            recyclerView.setVisibility(View.GONE);
        }else{

            updateUI();

        }


        return view;

    }

    public void refreshUI(){
        stocknames.clear();
        stocktickers.clear();

        stocknames = tinyDB.getListString("names");
        stocktickers= tinyDB.getListString("tickers");
        if (mAdapter == null) {
            mAdapter = new StockAdapter(stocknames,stocktickers);
            recyclerView.setAdapter(mAdapter);
        } else {
            recyclerView.invalidate();
            mAdapter.notifyDataSetChanged();


        }

    }


    public void updateUI() {
        //updateUI must be called EXPLICITLY!

        stocknames = tinyDB.getListString("names");
        stocktickers= tinyDB.getListString("tickers");
            if (mAdapter == null) {
                mAdapter = new StockAdapter(stocknames,stocktickers);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();


            }



    }

    private class StockAdapter  extends RecyclerView.Adapter<StockHolder>{
        private ArrayList<String>stocknames;
        private ArrayList<String>stocktickers;



        public StockAdapter(ArrayList<String>names,ArrayList<String> tickers){

            this.stocknames=names;
            this.stocktickers=tickers;
        }

        @Override
        public StockHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutinflater = LayoutInflater.from(getActivity());
            View view= layoutinflater.inflate(R.layout.row,parent,false);
            return new StockHolder (view);
        }

        //Bind datato stockholder depending on position in arraylist
        public void onBindViewHolder(StockHolder holder, int position){
           String stockname = stocknames.get(position);
            String stockticker =stocktickers.get(position);
            holder.bindStock(stockname,stockticker);
        }

        @Override
        public int getItemCount (){
            return stocknames.size();
        }
    }


    private class StockHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private String stockname;
        private String stockticker;
        private TextView nametextView;
        private TextView tickertextView;

        public StockHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            nametextView =(TextView)itemView.findViewById(R.id.name);
            tickertextView= (TextView)itemView.findViewById(R.id.ticker);
        }

        @Override
        public void onClick(View v){
            Intent launchGraph= new Intent(v.getContext(),GraphActivity.class);

            launchGraph.putExtra("stockticker",stockticker);
            launchGraph.putExtra("stockname",stockname);

            startActivity(launchGraph);

            //Animations?


        }
        //Actual binder method, maybe add a current

        public void bindStock(String stocknom, String stocktick){
            this.stockname=stocknom;
            this.stockticker = stocktick;

            nametextView.setText(stockname);
            tickertextView.setText(stockticker);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
