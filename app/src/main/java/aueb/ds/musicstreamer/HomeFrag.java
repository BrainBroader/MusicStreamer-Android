package aueb.ds.musicstreamer;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class HomeFrag extends Fragment {

    private EditText editClick;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editClick = view.findViewById(R.id.editTxt);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.options_menu);

        editClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent s = new Intent(v.getContext(), OnlineModeActivity.class);
                startActivityForResult(s, 0);
            }
        });
    }
}