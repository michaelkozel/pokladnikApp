package com.kozlik.tmf;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
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
    String URL_Pay = "";
    String URL_AddUserOnlyForThisAction = "";
    String getUsersForActionURL = "";
    FloatingActionButton fab_addUser;
    seznamAdapter customAdapter;
    /**
     * id mysql tabulky do ktere se odesilaji data
     */
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RESPONSE", "ONCreATE");
        setContentView(R.layout.activity_seznam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("titleAkceZobrazen"));
        setSupportActionBar(toolbar);
        seznamAkci = (ListView) findViewById(R.id.lv_seznam);
        customAdapter = new seznamAdapter(SeznamActivity.this, AkceActivity.jmena, AkceActivity.zaplaceno);
        seznamAkci.setAdapter(customAdapter);
        id = getIntent().getStringExtra("nazevTabulky");
        Window window = this.getWindow();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String webURL = SP.getString("adress", "xxx");
        String heslo = SP.getString("heslo", "xxx");
        getUsersForActionURL = webURL + "/AndroidAppRequests/getUsersForAction.php";
        URL_AddUserOnlyForThisAction = webURL + "/AndroidAppRequests/addUserForEvent.php";
        URL_Pay = webURL+"/AndroidAppRequests/payForEvent.php";
// barva status baru
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

    }

    private void addUserOnlyForThisEvent() {
        final EditText et_dialogAddUser_name = new EditText(getApplicationContext());
        final EditText et_dialogAddUser_surname = new EditText(getApplicationContext());
        et_dialogAddUser_name.setTextColor(getResources().getColor(R.color.colorAccent));
        AlertDialog.Builder dialog_addUser = new AlertDialog.Builder(SeznamActivity.this);
        dialog_addUser.setMessage("Zadej jméno");
        dialog_addUser.setTitle("Přidat uživatele");
        dialog_addUser.setView(et_dialogAddUser_name);
        dialog_addUser.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = et_dialogAddUser_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_dialogAddUser_name.setError("Zadejte jméno");
                    return;
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(SeznamActivity.this);
                alert.setMessage("Zadejte příjmení");
                alert.setTitle("Přidat uživatele");
                et_dialogAddUser_surname.setTextColor(getResources().getColor(R.color.colorAccent));
                alert.setView(et_dialogAddUser_surname);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        final String name = et_dialogAddUser_name.getText().toString();
                        final String surname = et_dialogAddUser_surname.getText().toString();
                        RequestParams params = new RequestParams();
                        params.put("name", name);
                        params.put("surname", surname);
                        params.put("id", id);
                        AsyncHttpClient client = new AsyncHttpClient();
                        client.post(SeznamActivity.this, URL_AddUserOnlyForThisAction, params, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(SeznamActivity.this, "Fail " + statusCode, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Toast.makeText(SeznamActivity.this, "Success add user" + statusCode, Toast.LENGTH_SHORT).show();
                                updateList();
                            }
                        });
                    }

                });
                alert.show();
            }
        });
        dialog_addUser.show();
    }

    private void updateList() {
        AsyncHttpClient client = new AsyncHttpClient();
        Log.d("nazevTabulky", id);
        //Toast.makeText(getApplication(), id, Toast.LENGTH_SHORT).show();
        RequestParams params = new RequestParams("titulek", id);
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

    private String parseTextToJSON(String s) {
        int zacatek = s.indexOf("{");
        int konec = s.length();
        String text = s.substring(zacatek, konec);
        Log.d("Main2Activity", text);
        return text;

    }

    private void parseJSON(String parsedJSONString) {
        try {
            JSONObject jsonObj = new JSONObject(parsedJSONString);
            JSONArray UsersForAction = jsonObj.getJSONArray("UsersForAction");
            AkceActivity.jmena = new ArrayList<String>();
            AkceActivity.zaplaceno = new ArrayList<Integer>();
            Log.d("delka jmen", "" + UsersForAction.length());
            for (int i = 0; i < UsersForAction.length(); i++) {
                JSONObject c = UsersForAction.getJSONObject(i);
                AkceActivity.jmena.add(c.getString("Name") + " " + c.getString("Surname"));
                int zaplatil = c.getInt("Zaplaceno");
                switch (zaplatil) {
                    case 0:
                        AkceActivity.zaplaceno.add(0);
                        break;
                    case 1:
                        AkceActivity.zaplaceno.add(1);
                        break;
                    case 2:
                        AkceActivity.zaplaceno.add(2);
                        break;
                    default:
                        AkceActivity.zaplaceno.add(3);
                        break;
                }

            }
            Log.d("delka zaplaceno", AkceActivity.zaplaceno.size() + "");
            customAdapter = new seznamAdapter(SeznamActivity.this, AkceActivity.jmena, AkceActivity.zaplaceno);
            seznamAkci.setAdapter(customAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
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
        params.put("tableName", id);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(SeznamActivity.this, URL_Pay, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(SeznamActivity.this, "Nepovedlo se zapsat", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Toast.makeText(SeznamActivity.this, "Úspěšně zapsáno ", Toast.LENGTH_SHORT).show();
                Log.d("RESPONSE po backpressu", responseString);
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

            case R.id.action_add_user:
                addUserOnlyForThisEvent();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }


    }

    @Override
    public void onBackPressed() {
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
