package club.eslcc.bigsciencequiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.List;

import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;

class AnswerAdapter extends BaseAdapter
{
    private final LayoutInflater mTemplateInflater;
    private final List<QuestionOuterClass.Question.Answer> mAnswers;
    private int mSelection;
    private boolean mEnabled;

    AnswerAdapter(LayoutInflater templateInflater, List<QuestionOuterClass.Question.Answer> answers)
    {
        mTemplateInflater = templateInflater;
        mAnswers = answers;
        mSelection = -1;
        mEnabled = true;
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
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = mTemplateInflater.inflate(R.layout.answer, parent, false);

        final Button button = (Button) convertView.findViewById(R.id.answer_button);
        button.setText(mAnswers.get(position).getText());

        if (position == mSelection)
            convertView.setSelected(true);

        else
            convertView.setSelected(false);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return mEnabled;
    }

    void disable()
    {
        mEnabled = false;
    }
}
