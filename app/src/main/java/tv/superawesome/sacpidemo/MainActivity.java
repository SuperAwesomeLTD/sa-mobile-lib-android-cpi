package tv.superawesome.sacpidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONObject;

import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sanetwork.request.SANetworkInterface;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SANetwork network = new SANetwork();
        network.sendGET(this, "https://ads.staging.superawesome.tv/v2/click?placement=618&rnd=1270533&sourceBundle=tv.superawesome.demoapp&line_item=1141&creative=5884&ct=wifi&sdkVersion=android_5.4.9", new JSONObject(), new JSONObject(), new SANetworkInterface() {
            @Override
            public void saDidGetResponse(int i, String s, boolean b) {
                //
            }
        });
    }
}
