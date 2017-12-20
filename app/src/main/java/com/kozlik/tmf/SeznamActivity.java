package com.kozlik.tmf;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Trida seznamu lidi platicich na event
 */
public class SeznamActivity extends AppCompatActivity {
    ListView seznamAkci;
    List akceList;
    String URL = "http://tmf-u12.hys.cz/AndroidAppRequests/payForEvent.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RESPONSE", "ONCreATE");
        setContentView(R.layout.activity_seznam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("nazevTabulky"));
        setSupportActionBar(toolbar);
        seznamAkci = (ListView) findViewById(R.id.lv_seznam);
        seznamAdapter customAdapter = new seznamAdapter(SeznamActivity.this, AkceActivity.jmena, AkceActivity.zaplaceno);
        seznamAkci.setAdapter(customAdapter);
        Window window = this.getWindow();


// barva status baru
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

    }

    void odesliData() {
        Log.d("RESPONSE", "Odesilam");
        JSONArray hodnoty = new JSONArray();

        RequestParams params = new RequestParams();
        for (int i = 0; i < AkceActivity.zaplaceno.size(); i++) {
            Log.d("hodnoty", AkceActivity.zaplaceno.get(i) + " ");
            hodnoty.put(AkceActivity.zaplaceno.get(i));
        }
        String json = hodnoty.toString();
        params.put("zaplaceno", json);
        params.put("tableName", AkceActivity.nazevTabulky);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(SeznamActivity.this, URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(SeznamActivity.this, "Fail " + statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Toast.makeText(SeznamActivity.this, "Success " + statusCode, Toast.LENGTH_SHORT).show();
                Log.d("RESPONSE", responseString);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_aktivita_seznam, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_load:


                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }


    }

    @Override
    public void onBackPressed() {
        odesliData();
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        odesliData();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        odesliData();
        super.onDestroy();
    }

}
