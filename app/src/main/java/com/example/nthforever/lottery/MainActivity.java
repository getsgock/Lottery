package com.example.nthforever.lottery;

import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Lottery.LotteryOnClickListener {

    private Lottery lottery;
    private Button start1,reset,start2,start3,start4,start5,start6,start7,start8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lottery = (Lottery) findViewById(R.id.lottery);
        start1 = (Button) findViewById(R.id.start0);
        start2 = (Button) findViewById(R.id.start1);
        start3 = (Button) findViewById(R.id.start2);
        start4 = (Button) findViewById(R.id.start3);
        start5 = (Button) findViewById(R.id.start4);
        start6 = (Button) findViewById(R.id.start5);
        start7 = (Button) findViewById(R.id.start6);
        start8 = (Button) findViewById(R.id.start7);
        reset = (Button) findViewById(R.id.reset);

        start1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(0);
            }
        });
        start2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(1);
            }
        });
        start3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(2);
            }
        });
        start4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(3);
            }
        });
        start5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(4);
            }
        });
        start6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(5);
            }
        });
        start7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(6);
            }
        });
        start8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.start(7);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottery.reset();
            }
        });
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        lottery.setMetrics(metrics);
        lottery.setListener(this);
    }

    @Override
    public void onclick(int status) {
        Toast.makeText(this, "状态为: "+lottery.getStatus(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinish(int i) {
        Toast.makeText(this, "抽中了: "+i+"等奖", Toast.LENGTH_SHORT).show();
    }
}
