package home.alive.mqttclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    private String TAG = "MainActivity";
    ArrayList<String> users_list = new ArrayList<String>();
    private String id = "Nihaal";//Take from Edit text
    MqttClient client;
    LinearLayout mainLayout;
    Timer t;
//    private ArrayList<String> chat=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout) findViewById(R.id.chat_layout);

        try {
            client = new MqttClient("tcp://10.124.195.9:9555", id, new MemoryPersistence());
            client.setCallback(this);
            client.connect();
            client.subscribe("users");
            client.subscribe("message/" + id);
            final MqttMessage Clientname = new MqttMessage("Nihaal".getBytes());
            t = new Timer();
            TimerTask timerTaskObj = new TimerTask() {
                public void run() {
                    try {
                        client.publish("users", Clientname);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.schedule(timerTaskObj,0, 5000);//set timer here
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Button send=(Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit=(EditText) findViewById(R.id.editText);
                String m=edit.getText().toString();;
                MqttMessage message = new MqttMessage(m.getBytes());
                try {
                    client.publish("message/"+id, message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, String.valueOf(cause));
        t.cancel();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        if (topic.equals("users")) {
            for (int i=0;i<users_list.size();i++)
            if (!users_list.get(i).contains(payload)) {
                users_list.add(payload);
            }
        } else {
            String[] parsed = topic.split("/");
            final String user2 = parsed[1];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View addView = layoutInflater.inflate(R.layout.chat_inflator, null);
                    TextView username=(TextView) addView.findViewById(R.id.username);
                    TextView message=(TextView) addView.findViewById(R.id.message);
                    username.setText(user2);
                    message.setText(payload);
                    mainLayout.addView(addView);
//stuff that updates ui

                }
            });


        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete....");
    }
}
