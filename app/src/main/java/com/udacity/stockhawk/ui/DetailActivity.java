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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.entries;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int STOCK_CHART_LOADER = 0;
    private static final String TAG = DetailActivity.class.getSimpleName();

    private String symbol;

    @BindView(R.id.chart)
    LineChart lineChart;

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

    private List<ILineDataSet> populateChart(String symbol, Cursor data) throws ParseException {
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
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

            LineDataSet setComp1 = new LineDataSet(vals, symbol);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(setComp1);
        }

        return dataSets;
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
        LineData lineData = null;
        try {
            lineData = new LineData(populateChart(symbol, data));
        } catch (ParseException e) {
            Log.e(TAG, "error no parse", e);
        }
        lineChart.setData(lineData);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            private int i = 0;

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Log.d(TAG, "date: " + sdf.format(new Date((long)value)));
                return "Q" + i++;
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
