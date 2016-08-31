package wellke.com.pulldownview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by wellke(zengke) on 2016/5/31.
 */
public class MyPullDownView extends LinearLayout {
    private static final String TAG = "MyPullDownView";

    private int refreshTargetTop = -60;
    private int headContentHeight;

    private RefreshListener refreshListener;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    private final static int RATIO = 2;
    private int preY = 0;
    private boolean isElastic = false;
    private int startY;
    private int state;

    private String note_release_to_refresh = "";
    private String note_pull_to_refresh = "";
    private String note_refreshing = "";

    private MyPullDownElastic mElastic;


    public MyPullDownView(Context context) {
        super(context);
        init();

    }

    public MyPullDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);
    }

    /**
     * 刷新监听
     *
     * @param listener
     */
    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    /**
     * 下拉布局
     *
     * @param elastic
     */
    public void setPullDownElastic(MyPullDownElastic elastic) {
        mElastic = elastic;

        headContentHeight = mElastic.getElasticHeight();
        refreshTargetTop = -headContentHeight;
        LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, headContentHeight);
        lp.topMargin = refreshTargetTop;
        addView(mElastic.getElasticLayout(), 0, lp);
    }

    /**
     * 设置更新提示语
     *
     * @param pullToRefresh    下拉刷新提示语
     * @param releaseToRefresh 松开刷新提示语
     * @param refreshing       正在刷新提示语
     */
    public void setRefreshTips(String pullToRefresh, String releaseToRefresh, String refreshing) {
        note_pull_to_refresh = pullToRefresh;
        note_release_to_refresh = releaseToRefresh;
        note_refreshing = refreshing;
    }

    /*
     * 该方法一般和ontouchEvent 一起用 (non-Javadoc)
     *
     * @see
     * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        printMotionEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            preY = (int) ev.getY();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {

            if (!isElastic && canScroll()
                    && (int) ev.getY() - preY >= headContentHeight / (3 * RATIO)
                    && refreshListener != null && mElastic != null) {

                isElastic = true;
                startY = (int) ev.getY();
                return true;
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        printMotionEvent(event);
        handleHeadElastic(event);
        return super.onTouchEvent(event);
    }

    private void handleHeadElastic(MotionEvent event) {
        if (refreshListener != null && mElastic != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:

                    if (state != MyPullDownElastic.REFRESHING && isElastic) {

                        if (state == MyPullDownElastic.DONE) {
                            // 什么都不做
                            setMargin(refreshTargetTop);
                        }
                        if (state == MyPullDownElastic.PULL_To_REFRESH) {
                            state = MyPullDownElastic.DONE;
                            setMargin(refreshTargetTop);
                            changeHeaderViewByState(state, false);
                        }
                        if (state == MyPullDownElastic.RELEASE_To_REFRESH) {
                            state = MyPullDownElastic.REFRESHING;
                            setMargin(0);
                            changeHeaderViewByState(state, false);
                            onRefresh();
                        }

                    }
                    isElastic = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int tempY = (int) event.getY();

                    if (state != MyPullDownElastic.REFRESHING && isElastic) {
                        // 可以松手去刷新了
                        if (state == MyPullDownElastic.RELEASE_To_REFRESH) {
                            if (((tempY - startY) / RATIO < headContentHeight)
                                    && (tempY - startY) > 0) {
                                state = MyPullDownElastic.PULL_To_REFRESH;
                                changeHeaderViewByState(state, true);
                            } else if (tempY - startY <= 0) {
                                state = MyPullDownElastic.DONE;
                                changeHeaderViewByState(state, false);
                            }
                        }
                        if (state == MyPullDownElastic.DONE) {
                            if (tempY - startY > 0) {
                                state = MyPullDownElastic.PULL_To_REFRESH;
                                changeHeaderViewByState(state, false);
                            }
                        }
                        if (state == MyPullDownElastic.PULL_To_REFRESH) {
                            // 下拉到可以进入RELEASE_TO_REFRESH的状态
                            if ((tempY - startY) / RATIO >= headContentHeight) {
                                state = MyPullDownElastic.RELEASE_To_REFRESH;
                                changeHeaderViewByState(state, false);
                            } else if (tempY - startY <= 0) {
                                state = MyPullDownElastic.DONE;
                                changeHeaderViewByState(state, false);
                            }
                        }
                        if (tempY - startY > 0) {
                            setMargin((tempY - startY) / 2 + refreshTargetTop);
                        }
                    }
                    break;
            }
        }
    }

    /**
     *
     */
    private void setMargin(int top) {
        LinearLayout.LayoutParams lp = (LayoutParams) mElastic.getElasticLayout()
                .getLayoutParams();
        lp.topMargin = top;
        // 修改后刷新
        mElastic.getElasticLayout().setLayoutParams(lp);
        mElastic.getElasticLayout().invalidate();
    }

    private void changeHeaderViewByState(int state, boolean isBack) {

        mElastic.changeElasticState(state, isBack);
        switch (state) {
            case MyPullDownElastic.RELEASE_To_REFRESH:
                mElastic.showArrow(View.VISIBLE);
                mElastic.showProgressBar(View.GONE);
                mElastic.showLastUpdate(View.VISIBLE);
                mElastic.setTips(note_release_to_refresh);

                mElastic.clearAnimation();
                mElastic.startAnimation(animation);
                break;
            case MyPullDownElastic.PULL_To_REFRESH:
                mElastic.showArrow(View.VISIBLE);
                mElastic.showProgressBar(View.GONE);
                mElastic.showLastUpdate(View.VISIBLE);
                mElastic.setTips(note_pull_to_refresh);

                mElastic.clearAnimation();

                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {
                    mElastic.startAnimation(reverseAnimation);
                }
                break;
            case MyPullDownElastic.REFRESHING:
                mElastic.showArrow(View.GONE);
                mElastic.showProgressBar(View.VISIBLE);
                mElastic.showLastUpdate(View.VISIBLE);
                mElastic.setTips(note_refreshing);

                mElastic.clearAnimation();
                break;
            case MyPullDownElastic.DONE:
                mElastic.showProgressBar(View.GONE);
                mElastic.clearAnimation();
                break;
        }
    }

    private void onRefresh() {

        if (refreshListener != null) {
            refreshListener.onRefresh(this);
        }
    }

    /**
     *
     */
    @Override
    public void computeScroll() {

    }

    /**
     * 结束刷新事件，UI刷新完成后必须回调此方法
     *
     * @param text 一般传入：“上次更新时间:12:23”
     */
    public void finishRefresh(String text) {
        if (mElastic == null) {
            return;
        }
        state = MyPullDownElastic.DONE;
        mElastic.setLastUpdateText(text);
        changeHeaderViewByState(state, false);

        mElastic.showArrow(View.VISIBLE);
        mElastic.showLastUpdate(View.VISIBLE);
        setMargin(refreshTargetTop);
    }

    private boolean canScroll() {
        View childView;
        if (getChildCount() > 1) {
            childView = this.getChildAt(1);
            if (childView instanceof AbsListView) {
                int top = ((AbsListView) childView).getChildAt(0).getTop();
                int pad = ((AbsListView) childView).getListPaddingTop();
                if ((Math.abs(top - pad)) < 3
                        && ((AbsListView) childView).getFirstVisiblePosition() == 0) {
                    return true;
                } else {
                    return false;
                }
            } else if (childView instanceof ScrollView) {
                if (((ScrollView) childView).getScrollY() == 0) {
                    return true;
                } else {
                    return false;
                }
            }

        }
        return canScroll(this);
    }

    /**
     * 子类重写此方法可以兼容其它的子控件，目前只兼容AbsListView和ScrollView
     *
     * @param view
     * @return
     */
    public boolean canScroll(MyPullDownView view) {
        return false;
    }

    private void printMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            default:
                break;
        }
    }

    /**
     * 刷新监听接口
     */
    public interface RefreshListener {
        public void onRefresh(MyPullDownView view);
    }

}
