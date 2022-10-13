package com.example.controllaccess;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.util.LogPrinter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.sql.SQLOutput;


public class MainActivity extends AppCompatActivity {

    EditText textCode;
    Button buttonSend;
    TextView textBarcode;
    RelativeLayout bgLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textCode = findViewById(R.id.textCode);
        buttonSend = findViewById(R.id.buttonSend);
        textBarcode = (TextView) findViewById(R.id.textBarcode);
        bgLayout = findViewById(R.id.bgLayout);
        textBarcode.setInputType(InputType.TYPE_NULL);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addElementsDataBase();
            }
        });

        textCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66){
                    showCode(textCode.getText().toString());
                    textCode.setText("");
                    Log.d("Message","Validado");
                    closeTecladoMovil();
                    return true;
                }
                return false;
            }
        });

    }

    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void showCode(String barcode){
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
                        textBarcode.setText("PASE");

                    }else if(status == 0){
                        bgLayout.setBackgroundResource(R.color.red);
                        textBarcode.setTextColor(Color.rgb(255, 255, 255));
                        textBarcode.setText("ALTO");
                    }
                } catch (JSONException e) {
                    textBarcode.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textBarcode.setText("Codigo no valid");

            }
        });
        Volley.newRequestQueue(this).add(codeRequest);
    }

    private void addElementsDataBase(){
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
                    JSONArray jsonObject = new JSONArray(response);
                    Log.d("Conection","Si me conecte");
                    //Log.d("Objeto",""+jsonObject);
                    textBarcode.setText(""+jsonObject.length());
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
}