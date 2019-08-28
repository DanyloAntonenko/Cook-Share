package com.example.dictaphone;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int record_fragment = 0;
    public static final int show_record_fragment = 1;

    public static final int fragments = 2;

    private FragmentPagerAdapter fragment_pager_adapter;
    public final List<Fragment> fragments_list = new ArrayList<>();
    public static CustomViewPager view_pager;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragments_list.add(record_fragment, new RecordFragment());
        fragments_list.add(show_record_fragment, new ShowRecordsFragment());

        fragment_pager_adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments_list.get(position);
            }

            @Override
            public int getCount() {
                return fragments;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case record_fragment:
                        return "Title One";
                    case show_record_fragment:
                        return "Title Two";
                    default:
                        return null;

                }
            }
        };


        view_pager = findViewById(R.id.pager);
        view_pager.setAdapter(fragment_pager_adapter);
        view_pager.setCurrentItem(0);

    }
}