package com.xu.investo;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Formatting Y axis to show decimal values
 */

public class DecimalValueFormatter  implements IValueFormatter {

    private DecimalFormat mFormat;

    public DecimalValueFormatter() {
        mFormat = new DecimalFormat("###,###,###.##"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return mFormat.format(value);
    }
}
