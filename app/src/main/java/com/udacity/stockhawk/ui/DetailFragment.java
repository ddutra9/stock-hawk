package com.udacity.stockhawk.ui;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by donato on 17/11/17.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static String SELECTED_TIME_DAY = "SELECTED_TIME_DAY";
    public static String SELECTED_SYMBOL = "SELECTED_SYMBOL";

    private Long dateSelected;
    private String symbol;
    private static final String TAG = DetailFragment.class.getSimpleName();

    @BindView(R.id.chart)
    LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        dateSelected = getArguments().getLong(SELECTED_TIME_DAY);
        symbol = getArguments().getString(SELECTED_SYMBOL);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateChart(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void populateChart(Cursor data){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        Date historyTime = new Date();
        Date limitTime = new Date(dateSelected);

        while (data.moveToNext()) {

            List<Entry> vals = new ArrayList<Entry>();
            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");
            for(int x = 0; historyTime.getTime() > limitTime.getTime(); x++){
                String s = history[x];
                String[] val = s.split(",");
                Log.d(TAG, "val: " + Arrays.toString(val));
                Entry entry = null;
                try {
                    historyTime = sdf.parse(val[0]);
                    entry = new Entry(historyTime.getTime(), Float.parseFloat(val[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                vals.add(entry);
            }

            LineDataSet lineDataSet = new LineDataSet(Lists.reverse(vals), symbol);

            lineDataSet.setColor(Color.WHITE);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setHighLightColor(Color.WHITE);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setValueTextColor(Color.GREEN);

            dataSets.add(lineDataSet);
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setHighlightPerDragEnabled(true);

        setUpXAxis(lineChart.getXAxis());
        setUpYAxis(lineChart.getAxisLeft());

        lineChart.invalidate(); // refresh
    }

    private void setUpXAxis(XAxis xAxis){
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat(getStringDateFormat());

            @Override
            public String getFormattedValue(float key, AxisBase axis) {
                return mFormat.format(new Date((long) key));
            }
        };

        xAxis.setGranularity(1f); // quantas vezes aparece no eixo x
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineWidth(2f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setValueFormatter(formatter);
    }

    private void setUpYAxis(YAxis yAxis){
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(Color.WHITE);
        yAxis.setAxisLineWidth(2f);
        yAxis.setTextColor(Color.WHITE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DetailActivity.STOCK_CHART_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private String getStringDateFormat(){
        int days = getDiferenceDays();
        if(days < 8){
            return "EEE";
        } else {
            return "dd";
        }
    }

    private int getDiferenceDays(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateSelected);

        long difference = Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis();
        return (int) (difference/ (1000*60*60*24));
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DetailActivity.STOCK_CHART_LOADER, null, this);
    }
}
