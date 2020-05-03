package aueb.ds.musicstreamer;

import android.app.Activity;
import android.os.Bundle;

public class OfflineModeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_mode_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
