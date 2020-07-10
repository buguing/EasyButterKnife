package com.wellee.butterknife;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wellee.butterknife.annotation.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv1)
    TextView tv1;

    @BindView(R.id.tv2)
    TextView tv2;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUnbinder = ButterKnife.bind(this);

        tv1.setText("哈哈哈");
        tv2.setText("哈哈哈");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
