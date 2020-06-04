package aueb.ds.musicstreamer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IpAccess extends Activity {

    EditText ipInput;
    Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_access);

        ipInput = findViewById(R.id.ipInput);
        ok = findViewById(R.id.ok);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //space is not allowed
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        ipInput.setFilters(new InputFilter[] {filter});

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ips = ipInput.getText().toString();
                String[] splited = ips.split("\\s+");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                int i = 0;
                for (i = 0; i < splited.length; i++) {
                    int port = 5056+i;
                    editor.putString("broker"+i+"ip", splited[i]);
                    editor.putInt("broker"+i+"port", port);
                    editor.apply();
                }
                editor.putInt("size", i);
                editor.apply();
                Toast.makeText(getBaseContext(),"Done!", Toast.LENGTH_SHORT).show();
                Intent s = new Intent(view.getContext(), OnlineModeActivity.class);
                startActivityForResult(s, 0);
            }
        });
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
