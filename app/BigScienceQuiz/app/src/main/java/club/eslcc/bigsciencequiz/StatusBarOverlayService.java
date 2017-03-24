package club.eslcc.bigsciencequiz;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

public class StatusBarOverlayService extends Service
{
    private WindowManager mWindowManager;
    private StatusBarOverlayView mOverlayView;

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

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        if (intent != null)
        {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            layoutParams.gravity = Gravity.TOP;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    // this is to enable the notification to receive touch events
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    // Draws over status bar
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) (30 * getResources().getDisplayMetrics().scaledDensity);
            layoutParams.format = PixelFormat.TRANSPARENT;

            mOverlayView = new StatusBarOverlayView(this);
            mWindowManager.addView(mOverlayView, layoutParams);
        }

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