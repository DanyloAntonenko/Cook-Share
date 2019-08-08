package com.example.dictaphone;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String db_name = "dictaphone.db";
    private static final int db_version = 1;

    public DatabaseOpenHelper(Context context){super(context, db_name, null, db_version);}
}
