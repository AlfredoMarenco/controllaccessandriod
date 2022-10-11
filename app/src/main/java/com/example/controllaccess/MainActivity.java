package com.example.controllaccess;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText textCode;
    Button buttonSend;
    TextView textBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textCode = findViewById(R.id.textCode);
        buttonSend = findViewById(R.id.buttonSend);
        textBarcode = findViewById(R.id.textBarcode);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCode(textCode.getText().toString());
            }
        });

    }

    private void showCode(String barcode){
        String url = "https://controllaccess.boletea.com/api/code/"+barcode;
        StringRequest codeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    textBarcode.setText(jsonObject.getString("barcode"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.getMessage());
            }
        });

        Volley.newRequestQueue(this).add(codeRequest);


    }
}