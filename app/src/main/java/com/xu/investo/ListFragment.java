package com.xu.investo;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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


import com.buzzilla.webhose.client.WebhosePost;
import com.buzzilla.webhose.client.WebhoseQuery;
import com.buzzilla.webhose.client.WebhoseResponse;
import com.xu.investo.webhose.WebhoseClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * TODO Save all of portfolio upon exit and load all historical portfolios
 */

public class ListFragment extends Fragment {
    private HistoricalQuote[] hisstocks;
    private Stock[] stocks;
    private RecyclerView recyclerView;
    private StockAdapter mAdapter;
    private StockAdapter updatedAdapter;

    private RecyclerView newsRecyclerView;
    private NewsAdapter nAdapter;
    private NewsAdapter updatedNAdapter;
    private TextView instructions;


    public  ArrayList<String> stocknames;
    public  ArrayList<String>stocktickers;
    public ArrayList<String>stockprices;

    public ArrayList<String> newnames;
    public ArrayList<String>newtickers;
    public ArrayList<String>newprices;

    public ArrayList<String>updatednames;
    public ArrayList<String>updatedtickers;
    public ArrayList<String>updatedprices;

    public ArrayList<String>newsItems;
    public ArrayList<String>newsUrls;
    public ArrayList<String>newsDates;
    public ArrayList<String>newsTimes;
    TinyDB tinyDB;
    WebhoseClient client;
    WebhoseResponse newsresult;
    public int selected_position=0;
    public boolean firstclick =true;
    boolean internetAccess;
    int MY_PERMISSIONS_REQUEST_INTERNET=123;



    @Override
    public void onCreate(Bundle savedInstanceState){
        //exists only to set the options menu
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_list, container, false);
        //Convert the arraylist into an array for arrayadapter
        //TODO maybe remove these to allow for persistence once app is closed
        stocknames = new ArrayList<String>();
        stocktickers = new ArrayList<String>();
        stockprices = new ArrayList<String>();
        newnames =new ArrayList<String>();
        newtickers= new ArrayList<String>();
        newprices= new ArrayList<String>();
        updatednames=new ArrayList<String>();
        updatedprices= new ArrayList<String>();
        updatedtickers= new ArrayList<String>();
        newsItems = new ArrayList<String>();
        newsUrls = new ArrayList<String>();
        newsDates = new ArrayList<String>();
        newsTimes = new ArrayList<String>();
        checkPermissions();


        tinyDB = new TinyDB(getContext());



            stocknames = tinyDB.getListString("names");
            stocktickers= tinyDB.getListString("tickers");
        stockprices =tinyDB.getListString("prices");

        /* DEBUG toast test
            if (!stocknames.isEmpty()){
                for (int i =0;i<stocknames.size();i++){
                    Toast toast= Toast.makeText(getActivity(),stocknames.get(i),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            */


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

        newsRecyclerView = (RecyclerView)view.findViewById(R.id.newsrecylerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
        //updateNews();


        //FETCHING NEWS
         client = new WebhoseClient("aaa3e8b7-6b24-456f-a1f9-1fad25fc72b5");


        return view;

    }

    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);}
    }


    //check internet
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

    //called when user clicks option to refresh all buttons on list
    public void refreshAllStocks() {
        updatednames=new ArrayList<String>();
        updatedprices= new ArrayList<String>();
        updatedtickers= new ArrayList<String>();
        if (stocktickers.isEmpty()){
            for (int i=0;i<newtickers.size();i++){
                FetchXDayData task = new FetchXDayData();
                task.execute(newtickers.get(i));

            }
        }else if(newtickers.isEmpty()){
            for (int i=0;i<stocktickers.size();i++){
                FetchXDayData task = new FetchXDayData();
                task.execute(stocktickers.get(i));

            }
        }

        Toast.makeText(getActivity(),"Portfolio refreshed",Toast.LENGTH_LONG).show();
        //reverse the list of course,  stock names and tickrs to portfolio


    }

    public void refreshUI(){
        recyclerView.setVisibility(View.VISIBLE);
        tinyDB = null;

        tinyDB = new TinyDB(getContext());

        newnames = tinyDB.getListString("names");
        newtickers= tinyDB.getListString("tickers");
        newprices =tinyDB.getListString("prices");

            mAdapter = new StockAdapter(newnames,newtickers,newprices);
            recyclerView.setAdapter(mAdapter);


    }


    public void updateUI() {
        //updateUI must be called EXPLICITLY!

        stocknames = tinyDB.getListString("names");
        stocktickers= tinyDB.getListString("tickers");
        stockprices =tinyDB.getListString("prices");
            if (mAdapter == null) {
                mAdapter = new StockAdapter(stocknames,stocktickers,stockprices);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();


            }





    }

    private class StockAdapter  extends RecyclerView.Adapter<StockHolder>{
        private ArrayList<String>stocknames;
        private ArrayList<String>stocktickers;
        private ArrayList<String>stockprices;



        public StockAdapter(ArrayList<String>names,ArrayList<String> tickers,ArrayList<String>prices){

            this.stocknames=names;
            this.stocktickers=tickers;
            this.stockprices=prices;
        }

        @Override
        public StockHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutinflater = LayoutInflater.from(getActivity());
            View view= layoutinflater.inflate(R.layout.row,parent,false);
            return new StockHolder (view);
        }

        //Bind datato stockholder depending on position in arraylist
        public void onBindViewHolder(StockHolder holder, final int position){
           final String stockname = stocknames.get(position);
            final String stockticker =stocktickers.get(position);
            final String stockprice= stockprices.get(position);
            holder.bindStock(stockname,stockticker,stockprice);

            if(selected_position==position){
                holder.itemView.setBackgroundColor(Color.WHITE);
            }else{
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    internetAccess= isOnline();
                    if(internetAccess ==false){
                        Toast.makeText(getActivity(),"No network connection detected. Please check your internet connection",Toast.LENGTH_LONG).show();
                    }else {
                        notifyItemChanged(selected_position);
                        selected_position = position;
                        notifyItemChanged(selected_position);
                        if (firstclick == true) {
                            Toast.makeText(getActivity(), "Tap to view latest stock-related news. Hold to view graph of performance", Toast.LENGTH_LONG).show();
                        }
                        firstclick = false;
                        FetchNewsData data = new FetchNewsData();
                        data.execute(stockticker);

                    }


                }
            });
        }

        @Override
        public int getItemCount (){
            return stocknames.size();
        }
    }


    private class StockHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

        private String stockname;
        private String stockticker;
        private String stockprice;
        private TextView nametextView;
        private TextView tickertextView;
        private TextView pricetextView;

        public StockHolder(View itemView){
            super(itemView);
            itemView.setOnLongClickListener(this);
            nametextView =(TextView)itemView.findViewById(R.id.name);
            tickertextView= (TextView)itemView.findViewById(R.id.ticker);
            pricetextView=(TextView)itemView.findViewById(R.id.price);
        }




        @Override
        public boolean onLongClick(View v) {
            Intent launchGraph= new Intent(v.getContext(),GraphActivity.class);

            launchGraph.putExtra("stockticker",stockticker);
            launchGraph.putExtra("stockname",stockname);

            startActivity(launchGraph);
            return false;
        }
        //Actual binder method, maybe add a current

        public void bindStock(String stocknom, String stocktick, String stockpris){
            this.stockname=stocknom;
            this.stockticker = stocktick;
            this.stockprice=stockpris;

            nametextView.setText(stockname);
            tickertextView.setText(stockticker);
            pricetextView.setText(stockprice);
        }


    }


    //----------------------- NewsFetcherAsyncTask

    public static ArrayList<String>reverse(List<String>list){
        int length = list.size();
        ArrayList<String>result  =new ArrayList<String>(length);
        //TODO Check this, we are getting an indexout of bounds error,
        for(int i= length-1;i>=0;i--){
            result.add(list.get(i));
        }
        return result;
    }

    class FetchNewsData extends AsyncTask<String,Void,Boolean> {
        private Stock temp;
        List<HistoricalQuote> data;
        private ProgressDialog pdialog;

        @Override
        protected void onPreExecute() {



        }

        @Override
        protected Boolean doInBackground(String... params) {

            newsItems.clear();
            newsDates.clear();
            newsUrls.clear();
            newsTimes.clear();



            //Proper Query code
            try{
                WebhoseQuery query = new WebhoseQuery();
                query.allTerms.add(params[0]);
                query.siteTypes.add(WebhoseQuery.SiteType.news);
                query.someTerms.add("stock");
                query.someTerms.add("market");
                newsresult = client.search(query);
                for (WebhosePost post:newsresult.posts) {
                    newsItems.add(post.title);
                    String trueDate= post.published.substring(0,10);
                    newsDates.add(trueDate);
                    newsUrls.add(post.url);
                    String trueTime = post.published.substring(11,16);
                    newsTimes.add(trueTime);
                }


            }catch (IOException e){
                return true;
            }
            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result) {
            newsItems = reverse(newsItems);
            newsDates = reverse(newsDates);
            newsUrls = reverse(newsUrls);
            newsTimes = reverse(newsTimes);


            if (nAdapter == null){
                nAdapter = new NewsAdapter(newsItems,newsDates,newsUrls,newsTimes);
                newsRecyclerView.setAdapter(nAdapter);
                newsRecyclerView.invalidate();
            }else{
                updatedNAdapter= new NewsAdapter(newsItems,newsDates,newsUrls,newsTimes);
                newsRecyclerView.swapAdapter(updatedNAdapter,true);
                newsRecyclerView.invalidate();
            }


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
                refreshAllStocks();


                return true;
            case R.id.action_help:
                WelcomeFragment wc= new WelcomeFragment();
                wc.show(getFragmentManager(),"Getting Started");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //-----------------------------------------NewsAdapter

    private class NewsAdapter  extends RecyclerView.Adapter<NewsHolder>{
        private ArrayList<String>newstitles;
        private ArrayList<String>newsdates;
        private ArrayList<String>newsurls;
        private ArrayList<String>newstimes;



        public NewsAdapter(ArrayList<String>titles,ArrayList<String> dates,ArrayList<String>urls, ArrayList<String>times){

            this.newstitles=titles;
            this.newsdates=dates;
            this.newsurls=urls;
            this.newstimes=times;
        }

        @Override
        public NewsHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutinflater = LayoutInflater.from(getActivity());
            View view= layoutinflater.inflate(R.layout.rownews,parent,false);
            return new NewsHolder(view);
        }

        //Bind datato stockholder depending on position in arraylist
        public void onBindViewHolder(NewsHolder holder,  int position){


            String title = newstitles.get(position);
            String date=newsdates.get(position);
            String url= newsurls.get(position);
            String time = newstimes.get(position);
            holder.bindStock(title,date,url,time);

            //Code for showing selected stock background

        }

        @Override
        public int getItemCount (){
            return newstitles.size();
        }
    }


    private class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private String newstitle;
        private String newsdate;
        private String newsurl;
        private String newstime;
        private TextView datetextView;
        private TextView newstextView;
        private TextView timetextView;

        public NewsHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            datetextView =(TextView)itemView.findViewById(R.id.dateText);
            newstextView= (TextView)itemView.findViewById(R.id.newsText);
            timetextView =(TextView)itemView.findViewById(R.id.timeView);

        }




        @Override
        public void onClick(View v) {
            //View news link in browser
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(newsurl));
            startActivity(i);

        }
        //Actual binder method, maybe add a current

        public void bindStock(String newstitl, String newsdat, String newsur, String newstim){
            this.newstitle=newstitl;
            this.newsdate = newsdat;
            this.newsurl=newsur;
            this.newstime =newstim;

            datetextView.setText(newsdate);
            newstextView.setText(newstitle);
            timetextView.setText(newstime);
          ;
        }


    }

//-------------------------------------------FetchDataCode
class FetchXDayData extends AsyncTask<String,Void,Boolean> {
    private Stock temp;
    List<HistoricalQuote> data;
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


    public void  moveResultToUI(Stock result){
        if (result ==null||result.getName().isEmpty()){
            Toast nostockfound = Toast.makeText(getActivity(),"No such stock found, check ticker",Toast.LENGTH_LONG);
            nostockfound.show();
        }else{






            updatednames.add(result.getName());
            updatedtickers.add(result.getSymbol());
            updatedprices.add(result.getQuote().getPrice().toString());






            //
            if (result!= null){
                updatedAdapter = new StockAdapter(updatednames,updatedtickers,updatedprices);
                recyclerView.setAdapter(updatedAdapter);





            }else{
                Toast error = Toast.makeText(getActivity(),"Network Problem",Toast.LENGTH_SHORT);
                error.show();
            }
        }



    }

}
