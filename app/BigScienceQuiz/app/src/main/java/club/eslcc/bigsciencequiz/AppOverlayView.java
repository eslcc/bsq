package club.eslcc.bigsciencequiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import club.eslcc.bigsciencequiz.proto.Rpc;

class AppOverlayView extends RelativeLayout
{
    private Context mContext;
    private WebSocket mWebSocket;

    private OnClickListener mCloseOnClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mWebSocket.disconnect();
            mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
        }
    };

    interface Callback
    {
        void action();
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

    public void setup()
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

    private void connectToServer(String address)
    {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setConnectionTimeout(10000);

        try
        {
            //TODO: replace with server IP
            mWebSocket = factory.createSocket("ws://" + address + "/socket");
            mWebSocket.connectAsynchronously();

            mWebSocket.addListener(new WebSocketAdapter()
            {
                @Override
                public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception
                {
                    super.onConnectError(websocket, exception);
                    System.out.println("OH NOES :((((");
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
                    onReceivedMessage(websocket, binary);
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception
                {
                    super.onError(websocket, cause);
                    Toast.makeText(mContext, cause.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

    private void onReceivedMessage(WebSocket websocket, byte[] binary)
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
                System.out.println("Game supposed to start");
                break;

            default:
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
                handleTeamReadyResponse(response.getTeamReadyResponse());
                break;

            default:
                System.out.println("Got RpcResponseThing of type " + response.getResponseCase());
                break;
        }
    }

    private void handleIdentifyUserResponse(final Rpc.IdentifyUserResponse response)
    {
        if (!response.hasTeam())
        {
            Callback callback = new Callback()
            {
                @Override
                public void action()
                {
                    TextView errorText = (TextView) findViewById(R.id.error_text);
                    TextView sadFace = (TextView) findViewById(R.id.sad_face);

                    errorText.setText(response.getFailureReason().toString());
                    sadFace.setOnClickListener(mCloseOnClick);
                }
            };

            swapLayout(R.layout.error, callback);
        }

        else
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    final TextView teamNumber = (TextView) findViewById(R.id.team_number);
                    final ImageView bsqLogo = (ImageView) findViewById(R.id.bsq_logo);
                    final TextInputLayout textLayout = (TextInputLayout) findViewById(R.id.text_layout);
                    final Button startButton = (Button) findViewById(R.id.start_button);

                    textLayout.setVisibility(INVISIBLE);
                    startButton.setVisibility(INVISIBLE);
                    teamNumber.setText(response.getTeam().getNumber());

                    bsqLogo.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            final List<String> names = response.getTeam().getMemberNamesList();
                            final ArrayList<String> namesArray = new ArrayList<>(names.size());
                            namesArray.addAll(names);

                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                    mContext, android.R.layout.simple_list_item_1, namesArray);

                            Callback callback = new Callback()
                            {
                                @Override
                                public void action()
                                {
                                    final ListView teamMembers = (ListView) findViewById(R.id.team_members);
                                    final TextInputEditText teamName = (TextInputEditText) findViewById(R.id.team_name);
                                    final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.team_name_fab);

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
                            };

                            swapLayout(R.layout.team_select, callback);
                        }
                    });
                }
            });
        }
    }

    private void handleTeamReadyResponse(Rpc.TeamReadyResponse response)
    {
        swapLayout(R.layout.wait_for_start, null);
    }

    private void swapLayout(final int layoutId, final Callback callback)
    {
        final AppOverlayView ctx = this;

        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                removeView(findViewById(R.id.current_layout));

                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                View newLayout = layoutInflater.inflate(layoutId, ctx, false);

                newLayout.setId(R.id.current_layout);
                addView(newLayout);

                if (callback != null)
                    callback.action();
            }
        });
    }
}
