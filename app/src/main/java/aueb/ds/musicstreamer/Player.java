package aueb.ds.musicstreamer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class Player extends Activity {

    MediaPlayer player;
    TextView temp;
    Button play;
    String IP;
    String PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        Bundle b = getIntent().getExtras();
        String songname = b.getString("songname");
        IP = b.getString("IP");
        PORT = b.getString("PORT");
        temp = findViewById(R.id.textView);
        play = findViewById(R.id.button);
        temp.setText(songname);
    }

    @Override
    protected void onStart() {
        super.onStart();

        player = MediaPlayer.create(this, R.raw.apex);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });
    }
}
