package com.skynet.stream.network.socket;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.parser.Packet;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by Huy on 7/3/2017.
 */

public class SocketClient extends Service {
    IBinder mBinder;
    Socket socket;
    OnListenResponse onListenRespone;
//    SPUtils mSetting = new SPUtils(AppConstant.KEY_SETTING);
    Notification notification;
    CountDownTimer countDownTimer;
    public static final int STATE_NEWORDER = 1;
    public static final int STATE_RECEIVED = 2;
    public static final int STATE_INPROGRESS = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_CANCELED = 4;
    public static final int TYPE_NOTIFY = 0;
    public static final int TYPE_BOOKING = 1;
    public static final int TYPE_MESSAGE = 2;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.e("OnBind");
        initSocket();
        return mBinder;
    }


    public void sendMessage(String sendFrom, String idUser, String idHost, int idPost, String content, int type) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sendFrom", sendFrom);
            jsonObject.put("idUser", idUser);
            jsonObject.put("idHost", idHost);
            jsonObject.put("idPost", idPost);
            jsonObject.put("content", content);
            jsonObject.put("type", type);
            socket.emit("tn_chat", jsonObject);

            LogUtils.e("send socket chat to " + sendFrom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendViewPost(int idPost, String idUser, String sendFrom, String idHost) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idUser", idUser);
            jsonObject.put("sendFrom", sendFrom);
            jsonObject.put("idHost", idHost);
            jsonObject.put("idPost", idPost);
            jsonObject.put("type", 3);
            socket.emit("view_post", jsonObject);
            LogUtils.e("sendViewPost to " + idHost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnListenResponse {
        void onResponse(String str);
    }

    public class LocalBinder extends Binder {
        public SocketClient getServerInstance() {
            return SocketClient.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("OnDestroy");
        if (socket != null)
            this.socket.disconnect();
        Intent broadcastIntent = new Intent("chayngamT.restart");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("OnCreate");

        mBinder = new LocalBinder();

        initSocket();
    }


    public void initSocket() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            //   SSLContext sc = SSLContext.getInstance("TLS");
            //   sc.init(null, trustManagers, null);
            //   IO.setDefaultSSLContext(sc);
            //   IO.Options os = new IO.Options();
            //  os.secure = true;
            //   os.reconnection = true;
            //  os.reconnectionDelay = 2000;
            //  os.reconnectionAttempts = 5;
            // os.sslContext = sc;
            socket = IO.socket("http://192.168.0.130:5555/");
            socket.connect();
            LogUtils.e("Set socket IO", "Socket IO setting");
        } catch (Exception e) {
            LogUtils.e("Socket Problem", "Try cath", e);
        }

        this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtils.e("Connected");
                final Intent intent = new Intent();
                intent.setAction(SocketConstants.EVENT_CONNECTION);
//                intent.putExtra(SocketConstants.KEY_STATUS_CONNECTION, getString(R.string.connected));
                SocketClient.this.sendBroadcast(intent);

                socket.off("send_tn_notification");
                socket.on("noti", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        byte[] buf = (byte[]) args[0];
                    }
                });

            }
        });

        this.socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            public void call(Object... args) {
                Log.i("desc", "error desc");
                Intent intent = new Intent();
                intent.setAction(SocketConstants.EVENT_CONNECTION);
//                intent.putExtra(SocketConstants.KEY_STATUS_CONNECTION, getString(R.string.disconnect));
                SocketClient.this.sendBroadcast(intent);
            }
        });
        this.socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            public void call(Object... args) {
                LogUtils.e("Error", args[0].toString());
                Intent intent = new Intent();
                intent.setAction(SocketConstants.EVENT_CONNECTION);
//                intent.putExtra(SocketConstants.KEY_STATUS_CONNECTION, getString(R.string.disconnect));
                // SocketClient.this.sendBroadcast(intent);
            }
        });
        this.socket.on(Packet.ERROR, new Emitter.Listener() {
            public void call(Object... args) {
                LogUtils.e("Error", "Event Error");
                LogUtils.e(args[0].toString());
            }
        });
        this.socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            public void call(Object... args) {
                Intent intent = new Intent();
                intent.setAction(SocketConstants.EVENT_CONNECTION);
//                intent.putExtra(SocketConstants.KEY_STATUS_CONNECTION, SocketClient.this.getString(R.string.reconnect));
                SocketClient.this.sendBroadcast(intent);
                LogUtils.e(" reconecting ");
            }
        });
    }

    public void showNotificationInStack(int id) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;

    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //create a intent that you want to start again..
        Intent intent = new Intent(getApplicationContext(), SocketClient.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

}

