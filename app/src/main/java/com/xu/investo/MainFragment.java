package com.xu.investo;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;


/*
http://www.kibot.com/api/download_historical_data.aspx
http://stackoverflow.com/questions/754593/source-of-historical-stock-data


http://financequotes-api.com/#singlestock-hist
Probably Yahoo Finance is the most toleraant and best choice
 */

//May have to actually make all arrayLists of a type Period, and for each index, assign a Period with the value and corresponding Date?
//Alternative is to make a method to assign labels separately, once in graphActivit

//WHy is this? Figure out!
//http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_true_range_atr

//following splash, mainactivitys job is to  allow user to save stocks of interest, saved as sharedPreferences
//These should be displayed in a listView
//After selection of listview we then launch GraphActivity.

//TODO main page should not allow edittext dates, simply add to portfolio permanently.
//All date setting should be done in GraphActivity
public class MainFragment extends Fragment   {

    String id;
    Stock stock;
    Button fetch;
    EditText enterID;
    TextView display;
    Button clear;
    private static final String TAG = "MainFragment";

    ArrayList<String>stocknames;
    ArrayList<String>stocktickers;
    List<HistoricalQuote> historicaldata;

    TinyDB tinyDB;
    //https://github.com/kcochibili/TinyDB--Android-Shared-Preferences-Turbo
    long SMA;
    String decision;
    TextView decisionView;
    Button launchList;
   TextView dateFrom;
    TextView dateTo;

    Date datefrom;
    Date dateto;

    //Following boolean is used to see which date we are editing
    boolean isDateEdit1;

    MethodDatabase db;
    public DialogFragment dateFragment;
    public Calendar mCalendar;

    static final int DATE_DIALOG_ID = 999;
    CommunicateToActivity recyclerinterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =inflater.inflate(R.layout.content_main,container,false);
        stocknames = new ArrayList<String>();
        stocktickers = new ArrayList<String>();
        tinyDB = new TinyDB(getContext());



        /*
        menu.setDisplayShowHomeEnabled(true);
        //menu.setLogo("INSERT LOGO HERE");
        menu.setDisplayUseLogoEnabled(true);
        menu.setTitle(" Stock Selector");
        */

        fetch =(Button) view.findViewById(R.id.fetchBtn);
        enterID =(EditText)view.findViewById(R.id.enterID);
        display =(TextView)view.findViewById(R.id.display);
        mCalendar = Calendar.getInstance();
        clear = (Button)view.findViewById(R.id.clearportfolio);









        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO all the main page should do is add stocktickers and names to portfolio
                //Fetch id and the dates
                id =enterID.getText().toString();




                /*to = Calendar.getInstance();
                from = Calendar.getInstance();
                to.setTime(dateto);
                from.setTime(datefrom);
                */

                FetchXDayData getData = new FetchXDayData();
                getData.execute(id);












            }
        });
        clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tinyDB.clear();
                recyclerinterface.communicateup();

            }
        });





        return view;

    }

    //----------------------------INTERFACE CODE


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            recyclerinterface = (CommunicateToActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RecyclerUpdateInterface");
        }
    }







    ///-------End of Oncreate---------------------------------------------------------------

    //Called by AsyncTask, moving result to main thread
    public void  moveResultToUI(Stock result){

        this.stock = result;
        Toast.makeText(getActivity(),"Stock "+stock.getName()+" successfully added to portofolio",Toast.LENGTH_LONG).show();
        //reverse the list of course,  stock names and tickrs to portfolio



        stocknames.add(stock.getName());
        stocktickers.add(stock.getSymbol());


        /*DEBUG Test code, Test. 30012017 WORKS
        for (int i =0;i<historicaldata.size();i++){
            HistoricalQuote current = historicaldata.get(i);
            Toast toast = Toast.makeText(this,current.getClose().toString(),Toast.LENGTH_SHORT);
            toast.show();
        }
        */


        //
        if (stock != null){
            display.setText("Name: "+stock.getName() +"\n"+"Price: "+ stock.getQuote().getPrice()+"\n"+ "Change(%)"+stock.getQuote().getChangeInPercent());
            /*SMA = getSMA(10);
            decision=checkSimpleCrossover(SMA,stock.getQuote().getPrice().longValue());
            decisionView.setText("SMA: " + SMA + "\n"+decision);
            */
            tinyDB.putListString("names",stocknames);
            tinyDB.putListString("tickers",stocktickers);
            //call interface activity comming up to Activity, then down to next fragment
            recyclerinterface.communicateup();






        }else{
            Toast error = Toast.makeText(getActivity(),"Network Problem",Toast.LENGTH_SHORT);
            error.show();
        }

    }

    //Method for reversing arrayList, since Yahoo gives us newest data first, while StockCharts uses oldest data first
    //By having oldest data first, the first days within realRange will obviously have no calculated values.
    public static ArrayList<HistoricalQuote>reverse(List<HistoricalQuote>list){
        int length = list.size();
        ArrayList<HistoricalQuote>result  =new ArrayList<HistoricalQuote>(length);
        //TODO Check this, we are getting an indexout of bounds error,
        for(int i= length-1;i>=0;i--){
            result.add(list.get(i));
        }
        return result;
    }



    //Fetches the last 10 days for SMA
    public void getHistory(String ticket){
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, -10); // from 10 days
        try{
            Stock interested = YahooFinance.get(id);

            historicaldata = interested.getHistory(from, to);
        }catch (java.io.IOException e){
            Toast.makeText(getActivity(),"Error fetching history",Toast.LENGTH_SHORT);
        }

    }



    public long getSMA(int N) {
        //SMA for 10 last days
        long sum =0;

        long SMA;

        for (int i = 0; i < historicaldata.size(); i++) {
            //for each item in historical data Array
            sum += historicaldata.get(i).getClose().longValue();
        }
        SMA = sum / N;
        Log.d(TAG,"sum ="+sum);
        Log.d(TAG,"Number of historical entries ="+historicaldata.size());
        return SMA;
    }









//-------------------------------------------------------------------------------------------------------------------------------------------AsyncTask networking



    //Inner test class for fetching X days from now
    //Check if getHistory actually fetches till current, or just till yesterday
    class FetchXDayData extends AsyncTask<String,Void,Boolean> {
        private Stock temp;
        List<HistoricalQuote>data;
        private ProgressDialog pdialog;

        @Override
        protected void onPreExecute(){
            pdialog= new ProgressDialog(getActivity());
            pdialog.setMessage("Fetching stock data...");
            pdialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params){
            try{
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();

                temp = YahooFinance.get(params[0]);



            }catch (java.io.IOException e){

            }
            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (pdialog.isShowing()){
                pdialog.dismiss();
            }

            //May have some overlap here
            moveResultToUI(temp);




        }


    }









}
