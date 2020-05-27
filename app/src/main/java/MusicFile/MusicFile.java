package MusicFile;

import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MusicFile implements Serializable {

    private static final long serialVersionUID = -8051146778562959215L;

    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private long duration;
    private byte[] musicFileExtract;

    public MusicFile() {}

    public MusicFile (String trackName, String artistName, String albumInfo, String genre, long duration, byte[] musicFileExtract) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
        this.duration = duration;
        this.musicFileExtract = musicFileExtract;

    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public void setAlbumInfo(String albumInfo) {
        this.albumInfo = albumInfo;
    }

    public String getAlbumInfo() {
        return this.albumInfo;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return this.genre;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setMusicFileExtract(byte[] musicFileExtract) {
        this.musicFileExtract = musicFileExtract;
    }

    public byte[] getMusicFileExtract() {
        return this.musicFileExtract;
    }

    public void printTrack() {
        System.out.println("----------------------------------------");
        System.out.println("Title: " + getTrackName() + "\nArtist: " + getArtistName() + "\nAlbum: " + getAlbumInfo() + "\nGenre: " + getGenre());
        System.out.println("----------------------------------------");
    }

    public void printTrackInfo() {
        System.out.println(getMusicFileExtract().length + " bytes");
    }

    public static List<MusicFile> chunks(byte[] array, MusicFile music) throws IOException {
        List<MusicFile> ret = new ArrayList<MusicFile>();

        int bytes = array.length;
        int loops = 0;
        if (bytes % 512000 == 0) {
            loops = bytes / 512000;
        } else {
            loops = (bytes / 512000) + 1;
        }


        for (int i = 0; i < loops; i++) {

            if (i == loops - 1) {

                byte[] a = new byte[array.length - i * 512000];
                for (int j = 0; j < a.length; j++) {
                    a[j] = array[(i * 512000) + j];
                }
                MusicFile m = new MusicFile(music.getTrackName(),music.getArtistName(),music.getAlbumInfo(),music.getGenre(),music.getDuration(),a);
                ret.add(m);

            } else {
                byte[] a = new byte[512000];
                for (int j = 0; j < 512000; j++) {
                    a[j] = array[(i * 512000) + j];
                }
                MusicFile m = new MusicFile(music.getTrackName(),music.getArtistName(),music.getAlbumInfo(),music.getGenre(),music.getDuration(),a);
                ret.add(m);
            }
        }
        return ret;
    }

    public MusicFile reproduce(List<MusicFile> list) {

        int bounds = (list.size() - 1) * 512000;
        int last = list.get(list.size()-1).getMusicFileExtract().length;
        bounds = bounds + last;


        byte[] array = new byte[bounds];

        int counter = 0;
        for (int i = 0; i < list.size(); i++) {
            byte[] chunk = list.get(i).getMusicFileExtract();
            for (int j = 0; j < chunk.length; j++) {
                array[counter] = chunk[j];
                counter++;
            }
        }

        MusicFile musicfile = new MusicFile();
        musicfile.setTrackName(list.get(0).getTrackName());
        musicfile.setArtistName(list.get(0).getArtistName());
        musicfile.setAlbumInfo(list.get(0).getAlbumInfo());
        musicfile.setGenre(list.get(0).getGenre());
        musicfile.setMusicFileExtract(array);

        return musicfile;
    }

    public static void createMP3(Context c, MusicFile m, String path) throws IOException {

        FileOutputStream fos = null;

        try {
            fos = c.openFileOutput(path, Context.MODE_PRIVATE);
            fos.write(m.getMusicFileExtract());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}