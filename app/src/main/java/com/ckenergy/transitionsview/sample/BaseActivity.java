package com.ckenergy.transitionsview.sample;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.ckenergy.transitionsview.transitions.ITransferView;
import com.ckenergy.transitionsview.transitions.MoveTransfer;
import com.ckenergy.transitionsview.transitions.TransitionsHeleper;


public class BaseActivity extends AppCompatActivity {

    MoveTransfer showMethod = new MoveTransfer();
    ImageView moveView;
    String tag;
    ITransferView.OnShowListener back;

    public void startMove(Activity activity, ImageView view, ITransferView.OnShowListener listener) {
        this.moveView = view;
        tag = activity.getClass().getName();
        TransitionsHeleper.getInstance().show(showMethod, activity, view, listener);
    }

    public boolean backMove(final ITransferView.OnShowListener backListener) {
        if (this.moveView == null) {
            return false;
        }
        boolean isFinish = TransitionsHeleper.getInstance().back(showMethod, tag, this.moveView, new ITransferView.OnShowListener() {
            @Override public void onStart() {
                if (backListener != null) {
                    backListener.onStart();
                }
            }

            @Override public void onEnd() {
                if (backListener != null) {
                    backListener.onEnd();
                }
                finish();
                overridePendingTransition(0,0);
            }
        });
        return isFinish;
    }

    public void setBackListener(ITransferView.OnShowListener back) {
        this.back = back;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(getClass().getSimpleName(),"onKeyDown, KEYCODE_BACK:");
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (backMove(back)) {
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TransitionsHeleper.unBind(tag);
    }

}
