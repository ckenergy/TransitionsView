package com.ckenergy.transitionsview.transitions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;

public class TransitionsHeleper {

    private final static String TAG = "TransitionsHeleper";

    public static final String TRANSITION_FLAG = "transition";

    public static final int TRANSITION_TYPE = 1;

    private static TransitionsHeleper INSTANCE;

    private static HashMap<String, MoveInfo> staticMap = new HashMap<>();

    private TransitionsHeleper() {
    }

    public static TransitionsHeleper getInstance() {
        if (INSTANCE == null) {
            synchronized (TransitionsHeleper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TransitionsHeleper();
                }
            }
        }
        return INSTANCE;
    }

    public static void startActivity(Context context, Intent intent, View view) {
        startActivity(context, null, intent, view);
    }

    public static void startActivity(final Context context, final Class<?> startActivity, final View view) {
        startActivity(context, startActivity, null, view);
    }

    private static void startActivity(final Context context, final Class<?> startActivity, Intent intent, final View view) {
        if (intent == null) {
            intent = new Intent(context, startActivity);
        }
        if (view == null || !(context instanceof Activity)) {
            context.startActivity(intent);
            return;
        }
//        intent.putExtra(TRANSITION_FLAG, TRANSITION_TYPE);
        final Rect rect = new Rect();
        //get statusBar height
        final MoveInfo bean = new MoveInfo();
        view.getWindowVisibleDisplayFrame(rect);
        bean.statusBarHeight = rect.top;
        final Activity activity = (Activity) context;
        final Intent finalIntent = intent;
        view.post(new Runnable() {
            @Override
            public void run() {
                view.getGlobalVisibleRect(rect);
                bean.originPoint.set(rect.left, rect.top);
                bean.originWidth = view.getWidth();
                bean.originHeight = view.getHeight();
                bean.scaleType = ImageView.ScaleType.FIT_XY;
                if (view instanceof ImageView) {
                    Log.d(TAG, "imageview");
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();
                    bean.bitmap = drawableToBitamp(drawable);
                    bean.scaleType = imageView.getScaleType();

                }
                if (bean.bitmap == null) {
                    bean.bitmap = createBitmap(view, bean.originWidth, bean.originHeight, false);
//                    Log.d(TAG, "width:"+bean.bitmap.getWidth()+",height:"+bean.bitmap.getHeight());
                }else {
//                    Log.d(TAG, "bitmap not null");
                }
                finalIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                staticMap.put(finalIntent.getComponent().getClassName(), bean);
                activity.startActivity(finalIntent);
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    public static Bitmap drawable2BitmapWithType(Drawable drawable) {
        Bitmap bitmap = null;
        Log.d(TAG, drawable.getClass().getName());
        if (drawable instanceof BitmapDrawable) {
            Log.d(TAG, "bitmapdrawable");
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap());
        }/*else if (drawable instanceof GlideBitmapDrawable) { // 当有占位图时glide会使用这个
            GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) drawable;
            bitmap = glideBitmapDrawable.getBitmap();
        }*/else if (drawable instanceof TransitionDrawable) { //当有占位图时glide会使用这个
            TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
            int number = transitionDrawable.getNumberOfLayers();
            Log.d(TAG, "number:" +number);
            Drawable drawable1;
            if (number < 1) {
                drawable1 = transitionDrawable.getDrawable(0);
            }else {
                drawable1 = transitionDrawable.getDrawable(1);
            }
            if (drawable1 != null) {
                Log.d(TAG, "drawable1:"+drawable1.getClass().getName());
//                bitmap = drawable2BitmapWithType(drawable1);
            }else {
                Log.d(TAG, "drawable1 is null");
            }
        }
        return bitmap;
    }

    public boolean back(ITransferView moveMethod, final String tag, final ImageView targetView) {
        return back(moveMethod, tag, targetView, null);
    }

    public boolean back(ITransferView moveMethod, final String tag, final ImageView targetView, final ITransferView.OnShowListener listener) {
        final MoveInfo bean = staticMap.get(tag);
        if (bean == null || moveMethod == null) {
            return false;
        }
        targetView.setImageBitmap(null);

        int[] position = new int[2];
        targetView.getLocationOnScreen(position);
        Log.d(TAG,"top:"+position[1]+",left:"+position[0]);
        if (position[0] < -targetView.getWidth()) {
            position[0] = 0;
        }
        if (position[1] < -targetView.getHeight()) {
            position[1] = 0;
        }
        bean.targetPoint.set(position[0], position[1]);
        bean.translationY = -(bean.originPoint.y + (int) (bean.targetHeight * bean.scale) / 2
                - bean.targetPoint.y - bean.targetHeight / 2);
        bean.translationX = -(bean.originPoint.x + bean.originWidth / 2 - bean.targetPoint.x - bean.targetWidth / 2);
        bean.targetHeight = targetView.getHeight();
        bean.scale =  1.0f*bean.originHeight/bean.targetHeight;

        bean.moveLayout.setVisibility(View.VISIBLE);
        View moveView = bean.moveLayout.getChildAt(0);
        moveView.setTranslationX(bean.translationX);
        moveView.setTranslationY(bean.translationY);

        moveMethod.back(bean, moveView, new ITransferView.OnShowListener() {
            @Override
            public void onStart() {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onEnd() {
                if (listener != null) {
                    listener.onEnd();
                }
                unBind(tag);
            }
        });
        return true;
    }

    public void show(ITransferView moveMethod, final Activity activity, final ImageView targetView) {
        show(moveMethod, activity, targetView, null);
    }

    public void show(final ITransferView moveMethod, final Activity activity, final ImageView targetView, final ITransferView.OnShowListener listener) {
        if (targetView == null || activity == null) {
            return;
        }
        final MoveInfo bean = staticMap.get(activity.getClass().getName());
        if (bean == null || moveMethod == null) {
            return;
        }

        final ViewGroup parent = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (activity.getWindow().getStatusBarColor() == 0 ||
                    (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS & activity.getWindow().getAttributes().flags) != 0) {
                bean.statusBarHeight = 0;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS & activity.getWindow().getAttributes().flags) != 0) {
                bean.statusBarHeight = 0;
            }
        }
        targetView.post(new Runnable() {
            @Override
            public void run() {

                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                final FrameLayout moveLayout = new FrameLayout(activity);
                parent.addView(moveLayout, params);
                Rect rect = new Rect();
                targetView.getGlobalVisibleRect(rect);
                bean.targetPoint.set(rect.left, rect.top);
                bean.targetWidth = rect.width();
                bean.targetHeight = rect.height();
                targetView.getScaleType();
                ImageView moveView = new ImageView(activity);

                bean.scale =  1.0f*bean.originHeight/bean.targetHeight;

                FrameLayout.LayoutParams moveParams = new FrameLayout.LayoutParams(bean.originWidth, bean.originHeight);
                moveParams.setMargins(bean.originPoint.x, bean.originPoint.y-parent.getTop()-bean.statusBarHeight, 0, 0);

                moveLayout.addView(moveView, moveParams);
                moveView.setScaleType(bean.scaleType);
                if (bean.bitmap != null) {
                    moveView.setImageBitmap(bean.bitmap);
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    } else {
                        moveView.setImageDrawable(new BitmapDrawable(bean.bitmap));
                    }*/
                }
                bean.moveLayout = moveLayout;
                bean.translationY = -(bean.originPoint.y + (int) (bean.targetHeight * bean.scale) / 2
                        - bean.targetPoint.y - bean.targetHeight / 2);
                bean.translationX = -(bean.originPoint.x + bean.originWidth / 2 - bean.targetPoint.x - bean.targetWidth / 2);

                Log.d(TAG, bean.toString());

                float scale = bean.scale;

                moveView.setScaleX(scale);
                moveView.setScaleY(scale);
                moveView.setTranslationX(bean.translationX);
                moveView.setTranslationY(bean.translationY);

                moveMethod.start(bean, moveView, new ITransferView.OnShowListener() {
                    @Override
                    public void onStart() {
                        if (listener != null) {
                            listener.onStart();
                        }
                    }

                    @Override
                    public void onEnd() {
                        targetView.setImageBitmap(bean.bitmap);
                        moveLayout.setVisibility(View.GONE);
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }
                });

            }
        });
    }

    public static void unBind(String tag) {
        Log.d(TAG, "unBind");
        if (staticMap.get(tag) != null) {
            MoveInfo bean = staticMap.get(tag);
            if (bean != null) {
                bean.bitmap.recycle();
                bean.bitmap = null;
                bean.moveLayout = null;
                staticMap.remove(tag);
            }
        }
    }


    public static Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Log.d(TAG, "drawable width:"+w+",height:"+h);
        Bitmap.Config config =
                     drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                     : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w, h, config);
        Log.d(TAG, config.toString());
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        final Rect rect = drawable.copyBounds();
        Log.d(TAG, rect.toString());
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        drawable.setBounds(rect);
        return bitmap;
    }

    private static Bitmap createBitmap(View view, int width, int height, boolean needOnLayout) {
        Bitmap bitmap = null;
        if (view != null) {
            view.clearFocus();
            view.setPressed(false);

            boolean willNotCache = view.willNotCacheDrawing();
            view.setWillNotCacheDrawing(false);

            // Reset the drawing cache background color to fully transparent
            // for the duration of this operation
            int color = view.getDrawingCacheBackgroundColor();
            view.setDrawingCacheBackgroundColor(0);
            float alpha = view.getAlpha();
            view.setAlpha(1.0f);

            if (color != 0) {
                view.destroyDrawingCache();
            }

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
            if (needOnLayout) {
                view.layout(0, 0, width, height);
            }
            view.buildDrawingCache();
            Bitmap cacheBitmap = view.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e("view.ProcessImageToBlur", "failed getViewBitmap(" + view + ")",
                        new RuntimeException());
                return null;
            }
            Log.d(TAG, cacheBitmap.getConfig().toString());
            bitmap = Bitmap.createBitmap(cacheBitmap);
            // Restore the view
            view.setAlpha(alpha);
            view.destroyDrawingCache();
            view.setWillNotCacheDrawing(willNotCache);
            view.setDrawingCacheBackgroundColor(color);
        }
        return bitmap;
    }
}
