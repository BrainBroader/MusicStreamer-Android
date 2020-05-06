package aueb.ds.musicstreamer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aueb.ds.musicstreamer.MusicFile;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Player extends Activity {

    MediaPlayer player;
    TextView temp;
    Button play;
    String IP;
    String PORT;
    String songname;
    String artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        Bundle b = getIntent().getExtras();
        songname = b.getString("songname");
        IP = b.getString("IP");
        PORT = b.getString("PORT");
        artist = b.getString("artist");
        temp = findViewById(R.id.textView);
        play = findViewById(R.id.button);
        temp.setText(songname + " "+ IP + " " + PORT + " " + artist);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AsyncClient runner = new AsyncClient();
        runner.execute(artist, songname, PORT, IP);

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

    private class AsyncClient extends AsyncTask<String, String, String> {
        private ArrayList<Integer> brokers_ports = new ArrayList<>();
        private ArrayList<String> brokers_ip = new ArrayList<>();
        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(Player.this,
                    "ProgressDialog",
                    "Searching songs for artist "+ songname);*/
        }

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Searching...", "Searching..."); // Calls onProgressUpdate()


            int PORT = Integer.parseInt(params[2]);
            String IP = params[3];

            Log.e("IP","Server>> " + IP);
            Log.e("PORT","Server>> " + PORT);

            try {
                String art_name = params[0];
                String input_song = params[1];
                //Thread.sleep(1000);

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    requestSocket = new Socket(IP, PORT);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    String p = "ConsumerPart2";
                    out.writeObject(p);
                    out.flush();

                    out.writeObject(art_name);

                    out.writeObject(input_song + ".mp3");

                    int chunk_size = (int) in.readObject();
                    Log.e("chunks","Server>> " + chunk_size);

                    List<MusicFile> array = new ArrayList<>();

                    for (int i = 0; i < chunk_size; i++) {
                        MusicFile chunk = (MusicFile) in.readObject();
                        array.add(chunk);
                    }
                    resp = Integer.toString(array.size());
                    Log.e("array.size()","Server>> " + array.size());
                    MusicFile parse = new MusicFile();


                    System.out.println("Playing "+ input_song + "...");
                    String name = input_song.substring(0, input_song.length() - 4);

                    /*for (int i = 0; i < array.size(); i++) {
                        String path = name + "-chunk" + (i+1) + ".mp3";
                        parse.createMP3(array.get(i), System.getProperty("user.dir") + "\\created_songs\\" + path);
                    }*/


                    //this.arts = new ArrayList<>();



                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    try {
                        in.close(); out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //resp = e.getMessage();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            //finalResult.setText("Search Complete.");
            //resultFromServer.setVisibility(View.VISIBLE);
            //resultFromServer.setText(result);
            temp.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            /*if (text[1] == "Searching...") {
                finalResult.setText(text[0]);
            }*/

        }
    }
}
