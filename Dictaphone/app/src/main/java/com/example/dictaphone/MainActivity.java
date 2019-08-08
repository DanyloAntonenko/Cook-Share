package com.example.dictaphone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView smallest_line, middle_line, biggest_line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smallest_line = findViewById(R.id.smallest_line);
        middle_line = findViewById(R.id.middle_line);
        biggest_line = findViewById(R.id.biggest_line);

        smallest_line.setVisibility(View.INVISIBLE);
        middle_line.setVisibility(View.INVISIBLE);
        biggest_line.setVisibility(View.INVISIBLE);

        registerForContextMenu(smallest_line);
        registerForContextMenu(middle_line);
        registerForContextMenu(biggest_line);



            startAnimation();
            stopAnimation();
            startAnimation();
            stopAnimation();

            //HelloWorld
            //watafacmazafaca


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

}