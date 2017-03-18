package club.eslcc.bigsciencequiz;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class AppOverlayService extends Service
{
    private WindowManager mWindowManager;
    private AppOverlayView mOverlayView;

    @Override
    public IBinder onBind(Intent intent)
    {
        // Not used
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        setTheme(R.style.AppTheme);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        int windowFlags = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        //| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        //| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        //| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            windowFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                windowFlags,
                PixelFormat.TRANSLUCENT);

        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout dummyRoot = new RelativeLayout(this);
        mOverlayView = (AppOverlayView) inflater.inflate(R.layout.overlay_app, dummyRoot, false);
        mOverlayView.setup();
        mWindowManager.addView(mOverlayView, windowParams);

        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
    }
}
