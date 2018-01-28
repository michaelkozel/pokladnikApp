package com.kozlik.tmf;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    /**
     * Tlačítko k odeslání transakce příspěvku na web
     */
    Button send;
    /**
     * Textové pole pro počet peněz které uživatel zvolený ve spinneru přidává do pokladny
     */
    EditText Eamount;
    /**
     * Textové pole pro komentář k příspěvku do pokladny
     */
    EditText komentt;
    /**
     * //pole kde jsou jen jmena uzivatelu
     */
    public String[] pname,
    /**
     * //pole kde jsou jen prijmeni uzivatelu
     */
    psurname,
    /**
     * //pole kde jsou jen zustatky uzivatelu serazenych stejne jako pole se jmeny, prvni clen pole je zustatek prvniho clena pole s jmeny
     */
    pbalance;
    /**
     * pole se zustatkem uzivatelu, serazenych od prvniho po posledni, stejne jako pole se jmeny
     */
    /**
     * Promenna jen pro log string radky se jmeny a balance vsech uzivatelu
     */
    public static String radky = "";

    /**
     * Spinner se jmenama zaku tridy
     */
    public static Spinner s;
    /**
     * listview s balace jednotlivych uzivatelu mimo placene akce
     */
    ListView lv;
    invhelper customadapter;
    public static Boolean onlyaktualizovat;
    public static Boolean json_done;
    String pAmount = ""; //kolik je v pokladně
    TextView tpAmount;   // kolik je v pokladně textview
    Button addEvent;     // tlacitko pridat akci
    static List<String> akceList;         // list s titulky akce
    static List<String> idAkceList;      //list s id jednotlivych akci serazeny od prvni akce
    Button bt_zobrazAkce;
    static String webURL = ""; // Url na web
    static String heslo = ""; // admin heslo
    SharedPreferences SP;
    static boolean pristup = false;
    /**
     * Titulek akce
     */
    String StrTitulek = "";
    /**
     * popis akce
     */
    String popis = "";
    /**
     * datum akce
     */
    String datum = "";
    /**
     * Kolik se ma zaplatit
     */
    String castka = "";
    String AddActionURL = "";
    String adresaGetJSON = "";
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private final int MY_PERMISSIONS_REQUEST_INTERNET = 1;

    //todo add titulek
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onlyaktualizovat = false;
        super.onCreate(savedInstanceState);
        new GetJson().execute();
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.INTERNET)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_INTERNET);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        webURL = SP.getString("adress", "xxx");
        heslo = SP.getString("heslo", "xxx");
        if (webURL.contains("xxx")) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            Toast.makeText(this, "Nastavte adresu vaší stránky", Toast.LENGTH_SHORT).show();
            startActivity(i);
        } else {
            SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            webURL = SP.getString("adress", "xxx");
            heslo = SP.getString("heslo", "xxx");
        }
        AddActionURL = webURL + "/AndroidAppRequests/AddAction.php";
        adresaGetJSON = webURL + "/AndroidAppRequests/GetJsonEvents.php";
        Log.d("Mainactivity", "adresa " + webURL + " heslo " + heslo);
        onlyaktualizovat = true;
        new WriteAsync().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        json_done = false;
        lv = (ListView) findViewById(R.id.listView);
        tpAmount = (TextView) findViewById(R.id.textView2);


        s = (Spinner) findViewById(R.id.spinner);

        send = (Button) findViewById(R.id.button);
        addEvent = (Button) findViewById(R.id.button2);
        bt_zobrazAkce = (Button) findViewById(R.id.bt_akce);
        Eamount = (EditText) findViewById(R.id.Eamount);
        komentt = (EditText) findViewById(R.id.koment);


        onlyaktualizovat = false;


        addEvent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (pristup) {
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                            android.R.style.Theme_DeviceDefault_Light_Dialog,
                            mDateSetListener, year, month, day);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    dialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Pravděpodobně špatné heslo", Toast.LENGTH_SHORT).show();
                }
            }

        });

        bt_zobrazAkce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pristup) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(adresaGetJSON, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

                        }

                        @Override
                        public void onSuccess(int i, Header[] headers, String s) {

                            String parsedJSONString = parseTextToJSON(s);
                            parseJSON(parsedJSONString);

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Pravděpodobně špatné heslo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                // změť alertů nejdřív datum, pak popis, a nakonec cena kolik se bude platit
                datum = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                //  Toast.makeText(getApplicationContext(), datum, Toast.LENGTH_SHORT).show();
                final EditText et_titulek = new EditText(getApplicationContext());
                et_titulek.setTextColor(getResources().getColor(R.color.colorAccent));
                AlertDialog.Builder alertD = new AlertDialog.Builder(MainActivity.this);
                alertD.setMessage("Zadej titulek");
                alertD.setTitle("Titulek");
                alertD.setView(et_titulek);
                alertD.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StrTitulek = et_titulek.getText().toString();
                        if (TextUtils.isEmpty(StrTitulek)) {
                            et_titulek.setError("Zadejte titulek");
                            return;
                        }
                        final EditText edittext = new EditText(getApplicationContext());
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setMessage("Zadejte popis");
                        alert.setTitle("Popis");
                        edittext.setTextColor(getResources().getColor(R.color.colorAccent));
                        alert.setView(edittext);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                popis = edittext.getText().toString();
                                //Toast.makeText(MainActivity.this, popis, Toast.LENGTH_SHORT).show();
                                final EditText et_dialog_castka = new EditText(getApplicationContext());
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setMessage("Zadejte částku");
                                alert.setTitle("Částka");
                                et_dialog_castka.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                et_dialog_castka.setInputType(InputType.TYPE_CLASS_NUMBER);
                                alert.setView(et_dialog_castka);
                                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        castka = et_dialog_castka.getText().toString();
                                        if (TextUtils.isEmpty(castka)) {
                                            et_dialog_castka.setError("Zadejte částku");
                                            return;
                                        }
                                        //   Toast.makeText(MainActivity.this, castka, Toast.LENGTH_SHORT).show();
                                        postRequestAkce(datum, popis, StrTitulek, castka);

                                    }

                                });

                                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });

                                alert.show();
                            }


                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });

                        alert.show();

                    }
                });
                alertD.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });


                alertD.show();
            }


        };


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                onlyaktualizovat = false;
                updatePOST();


            }
        });





       /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "TEST", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

      }

        });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        webURL = SP.getString("adress", "");
        heslo = SP.getString("heslo", "");
        AddActionURL = webURL + "/AndroidAppRequests/AddAction.php";
        adresaGetJSON = webURL + "/AndroidAppRequests/GetJsonEvents.php";

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new GetJson().execute();
    }

    private void parseJSON(String parsedJSONString) {
        String textInTextview = "";
        akceList = new ArrayList<String>();
        idAkceList = new ArrayList<String>();
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

                akceList.add(poleDatum[2] + " ." + poleDatum[1] + " ." + poleDatum[0] + " " + popis + " " + cena);
                Log.d("titulekmainactivity", id);
                idAkceList.add(id);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(this, AkceActivity.class);
        i.putStringArrayListExtra("test", (ArrayList<String>) akceList);
        startActivity(i);

    }

    private String parseTextToJSON(String s) {
        int zacatek = s.indexOf("{");
        int konec = s.length();
        String text = s.substring(zacatek, konec);
        Log.d("Main2Activity", text);
        return text;
    }

    private void postRequestAkce(String datum, String popis, String titulek, String castka) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("titulek", titulek);
        params.put("popis", popis);
        params.put("datum", datum);
        params.put("amountPost", castka);
        Log.d("params", params.toString());
        if (pristup) {
            client.post(AddActionURL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    Toast.makeText(MainActivity.this, "Success, akce přidána!" + statusCode, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Toast.makeText(MainActivity.this, "Fail " + statusCode, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Přístup odepřen, zkontrolujte heslo", Toast.LENGTH_SHORT).show();
        }
    }


    public void updatePOST() {

        WriteAsync.Koment = komentt.getText().toString();
        WriteAsync.amount = Eamount.getText().toString();
        String jmeno[] = s.getSelectedItem().toString().split(" ");
        WriteAsync.name = jmeno[1];
        WriteAsync.surname = jmeno[0];


        if (TextUtils.isEmpty(WriteAsync.amount)) {
            Eamount.setError("Prázdné");
            Toast.makeText(MainActivity.this, "Nevyplněná pole, neodeslané", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(WriteAsync.Koment)) {
            komentt.setError("Prázdné");
            Toast.makeText(MainActivity.this, "Nevyplněná pole, neodeslané", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!WriteAsync.amount.matches("")) {
            new WriteAsync().execute();
            new GetJson().execute();
            Eamount.setText("");
            komentt.setText("");
        } else {
            Toast.makeText(MainActivity.this, "Nevyplněná pole, neodeslané", Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;

            case R.id.action_load:
                new GetJson().execute();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }


    }

    public static String getRadky() {

        return radky;

    }

    /**
     * Třida pomoci ktere se dostává asynchronně json informace o lidech a jednotlivých zůstatcích v pokladně, v onPostExecute se vyplní
     * textviev s částkou pokladny a listview s uživateli a zůstatky
     */
    class GetJson extends AsyncTask<Void, Void, Boolean> {
        /**
         * pole stringu ve kterem je jmeno i prijmeni cloveka
         */
        String[] pjmeno;


        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Načítám data", Toast.LENGTH_SHORT).show();
            pjmeno = new String[]{""};
            pbalance = new String[]{""};
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String source = "";
            URL url = null;
            try {
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                webURL = SP.getString("adress", "xxx");
                heslo = SP.getString("heslo", "xxx");
                url = new URL(webURL + "/AndroidAppRequests/GetJson.php");
                URLConnection conn = url.openConnection();

                String data = "heslo"  //post data
                        + "=" + heslo;
                // data += "&" + "surnamePost" + "=" + surname;


                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine + "\n");
                in.close();
                source = sb.toString();
                if (!source.contains("{")) {
                    Log.d("getjson_source", source);
                    return false;
                }
                int kde = source.indexOf('{');
                char a = source.charAt(kde);
                System.out.println("KDE" + a);
                JSONObject jsonObj = new JSONObject(source.substring(kde, source.length()));
                System.out.println(source.substring(kde, source.length()));
//users
                // Getting JSON Array node
                JSONArray users = jsonObj.getJSONArray("users");
                pname = new String[users.length()];
                psurname = new String[users.length()];
                pbalance = new String[users.length()];
                pjmeno = new String[users.length()];
                for (int i = 0; i < users.length(); i++) {
                    JSONObject c = users.getJSONObject(i);
                    if (c.getString("surname").equals("Pokladna")) {
                        pAmount = c.getString("balance");
                    }

                    {
                        String surname = c.getString("surname");
                        String name = c.getString("name");
                        String balance = c.getString("balance");
                        psurname[i] = surname;
                        pname[i] = name;

                        pjmeno[i] = psurname[i] + " " + pname[i];
                        pbalance[i] = balance;
                    }
                }
                //transakce post request k ziskani dat o transakcich (aktivita view transactions, nakonec nepouzivane)
                url = new URL(MainActivity.webURL + "/AndroidAppRequests/GetTransactions.php");
                conn = url.openConnection();
                in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));

                sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine + "\n");
                in.close();
                //substring pro odmazani reklamy
                source = sb.toString();
                kde = source.indexOf('{');
                a = source.charAt(kde);
                jsonObj = new JSONObject(source.substring(kde, source.length()));
                System.out.println(source.substring(kde, source.length()));
                radky = "";
                JSONArray transactions = jsonObj.getJSONArray("transactions");
                for (int i = 0; i < transactions.length(); i++) {
                    JSONObject c = transactions.getJSONObject(i);
                    {
                        String Name = c.getString("Name");
                        String Amount = c.getString("Amount");
                        String Comment = c.getString("Comment");
                        radky += Name + " " + Amount + " " + Comment + "\n";
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {

                e.printStackTrace();
                return false;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_item, pjmeno);
            s.setAdapter(adapter);
            if (result) {
                Toast.makeText(getApplicationContext(), "Data úspěšně stažena", Toast.LENGTH_SHORT).show();
                customadapter = new invhelper(MainActivity.this, pjmeno, pbalance);
                lv.setAdapter(customadapter);
                customadapter.notifyDataSetChanged();
                tpAmount.setText(pAmount);
                pristup = true;
            } else {
                pristup = false;
                Toast.makeText(getApplicationContext(), "Data nebyla stažena, zkontrolujte nastavení a připojení k internetu", Toast.LENGTH_SHORT).show();
            }
            if (!result)
                System.out.println("CHYBA");

            MainActivity.json_done = true;

        }
    }
}



