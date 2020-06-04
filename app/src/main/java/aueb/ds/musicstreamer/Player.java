package aueb.ds.musicstreamer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import MusicFile.MusicFile;

public class Player extends Activity  {

    private MediaPlayer currentPlayer;
    private MediaPlayer nextPlayer;
    private TextView temp;
    private TextView art_name;
    private Button play_pause;
    private Button download;
    private String IP;
    private String PORT;
    private String songname;
    private String artist;
    private ImageView coverart;
    private SeekBar TimeBar;
    private SeekBar soundBar;
    private AudioManager audioManager;
    private ArrayList<InputStreamDataSource> mds;
    private ArrayList<MusicFile> forMerge;
    private int ch = 0;
    private boolean started = false;
    private int totalCurPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        Bundle b = getIntent().getExtras();
        songname = b.getString("songname");
        IP = b.getString("IP");
        PORT = b.getString("PORT");
        artist = b.getString("artist");

        temp = findViewById(R.id.songName);
        art_name = findViewById(R.id.artist);
        play_pause = findViewById(R.id.play_pause);
        coverart = findViewById(R.id.cover);
        download = findViewById(R.id.downloadBtn);
        TimeBar = findViewById(R.id.TimeBar);
        temp.setText(songname);
        art_name.setText(artist);

        //soundBar
        soundBar = findViewById(R.id.SoundBar);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mds = new ArrayList<>();
        forMerge = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        AsyncClient runner = new AsyncClient();
        runner.execute(artist, songname, PORT, IP);

        while (mds.size() == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (currentPlayer == null && !started) {
            started = true;
            currentPlayer = new MediaPlayer();
            currentPlayer.setDataSource(mds.get(ch));
            try {
                currentPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentPlayer.start();
            createNextPlayer();
        }

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentPlayer.isPlaying()){
                    currentPlayer.start();
                    play_pause.setBackgroundResource(R.drawable.pause);
                }
                else{
                    currentPlayer.pause();
                    play_pause.setBackgroundResource(R.drawable.play);
                }
            }
        });

        //SoundBar
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        soundBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        soundBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        final Handler handler = new Handler();
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (currentPlayer != null){
                    TimeBar.setProgress(totalCurPosition + currentPlayer.getCurrentPosition());
                    Log.e("PROGRESS", String.valueOf(totalCurPosition + currentPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    download();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentPlayer.isPlaying()) {
            currentPlayer.pause();
        }
    }

    private class AsyncClient extends AsyncTask<String, String, String> {
        private String resp;

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Searching...", "Searching..."); // Calls onProgressUpdate()

            int port = Integer.parseInt(params[2]);
            String ip = params[3];

            Log.e("IP","Server>> " + ip);
            Log.e("PORT","Server>> " + port);

            try {
                String art_name = params[0];
                String input_song = params[1];
                //Thread.sleep(1000);

                Socket s = null;
                ObjectOutputStream dos = null;
                ObjectInputStream dis = null;
                try {
                    s = new Socket(ip, port);
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

                        InputStreamDataSource isdt = new InputStreamDataSource(chunk.getMusicFileExtract());
                        mds.add(isdt);
                        forMerge.add(chunk);

                        //taking the album cover from the first chunk
                        if (i == 0) {
                            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(isdt);
                            byte[] data = mmr.getEmbeddedPicture();

                            Log.e("DURATION", Long.toString(chunk.getDuration()));
                            TimeBar.setMax((int)chunk.getDuration());

                            if (data != null) {
                                final Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        float scale = getResources().getDisplayMetrics().density;
                                        coverart.setImageBitmap(cover);
                                        coverart.getLayoutParams().height = (int) (250*scale);
                                        coverart.getLayoutParams().width = (int) (250*scale);

                                    }
                                });
                            }
                            mmr.release();
                        }
                    }
                    resp = Integer.toString(array.size());
                    Log.e("array.size()","Server>> " + array.size());

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
        protected void onPostExecute(String result) {}

        @Override
        protected void onProgressUpdate(String... text) {}
    }

    private void createNextPlayer() {
        try {
            nextPlayer = new MediaPlayer();
            nextPlayer.setDataSource(mds.get(++ch));
            nextPlayer.prepare();
            currentPlayer.setNextMediaPlayer(nextPlayer);
            totalCurPosition = TimeBar.getProgress();
            currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (ch == mds.size()-1) {
                        mp.stop();
                    } else {
                        mp.release();
                        currentPlayer = nextPlayer;
                        createNextPlayer();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void download() throws IOException {
        if (isExternalStorageWritable()) {
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath()+"/Music/Music Streamer");
            if (!dir.exists()) {
                dir.mkdir();
            }

            String path = dir.getAbsolutePath()+"/"+ songname + ".mp3";
            boolean exists = new File(path).exists();
            if (exists) {
                Toast.makeText(getBaseContext(), "Already downloaded.", Toast.LENGTH_LONG).show();
            } else {
                File mp3 = new File(dir, songname + ".mp3");
                FileOutputStream out = new FileOutputStream(mp3);
                for (int i = 0; i < forMerge.size(); i++) {
                    out.write(forMerge.get(i).getMusicFileExtract());
                }
                out.close();
                Toast.makeText(getBaseContext(), "Downloaded.", Toast.LENGTH_LONG).show();
            }
        }
    }



    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}