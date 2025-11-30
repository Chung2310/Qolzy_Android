package com.example.qolzy;

import static com.example.qolzy.util.NotificationHelper.showNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qolzy.data.model.Notification;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.util.NotificationHelper;
import com.example.qolzy.util.Utils;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class QolzyApp extends Application {
    private static final String TAG = "WebSocket";
    private WebSocket webSocket;
    private StompClient stompClient;
    private final Gson gson = new Gson();
    private UserRepository userRepository;
    private static final int REQ_NOTIFY = 1001;

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // Lấy user hiện tại
        userRepository = new UserRepository(getApplicationContext());

        connectStomp();
//        connectWebSocket(userId);
    }

    @SuppressLint("CheckResult")
    private void connectStomp() {
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String fixedUrlWs = fixedUrl.replace("https", "wss");
        String wsUrl = fixedUrlWs + "/chat/websocket";

        stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                wsUrl
        );
        stompClient.withClientHeartbeat(10000)
                .withServerHeartbeat(10000);


        stompClient.lifecycle().subscribe(event ->{
            switch (event.getType()) {
                case OPENED:
                    Log.d(TAG, "✅ STOMP CONNECTED");
                    subscribeNotification();
                    break;

                case ERROR:
                    Log.e(TAG, "❌ STOMP ERROR", event.getException());
                    break;

                case CLOSED:
                    Log.w(TAG, "🔌 STOMP CLOSED");
                    break;
            }
        });

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("user-id", String.valueOf(userRepository.getUserId())));
        stompClient.connect(headers);
    }


    @SuppressLint("CheckResult")
    private void subscribeNotification() {
        stompClient.topic("/user/queue/notifications") // queue dành cho user riêng
                .subscribe(stompMessage -> {
                    String payload = stompMessage.getPayload();
                    Log.d(TAG, "📩 Received notification: " + payload);

                    // Parse payload JSON
                    Notification notification =
                            new Gson().fromJson(payload, Notification.class);

                    String messageNotification = "";

                    if ("follow".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã theo dõi bạn";
                    } else if ("comment".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã bình luận về bài viết của bạn";
                    } else if ("comment-reply".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã trả lời bình luận của bạn";
                    }

                    showNotification(getApplicationContext(), "Thông báo mới", messageNotification);
                });
    }



    @Override
    public void onTerminate() {
        super.onTerminate();
        if (webSocket != null) {
            webSocket.close(1000, "App terminated");
        }
    }
}
