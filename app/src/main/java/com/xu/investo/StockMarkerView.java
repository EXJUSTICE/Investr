package com.xu.investo;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that handles popups created when one touches items on the graph
 */

public class StockMarkerView extends MarkerView {

    private TextView DateView;
    private TextView PriceView;
    private TextView SMAView;
    private TextView EMAView;
    private HashMap<Integer,String> datemap;
    private String ID="";

    //CustomMarkerView mv = new CustomMarkerView(Context, R.layout.custom_marker_view_layout);

// set the marker to the chart.setMarkerView(mv);

    public StockMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays textViews
        PriceView = (TextView) findViewById(R.id.priceView);

        DateView = (TextView)findViewById (R.id.dateView);
        datemap= new HashMap<Integer,String>();

}

    //Create bunch of setting methods to update the markerview with
    //Create for all TIs too. Check in Graphactivity if theyre true, if so, call set methods here
    public void setXDates (HashMap<Integer,String>dates) {
        this.datemap = dates;

    }

    public void setID (String name){
        this.ID=name;

    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {


        PriceView.setText("Price: " + e.getY()); // set the entry-value as the display text
        DateView.setText("Date: "+datemap.get((int)e.getX()));

    }

    private MPPointF mOffset;
    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

}

