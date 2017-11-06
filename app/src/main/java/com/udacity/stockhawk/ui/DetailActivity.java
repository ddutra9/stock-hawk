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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
        try {
            populateChart(symbol, data, lineChart);
        } catch (ParseException e) {
            Log.e(TAG, "error no parse", e);
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("dd");
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String label = sdf.format(new Date((long)value));
                Log.d(TAG, "date: " + label);
                return label;
            }

        };

        IAxisValueFormatter formatterY = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Log.d(TAG, "date: " + value);
                return "$" + value;
            }

        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
//        xAxis.setAxisLineColor(white);
        xAxis.setAxisLineWidth(1.5f);
//        xAxis.setTextColor(white);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(formatterY);
        yAxis.setDrawGridLines(false);
//        yAxis.setAxisLineColor(white);
        yAxis.setAxisLineWidth(1.5f);
//        yAxis.setTextColor(white);
        yAxis.setTextSize(12f);

        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragDecelerationEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        lineChart.setDescription(description);
        lineChart.setExtraOffsets(10, 0, 0, 10);
        lineChart.animateX(1500, Easing.EasingOption.Linear);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        lineChart.invalidate(); // refresh
    }
}
