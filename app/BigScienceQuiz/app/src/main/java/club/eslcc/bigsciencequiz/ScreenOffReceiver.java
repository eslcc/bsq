package club.eslcc.bigsciencequiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ScreenOffReceiver extends BroadcastReceiver
{
    public ScreenOffReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        MainActivity activityContext = (MainActivity) context;
        turnScreenOn(activityContext);
    }

    private void turnScreenOn(MainActivity activityContext)
    {
        PowerManager.WakeLock wakeLock = activityContext.getWakeLock();

        if (wakeLock.isHeld())
            wakeLock.release(); // release old wake lock

        // create a new wake lock...
        wakeLock.acquire();

        // ... and release again
        wakeLock.release();
    }
}