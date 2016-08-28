package com.example.niugulu.viewstudy;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.niugulu.viewstudy.view.LeafLoadingView;
import com.example.niugulu.viewstudy.view.MagicCircle;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int REFRESH_PROGRESS = 0x10;
    private int mProgress;
    private LeafLoadingView mLeafLoadingView;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_PROGRESS:
                    if (mProgress < 40) {
                        mProgress += 1;
                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(800));
                        mLeafLoadingView.setCurrentProgress(mProgress);
                    } else {
                        mProgress += 1;
                        // 随机1200ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(1200));
                        mLeafLoadingView.setCurrentProgress(mProgress);

                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLeafLoadingView = (LeafLoadingView) findViewById(R.id.loading_view);
        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS, 3000);
    }
}
