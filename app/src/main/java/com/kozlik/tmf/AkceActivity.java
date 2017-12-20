package com.kozlik.tmf;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Aktivita seznamu akci
 */
public class AkceActivity extends AppCompatActivity {
    ListView lv_akce;
    TextView tv_Akce;
    static String nazevTabulky = "";
    static ArrayList<String> jmena;
    static ArrayList<Integer> zaplaceno;
    static String TAG = AkceActivity.class.getSimpleName();
    String getUsersForActionURL = "http://tmf-u12.hys.cz/AndroidAppRequests/getUsersForAction.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akce);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Akce");
        setSupportActionBar(toolbar);
        tv_Akce = (TextView) findViewById(R.id.tv_Akce);
        lv_akce = (ListView) findViewById(R.id.lv_akce);
        Intent i = this.getIntent();
        if (MainActivity.akceList == null) {
            lv_akce.setVisibility(View.INVISIBLE);
            tv_Akce.setText("Žádná akce");
        } else {
            tv_Akce.setText("Akce:");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.row_akce, R.id.rowAkce, MainActivity.akceList);
            lv_akce.setAdapter(adapter);
        }
        lv_akce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AsyncHttpClient client = new AsyncHttpClient();
                nazevTabulky = MainActivity.idAkceList.get(i);
                Log.d(TAG, nazevTabulky);
                Toast.makeText(getApplication(), nazevTabulky, Toast.LENGTH_SHORT).show();
                RequestParams params = new RequestParams("titulek", nazevTabulky);
                client.post(getUsersForActionURL, params, new TextHttpResponseHandler() {


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                     Toast.makeText(getApplicationContext(),"Problém s načtením uživatelů",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        String parsed = parseTextToJSON(responseString);
                        parseJSON(parsed);

                    }
                });

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void parseJSON(String parsedJSONString) {

        try {
            JSONObject jsonObj = new JSONObject(parsedJSONString);
            JSONArray UsersForAction = jsonObj.getJSONArray("UsersForAction");
            jmena = new ArrayList<String>();
            zaplaceno = new ArrayList<Integer>();
            for (int i = 0; i < UsersForAction.length(); i++) {
                JSONObject c = UsersForAction.getJSONObject(i);
                jmena.add(c.getString("Name") + " " + c.getString("Surname"));
                int zaplatil = c.getInt("Zaplaceno");
                switch (zaplatil) {
                    case 0:
                        zaplaceno.add(0);
                        break;
                    case 1:
                        zaplaceno.add(1);
                        break;
                    case 2:
                        zaplaceno.add(2);
                        break;
                    default:
                        zaplaceno.add(3);
                        break;
                }


            }
            spustitAktivituSeznamu();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void spustitAktivituSeznamu() {
        Intent i = new Intent(this, SeznamActivity.class);
        i.putExtra("nazevTabulky", nazevTabulky);
        startActivity(i);
    }

    private String parseTextToJSON(String s) {
        int zacatek = s.indexOf("{");
        int konec = s.length();
        String text = s.substring(zacatek, konec);
        Log.d("Main2Activity", text);
        return text;

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
