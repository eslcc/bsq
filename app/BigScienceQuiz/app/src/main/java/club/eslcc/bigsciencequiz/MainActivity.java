package club.eslcc.bigsciencequiz;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    public static MainActivity mInstance;
    public boolean mExit;

    private static final int PENDING_INTENT_ID = 123456;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private PowerManager.WakeLock mWakeLock;
    private ScreenOffReceiver mScreenOffReceiver;
    private ExitReceiver mExitReceiver;
    private Intent mAppOverlayIntent;
    private Intent mStatusBarOverlayIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mInstance = this;
        mExit = false;

        // make app relaunch after crash
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(this,
                PENDING_INTENT_ID, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this, restartIntent));

        // register screen off receiver
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mScreenOffReceiver = new ScreenOffReceiver();
        registerReceiver(mScreenOffReceiver, filter);

        // register exit receiver
        final IntentFilter filter2 = new IntentFilter(getString(R.string.exit_intent));
        mExitReceiver = new ExitReceiver();
        registerReceiver(mExitReceiver, filter2);

        // disable screen lock
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        getOverlayPermission();
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(mScreenOffReceiver);
        unregisterReceiver(mExitReceiver);
        stopService(mAppOverlayIntent);
        stopService(mStatusBarOverlayIntent);

        if (!mExit)
        {
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent restartIntent = PendingIntent.getActivity(this,
                    PENDING_INTENT_ID, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        hideSystemUI();
    }

    // Close system dialogs (for long power button press)
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus)
        {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    // Disable volume buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        switch (event.getKeyCode())
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }

    // Disable back and other buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return true;
    }

    // Retry to draw overlay, with permission
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE)
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            {
                if (!Settings.canDrawOverlays(this))
                {
                    Toast.makeText(this, "Pls gib permission", Toast.LENGTH_LONG).show();

                    Intent launchIntent = new Intent(this, MainActivity.class);
                    PendingIntent restartIntent = PendingIntent.getActivity(this,
                            PENDING_INTENT_ID, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent);
                    finishAffinity();
                    Runtime.getRuntime().exit(0);
                }

                else
                    drawOverlays();
            }

            else
                drawOverlays();
        }
    }

    private void drawOverlays()
    {
        mStatusBarOverlayIntent = new Intent(this, StatusBarOverlayService.class);
        startService(mStatusBarOverlayIntent);

        mAppOverlayIntent = new Intent(getApplicationContext(), AppOverlayService.class);
        startService(mAppOverlayIntent);
    }

    private void getOverlayPermission()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            if (!Settings.canDrawOverlays(this))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }

            else
                drawOverlays();
        }

        else
            drawOverlays();
    }

    // Obtain wakelock to wake up the screen if it's turned off
    public PowerManager.WakeLock getWakeLock()
    {
        if (mWakeLock == null)
        {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "wakeup");
        }

        return mWakeLock;
    }

    // Hide as much as possible of the system UI
    private void hideSystemUI()
    {
        final View decorView = getWindow().getDecorView();

        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        flags |= View.GONE;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        final int uiOptions = flags;

        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                    }
                });
    }

    // If app crashes, it will restart
    private class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        private Thread.UncaughtExceptionHandler mDefaultUEH;
        private Context mContext;
        private PendingIntent mPendingIntent;

        CustomExceptionHandler(Context context, PendingIntent intent)
        {
            mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            mContext = context;
            mPendingIntent = intent;
        }

        public void uncaughtException(Thread thread, Throwable ex)
        {
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            mDefaultUEH.uncaughtException(thread, ex);
            Runtime.getRuntime().exit(2);
        }
    }
}
