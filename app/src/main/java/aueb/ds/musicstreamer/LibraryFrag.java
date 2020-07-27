package aueb.ds.musicstreamer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryFrag extends Fragment {

    private TextView title;
    private ListView listView;
    private List<String> musicFilesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.title);
        listView = view.findViewById(R.id.downloadsLV);

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
                ArrayList<String> myList = new ArrayList<>();
                for (int i = 0; i < musicFilesList.size(); i++) {
                    myList.add(musicFilesList.get(i));
                }
                s.putExtra("songsList", myList);
                startActivityForResult(s, 0);
            }
        });

    }

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
    }

    private class TextAdapter extends BaseAdapter {

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

        private class ViewHolder {
            TextView info;

            ViewHolder(TextView mInfo) {
                info = mInfo;
            }
        }
    }
}