package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

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

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int STOCK_CHART_LOADER = 0;
    private static final String TAG = DetailActivity.class.getSimpleName();

    public String symbol;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.stock_name_tv)
    TextView stockNameTV;
    @BindView(R.id.day_stock_hight_tv)
    TextView dayStockHightTV;
    @BindView(R.id.stock_symbol_tv)
    TextView stockSymbolTV;
    @BindView(R.id.day_stock_low_tv)
    TextView dayStockLowTV;
    @BindView(R.id.stock_price_now_tv)
    TextView stockPriceNowTV;
    @BindView(R.id.chart_vp)
    ViewPager chartVP;
    @BindView(R.id.tab_months)
    TabLayout tabMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null){
            symbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);
        } else {
            symbol = savedInstanceState.getString(MainActivity.STOCK_SYMBOL);
        }

        tabMonths.setupWithViewPager(chartVP, true);
        getSupportLoaderManager().initLoader(STOCK_CHART_LOADER, null, this);
        setupViewPager();
    }

    private void setupViewPager() {

        Calendar c = Calendar.getInstance();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        DetailFragment monthlyStock = new DetailFragment();
        c.add(Calendar.DATE, 7);
        bundle.putLong(DetailFragment.SELECTED_TIME_DAY, c.getTimeInMillis());
        monthlyStock.setArguments(bundle);

        DetailFragment weeklyStock = new DetailFragment();
        bundle = new Bundle();
        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 3);
        bundle.putLong(DetailFragment.SELECTED_TIME_DAY, c.getTimeInMillis());
        weeklyStock.setArguments(bundle);

        DetailFragment dailyStock = new DetailFragment();
        bundle = new Bundle();
        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 3);
        bundle.putLong(DetailFragment.SELECTED_TIME_DAY, c.getTimeInMillis());
        dailyStock.setArguments(bundle);

        viewPagerAdapter.addFragment(dailyStock, getString(R.string.week_title));
        viewPagerAdapter.addFragment(weeklyStock, getString(R.string.six_months_title));
        viewPagerAdapter.addFragment(monthlyStock, getString(R.string.one_year_title));

        chartVP.setAdapter(viewPagerAdapter);
        chartVP.setOffscreenPageLimit(2);
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
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }
    }
}
