package com.kozlik.tmf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.kozlik.tmf.MainActivity;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 8. 6. 2016.
 */

/**
 * Třída pro odeslání transakce příspěvku do pokladny
 */
public class WriteAsync extends AsyncTask<Void, Void, Boolean> {
    public static String suma = "";
    public static String name = "";
    public static String amount = "";
    public static String Koment = "";
    public static String kod = "";
    String text;

    protected void onPreExecute() {


    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            if (MainActivity.onlyaktualizovat) {
                kod = ShowTransactions();
            } else {


                updatePOST();
                kod = ShowTransactions();
            }


            return true;
        } catch (Exception e) {

            System.out.println("CHYBA před ");
            return false;
        }


    }


    protected void onPostExecute(Boolean result) {


    }


    public void updatePOST() throws UnsupportedEncodingException {


        text = "";
        // Create data variable for sent values to server

        String data = "namePost"
                + "=" + name;

        data += "&" + "amountPost" + "="
                + amount;

        data += "&" + "sumPost"
                + "=" + suma;

        data += "&" + "commentPost"
                + "=" + Koment;


        BufferedReader reader = null;

        // Send data
        try {
            // Defined URL  where to send data
            URL url = new URL(MainActivity.webURL + "/AndroidAppRequests/NewTransaction.php");

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();


            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }


            text = sb.toString();
            System.out.print("odezva" + text);
        } catch (Exception ex) {
            System.out.println("spadlo to");

        } finally {
            try {

                reader.close();
            } catch (Exception ex) {

                System.out.println("spadlo to");

            }
        }

        name = "";
        amount = "";
        suma = "";
        Koment = "";


    }


    public String ShowTransactions() {

        String source = "";
        URL url = null;
        try {
            url = new URL(MainActivity.webURL + "/ShowData.php");
            URLConnection conn = url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine + "\n");
            in.close();
            source = sb.toString();

            return source;


        } catch (MalformedURLException e) {

            e.printStackTrace();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }


}
