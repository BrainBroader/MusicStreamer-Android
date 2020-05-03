package aueb.ds.musicstreamer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class OnlineModeActivity extends Activity {
    private Button button;
    private EditText time;
    private TextView finalResult;

    private TextView resultFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_mode);
        //time = (EditText) findViewById(R.id.in_time);
        //button = (Button) findViewById(R.id.btn_run);
        finalResult = (TextView) findViewById(R.id.tv_result);
        resultFromServer = (TextView) findViewById(R.id.resultFS);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    public void onStart() {
        super.onStart();
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            //publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                //int time = Integer.parseInt(params[0])*1000;
                //Thread.sleep(time);
                //resp = "Slept for " + params[0] + " seconds";

                int a = 10;
                int b = 40;

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    requestSocket = new Socket("192.168.1.11", 5057);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    String p = "Consumer";
                    out.writeObject(p);
                    //out.flush();

                    HashMap<String, String> bl = new HashMap<>();
                    bl = (HashMap<String, String>) in.readObject();


                    Log.e("PAME KAYLA","Server>> " + bl.size());

                    ArrayList<String> artists = new ArrayList<>();
                    artists = (ArrayList<String>) in.readObject();

                    for (int i=0; i < artists.size(); i++) {
                        Log.e("ASYNCTASKLAB","Server>> " + artists.get(i));
                    }

                    resp = Integer.toString(artists.size());


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
                resp = e.getMessage();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            //finalResult.setText("I slept for some seconds...");
            resultFromServer.setVisibility(View.VISIBLE);
            resultFromServer.setText(result);
        }

        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(OnlineModeActivity.this,
                    "ProgressDialog",
                    "Wait for "+time.getText().toString()+ " seconds");*/
        }

        @Override
        protected void onProgressUpdate(String... text) {
            finalResult.setText(text[0]);
        }
    }
}
