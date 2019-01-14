package com.xu.investo;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.*;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/*
An important thing to note is that graphs have to bee plotted from left to right
Since all of our data is created with the most DISTANT being index 0


//Should give a spinner for selection for active calculations
//Also on click of a entry should launch a little box window

//TODO 1403 Time to chec if intervals actually work
//TODO add a second graph, then maybe even more in nested scrollviews
TODO 2502 BUG FIXING INPUT FROM QIFEI - RSI PLOTS SHHOULD HAVE STRAIGHT LINE. ADXS ALSO NEED +/- DMIS
TODO ABILITY TO SET CUSTOM SMA LENGTH AND START INTERVAL
 */


//TODO Implement markerview for effective popups, current system doesnt work
//TODO 2 FIX ROUNDING OF NUMBERS
//TODO 3 ADD 9 DAY EMA as SIGNAL LINE TO MACD as SEPARATE DATASET
//https://www.numetriclabz.com/android-line-chart-using-mpandroidchart-tutorial/
public class GraphActivity extends AppCompatActivity implements OnChartGestureListener, SettingsFragment.InterfaceCommunicator,DateChooserFragment.passDatesToActivityInterface, IntervalChooserFragment.setDefaultIntervalInterface {
    private int lastTappedIndex = -1;

    ArrayList<HistoricalQuote> historical;
    ArrayList<Date> historicaldates;
    ArrayList<Float> floatdates;
    List<Entry> entries;
    ArrayList<Integer> testInts;
    //Datemap works with XAxisFormatter to produce good labels that make sense
    HashMap<Integer, String> datemap;

    int Day;
    float Close;
    float value;
    int month;
    int year;
    String stockname;
    String stockticker;
    LineChart chart;
    TextView displayPrice;
    //Smaller chart to display histograms, macds etc. should eventualy be nestscrollviewed
    LineChart auxchart;

    Date datefrom;
    Date dateto;
    TextView startDay;
    TextView endDay;

    boolean isDateEdit1;
    public DialogFragment dateFragment;
    public Calendar mCalendar;
    Button refresh;

    //TextViews for popups
    TextView day;
    TextView price;
    TextView SMA;
    TextView EMA;
    TextView MACD;
    TextView ADX;
    TextView RSI;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    SharedPreferences settingspreferences;
    MethodDatabase metdb;

    //SharedPreferences for dates

    SharedPreferences dateStorage;
    SharedPreferences.Editor dateeditor;
    int REQUEST_SETTINGS = 0;
    public HashMap<String, Boolean> settings;


    //All indicators TODO may have to change all to floats
    ArrayList<Float> SMAs;
    ArrayList<Float> SMA50s;
    ArrayList<Float> EMAs;
    ArrayList<Float> EMA50s;
    ArrayList<Float> MACDs;
    ArrayList<Float> MACDSignals;
    ArrayList<Float> MACDHistograms;
    ArrayList<Float> RSIs;
    ArrayList<Float> RSI30s;
    ArrayList<Float> RSI70s;
    ArrayList<Float> UpperBoillinger;
    ArrayList<Float> LowerBoillinger;
    ArrayList<Float> MiddleBoillinger;
    ArrayList<Float> ADXs;
    ArrayList<Float> DMIplus;
    ArrayList<Float> DMImin;
    ArrayList<Float> MFVs;
    ArrayList<String> indicatorSettings;
    private static final String TAG = "DetailActivity";

    //All LineDataSets of Technical Indicators
    LineDataSet SMAdataset;
    LineDataSet SMA50dataset;
    LineDataSet EMAdataset;
    LineDataSet EMA50dataset;
    LineDataSet MACDdataset;
    LineDataSet MACDSignaldataset;
    LineDataSet ADXdataset;
    LineDataSet DMIplusdataset;
    LineDataSet DMImindataset;
    LineDataSet UpperBoildataset;
    LineDataSet LowerBoildataset;
    LineDataSet MiddleBoildataset;
    LineDataSet MFVdataset;
    LineDataSet RSIdataset;
    LineDataSet dataSet;
    LineDataSet RSI30Linedataset;
    LineDataSet RSI70Linedataset;

    //Calendar for loading initial data of 14 days
    Calendar current;
    Calendar past;
    //popup View holders
    StockMarkerView markerView;
    YAxis auxyaxisleft;
    YAxis auxyaxisright;
    ProgressDialog pDialog;
    String startDate;
    String endDate;
    boolean isOnline;
    SharedPreferences defaultDates;
    //TODO we need to put userModified into db as well
    boolean userModified;
    String userDefaultInterval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        historical = new ArrayList<HistoricalQuote>();
        entries = new ArrayList<Entry>();
        floatdates = new ArrayList<Float>();
        historicaldates = new ArrayList<Date>();
        datemap = new HashMap<Integer, String>();
        testInts = new ArrayList<Integer>();
        settings = new HashMap<String, Boolean>();

        //Load name and ticker for selected stock from List?
        Intent intent = getIntent();
        stockticker = intent.getStringExtra("stockticker");
        //Calling FetchXDayDatprsicker);
        //TODO loadStockData should create a dummy 2 week timeframe
        stockname = intent.getStringExtra("stockname");
        isOnline = checkInternetAccess();

        //Load SavedPrefs for User interval dates, we do this because it happens on NEXT startup
        defaultDates = this.getSharedPreferences("defaultDates", Context.MODE_PRIVATE);
        userModified = defaultDates.getBoolean("userModified",false);
        userDefaultInterval = defaultDates.getString("defaultInterval", "NULL");
        //default date objects for blocking use of technical indicators

        current = Calendar.getInstance();
        past = Calendar.getInstance();



        android.support.v7.app.ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        //menu.setLogo("INSERT LOGO HERE");
        menu.setDisplayUseLogoEnabled(true);
        if (userModified == false) {
            //TODO 25032017 modify user
            past.add(Calendar.DAY_OF_MONTH, -14);
            menu.setTitle(" " + stockname + " 2-week" + " Performance");
            if (isOnline == true) {


                //parseDate Default by 2 weeks
                loadDefaultStockData(stockticker);

            } else {
                Toast.makeText(this, "No Internet connection detected", Toast.LENGTH_LONG);
            }
        }else if (userModified ==true){
            //TODO user has modified
            if (isOnline == true) {

                if (defaultDates.getString("defaultInterval", "NULL").equals("0.5")) {
                    menu.setTitle(" " + stockname + " 2-week" + " Performance");
                }
                if (defaultDates.getString("defaultInterval", "NULL").equals("1")) {
                    menu.setTitle(" " + stockname + " 1-month" + " Performance");
                }

                if (defaultDates.getString("defaultInterval", "NULL").equals("3")) {
                    menu.setTitle(" " + stockname + " 3-month" + " Performance");
                }
                if (defaultDates.getString("defaultInterval", "NULL").equals("6")) {
                    menu.setTitle(" " + stockname + " 6-month" + " Performance");
                }
                if (defaultDates.getString("defaultInterval", "NULL").equals("12")) {
                    menu.setTitle(" " + stockname + " 1-year" + " Performance");
                }

                loadDefaultStockData(stockticker);

            } else {
                Toast.makeText(this, "No Internet connection detected", Toast.LENGTH_LONG);
            }
        }


        //Initialize the Database ,with which we can calculate all the indicators we want
        metdb = MethodDatabase.get();
        settingspreferences = getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
        editor = settingspreferences.edit();

        dateStorage = getSharedPreferences("defaultDates", Context.MODE_PRIVATE);
        dateeditor = dateStorage.edit();


        //initialize chart
        chart = (LineChart) findViewById(R.id.chart);
        displayPrice = (TextView) findViewById(R.id.displayPrice);
        //allow for touch
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        //allow for highlighting when touching values on chart
        chart.setHighlightPerDragEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setOnChartGestureListener(this);
        chart.setNoDataText("Loading...");
        chart.setAutoScaleMinMaxEnabled(true);
        Description empty = new Description();
        empty.setText("");
        chart.setDescription(empty);

        markerView = new StockMarkerView(this, R.layout.markerview_popup);
        markerView.setXDates(datemap);
        markerView.setID(stockname);

        chart.setMarkerView(markerView);
        chart.animateX(2000);


        //CODE WIP
        /*
        if(!dateStorage.getString("startD","Error").equals(null)){
                startDate  = dateStorage.getString("startD","Error");
                endDate = dateStorage.getString("endD","Error");
                passDates(startDate,endDate);
            }else{
         */



        //Initialize AuxChart for MACDs and stuff
        auxchart = (LineChart) findViewById(R.id.auxchart);

        auxchart.setTouchEnabled(true);
        auxchart.setDragEnabled(true);
        auxchart.setScaleEnabled(true);
        auxchart.setPinchZoom(true);
        //allow for highlighting when touching values on chart
        auxchart.setHighlightPerDragEnabled(true);
        auxchart.setHighlightPerTapEnabled(true);
        auxchart.setOnChartGestureListener(this);
        auxchart.setNoDataText("Auxiliary Technical Indicators");
        auxchart.setAutoScaleMinMaxEnabled(true);
        auxchart.setVisibility(View.GONE);
        auxchart.setDescription(empty);
        auxchart.animateX(2000);
        auxyaxisleft = auxchart.getAxisLeft();
        auxyaxisleft.setEnabled(false);

        auxyaxisright = auxchart.getAxisRight();
        auxyaxisright.setDrawZeroLine(true);
        //we only use largevalueformatter if we are viewing MFVs

        resizeCharts();

        //we set the settings for settingsfragment to false so that whenever, the activity launched for the first time
        //we have a blank slate
        setSettingsToFalse();


    }
    //To be honest, theres really not much point to this if we arent doing a live session update
    @Override
    public void updateDefaultInterval() {
        if (!defaultDates.getString("defaultInterval", "NULL").equals("NULL")) {
            //Since not null, we check for interval conditions
            userModified=true;


            userDefaultInterval = defaultDates.getString("defaultInterval", "NULL");

        }
    }


    public boolean checkInternetAccess() {

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


    public void resizeCharts() {
        if (auxchart.getVisibility() != View.VISIBLE) {
            ViewGroup.LayoutParams params = chart.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            chart.setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = chart.getLayoutParams();
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
            params.height = (int) pixels;
            chart.setLayoutParams(params);
        }

    }

    public void setSettingsToFalse() {
        indicatorSettings = new ArrayList<String>();
        indicatorSettings.add("SMA 14");
        indicatorSettings.add("SMA 50");
        indicatorSettings.add("EMA 14");
        indicatorSettings.add("EMA 50");
        indicatorSettings.add("MACDs");
        indicatorSettings.add("ADXs");
        indicatorSettings.add("RSIs");
        indicatorSettings.add("Boillinger bands");
        indicatorSettings.add("MFVs");

        for (int i = 0; i < indicatorSettings.size(); i++) {
            editor.putBoolean(indicatorSettings.get(i), false);
            editor.commit();
        }
    }


    public void showDatePickerDialog(View v) {
        dateFragment = new DateChooserFragment();
        dateFragment.show(getSupportFragmentManager(), "datepicker");
    }

    //Interface method for passing dates back FROM DATECHOOSERFRAGMENT
    @Override
    public void passDates(String startD, String endD) {
        chart.clear();
        //TODO clear all the arraylists as well -SEEMS TO WORK FINE NOW!!!!
        historical.clear();
        entries.clear();
        floatdates.clear();
        historicaldates.clear();
        datemap.clear();
        testInts.clear();


        this.datefrom = parseDate(startD);
        this.dateto = parseDate(endD);
        //TODO added date comparison check
        if (dateto.after(datefrom)) {


            if (isOnline) {
                FetchXDayData getData = new FetchXDayData(getApplicationContext());
                getData.execute(stockticker);
                android.support.v7.app.ActionBar menu = getSupportActionBar();
                menu.setDisplayShowHomeEnabled(true);
                //menu.setLogo("INSERT LOGO HERE");
                menu.setDisplayUseLogoEnabled(true);
                menu.setTitle(" " + stockname + " " + startD + " - " + endD + " Performance");
            } else {
                Toast.makeText(this, "No Internet connection detected", Toast.LENGTH_LONG);
            }
        }

    }


    //------------------------------------------------ AUX METHODS -------------------------------------------------
    public static String parseDateIntoString(Date inputDate) {
        String outputDate;

        //Create a format that user understands in String, then parse the date out of it

        SimpleDateFormat original = new SimpleDateFormat("dd/MMM/yyyy");
        outputDate = original.format(inputDate);
        return outputDate;


    }

    public static Date parseDate(String inputDate) {
        String outputDate;
        Date date = new Date();
        try {
            //TODO Correct format, but for some reason we are actually getting one month before data
            SimpleDateFormat original = new SimpleDateFormat("dd/MM/yyyy");
            date = original.parse(inputDate);
            return date;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "No Date Found");
        return date;


    }


    //Callback for onChartValueSelectedListener

    /*public void onValueSelected(Entry e, Highlight h){
        int dayofmonth=Math.round(e.getX());
        float price= e.getY();
        displayPrice.setText("Price: "+price + "\n"+"Day of Month: "+dayofmonth);

    }
    */

    public void bindDataToGraph() {
        //AxisFormatter works to use testInts in order to fetch the appropriate date String
        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return datemap.get((int) value);
            }

        });
        //TODO We implement a BTC Handler, But doesnt work because no historicalquote


        for (int i = 0; i < historical.size(); i++) {

            Close = historical.get(i).getClose().floatValue();

            //Add into Entries
            entries.add(new Entry(testInts.get(i), Close));
        }


        //Add entries into a DataSet
        dataSet = new LineDataSet(entries, historical.get(0).getSymbol());
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setHighLightColor(Color.YELLOW);

        //Add all dataSets into LineData
        LineData linedata = new LineData(dataSet);
        //FOr setting use on whole data object, if just individual linedataset, use lineDataSet.setValueFormatter instead
        linedata.setValueFormatter(new DecimalValueFormatter());
        chart.setData(linedata);
        chart.animateX(2000);

        chart.invalidate();

    }

    //-------------------------------------AxisFromatter---------------------------


    //------------------------------------------------GESTURES------------------------------------------------------------


    @Override
    public void onChartSingleTapped(MotionEvent me) {
        final Entry entry = chart.getEntryByTouchPoint(me.getX(), me.getY());
        if (entry != null) {
            HistoricalQuote data = (HistoricalQuote) entry.getData();
            //Now that we got the historical quote we move on to  displaying data via android popupWin;dow class
            createPopUp(data, me.getX(), me.getY());

        }
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {


        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            chart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    //DEPRECATED METHOD
    public int turnDateIntoInteger(Calendar date) {
        int day = 0;
        day = date.DAY_OF_MONTH;
        month = date.MONTH;
        year = date.YEAR;
        return day;
        //Need to account for over a year

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }
    //------------------------------------------------------------------------- GESTURE END

    //-----------------------------------------------------POP CODE----------------------------------------------------
    //TODO Not sure if we should actually use HistoricalQuote, more likely some existing arraylists
    //TODO 1407 Old Code, we will be using markerView instead
    public void createPopUp(HistoricalQuote data, float x, float y) {
        PopupWindow pop = new PopupWindow();
        //Fetch inflater and find view to inflate
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);
        pop.setContentView(popupView);
        pop.showAtLocation(chart, Gravity.RIGHT, (int) Math.round(x), (int) Math.round(y));

        //TODO how to show day-specific  values? Probably all arraylists share same indexes
        day = (TextView) popupView.findViewById(R.id.dayView);
        price = (TextView) popupView.findViewById(R.id.priceView);
        SMA = (TextView) popupView.findViewById(R.id.smaView);
        EMA = (TextView) popupView.findViewById(R.id.emaView);
        MACD = (TextView) popupView.findViewById(R.id.macdView);
        ADX = (TextView) popupView.findViewById(R.id.adxView);
        RSI = (TextView) popupView.findViewById(R.id.rsiView);

    }

    public void loadStockData(String ticker) {
        FetchXDayData task = new FetchXDayData(getApplicationContext());
        task.execute(ticker);


    }

    public void loadDefaultStockData(String ticker) {
        if (userModified == false) {
            Fetch12DayData task12 = new Fetch12DayData(getApplicationContext());
            task12.execute(ticker);
        } else if (userModified ==true) {
            //Load user custom interval
            FetchUserIntervalData task22 = new FetchUserIntervalData(getApplicationContext());
            task22.execute(ticker,userDefaultInterval);
        }

    }

    public static long getDifferenceDays(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static long getDaysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // http://stackoverflow.com/questions/27355473/startactivityforresult-from-dialog-fragment
        //http://stackoverflow.com/questions/33032214/android-call-onactivityresult-from-dialog-fragment
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            long daysBetween = getDaysBetween(past, current);
            if (datefrom == null ) {
                if (daysBetween < 30F) {
                    Toast.makeText(this, "To access technical indicators, please set the minimum interval to at least 30 days", Toast.LENGTH_LONG).show();
                } else {
                    //We will use the action settings to actually launch new fragment,
                    //Need to implement startActivityForResult related stuff
                    SettingsFragment settingsFrag = new SettingsFragment();

                    settingsFrag.show(getSupportFragmentManager(), "Settings");
                }
            } else {
                // User has set interval once
                long diff = getDifferenceDays(datefrom, dateto);
                if (diff < 30F) {
                    Toast.makeText(this, "To access technical indicators, please ensure the minimum interval is at least 30 days", Toast.LENGTH_LONG).show();
                } else {
                    //We will use the action settings to actually launch new fragment,
                    //Need to implement startActivityForResult related stuff
                    SettingsFragment settingsFrag = new SettingsFragment();

                    settingsFrag.show(getSupportFragmentManager(), "Settings");
                }
            }


        }
        if (id == R.id.action_setdate) {
            DateChooserFragment dateFrag = new DateChooserFragment();

            dateFrag.show(getSupportFragmentManager(), "Set Date");
        }

        if(id ==R.id.action_setdefault){
            IntervalChooserFragment intervalFrag = new IntervalChooserFragment();

            intervalFrag.show(getSupportFragmentManager(), "Set Default Date");
        }

        return super.onOptionsItemSelected(item);
    }

    //Interface code
    @Override
    public void sendRequestCode() {

        //Store selected settings


        if (settingspreferences.getBoolean("SMA 14", false) == true) {
            settings.put("SMA 14", true);
        }
        if (settingspreferences.getBoolean("SMA 50", false) == true) {
            settings.put("SMA 50", true);
        }
        if (settingspreferences.getBoolean("EMA 14", false) == true) {
            settings.put("EMA 14", true);
        }
        if (settingspreferences.getBoolean("EMA 50", false) == true) {
            settings.put("EMA 50", true);
        }
        if (settingspreferences.getBoolean("MACDs", false) == true) {
            settings.put("MACDs", true);
        }
        if (settingspreferences.getBoolean("ADXs", false) == true) {
            settings.put("ADXs", true);
        }
        if (settingspreferences.getBoolean("Boillinger bands", false) == true) {
            settings.put("Boillinger bands", true);
        }
        if (settingspreferences.getBoolean("MFVs", false) == true) {
            settings.put("MFVs", true);
        }
        if (settingspreferences.getBoolean("RSIs", false) == true) {
            settings.put("RSIs", true);
        }

        // Checking for false conditions

        if (settingspreferences.getBoolean("SMA 14", false) == false) {
            settings.put("SMA 14", false);
        }
        if (settingspreferences.getBoolean("SMA 50", false) == false) {
            settings.put("SMA 50", false);
        }
        if (settingspreferences.getBoolean("EMA 14", false) == false) {
            settings.put("EMA 14", false);
        }
        if (settingspreferences.getBoolean("EMA 50", false) == false) {
            settings.put("EMA 50", false);
        }
        if (settingspreferences.getBoolean("MACDs", false) == false) {
            settings.put("MACDs", false);
        }
        if (settingspreferences.getBoolean("ADXs", false) == false) {
            settings.put("ADXs", false);
        }
        if (settingspreferences.getBoolean("Boillinger bands", false) == false) {
            settings.put("Boillinger bands", false);
        }
        if (settingspreferences.getBoolean("MFVs", false) == false) {
            settings.put("MFVs", false);
        }
        if (settingspreferences.getBoolean("RSIs", false) == false) {
            settings.put("RSIs", false);
        }

        chart.clear();
        auxchart.clear();
        updateGraphSettingsTask updateTask = new updateGraphSettingsTask(this);
        updateTask.execute(settings);


    }

    //-----------------AsyncTask Class for handling indicator creation------------------------------

    class updateGraphSettingsTask extends AsyncTask<HashMap<String, Boolean>, Void, Boolean> {
        private Stock temp;
        List<HistoricalQuote> data;
        public ProgressDialog pdialog;
        private Context context;

        public updateGraphSettingsTask(Context appcontext) {
            this.context = appcontext;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(HashMap<String, Boolean>... params) {

            if (params[0] == null || params[0].isEmpty()) {
                return false;
            }
            //Iterate through settings HashMAP
            if (params[0].containsKey("SMA 14") && params[0].get("SMA 14") == true) {

                SMAs = metdb.getNdaySMA(historical, 14);

                //TODO DateMap and historical size is 252 ,while SMA size is 282, probably due to 30 paramteter of getNDaySMA
        /*for (int i =0;i<SMAs.size();i++){
            Toast.makeText(this,SMAs.get(i).toString(),Toast.LENGTH_LONG).show();
        }*/
                SMAdataset = makeDataIntoGraph(SMAs, "SMA 14");


            } else {
                SMAs = null;
                SMAdataset = null;
            }

            if (params[0].containsKey("SMA 50") && params[0].get("SMA 50") == true) {

                SMA50s = metdb.getNdaySMA(historical, 50);
                //Toast.makeText(this,"smasize "+SMAs.size(),Toast.LENGTH_LONG).show();
                //Toast.makeText(this,"datemap size"+datemap.size(),Toast.LENGTH_LONG).show();
                //Toast.makeText(this,"smasize "+historical.size(),Toast.LENGTH_LONG).show();
                //TODO DateMap and historical size is 252 ,while SMA size is 282, probably due to 30 paramteter of getNDaySMA
        /*for (int i =0;i<SMAs.size();i++){
            Toast.makeText(this,SMAs.get(i).toString(),Toast.LENGTH_LONG).show();
        }*/
                SMA50dataset = makeDataIntoGraph(SMA50s, "SMA 50");


            } else {
                SMA50s = null;
                SMA50dataset = null;
            }


            if (params[0].containsKey("EMA 14") && params[0].get("EMA 14") == true) {
                EMAs = metdb.getNdayEMA(historical, 14);
                EMAdataset = makeDataIntoGraph(EMAs, "EMA 14");

            } else {
                EMAs = null;
                EMAdataset = null;
            }

            if (params[0].containsKey("EMA 50") && params[0].get("EMA 50") == true) {
                EMA50s = metdb.getNdayEMA(historical, 50);
                EMA50dataset = makeDataIntoGraph(EMA50s, "EMA 50");

            } else {
                EMA50s = null;
                EMA50dataset = null;
            }

            if (params[0].containsKey("MACDs") && params[0].get("MACDs") == true) {
                //TODO we will be loading this data into aux chart
                MACDs = metdb.getNDayMACD(historical);
                MACDdataset = makeDataIntoAux(MACDs, "MACDs");
                MACDSignals = metdb.getNDaySignalLine(MACDs);
                MACDSignaldataset = makeDataIntoAux(MACDSignals, "Signal Line");


            } else {
                MACDs = null;
                MACDdataset = null;
                MACDSignals = null;
                MACDdataset = null;
            }

            if (params[0].containsKey("ADXs") && params[0].get("ADXs") == true) {
                ADXs = metdb.getADX(historical);
                ADXdataset = makeDataIntoGraph(ADXs, "ADXs");
                //Is this correct?  Check if its posDM or DM14
                DMImin = metdb.getNegDIs(metdb.getNegDM14s(historical), metdb.getTR14(historical));
                DMIplus = metdb.getPosDIs(metdb.getposDM14(historical), metdb.getTR14(historical));

                DMImindataset = makeDataIntoGraph(DMImin, "-DIs");
                DMIplusdataset = makeDataIntoGraph(DMIplus, "+DIs");


            } else {
                ADXs = null;
                ADXdataset = null;
                DMImin = null;
                DMIplus = null;
                DMImindataset = null;
                DMIplusdataset = null;
            }

            if (params[0].containsKey("Boillinger bands") && params[0].get("Boillinger bands") == true) {
                UpperBoillinger = metdb.getUpperBoilBand(historical);
                LowerBoillinger = metdb.getLowerBoilBand(historical);
                MiddleBoillinger = metdb.getMiddleBoilBand(historical);
                UpperBoildataset = makeDataIntoGraph(UpperBoillinger, "Upper Boillinger band");
                LowerBoildataset = makeDataIntoGraph(LowerBoillinger, "Lower Boillinger band");
                MiddleBoildataset = makeDataIntoGraph(MiddleBoillinger, "Middle Boillinger band");

            } else {
                UpperBoillinger = null;
                LowerBoillinger = null;
                UpperBoildataset = null;
                LowerBoildataset = null;
            }
            if (params[0].containsKey("MFVs") && params[0].get("MFVs") == true) {
                //Need to double check this
                MFVs = metdb.getMoneyFlowVolume(historical);
                MFVdataset = makeDataIntoGraph(MFVs, "MFVs");

            } else {
                MFVs = null;
                MFVdataset = null;
            }
            if (params[0].containsKey("RSIs") && params[0].get("RSIs") == true) {
                RSIs = metdb.getRSI(historical);
                RSIdataset = makeDataIntoGraph(RSIs, "RSIs");

                //add RSI signal lines
                RSI30s = new ArrayList<Float>();
                RSI70s = new ArrayList<Float>();
                for (int i = 0; i < RSIs.size(); i++) {
                    RSI30s.add(30F);
                    RSI70s.add(70F);
                }
                RSI30Linedataset = makeDataIntoGraph(RSI30s, "RSI 30 Line");
                RSI70Linedataset = makeDataIntoGraph(RSI70s, "RSI 70 Line");

            } else {
                RSIs = null;
                RSIdataset = null;


            }


            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result) {
            refreshScreen();
            refreshAux();


        }


    }
    //------------------------------End of AsyncTask


    public void refreshAux() {
        //Bug discovered 04032017- If any one dataset was null, we would set visbility to gone, which is problematic

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        auxchart.clear();
        auxchart.animateX(2000);


        XAxis xaxis = auxchart.getXAxis();
        xaxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return datemap.get((int) value);
            }

        });


        if (MACDdataset != null) {
            dataSets.add(MACDdataset);
            dataSets.add(MACDSignaldataset);
            auxchart.setVisibility(View.VISIBLE);

            resizeCharts();
        } else {

            dataSets.remove(MACDdataset);
            dataSets.remove(MACDSignaldataset);


        }
        if (RSIdataset != null) {
            dataSets.add(RSIdataset);
            dataSets.add(RSI30Linedataset);
            dataSets.add(RSI70Linedataset);
            auxchart.setVisibility(View.VISIBLE);
            resizeCharts();
        } else {
            dataSets.remove(RSIdataset);
            dataSets.remove(RSI30Linedataset);
            dataSets.remove(RSI70Linedataset);


        }
        if (ADXdataset != null) {
            dataSets.add(ADXdataset);
            dataSets.add(DMImindataset);
            dataSets.add(DMIplusdataset);
            auxchart.setVisibility(View.VISIBLE);
            resizeCharts();
        } else {
            dataSets.remove(ADXdataset);
            dataSets.remove(DMImindataset);
            dataSets.remove(DMIplusdataset);


        }
        if (MFVdataset != null) {
            dataSets.add(MFVdataset);
            auxyaxisright.setValueFormatter(new LargeValueFormatter());
            auxyaxisleft.setValueFormatter(new LargeValueFormatter());

            auxchart.setVisibility(View.VISIBLE);

        } else {
            dataSets.remove(MFVdataset);


        }
        //04032017 New check here to set visbility as a whole if data exists or not
        //SYSTEM WORKS NOW
        if (MACDdataset == null && RSIdataset == null && ADXdataset == null && MFVdataset == null) {
            auxchart.setVisibility(View.GONE);
        }
        resizeCharts();

        LineData updatedGroup = new LineData(dataSets);
        updatedGroup.setValueFormatter(new DecimalValueFormatter());
        auxchart.setData(updatedGroup);
        //TODO doesnt look nice but will have to do
        Legend legend = auxchart.getLegend();
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART_INSIDE);

        //auxchart.getLegend().setEnabled(false);


        auxchart.invalidate();


    }

    public void refreshScreen() {
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        chart.clear();
        chart.animateX(2000);


        if (SMAdataset != null) {
            dataSets.add(SMAdataset);
            /*
            for(int i = 0;i<SMAs.size();i++){
                Toast.makeText(getApplicationContext(),SMAs.get(i).toString(),Toast.LENGTH_SHORT).show();
            }
            */

        } else {
            dataSets.remove(SMAdataset);
        }

        if (SMA50dataset != null) {
            dataSets.add(SMA50dataset);
            /*
            for(int i = 0;i<SMAs.size();i++){
                Toast.makeText(getApplicationContext(),SMAs.get(i).toString(),Toast.LENGTH_SHORT).show();
            }
            */

        } else {
            dataSets.remove(SMA50dataset);
        }
        if (EMAdataset != null) {
            dataSets.add(EMAdataset);

        } else {
            dataSets.remove(EMAdataset);
        }

        if (EMA50dataset != null) {
            dataSets.add(EMA50dataset);

        } else {
            dataSets.remove(EMA50dataset);
        }
        /* TODO disabling the following while we make a new method for aux chart
        if(MACDdataset != null){
            dataSets.add(MACDdataset);

        }
        if(RSIdataset != null){

        }
        if(ADXdataset != null){
            dataSets.add(ADXdataset);

        }
        if(MFVdataset != null){
            dataSets.add(MFVdataset);

        }
        */
        if (UpperBoildataset != null) {
            //Both Upper and Lower Boillingers will be set
            dataSets.add(UpperBoildataset);
            dataSets.add(LowerBoildataset);
            dataSets.add(MiddleBoildataset);
        } else {
            dataSets.remove(UpperBoildataset);
            dataSets.remove(LowerBoildataset);
            dataSets.remove(MiddleBoildataset);
        }
        //Add the actual stock data too
        dataSets.add(dataSet);
        LineData updatedGroup = new LineData(dataSets);
        updatedGroup.setValueFormatter(new DecimalValueFormatter());
        chart.setData(updatedGroup);
        chart.invalidate();

    }

    //Self-contained Method that takes a returned indicator and places it in graph
    public LineDataSet makeDataIntoGraph(ArrayList<Float> lists, String name) {
        List<Entry> indicatorEntries = new ArrayList<Entry>();
        Entry indicatorEntry;
        float value = 0;

        for (int i = 0; i < lists.size(); i++) {
            //TODO lets hope that this works - Date should be shared since all data
            //TODO 2 is derived from historicalQuote ArrayList
            //Calendar date = historical.get(i).getDate();

            value = lists.get(i);
            //still fucking 0
            //Toast.makeText(this,Float.toString(value),Toast.LENGTH_LONG).show();
            indicatorEntry = new Entry(testInts.get(i), value);
            indicatorEntries.add(indicatorEntry);

        }
        //Turn into data
        LineDataSet indicatorData = new LineDataSet(indicatorEntries, name);
        indicatorData.setAxisDependency(YAxis.AxisDependency.LEFT);

        //Set color depending on the name of indicator
        if (name.equals("SMA 14")) {
            indicatorData.setColor(Color.DKGRAY);
            indicatorData.setCircleColor(Color.DKGRAY);


        }
        if (name.equals("SMA 50")) {
            indicatorData.setColor(Color.GRAY);
            indicatorData.setCircleColor(Color.GRAY);

        }
        if (name.equals("EMA 14")) {
            indicatorData.setColor(Color.RED);
            indicatorData.setCircleColor(Color.RED);


        }
        if (name.equals("EMA 14")) {
            indicatorData.setColor(Color.YELLOW);
            indicatorData.setCircleColor(Color.YELLOW);
        }
        if (name.equals("MACDs")) {
            indicatorData.setColor(Color.BLUE);
            indicatorData.setCircleColor(Color.BLUE);


        }
        if (name.equals("ADXs")) {
            indicatorData.setColor(Color.CYAN);
            indicatorData.setCircleColor(Color.CYAN);
        }
        if (name.equals("+DIs")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.RED);
        }
        if (name.equals("-DIs")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.GREEN);

        }
        if (name.equals("MFVs")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.BLACK);

        }
        if (name.equals("RSIs")) {
            indicatorData.setColor(Color.YELLOW);
            indicatorData.setCircleColor(Color.YELLOW);
        }
        if (name.equals("RSI 30 Line")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.BLACK);

        }
        if (name.equals("RSI 70 Line")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.BLACK);
        }
        if (name.equals("Upper Boillinger band")) {
            indicatorData.setColor(Color.MAGENTA);
            indicatorData.setCircleColor(Color.MAGENTA);

        }
        if (name.equals("Lower Boillinger band")) {
            indicatorData.setColor(Color.MAGENTA);
            indicatorData.setCircleColor(Color.MAGENTA);
        }
        if (name.equals("Middle Boillinger band")) {
            indicatorData.setColor(Color.MAGENTA);
            indicatorData.setCircleColor(Color.MAGENTA);
        }

        //Tecnically we should make LineData that holds multiple LineDataSets, but meh


        return indicatorData;


    }

    //------------------------------------Auxiliary data chart method for MACDs and stuff

    public LineDataSet makeDataIntoAux(ArrayList<Float> lists, String name) {
        List<Entry> indicatorEntries = new ArrayList<Entry>();
        Entry indicatorEntry;
        float value = 0;

        for (int i = 0; i < lists.size(); i++) {

            //Calendar date = historical.get(i).getDate();

            value = lists.get(i);
            //still fucking 0
            //Toast.makeText(this,Float.toString(value),Toast.LENGTH_LONG).show();
            indicatorEntry = new Entry(testInts.get(i), value);
            indicatorEntries.add(indicatorEntry);

        }
        //Turn into data
        LineDataSet indicatorData = new LineDataSet(indicatorEntries, name);
        indicatorData.setAxisDependency(YAxis.AxisDependency.LEFT);

        //Set color depending on the name of indicator


        if (name.equals("MACDs")) {
            indicatorData.setColor(Color.BLUE);
            indicatorData.setCircleColor(Color.BLUE);
        }
        if (name.equals("Signal Line")) {
            indicatorData.setColor(Color.YELLOW);
        }
        if (name.equals("ADXs")) {
            indicatorData.setColor(Color.CYAN);
            indicatorData.setCircleColor(Color.CYAN);


        }
        if (name.equals("MFVs")) {
            indicatorData.setColor(Color.BLACK);
            indicatorData.setCircleColor(Color.BLACK);


        }
        if (name.equals("RSIs")) {
            //TODO fix RSIs first
        }
        if (name.equals("Upper Boillinger band")) {
            indicatorData.setColor(Color.MAGENTA);
            indicatorData.setCircleColor(Color.MAGENTA);


        }
        if (name.equals("Lower Boillinger band")) {
            indicatorData.setColor(Color.MAGENTA);
            indicatorData.setCircleColor(Color.MAGENTA);

        }

        //Tecnically we should make LineData that holds multiple LineDataSets, but meh


        return indicatorData;


    }

    //---------------------------------------------------------------------------------------------------------
    //Reverse method in order to get the correct order for graphing and calculations
    public static ArrayList<HistoricalQuote> reverse(List<HistoricalQuote> list) {
        int length = list.size();
        ArrayList<HistoricalQuote> result = new ArrayList<HistoricalQuote>(length);

        for (int i = length - 1; i >= 0; i--) {
            result.add(list.get(i));
        }
        return result;
    }


    public void moveResultToStorage(List<HistoricalQuote> history) {
        //reverse and store historicalquotes
        GraphActivity.this.historical = reverse(history);
        //04-02-2017 Debug code -- Seems to pull fine
        for (int i = 0; i < historical.size(); i++) {
            //Toast.makeText(this,historical.get(i).getClose().toString(),Toast.LENGTH_LONG).show();
            //Fetch the day of month and corresponding price for chart
            Calendar date = historical.get(i).getDate();


            //05022017 Overcame ssue with date 0 by using MMM which returns JAN
            String testDate = parseDateIntoString(date.getTime());
            datemap.put(i, testDate);
            //Creating an arrayList of ints that correspond to datemap HashMap, to extract dates
            testInts.add(i);


        }
        bindDataToGraph();
    }

    public void stockNotFound() {
        Toast.makeText(this, "Invalid stock ticker, stock not found. Please check theticker", Toast.LENGTH_LONG);
    }

    //----------------------------------------------------AsyncTask Fetcher classes------------------------------------
    //Inner test class for fetching X days from now
    //Check if getHistory actually fetches till current, or just till yesterday
    class FetchXDayData extends AsyncTask<String, Void, Boolean> {
        private Stock temp;
        List<HistoricalQuote> data;
        public ProgressDialog pdialog;
        private Context context;

        public FetchXDayData(Context appcontext) {
            this.context = appcontext;
        }

        @Override
        protected void onPreExecute() {
            /*04-02-2017 temporarily disable all pDialog code
            pdialog= new ProgressDialog(GraphActivity.this);
            pdialog.setMessage("Fetching stock data...");
            pdialog.show();
            */
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                //Set from/beginning point, NOT SURE IF THIS WILL WORK, a bit wonky
                //TODO instead of passing along serilizable stocklists, we should
                //TODO 2 Instead just pass along dates

                from.setTime(GraphActivity.this.datefrom);
                to.setTime(GraphActivity.this.dateto);
                temp = YahooFinance.get(params[0]);
                //TODO debug testing
                data = temp.getHistory(from, to, Interval.DAILY);

            } catch (java.io.IOException e) {

            }
            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result) {
                /*
                pdialog.dismiss();
                */


            //May have some overlap here
            if (temp.getName().isEmpty()) {
                stockNotFound();
            } else {
                moveResultToStorage(data);
                Toast.makeText(GraphActivity.this, "Stock " + temp.getName() + " successfully added to portofilio", Toast.LENGTH_LONG);

            }


        }


    }

    class Fetch12DayData extends AsyncTask<String, Void, Boolean> {
        private Stock temp;
        List<HistoricalQuote> data;
        public ProgressDialog pdialog;
        private Context context;

        public Fetch12DayData(Context appcontext) {
            this.context = appcontext;
        }

        @Override
        protected void onPreExecute() {
            /*04-02-2017 temporarily disable all pDialog code
            pdialog= new ProgressDialog(GraphActivity.this);
            pdialog.setMessage("Fetching stock data...");
            pdialog.show();
            */
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();

                from.add(Calendar.DAY_OF_MONTH, -14);

                temp = YahooFinance.get(params[0]);

                data = temp.getHistory(from, to, Interval.DAILY);

            } catch (java.io.IOException e) {

            }
            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //API will always return a stock object, even an empty one for an invalid symbol - hence we use getName
            if (temp.getName().isEmpty()) {
                stockNotFound();
            } else {
                moveResultToStorage(data);
            }
                /*
                pdialog.dismiss();
                */


            //May have some overlap here


        }


    }

    class FetchUserIntervalData extends AsyncTask<String, Void, Boolean> {
        private Stock temp;
        List<HistoricalQuote> data;
        public ProgressDialog pdialog;
        private Context context;
        int daysBefore;

        public FetchUserIntervalData(Context appcontext) {
            this.context = appcontext;
        }

        @Override
        protected void onPreExecute() {
            /*04-02-2017 temporarily disable all pDialog code
            pdialog= new ProgressDialog(GraphActivity.this);
            pdialog.setMessage("Fetching stock data...");
            pdialog.show();
            */
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                //TODO now we check for default intervals
                if(params[1].equals("0.5")){
                    daysBefore =-14;

                } if(params[1].equals("1")){
                    daysBefore =-30;

                }if(params[1].equals("3")){
                    daysBefore =-90;

                }if(params[1].equals("6")){
                    daysBefore =-180;

                }
                if(params[1].equals("12")){
                    daysBefore =-360;

                }



                from.add(Calendar.DAY_OF_MONTH, daysBefore);

                temp = YahooFinance.get(params[0]);

                data = temp.getHistory(from, to, Interval.DAILY);

            } catch (java.io.IOException e) {

            }
            return true;
            //this temp is simply returned to onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //API will always return a stock object, even an empty one for an invalid symbol - hence we use getName
            if (temp.getName().isEmpty()) {
                stockNotFound();
            } else {
                moveResultToStorage(data);
            }
                /*
                pdialog.dismiss();
                */


            //May have some overlap here


        }


    }
}
