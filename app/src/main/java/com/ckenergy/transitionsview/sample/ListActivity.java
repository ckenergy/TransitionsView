package com.ckenergy.transitionsview.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ListActivity extends AppCompatActivity {

    RecyclerView rv;
    private List<Integer> mList;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        rv = (RecyclerView) findViewById(R.id.rv);
        mList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mList.add(R.mipmap.bg_rv1);
        }
        mAdapter = new MyAdapter(this, R.layout.item_rv, mList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mAdapter);
    }

}
