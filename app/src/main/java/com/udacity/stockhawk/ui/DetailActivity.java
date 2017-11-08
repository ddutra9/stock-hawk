package com.udacity.stockhawk.ui;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.threeten.bp.DayOfWeek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.entries;
import static android.R.color.white;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int STOCK_CHART_LOADER = 0;
    private static final String TAG = DetailActivity.class.getSimpleName();

    private String symbol;

    @BindView(R.id.chart)
    LineChart lineChart;
    private int SELECTED_TIME = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        if(savedInstanceState == null){
            symbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);
        } else {
            symbol = savedInstanceState.getString(MainActivity.STOCK_SYMBOL);
        }

        getSupportLoaderManager().initLoader(STOCK_CHART_LOADER, null, this);
    }

    private void populateChart(String symbol, Cursor data, LineChart lineChart) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        while (data.moveToNext()) {
            List<Entry> vals = new ArrayList<Entry>();

            for (String s : data.getString(Contract.Quote.POSITION_HISTORY).split("\n")) {
                String[] val = s.split(",");
                Log.d(TAG, "val: " + Arrays.toString(val));
                Entry entry = new Entry(sdf.parse(val[0]).getTime(), Float.parseFloat(val[1]));
                vals.add(entry);

                if(vals.size() == 3)
                    break;
            }

            LineDataSet dataSet = new LineDataSet(Lists.reverse(vals), symbol);
//            dataSet.setColor(white);
            dataSet.setLineWidth(2f);
            dataSet.setDrawHighlightIndicators(false);
//            dataSet.setCircleColor(white);
//            dataSet.setHighLightColor(white);
            dataSet.setDrawValues(false);
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        List<Entry> valsComp1 = new ArrayList<Entry>();
//        List<Entry> valsComp2 = new ArrayList<Entry>();
//
//        Entry c1e1 = new Entry(0f, 100000f); // 0 == quarter 1
//        valsComp1.add(c1e1);
//        Entry c1e2 = new Entry(1f, 140000f); // 1 == quarter 2 ...
//        valsComp1.add(c1e2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        final Map<Integer, Long> mapDateXaxis = new HashMap<>();
        while (data.moveToNext()) {

            List<Entry> vals = new ArrayList<Entry>();
            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");
            for(int x = 0; x < SELECTED_TIME; x++){
                String s = history[x];
                String[] val = s.split(",");
                Log.d(TAG, "val: " + Arrays.toString(val));
                Entry entry = null;
                try {
                    int key = SELECTED_TIME - (x + 1);
                    mapDateXaxis.put(key, sdf.parse(val[0]).getTime());
                    entry = new Entry(key, Float.parseFloat(val[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                vals.add(entry);
            }

            LineDataSet setComp1 = new LineDataSet(Lists.reverse(vals), symbol);
//            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(setComp1);
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

//        final String[] quarters = new String[] { "Q1", "Q2", "Q3", "Q4" };

        final Calendar c = Calendar.getInstance();
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                c.setTimeInMillis(mapDateXaxis.get((int) value));
                return new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        lineChart.invalidate(); // refresh
    }
}
