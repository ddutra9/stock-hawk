package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.PersistableBundle;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.R.id.symbol;

public class DetailActivity extends AppCompatActivity {

    public static final int STOCK_CHART_LOADER = 0;
    private static final String TAG = DetailActivity.class.getSimpleName();

    private String symbol;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.chart_vp)
    ViewPager chartVP;
    @BindView(R.id.tab_months)
    TabLayout tabMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(symbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabMonths.setupWithViewPager(chartVP, true);
        setupViewPager();
    }

    private void setupViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(setFragment(Calendar.DATE, 7), getString(R.string.week_title));
        viewPagerAdapter.addFragment(setFragment(Calendar.MONTH,1), getString(R.string.one_month_title));
        viewPagerAdapter.addFragment(setFragment(Calendar.MONTH, 3), getString(R.string.three_months_title));

        chartVP.setAdapter(viewPagerAdapter);
        chartVP.setOffscreenPageLimit(2);
    }

    private DetailFragment setFragment(int calendarField, int month){
        DetailFragment df = new DetailFragment();
        Bundle bundle = new Bundle();
        Calendar c = Calendar.getInstance();
        c.add(calendarField, -month);
        bundle.putLong(DetailFragment.SELECTED_TIME_DAY, c.getTimeInMillis());
        bundle.putString(DetailFragment.SELECTED_SYMBOL, symbol);
        df.setArguments(bundle);

        return df;
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
