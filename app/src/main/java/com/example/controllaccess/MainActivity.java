package com.example.controllaccess;


import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;


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
        textBarcode = findViewById(R.id.textBarcode);
        bgLayout = findViewById(R.id.bgLayout);

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
                    final Handler handler = new Handler();

                    JSONObject jsonObject = new JSONObject(response);
                    Integer status = jsonObject.getInt("status");
                    if (status == 1){
                        bgLayout.setBackgroundResource(R.color.green);
                    }else{
                        bgLayout.setBackgroundResource(R.color.red);
                    }
                } catch (JSONException e) {
                    textBarcode.setText("Codigo no valid");
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
}