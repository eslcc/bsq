package club.eslcc.bigsciencequiz;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.joshdholtz.sentry.Sentry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import club.eslcc.bigsciencequiz.proto.Appstate;
import club.eslcc.bigsciencequiz.proto.Events;
import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.proto.Rpc;

public class AppOverlayView extends RelativeLayout
{
    private Context mContext;
    private ViewFlipper mViewFlipper;
    private WebSocket mWebSocket;
    private boolean mConnected;
    private int mReconnectAttempts;

    private void runOnUiThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void changeToLayout(final int layoutId, final Runnable runnable)
    {
        runOnUiThread(new Runnable()
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

    private OnClickListener mClose = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mConnected)
            {
                mConnected = false;
                mWebSocket.disconnect();
            }

            mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
        }
    };

    private void showError(final String error)
    {
        Sentry.captureMessage(error);

        changeToLayout(R.id.error_layout, new Runnable()
        {
            @Override
            public void run()
            {
                TextView text = (TextView) findViewById(R.id.error_text);
                TextView sadFace = (TextView) findViewById(R.id.sad_face);
                text.setText(error);
                sadFace.setOnClickListener(mClose);
            }
        });
    }

    private void showError(final int errorResource)
    {
        Sentry.captureMessage(mContext.getString(errorResource));

        changeToLayout(R.id.error_layout, new Runnable()
        {
            @Override
            public void run()
            {
                TextView text = (TextView) findViewById(R.id.error_text);
                TextView sadFace = (TextView) findViewById(R.id.sad_face);
                text.setText(errorResource);
                sadFace.setOnClickListener(mClose);
            }
        });
    }

    private void showError(final Throwable e)
    {
        e.printStackTrace();
        Sentry.captureException(e);

        changeToLayout(R.id.error_layout, new Runnable()
        {
            @Override
            public void run()
            {
                TextView text = (TextView) findViewById(R.id.error_text);
                TextView sadFace = (TextView) findViewById(R.id.sad_face);
                text.setText(e.getLocalizedMessage());
                sadFace.setOnClickListener(mClose);
            }
        });
    }

    private void closeDialog(final AlertDialog alertToDismiss)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("Need organiser authorisation");
        builder.setMessage("Please enter pin to close the app");
        builder.setCancelable(true);

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", null);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        final AlertDialog alert = builder.create();

        if (alert.getWindow() != null)
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        alert.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(final DialogInterface dialog)
            {
                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);

                b.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (!input.getText().toString().equals("424974"))
                            Toast.makeText(mContext, "Wrong pin", Toast.LENGTH_SHORT).show();

                        else
                        {
                            mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
                            alert.dismiss();

                            if (alertToDismiss != null)
                                alertToDismiss.dismiss();
                        }
                    }
                });
            }
        });

        alert.show();
    }

    private void reconnectDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("Failed to connect");
        builder.setMessage("Would you like to try reconnecting?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mReconnectAttempts = 0;
                reconnect();
            }
        });

        builder.setNegativeButton("No, close app", null);

        final AlertDialog alert = builder.create();

        if (alert.getWindow() != null)
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        alert.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                Button b = alert.getButton(AlertDialog.BUTTON_NEGATIVE);

                b.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        closeDialog(alert);
                    }
                });
            }
        });

        alert.show();
    }

    private void reconnect()
    {
        if (mConnected)
        {
            mWebSocket.disconnect();

            final ProgressDialog waitDialog = new ProgressDialog(mContext);
            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitDialog.setIndeterminate(true);
            waitDialog.setMessage("Please wait while reconnecting to server");
            waitDialog.setCancelable(false);

            if (waitDialog.getWindow() != null)
                waitDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            waitDialog.show();
            int min = 3000;
            int max = 6000;

            Random r = new Random();
            int randomDelay = r.nextInt(max - min + 1) + min;

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        mWebSocket.recreate().connectAsynchronously();
                    } catch (IOException e)
                    {
                        showError(e);
                    }
                    waitDialog.dismiss();
                }
            }, randomDelay);
        }
    }

    void attemptReconnecting()
    {
        mReconnectAttempts++;

        if (mReconnectAttempts != 5)
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    reconnect();
                }
            });

        else
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    reconnectDialog();
                }
            });
    }

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

    @SuppressLint("HardwareIds")
    private void sendIdentifyUserRequest()
    {
        Rpc.RpcRequest.Builder builder = Rpc.RpcRequest.newBuilder();
        Rpc.IdentifyUserRequest.Builder requestBuilder = Rpc.IdentifyUserRequest.newBuilder();

        requestBuilder.setDeviceId(Settings.Secure.getString(
                getContext().getContentResolver(), Settings.Secure.ANDROID_ID));

        builder.setIdentifyUserRequest(requestBuilder);

        byte[] data = builder.build().toByteArray();
        mWebSocket.sendBinary(data);
    }

    private void sendTeamReadyRequest(String name)
    {
        //TODO Filter names maybe
        Rpc.RpcRequest.Builder wrapperBuilder = Rpc.RpcRequest.newBuilder();
        Rpc.TeamReadyRequest.Builder builder = Rpc.TeamReadyRequest.newBuilder();
        builder.setTeamName(name);
        wrapperBuilder.setTeamReadyRequest(builder);
        byte[] data = wrapperBuilder.build().toByteArray();
        mWebSocket.sendBinary(data);
    }

    private void sendAnswerQuestionRequest(int answerId)
    {
        Rpc.RpcRequest.Builder wrapperBuilder = Rpc.RpcRequest.newBuilder();
        Rpc.AnswerQuestionRequest.Builder builder = Rpc.AnswerQuestionRequest.newBuilder();
        builder.setAnswerId(answerId);
        wrapperBuilder.setAnswerQuestionRequest(builder);
        byte[] data = wrapperBuilder.build().toByteArray();
        mWebSocket.sendBinary(data);
    }

    private void sendAppStateRequest()
    {
        Rpc.RpcRequest.Builder builder = Rpc.RpcRequest.newBuilder();
        Rpc.GetAppStateRequest.Builder requestBuilder = Rpc.GetAppStateRequest.newBuilder();
        builder.setGetAppStateRequest(requestBuilder);
        byte[] data = builder.build().toByteArray();
        mWebSocket.sendBinary(data);
    }

    public void setup()
    {
        mViewFlipper = (ViewFlipper) findViewById(R.id.current_view);
        mConnected = false;
        mReconnectAttempts = 0;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting())
            showError(R.string.no_internet);

        else
        {
            changeToLayout(R.id.connect_server_layout, new Runnable()
            {
                @Override
                public void run()
                {
                    final TextInputEditText serverIP = (TextInputEditText) findViewById(R.id.server_ip);
                    final AppCompatButton startButton = (AppCompatButton) findViewById(R.id.start_button);
                    final AppCompatButton closeButton = (AppCompatButton) findViewById(R.id.close_button);

                    startButton.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            connectToServer(serverIP.getText().toString());
                        }
                    });

                    closeButton.setOnClickListener(mClose);
                }
            });
        }
    }

    private void connectToServer(String address)
    {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setConnectionTimeout(5000);

        try
        {
            mWebSocket = factory.createSocket("ws://" + address + "/socket");
        } catch (IllegalArgumentException e)
        {
            Toast.makeText(mContext, "Bad server IP", Toast.LENGTH_LONG).show();
            return;
        } catch (IOException e)
        {
            showError(e);
            return;
        }

        mWebSocket.addListener(new WebSocketAdapter()
        {
            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
            {
                super.onUnexpectedError(websocket, cause);
                showError(cause);
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception
            {
                super.onConnected(websocket, headers);

                mReconnectAttempts = 0;
                mConnected = true;
                sendAppStateRequest();
            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception
            {
                super.onConnectError(websocket, exception);
                mConnected = false;
                Sentry.captureException(exception, exception.getLocalizedMessage());
                attemptReconnecting();
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception
            {
                super.onBinaryMessage(websocket, binary);
                onReceivedMessage(binary);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception
            {
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);

                if (mConnected)
                {
                    mConnected = false;
                    Sentry.captureMessage("Client disconnected when it shouldn't have, reconnecting");
                    attemptReconnecting();
                }
            }
        });

        mWebSocket.connectAsynchronously();
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
                Sentry.captureException(e, e.getLocalizedMessage());
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
                Sentry.captureException(e, e.getLocalizedMessage());
            }
        }
    }

    private void onReceivedGameEvent(Events.GameEvent event)
    {
        switch (event.getEventCase())
        {
            case GAMESTATECHANGEEVENT:
                switch (event.getGameStateChangeEvent().getNewState().getState())
                {
                    case QUESTION_LIVEANSWERS:
                    case QUESTION_ANSWERS_REVEALED:
                        break;

                    case QUESTION_ANSWERING:
                        handleQuestionEvent(event.getGameStateChangeEvent().getNewState().getCurrentQuestion(), false);
                        break;

                    case QUESTION_CLOSED:
                        handleQuestionEvent(event.getGameStateChangeEvent().getNewState().getCurrentQuestion(), true);
                        break;

                    case READY:
                        changeToLayout(R.id.waiting_layout, null);
                        break;

                    default:
                        Sentry.addBreadcrumb("Unknown NewGameState content", String.valueOf(event.getGameStateChangeEvent().getNewState()));
                        Sentry.addBreadcrumb("Unknown NewGameState binary", Arrays.toString(event.getGameStateChangeEvent().getNewState().toByteArray()));
                        showError("Unknown NewGameState of type " + event.getGameStateChangeEvent().getNewState().getState());
                        break;
                }
                break;

            case REVEALANSWERSEVENT:
                Events.RevealAnswersEvent rae = event.getRevealAnswersEvent();
                handleAnswersRevealed(rae.getCurrentQuestion(), rae.getUserAnswer(), rae.getCorrectAnswer());
                break;

            case REMOTESHUTDOWNEVENT:
                if (mConnected)
                    mWebSocket.disconnect();

                mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
                break;

            case RECONNECTEVENT:
                attemptReconnecting();
                break;

            default:
                Sentry.addBreadcrumb("Unknown GameEvent content", String.valueOf(event));
                Sentry.addBreadcrumb("Unknown GameEvent binary", Arrays.toString(event.toByteArray()));
                showError("Unknown GameEvent of type " + event.getEventCase());
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

            case GETGAMESTATERESPONSE:
                break;

            case GETAPPSTATERESPONSE:
                handleAppStateResponse(response.getGetAppStateResponse());
                break;

            default:
                Sentry.addBreadcrumb("Unknown RPCResponse content", String.valueOf(response));
                Sentry.addBreadcrumb("Unknown RPCResponse binary", Arrays.toString(response.toByteArray()));
                showError("Unknown RPCResponse of type " + response.getResponseCase());
                break;
        }
    }

    private void handleIdentifyUserResponse(final Rpc.IdentifyUserResponse response)
    {
        if (!response.hasTeam())
        {
            showError(response.getFailureReason().toString());
        }

        else
        {
            Sentry.init(mContext, response.getSentryDsn(), true);

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
                                Toast.makeText(mContext, "Please enter a team name", Toast.LENGTH_LONG).show();

                            else
                                sendTeamReadyRequest(name);
                        }
                    });

                    // TODO REMOVE
                    //throw(new RuntimeException("haha"));
                }
            });
        }
    }

    private void handleQuestionEvent(final QuestionOuterClass.Question question, final boolean locked)
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
                confirmAnswerHint.setVisibility(INVISIBLE);

                final AnswerAdapter adapter = new AnswerAdapter(
                        LayoutInflater.from(mContext),
                        question.getAnswersList());

                answers.setAdapter(adapter);

                if (locked)
                    adapter.disable();

                else
                {
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
                                sendAnswerQuestionRequest(adapter.getAnswerId(position));
                            }
                        }
                    });
                }
            }
        });
    }

    private void handleAnswerQuestionResponse(Rpc.AnswerQuestionResponse response)
    {
        if (response.getFailureReason() != Rpc.AnswerQuestionResponse.FailureReason.NONE)
        {
            final TextView confirmAnswerHint = (TextView) findViewById(R.id.confirm_answer_hint);

            switch(response.getFailureReason())
            {
                case INVALID_STATE:
                    confirmAnswerHint.setText(R.string.question_closed);
                    break;

                case ALREADY_ANSWERED:
                    confirmAnswerHint.setText(R.string.already_answered);
                    break;

                default:
                    showError(response.getFailureReason().toString());
                    break;
            }
        }
    }

    private void handleAnswersRevealed(final QuestionOuterClass.Question question, final int userAnswer, final int correctAnswer)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final ListView answersListView = (ListView) findViewById(R.id.question_answers);
                final TextView confirmAnswerHint = (TextView) findViewById(R.id.confirm_answer_hint);
                View currentAnswerView = null;
                View correctAnswerView = null;

                boolean currentAnswerIsCorrect = false;

                if (userAnswer == correctAnswer)
                    currentAnswerIsCorrect = true;

                List<QuestionOuterClass.Question.Answer> answersList = question.getAnswersList();

                for (int i = 0; i < answersList.size(); ++i)
                {
                    if (userAnswer == answersList.get(i).getId())
                        currentAnswerView = answersListView.getChildAt(i);

                    if (correctAnswer == answersList.get(i).getId())
                        correctAnswerView = answersListView.getChildAt(i);
                }

                if (correctAnswer == 0 || correctAnswerView == null)
                    showError("Question had no correct answer?");

                else
                {
                    if (userAnswer == 0 || currentAnswerView == null)
                    {
                        ((AnswerAdapter) answersListView.getAdapter()).disable();
                        ((AnswerButton) correctAnswerView.findViewById(R.id.answer_button)).setStateWrong();
                        confirmAnswerHint.setText(R.string.not_answered);
                        confirmAnswerHint.setVisibility(VISIBLE);
                    }

                    else if (currentAnswerIsCorrect)
                    {
                        ((AnswerButton) currentAnswerView.findViewById(R.id.answer_button)).setStateRight();
                        confirmAnswerHint.setText(R.string.correct_answer_hint);
                    }

                    else
                    {
                        ((AnswerButton) currentAnswerView.findViewById(R.id.answer_button)).setStateWrong();
                        ((AnswerButton) correctAnswerView.findViewById(R.id.answer_button)).setStateRight();
                        confirmAnswerHint.setText(R.string.wrong_answer_hint);
                    }
                }
            }
        });
    }

    private void handleAppStateResponse(final Rpc.GetAppStateResponse response)
    {

        Appstate.AppState appState = response.getAppState();

        if (!appState.hasGameState())
            showError("Got no game state from server");

        else if (!appState.hasTeam())
            sendIdentifyUserRequest();

        else
        {
            switch (appState.getGameState().getState())
            {
                case NOTREADY:
                case INTRO:
                case READY:
                case STARTING:
                    changeToLayout(R.id.waiting_layout, null);
                    break;

                case QUESTION_ANSWERING:
                case QUESTION_LIVEANSWERS:
                    handleQuestionEvent(appState.getGameState().getCurrentQuestion(), false);
                    break;

                case QUESTION_CLOSED:
                    handleQuestionEvent(appState.getGameState().getCurrentQuestion(), true);
                    break;

                case QUESTION_ANSWERS_REVEALED:
                    handleQuestionEvent(appState.getGameState().getCurrentQuestion(), true);
                    handleAnswersRevealed(appState.getGameState().getCurrentQuestion(),
                            appState.getUserAnswer(),
                            appState.getCorrectAnswer());
                    break;

                case LEADERBOARD:
                    break;

                default:
                    showError("Unknown game state" + appState.getGameState().getState().toString());
            }
        }
    }
}
