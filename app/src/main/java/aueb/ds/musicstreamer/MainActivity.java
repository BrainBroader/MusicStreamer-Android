package aueb.ds.musicstreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button startBtn;
    Switch offModeSwitch;
    Button retry;
    TextView noInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn = (Button) findViewById(R.id.start_btn);
        offModeSwitch = (Switch) findViewById(R.id.onOff_switch);
        retry = (Button) findViewById(R.id.retry);
        noInternet = (TextView) findViewById(R.id.noInternet);
    }

    @Override
    protected void onStart() {
        super.onStart();

        offModeSwitch.setChecked(!isNetworkAvailable());

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        if (offModeSwitch.isChecked()) {
            offModeSwitch.setClickable(false);
            onlineOfflineSwitcher(true);
            Toast.makeText(getBaseContext(), "Offline mode enabled.", Toast.LENGTH_SHORT).show();
            noInternet.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            if (!offModeSwitch.isChecked()) {
                onlineOfflineSwitcher(false);
            } else {
                onlineOfflineSwitcher(true);
            }
            offModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(getBaseContext(), "Offline", Toast.LENGTH_SHORT).show();
                        onlineOfflineSwitcher(true);
                    } else {
                        Toast.makeText(getBaseContext(), "Online", Toast.LENGTH_SHORT).show();
                        onlineOfflineSwitcher(false);
                    }
                }
            });
        }
    }

    public void onlineOfflineSwitcher(boolean b) {
        if (b) {
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent s = new Intent(v.getContext(), OfflineModeActivity.class);
                    startActivityForResult(s, 0);
                }
            });
        } else {
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent s = new Intent(v.getContext(), OnlineModeActivity.class);
                    startActivityForResult(s, 0);
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
