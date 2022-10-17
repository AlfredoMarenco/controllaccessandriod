package com.example.controllaccess;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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


public class MainActivity extends AppCompatActivity {

    EditText textCode;
    Button buttonSend;
    TextView textBarcode;
    RelativeLayout bgLayout;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textCode = findViewById(R.id.textCode);
        buttonSend = findViewById(R.id.buttonSend);
        textBarcode = (TextView) findViewById(R.id.textBarcode);
        bgLayout = findViewById(R.id.bgLayout);
        textBarcode.setInputType(InputType.TYPE_NULL);
        mp = MediaPlayer.create(MainActivity.this,R.raw.ok);

        updateDatabase();




        buttonSend.setOnClickListener(new View.OnClickListener() {
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
            });



        new Thread(new Runnable() {
            @Override
            public void run() {
                textCode.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66){
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showCode(textCode.getText().toString());
                                    textCode.setText("");
                                    Log.d("Message","Validado");
                                    closeTecladoMovil();
                                }
                            });
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bgLayout.setBackgroundResource(R.color.white);
                                    textBarcode.setText("");
                                }
                            },2000);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }).start();
    }

    private void updateDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            Log.d("Conection","Si me conecte");
                            //Log.d("Objeto",""+jsonArray);
                            for (int i = 0; i <= jsonArray.length();i++){
                                DataBaseCodes dataBaseCodes =  new DataBaseCodes(MainActivity.this);
                                if (dataBaseCodes.selectCode(jsonArray.getJSONObject(i).getString("barcode").toString())){
                                    Log.d("Mensaje de validacion","Ya esta registrado");
                                }else{
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

                                    long id = dataBaseCodes.insertCode(barcode,name,section,price_category,row,seat,amount,order,sales_channel,ext,status,event_id);
                                    Log.d("Mensaje de registro","Agregado");
                                    if (id > 0){
                                        //Toast.makeText(MainActivity.this,"Se cargo la base de datos",Toast.LENGTH_LONG).show();
                                        textBarcode.setText("Se cargo la base de datos");
                                    }else{
                                        textBarcode.setText("No se cargo la base de datos");
                                    }
                                }
                            }
                            Toast.makeText(MainActivity.this,"Todos los codigos Obtenidos",Toast.LENGTH_LONG).show();
                        }catch (JSONException ex){
                            Log.d("Error:",ex.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Conection",error.toString());
                    }
                });
                requestQueue.add(codeRequest);
            }
        }).start();
    }

    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void showCode(String barcode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://controllaccess.boletea.com/api/code/"+barcode;
                StringRequest codeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Integer status = jsonObject.getInt("status");
                            if (status == 1){
                                bgLayout.setBackgroundResource(R.color.green);
                                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                textBarcode.setTextSize(60);
                                textBarcode.setText("PASE");
                                mp.start();

                            }else if(status == 0){
                                bgLayout.setBackgroundResource(R.color.red);
                                textBarcode.setTextColor(Color.rgb(255, 255, 255));
                                textBarcode.setText("ALTO");
                            }
                        } catch (JSONException e) {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textBarcode.setText("Codigo no valido");

                    }
                });
                Volley.newRequestQueue(MainActivity.this).add(codeRequest);
            }
        }).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(MainActivity.this,"Lector cancelado",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this,"Lector funciona",Toast.LENGTH_LONG).show();
                showCode(result.getContents());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bgLayout.setBackgroundResource(R.color.white);
                        textBarcode.setText("");
                    }
                },2000);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}