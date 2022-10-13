package com.example.controllaccess.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "controllaccess.db";
    public static final String DATABASE_TABLE = "codes";


    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DATABASE_TABLE +"(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "barcode TEXT," +
                "name TEXT," +
                "section TEXT," +
                "price_category TEXT," +
                "fila TEXT," +
                "seat TEXT," +
                "amount TEXT," +
                "orden TEXT," +
                "sales_channel TEXT," +
                "ext TEXT," +
                "status TEXT," +
                "event_id TEXT," +
                "box_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE "+DATABASE_TABLE);
            onCreate(db);
    }
}
