package com.xu.investo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Omistaja on 16/01/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {
    RecyclerView settinglist;
    ArrayList<String>settings;
    SettingsAdapter adapter;
    public static SharedPreferences settingspreferences;
    SharedPreferences.Editor editor;
    public InterfaceCommunicator interfaceCommunicator;
    public int REQUEST_SETTINGS =0;

    public SettingsFragment(){

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_TITLE, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //need to design g(STYLE_NO_TITLE,0);
        initializeSettings();
        View dialogView = inflater.inflate(R.layout.fragment_settings, container, false);
        settinglist=(RecyclerView) dialogView.findViewById(R.id.list);
        settinglist.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.getDialog().setTitle("Select Active Indicators");
        settingspreferences = getActivity().getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
        editor= settingspreferences.edit();

        if(adapter == null){
            SettingsAdapter adapter= new SettingsAdapter(getActivity(),settings);
            settinglist.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }







        return dialogView;
    }

    //interface to allow for call onActivityResult
    public interface InterfaceCommunicator{
        void sendRequestCode();
    }

    //Attaching InterfaceCommunicator
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            interfaceCommunicator = (InterfaceCommunicator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InterfaceCOmmunicator");
        }
    }

    public void initializeSettings(){
        settings = new ArrayList<String>();
        settings.add("SMA 14");
        settings.add("SMA 50");
        settings.add("EMA 14");
        settings.add("EMA 50");
        settings.add("MACDs");
        settings.add("ADXs");

        settings.add("RSIs");
        settings.add("Boillinger bands");
        settings.add("MFVs");
    }

    private class SettingsHolder extends RecyclerView.ViewHolder   {
        SharedPreferences settingspreferences;
        boolean selected;
        TextView settingName;
        CheckBox selectbox;
        SharedPreferences.Editor editor;

        String settingDebug;
        boolean initialselect;

        public SettingsHolder(View itemview) {
            super(itemview);
            settingspreferences = getActivity().getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
            editor= settingspreferences.edit();
            settingName = (TextView) itemview.findViewById(R.id.name);

            //check if setting has been initialized

            selectbox = (CheckBox) itemview.findViewById(R.id.selectedBox);
            selectbox.setEnabled(false);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //TODO error here, always false

                    if (selectbox.isChecked()){
                        selectbox.setChecked(false);
                        editor.putBoolean(settingDebug,false);

                        editor.commit();

                    }else if (!selectbox.isChecked()){
                        // Toast works Toast.makeText(getActivity(),"clicked",Toast.LENGTH_LONG).show();
                        selectbox.setChecked(true);
                        editor.putBoolean(settingDebug,true);

                        editor.commit();

                    }


                    updateWithNewSettings();


                    //Works fine, forgot the goddam commit()


                }
            });

            /*
            selectbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!selectbox.isChecked()){
                        editor.putBoolean(settingDebug,true);
                        selectbox.setChecked(true);
                        editor.commit();
                    }
                    else if (selectbox.isChecked()){
                        editor.putBoolean(settingDebug,false);
                        selectbox.setChecked(false);
                        editor.commit();
                    }
                    updateWithNewSettings();
                }
            });*/




        }




        //Individual viewholder binding only, for access to data from lists consult adapter
        public void bindTopic(String setting){
            settingName.setText(setting);
            settingDebug = setting;

           //Since every tmme we create a settings fragment we will be reinitializing the view, its important to restore checked status
            initialselect =settingspreferences.getBoolean(setting,false);
            selectbox.setChecked(initialselect);

        }

        public void updateWithNewSettings(){
           //OnActivityResult, send message to GraphActivity


            interfaceCommunicator.sendRequestCode();

        }


    }

    public class SettingsAdapter extends RecyclerView.Adapter<SettingsHolder>{
        ArrayList<String>settings;

        public SettingsAdapter(Context context,ArrayList<String>setting){
            settings = setting;
        }

        @Override
        public SettingsHolder onCreateViewHolder(ViewGroup parent, int viewType ){
            LayoutInflater layinf= LayoutInflater.from(getActivity());


            View rowview = layinf.inflate(R.layout.rowsettings, parent,false);

            return new SettingsHolder(rowview);

        }

        public void onBindViewHolder(SettingsHolder holder, int position) {
            settingspreferences = getActivity().getSharedPreferences("settingsprefs", Context.MODE_PRIVATE);
            final String selectedsetting= settings.get(position);
            holder.bindTopic(selectedsetting);




            /*
            //CODE BROKEN
            holder.selectbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Cast CB to view http://android-pratap.blogspot.co.uk/2015/01/recyclerview-with-checkbox-example.html
                    CheckBox cb = (CheckBox)view;

                    if (!cb.isChecked()){
                        editor.putBoolean(selectedsetting,true);
                        cb.setChecked(true);
                        editor.commit();
                    }
                    else if (cb.isChecked()){
                        editor.putBoolean(selectedsetting,false);
                        cb.setChecked(false);
                        editor.commit();
                    }
                    updateWithNewSettings();
                }
            });

            boolean selected = settingspreferences.getBoolean(selectedsetting, false);

            holder.selectbox.setChecked(selected);
            */

        }

        public void updateWithNewSettings(){
            //OnActivityResult, send message to GraphActivity


            interfaceCommunicator.sendRequestCode();

        }

        @Override
        public int getItemCount() {
            return settings.size();
        }






    }








    }
