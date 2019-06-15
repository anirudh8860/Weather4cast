package com.example.weather4cast;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeatherDataAdapter extends RecyclerView.Adapter <WeatherDataAdapter.UpcomingTempViewHolder>{

    private List<Forecastday> mForecastDayList;
    private LayoutInflater inflater;

    public static class UpcomingTempViewHolder extends RecyclerView.ViewHolder {
        final TextView dayView, maxTempView;

        public UpcomingTempViewHolder(@NonNull View itemView) {
            super(itemView);
            dayView = (TextView) itemView.findViewById(R.id.day);
            maxTempView = (TextView) itemView.findViewById(R.id.max_temp);
        }
    }

    public WeatherDataAdapter(List<Forecastday> forecastdayList) {
        mForecastDayList = forecastdayList;
    }

    @NonNull
    @Override
    public UpcomingTempViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_text_view, parent, false);
        return new UpcomingTempViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingTempViewHolder holder, int pos) {
        String dateStr = mForecastDayList.get(pos+1).getDate();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dayOfTheWeek = (String) DateFormat.format("EEEE", date);
        holder.dayView.setText(dayOfTheWeek);

        String maxTemp = String.valueOf(Math.round(mForecastDayList.get(pos+1).getDay().getMaxtempC()));
        holder.maxTempView.setText(maxTemp+"°C");
    }

    @Override
    public int getItemCount() {
        return mForecastDayList.size()-1;
    }
}
