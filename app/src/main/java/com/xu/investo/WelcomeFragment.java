package com.xu.investo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by Omistaja on 18/02/2017.
 */

public class WelcomeFragment extends android.support.v4.app.DialogFragment {

    SharedPreferences sp;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_TITLE,R.style.DialogStyle);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    //onCreateDialog vs OnCreateView?
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        setStyle(STYLE_NO_TITLE,R.style.DialogStyle);
        View dialogView = inflater.inflate(R.layout.fragment_welcome, container, false);
        CheckBox shown= (CheckBox)dialogView.findViewById(R.id.showBox);
        shown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferences.Editor editor =sp.edit();
                    editor.putBoolean("welcomeShown", true);
                    editor.commit();
                    dismiss();
                }

            }
        });


        return dialogView;
    }


}
