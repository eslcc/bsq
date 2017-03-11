package club.eslcc.bigsciencequiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
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
                    throw new RuntimeException(cause);
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
        try
        {
            Events.GameEvent event = Events.GameEvent.parseFrom(binary);

            if (event.getEventCase() != Events.GameEvent.EventCase.EVENT_NOT_SET)
                onReceivedGameEvent(event);

            else
            {
                Rpc.RpcResponse response = Rpc.RpcResponse.parseFrom(binary);
                onReceivedRpcResponse(response);
            }


        } catch (InvalidProtocolBufferException | NullPointerException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void onReceivedGameEvent(Events.GameEvent event)
    {
        System.out.println("Got GameEventThing of type " + event.getEventCase());

        switch (event.getEventCase())
        {
            default:
                System.out.println("bish pls");
                break;
        }
    }

    private void onReceivedRpcResponse(Rpc.RpcResponse response)
    {
        System.out.println("Got RPC Response Thing");

        switch (response.getResponseCase())
        {
            case IDENTIFYUSERRESPONSE:
                final AppOverlayView ctx = this;

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        removeView(findViewById(R.id.current_layout));

                        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                        View newLayout = layoutInflater.inflate(R.layout.team_select, ctx, false);

                        newLayout.setId(R.id.current_layout);
                        addView(newLayout);

                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.team_name_fab);
                        fab.setOnClickListener(mCloseOnClick);
                    }
                });
                break;

            default:
                System.out.println("bish pls");
                break;
        }
    }
}
