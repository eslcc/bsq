package club.eslcc.bigsciencequiz;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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
    private Button mExitButton;
    private WebSocket mWebSocket;

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
        final AppOverlayView ctx = this;

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
                    Rpc.RpcRequest.Builder builder = Rpc.RpcRequest.newBuilder();
                    Rpc.GetGameStateRequest ggsR = Rpc.GetGameStateRequest.newBuilder().build();
                    builder.setGetGameStateRequest(ggsR);
                    byte[] data = builder.build().toByteArray();

                    mWebSocket.sendBinary(data);
                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception
                {
                    super.onBinaryMessage(websocket, binary);

                    Events.GameEvent event = Events.GameEvent.parseFrom(binary);

                    if (event.getEventCase() != Events.GameEvent.EventCase.EVENT_NOT_SET)
                    {
                        System.out.println("Got GameEventThing");
                    }

                    else
                    {
                        Rpc.RpcResponse response = Rpc.RpcResponse.parseFrom(binary);
                        System.out.println("Got RPC Response Thing");

                        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                        View newLayout = layoutInflater.inflate(R.layout.team_select, ctx, false);
                        
                        removeView(findViewById(R.id.current_layout));
                        newLayout.setId(R.id.current_layout);
                        addView(newLayout);

                        FloatingActionButton fab = (FloatingActionButton) newLayout.findViewById(R.id.team_name_fab);

                        fab.setOnClickListener(new OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                mWebSocket.disconnect();
                                mContext.sendBroadcast(new Intent(mContext.getString(R.string.exit_intent)));
                            }
                        });
                    }
                }
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
