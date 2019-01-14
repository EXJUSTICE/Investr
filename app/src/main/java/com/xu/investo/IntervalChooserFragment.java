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

import java.util.Calendar;

/**
 * Created by Omistaja on 07/03/2017.
 */

public class IntervalChooserFragment extends DialogFragment {
    Button twoweeks;
    Button onemonth;
    Button threemonths;
    Button sixmonths;
    Button twelvemonths;
    static Boolean isDateEdit;
    SharedPreferences defaultDates;
    SharedPreferences.Editor edit;

    IntervalChooserFragment.setDefaultIntervalInterface interfacer;
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

        View dialogView = inflater.inflate(R.layout.fragment_interval_chooser, container, false);
        twoweeks= (Button) dialogView.findViewById(R.id.twoweeks);
        onemonth= (Button) dialogView.findViewById(R.id.onemonth);
        threemonths= (Button) dialogView.findViewById(R.id.threemonths);
        sixmonths= (Button) dialogView.findViewById(R.id.sixmonths);
        twelvemonths= (Button) dialogView.findViewById(R.id.twelvemonths);

        defaultDates = getActivity().getSharedPreferences("defaultDates",Context.MODE_PRIVATE);
        edit =defaultDates.edit();

        twoweeks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("defaultInterval","0.5");
                edit.putBoolean("userModified",true);
                edit.commit();
                interfacer.updateDefaultInterval();
                dismiss();
            }

        });

        onemonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("defaultInterval","1");
                edit.putBoolean("userModified",true);
                edit.commit();
                interfacer.updateDefaultInterval();
                dismiss();
            }
        });

        threemonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("defaultInterval","3");
                edit.putBoolean("userModified",true);
                edit.commit();
                interfacer.updateDefaultInterval();
                dismiss();
            }

        });

        sixmonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("defaultInterval","6");
                edit.putBoolean("userModified",true);
                edit.commit();
                interfacer.updateDefaultInterval();
                dismiss();
            }

        });

        twelvemonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("defaultInterval","12");
                edit.putBoolean("userModified",true);
                edit.commit();
                interfacer.updateDefaultInterval();
                dismiss();
            }

        });







        return dialogView;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            interfacer= (IntervalChooserFragment.setDefaultIntervalInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement setDefaultIntervalInterface");
        }
    }


    public interface setDefaultIntervalInterface{
        public void updateDefaultInterval();
    }

}
