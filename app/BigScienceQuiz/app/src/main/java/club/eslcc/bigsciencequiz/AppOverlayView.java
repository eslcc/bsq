package club.eslcc.bigsciencequiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import club.eslcc.bigsciencequiz.proto.Events;
import club.eslcc.bigsciencequiz.proto.Gamestate;
import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.proto.Rpc;

class AppOverlayView extends RelativeLayout
{
    private Context mContext;
    private WebSocket mWebSocket;
    private ViewFlipper mViewFlipper;
    private Gamestate.GameState mLastGameState;

    private void changeToLayout(final int layoutId, final Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(findViewById(layoutId)));

                if (runnable != null)
                    runnable.run();
            }
        });
    }

    private void showError(final CharSequence error, final OnClickListener listener)
    {
        changeToLayout(R.id.error_layout, new Runnable()
        {
            @Override
            public void run()
            {
                TextView text = (TextView) findViewById(R.id.error_text);
                TextView sadFace = (TextView) findViewById(R.id.sad_face);
                text.setText(error);
                sadFace.setOnClickListener(listener);
            }
        });
    }

    private void showError(final int errorResource, final OnClickListener listener)
    {
        changeToLayout(R.id.error_layout, new Runnable()
        {
            @Override
            public void run()
            {
                TextView text = (TextView) findViewById(R.id.error_text);
                TextView sadFace = (TextView) findViewById(R.id.sad_face);
                text.setText(errorResource);
                sadFace.setOnClickListener(listener);
            }
        });
    }

    private OnClickListener mClose = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
        }
    };

    private OnClickListener mDisconnectAndClose = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mWebSocket.disconnect();
            mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
        }
    };

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
        mViewFlipper = (ViewFlipper) findViewById(R.id.current_view);

        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting())
            showError(R.string.no_internet, mClose);

        else
        {
            changeToLayout(R.id.connect_server_layout, new Runnable()
            {
                @Override
                public void run()
                {
                    final TextInputEditText serverIP = (TextInputEditText) findViewById(R.id.server_ip);
                    final Button startButton = (Button) findViewById(R.id.start_button);

                    startButton.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            connectToServer(serverIP.getText().toString());
                        }
                    });
                }
            });
        }
    }

    private void connectToServer(String address)
    {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setConnectionTimeout(3000);

        try
        {
            try
            {
                mWebSocket = factory.createSocket("ws://" + address + "/socket");
            }
            catch(IllegalArgumentException e)
            {
                Toast.makeText(mContext, "Bad server IP", Toast.LENGTH_LONG).show();
                return;
            }

            mWebSocket.connectAsynchronously();

            mWebSocket.addListener(new WebSocketAdapter()
            {
                @Override
                public void onConnectError(WebSocket websocket, final WebSocketException exception) throws Exception
                {
                    super.onConnectError(websocket, exception);
                    showError(exception.getLocalizedMessage(), mClose);
                    exception.printStackTrace();
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception
                {
                    super.onConnected(websocket, headers);
                    onConnectToServer();
                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception
                {
                    super.onBinaryMessage(websocket, binary);
                    onReceivedMessage(binary);
                }

                @Override
                public void onError(WebSocket websocket, final WebSocketException cause) throws Exception
                {
                    super.onError(websocket, cause);
                    showError(cause.getLocalizedMessage(), mDisconnectAndClose);
                    cause.printStackTrace();
                }
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressLint("HardwareIds")
    private void onConnectToServer()
    {
        Rpc.RpcRequest.Builder builder = Rpc.RpcRequest.newBuilder();
        Rpc.IdentifyUserRequest.Builder requestBuilder = Rpc.IdentifyUserRequest.newBuilder();

        requestBuilder.setDeviceId(Settings.Secure.getString(
                getContext().getContentResolver(), Settings.Secure.ANDROID_ID));

        builder.setIdentifyUserRequest(requestBuilder);

        byte[] data = builder.build().toByteArray();
        mWebSocket.sendBinary(data);
    }

    private void onReceivedMessage(byte[] binary)
    {
        if (binary[0] == (byte) 0xff
                && binary[1] == (byte) 0xff
                && binary[2] == (byte) 0xff
                && binary[3] == (byte) 0xff)
        {
            try
            {
                byte[] newBinary = Arrays.copyOfRange(binary, 4, binary.length);
                onReceivedGameEvent(Events.GameEvent.parseFrom(newBinary));
            } catch (InvalidProtocolBufferException e)
            {
                e.printStackTrace();
            }
        }

        else
        {
            try
            {
                onReceivedRpcResponse(Rpc.RpcResponse.parseFrom(binary));
            } catch (InvalidProtocolBufferException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void onReceivedGameEvent(Events.GameEvent event)
    {
        switch (event.getEventCase())
        {
            case GAMESTATECHANGEEVENT:
                mLastGameState = event.getGameStateChangeEvent().getNewState();

                switch (mLastGameState.getState())
                {
                    case QUESTION_LIVEANSWERS:
                    case QUESTION_CLOSED:
                        break;

                    case QUESTION_ANSWERS_REVEALED:
                        handleAnswersRevealed(mLastGameState);
                        break;

                    case QUESTION_ANSWERING:
                        handleQuestionEvent(mLastGameState.getCurrentQuestion());
                        break;

                    case READY:
                        changeToLayout(R.id.waiting_layout, null);
                        break;

                    default:
                        showError("Unknown NewGameState of type " + event.getGameStateChangeEvent().getNewState().getState(), mDisconnectAndClose);
                        System.out.println("Got NewGameState of type " + event.getGameStateChangeEvent().getNewState().getState());
                        System.out.println("Content: " + event.getGameStateChangeEvent().getNewState());
                        System.out.println("Binary: " + Arrays.toString(event.getGameStateChangeEvent().getNewState().toByteArray()));
                        break;
                }
                break;

            default:
                showError("Unknown Game Event of type " + event.getEventCase(), mDisconnectAndClose);
                System.out.println("Got GameEventThing of type " + event.getEventCase());
                System.out.println("Content: " + event);
                System.out.println("Binary: " + Arrays.toString(event.toByteArray()));
                break;
        }
    }

    private void onReceivedRpcResponse(final Rpc.RpcResponse response)
    {
        switch (response.getResponseCase())
        {
            case IDENTIFYUSERRESPONSE:
                handleIdentifyUserResponse(response.getIdentifyUserResponse());
                break;

            case TEAMREADYRESPONSE:
                changeToLayout(R.id.waiting_layout, null);
                break;

            case ANSWERQUESTIONRESPONSE:
                handleAnswerQuestionResponse(response.getAnswerQuestionResponse());
                break;

            default:
                showError("Unknown RPC Response of type " + response.getResponseCase(), mDisconnectAndClose);
                System.out.println("Got RpcResponseThing of type " + response.getResponseCase());
                System.out.println("Content: " + response);
                System.out.println("Binary: " + Arrays.toString(response.toByteArray()));
                break;
        }
    }

    private void handleIdentifyUserResponse(final Rpc.IdentifyUserResponse response)
    {
        if (!response.hasTeam())
            showError(response.getFailureReason().toString(), mDisconnectAndClose);

        else
        {
            changeToLayout(R.id.team_select_layout, new Runnable()
            {
                @Override
                public void run()
                {
                    final List<String> names = response.getTeam().getMemberNamesList();
                    final ArrayList<String> namesArray = new ArrayList<>(names.size());
                    namesArray.addAll(names);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                            mContext, android.R.layout.simple_list_item_1, namesArray);

                    final TextView teamNumber = (TextView) findViewById(R.id.team_number);
                    final ListView teamMembers = (ListView) findViewById(R.id.team_members);
                    final TextInputEditText teamName = (TextInputEditText) findViewById(R.id.team_name);
                    final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.team_name_fab);

                    teamNumber.setText(response.getTeam().getNumber());
                    teamMembers.setAdapter(arrayAdapter);

                    fab.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String name = teamName.getText().toString().trim();

                            if (name.isEmpty())
                            {
                                Toast.makeText(mContext, "Please enter a team name", Toast.LENGTH_LONG).show();
                                return;
                            }

                            //TODO Filter names maybe

                            Rpc.RpcRequest.Builder wrapperBuilder = Rpc.RpcRequest.newBuilder();
                            Rpc.TeamReadyRequest.Builder builder = Rpc.TeamReadyRequest.newBuilder();
                            builder.setTeamName(name);
                            wrapperBuilder.setTeamReadyRequest(builder);
                            byte[] data = wrapperBuilder.build().toByteArray();
                            mWebSocket.sendBinary(data);
                        }
                    });
                }
            });
        }
    }

    private void handleQuestionEvent(final QuestionOuterClass.Question question)
    {
        changeToLayout(R.id.question_layout, new Runnable()
        {
            @Override
            public void run()
            {
                final TextView category = (TextView) findViewById(R.id.question_category);
                final TextView questionText = (TextView) findViewById(R.id.question);
                final TextView confirmAnswerHint = (TextView) findViewById(R.id.confirm_answer_hint);
                final ListView answers = (ListView) findViewById(R.id.question_answers);

                category.setText(question.getCategory());
                questionText.setText(question.getQuestion());

                final AnswerAdapter adapter = new AnswerAdapter(
                        LayoutInflater.from(mContext),
                        question.getAnswersList());

                answers.setAdapter(adapter);

                answers.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    private int lastSelectionView = -1;

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        if (lastSelectionView == -1)
                        {
                            confirmAnswerHint.setText(R.string.press_to_confirm_answer);
                            confirmAnswerHint.setVisibility(VISIBLE);
                            view.findViewById(R.id.answer_button).setSelected(true);
                            lastSelectionView = position;
                        }

                        else if (lastSelectionView != position)
                        {
                            parent.getChildAt(lastSelectionView).findViewById(R.id.answer_button).setSelected(false);
                            view.findViewById(R.id.answer_button).setSelected(true);
                            lastSelectionView = position;
                        }

                        else
                        {
                            confirmAnswerHint.setText(R.string.confirmed_answer);

                            adapter.disable();

                            Rpc.RpcRequest.Builder wrapperBuilder = Rpc.RpcRequest.newBuilder();
                            Rpc.AnswerQuestionRequest.Builder builder = Rpc.AnswerQuestionRequest.newBuilder();
                            builder.setAnswerId(adapter.getAnswerId(position));
                            wrapperBuilder.setAnswerQuestionRequest(builder);
                            byte[] data = wrapperBuilder.build().toByteArray();
                            mWebSocket.sendBinary(data);
                        }
                    }
                });
            }
        });
    }

    private void handleAnswerQuestionResponse(Rpc.AnswerQuestionResponse response)
    {
        if (response.getFailureReason() != Rpc.AnswerQuestionResponse.AnswerQuestionFailedReason.SUCCESS)
        {
            final TextView confirmAnswerHint = (TextView) findViewById(R.id.confirm_answer_hint);

            switch(response.getFailureReason())
            {
                case INVALID_STATE:
                    confirmAnswerHint.setText(R.string.question_closed);
                    break;

                case ALREADY_ANSWERED:
                    confirmAnswerHint.setText(R.string.already_answered);
                    handleAnswersRevealed(mLastGameState);
                    break;

                default:
                    showError(response.getFailureReason().toString(), mDisconnectAndClose);
                    break;
            }
        }
    }

    private void handleAnswersRevealed(final Gamestate.GameState gameState)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                View currentAnswer = null;
                View correctAnswer = null;
                boolean currentAnswerIsCorrect = false;

                int currentAnswerId = gameState.getMyCurrentQuestionAnswer();
                int correctAnswerId = -1;

                List<QuestionOuterClass.Question.Answer> answersList =
                        gameState.getCurrentQuestion().getAnswersList();

                final ListView answersListView = (ListView) findViewById(R.id.question_answers);
                final TextView confirmAnswerHint = (TextView) findViewById(R.id.confirm_answer_hint);

                for (int i = 0; i < answersList.size(); ++i)
                {
                    if (answersList.get(i).getCorrect())
                    {
                        correctAnswerId = answersList.get(i).getId();

                        if (currentAnswerId == correctAnswerId)
                            currentAnswerIsCorrect = true;

                        else
                            correctAnswer = answersListView.getChildAt(i);
                    }

                    if (currentAnswerId == answersList.get(i).getId())
                        currentAnswer = answersListView.getChildAt(i);
                }

                if (currentAnswer == null || correctAnswerId == -1)
                    showError("Could not find current or correct answer", mDisconnectAndClose);

                else
                {
                    if (currentAnswerIsCorrect)
                    {
                        ((AnswerButton) currentAnswer.findViewById(R.id.answer_button)).setStateRight();
                        confirmAnswerHint.setText(R.string.correct_answer_hint);
                    }

                    else
                    {
                        ((AnswerButton) currentAnswer.findViewById(R.id.answer_button)).setStateWrong();
                        ((AnswerButton) correctAnswer.findViewById(R.id.answer_button)).setStateRight();
                        confirmAnswerHint.setText(R.string.wrong_answer_hint);
                    }
                }
            }
        });
    }
}
