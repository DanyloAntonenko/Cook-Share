package com.example.dictaphone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseHelper instance;

    private static final String table_name = "records";
    private static final String key_id = "id";
    private static final String key_name = "name";
    private static final String key_duration = "duration";
    private static final String key_date = "date";

    private DatabaseHelper(Context context){this.openHelper = new DatabaseOpenHelper(context);}

    public static DatabaseHelper getInstance(Context context){
        if(instance == null){
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    public void open(){this.database = openHelper.getWritableDatabase();}

    public void close(){
        if(database!=null){
            database.close();
        }
    }

    public List<Record> getAllRecords(){
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);

        List<Record> records = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            records.add(new Record(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(key_id))),
                    cursor.getString(cursor.getColumnIndex(key_name)),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(key_duration))),
                    cursor.getString(cursor.getColumnIndex(key_date))
            ));
            cursor.moveToNext();
        }
        cursor.close();

        return records;
    }

    public void insertRecord(Record record){
        ContentValues contentValues = new ContentValues();
        contentValues.put(key_name, record.getName());
        contentValues.put(key_duration, record.getDuration());
        contentValues.put(key_date, record.getDate());

        database.insert(table_name, null, contentValues);
    }

    public void deleteRecord(int id){
            database.delete(table_name, key_id + "=?", new String[]{String.valueOf(id)});
    }

    public void updateRecord(Record record){
        ContentValues contentValues = new ContentValues();
        contentValues.put(key_name, record.getName());
        contentValues.put(key_duration, record.getDuration());
        contentValues.put(key_date, record.getDate());

        database.update(table_name, contentValues, key_id + "=?", new String[]{String.valueOf(record.getId())});
    }

    public Record selectLastRecord(){
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name + " ORDER BY " + key_id + " DESC LIMIT 1", null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            return new Record(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(key_id))),
                    cursor.getString(cursor.getColumnIndex(key_name)),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(key_duration))),
                    cursor.getString(cursor.getColumnIndex(key_date))
            );
        }
        return null;
    }

}
