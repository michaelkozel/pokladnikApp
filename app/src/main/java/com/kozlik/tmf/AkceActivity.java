package com.kozlik.tmf;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
    /**
     * Základní adapter pro listview
     */
    ArrayAdapter<String> adapter;
    ListView lv_akce;
    TextView tv_Akce;
    static String nazevTabulky = "";
    static String titleAkceZobrazen = "";
    /**
     * Arraylist s celymi jmeny lidi
     */
    static ArrayList<String> jmena;
    /**
     * Arraylist s daty o zaplacení seřazenými stejně jako arraylist s lidmi
     */
    static ArrayList<Integer> zaplaceno;
    static String TAG = AkceActivity.class.getSimpleName();
    String getUsersForActionURL = "";
    String deleteEventURL = "";
    String adresaGetJSON = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akce);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Akce");
        setSupportActionBar(toolbar);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String webURL = SP.getString("adress", "xxx");
        String heslo = SP.getString("heslo", "xxx");
        getUsersForActionURL = webURL + "/AndroidAppRequests/getUsersForAction.php";
        deleteEventURL = webURL + "/AndroidAppRequests/deleteEvent.php";
        adresaGetJSON = webURL + "/AndroidAppRequests/GetJsonEvents.php";
        tv_Akce = (TextView) findViewById(R.id.tv_Akce);
        lv_akce = (ListView) findViewById(R.id.lv_akce);
        Intent i = this.getIntent();
        if (MainActivity.akceList.size() == 0) {
            lv_akce.setVisibility(View.INVISIBLE);
            tv_Akce.setText("Žádná akce");
        } else {
            tv_Akce.setText("Akce:");
            adapter = new ArrayAdapter<String>(this,
                    R.layout.row_akce, R.id.rowAkce, MainActivity.akceList);
            lv_akce.setAdapter(adapter);
        }
        lv_akce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                aktualizovatList(i);
            }
        });
        lv_akce.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                smazatEvent(i); //i je pozice v seznamu
                return true;
            }
        });
    }

    private void smazatEvent(final int i) {

        AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Odstranit akci");
        // set dialog message
        alertDialogBuilder
                .setMessage("Opravdu chcete odstranit akci?")
                .setCancelable(false)
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        AsyncHttpClient client = new AsyncHttpClient();
                        nazevTabulky = MainActivity.idAkceList.get(i);
                        Log.d("smazatEvent", nazevTabulky);
                        RequestParams params = new RequestParams("id", nazevTabulky);
                        client.post(deleteEventURL, params, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(getApplicationContext(), "Problém se smazáním akce", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Toast.makeText(getApplicationContext(), "Success akce smazána", Toast.LENGTH_SHORT).show();
                                Log.d("deleteResponse", responseString);
                                aktualizujSeznam();
                            }
                        });
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(), "Odstranění akce zrušeno", Toast.LENGTH_SHORT).show();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }

    private void aktualizujSeznam() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(adresaGetJSON, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {

                String parsedJSONString = parseTextToJSON(s);
                parseJSONAkce(parsedJSONString);

            }

        });
    }

    void aktualizovatList(int position) {
        int i = position;
        AsyncHttpClient client = new AsyncHttpClient();
        nazevTabulky = MainActivity.idAkceList.get(i);
        titleAkceZobrazen = MainActivity.akceList.get(i);
        Log.d("nazevTabulky", nazevTabulky);
        //  Toast.makeText(getApplication(), nazevTabulky, Toast.LENGTH_SHORT).show();
        RequestParams params = new RequestParams("titulek", nazevTabulky);
        client.post(getUsersForActionURL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Problém s načtením uživatelů", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String parsed = parseTextToJSON(responseString);
                parseJSON(parsed);

            }
        });

    }

    private void parseJSONAkce(String parsedJSONString) {
        MainActivity.akceList.clear();
        MainActivity.idAkceList.clear();
        String textInTextview = "";
        try {
            JSONObject jsonObj = new JSONObject(parsedJSONString);
            JSONArray akce = jsonObj.getJSONArray("Akce");
            for (int i = 0; i < akce.length(); i++) {
                JSONObject c = akce.getJSONObject(i);

                String datum = c.getString("datum");
                String popis = c.getString("popis");
                String cena = c.getString("cena");
                String titulek = c.getString("titulek");
                String id = c.getString("id");
                String poleDatum[] = datum.split("-");

                MainActivity.akceList.add(poleDatum[2] + " ." + poleDatum[1] + " ." + poleDatum[0] + " " + popis + " " + cena);
                Log.d("titulekakceactivity", id);
                MainActivity.idAkceList.add(id);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    private void parseJSON(String parsedJSONString) {
        try {
            JSONObject jsonObj = new JSONObject(parsedJSONString);
            JSONArray UsersForAction = jsonObj.getJSONArray("UsersForAction");
            Log.d("jmena",UsersForAction.toString());
            jmena = new ArrayList<String>();
            zaplaceno = new ArrayList<Integer>();
            for (int i = 0; i < UsersForAction.length(); i++) {
                JSONObject c = UsersForAction.getJSONObject(i);
                jmena.add(c.getString("Name") + " " + c.getString("Surname"));
                Log.d("jmena",c.getString("Name") + " " + c.getString("Surname"));
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
        i.putExtra("titleAkceZobrazen", titleAkceZobrazen);
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
