package com.xu.investo;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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

//TODO while TinyDB is a sharedpreferences tool, everytime user starts app
//TODO 2 implement a new fetchstockclass?
//TODO old and tries to place more shares, hes actually emptying list?
//TODO check internet access
public class MainFragment extends Fragment   {

    String id;
    Stock stock;
    Button fetch;
    EditText enterID;
    TextView display;
    Button clear;
    TextView continueadd;
    private static final String TAG = "MainFragment";

    ArrayList<String>stocknames;
    ArrayList<String>stocktickers;
    ArrayList<String>stockprices;
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

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    boolean upToDate;
    boolean internetAccess;
    int MY_PERMISSIONS_REQUEST_INTERNET=123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =inflater.inflate(R.layout.content_main,container,false);
        stocknames = new ArrayList<String>();
        stocktickers = new ArrayList<String>();
        stockprices= new ArrayList<String>();
        tinyDB = new TinyDB(getContext());

        //TODO 06022017 Checking if tinyDB sharedprefs already contains lists, we add to that
        if(tinyDB.getListString("stocknames")!=null){
            stocknames = tinyDB.getListString("names");
            stocktickers= tinyDB.getListString("tickers");
            stockprices= tinyDB.getListString("prices");
        }
        if (Build.VERSION.SDK_INT >= 23 && !(getActivity().checkSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED )) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.INTERNET"}, MY_PERMISSIONS_REQUEST_INTERNET);
        }

        internetAccess= isOnline();
        if(internetAccess ==false){
            Toast.makeText(getActivity(),"No network connection detected. Please check your internet connection",Toast.LENGTH_LONG).show();
        }

        /*
        menu.setDisplayShowHomeEnabled(true);
        //menu.setLogo("INSERT LOGO HERE");
        menu.setDisplayUseLogoEnabled(true);
        menu.setTitle(" Stock Selector");
        */

        fetch =(Button) view.findViewById(R.id.fetchBtn);
        enterID =(EditText)view.findViewById(R.id.enterID);
        enterID.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        display =(TextView)view.findViewById(R.id.display);
        mCalendar = Calendar.getInstance();
        clear = (Button)view.findViewById(R.id.clearportfolio);
        continueadd = (TextView)view.findViewById(R.id.continueAdding);

        //Check if we need to update recyclerView
        sp = getActivity().getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
        editor=sp.edit();
        upToDate = sp.getBoolean("infoUpToDate",false);

        /*TODO Currently it keeps adding more tickers, suggest adding refersh button to both main and listfrag menus
        //TODO user prompted refresh
        if(upToDate== false){
            tinyDB.clear();

            for(int i =0;i<stocktickers.size();i++){
                FetchXDayData getData = new FetchXDayData();
                getData.execute(stocktickers.get(i));
            }

            //Finally, set the boolean to upToDate to true, until next time activity calls onStop
            upToDate = true;
            //recyclerinterface.communicateup();

        }
        */







        fetch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                internetAccess = isOnline();
                if (internetAccess == false) {
                    Toast.makeText(getActivity(), "No network connection detected. Please check your internet connection", Toast.LENGTH_LONG).show();

                } else {


                    //TODO all the main page should do is add stocktickers and names to portfolio
                    //Fetch id and the dates
                    id = enterID.getText().toString();





                /*to = Calendar.getInstance();
                from = Calendar.getInstance();
                to.setTime(dateto);
                from.setTime(datefrom);
                */
                if (id ==null ||id.equals("")){
                    display.setText("Empty ticker entry, please try again");
                }else{
                    FetchXDayData getData = new FetchXDayData();
                    getData.execute(id);
                }








                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Remove all stocks in portfolio in sharedprefs
                tinyDB.clear();
                //18-02-2017
                stocknames.clear();
                stocktickers.clear();
                stockprices.clear();
                recyclerinterface.communicateup();
                Toast.makeText(getActivity(),"All stocks cleared from portfolio.",Toast.LENGTH_LONG).show();

            }
        });





        return view;

    }

    public void checkPermissions(){


        /*
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);}
                    */
    }

    //--------------Check internet
    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
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
        if (result ==null||result.getName().isEmpty()) {
            Toast nostockfound = Toast.makeText(getActivity(), "No such stock found, check ticker", Toast.LENGTH_LONG);
            nostockfound.show();


        }else{
            this.stock = result;
            Toast.makeText(getActivity(),"Stock "+stock.getName()+" successfully added to portofolio",Toast.LENGTH_LONG).show();
            //reverse the list of course,  stock names and tickrs to portfolio



            stocknames.add(stock.getName());
            stocktickers.add(stock.getSymbol());
            stockprices.add(stock.getQuote().getPrice().toString());


        /*DEBUG Test code, Test. 30012017 WORKS
        for (int i =0;i<historicaldata.size();i++){
            HistoricalQuote current = historicaldata.get(i);
            Toast toast = Toast.makeText(this,current.getClose().toString(),Toast.LENGTH_SHORT);
            toast.show();
        }
        */


            //
            if (stock != null){
                display.setText("Name: "+stock.getName() +"\n"+"Price: "+ stock.getQuote().getPrice()+"\n"+ "Change(%): "+stock.getQuote().getChangeInPercent());

                // Send data to ListFragment
                continueadd.setText("To add another stock, enter its ticker into the field below");
                tinyDB.putListString("names",stocknames);
                tinyDB.putListString("tickers",stocktickers);
                tinyDB.putListString("prices",stockprices);
                //call interface activity comming up to Activity, then down to next fragment
                recyclerinterface.communicateup();






            }else{
                Toast error = Toast.makeText(getActivity(),"Network Problem",Toast.LENGTH_SHORT);
                error.show();
            }
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

            if(temp==null||temp.getName()==null||temp.getQuote().getPrice()==null){
                Toast.makeText(getActivity(),"Invalid ticker, please check spelling",Toast.LENGTH_LONG).show();
            }else{
                moveResultToUI(temp);
            }









        }


    }









}
