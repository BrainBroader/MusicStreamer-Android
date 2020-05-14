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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private MediaPlayer player;
    private TextView temp;
    private TextView art_name;
    private TextView chunks_num;
    private Button play;
    private String IP;
    private String PORT;
    private String songname;
    private String artist;
    private ImageView coverart;
    private ArrayList<File> chunkFiles;
    private int i = 0;

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
        art_name = findViewById(R.id.textView2);
        chunks_num = findViewById(R.id.textView4);
        play = findViewById(R.id.button);
        coverart = findViewById(R.id.cover);
        temp.setText(songname);
        art_name.setText(artist);

        player = new MediaPlayer();
        chunkFiles = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        AsyncClient runner = new AsyncClient();
        runner.execute(artist, songname, PORT, IP);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!chunkFiles.isEmpty()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(chunkFiles.get(i));
                player.setDataSource(fis.getFD());
                fis.close();
                player.prepare();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.reset();
                    i++;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(chunkFiles.get(i));
                        Log.e("check","check1");
                        player.setDataSource(fis.getFD());
                        Log.e("check","check2");
                        player.prepare();
                        Log.e("check","check3");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    player.start();
                }
            });
        }

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
                        String path = input_song + "-chunk" + i+1;

                        File tempSongFile = File.createTempFile(path,"mp3",getCacheDir());
                        tempSongFile.deleteOnExit();
                        FileOutputStream fos = new FileOutputStream(tempSongFile);
                        fos.write(chunk.getMusicFileExtract());
                        fos.close();

                        chunkFiles.add(tempSongFile);

                        //chunk.createMP3(getBaseContext(),chunk,path);

                        //taking the album cover from the first chunk
                        if (i == 0) {
                            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            //mmr.setDataSource(getFilesDir().getPath() + "/" + path);
                            FileInputStream fis = new FileInputStream(tempSongFile);
                            mmr.setDataSource(fis.getFD());
                            fis.close();
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
            chunks_num.setVisibility(View.VISIBLE);
            chunks_num.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            /*if (text[1] == "Searching...") {
                finalResult.setText(text[0]);
            }*/

        }
    }

    private void playChunks(int next) {
        if (!chunkFiles.isEmpty()) {
            FileInputStream fis = null;
            //int i = 0;
            try {
                fis = new FileInputStream(chunkFiles.get(i));
                player.setDataSource(fis.getFD());
                player.prepare();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.reset();
                    try {
                        //player.setDataSource();
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    player.setNextMediaPlayer(player);
                    //playChunks(i+1);
                }
            });
            player.start();
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    player.stop();
//                    i++;
//                    FileInputStream fis = null;
//
//                    try {
//                        fis = new FileInputStream(chunkFiles.get(i));
//                        player.setDataSource(fis.getFD());
//                        player.prepare();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    player.start();
//                }
//            });
        }
    }
}