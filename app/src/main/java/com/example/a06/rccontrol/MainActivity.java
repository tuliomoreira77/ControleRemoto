package com.example.a06.rccontrol;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private static final int SERVERPORT = 4567;
    private static final String SERVER_IP = "192.168.4.1";
    private BufferedReader bufferedReader;
    private PrintWriter out;
    private Thread connection;
    private boolean control = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new connectionThread()).start();

        JoystickView aceleracao = (JoystickView) findViewById(R.id.aceleracao);
        aceleracao.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (control) {
                    int value = (255*100)/strength;
                    if (strength < 30) {
                        String message = createMessage("STP",255);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                    if (angle > 60 && angle < 150) {
                        String message = createMessage("FWD",value);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                    if (angle > 240 && angle < 300) {
                        String message = createMessage("BWD",value);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                }
            }
        });

        JoystickView direcao = (JoystickView) findViewById(R.id.curva);
        direcao.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if(control) {
                    if (strength < 10) {
                        String message = createMessage("CNT",0);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                    if (angle < 60 && angle > 0) {
                        String message = createMessage("RGT",strength);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                    if (angle > 150 && angle < 240) {
                        String message = createMessage("RGT",strength);
                        out.print((char) 0x8);
                        out.println(message);
                        return;
                    }
                }
            }
        });

        /*Button frente = (Button)findViewById(R.id.frente);
        Button re = (Button)findViewById(R.id.re);
        Button direita = (Button)findViewById(R.id.direita);
        Button esquerda = (Button)findViewById(R.id.esquerda);

        frente.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(control) {
                            out.print((char) 0x8);
                            out.println("FWD\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        if(control){
                            out.print((char) 0x8);
                            out.println("STP\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        re.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(control) {
                            out.print((char) 0x8);
                            out.println("BWD\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        if(control) {
                            out.print((char) 0x8);
                            out.println("STP\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        direita.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (control) {
                            out.print((char) 0x8);
                            out.println("RGT\n200");
                            //out.println("230");
                        }
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        if(control) {
                            out.print((char) 0x8);
                            out.println("CNT\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        esquerda.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (control) {
                            out.print((char) 0x8);
                            out.println("LFT\n200");
                            //out.println("230");
                        }
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        if(control) {
                            out.print((char) 0x8);
                            out.println("CNT\n255");
                            //out.println("255");
                        }
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });*/
    }

    String createMessage(String code,int value)
    {
        String message = code + "\n";
        message = message + String.format("%03d",value);
        return message;
    }

    private class connectionThread extends Thread {
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.print((char) 0x8);
                out.println("STP\n255");
                //out.println("255");

                out.print((char) 0x8);
                out.println("CNT\n255");
                //out.println("255");

                control = true;

            } catch (UnknownHostException e) {
                e.printStackTrace();
                socket = null;
                control = false;
            } catch (IOException e) {
                e.printStackTrace();
                control = false;
            }
        }
    }
}
