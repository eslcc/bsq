package club.eslcc.bigsciencequiz;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class StatusBarOverlayView extends ViewGroup
{
    public StatusBarOverlayView(Context context)
    {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return true;
    }
}
