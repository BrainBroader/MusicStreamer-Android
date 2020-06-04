package aueb.ds.musicstreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;

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
                Toast.makeText(getBaseContext(),"Done!", Toast.LENGTH_SHORT).show();
                Intent s = new Intent(view.getContext(), OnlineModeActivity.class);
                startActivityForResult(s, 0);
            }
        });
    }
}
