package com.example.dictaphone;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ImageView smallest_line, middle_line, biggest_line, record_button_bgr;
    Button  record_button,
            pause_button, stop_button, cancel_button,
            play_sound_button, pause_sound_button;
    private TextView timer_text;


    private Timer timer;
    private MyTymer myTymer;
    private MediaRecorder media_recorder;
    private MediaPlayer media_player;

    String file_name; File storage_dir;
    private short flag = 0;

    String[] audio_parts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play_sound_button = findViewById(R.id.play_sound_button);
        play_sound_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlaying();
            }
        });

        pause_sound_button = findViewById(R.id.pause_sound_button);
        pause_sound_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
            }
        });

        timer_text  = findViewById(R.id.timer_text);

        pause_button = findViewById(R.id.pause_button);
        pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag == 0){
                    stopAnimation();
                    pauseTimer();
                    pause_button.setBackgroundResource(R.drawable.play);
                    flag++;


                    pauseRecording();
                }else if(flag == 1){
                    startAnimation();
                    resumeTimer();
                    pause_button.setBackgroundResource(R.drawable.pause);
                    flag--;


                    resumeRecording();
                }

            }
        });

        stop_button = findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                stopTimer();


                mergeRecords(true, audio_parts, storage_dir + file_name);

                stopAnimation();
                showRecordButton();
                hideControlButtons();
                renameFile();

                audio_parts = null;
            }
        });

        cancel_button = findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAnimation();
                showRecordButton();
                hideControlButtons();
                stopTimer();
                stopRecording();
                deleteRecord(file_name);
            }
        });


        record_button_bgr = findViewById(R.id.record_button_bgr);
        record_button = findViewById(R.id.record_button);
        record_button .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                startTimer();

                startAnimation();
                showControlButtons();
                hideRecordButton();
            }
        });


        smallest_line = findViewById(R.id.smallest_line);
        middle_line = findViewById(R.id.middle_line);
        biggest_line = findViewById(R.id.biggest_line);

        smallest_line.setVisibility(View.INVISIBLE);
        middle_line.setVisibility(View.INVISIBLE);
        biggest_line.setVisibility(View.INVISIBLE);

        registerForContextMenu(smallest_line);
        registerForContextMenu(middle_line);
        registerForContextMenu(biggest_line);

        registerForContextMenu(pause_button);
        registerForContextMenu(stop_button);
        registerForContextMenu(cancel_button);

        pause_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        cancel_button.setVisibility(View.INVISIBLE);

        DatabaseHelper database = DatabaseHelper.getInstance(this);
        database.open();

        StringBuilder stringBuilder = new StringBuilder();
        List<Record> records = database.getAllRecords();

        for(int i = 0; i < records.size(); i++){
            stringBuilder.append(records.get(i)).append("\n");
        }
       // Toast.makeText(this, stringBuilder, Toast.LENGTH_LONG).show();
        database.close();

        storage_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Records/");
        file_name = "/record.3gpp";
        if(!storage_dir.exists()){
            storage_dir.mkdirs();
        }

    }

    public void startAnimation(){
        if(smallest_line.getAnimation() == null){

            try {
                smallest_line.setAnimation(createAnimation(0));
                middle_line.setAnimation(createAnimation(250));
                biggest_line.setAnimation(createAnimation(500));

            } catch (IllegalArgumentException ex){
                Toast.makeText(this, ex.getMessage() + "\nError caused by calling startAnimation()", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void stopAnimation(){
        if(smallest_line.getAnimation() != null){


            smallest_line.setAnimation(null);
            middle_line.setAnimation(null);
            biggest_line.setAnimation(null);

            smallest_line.clearAnimation();
            middle_line.clearAnimation();
            biggest_line.clearAnimation();

            smallest_line.setVisibility(View.INVISIBLE);
            middle_line.setVisibility(View.INVISIBLE);
            biggest_line.setVisibility(View.INVISIBLE);
        }
    }

    public Animation createAnimation(int delay) throws IllegalArgumentException{
        if(delay >= 0) {
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(750);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + delay);
            return animation;
        }
        else{
            throw new IllegalArgumentException("Delay must not be less then 0.");
        }
    }

    public void showControlButtons(){
        Animation pause_button_show = AnimationUtils.loadAnimation(this, R.anim.pause_button_show);
        pause_button.setAnimation(pause_button_show);

        Animation stop_button_show = AnimationUtils.loadAnimation(this, R.anim.stop_button_show);
        stop_button.setAnimation(stop_button_show);

        Animation cancel_button_show = AnimationUtils.loadAnimation(this, R.anim.cancel_button_show);
        cancel_button.setAnimation(cancel_button_show);

        pause_button.setVisibility(View.VISIBLE);
        stop_button.setVisibility(View.VISIBLE);
        cancel_button.setVisibility(View.VISIBLE);

    }

    public void hideControlButtons(){
        flag = 0;
        Animation pause_button_hide = AnimationUtils.loadAnimation(this, R.anim.pause_button_hide);
        pause_button.setAnimation(pause_button_hide);

        Animation stop_button_hide = AnimationUtils.loadAnimation(this, R.anim.stop_button_hide);
        stop_button.setAnimation(stop_button_hide);

        Animation cancel_button_hide = AnimationUtils.loadAnimation(this, R.anim.cancel_button_hide);
        cancel_button.setAnimation(cancel_button_hide);

        pause_button.setBackgroundResource(R.drawable.pause);

        pause_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        cancel_button.setVisibility(View.INVISIBLE);
    }

    public void showRecordButton(){
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);

        record_button.setAnimation(animation);
        record_button_bgr.setAnimation(animation);

        record_button.setVisibility(View.VISIBLE);
        record_button_bgr.setVisibility(View.VISIBLE);
    }

    public void hideRecordButton(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);

        record_button.setAnimation(animation);
        record_button_bgr.setAnimation(animation);

        record_button_bgr.setVisibility(View.INVISIBLE);
        record_button.setVisibility(View.INVISIBLE);
    }

    public void startTimer(){
        timer = new Timer();
        myTymer = new MyTymer();
        timer.schedule(myTymer, 1000, 1000);

    }

    public void pauseTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;

            Animation timer_text_blink =  AnimationUtils.loadAnimation(this, R.anim.timer_text_blink);
            timer_text.setAnimation(timer_text_blink);
        }
    }

    public void resumeTimer(){
        if(timer == null) {
            timer = new Timer();

            int seconds = myTymer.seconds;
            int minutes = myTymer.minutes;

            myTymer = new MyTymer();
            myTymer.seconds = seconds;
            myTymer.minutes = minutes;

            timer.schedule(myTymer, 1000, 1000);

            if(timer_text.getAnimation() != null){
                timer_text.clearAnimation();
            }
        }
    }

    public void stopTimer(){
        if(timer != null){
            timer.cancel();
        }
        timer = null;

        if(myTymer != null){
            myTymer.cancel();
        }
        myTymer = null;

        if(timer_text.getAnimation() != null) {
            timer_text.clearAnimation();
        }

    }

    public void startRecording(){
        try{
            file_name = getTemporaryFileName();
            File out_file = new File(storage_dir + file_name);
            while (out_file.exists()){
                file_name = getTemporaryFileName();
                out_file = new File(storage_dir + file_name);
            }

            addToAudioParts(storage_dir + file_name);

            releaseRecorder();

            media_recorder = new MediaRecorder();
            media_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            media_recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            media_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            media_recorder.setAudioEncodingBitRate(16 * 44100);
            media_recorder.setAudioSamplingRate(44100);
            media_recorder.setOutputFile(storage_dir + file_name);
            media_recorder.prepare();
            media_recorder.start();



        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void stopRecording() {
        if(media_recorder != null){
            media_recorder.stop();
        }
    }

    public void startPlaying(){
        try{
            releasePlayer();
            media_player = new MediaPlayer();
            media_player.setDataSource(storage_dir + file_name);
            media_player.prepare();
            media_player.start();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void stopPlaying(){
        if(media_player != null){
            media_player.stop();
        }
    }

    public void releaseRecorder(){
        if(media_recorder != null){
            media_recorder.release();
            media_recorder = null;
        }
    }

    public void releasePlayer(){
        if(media_player != null){
            media_player.release();
            media_player = null;
        }
    }

    public void renameFile(){

        View prompts = LayoutInflater.from(this).inflate(R.layout.enter_file_name_dialog_window, null);

        final AlertDialog.Builder alert_dialog = new AlertDialog.Builder(this);
        alert_dialog.setView(prompts);
        alert_dialog.setCancelable(false);

        final EditText user_input = prompts.findViewById(R.id.input_text);
        user_input.setText(file_name.substring(1, file_name.length() - 5));

        Button save_file_name_button = prompts.findViewById(R.id.save_file_name_button);
        Button cancel_file_name_button = prompts.findViewById(R.id.cancel_file_name_button);

        final AlertDialog dialog = alert_dialog.create();

        save_file_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!file_name.substring(1, file_name.length() - 5).equals(user_input.getText().toString()) && !user_input.getText().toString().isEmpty()) {
                    changeFileName(file_name, user_input.getText().toString());
                }
                dialog.dismiss();
            }
        });

        cancel_file_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecord(file_name);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void changeFileName(String file_name_old, String file_name_new){
        try {
            if(storage_dir.exists()) {

                File from = new File(storage_dir, file_name_old);
                File to = new File(storage_dir, file_name_new.trim() + ".3gpp");
                if (from.exists()) {
                    from.renameTo(to);
                } else {
                    throw new Exception("Temporary file does not exist");
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "Renaming file problem", Toast.LENGTH_LONG).show();
        }

    }

    public String getTemporaryFileName(){
        String symbols = "ABCDEFGJKLMNPRSTUVWXYZabcdefgjklmnrpstuvwxyz01234567890";
        SecureRandom random = new SecureRandom();
        StringBuilder string_builder = new StringBuilder("/");

        for(int i = 0; i < 20; i++){
            string_builder.append(symbols.charAt(random.nextInt(symbols.length())));
        }

        string_builder.append(".3gpp");

        return string_builder.toString();
    }

    public void deleteRecord(String file){
        try{
            File f = new File(storage_dir + file);
            if(f.delete()){
                Toast.makeText(this, "File deleted successfully", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "File delete failed", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void pauseRecording(){
        stopRecording();
    }

    public void resumeRecording(){
        startRecording();
    }

    public void addToAudioParts(String value){
        if(audio_parts == null){
            audio_parts = new String[1];
            audio_parts[0] = value;
        } else {
            String[] temp = new String[audio_parts.length + 1];
            for (int i = 0; i < audio_parts.length; i++) {
                temp[i] = audio_parts[i];
            }
            temp[temp.length - 1] = value;
            audio_parts = temp;
        }
    }


    public boolean mergeRecords(boolean is_audio, String source_files[], String target_file){
        try{
            String media_key = is_audio ? "soun" : "vide";
            List<Movie> list_movies = new ArrayList<>();
            for(String filename : source_files){
                list_movies.add(MovieCreator.build(filename));
            }

            List<Track> list_tracks = new LinkedList<>();
            for(Movie movie : list_movies){
                for(Track track : list_tracks){
                    if(track.getHandler().equals(media_key)){
                        list_tracks.add(track);
                    }
                }
            }

            Movie output_movie = new Movie();
            if(!list_tracks.isEmpty()){
                output_movie.addTrack(new AppendTrack(list_tracks.toArray(new Track[list_tracks.size()])));
            }

            Container container = new DefaultMp4Builder().build(output_movie);
            FileChannel file_channel = new RandomAccessFile(String.format(target_file), "rw").getChannel();
            container.writeContainer(file_channel);
            file_channel.close();
            return true;

        }catch (Exception e){
            Toast.makeText(this, "Error merging files", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRecorder();
        releaseRecorder();
    }

    class MyTymer extends TimerTask {
        int seconds = 0;
        int minutes = 0;
        @Override
        public void run() {
            seconds++;
            minutes = seconds/60;

            int seconds_formatted = seconds%60;
            int minutes_formatted = minutes%60;

            final String time = ((minutes_formatted < 10) ? "0" + String.valueOf(minutes_formatted) : String.valueOf(minutes_formatted)) +
                    ":" +
                    ((seconds_formatted < 10) ? "0" + String.valueOf(seconds_formatted):String.valueOf(seconds_formatted));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timer_text.setText(time);
                }
            });
        }
    }

}