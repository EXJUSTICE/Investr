package com.xu.investo;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Singleton class to hold all calculation methods. Allows us to separate out the clutter
 * TODO inspect all of these by hand or by excel, we have an issue regarding the dates
 */

public class MethodDatabase {
    private static MethodDatabase methodDatabase;

    public MethodDatabase(){

    }

    public static MethodDatabase get(){
        if (methodDatabase==null){
            methodDatabase = new MethodDatabase();
        }
        return methodDatabase;
    }



    /* 08-01-2017 added realrange and actual corresponding to excel. Should be perfect now
    The following method returns an arrayList of  SMAs  (for size RANGE) for every day of history ArrayList, no matter what size
    This is then returned as a new ArrayList
    */
    //TODO SMAs will serve as a base for our bug fixing
    public ArrayList<Float> getNdaySMA(List<HistoricalQuote> history, int range){
        float sum =0;
        float SMA = 0;
        float floatrange = (float)range;
        //TODO Although your arraylist has a capacity of 10, the real list has no elements here. The add method is used to insert a element to the real list. Since it has no elements, you can't insert an element to the index of 5.
        ArrayList<Float> SMAs = new ArrayList<Float>(history.size());
        //TODO 2 so first we initialize to correct size in order to access its indexes
        for (int i=0;i<history.size();i++){
            SMAs.add(0F);
        }
        //realRange is made due to the differences in defining "range in calculation vs speech
        //a 10 day range for day 9 is actually from prices of day0 to day9, inclusive
        int realRange =range-1;

        //First step, add in placeholder 0s for the days within the range that have no value
        //so if 10 day range, we have 0-> 9 days with no values

        for (int i=0;i<realRange;i++){
            SMAs.remove(i);
            //little trick here for masking 0
            SMAs.add(i,history.get(i).getClose().floatValue());
        }

        //Next, actually calculate the SMAs for i.e. day 10
        //TODO, changed i to start from realRange (orig i=0), otherwise the range values would be overriden a
        //TODO 2 and array sizes would not match

        // TODO FOR FUCKS SAKE FIXED
        for (int i =realRange;i<history.size();i++){

            //should be k<10, 0......9 = 10 days
            //TODO k starts off as i+ realrange, then we subtract 1  until we get k==i
            for(int k=i;k>=i-realRange;k--){
                //Sum first from k=i+range-1 , go down to i.
                //This should give us a value of RANGE
                sum +=history.get(k).getClose().floatValue();




            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = sum/floatrange;

            //we add the corresponding SMA to index i+range, made up of values calculated from before it
            //to excel
            SMAs.remove(i);
            SMAs.add(i,SMA);
            sum =0;
        }

        return SMAs;
    }


    /* 07-01-2017
    The following method returns an arrayList of EMAs (for size RANGE) for every day of history ArrayList, no matter what size
    This is then returned as a new ArrayList
    */
    public ArrayList<Float>getNdayEMA(List<HistoricalQuote> history, int range) {
        float sum =0;
        //The first EMA will be the SMA
        float SMA=0;
        //Smooth factor defined as 2/(N+1)
        //TODO fixed from displaying simply SMAs 718 by calling it a float, and assigning numbers as float! FYI for future probs
        //TODO 2 always examine the equation and evidence and determine whats the problem! in this case, we had a case of XXX*SMA+SMA =718, so XXX== 0!
        float floatrange = (float)range;
        //To see if our problem derives from integer division, we are changing all numbers to float
        float smoothfactor = 2F/(floatrange+1F);
        float EMA=0;
        float lastEMA=0;
        //realRange exists due to the difference in defining range for calculations vs in speech
        int realRange = range-1;
        float shareprice=0;
        //Results
        ArrayList<Float> EMAs = new ArrayList<Float>(history.size());

        for (int i=0;i<history.size();i++){
            EMAs.add(0F);
        }

        //1.First step, add in placeholder 0s for the days within the range that have no value
        for (int i=0;i<realRange;i++){
            EMAs.remove(i);
            //little trick here for masking 0
            EMAs.add(i,history.get(i).getClose().floatValue());
        }
        //Up till here works fine 08-02-2016




        //2. Calculate the SMA when i=range, this will be the "first EMA"
        for (int i = realRange; i <realRange+1; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            for (int k = i; k >=i-realRange ; k--) {
                //Sum first from start k=i, for the next i+range days
                // (i.e. 10 days we get k=0;k<10;k++)
                sum += history.get(k).getClose().floatValue();


            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = sum / range;
            //Add result as EMA[realRange]
            EMAs.remove(i);
            EMAs.add(i,SMA);

            //Zero the sum now for next calculations
            sum =0;
        }

        //2. Now we can actually start calculating the EMAs for when i =realRnage+1

        for (int i = realRange+1; i < realRange+2; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            // TODO is the line below necessary?? EMs keep showing 718
            // for (int k = i; k >= i-realRange; k--) {
                //Sum first from start k=i, for the next i+range days, (i.e. 10 days we get k=0;k<10;k++)

                //2.a Second EMA, so we use SMA as "lastEMA"


                     shareprice = history.get(i).getClose().floatValue();
            //TODO could the error be simply that smoothfactor is 0
                    EMA = (shareprice-SMA)*smoothfactor +SMA;
                    //set the new EMA as lastEMA
            EMAs.remove(i);
            EMAs.add(i,EMA);
            lastEMA =EMA;
            EMA=0;








        }

        for (int i = realRange+2; i < history.size(); i++) {
            //2.b Third++ EMAs, using previous lastEMA as calculation "lastEMA"

           shareprice = history.get(i).getClose().floatValue();
            EMA = (shareprice-lastEMA)*smoothfactor +lastEMA;
            //For the next run, we again set the current EMA as the lastEMA
            EMAs.remove(i);
            EMAs.add(i,EMA);

            lastEMA=EMA;
            EMA=0;

        }





        return EMAs;

    }
        //Difference between this and normal EMA is that we dont make any values the share price
    public ArrayList<Float>getMACDEMA(List<HistoricalQuote> history, int range) {
        float sum =0;
        //The first EMA will be the SMA
        float SMA=0;
        //Smooth factor defined as 2/(N+1)
        //TODO fixed from displaying simply SMAs 718 by calling it a float, and assigning numbers as float! FYI for future probs
        //TODO 2 always examine the equation and evidence and determine whats the problem! in this case, we had a case of XXX*SMA+SMA =718, so XXX== 0!
        float floatrange = (float)range;
        //To see if our problem derives from integer division, we are changing all numbers to float
        float smoothfactor = 2F/(floatrange+1F);
        float EMA=0;
        float lastEMA=0;
        //realRange exists due to the difference in defining range for calculations vs in speech
        int realRange = range-1;
        float shareprice=0;
        //Results
        ArrayList<Float> EMAs = new ArrayList<Float>(history.size());

        for (int i=0;i<history.size();i++){
            EMAs.add(0F);
        }






        //2. Calculate the SMA when i=range, this will be the "first EMA"
        for (int i = realRange; i <realRange+1; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            for (int k = i; k >=i-realRange ; k--) {
                //Sum first from start k=i, for the next i+range days
                // (i.e. 10 days we get k=0;k<10;k++)
                sum += history.get(k).getClose().floatValue();


            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = sum / floatrange;
            //Add result as EMA[realRange]
            EMAs.remove(i);
            EMAs.add(i,SMA);

            //Zero the sum now for next calculations
            sum =0;
        }

        //2. Now we can actually start calculating the EMAs for when i =realRnage+1

        for (int i = realRange+1; i < realRange+2; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            // TODO is the line below necessary?? EMs keep showing 718
            // for (int k = i; k >= i-realRange; k--) {
            //Sum first from start k=i, for the next i+range days, (i.e. 10 days we get k=0;k<10;k++)

            //2.a Second EMA, so we use SMA as "lastEMA"


            shareprice = history.get(i).getClose().floatValue();
            //TODO could the error be simply that smoothfactor is 0
            EMA = (shareprice-SMA)*smoothfactor +SMA;
            //set the new EMA as lastEMA
            EMAs.remove(i);
            EMAs.add(i,EMA);
            lastEMA =EMA;
            EMA=0;








        }

        for (int i = realRange+2; i < history.size(); i++) {
            //2.b Third++ EMAs, using previous lastEMA as calculation "lastEMA"

            shareprice = history.get(i).getClose().floatValue();
            EMA = (shareprice-lastEMA)*smoothfactor +lastEMA;
            //For the next run, we again set the current EMA as the lastEMA
            EMAs.remove(i);
            EMAs.add(i,EMA);

            lastEMA=EMA;
            EMA=0;

        }





        return EMAs;

    }

    //Another version of EMA, targeted at MACD inputs, so basically return averages of MACDs instead of stocks
    public ArrayList<Float>getNdayEMAforMACD(List<Float> MACDs, int range) {
        float sum = 0;
        //The first EMA will be the SMA
        float SMA = 0;
        //Smooth factor defined as 2/(N+1)
        //TODO fixed from displaying simply SMAs 718 by calling it a float, and assigning numbers as float! FYI for future probs
        //TODO 2 always examine the equation and evidence and determine whats the problem! in this case, we had a case of XXX*SMA+SMA =718, so XXX== 0!
        float floatrange = (float) range;
        //To see if our problem derives from integer division, we are changing all numbers to float
        float smoothfactor = 2F / (floatrange + 1F);
        float EMA = 0;
        float lastEMA = 0;
        //realRange exists due to the difference in defining range for calculations vs in speech
        int realRange = range - 1;
        float shareprice = 0;

        ArrayList<Float> EMAs = new ArrayList<Float>(MACDs.size());

        //1.First step, add in placeholder 0s for the days within the range that have no EMA value -remember we are viewing oldest ones
        for (int i = 0; i < MACDs.size(); i++) {
            EMAs.add(0F);
        }
        for (int i = 0; i < realRange; i++) {
            EMAs.remove(i);
            //little trick here for masking 0
            EMAs.add(i, MACDs.get(i));
        }


        //2. Calculate the SMA when i=range, this will be the "first EMA"
        for (int i = realRange; i < realRange + 1; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            for (int k = i; k >= i - realRange; k--) {
                //Sum first from start k=i, for the next i+range days
                // (i.e. 10 days we get k=0;k<10;k++)
                sum += MACDs.get(k);


            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = sum / floatrange;
            //Add result as EMA[0]
            EMAs.remove(i);
            EMAs.add(i, SMA);

            //Zero the sum now for next calculations
            sum = 0;
        }

        //2. Now we can actually start calculating the EMAs for when i =1, and onwards

        for (int i = realRange + 1; i < realRange + 2; i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA


            float MACD = MACDs.get(i);
            EMA = (MACD - SMA) * smoothfactor + SMA;
            //set the new EMA as lastEMA
            lastEMA = EMA;

            EMAs.remove(i);
            EMAs.add(i, EMA);
            EMA=0;

        }
        for(int i = realRange+2;i<MACDs.size();i++){

                //2.b Third++ EMAs, using previous lastEMA as calculation "lastEMA"

                float MACD = MACDs.get(i);
                EMA = (MACD-lastEMA)*smoothfactor +lastEMA;
                lastEMA=EMA;
            EMAs.remove(i);
            EMAs.add(i, EMA);
            EMA=0;
            }


        return EMAs;

    }


    public String checkSimpleCrossover(float SMA, float shareprice){
        String result="DEBUG";
        if (SMA>shareprice){
            result= "SELL";
        }
        if(SMA<shareprice){
            result= "HOLD";
        }
        if (SMA ==shareprice){
            result ="CROSS";
        }
        return result;
    };

    public String checkExpCrossover(float EMA, float shareprice){
        String result="DEBUG";
        if (EMA>shareprice){
            result= "SELL";
        }
        if(EMA<shareprice){
            result= "HOLD";
        }
        if(EMA==shareprice){
            result ="CROSS";
        }
        return result;
    };

    /*Double crossover commonly using  5 day + 35 day or 50 day + 200 day
    If MAShort>MALong, Bullish, or
    */
    public String checkDoubleCrossover(float MAshort, float MAlong){
        String result="DEBUG";

        if (MAshort>MAlong) {
            result = "HOLD";
        }
        if (MAlong> MAshort){
            result = "SELL";

        }
        if(MAlong== MAshort){
            result = "CROSS";
        }
        return result;

    }

    //------------------------------------------------------------------------------------------------------------------------------------------MACD CALCULATIONS
    //EMA(12)-EMA(26)
    public Float getMACD(float EMAshort, float EMAlong){
        float result = EMAshort-EMAlong;
        return result;
    }

    public String getMACDdecision(float EMAshort, float EMAlong){
        //MACD is simply the divergence between two EMA values for a given day
        String result="DEBUG";
        float MACD = EMAshort-EMAlong;
        if(MACD>0){
            //momentum is positive
            result="POSITIVE MOMENTUM- HOLD";
        }
        if(MACD<0){
            //momentum is negative
            result ="NEGATIVE MOMENTUM- SELL";
        }
        return result;
    }



    //Following is the overall method that returns MACD over a whatever-sized dataset
    //So the first steps would be to calculate the 12Day EMA as well as the 26 day EMA
    //MACD is simply the difference of these
    public ArrayList<Float> getNDayMACD(List<HistoricalQuote>history){
        //The MACD values for a $20 stocks may range from -1.5 to 1.5, while the MACD values for a $100 may range from -10 to +10
        ArrayList<Float>MACDs = new ArrayList<Float>(history.size());
        ArrayList<Float> twelveDayEMAs= new ArrayList<Float>();
        ArrayList<Float> twentysixDayEMAs = new ArrayList<Float>();
        float MACD =0;

        for(int i=0;i< history.size();i++){
            MACDs.add(0F);
        }


        //Lets get the EMAs
        twelveDayEMAs = getNdayEMA(history, 12);
        twentysixDayEMAs = getNdayEMA(history,26);

        //TODO length of EMAs is different. Only at index 26 will we have
        for (int i = 25; i<history.size();i++){
            //MACD is simply the difference betwenthe two EMAs

            MACD =twelveDayEMAs.get(i)-twentysixDayEMAs.get(i);
            MACDs.remove(i);
            MACDs.add(i,MACD);
        }
        return MACDs;
    }

    //Similiary a corresponding method to get the the Signal line, defined as 9 DAY EMA of MACD line
    //Note the input here is MACD list not historical quotes
    public ArrayList<Float>getNDaySignalLine(List<Float>MACDs){
        ArrayList<Float>SignalLines = new ArrayList<Float>();

        SignalLines=getNdayEMAforMACD(MACDs,9);

        return SignalLines;

    }



    //Finally a method that gives you a MACD histogram = MACD Line - Signal Line
    //In theory should be same length right?

    public ArrayList<Float>getMACDHistogram(List<Float>SignalLine, List<Float>MACDs){
        ArrayList<Float>difference = new ArrayList<Float>(SignalLine.size());
        for(int i =0;i<SignalLine.size();i++){
            difference.add(0F);
        }
        for (int i =0; i<SignalLine.size();i++){
            float diff = MACDs.get(i)-SignalLine.get(i);
            //TODO do we need size initialization?
            difference.remove(i);
            difference.add(i,diff);
        }

        return difference;
    }



    //------------------------------------------------------------------------------------------------------------------------------------------RSI calculations

    // Relative Strength Index (tells you if overbought or oversold over the length of a period
    //compare difference in close pricing, measure the number of days and total gains, also measure number of down days and total losses
    //RSI =100 -100/1+RS
    //RS= Average Gain/ Average Loss
    //First Average Gain = Sum of Gains over 14/14
    //First Average Loss = Sum of Losses over 14/14
    //Average Gain =[(lastAverageGain)x13 +currentGain]/14
    //Average Loss =[(lastAverageLoss)x13 +currentLoss]/14
    //Gains are days when difference with last is >0, and vice versa

    //TODO does display but incorrect. figure it out

    public ArrayList<Float> getRSI(List<HistoricalQuote>data){
        ArrayList<Float>RSIs = new ArrayList<Float>(data.size());
        ArrayList<Float>gainDays = new ArrayList<Float>(data.size());
        ArrayList<Float>lossDays = new ArrayList<Float>(data.size());
        ArrayList<Float>averageGain= new ArrayList<Float>(data.size());
        ArrayList<Float>averageLoss = new ArrayList<Float>(data.size());
        ArrayList<Float>RSs =new ArrayList<Float>(data.size());
        int range = 14;
        int realRange=13;
        float floatrange = (float)realRange;
        //Number of days should be 15, bcause 14 needed with chaup nges from previous day
        //TODO changing all day numbers from int to float to avoid integer division
        float totalGain=0F;
        int numberOfUpDays=0;
        float floatUpDays = (float) numberOfUpDays;

        float totalLoss=0F;
        int numberOfDownDays=0;
        float floatDownDays = (float) numberOfDownDays;
        float RS=0F;
        float RSI=0F;
        float averageG=0F;
        float averageL=0F;
        float lastAverageG=0F;
        float lastAverageL=0F;
        float sumG=0F;
        float sumL=0F;
        float difference=0F;



        //0. Initialize all the lists with 0s
        for (int i =0;i<data.size();i++){
            RSIs.add(0F);
            gainDays.add(0F);
            lossDays.add(0F);
            averageGain.add(0F);
            averageLoss.add(0F);
            RSs.add(0F);
        }
        //1. for every day from int=1 , calculate getChange
        //if >0, put into gaindays, and if<0, put into lossdays.
        //Correspondingly put 0 as placeholder in other list
        for(int i=1;i<data.size();i++){
            difference= data.get(i).getClose().floatValue()-data.get(i-1).getClose().floatValue();

            if (difference>0F){


                gainDays.remove(i);
                gainDays.add(i,Math.abs(difference));
                lossDays.remove(i);
                lossDays.add(i,0F);

            }else if(difference<0F){


                gainDays.remove(i);
                gainDays.add(i,0F);
                lossDays.remove(i);
                lossDays.add(i,Math.abs(difference));

            }
        }

        //2. Now that we have an arrayList of gainDays and downDays, we calculate Average gains
        //and Average Losses. But remember, we dont start our  list from i=0, but i=1
        //So we start our corresponding average lists from truerange+1
        for (int i = realRange+1;i<realRange+2;i++){
            for(int k =i;k>=i-realRange;k--){
                sumG+=gainDays.get(i);
                sumL +=lossDays.get(i);
            }
            //Now divide and add into respective lists of averages
            averageG = sumG/14F;
            averageL = sumL/14F;

            sumG=0F;
            sumL=0F;

            lastAverageG= averageG;
            lastAverageL= averageL;

            averageGain.remove(i);
            averageGain.add(i,averageG);
            averageLoss.remove(i);
            averageLoss.add(i,averageL);
        }
        //2.b That was just first average, now we have to add it using the equation
        for (int i = realRange+2;i<data.size();i++){


            averageG = ((lastAverageG*13)+gainDays.get(i))/14F;
            lastAverageG=averageG;
            averageL = ((lastAverageL*13)+lossDays.get(i))/14F;
            lastAverageL=averageL;

            averageGain.remove(i);
            averageGain.add(i,averageG);
            averageLoss.remove(i);
            averageLoss.add(i,averageL);
        }

        //3. Now that thats done, we create a list of RSs,which is simply average gain/averageloss
        for (int  i = realRange+1;i<data.size();i++) {
            //Check that averageLoss at i is not 0
            //if it is, set it to 100, and also take RSI for i as 100 as well
            if(averageLoss.get(i)!=0F){
                RS = averageGain.get(i) / averageLoss.get(i);
                RSs.remove(i);
                RSs.add(i, RS);
                RS = 0F;
            }else{
                RSs.remove(i);
                RSs.add(i,100F);
            }

        }

        //4. Finally, get RSI
        for (int  i = realRange+1;i<data.size();i++) {
            //divide by zero check in RSs carried over
            if (RSs.get(i)!=100F){
                RSI = 100F - (100F/(1F+RSs.get(i)));
                RSIs.remove(i);
                RSIs.add(i,RSI);
            }else{
                RSIs.remove(i);
                RSIs.add(i,100F);
            }

        }


        //TODO we can check for individual outputs simply by exchanging this return with another intermediate
        return RSIs;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------Bollinger Bands
    /*
    Bolinger bands measure normal price fluctuation, are displayed as three bands
    - Middle band is simply SMA(20)
    - Upper band = SMA(20) + (20-day std)*2
    - Lower Band = SMA(20) - (20-day std)*2
     */
    public ArrayList<Float>getMiddleBoilBand(List<HistoricalQuote>data){
        ArrayList<Float> MiddleBand = new ArrayList<Float>();
        MiddleBand = getNdaySMA(data,20);
        return MiddleBand;
    }
    public ArrayList<Float>getUpperBoilBand(List<HistoricalQuote>data){
        ArrayList<Float> upperBand = new ArrayList<Float>(data.size());
        ArrayList<Float>SMAs= new ArrayList<Float>(data.size());
        float SMA = 0;
        float sum =0;
        float upperBollinger;
        float std;
        int realRange = 19;
        float floatRange= (float)realRange;
        //we store the 20 previous items of history here, for Stats STD calculations
        ArrayList<Float> twentySelection = new ArrayList<Float>();
        SMAs = getNdaySMA(data,20);

        //1. Calculate SMAs.
        //a. Add 0 for every single day within range that does not have SMA value
        for (int i=0;i<data.size();i++){
            upperBand.add(0F);
        }
        for (int i=0;i<realRange;i++){
            upperBand.remove(i);
            upperBand.add(i,data.get(i).getClose().floatValue());
        }




        for (int i = realRange; i < data.size(); i++) {

            for (int k = i; k >= i-realRange; k--) {
                /*
                Sum first from start k=i, for the next i+range days
                (i.e. 20days we get k=19;k==0;k--)
                */
                sum += data.get(k).getClose().floatValue();

                //For STD, we now add all next twenty items (from k =i+realRange to k==i) to arrayList to get the 20 day sample
                //NOTE NO ZEROS ADDED TO THIS LIST, ONLY AS A TEMPORARY HOLDER OF 20 VALUES EACH TIME

                twentySelection.add(data.get(k).getClose().floatValue());

            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = SMAs.get(i);

            //2.Convert ArrayList of twenty items to array for STD

            float[]selection = new float[twentySelection.size()];
            for(int m =0;m<twentySelection.size();m++){
                selection[m] =twentySelection.get(m);
            }
            //2.b calculate the STD for the selection of next twenty items
            std =Statistics.getStdDev(selection,20);


            upperBollinger = SMA +std*2F;
            //Add to final ArrayList, at
            //Now calculate UpperBollinger = SMA +2std the corresponding index i+ realRange
            upperBand.remove(i);
            upperBand.add(i,upperBollinger);

            //Clear both the sum and the STD selection for next value of i
            sum =0F;
            selection =null;
            twentySelection.clear();
            upperBollinger= 0F;
        }
        return upperBand;
    }

    public ArrayList<Float>getLowerBoilBand(List<HistoricalQuote>data){
        ArrayList<Float> lowerBand = new ArrayList<Float>();
        float SMA = 0;
        float sum =0;
        float lowerBollinger;
        float std;
        int realRange= 19;
        //we store the 20 next items of history here, for Stats STD calculations
        ArrayList<Float> twentySelection = new ArrayList<Float>();

        //1. Calculate SMAs.
        //a. Add 0 for every single day within range that does not have SMA value
        for (int i=0;i<data.size();i++){
            lowerBand.add(0F);
        }

        for (int i=0;i<realRange;i++){
            lowerBand.remove(i);
            lowerBand.add(i,data.get(i).getClose().floatValue());
        }
        for (int i = realRange; i < data.size(); i++) {
            //Again, we use the defined range here, so if we wanted 30 day EMAs the first SMA would also be calculated for EMA
            for (int k = i; k >=i-realRange; k--) {
                //Sum first from start k=i, for the next i+range days
                // (i.e. 10 days we get k=0;k<10;k++)
                sum += data.get(k).getClose().longValue();

                //For STD, we now add all next twenty items (from k =i+realRange to k==i) to arrayList to get the 20 day sample
                //NOTE NO ZEROS ADDED TO THIS LIST, ONLY AS A TEMPORARY HOLDER OF 20 VALUES EACH TIME
                twentySelection.add(data.get(k).getClose().floatValue());

            }
            //after summing up, we add calculate SMA and add it to list of SMAs
            SMA = sum / 20F;

            //Convert ArrayList of twenty items to array for STD

            float[]selection = new float[twentySelection.size()];
            for(int m =0;m<twentySelection.size();m++){
                selection[m] =twentySelection.get(m);
            }
            //2.b calculate the STD for the selection of next twenty items
            std =Statistics.getStdDev(selection,20);

            //Now calculate UpperBollinger = SMA +2std
            lowerBollinger = SMA -std*2F;
            //Add to ArrayList, then Zero the sum for next calculations
            lowerBand.remove(i);
            lowerBand.add(i,lowerBollinger);
            //Clear both the sum and the STD selection for next value of i
            sum =0F;
            selection=null;
            twentySelection.clear();
            lowerBollinger= 0F;
        }
        return lowerBand;
    }


//----------------------------------------------------------------------------------------------------------------------------------------------ADX

    /*
    ADXs are supposed to tell us if the trend we have is sustainable, or is it an oscillating environment. Apparently not great but meh
     */

    /*
    One day's true range chosen from maximum of 3
    - Days High-Low
    - Days High-Close
    - Days Low- Close
     */

    //TODO since our oldest data was at index = 0
    //StockCharts = >Current High -Current Low
    //Current High -Previous close
    //Current Low - Previous close



    public ArrayList<Float> getTrueRange(ArrayList<HistoricalQuote>history){
        ArrayList<Float>TRs = new ArrayList<Float>(history.size());
        float HiMinusLow =0F;
        float HiMinusClose =0F;
        float LowMinusClose=0F;
        float TR= 0F;

        for(int i =0;i<history.size();i++){
            TRs.add(0F);
        }

        //for the oldest data point, the only data we can provide is HiMinusLow

        for(int i = 0;i<1;i++){
            HiMinusLow = history.get(i).getHigh().floatValue()-history.get(i).getLow().floatValue();

            TR = HiMinusLow;
            TRs.remove(i);
            TRs.add(i,TR);
            HiMinusLow=0F;
            TR =0F;
        }

        for(int i =1; i<history.size();i++ ){
            HiMinusLow= history.get(i).getHigh().floatValue()-history.get(i).getLow().floatValue();
            HiMinusClose= Math.abs(history.get(i).getHigh().floatValue()-history.get(i-1).getClose().floatValue());
            LowMinusClose= Math.abs(history.get(i).getLow().floatValue()-history.get(i-1).getClose().floatValue());

            TR= Math.max(HiMinusClose,Math.max(HiMinusLow,LowMinusClose));
            TRs.remove(i);
            TRs.add(i,TR);
            HiMinusLow=0F;
            HiMinusClose=0F;
            LowMinusClose=0F;
            TR =0F;
        }
        return TRs;
    }

    //Know that TRs use data from "previous day", hence the DAY 0 =>>> NO TR OR DMs for DAY 0
    // TODO check: This method is due to a discrepancy between ATR and DMs, figure it out first
    public ArrayList<Float> getTrueRangeForADX(ArrayList<HistoricalQuote>history){
        ArrayList<Float>TRs = new ArrayList<Float>(history.size());
        float HiMinusLow =0F;
        float HiMinusClose =0F;
        float LowMinusClose=0F;
        float TR= 0F;

        for(int i =0;i<history.size();i++){
            TRs.add(0F);
        }

        for(int i =1; i<history.size();i++ ){
            HiMinusLow= history.get(i).getHigh().longValue()-history.get(i).getLow().floatValue();
            HiMinusClose= Math.abs(history.get(i).getHigh().longValue()-history.get(i+1).getClose().floatValue());
            LowMinusClose= Math.abs(history.get(i).getLow().longValue()-history.get(i+1).getClose().floatValue());

            TR= Math.max(HiMinusClose,Math.max(HiMinusLow,LowMinusClose));
            TRs.add(i,TR);
        }
        return TRs;
    }


    //ATRs have ranges that are (usually) 14 days long, input being TRs
    public ArrayList<Float> getAverageTrueRange(ArrayList<Float>TRs){
        ArrayList<Float>ATRs = new ArrayList<Float>(TRs.size());
        float sum =0F;
        float ATR =0F;
        float SMA =0F;
        int trueRange = 13;
        float lastATR=0F;

        for(int i =0;i<TRs.size();i++){
            ATRs.add(0F);
        }


        //First ATR is an SMA
        for (int i=trueRange;i<trueRange+1;i++){

            for(int k=i;k>=i-trueRange;k--){
                sum += TRs.get(i);
            }
            SMA = sum/((float)TRs.size());
            ATRs.remove(i);
            ATRs.add(i,SMA);
            sum=0;


        }

        //For the rest, follow a modfied EMA formula
        //ATR = [(lastATR x13) + Current TR] /14
        for (int i =trueRange+1;i<TRs.size();i++){

            //if its just the secondEMA, we are using SMA as the last value
            //TODO watch out for integer division
            if (i ==trueRange+1){
                ATR = ((SMA*13F)+TRs.get(i))/14F;
                ATRs.remove(i);
                ATRs.add(i,ATR);
                lastATR =ATR;
            }
            //For all the other days we can safely use ATR previously
            else{
                ATR = ((lastATR*13F)+TRs.get(i))/14F;
                ATRs.remove(i);
                ATRs.add(i,ATR);
                lastATR = ATR;
            }

        }

        return ATRs;

    }

    ///TODO its not as simple as getHigh(i)-getHigh(i-1). Theres a condition as well
    // TODO Condition is actually simple: if upmove> downmove>0, it =upmove
    // TODO if downmove> upmove and downmove>0, it = downmove
    public ArrayList<Float>getPosDM(ArrayList<HistoricalQuote>history){
        ArrayList<Float>posDMs=new ArrayList<Float>(history.size());
        float posDM=0F;
        float negDM =0F;
        //we need a negDM for comparison purposes

        for(int i =0;i<history.size();i++){
            posDMs.add(0F);
        }


        for (int i=1;i<history.size();i++){
            posDM= Math.abs(history.get(i).getHigh().floatValue()-history.get(i-1).getHigh().floatValue());
            negDM = Math.abs(history.get(i).getLow().floatValue()-history.get(i-1).getLow().floatValue());
            if (posDM>negDM&&posDM>0F){
                posDMs.remove(i);
                posDMs.add(posDM);
            }else{
                posDMs.remove(i);
                posDMs.add(0F);
            }


        }

        return posDMs;
    }

    public ArrayList<Float>getNegDM(ArrayList<HistoricalQuote>history){
        ArrayList<Float>negDMs=new ArrayList<Float>(history.size());
        float negDM=0F;
        float posDM=0F;

        for(int i =0;i<history.size();i++){
            negDMs.add(0F);
        }


        for (int i=1;i<history.size();i++){
            negDM= Math.abs(history.get(i).getLow().floatValue()-history.get(i-1).getLow().floatValue());
            posDM = Math.abs(history.get(i).getHigh().floatValue()-history.get(i).getHigh().floatValue());

            if (negDM>posDM&&negDM>0F){
                negDMs.remove(i);
                negDMs.add(i,negDM);
            }else{
                negDMs.remove(i);
                negDMs.add(i,0F);
            }


        }

        return negDMs;
    }

    //TODO TR14 != ATR!!!!,

    public ArrayList<Float>getTR14(ArrayList<HistoricalQuote>history){
        ArrayList<Float>TR14s = new ArrayList<Float>(history.size());
        ArrayList<Float>TRs = new ArrayList<Float>();
        ArrayList<Float>posDMs = new ArrayList<Float>();
        ArrayList<Float>negDMs = new ArrayList<Float>();

        //TODO again for True Range, is there any point to use truerangeforADX?
        TRs =getTrueRange(history);
        posDMs =getPosDM(history);
        negDMs =getNegDM(history);

        int trueRange = 13;
        float floatRange  =13F;
        float sum =0;
        float lastTR =0;
        float TR =0;

        //Again, fill blanks first, except this time we have n artificial empty first row TODO what does that mean?!?!?!?!?!?! check in e


        for(int i =0; i<history.size();i++){
            TR14s.add(i,0F);

        }

        //TR14 = SUM (14day TR)  when i=range+1, remember first row is 0

        for(int i =trueRange+1;i<trueRange+2;i++){
            for (int k =i;k>=i-trueRange;k--){
                sum += TRs.get(i);

            }
            TR14s.remove(i);
            TR14s.add(i,sum);
            lastTR = sum;
            sum=0;
        }

        //For post i=2, we use equation
        //lastTR14-(lastTR14/14)+Current TR1

        for(int i =trueRange+2;i< history.size();i++){
            TR =lastTR -(lastTR/14)+TRs.get(i);
            TR14s.remove(i);
            TR14s.add(i,TR);
            lastTR =TR;

        }

        return TR14s;




        //
    }
    //calculation that returns smoothed out posDMs, defined as SUM of 14 DMs for first DM14,
    //followed by the equation LastDM14-(LastDM14/14) +currentPosDM1

    public ArrayList<Float> getposDM14 (ArrayList<HistoricalQuote>history){
        ArrayList<Float>posDM14s = new ArrayList<Float>(history.size());
        ArrayList<Float>posDMs= new ArrayList<Float>();
        int trueRange = 13;
        float sum =0;
        float lastPosDM=0;
        float posDM=0;

        //Fix method to make "oldest row" DM =0, plus of course the range needs to have 0-valued DMs as well
        posDMs = getPosDM(history);


        for(int i =0;i<history.size();i++){
            posDM14s.add(0F);
        }

        //calculate SUM for when i = range to i=1 the first posDM14 value,

        for(int i=trueRange+1;i<trueRange+2;i++){
            for(int k=i;k>=i-trueRange;k--){
                sum +=posDMs.get(i);
            }
            posDM14s.remove(i);
            posDM14s.add(i,sum);
            lastPosDM =sum;
            sum=0;
        }

        //For post i=1, we use equation
        //lastDM14-(lastTDM144/14)+Current DM1

        for(int i =trueRange+2;i<history.size();i++){
            posDM = lastPosDM - (lastPosDM/14)+posDMs.get(i);
            posDM14s.remove(i);
            posDM14s.add(i,posDM);

            lastPosDM = posDM;


        }

        return posDM14s;
    }

    //Same calculation for getPosDM14s
    public ArrayList<Float>getNegDM14s(ArrayList<HistoricalQuote>history){
        ArrayList<Float>negDM14s = new ArrayList<Float>(history.size());
        ArrayList<Float>negDMs= new ArrayList<Float>();
        int trueRange = 0;
        float sum =0;
        float lastNegDM=0;
        float negDM=0;

        //Fix method to make "oldest row" DM =0, plus of course the range needs to have 0-valued DMs as well
        negDMs = getNegDM(history);

        //Again, fill blanks first, except this time we have n artificial empty first row
        for(int i =0;i<history.size();i++){
            negDM14s.add(i,0F);
        }

        //calculate SUM for when i = range to i=1 the first posDM14 value,

        for(int i=trueRange+1;i<trueRange+2;i++){
            for(int k=i;k>=i-trueRange;k--){
                sum +=negDMs.get(i);
            }
            negDM14s.remove(i);
            negDM14s.add(i,sum);
            lastNegDM =sum;
            sum=0;
        }

        //For post i=1, we use equation
        //lastDM14-(lastTDM144/14)+Current DM1

        for(int i =trueRange+2;i<history.size();i++){
            negDM = lastNegDM - (lastNegDM/14)+negDMs.get(i);
            negDM14s.remove(i);
            negDM14s.add(i,negDM);

            lastNegDM = negDM;


        }

        return negDM14s;
    }
    //TODO note that DM14s inputs are posDM14 and negDM14 respectively
    public ArrayList<Float> getPosDIs(ArrayList<Float>DM14s, ArrayList<Float>TR14s){
        //we cant divide by 0, so first we take care of the first 14 spaces by assigning 0 to them
        ArrayList<Float>posDIs= new ArrayList<Float>(TR14s.size());
        float posDI =0F;


        for(int i=0;i <TR14s.size();i++){
            posDIs.add(0F);
        }

        //Now for i =14 and onwards to end of sample size, we do 100x(DM14/TR14)
        //TODO one of these arrays is not the same size
        for(int i =14;i<TR14s.size();i++){
            posDI = (DM14s.get(i)/TR14s.get(i))*100F;
            posDIs.remove(i);
            posDIs.add(i,posDI);

        }
        return posDIs;

    }

    public ArrayList<Float> getNegDIs(ArrayList<Float>DM14s, ArrayList<Float>TR14s){
        //we cant divide by 0, so first we take care of the first 14 spaces by assigning 0 to them
        ArrayList<Float>negDIs= new ArrayList<Float>(TR14s.size());
        float negDI =0F;
        for(int i=0;i <TR14s.size();i++){
            negDIs.add(0F);
        }

        //Now for i =14 and onwards to end of sample size, we do 100x(DM14/TR14)
        for(int i =14;i<TR14s.size();i++){
            negDI = DM14s.get(i)/TR14s.get(i)*100F;
            negDIs.remove(i);
            negDIs.add(i,negDI);

        }
        return negDIs;

    }

    public ArrayList<Float>getDISums(ArrayList<Float>posDI14s, ArrayList<Float>negDI14s){
        ArrayList<Float>DISums = new ArrayList<Float>(posDI14s.size());
        float sum;
        //Since both posDI14s and negDI14s have 0s in them already from the beginning
        //DISums will have appropriate 0

        for(int i=0;i<posDI14s.size();i++) {
            DISums.add(0F);
        }
        for (int i =14; i<posDI14s.size();i++){
            sum = posDI14s.get(i)+negDI14s.get(i);
            DISums.remove(i);

            DISums.add(i,sum);
        }

        return DISums;

    }

    public ArrayList<Float>getDIDiffs(ArrayList<Float>posDI14s, ArrayList<Float>negDI14s){
        ArrayList<Float>DIDiffs = new ArrayList<Float>(posDI14s.size());
        float diff=0F;

        for (int i=0;i<posDI14s.size();i++){
            DIDiffs.add(0F);
        }
        for(int i =14; i<posDI14s.size();i++){
             diff = posDI14s.get(i)-negDI14s.get(i);
            diff =Math.abs(diff);
            DIDiffs.remove(i);
            DIDiffs.add(i,diff);
        }

        return DIDiffs;

    }

    public ArrayList<Float>getDXs(ArrayList<Float>DISums, ArrayList<Float>DIDiffs){
        ArrayList<Float>DXs = new ArrayList<Float>(DISums.size());
        float DX;
        for (int i  =0;i<DISums.size();i++){
            DXs.add(0F);

        }

        for (int j =14;j<DISums.size();j++){
           DX = 100*(DIDiffs.get(j)/DISums.get(j));
            DXs.remove(j);
            DXs.add(j,DX);
        }
        return DXs;
    }

    // Finally, One step solution to calculating ADX. Pretty fucking sweet.
    //TODO Works finally! seeing alot of 100s tho, check it!
    public ArrayList<Float>getADX(ArrayList<HistoricalQuote>history){
        ArrayList<Float>ADXs = new ArrayList<Float>(history.size());
        ArrayList<Float>posDMs=new ArrayList<Float>();
        ArrayList<Float>negDMs=new ArrayList<Float>();
        ArrayList<Float>TRs = new ArrayList<Float>();
        ArrayList<Float>ATRs = new ArrayList<Float>();
        ArrayList<Float>TR14s = new ArrayList<Float>();
        ArrayList<Float>posDM14s = new ArrayList<Float>();
        ArrayList<Float>negDM14s = new ArrayList<Float>();

        ArrayList<Float>posDIs = new ArrayList<Float>();
        ArrayList<Float>negDIs = new ArrayList<Float>();
        ArrayList<Float>DISums = new ArrayList<Float>();
        ArrayList<Float>DIDiffs= new ArrayList<Float>();
        ArrayList<Float>DXs = new ArrayList<Float>();


        //Calculate DMs first
        posDMs = getPosDM(history);
        negDMs = getNegDM(history);
        //TODO is it trueRange or TrueRangeforADX
        TRs = getTrueRange(history);
        //Since TR already makes first row 0, ATR should also have first row 0
        ATRs = getAverageTrueRange(TRs);


        TR14s = getTR14(history);
        posDM14s = getposDM14(history);;
        negDM14s = getNegDM14s(history);

        posDIs = getPosDIs(posDM14s,TR14s);
        negDIs = getNegDIs(negDM14s,TR14s);
        DISums=getDISums(posDIs,negDIs);
        DIDiffs = getDIDiffs(posDIs,negDIs);
        DXs=getDXs(DISums,DIDiffs);

        //Now we can actually start calculating ADXs.
        //First we append the result list with  0s from when i =0;i==26 (consult excel)
        for (int i =0; i<history.size();i++){
            ADXs.add(0F);
        }

        //for the rest, its a simple average of DXs
        //All of this needs to be checked was pretty tired
        for(int i=27;i<history.size();i++){
            float sum =0;
            float average= 0;
            for (int j =i;j>=i-13;j--){
                sum += DXs.get(j);

            }
            average= sum/14F;
            ADXs.remove(i);
            ADXs.add(i,average);
            sum =0;
            average=0;
        }


        return ADXs;
    }

//----------------------------------------------------------------------------------------------------------------------------Accumulation/Distribution line
    /*Very responsive indicator, lets you know if money is flowing in or out. Highly positive means strong buying pressure
    Highly negative number means strong selling pressure.
    Lets you see if a trend is sutainable
    Uptrend in prices + downtrend in ADL = selling pressure that may foreshadow bearish reversal
    Downtrend in prices + uptrend in ADL = possible bullish reversal
    */

    //Calculate Money FLow Multiplier = [(Close- Low) - (High- Close)]/(High-Low)

    public ArrayList<Float> getMoneyFlowMultiplier(ArrayList<HistoricalQuote> history) {
        ArrayList<Float> MFMs = new ArrayList<Float>(history.size());


        for (int i = 0; i < history.size(); i++) {
            float value = 0;

            value = ((history.get(i).getClose().floatValue() - history.get(i).getLow().floatValue()) - (history.get(i).getHigh().floatValue() - history.get(i).getClose().floatValue())
                    / (history.get(i).getHigh().floatValue() - history.get(i).getLow().floatValue()));

            MFMs.add(value);
        }

        return MFMs;

    }
    //Simply MFM x corresponding volume

    public ArrayList<Float>getMoneyFlowVolume(ArrayList<HistoricalQuote>history){
        ArrayList<Float>MFVs = new ArrayList<Float>(history.size());
        ArrayList<Float>MFMs = getMoneyFlowMultiplier(history);

        for (int i=0; i<history.size();i++){
            float volume =0;
            volume =MFMs.get(i)*history.get(i).getVolume().floatValue();
            MFVs.add(volume);
        }

        return MFVs;
    }

    public ArrayList<Float>getADL(ArrayList<HistoricalQuote>history){
        ArrayList<Float>ADLs = new ArrayList<Float>(history.size());
        ArrayList<Float>MFVs= getMoneyFlowVolume(history);
        float ADL =0;
       float lastADL=0;
        //first ADL /oldest ADL is simply corresponding MFV
        for(int i =0; i<1;i++){
            ADLs.add(MFVs.get(i));
            lastADL = MFVs.get(i);

        }
        //for rest its simply last ADL + current MFV
        for(int i=1;i<history.size();i++){
            ADL = lastADL + MFVs.get(i);
            ADLs.add(ADL);
            lastADL = ADL;
        }

        return ADLs;


    }





    //------------------------------------------------------------------------------------------------------------------------------------------MACD CALCULATIONS






}
