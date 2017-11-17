package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

        DetailActivity detailActivity = (DetailActivity) getActivity();

        if (savedInstanceState == null) {
            dateSelected = getArguments().getLong(SELECTED_TIME_DAY);
            symbol = detailActivity.symbol;
        }
        getLoaderManager().initLoader(DetailActivity.STOCK_CHART_LOADER, null, this);

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

    void populateChart(Cursor data){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        final Map<Integer, Long> mapDateXaxis = new HashMap<>();
        int days = getDiferenceDays();

        while (data.moveToNext()) {

            List<Entry> vals = new ArrayList<Entry>();
            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");
            for(int x = 0; x < days; x++){
                String s = history[x];
                String[] val = s.split(",");
                Log.d(TAG, "val: " + Arrays.toString(val));
                Entry entry = null;
                try {
                    int key = days - (x + 1);
                    mapDateXaxis.put(key, sdf.parse(val[0]).getTime());
                    entry = new Entry(key, Float.parseFloat(val[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                vals.add(entry);
            }

            LineDataSet setComp1 = new LineDataSet(Lists.reverse(vals), symbol);
            dataSets.add(setComp1);
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        final Calendar c = Calendar.getInstance();
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                c.setTimeInMillis(mapDateXaxis.get((int) value));
                return new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // é isso que fala quantassve aparece no eixo x
        xAxis.setValueFormatter(formatter);
    }

    private int getDiferenceDays(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateSelected);

        long difference =  c.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        return (int) (difference/ (1000*60*60*24));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        lineChart.invalidate(); // refresh
    }
}
