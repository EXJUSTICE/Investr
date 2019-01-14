package com.xu.investo;

import java.io.Serializable;

/**
 * model class with data on period, inspired by the PopularMovies App design
 * This way we dont need to call indicator methods from MethodDB in two separate activities
 * Not sure we will use this tbh
 */

public class Period implements Serializable {
    int high;
    int low;
    int close;

    long SMA;
    long EMA;
    //returns length of EMA/SMA for sorting
    long type;
}
