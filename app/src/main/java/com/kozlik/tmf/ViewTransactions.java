package com.kozlik.tmf;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class ViewTransactions extends AppCompatActivity {
    public static TextView tv ;
    ListView listTrans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transactions);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv = (TextView)findViewById(R.id.textView3) ;

String ee = getIntent().getStringExtra("kod"); //html kod ze stranky
        //Toast.makeText(this,ee,Toast.LENGTH_SHORT).show();




tv.setText(MainActivity.getRadky());

        //Toast.makeText(this,count, Toast.LENGTH_SHORT).show();


       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
*/
    }

    @Override
    public void onBackPressed() {
      finish();
    }

}
