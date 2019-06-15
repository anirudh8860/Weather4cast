package com.example.weather4cast;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentTempFragment extends Fragment {

    TextView currTemp, currLoc;

    public CurrentTempFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currView = inflater.inflate(R.layout.fragment_current_temp, container, false);

        currTemp = currView.findViewById(R.id.curr_temp_c);
        currLoc = currView.findViewById(R.id.curr_location);

        currTemp.setText(getArguments().getString("curr_temp")+"°");
        currLoc.setText(getArguments().getString("curr_loc"));

        return currView;
    }

}
