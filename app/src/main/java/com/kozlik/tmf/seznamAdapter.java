package com.kozlik.tmf;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class seznamAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private ArrayList<String> names;
    private ArrayList<Integer> zaplaceno;
    private final Context c;

    public seznamAdapter(Activity context,
                         ArrayList<String> names, ArrayList<Integer> zaplaceno) {
        super(context, R.layout.rowlayout, names);
        this.context = context;
        this.zaplaceno = zaplaceno;
        this.names = names;
        this.c = context;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        // System.out.println("getView " + position + " " + view);
        LayoutInflater inflater = context.getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.seznam_row, null, true);
        TextView tv_jmena = (TextView) rowView.findViewById(R.id.tv_row_name);
        final CheckBox checkBZaplaceno = (CheckBox) rowView.findViewById(R.id.checkBoxZaplaceno);

        switch (zaplaceno.get(position)) {
            case 0:  //nezaplaceno
                checkBZaplaceno.setChecked(false);
                break;
            case 1:  //zaplaceno
                checkBZaplaceno.setChecked(true);
                break;
            case 2:  // nejde na akci
                rowView.setBackgroundResource(R.color.gray);
                break;
            case 3:  // chyba
                rowView.setBackgroundResource(R.color.red);
                break;

        }

        checkBZaplaceno.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {  //nevim proc to je opacne ale funguje to
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBZaplaceno.isChecked()) {
                    //Toast.makeText(context, "setting" + position + " to " + 1, Toast.LENGTH_SHORT).show();
                    AkceActivity.zaplaceno.set(position, 1);
                } else if (!checkBZaplaceno.isChecked()) {
                    //Toast.makeText(context, "setting" + position + " to " + 0, Toast.LENGTH_SHORT).show();
                    AkceActivity.zaplaceno.set(position, 0);
                }
            }
        });

        if (names.get(position) == null) {
            tv_jmena.setText("Chyba");

        } else {
            tv_jmena.setText(names.get(position));
        }


        return rowView;
    }


}
