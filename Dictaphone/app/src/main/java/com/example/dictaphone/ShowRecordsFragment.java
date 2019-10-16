package com.example.dictaphone;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowRecordsFragment extends Fragment {
    String log_key = "myLogs";
    private Context context;

    private MediaPlayer media_player;

    public static ListView list_of_records;

    public static final String attribute_name = "name";
    public static final String attribute_date = "date";
    public static final String attribute_duration = "duration";

    public static SimpleAdapter adapter;
    public static ArrayList<Map<String, Object>> data;
    public static Map<String, Object> m;

    public static List<Record> records;
    DatabaseHelper database;


    /** Handle the results from the voice recognition activity. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        data = new ArrayList<>();

        database = DatabaseHelper.getInstance(context);
        database.open();

        String[] from = {attribute_name, attribute_date, attribute_duration};
        int[] to = {R.id.record_name, R.id.record_date, R.id.record_duration};


        adapter = new SimpleAdapter(context, data, R.layout.item, from, to){

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                final Button play_button = v.findViewById(R.id.play_button);
                final Button delete_button = v.findViewById(R.id.delete_button);
                final SeekBar sound_progress = v.findViewById(R.id.sound_progress);

                sound_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(media_player.isPlaying()){

                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                final Handler handler = new Handler();
                final Runnable move_seek_bar_thread = new Runnable() {

                    public void run() {
                        if(media_player.isPlaying()){

                            int mediaPos_new = media_player.getCurrentPosition();
                            int mediaMax_new = media_player.getDuration();
                            sound_progress.setMax(mediaMax_new);
                            sound_progress.setProgress(mediaPos_new);

                            handler.postDelayed(this, 10); //Looping the thread after 0.1 second
                            // seconds

                        }
                        else if(!media_player.isPlaying()){
                            sound_progress.setVisibility(View.INVISIBLE);
                        }
                    }
                };




                play_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlaying(records.get(records.size() - position - 1).getName());
                        //delete_button.setVisibility(View.VISIBLE);
                        //play_button.setVisibility(View.INVISIBLE);
                        sound_progress.setVisibility(View.VISIBLE);

                        int mediaPos = media_player.getCurrentPosition();
                        int mediaMax = media_player.getDuration();

                        sound_progress.setMax(mediaMax); // Set the Maximum range of the
                        sound_progress.setProgress(mediaPos);// set current progress to song's

                        handler.removeCallbacks(move_seek_bar_thread);
                        handler.postDelayed(move_seek_bar_thread, 10);

                    }
                });


                play_button.setVisibility(View.VISIBLE);


















                delete_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        play_button.setVisibility(View.VISIBLE);
                        delete_button.setVisibility(View.INVISIBLE);
                    }
                });
                delete_button.setVisibility(View.INVISIBLE);

                RelativeLayout header_layout = v.findViewById(R.id.header_layout);
                header_layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), "AAAA", Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
                RelativeLayout body_layout = v.findViewById(R.id.body_layout);
                body_layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), "AAAA", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });


                return v;

            }
        };
        list_of_records = getView().findViewById(R.id.records_view);
        list_of_records.setAdapter(adapter);


        registerForContextMenu(list_of_records);

        getAllRecords();

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



    public void startPlaying(String file_name){
        try{
            releasePlayer();
            media_player = new MediaPlayer();
            media_player.setDataSource(file_name);
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

    public void getAllRecords() throws NullPointerException{
        try {
            if (database == null) {
                try {
                    database.open();
                }catch (NullPointerException e){
                    throw new NullPointerException("Database reference equals to null");
                }
            }
            records = database.getAllRecords();
            for(Record r : records){
                showNewRecord(r);
            }

        } catch (Exception e){

        }
    }


    public static void showNewRecord(Record record){
        m = new HashMap<>();

        String name = record.getName();
        if(name.contains("/") && name.contains(".3gpp")){
            m.put(attribute_name, name.substring(name.lastIndexOf('/') + 1, name.length() - 5));
        } else {
            m.put(attribute_name, name);
        }

        m.put(attribute_date, record.getDate());
        m.put(attribute_duration, getNormalDurationForm(record.getDuration()));

        data.add(0, m);

        adapter.notifyDataSetChanged();
    }

    public static String getNormalDurationForm(int duration){
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
        Log.e(log_key, "Application terminated");
    }
}
