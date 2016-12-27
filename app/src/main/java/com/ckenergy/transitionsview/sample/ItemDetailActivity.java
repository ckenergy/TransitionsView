package com.ckenergy.transitionsview.sample;

import android.os.Bundle;
import android.widget.ImageView;

import com.ckenergy.transitionsview.transitions.ITransferView;

public class ItemDetailActivity extends BaseActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageView = (ImageView) findViewById(R.id.iv_detail);

        startMove(this, imageView, new ITransferView.OnShowListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onEnd() {
            }
        });
    }

}
