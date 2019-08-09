package com.example.dictaphone;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ImageView smallest_line, middle_line, biggest_line, record_button_bgr;
    Button record_button, pause_button, stop_button, cancel_button;
    TextView timer_text;

    short flag = 0;
    Timer timer;
    MyTymer myTymer;



    //////////////////////////////
    private static final int recorder_samplerate = 8000;
    private static final int recorder_chanels = AudioFormat.CHANNEL_IN_MONO;
    private static final int recorder_audio_encoding = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    /////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer_text = findViewById(R.id.timer_text);

        pause_button = findViewById(R.id.pause_button);
        pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag == 0){
                    stopAnimation();
                    pauseTimer();
                    pause_button.setBackgroundResource(R.drawable.play);
                    flag++;
                }else if(flag == 1){
                    startAnimation();
                    resumeTimer();
                    pause_button.setBackgroundResource(R.drawable.pause);
                    flag--;
                }

            }
        });

        stop_button = findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAnimation();
                showRecordButton();
                hideControlButtons();
                stopTimer();

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
            }
        });


        record_button_bgr = findViewById(R.id.record_button_bgr);

        record_button = findViewById(R.id.record_button);
        record_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    startAnimation();
                    showControlButtons();
                    hideRecordButton();
                    startTimer();
                }
                return false;
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
            stringBuilder.append(records.get(i) + "\n");
        }
        Toast.makeText(this, stringBuilder, Toast.LENGTH_LONG).show();
        database.close();

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

    ////////////////////////////////////////////////////////////////////////////////////////
    //ПРОВЕРИТЬ РАББОТУ
    ///////////////////////////////////////////////////////////////////////////////////////
    int buffer_element_to_rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int bytes_per_element = 2;

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                recorder_samplerate, recorder_chanels,
                recorder_audio_encoding, buffer_element_to_rec * bytes_per_element);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte

        String filePath = "/sdcard/voice8K16bitmono.pcm";
        short sData[] = new short[buffer_element_to_rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, buffer_element_to_rec);
            System.out.println("Short wirting to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, buffer_element_to_rec * bytes_per_element);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
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