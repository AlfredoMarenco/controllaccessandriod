package com.example.controllaccess;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.controllaccess.database.DataBaseCodes;
import com.example.controllaccess.database.DataBaseHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;


public class MainActivity extends AppCompatActivity {
    private static ConnectivityManager manager;
    EditText textCode;
    Button buttonSend;
    TextView textBarcode;
    TextView textInfo;
    RelativeLayout bgLayout;
    MediaPlayer ok;
    MediaPlayer no;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateDatabase();
        setContentView(R.layout.activity_main);
        textCode = findViewById(R.id.textCode);
        //buttonSend = findViewById(R.id.buttonSend);
        textBarcode = (TextView) findViewById(R.id.textBarcode);
        textInfo = (TextView) findViewById(R.id.textInfo);
        bgLayout = findViewById(R.id.bgLayout);
        textBarcode.setInputType(InputType.TYPE_NULL);
        progressBar = findViewById(R.id.progressBar);
        ok = MediaPlayer.create(MainActivity.this, R.raw.ok);
        no = MediaPlayer.create(MainActivity.this, R.raw.no);




        /*buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                            intentIntegrator.setPrompt("Lector");
                            intentIntegrator.setCameraId(0);
                            intentIntegrator.setBeepEnabled(true);
                            intentIntegrator.setBarcodeImageEnabled(true);
                            intentIntegrator.setOrientationLocked(true);
                            intentIntegrator.initiateScan();
                        }
                    }).start();
                }
            });*/

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                showCode(textCode.getText().toString());
                textCode.setText("");
                Log.d("Message", "Validado");
                closeTecladoMovil();
                textInfo.setText("");
            default:
                return false;
        }
    }

    /*public boolean isOnlineNet() {
        boolean connected = false;
        ConnectivityManager connec = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();

        for (int i = 0; i < redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }*/
    public static boolean isOnlineNet(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo .isAvailable()) && (networkInfo .isConnected())) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                        urlc.setRequestProperty("User-Agent", "Test");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(1500);
                        urlc.connect();
                        return (urlc.getResponseCode() == 200);
                    } catch (IOException e) {
                        Log.e("TAG", "Error checking internet connection", e);
                    }
                } else {
                    Log.d("TAG", "No network available!");
                }
                return false;
            }
        });

        new Thread(futureRun).start();


        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

    }


    private void updateDatabase() {

        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024000 * 1024000); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = "https://controllaccess.boletea.com/api/code";
        StringRequest codeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    Log.d("Conection", "Si me conecte");
                    //Log.d("Objeto",""+jsonArray);
                    for (int i = 0; i <= jsonArray.length(); i++) {
                        DataBaseCodes dataBaseCodes = new DataBaseCodes(MainActivity.this);
                        if (dataBaseCodes.selectCode(jsonArray.getJSONObject(i).getString("barcode").toString())) {
                            Log.d("Mensaje de validacion", "Ya esta registrado");
                        } else {
                            String barcode = jsonArray.getJSONObject(i).getString("barcode").toString();
                            String name = jsonArray.getJSONObject(i).getString("name").toString();
                            String section = jsonArray.getJSONObject(i).getString("section").toString();
                            String price_category = jsonArray.getJSONObject(i).getString("price_category").toString();
                            String row = jsonArray.getJSONObject(i).getString("row").toString();
                            String seat = jsonArray.getJSONObject(i).getString("seat").toString();
                            String amount = jsonArray.getJSONObject(i).getString("amount").toString();
                            String order = jsonArray.getJSONObject(i).getString("order").toString();
                            String sales_channel = jsonArray.getJSONObject(i).getString("sales_channel").toString();
                            String ext = jsonArray.getJSONObject(i).getString("ext").toString();
                            String status = jsonArray.getJSONObject(i).getString("status").toString();
                            String event_id = jsonArray.getJSONObject(i).getString("event_id").toString();

                            long id = dataBaseCodes.insertCode(barcode, name, section, price_category, row, seat, amount, order, sales_channel, ext, status, event_id);
                            Log.d("Mensaje de registro", "Agregado");
                            if (id > 0) {
                                //Toast.makeText(MainActivity.this,"Se cargo la base de datos",Toast.LENGTH_LONG).show();
                                textBarcode.setText("Se cargo la base de datos");
                            } else {
                                textBarcode.setText("No se cargo la base de datos");
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(MainActivity.this, "Todos los codigos Obtenidos", Toast.LENGTH_LONG).show();
                } catch (JSONException ex) {
                    Log.d("Error:", ex.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Conection", error.toString());
            }
        });
        requestQueue.add(codeRequest);

    }

    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showCode(String barcode) {
        Log.d("Message", "Entre");
        if (isOnlineNet(MainActivity.this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RequestQueue requestQueue;

                    // Instantiate the cache
                    Cache cache = new DiskBasedCache(getCacheDir(), 1024000 * 1024000); // 1MB cap

                    // Set up the network to use HttpURLConnection as the HTTP client.
                    Network network = new BasicNetwork(new HurlStack());

                    // Instantiate the RequestQueue with the cache and network.
                    requestQueue = new RequestQueue(cache, network);

                    // Start the queue
                    requestQueue.start();

                    String url = "https://controllaccess.boletea.com/api/code/" + barcode;
                    StringRequest codeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Integer status = jsonObject.getInt("status");

                                String updated_at = jsonObject.getString("updated_at");

                                //String info = dateFormat.format(updated_at);

                                if (jsonObject != null) {
                                    if (status == 1) {
                                        bgLayout.setBackgroundResource(R.color.green);
                                        textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                        textBarcode.setTextSize(60);
                                        textBarcode.setText("PASE");
                                        ok.start();
                                        updateCodeOnline(barcode);
                                        updateCodeOffline(barcode);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                bgLayout.setBackgroundResource(android.R.color.transparent);
                                                textBarcode.setText("");
                                            }
                                        }, 2000);

                                    } else if (status == 0) {
                                        bgLayout.setBackgroundResource(R.color.red);
                                        textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                        textBarcode.setTextSize(60);
                                        textBarcode.setText("ALTO");
                                        no.start();
                                        textInfo.setText("Escaneado: " + updated_at);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.d("TAG", e.toString());
                                bgLayout.setBackgroundResource(android.R.color.holo_orange_dark);
                                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                textBarcode.setTextSize(30);
                                textBarcode.setText("Evento invalido");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (updateCodeOffline(barcode)) {
                                bgLayout.setBackgroundResource(R.color.green);
                                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                textBarcode.setTextSize(60);
                                textBarcode.setText("PASE");
                                ok.start();
                                updateCodeOnline(barcode);
                                updateCodeOffline(barcode);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        bgLayout.setBackgroundResource(android.R.color.transparent);
                                        textBarcode.setText("");
                                    }
                                }, 2000);
                            }else{
                                bgLayout.setBackgroundResource(R.color.red);
                                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                textBarcode.setTextSize(60);
                                textBarcode.setText("ALTO");
                                no.start();
                                textInfo.setText("Escaneado: El boleto ya fue escaneado");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        bgLayout.setBackgroundResource(android.R.color.transparent);
                                        textBarcode.setText("");
                                        textInfo.setText("");
                                    }
                                }, 2000);
                            }

                        }
                    });
                    requestQueue.add(codeRequest);
                }
            }).start();

        } else {
            if (updateCodeOffline(barcode)){
                bgLayout.setBackgroundResource(R.color.green);
                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                textBarcode.setTextSize(60);
                textBarcode.setText("PASE");
                ok.start();
                updateCodeOnline(barcode);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bgLayout.setBackgroundResource(android.R.color.transparent);
                        textBarcode.setText("");
                    }
                }, 2000);
            }else{
                bgLayout.setBackgroundResource(R.color.red);
                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                textBarcode.setTextSize(60);
                textBarcode.setText("ALTO");
                no.start();
            }
        }
    }

    public void updateCodeOnline(String barcode) {
        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024000 * 1024000); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
        String url = "https://controllaccess.boletea.com/api/code/" + barcode;
        StringRequest codeRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PUT", error.toString());
            }
        });
        Volley.newRequestQueue(MainActivity.this).add(codeRequest);
    }

    public boolean updateCodeOffline(String barcode) {
        DataBaseCodes dataBaseCodes = new DataBaseCodes(MainActivity.this);
        boolean updated = dataBaseCodes.editCode(barcode);
        return updated;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(MainActivity.this, "Lector cancelado", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Lector funciona", Toast.LENGTH_LONG).show();
                showCode(result.getContents());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bgLayout.setBackgroundResource(R.color.white);
                        textBarcode.setText("");
                    }
                }, 2000);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}