package club.eslcc.bigsciencequiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;

class AnswerAdapter extends BaseAdapter
{
    private Context mContext;
    private int mRendererId;
    private final int mFocusColor;
    private List<QuestionOuterClass.Question.Answer> mAnswers;

    AnswerAdapter(Context context, int rendererId, int focusColor, List<QuestionOuterClass.Question.Answer> answers)
    {
        mContext = context;
        mRendererId = rendererId;
        mFocusColor = focusColor;
        mAnswers = answers;
    }

    @Override
    public int getCount()
    {
        return mAnswers != null ? mAnswers.size() : 0;
    }

    @Override
    public QuestionOuterClass.Question.Answer getItem(int position)
    {
        return mAnswers != null ? mAnswers.get(position) : null;
    }

    @Override
    public long getItemId(int position)
    {
        return mAnswers != null ? mAnswers.get(position).getId() : -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(mRendererId, null);

        final TextView confirmText = (TextView) convertView.findViewById(R.id.confirm_text);
        Button button = (Button) convertView.findViewById(R.id.answer_button);
        button.setText(mAnswers.get(position).getText());

        button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.setBackgroundColor(mFocusColor);
                confirmText.setVisibility(View.VISIBLE);
                return false;
            }
        });

        return convertView;
    }
}
