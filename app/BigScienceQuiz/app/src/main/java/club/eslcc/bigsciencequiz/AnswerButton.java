package club.eslcc.bigsciencequiz;

import android.content.Context;
import android.util.AttributeSet;

public class AnswerButton extends android.support.v7.widget.AppCompatButton
{
    private static final int[] mStates =
            {
                    R.attr.state_none,
                    R.attr.state_wrong,
                    R.attr.state_right
            };

    private int mCurrentState = 0;

    void setStateWrong()
    {
        mCurrentState = 1;
        refreshDrawableState();
    }

    void setStateRight()
    {
        mCurrentState = 2;
        refreshDrawableState();
    }

    public AnswerButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AnswerButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        int[] state = { mStates[mCurrentState] };

        mergeDrawableStates(drawableState, state);
        return drawableState;
    }
}
