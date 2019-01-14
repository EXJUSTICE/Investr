package com.xu.investo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Omistaja on 07/01/2017.
 */

public class Statistics {

        float[] data;
        int size;

        public Statistics(float[] data,int size)
        {
            this.data = data;
            size = data.length;
        }

        public static float getMean(float[]data, int size)
        {
            float floatSize= (float)size;

            float sum = 0F;
            for(float a : data){
                sum += a;
            }

            return sum/floatSize;
        }

        public static float getVariance(float[]data,int size)
        {
            float floatSize= (float)size;
            float mean = getMean(data, size);
            float temp = 0;
            for(double a :data) {
                temp += (a - mean) * (a - mean);
            }
            return temp/floatSize;
        }

        public static float getStdDev(float[]data, int size)
        {
            return (float)Math.sqrt(getVariance(data, size));
        }

         public static float median(float[]data)
        {
            Arrays.sort(data);

            if (data.length % 2 == 0)
            {
                return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2F;
            }
            return data[data.length / 2];
        }

    //Long class is java class, long is primitve type. following method converts arraylist Long to long[]
        public static float[] convertFloat(ArrayList<Float> data){
            float [] ret = new float[data.size()];
            Iterator<Float> iterator = data.iterator();
            for(int i =0; i<ret.length;i++){
                ret[i] = iterator.next().longValue();
            }
            return ret;
        }

}
