package aueb.ds.musicstreamer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import MusicFile.MusicFile;
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
    ImageView coverart;

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
        coverart = findViewById(R.id.cover);
        temp.setText(songname + " "+ IP + " " + PORT + " " + artist);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
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

                Socket s = null;
                ObjectOutputStream dos = null;
                ObjectInputStream dis = null;
                try {
                    s = new Socket(IP, PORT);
                    dos = new ObjectOutputStream(s.getOutputStream());
                    dis = new ObjectInputStream(s.getInputStream());

                    String p = "ConsumerPart2";
                    dos.writeObject(p);
                    dos.flush();

                    dos.writeObject(art_name);

                    dos.writeObject(input_song + ".mp3");

                    int chunk_size = (int) dis.readObject();
                    Log.e("chunks","Server>> " + chunk_size);

                    List<MusicFile> array = new ArrayList<>();

                    for (int i = 0; i < chunk_size; i++) {
                        MusicFile chunk = (MusicFile) dis.readObject();
                        array.add(chunk);
                        String path = input_song + "-chunk" + (i+1) + ".mp3";
                        chunk.createMP3(getBaseContext(),chunk,path);

                        //taking the album cover from the first chunk
                        if (i ==0) {
                            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(getFilesDir().getPath() + "/" + path);
                            byte[] data = mmr.getEmbeddedPicture();

                            if (data != null) {
                                final Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        coverart.setImageBitmap(cover);
                                        coverart.getLayoutParams().height = 500;
                                        coverart.getLayoutParams().width = 500;

                                    }
                                });
                            }
                        }
                    }

                    resp = Integer.toString(array.size());
                    Log.e("array.size()","Server>> " + array.size());


                    /*MusicFile parse = new MusicFile();
                    System.out.println("Playing "+ input_song + "...");
                    String name = input_song.substring(0, input_song.length() - 4);
                    for (int i = 0; i < array.size(); i++) {
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
                        dis.close(); dos.close();
                        s.close();
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