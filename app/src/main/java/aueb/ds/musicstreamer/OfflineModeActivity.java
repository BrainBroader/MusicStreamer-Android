package aueb.ds.musicstreamer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OfflineModeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode);
    }

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSIONS = 12345;

    private static final int PERMISSIONS_COUNT = 1;

    private boolean arePermissionsDenied() {
        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (arePermissionsDenied()) {
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        } else {
            onResume();
        }
    }

    private boolean isMusicPlayerInit;
    private List<String> musicFilesList;

    private void addMusicFilesFrom(String dirPath) {
        final File musicDir = new File(dirPath);
        if (!musicDir.exists()) {
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        for (File file : files) {
            final String path = file.getAbsolutePath();
            if ( path.endsWith(".mp3")) {
                musicFilesList.add(path);
            }
        }
    }

    private void fillMusicList() {
        musicFilesList.clear();
        addMusicFilesFrom(Environment.getExternalStorageDirectory()+"/Music/Music Streamer");
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    }

    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        final ListView listView = findViewById(R.id.listView);
        final TextAdapter textAdapter = new TextAdapter();
        musicFilesList = new ArrayList<>();
        fillMusicList();
        textAdapter.setData(musicFilesList);
        listView.setAdapter(textAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent s = new Intent(view.getContext(), OfflinePlayer.class);
                String musicFilePath = musicFilesList.get(position);
                String songname = musicFilePath.substring(musicFilePath.lastIndexOf('/') + 1);
                s.putExtra("songname", songname);
                s.putExtra("path", musicFilePath);
                s.putExtra("position", position);
                ArrayList<String> myList = new ArrayList<String>();
                for (int i = 0; i < musicFilesList.size(); i++) {
                    myList.add(musicFilesList.get(i));
                }
                s.putExtra("songsList", myList);
                startActivityForResult(s, 0);

            }
        });

    }

    class TextAdapter extends BaseAdapter {

        private List<String> data = new ArrayList<>();

        void setData(List<String> mData) {
            data.clear();
            data.addAll(mData);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
             return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.listview_layout, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.listRaw)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = data.get(position);
            holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
            return convertView;
        }

        class ViewHolder {
            TextView info;

            ViewHolder(TextView mInfo) {
                info = mInfo;
            }
        }
    }
}
