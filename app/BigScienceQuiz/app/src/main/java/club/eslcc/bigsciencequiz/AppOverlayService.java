package club.eslcc.bigsciencequiz;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import club.eslcc.bigsciencequiz.proto.Rpc;

public class AppOverlayService extends Service
{
    private WindowManager mWindowManager;
    private AppOverlayView mOverlayView;
    private WebSocket mWebSocket;

    @Override
    public IBinder onBind(Intent intent)
    {
        // Not used
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        setTheme(R.style.AppTheme);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        int windowFlags = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        //| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        //| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        //| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            windowFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                windowFlags,
                PixelFormat.TRANSLUCENT);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout dummyRoot = new RelativeLayout(this);
        mOverlayView = (AppOverlayView) inflater.inflate(R.layout.overlay_app, dummyRoot, false);
        mOverlayView.setup();
        mWindowManager.addView(mOverlayView, windowParams);

        WebSocketFactory factory = new WebSocketFactory();
        factory.setConnectionTimeout(10000);

        try
        {
            //TODO: replace with server IP
            mWebSocket = factory.createSocket("ws://192.168.177.71:8080/socket");
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
                    System.out.println(Arrays.toString(binary));
                }
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);

        if (mWebSocket != null)
            mWebSocket.disconnect();
    }
}
