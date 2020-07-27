package aueb.ds.musicstreamer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class OnlineModeActivity extends Activity {
    private Button button;
    private EditText artistName;
    private TextView finalResult;
    private ListView listView;
    private TextView resultFromServer;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_mode);

        artistName = (EditText) findViewById(R.id.artistname);
        artistName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(artistName, InputMethodManager.SHOW_IMPLICIT);

        button = (Button) findViewById(R.id.btn_run);
        finalResult = (TextView) findViewById(R.id.tv_result);
        resultFromServer = (TextView) findViewById(R.id.resultFS);

        listView = findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.listview_layout, R.id.listRaw, new ArrayList<String>());
        listView.setAdapter(arrayAdapter);

    }

    public void onStart() {

        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String temp = prefs.getString("broker0ip",null);

        if (temp == null) {
            Intent s = new Intent(this, IpAccess.class);
            startActivityForResult(s, 0);
        }

        artistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        closeKeyboard();

                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        String artist = artistName.getText().toString();
                        runner.execute(artist);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                final String songname = arrayAdapter.getItem(position);
                                Intent s = new Intent(view.getContext(), OnlinePlayer.class);
                                s.putExtra("songname", songname);
                                String iportname = resultFromServer.getText().toString();
                                String[] splited = iportname.split("\\s+");
                                int size = splited[0].length() + splited[1].length() + 2;
                                iportname = iportname.substring(size);
                                s.putExtra("IP", splited[0]);
                                s.putExtra("PORT", splited[1]);
                                s.putExtra("artist", iportname);
                                startActivityForResult(s, 0);
                            }
                        });
                    }
                });
            }
        });
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private ArrayList<Integer> brokers_ports = new ArrayList<>();
        private ArrayList<String> brokers_ip = new ArrayList<>();
        private String resp;
        private ArrayAdapter<String> adapter;
        ArrayList<String> artists = new ArrayList<>();
        ProgressDialog progressDialog;
        String IP;
        int PORT;

        @Override
        protected void onPreExecute() {
            adapter = (ArrayAdapter<String>) listView.getAdapter();
            adapter.clear();
            progressDialog = ProgressDialog.show(OnlineModeActivity.this,
                    "Searching",
                    "Searching songs for artist "+ artistName.getText().toString());
        }

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Searching...", "Searching..."); // Calls onProgressUpdate()

            loadPorts(brokers_ip, brokers_ports);

            Random r = new Random();
            int number = r.nextInt(brokers_ip.size());

            PORT = brokers_ports.get(number);
            IP = brokers_ip.get(number);

            Log.e("IP","Server>> " + IP);
            Log.e("PORT","Server>> " + PORT);


            String art_name = params[0];

            Socket requestSocket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            try {
                requestSocket = new Socket(IP, PORT);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                String p = "Consumer";
                out.writeObject(p);
                out.flush();

                HashMap<String, String> bl = new HashMap<>();
                bl = (HashMap<String, String>) in.readObject();

                artists = (ArrayList<String>) in.readObject();

                boolean found = false;

                for (int i = 0; i < artists.size(); i++) {
                    if ((artists.get(i).toLowerCase()).equals(art_name.toLowerCase())) {
                        art_name = artists.get(i);
                        found = true;
                        break;
                    }
                }

                if (found == true) {

                    String iportname = bl.get(art_name);
                    resp = iportname + " " + art_name;
                    String[] splited = iportname.split("\\s+");


                    if (!(splited[0].equals(IP) && (Integer.parseInt(splited[1]) == PORT))) {
                        out.writeObject("yes");
                        requestSocket.close();
                        in.close();
                        out.close();

                        IP = splited[0];
                        PORT = Integer.parseInt(splited[1]);
                        requestSocket = new Socket(IP, PORT);

                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        in = new ObjectInputStream(requestSocket.getInputStream());

                        out.writeObject("reconnect");

                    } else {
                        String exit = "no";
                        out.writeObject(exit);
                        out.flush();
                    }

                    out.writeObject(art_name);
                    ArrayList<String> list = (ArrayList<String>) in.readObject();
                    //resp = Integer.toString(list.size());
                    for (String a : list) {
                        publishProgress("Searching...", a.substring(0, a.length() - 4));
                    }

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Cannot find this artist.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            finalResult.setText("Search Complete.");
            //resultFromServer.setVisibility(View.VISIBLE);
            resultFromServer.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if (text[1] == "Searching...") {
                finalResult.setText(text[0]);
            } else {
                adapter.add(text[1]);
            }

        }
    }

    public void loadPorts(ArrayList<String> brokers_ip, ArrayList<Integer> brokers_ports) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int size = prefs.getInt("size",0);

        for (int i = 0; i < size; i++) {
            String ip = prefs.getString("broker"+i+"ip", null);
            brokers_ip.add(ip);
            int port = prefs.getInt("broker"+i+"port", 0);
            brokers_ports.add(port);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent s = new Intent(getBaseContext(), MainActivity.class);
            startActivityForResult(s, 0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
