package com.PMCar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Joystick extends AppCompatActivity {

    public static TextView mCarMessage;
    private Button mReturn;
    private Button mSpeedUp;
    private Button mSpeedDown;
    private Button mTurnOn;
    private Button mTurnOff;
    private Button mCarBeep;
    private Boolean fingerUp = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        mCarMessage = (TextView) findViewById(R.id.car_message);
        mReturn = (Button) findViewById(R.id.goToMain);
        mSpeedUp = (Button) findViewById(R.id.speed_up);
        mSpeedDown = (Button) findViewById(R.id.speed_down);
        mTurnOn = (Button) findViewById(R.id.turnOn);
        mTurnOff = (Button) findViewById(R.id.turnOff);
        mCarBeep = (Button) findViewById(R.id.beep_beep);


        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnTouchListener(new JoystickView.OnTouchListener() {


            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("Trimis", "0");
                    fingerUp = true;
                    if (MainActivity.mConnectedThread != null) {
                        MainActivity.mConnectedThread.write("0");
                    }
                } else {
                    fingerUp = false;
                }
                return false;
            }


        });
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {

                if (strength <= 10) {
                    Log.d("Trimis", "0");
                    if (MainActivity.mConnectedThread != null && !fingerUp) {
                        MainActivity.mConnectedThread.write("0");
                    }
                    return;
                }

                if (angle >= 45 && angle <= 135) {
                    Log.d("Trimis", "1");
                    if (MainActivity.mConnectedThread != null && !fingerUp) {
                        MainActivity.mConnectedThread.write("1");
                    }
                } else if (angle <= 315 && angle >= 225) {
                    Log.d("Trimis", "2");
                    if (MainActivity.mConnectedThread != null && !fingerUp) {
                        MainActivity.mConnectedThread.write("2");
                    }
                } else if (angle > 135 && angle < 225) {
                    Log.d("Trimis", "3");
                    if (MainActivity.mConnectedThread != null && !fingerUp) {
                        MainActivity.mConnectedThread.write("3");
                    }
                } else {
                    Log.d("Trimis", "4");
                    if (MainActivity.mConnectedThread != null && !fingerUp) {
                        MainActivity.mConnectedThread.write("4");
                    }
                }
            }
        });

        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        mSpeedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Trimis", "5");
                if (MainActivity.mConnectedThread != null) {
                    MainActivity.mConnectedThread.write("5");
                }
            }
        });

        mSpeedDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Trimis", "6");
                if (MainActivity.mConnectedThread != null) {
                    MainActivity.mConnectedThread.write("6");
                }
            }
        });

        mTurnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Trimis", "7");
                if (MainActivity.mConnectedThread != null) {
                    MainActivity.mConnectedThread.write("7");
                }
            }
        });

        mTurnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Trimis", "8");
                if (MainActivity.mConnectedThread != null) {
                    MainActivity.mConnectedThread.write("8");
                }
            }
        });

        mCarBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Trimis", "9");
                if (MainActivity.mConnectedThread != null) {
                    MainActivity.mConnectedThread.write("9");
                }
            }
        });
    }
}
