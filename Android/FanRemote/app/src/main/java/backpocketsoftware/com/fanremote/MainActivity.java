package backpocketsoftware.com.fanremote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int STATION_PORT = 4210;

    public static final int CMD_LIGHT = 0x01; // 0000001b;}
    public static final int CMD_FAN_OFF = 0x02; // 0000010b;
    public static final int CMD_FAN_REV = 0x04; // 0000100b;
    public static final int CMD_FAN_1 = 0x08; // 0001000b;
    public static final int CMD_FAN_2 = 0x10; // 0010000b;
    public static final int CMD_FAN_3 = 0x20; // 0100000b;

    private static final String PREFS_NAME = "com.backpocketsoftware.fanremote";
    private static final String FAN_ID_KEY = "fanID";
    protected String _stationIP = "192.168.86.188";
    protected int _fanID = 0x0B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_1).setOnClickListener(this);
        findViewById(R.id.button_2).setOnClickListener(this);
        findViewById(R.id.button_3).setOnClickListener(this);
        findViewById(R.id.button_on_off).setOnClickListener(this);
        findViewById(R.id.button_light).setOnClickListener(this);
        findViewById(R.id.button_reverse).setOnClickListener(this);
        findViewById(R.id.button_settings).setOnClickListener(this);

        _fanID = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(FAN_ID_KEY, _fanID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_id:
                setID();
                break;

            default:
                return false;
        }
        return true;
    }

    protected void setID() {
        final EditText et = new EditText(this);
        et.setText(String.format("%1X", _fanID));
        new AlertDialog.Builder(this)
                .setView(et)
                .setTitle("Set the fan ID below:")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _fanID = Integer.parseInt(et.getText().toString(), 16);
                        SharedPreferences.Editor ed = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                        ed.putInt(FAN_ID_KEY, _fanID);
                        ed.apply();
                    }
                })
                .create().show();
    }

    public void sendCommand(final int command) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                byte[] packet = new byte[2];
                packet[0] = (byte)(_fanID & 0xFF);
                packet[1] = (byte)(command & 0xFF);
                try {
                    DatagramSocket ds = new DatagramSocket();
                    InetAddress addr = InetAddress.getByName(_stationIP);
                    DatagramPacket dp = new DatagramPacket(packet, 2, addr, STATION_PORT);
                    ds.send(dp);
                } catch (SocketException e) {
                    Toast.makeText(MainActivity.this, "Socket exception: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                } catch (UnknownHostException e) {
                    Toast.makeText(MainActivity.this, "Unknown host exception: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "IO exception: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_1:
                sendCommand(CMD_FAN_1);
                break;
            case R.id.button_2:
                sendCommand(CMD_FAN_2);
                break;
            case R.id.button_3:
                sendCommand(CMD_FAN_3);
                break;
            case R.id.button_light:
                sendCommand(CMD_LIGHT);
                break;
            case R.id.button_reverse:
                sendCommand(CMD_FAN_REV);
                break;
            case R.id.button_on_off:
                sendCommand(CMD_FAN_OFF);
                break;
        }
    }
}
