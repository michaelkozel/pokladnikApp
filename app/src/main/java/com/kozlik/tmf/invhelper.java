package com.kozlik.tmf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class invhelper extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] items1, items2;

    private final Context c;

    public invhelper(Activity context,
                     String[] items1, String[] items2) {
        super(context, R.layout.rowlayout, items1);
        this.context = context;
        this.items1 = items1;
        this.items2 = items2;
        this.c = context;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        // System.out.println("getView " + position + " " + view);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.rowlayout, null, true);
        TextView nazev1 = (TextView) rowView.findViewById(R.id.TJmeno);
        TextView nazev2 = (TextView) rowView.findViewById(R.id.TCastka);


        if (items1[position] == null) {
            nazev1.setText("aaa");

        } else {
            nazev1.setText(items1[position]);
        }
        if (items2[position] == null) {

            nazev2.setText("111222333");
        } else {
            nazev2.setText(items2[position]);
        }


        System.out.println("item1 " + items1[position] + "item2 " + items2[position] + " " + position);
        if (Integer.parseInt(items2[position]) > 0)
            nazev2.setTextColor(context.getResources().getColor(R.color.green));
        else {

            nazev2.setTextColor(context.getResources().getColor(R.color.red));

        }
Log.d("log",rowView.toString());
        return rowView;
    }


}
