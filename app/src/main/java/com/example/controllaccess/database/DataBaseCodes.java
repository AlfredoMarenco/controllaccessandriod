package com.example.controllaccess.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

public class DataBaseCodes extends DataBaseHelper{

    Context context;

    public DataBaseCodes(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public long insertCode(String barcode, String name, String section, String price_category, String row, String seat, String amount, String order, String sales_channel, String ext, String status, String event_id){
        long id=0;
        try {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
            SQLiteDatabase dataBase = dataBaseHelper.getWritableDatabase();
            ContentValues values  = new ContentValues();
            values.put("barcode",barcode);
            values.put("name",name);
            values.put("section",section);
            values.put("price_category",price_category);
            values.put("fila",row);
            values.put("seat",seat);
            values.put("amount",amount);
            values.put("orden",order);
            values.put("sales_channel",sales_channel);
            values.put("ext",ext);
            values.put("status",status);
            values.put("event_id",event_id);

            id = dataBase.insert(DATABASE_TABLE,null,values);
        }catch (Exception ex){
            ex.toString();
        }



        return id;
    }

    public boolean selectCode(String code){
        Cursor query = null;
        try {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
            SQLiteDatabase dataBase = dataBaseHelper.getWritableDatabase();
            query = dataBase.rawQuery("SELECT barcode FROM "+ DATABASE_TABLE+ " WHERE barcode LIKE '"+code+"'",null);

            if (query.moveToFirst()){
                return true;
            }else{
                return false;
            }
        }finally {
            query.close();
        }

    }
}
