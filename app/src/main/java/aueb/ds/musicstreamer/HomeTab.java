package aueb.ds.musicstreamer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeTab extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button startBtn;
    Switch offModeSwitch;
    Button retry;
    TextView noInternet;
    //Boolean switch_state;

    public HomeTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeTab.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeTab newInstance(String param1, String param2) {
        HomeTab fragment = new HomeTab();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab, container, false);

        startBtn = (Button) view.findViewById(R.id.start_btn);
        offModeSwitch = (Switch) view.findViewById(R.id.onOff_switch);
        retry = (Button) view.findViewById(R.id.retry);
        noInternet = (TextView) view.findViewById(R.id.noInternet);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_home_tab, viewGroup);
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("switch", offModeSwitch.isChecked());
    }*/

    public void onStart() {
        super.onStart();

        offModeSwitch.setChecked(!isNetworkAvailable());

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });

        if (offModeSwitch.isChecked()) {
            offModeSwitch.setClickable(false);
            onlineOfflineSwitcher(true);
            Toast.makeText(getActivity(), "Offline mode enabled.", Toast.LENGTH_SHORT).show();
            noInternet.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            if (!offModeSwitch.isChecked()) {
                onlineOfflineSwitcher(false);
            } else {
                onlineOfflineSwitcher(true);
            }
            offModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(getActivity(), "Offline", Toast.LENGTH_SHORT).show();
                        onlineOfflineSwitcher(true);
                    } else {
                        Toast.makeText(getActivity(), "Online", Toast.LENGTH_SHORT).show();
                        onlineOfflineSwitcher(false);
                    }
                }
            });
        }
    }

    public void onlineOfflineSwitcher(boolean b) {
        if (b) {
            //switch_state = true;
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent s = new Intent(v.getContext(), OfflineModeActivity.class);
                    startActivityForResult(s, 0);
                }
            });
        } else {
            //switch_state = false;
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new SearchTab()).commit();
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
