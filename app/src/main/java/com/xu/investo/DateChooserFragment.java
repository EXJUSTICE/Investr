package com.xu.investo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateChooserFragment extends android.support.v4.app.DialogFragment {
    static TextView startD;
    static TextView endD;
    //we need two sets, since one is real month one is java code
    static String startDay;
    static String endDay;
    Button refresh;
    Button save;
    static Boolean isDateEdit;
    SharedPreferences defaultDates;
    SharedPreferences.Editor edit;

    passDatesToActivityInterface interfacer;
    private Context mContext;
    DatePickerDialog.OnDateSetListener dateSetListener;
    Calendar cal;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_TITLE, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //need to design g(STYLE_NO_TITLE,0);
        mContext = getActivity();

        View dialogView = inflater.inflate(R.layout.fragment_date_chooser, container, false);
        startD = (TextView) dialogView.findViewById(R.id.startDate);
        endD = (TextView) dialogView.findViewById(R.id.endDate);
        refresh = (Button) dialogView.findViewById(R.id.refreshButton);
        //save =(Button)dialogView.findViewById(R.id.saveButton);
        defaultDates = getActivity().getSharedPreferences("defaultDates",Context.MODE_PRIVATE);
        edit =defaultDates.edit();

        startD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDateEdit = true;
                showDatePickerDialog(view);
            }

        });

        endD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDateEdit = false;
                showDatePickerDialog(view);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call some interface method to comm back to Activity with our dates
                //Seems to work now 05022017?
                interfacer.passDates(startD.getText().toString(), endD.getText().toString());

                dismiss();
            }

        });

       /* save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Added in date saving
                interfacer.passDates(startD.getText().toString(), endD.getText().toString());
                edit.putString("startD",startD.getText().toString());
                edit.putString("endD", endD.getText().toString());
                edit.commit();
                dismiss();
            }
        });
        */
        cal = Calendar.getInstance();
        this.getDialog().setTitle("Set Analysis Interval");




        return dialogView;
    }

    public void showDatePickerDialog(View v){
        DialogFragment datepicker = new SelectDateFragment();
        datepicker.show(getFragmentManager(),"DatePicker");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            interfacer= (passDatesToActivityInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RecyclerUpdateInterface");
        }
    }


    public interface passDatesToActivityInterface{
        public void passDates(String startDay,String endDay);
    }
    //Inner fragment class

    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm, dd);
        }
        //05022017 works to set the text.
        public void populateSetDate(int year, int month, int day) {
            int realMonth = month+1;
            if(isDateEdit==true){

                //TODO AHA HERE WE HAVE AN ISSUE WE ARE PARSING A DATE WITH REALMONTH

                startD.setText(day+"/"+realMonth+"/"+year);
                startDay= day+"/"+month+"/"+year;

            }
            if(isDateEdit==false){
                endD.setText(day+"/"+realMonth+"/"+year);
                endDay= day+"/"+month+"/"+year;

            }
        }

    }

}
