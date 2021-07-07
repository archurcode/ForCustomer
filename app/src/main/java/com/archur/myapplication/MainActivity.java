package com.archur.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.iot.hub.device.android.core.dynreg.TXMqttDynreg;
import com.tencent.iot.hub.device.android.core.dynreg.TXMqttDynregCallback;
import com.tencent.iot.hub.device.android.core.util.AsymcSslUtils;
import com.tencent.iot.hub.device.java.core.common.Status;
import com.tencent.iot.hub.device.java.core.mqtt.TXMqttActionCallBack;
import com.tencent.iot.hub.device.java.core.mqtt.TXMqttConnection;
import com.tencent.iot.hub.device.java.main.mqtt.MQTTRequest;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mDynConnBtn;
    private String mProductId = "";
    private String mProductKey = "";
    private String mDevName = "";
    private String mDevPSK = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDynConnBtn = findViewById(R.id.btn_dyn_conn);

        mDynConnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TXMqttDynreg dynreg = new TXMqttDynreg(mProductId, mProductKey, mDevName, new SelfMqttDynregCallback());
                if (dynreg.doDynamicRegister()) {
                    Log.d(TAG, "=====Dynamic Register OK!");
                } else {
                    Log.e(TAG, "=====Dynamic Register failed!");
                }
            }
        });
    }

    private class SelfMqttDynregCallback extends TXMqttDynregCallback {

        @Override
        public void onGetDevicePSK(String devicePsk) {
            mDevPSK = devicePsk;
            String logInfo = String.format("Dynamic register OK! onGetDevicePSK, devicePSK[%s]", devicePsk);
            Log.d(TAG, logInfo);
            TXMqttConnection conn = new TXMqttConnection(mProductId, mDevName, mDevPSK, new TXMqttActionCallBack() {
                @Override
                public void onConnectCompleted(Status status, boolean reconnect, Object userContext, String msg) {
                    Log.d(TAG, "=====onConnectCompleted " + status + msg);
                }

                @Override
                public void onConnectionLost(Throwable cause) {
                    Log.d(TAG, "=====onConnectionLost " + cause );
                }

                @Override
                public void onDisconnectCompleted(Status status, Object userContext, String msg) {
                    Log.d(TAG, "=====onDisconnectCompleted");
                }
            });
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(8);
            options.setKeepAliveInterval(240);
            options.setAutomaticReconnect(true);
            options.setSocketFactory(AsymcSslUtils.getDefaultSLLContext().getSocketFactory());
            MQTTRequest mqttRequest = new MQTTRequest("connect", 123456);
            conn.connect(options, mqttRequest);
        }

        @Override
        public void onGetDeviceCert(String deviceCert, String devicePriv) { }

        @Override
        public void onFailedDynreg(Throwable cause, String errMsg) {
            String logInfo = String.format("Dynamic register failed! onFailedDynreg, ErrMsg[%s]", cause.toString() + errMsg);
            Log.e(TAG, logInfo);
        }

        @Override
        public void onFailedDynreg(Throwable cause) {
            String logInfo = String.format("Dynamic register failed! onFailedDynreg, ErrMsg[%s]", cause.toString());
            Log.e(TAG, logInfo);
        }
    }
}