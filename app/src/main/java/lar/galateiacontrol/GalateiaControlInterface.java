package lar.galateiacontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GalateiaControlInterface extends AppCompatActivity implements OnTouchListener, SensorEventListener {

    private Socket connectionSocket;
    private long lastUpdate;
    private int lastCommand;
    private int controlMode = CONTROL_MODE_MANUAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galateia_control_interface);


        SensorManager senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_galateia_control_interface, menu);

        ImageButton backwardsButton = (ImageButton) findViewById(R.id.backwardsButton);
        backwardsButton.setOnTouchListener(this);
        backwardsButton.setEnabled(false);

        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setOnTouchListener(this);
        forwardButton.setEnabled(false);

        ImageButton turnLeftButton = (ImageButton) findViewById(R.id.turnLefButton);
        turnLeftButton.setOnTouchListener(this);
        turnLeftButton.setEnabled(false);

        ImageButton turnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        turnRightButton.setOnTouchListener(this);
        turnRightButton.setEnabled(false);

        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnTouchListener(this);
        stopButton.setEnabled(false);

        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnTouchListener(this);
        connectButton.setEnabled(true);

        Button controlTypeButton = (Button) findViewById(R.id.controlTypeButton);
        controlTypeButton.setOnTouchListener(this);
        controlTypeButton.setEnabled(true);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {


        }

        return super.onOptionsItemSelected(item);
    }


    /*
    #define U 10 //256
    #define D 5  //257
    #define L 6  //258
    #define R 9  //259
    #define S 0  //261
    */
    static final char forward=5;
    static final char backward=10;
    static final char turnleft=6;
    static final char turnright=9;
    static final char stop=0;
    static final int CONTROL_MODE_ACCELEROMETER = 0;
    static final int CONTROL_MODE_MANUAL = 1;

    public void onClick(View v) {


    }

    private void enableManualControl() {
        ImageButton backwardsButton = (ImageButton) findViewById(R.id.backwardsButton);
        backwardsButton.setEnabled(true);

        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setEnabled(true);

        ImageButton turnLeftButton = (ImageButton) findViewById(R.id.turnLefButton);
        turnLeftButton.setEnabled(true);

        ImageButton turnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        turnRightButton.setEnabled(true);

        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setEnabled(true);
    }

    private void disableManualControl() {
        ImageButton backwardsButton = (ImageButton) findViewById(R.id.backwardsButton);
        backwardsButton.setEnabled(false);

        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setEnabled(false);

        ImageButton turnLeftButton = (ImageButton) findViewById(R.id.turnLefButton);
        turnLeftButton.setEnabled(false);

        ImageButton turnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        turnRightButton.setEnabled(false);

        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
    }

    public void connectionLost(){

        disableManualControl();

        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setEnabled(true);
        connectButton.setText("Reconectar");
    }

    public void connectionDone(Socket connectionSocket){

        this.connectionSocket = connectionSocket;

        ImageButton backwardsButton = (ImageButton) findViewById(R.id.backwardsButton);
        backwardsButton.setEnabled(true);

        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setEnabled(true);

        ImageButton turnLeftButton = (ImageButton) findViewById(R.id.turnLefButton);
        turnLeftButton.setEnabled(true);

        ImageButton turnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        turnRightButton.setEnabled(true);

        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setEnabled(true);

        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setText("Conectado!");
    }

    public void connectionFailed(){
        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setEnabled(true);
        connectButton.setText("Tentar Novamente");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if(controlMode == CONTROL_MODE_ACCELEROMETER) {
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    if (z > 10 && y > -3 && y < 3) { //turns instead of going forward
                        try {
                            OutputStream out = connectionSocket.getOutputStream();
                            out.write(forward);
                            lastCommand = forward;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connectionLost();
                        }
                    } else if (z < 5 && y > -3 && y < 3) {
                        try {
                            OutputStream out = connectionSocket.getOutputStream();
                            out.write(backward);
                            lastCommand = backward;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connectionLost();
                        }
                    } else if (y < -3) {

                        try {
                            OutputStream out = connectionSocket.getOutputStream();
                            out.write(turnleft);
                            lastCommand = turnleft;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connectionLost();
                        }
                    } else if (y > 3) {
                        try {
                            OutputStream out = connectionSocket.getOutputStream();
                            out.write(turnright);
                            lastCommand = turnright;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connectionLost();
                        }
                    } else if (z < 10 && z > 5 &&
                            y > -3 && y < 3 ) {
                        try {
                            OutputStream out = connectionSocket.getOutputStream();
                            lastCommand = stop;
                            out.write(stop);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connectionLost();
                        }
                    }
                }
            }
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println(v);


        if(v == findViewById(R.id.connectButton) && event.getAction() == MotionEvent.ACTION_UP) {
            Button connectButton = (Button) v;
            connectButton.setText("Conectando");
            System.out.println("down");

            connectButton.setEnabled(false);

            MyClientTask myClientTask = new MyClientTask(this);
            myClientTask.execute();
        }
        else if(v == findViewById(R.id.controlTypeButton)
                && event.getAction() == MotionEvent.ACTION_UP){
            if (controlMode == CONTROL_MODE_ACCELEROMETER){
                controlMode = CONTROL_MODE_MANUAL;
                Button controlModeButton = (Button) v;
                controlModeButton.setText("Controle pelo Acelerômetro");
                enableManualControl();
            }
            else{
                disableManualControl();
                controlMode = CONTROL_MODE_ACCELEROMETER;
                Button controlModeButton = (Button) v;
                controlModeButton.setText("Controle Manual");
            }

        }
        if(controlMode == CONTROL_MODE_MANUAL){
            if(v == findViewById(R.id.turnLefButton)){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(turnleft);
                        System.out.println("sending " + (int)turnleft);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
                else{
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(stop);
                        System.out.println("sending " + (int)stop);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
            }
            else if(v == findViewById(R.id.turnRightButton)){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(turnright);
                        System.out.println("sending " + (int)turnright);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
                else{
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(stop);
                        System.out.println("sending " + (int)stop);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
            }
            else if(v == findViewById(R.id.forwardButton)){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(forward);
                        System.out.println("sending " + (int)forward);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
                else{
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(stop);
                        System.out.println("sending " + (int)stop);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
            }
            else if(v == findViewById(R.id.backwardsButton)){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(backward);
                        System.out.println("sending " + (int)backward);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    try {
                        OutputStream out = connectionSocket.getOutputStream();
                        out.write(stop);
                        System.out.println("sending " + (int)stop);
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                    }
                }

            }
            else if(v == findViewById(R.id.stopButton) &&
                    event.getAction() == MotionEvent.ACTION_DOWN){

            }
        }

        return false;
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        GalateiaControlInterface selfInstance;
        Socket connectionSocket = null;

        boolean connected = false;

        MyClientTask(GalateiaControlInterface selfInstance){
            dstAddress = "192.168.0.101"; //hardcoded address
            dstPort = 7777;
            this.selfInstance = selfInstance;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            System.out.println("Conntecting to robot");

            try {
                connectionSocket = new Socket(dstAddress, dstPort);


                //outputStream = socket.getOutputStream();
                connected = true;


            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(connected)
                selfInstance.connectionDone(connectionSocket);
            else
                selfInstance.connectionFailed();

        }

    }
}
