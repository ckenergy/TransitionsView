package com.ckenergy.transitionsview.sample;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.ckenergy.transitionsview.transitions.TransitionsHeleper;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

public class MyAdapter extends CommonAdapter<Integer> {
    public MyAdapter(Context context, int layoutId, List<Integer> datas) {
        super(context, layoutId, datas);
    }

    @Override
    protected void convert(final ViewHolder holder, final Integer res, int position) {

        ((ImageView) holder.itemView.findViewById(R.id.img)).setImageResource(res);
        holder.setOnClickListener(R.id.img, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mContext.startActivity(new Intent(mContext, RvDetailActivity.class));
                TransitionsHeleper.startActivity(mContext, ItemDetailActivity.class,
                        holder.itemView.findViewById(R.id.img));
            }
        });
    }
}
