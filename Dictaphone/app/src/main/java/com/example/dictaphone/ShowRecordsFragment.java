package com.example.dictaphone;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowRecordsFragment extends Fragment {

    private Context context;

    private MediaPlayer media_player;
    private String storage_dir, file_name;



    /////////////////////////////////////////////////////////
    public static ListView list_of_records;

    public static final String attribute_name = "name";
    public static final String attribute_date = "date";
    public static final String attribute_duration = "duration";

    public static SimpleAdapter adapter;
    public static ArrayList<Map<String, Object>> data;
    public static Map<String, Object> m;


    DatabaseHelper database;
    //////////////////////////////////////////////////////////

    /** Handle the results from the voice recognition activity. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        data = new ArrayList<>();

        database = DatabaseHelper.getInstance(context);
        database.open();

        String[] from = {attribute_name, attribute_date, attribute_duration};
        int[] to = {R.id.record_name, R.id.record_date, R.id.record_duration};

        adapter = new SimpleAdapter(context, data, R.layout.item, from, to);
        list_of_records = getView().findViewById(R.id.records_view);
        list_of_records.setAdapter(adapter);

        registerForContextMenu(list_of_records);

        showAllRecords();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //задаем разметку фрагменту
        final View view = inflater.inflate(R.layout.show_records, container, false);
        //ну и контекст, так как фрагменты не содержат собственного
        context = view.getContext();
        return view;
    }

    public void startPlaying(){
        try{
            releasePlayer();
            media_player = new MediaPlayer();
            media_player.setDataSource(storage_dir + file_name);
            media_player.prepare();
            media_player.start();
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void stopPlaying(){
        if(media_player != null){
            media_player.stop();
        }
    }

    public void releasePlayer(){
        if(media_player != null){
            media_player.release();
            media_player = null;
        }
    }

    public void showAllRecords() throws NullPointerException{
        try {
            if (database == null) {
                try {
                    database.open();
                }catch (NullPointerException e){
                    throw new NullPointerException("Database reference equals to null");
                }
            }
            List<Record> records = database.getAllRecords();
            for(Record r : records){
                addRecordToList(r);
            }

        } catch (Exception e){

        }
    }

    public void addRecordToList(Record record){
        m = new HashMap<>();
        m.put(attribute_name, record.getName());
        m.put(attribute_date, record.getDate());
        m.put(attribute_duration, getNormalDurationForm(record.getDuration()));

        data.add(m);

        adapter.notifyDataSetChanged();
    }

    public String getNormalDurationForm(int duration){
        int minutes = duration / 60,
                seconds = duration % 60;
        return ((minutes < 10) ? "0" + String.valueOf(minutes) : String.valueOf(minutes))
                .concat(":")
                .concat((seconds < 10) ? "0" +  String.valueOf(seconds) : String.valueOf(seconds));
    }







    @Override
    public void onDestroy() {
        super.onDestroy();
        if(database != null) database.close();
    }
}
