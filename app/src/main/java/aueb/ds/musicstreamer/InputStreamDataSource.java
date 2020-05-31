package aueb.ds.musicstreamer;

import android.media.MediaDataSource;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.M)

public class InputStreamDataSource extends MediaDataSource {

    private final byte[] data;

    public InputStreamDataSource(byte[] data) {
        this.data=data;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (position >= data.length) {
            return -1;
        }
        if (position + size > data.length) {
            size -= (position + size) - data.length;
        }
        System.arraycopy(data, (int) position,buffer,offset,size);
        return size;
    }

    @Override
    public long getSize() throws IOException {
        return data.length;
    }

    @Override
    public void close() throws IOException {}
}
