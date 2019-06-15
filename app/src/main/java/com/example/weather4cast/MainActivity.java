package com.example.weather4cast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    private String lat, lon, latlonStr = "";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager manager;
    private RecyclerView.Adapter adapter;
    private FrameLayout currTempLayout, errorLayout;
    private Fragment currTempFragment, errorFragment;
    private FragmentManager fragmentManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout refreshView;
    private StringRequest request;
    private ImageView refreshImage;
    private Animation rotation;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        refreshView = (RelativeLayout) findViewById(R.id.image_view_rl);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        fragmentManager = getSupportFragmentManager();
        currTempLayout = (FrameLayout) findViewById(R.id.curr_temp_fragment);
        errorLayout = (FrameLayout) findViewById(R.id.error_fragment);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(true);

        refreshImage = (ImageView) findViewById(R.id.refresh_img);
        rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
        rotation.setFillAfter(false);
        refreshImage.startAnimation(rotation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getAppPermissions();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                runMethods();
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        runMethods();
    }

    public void runMethods(){

        refreshView.setVisibility(View.VISIBLE);
        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    lat = String.valueOf(location.getLatitude());
                    lon = String.valueOf(location.getLongitude());
                    latlonStr = lat + "," + lon;
                    Log.e("Location", latlonStr);
                }
            }
        });
        client.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                createErrorFragment();
            }
        });

        String url = "https://api.apixu.com/v1/forecast.json?key="+BuildConfig.API_KEY+"&q="+latlonStr+"Delhi&days=5";

        final RequestQueue queue = Volley.newRequestQueue(this);
        request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        refreshView.setVisibility(View.GONE);
                        showResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        refreshView.setVisibility(View.GONE);
                        createErrorFragment();
                    }
                });

        queue.add(request);

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                queue.getCache().clear();
            }
        });
    }

    private void showResponse(final String response){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                Example example = gson.fromJson(response, Example.class);
                List<Forecastday> forecastdayList = example.getForecast().getForecastday();
                Bundle bundle = new Bundle();
                bundle.putString("curr_temp", String.valueOf((Math.round(example.getCurrent().getTempC()))));
                bundle.putString("curr_loc", example.getLocation().getName());
                adapter = new WeatherDataAdapter(forecastdayList);
                recyclerView.setAdapter(adapter);
                createMainFragment();
                currTempFragment.setArguments(bundle);
            }
        }, 3000);
    }

    private void createMainFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (getSupportFragmentManager().findFragmentById(R.id.curr_temp_fragment) != null) {
            currTempFragment = (CurrentTempFragment) getSupportFragmentManager().findFragmentById(R.id.curr_temp_fragment);
            fragmentTransaction.detach(currTempFragment);
            fragmentTransaction.attach(currTempFragment);
        }
        else {
            currTempFragment = new CurrentTempFragment();
            fragmentTransaction.replace(R.id.curr_temp_fragment, currTempFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        errorLayout.setVisibility(View.GONE);
        currTempLayout.setVisibility(View.VISIBLE);
    }

    private void createErrorFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (getSupportFragmentManager().findFragmentById(R.id.curr_temp_fragment) != null) {
            errorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.error_fragment);
            fragmentTransaction.detach(errorFragment);
            fragmentTransaction.attach(errorFragment);
        }
        else {
            errorFragment = new ErrorFragment();
            fragmentTransaction.replace(R.id.error_fragment, errorFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        errorLayout.setVisibility(View.VISIBLE);
        currTempLayout.setVisibility(View.GONE);
    }

    private void getAppPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }
}
