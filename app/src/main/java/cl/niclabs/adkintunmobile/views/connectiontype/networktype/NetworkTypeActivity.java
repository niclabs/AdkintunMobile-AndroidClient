package cl.niclabs.adkintunmobile.views.connectiontype.networktype;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.utils.display.ShowCaseTutorial;
import cl.niclabs.adkintunmobile.views.connectiontype.ConnectionTypeActivity;
import cl.niclabs.adkintunmobile.views.connectiontype.ConnectionTypeViewFragment;
import cl.niclabs.adkintunmobile.views.connectiontype.ConnectionTypeViewPagerAdapter;
import cl.niclabs.adkintunmobile.views.connectiontype.DoughnutChartViewFragment;
import cl.niclabs.adkintunmobile.views.connectiontype.TimelineViewFragment;

public class NetworkTypeActivity extends ConnectionTypeActivity {

    /* Android Lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_type);

        setBaseActivityParams();
        setUpToolbar();
        setUpViewPager();

        startUpdateUiThread();
    }

    // Responsabilidad de la herencia
    // Métodos a implementar de ConnectionTypeActivity
    @Override
    public void loadData(long initialTime) {
        long currentTime = System.currentTimeMillis();
        this.statistic = new DailyNetworkTypeInformation(context, initialTime, currentTime);
        this.statistic.setStatisticsInformation();
    }

    private int helpCounter;
    private ShowcaseView showcaseView;

    @Override
    public void showTutorial() {
        helpCounter = 0;
        final String[] tutorialTitle = getResources().getStringArray(R.array.tutorial_network_type_title);
        final String[] tutorialBody = getResources().getStringArray(R.array.tutorial_network_type_body);
        Target firstTarget = new ViewTarget(toolbar.getChildAt(0));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpCounter++;
                Target mTarget;

                switch (helpCounter) {
                    case 1:
                        mViewPager.setCurrentItem(0);
                        mTarget = new ViewTarget(findViewById(R.id.layout_time));
                        break;

                    case 2:
                        View mView = ((TableLayout) findViewById(R.id.time_info_table_layout)).getChildAt(0);
                        mTarget = new ViewTarget(((TableRow) mView).getChildAt(0));
                        break;

                    case 3:
                        mViewPager.setCurrentItem(1);
                        View mTimelineLegend = findViewById(R.id.timeline);
                        mTimelineLegend = ((RecyclerView)mTimelineLegend).getChildAt(0) != null ?
                                ((RecyclerView)mTimelineLegend).getChildAt(0) :
                                mTimelineLegend;
                        mTarget = new ViewTarget(mTimelineLegend);
                        break;

                    case 4:
                        mTarget = new ViewTarget(findViewById(R.id.menu_date_picker_btn));
                        showcaseView.setButtonText(getString(R.string.tutorial_close));
                        break;

                    default:
                        showcaseView.hide();
                        return;
                }
                showcaseView.setContentTitle(tutorialTitle[helpCounter]);
                showcaseView.setContentText(tutorialBody[helpCounter]);
                showcaseView.setShowcase(mTarget, true);
            }
        };

        showcaseView = ShowCaseTutorial.createViewTutorial(this, firstTarget, tutorialTitle, tutorialBody, onClickListener);
    }

    @Override
    protected void setBaseActivityParams() {
        this.title = getString(R.string.view_network_type);
        this.context = this;
        this.loadingPanel = (RelativeLayout) findViewById(R.id.loading_panel);
        ShimmerFrameLayout container =
                (ShimmerFrameLayout) findViewById(R.id.shimmer_loading);
        container.startShimmerAnimation();
    }

    @Override
    protected void setUpViewPager(){
        this.mViewPagerAdapter = new ConnectionTypeViewPagerAdapter(getSupportFragmentManager());
        ConnectionTypeViewFragment donutchartFragment, timelineFragment;

        donutchartFragment =
                DoughnutChartViewFragment.newInstance(
                        R.array.network_type_legend_colors,
                        R.array.network_type_legend_colors_soft,
                        R.array.network_type_legend_icons);
        donutchartFragment.setTitle(getString(R.string.view_connection_type_summary));

        timelineFragment = new TimelineViewFragment();
        timelineFragment.setTitle(getString(R.string.view_connection_type_detailed));

        this.mViewPagerAdapter.addFragment(donutchartFragment);
        this.mViewPagerAdapter.addFragment(timelineFragment);

        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);
        this.mViewPager.setAdapter(this.mViewPagerAdapter);
    }
}
