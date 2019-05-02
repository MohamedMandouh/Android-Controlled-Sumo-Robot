package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "Main";

    private TextView status;
    private Bluetooth bt;
    private long lastTimeSent;
    private int L = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new Bluetooth(this, mHandler);
        status = (TextView) findViewById(R.id.textStatus);

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectService();
            }
        });
        findViewById(R.id.btnrst).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.sendMessage("-1 -100");
            }
        });
        lastTimeSent = 0;
//TODO : add imageview in activity_main.xml with src=
        ImageView imageView = (ImageView) findViewById(R.id.analogImage);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(System.currentTimeMillis() - lastTimeSent < 100)
                    return true;
                lastTimeSent = System.currentTimeMillis();
                double x = event.getX()-L/2;
                double y = event.getY()*-1 + L/2; //get and shift the coordintes (all in pixels)

                int r = (int)Math.sqrt(x*x+y*y) * 255 / 500;

                double theta = Math.atan2(y, x) - Math.PI / 2;
                if(theta < 0)theta += 2 * Math.PI;
                if(theta < 0)theta += 2 * Math.PI;
                int integer_theta = (int)Math.round(theta / (2 * Math.PI) * 100);
                if(r <= 255)
                    bt.sendMessage(String.format("%d %d", r, integer_theta)); //format : "r;theta"
                return true;
            }
        });

    }

    public void connectService() {
        try {
            status.setText("Connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice("HC-05");
                status.setText(String.valueOf(bt.getState()));
            } else {
                status.setText("Bluetooth Not enabled");
            }
        } catch (Exception e) {
            status.setText("Unable to connect " + e);
        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    status.setText("MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    status.setText("MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:
                    status.setText("MESSAGE_READ ");
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    status.setText("MESSAGE_DEVICE_NAME " + msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    status.setText("MESSAGE_TOAST " + msg);
                    break;
            }
        }
    };
}