package com.example.violet;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    float humid, temperature, light;
    int x_temp = 0, x_hum = 0, x_light = 0;
    List<Entry> humLineEntries = new ArrayList<>();
    List<Entry> tempLineEntries = new ArrayList<>();
    List<Entry> lightLineEntries = new ArrayList<>();
    LineChart humLineChart;
    LineChart tempLineChart;
    LineChart lightLineChart;
    boolean init_tempLineChart = false;
    boolean init_humLineChart = false;
    boolean init_lightLineChart = false;
    TextView led_status;
    Button LED_button;
    String humid_str;
    String temperature_str;
    String light_str;
    String led = "0";
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        humid = findViewById(R.id.humid);
//        temperature = findViewById(R.id.temperature);
//        light = findViewById(R.id.light);
        LED_button = findViewById(R.id.LED_button);
        led_status = findViewById(R.id.led_status);
        humLineChart = findViewById(R.id.humLineChart);
        tempLineChart = findViewById(R.id.tempLineChart);
        lightLineChart = findViewById(R.id.lightLineChart);

        initGraph();

        startMQTT();
//
        LED_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(led.equals("0")){
                    led = "1";
                }
                else if(led.equals("1")){
                    led = "0";
                }
                sendLedSignalToMQTT(led);
            }
        });
//        drawLineChartHumid();
//        drawLineChartTemp();
//        drawLineChartLight();
    }

    void initGraph(){
        drawLineChartHumid(0, 0);
        drawLineChartLight(0, 0);
        drawLineChartTemp(0, 0);
        init_humLineChart = true;
        init_tempLineChart = true;
        init_lightLineChart = true;
    }

    private void drawLineChartHumid(int x, float humid){
        //LineChart lineChart = findViewById(R.id.lineChart);
        List<Entry> lineEntries = getDataSetHumid(x, humid);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, getString(R.string.humidity));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(3);
        lineDataSet.setColor(Color.parseColor("#9C27B0"));
        lineDataSet.setCircleColor(Color.parseColor("#9C27B0"));
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        humLineChart.getDescription().setText(getString(R.string.value_in_24_hours));
        humLineChart.getDescription().setTextSize(12);
        humLineChart.setDrawMarkers(true);
        humLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        humLineChart.animateY(1000);
        humLineChart.getXAxis().setGranularityEnabled(true);
        humLineChart.getXAxis().setGranularity(1.0f);
        humLineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        humLineChart.getXAxis().setAxisMinimum(0);
        humLineChart.getXAxis().setAxisMaximum(23);
        //humLineChart.getAxisLeft().setAxisMinimum(0);
        humLineChart.setMinimumHeight(1000);
        //lineChart.getXAxis().setLabelCount(24);
        humLineChart.setData(lineData);
    }

    private List<Entry> getDataSetHumid(int x, float humid){
//        String humid_str = "87.7";
//        float humid = Float.parseFloat(humid_str);
//        List<Entry> lineEntries = new ArrayList<>();
//        lineEntries.add(new Entry(0, 85.87f));
//        lineEntries.add(new Entry(1, 82.55f));
//        lineEntries.add(new Entry(2, 83.37f));
//        lineEntries.add(new Entry(3, 88.0f));
//        lineEntries.add(new Entry(4, humid));
//        lineEntries.add(new Entry(5, 86.3f));
//        lineEntries.add(new Entry(6, 79.47f));
//        lineEntries.add(new Entry(7, 78));
//        lineEntries.add(new Entry(8, 75));
//        lineEntries.add(new Entry(9, 77));
//        lineEntries.add(new Entry(10,87));
//        lineEntries.add(new Entry(11,80));
//        lineEntries.add(new Entry(12, 85.87f));
//        lineEntries.add(new Entry(13, 82.55f));
//        lineEntries.add(new Entry(14, 83.37f));
//        lineEntries.add(new Entry(15, 88.0f));
//        lineEntries.add(new Entry(16, humid));
//        lineEntries.add(new Entry(17, 86.3f));
//        lineEntries.add(new Entry(18, 79.47f));
//        lineEntries.add(new Entry(19, 78));
//        lineEntries.add(new Entry(20, 75));
//        lineEntries.add(new Entry(21, 77));
//        lineEntries.add(new Entry(22,87));
        if(init_humLineChart){
            init_humLineChart = false;
            humLineEntries.remove(0);
        }
        if(x > 23){
            x = 0;
            humLineEntries.clear();
        }
        humLineEntries.add(new Entry(x, humid));
        //lineEntries.add(new Entry(23,80));
//        lineEntries.clear();
//        lineEntries.add(new Entry(0, 85.87f));
        return humLineEntries;
    }

    private void drawLineChartTemp(int x, float temp){
        //LineChart lineChart1 = findViewById(R.id.lineChart1);
        List<Entry> lineEntries = getDataSetTemp(x, temp);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, getString(R.string.temperature));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(3);
        lineDataSet.setColor(Color.parseColor("#FF007F"));
        lineDataSet.setCircleColor(Color.parseColor("#FF007F"));
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        tempLineChart.getDescription().setText(getString(R.string.value_in_24_hours));
        tempLineChart.getDescription().setTextSize(12);
        tempLineChart.setDrawMarkers(true);
        tempLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        tempLineChart.animateY(1000);
        tempLineChart.getXAxis().setGranularityEnabled(true);
        tempLineChart.getXAxis().setGranularity(1.0f);
        tempLineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        tempLineChart.getXAxis().setAxisMinimum(0);
        tempLineChart.getXAxis().setAxisMaximum(23);
        tempLineChart.setMinimumHeight(1000);
        //tempLineChart.getAxisLeft().setAxisMinimum(0);
        //lineChart.getXAxis().setLabelCount(24);
        tempLineChart.setData(lineData);

    }

    private List<Entry> getDataSetTemp(int x, float temp){
//        String humid_str = "87.7";
//        float humid = Float.parseFloat(humid_str);
//        List<Entry> lineEntries = new ArrayList<>();
//        lineEntries.add(new Entry(0, 85.87f));
//        lineEntries.add(new Entry(1, 82.55f));
//        lineEntries.add(new Entry(2, 83.37f));
//        lineEntries.add(new Entry(3, 88.0f));
//        lineEntries.add(new Entry(4, humid));
//        lineEntries.add(new Entry(5, 86.3f));
//        lineEntries.add(new Entry(6, 79.47f));
//        lineEntries.add(new Entry(7, 78));
//        lineEntries.add(new Entry(8, 75));
//        lineEntries.add(new Entry(9, 77));
//        lineEntries.add(new Entry(10,87));
//        lineEntries.add(new Entry(11,80));
//        lineEntries.add(new Entry(12, 85.87f));
//        lineEntries.add(new Entry(13, 82.55f));
//        lineEntries.add(new Entry(14, 83.37f));
//        lineEntries.add(new Entry(15, 88.0f));
//        lineEntries.add(new Entry(16, humid));
//        lineEntries.add(new Entry(17, 86.3f));
//        lineEntries.add(new Entry(18, 79.47f));
//        lineEntries.add(new Entry(19, 78));
//        lineEntries.add(new Entry(20, 75));
//        lineEntries.add(new Entry(21, 77));
//        lineEntries.add(new Entry(22,87));
//        lineEntries.add(new Entry(23,80));
        if(init_tempLineChart){
            init_tempLineChart = false;
            tempLineEntries.remove(0);
        }
        if(x > 23){
            x = 0;
            tempLineEntries.clear();
        }
        tempLineEntries.add(new Entry(x, temp));
//        lineEntries.clear();
//        lineEntries.add(new Entry(0, 85.87f));
        return tempLineEntries;
    }

    private void drawLineChartLight(int x, float light){
        //LineChart lineChart = findViewById(R.id.lineChart2);
        List<Entry> lineEntries = getDataSetLight(x, light);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, getString(R.string.light));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(3);
        lineDataSet.setColor(Color.parseColor("#0000FF"));
        lineDataSet.setCircleColor(Color.parseColor("#0000FF"));
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        lightLineChart.getDescription().setText(getString(R.string.value_in_24_hours));
        lightLineChart.getDescription().setTextSize(12);
        lightLineChart.setDrawMarkers(true);
        lightLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        lightLineChart.animateY(1000);
        lightLineChart.getXAxis().setGranularityEnabled(true);
        lightLineChart.getXAxis().setGranularity(1.0f);
        lightLineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lightLineChart.getXAxis().setAxisMinimum(0);
        lightLineChart.getXAxis().setAxisMaximum(23);
        //lightLineChart.getAxisLeft().setAxisMinimum(0);
        lightLineChart.setMinimumHeight(1000);
        //lineChart.getXAxis().setLabelCount(24);
        lightLineChart.setData(lineData);

    }

    private List<Entry> getDataSetLight(int x, float light){
//        String humid_str = "87.7";
//        float humid = Float.parseFloat(humid_str);
//        List<Entry> lineEntries = new ArrayList<>();
//        lineEntries.add(new Entry(0, 85.87f));
//        lineEntries.add(new Entry(1, 82.55f));
//        lineEntries.add(new Entry(2, 83.37f));
//        lineEntries.add(new Entry(3, 88.0f));
//        lineEntries.add(new Entry(4, humid));
//        lineEntries.add(new Entry(5, 86.3f));
//        lineEntries.add(new Entry(6, 79.47f));
//        lineEntries.add(new Entry(7, 78));
//        lineEntries.add(new Entry(8, 75));
//        lineEntries.add(new Entry(9, 77));
//        lineEntries.add(new Entry(10,87));
//        lineEntries.add(new Entry(11,80));
//        lineEntries.add(new Entry(12, 85.87f));
//        lineEntries.add(new Entry(13, 82.55f));
//        lineEntries.add(new Entry(14, 83.37f));
//        lineEntries.add(new Entry(15, 88.0f));
//        lineEntries.add(new Entry(16, humid));
//        lineEntries.add(new Entry(17, 86.3f));
//        lineEntries.add(new Entry(18, 79.47f));
//        lineEntries.add(new Entry(19, 78));
//        lineEntries.add(new Entry(20, 75));
//        lineEntries.add(new Entry(21, 77));
//        lineEntries.add(new Entry(22,87));
//        lineEntries.add(new Entry(23,80));
//        lineEntries.clear();
//        lineEntries.add(new Entry(0, 85.87f));
        if(init_lightLineChart){
            lightLineEntries.remove(0);
            init_lightLineChart = false;
        }
        if(x > 23){
            x = 0;
            lightLineEntries.clear();
        }
        lightLineEntries.add(new Entry(x, light));
        return lightLineEntries;
    }

    private void startMQTT(){
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                //Log.d("mqtt", "Connected!");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("mqtt", "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                Log.d("MQTT", mqttMessage.toString());
                Log.d("topic", topic);

                if(topic.equals(mqttHelper.humidityTopic)){
                    humid_str = mqttMessage.toString();
                    humid = Float.parseFloat(humid_str);
                    drawLineChartHumid(x_hum, humid);
                    x_hum = x_hum + 1;
                    //humid.setText(humid_str);
                }
                if(topic.equals(mqttHelper.temperatureTopic)){
                    temperature_str = mqttMessage.toString();
                    temperature = Float.parseFloat(temperature_str);
                    drawLineChartTemp(x_temp, temperature);
                    x_temp = x_temp + 1;
                    //temperature.setText(temperature_str);
                }
                if(topic.equals(mqttHelper.lightTopic)){
                    light_str = mqttMessage.toString();
                    //light.setText(light_str);
                    light = Float.parseFloat(light_str);
                    drawLineChartLight(x_light, light);
                    x_light = x_light + 1;
                }
                if(topic.equals(mqttHelper.LEDTopic)){
                    status = mqttMessage.toString();
                    if(status.equals("OFF")){
                        led_status.setText("OFF");
                    }
                    else if(status.equals("ON")){
                        led_status.setText("ON");
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void sendLedSignalToMQTT(String value){

        MqttMessage msg = new MqttMessage(); msg.setId(1234);
        msg.setQos(0); msg.setRetained(true);

        byte[] b = value.getBytes(Charset.forName("UTF-8")); msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(mqttHelper.LEDTopic, msg);

        }catch (MqttException e){
        }
    }
}