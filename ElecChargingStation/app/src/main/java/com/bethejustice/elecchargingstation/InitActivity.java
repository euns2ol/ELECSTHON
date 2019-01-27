package com.bethejustice.elecchargingstation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pm10.library.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

public class InitActivity extends AppCompatActivity {

    ViewPager pager;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SettingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        setViewPager();



        Button button = findViewById(R.id.btn_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Dialog();



//                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
//                startActivity(intent);
//
//                finish();
            }
        });

    }

    private void setViewPager(){
        pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setOffscreenPageLimit(10);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext());

        pager.setAdapter(adapter);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.circle_indicator);
        circleIndicator.setupWithViewPager(pager);
    }

    private class ImageAdapter extends PagerAdapter{

        /**
         *  images에 사용방법 이미지 추가하기
         */

        int[] images = {R.drawable.tuto1, R.drawable.tuto2, R.drawable.tuto3};
        Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(images[position]);
            container.addView(imageView, 0);

            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view ==((ImageView) o);
        }
    }

    public void Dialog(){
        dialog = new SettingDialog(InitActivity.this);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Window window = dialog.getWindow();

        int x = (int)(size.x * 0.9f);
        int y = (int)(size.y * 0.7f);
        window.setLayout(x, y);
    }
}
