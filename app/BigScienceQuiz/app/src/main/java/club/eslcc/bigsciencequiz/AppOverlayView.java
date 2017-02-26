package club.eslcc.bigsciencequiz;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

class AppOverlayView extends RelativeLayout
{
    private Context mContext;
    private Button mExitButton;

    public AppOverlayView(Context context)
    {
        super(context);
        mContext = context;
    }

    public AppOverlayView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }

    public AppOverlayView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setup()
    {
//        mExitButton = (Button) findViewById(R.id.exit_button);
//
//        mExitButton.setOnClickListener(new OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
//            }
//        });
    }
}
