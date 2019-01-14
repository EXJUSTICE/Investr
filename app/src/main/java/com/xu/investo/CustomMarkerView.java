package com.xu.investo;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * http://stackoverflow.com/questions/35841885/how-to-make-popup-window-on-that-shows-values-of-x-and-y-axis
 * Native way to create popups
 *
 * //Actual setting and assinging layouts is done in GraphActivity
 * CustomMarkerView mv = new CustomMarkerView(Context, R.layout.custom_marker_view_layout);

 // set the marker to the chart
    chart.setMarkerView(mv);
 */

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // Initialize all views here
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //TODO heres where you will getX and getY. Remember different charts have different
        //TODO 2 Y values, hence there is no problem with multiple lines
    }

   /* @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();

    }
    */
}
