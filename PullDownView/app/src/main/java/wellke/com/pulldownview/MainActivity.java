package wellke.com.pulldownview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import wellke.com.pulldownview.view.MyPullDownElasticImpl;
import wellke.com.pulldownview.view.MyPullDownView;

public class MainActivity extends AppCompatActivity implements MyPullDownView.RefreshListener {
    private MyPullDownView refresh_root;
    private ImageView imageView;
    private int[] images;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        refresh_root = (MyPullDownView) findViewById(R.id.refresh_root);
        imageView = (ImageView) findViewById(R.id.image);
        random = new Random();
        images = new int[]{R.mipmap.a, R.mipmap.b, R.mipmap.c, R.mipmap.d};
        refresh_root.setRefreshTips("你在旅行，他在赚钱", "你在旅行，他在赚钱", "正在加载...");
        refresh_root.setRefreshListener(this);
        refresh_root.setPullDownElastic(new MyPullDownElasticImpl(this));

    }

    @Override
    public void onRefresh(MyPullDownView view) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setBackgroundResource(images[random.nextInt(images.length)]);
                            refresh_root.finishRefresh("上次更新: " + Formatter.simpleTimeFormat.format(new Date(System.currentTimeMillis())));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    interface Formatter {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DecimalFormat amountFormat = new DecimalFormat("##.##");
    }

}
