package aueb.ds.musicstreamer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class OfflinePlayer extends Activity {

    private SeekBar seekBar;
    private TextView songPositionTextView;
    private TextView songDurationTextView;
    private TextView songName;
    private Button pauseButton;
    private ImageView coverart;
    private Button skip_next;
    private Button skip_previous;
    private String songname;
    private String artist;
    private TextView Artist;
    private String musicPath;
    private int songPosition;
    private ArrayList<String> songList;
    private int position;
    private MediaPlayer mp;
    private int songDuration;
    private SeekBar soundBar;
    private AudioManager audioManager;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_player);
        seekBar = findViewById(R.id.seekBar2);
        songPositionTextView = findViewById(R.id.currentPosition);
        songDurationTextView = findViewById(R.id.songDuration);
        pauseButton = findViewById(R.id.pause);
        coverart = findViewById(R.id.cover);
        songName = findViewById(R.id.songName);
        skip_next = findViewById(R.id.next);
        skip_previous = findViewById(R.id.previous);

        Bundle b = getIntent().getExtras();

        songname = b.getString("songname");
        songName.setText(songname.substring(0, songname.length() - 4));
        musicPath = b.getString("path");
        songList = (ArrayList<String>) getIntent().getSerializableExtra("songsList");
        position = b.getInt("position");

        //artist = b.getString("artist");
        //Artist.setText(artist);

        //soundBar
        soundBar = findViewById(R.id.SoundBar);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    @Override
    protected void onStart() {
        super.onStart();

        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(musicPath);
        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            final Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
            coverart.setImageBitmap(cover);
            //coverart.getLayoutParams().height = 500;
            //coverart.getLayoutParams().width = 500;
        }

        if(mp == null) {
            playSong();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int songProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songPosition = songProgress;
                mp.seekTo(songProgress);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp.isPlaying()) {
                    mp.pause();
                    pauseButton.setBackgroundResource(R.drawable.play);
                } else {
                    if (songPosition == 0) {
                        playSong();
                    } else {
                        mp.start();
                    }
                    pauseButton.setBackgroundResource(R.drawable.pause);
                }
            }
        });

        skip_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = true;
                mp.stop();
                //mp.release();
                songPosition = 0;
                mp.seekTo(songDuration);
                songPositionTextView.setText("0:00");
                seekBar.setProgress(songPosition);
                pauseButton.setBackgroundResource(R.drawable.pause);
                if (position == songList.size()-1) {
                    position = 0;
                } else {
                    position++;
                }
                String p = songList.get(position);
                musicPath = p;
                String name = p.substring(p.lastIndexOf('/') + 1);
                name = name.substring(0, name.length()-4);
                songName.setText(name);
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(musicPath);
                byte[] data = mmr.getEmbeddedPicture();

                if (data != null) {
                    final Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                    coverart.setImageBitmap(cover);
                } else {
                    coverart.setImageDrawable(getDrawable(R.drawable.album));
                }
                while (flag == true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                playSong();
            }
        });

        skip_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = true;
                mp.stop();
                //mp.release();
                songPosition = 0;
                mp.seekTo(songDuration);
                songPositionTextView.setText("0:00");
                seekBar.setProgress(songPosition);
                pauseButton.setBackgroundResource(R.drawable.pause);
                if (position == 0) {
                    position = songList.size()-1;
                } else {
                    position--;
                }
                String p = songList.get(position);
                musicPath = p;
                String name = p.substring(p.lastIndexOf('/') + 1);
                name = name.substring(0, name.length()-4);
                songName.setText(name);
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(musicPath);
                byte[] data = mmr.getEmbeddedPicture();

                if (data != null) {
                    final Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
                    coverart.setImageBitmap(cover);
                } else {
                    coverart.setImageDrawable(getDrawable(R.drawable.album));
                }
                while (flag == true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                playSong();
            }
        });

        //soundBar
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mp.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp.isPlaying()) {
            mp.pause();
        }
    }

    private void playSong() {
        final String musicFilePath = musicPath;
        songDuration = playMusicFile(musicFilePath);
        seekBar.setMax(songDuration);
        if (((songDuration/1000)%60) < 10) {
            songDurationTextView.setText((songDuration / 1000)/60 + ":0"+ (songDuration / 1000)%60);
        } else {
            songDurationTextView.setText((songDuration / 1000) / 60 + ":" + (songDuration / 1000) % 60);
        }

        new Thread() {
            public void run() {
                //isSongPlaying = true;
                songPosition = 0;
                while (songPosition < songDuration) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mp.isPlaying()) {
                        songPosition += 1000;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(songPosition);
                                if (((songPosition/1000)) < 60) {
                                    if (((songPosition/1000)) < 10) {
                                        songPositionTextView.setText(String.valueOf("0:0"+songPosition / 1000));
                                    } else {
                                        songPositionTextView.setText(String.valueOf("0:" + songPosition / 1000));
                                    }
                                } else {
                                    if (((songPosition/1000)%60) < 10) {
                                        songPositionTextView.setText(String.valueOf((songPosition / 1000)/60 + ":0"+ String.valueOf((songPosition / 1000)%60)));
                                    } else {
                                        songPositionTextView.setText(String.valueOf((songPosition / 1000) / 60 + ":" + String.valueOf((songPosition / 1000) % 60)));
                                    }
                                }
                            }
                        });
                    } else {
                        if (flag == true) {
                            break;
                        }
                    }
                }
                if (flag == false) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mp.pause();
                            songPosition = 0;
                            mp.seekTo(songPosition);
                            songPositionTextView.setText("0:00");
                            pauseButton.setBackgroundResource(R.drawable.play);
                            seekBar.setProgress(songPosition);
                        }
                    });
                } else {
                    flag = false;
                }
            }
        }.start();
    }

    private int playMusicFile(String path) {
        mp = new MediaPlayer();

        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mp.getDuration();
    }
}
