package aueb.ds.musicstreamer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Player extends Activity {

    //mediaplayer or exoplayer

    TextView temp;
    Button play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        Bundle b = getIntent().getExtras();
        String songname = b.getString("songname");

        temp = findViewById(R.id.textView);
        play = findViewById(R.id.button);
        temp.setText(songname);
    }

    @Override
    protected void onStart() {
        super.onStart();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //play the song
            }
        });
    }
}
