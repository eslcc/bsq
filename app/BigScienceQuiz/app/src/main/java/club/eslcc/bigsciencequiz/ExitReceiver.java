package club.eslcc.bigsciencequiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ExitReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        MainActivity activity = MainActivity.mInstance;

        activity.mExit = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            activity.finishAffinity();

        else
            activity.finish();
    }
}
