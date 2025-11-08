package com.example.qolzy;

import android.app.Application;
import android.util.Log;

import com.example.qolzy.data.model.Notification;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.util.NotificationHelper;
import com.example.qolzy.util.Utils;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class QolzyApp extends Application {
    private static final String TAG = "WebSocket";
    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private UserRepository userRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // Lấy user hiện tại
        userRepository = new UserRepository(getApplicationContext());
        Long userId = userRepository.getUser().getId();

        connectWebSocket(userId);
    }

    private void connectWebSocket(Long userId) {
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String wsUrl = fixedUrl + "/chat/websocket";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "✅ Connected to WebSocket");
                // Khi server yêu cầu subscribe, bạn có thể gửi message đăng ký topic
                String subscribeMsg = String.format("{\"command\":\"subscribe\",\"destination\":\"/user/%d/queue/notifications\"}", userId);
                webSocket.send(subscribeMsg);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "📩 Received: " + text);

                try {
                    Notification notification = gson.fromJson(text, Notification.class);
                    String messageNotification = "";

                    if ("follow".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã theo dõi bạn";
                    } else if ("comment".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã bình luận về bài viết của bạn";
                    } else if ("comment-reply".equals(notification.getType())) {
                        messageNotification = notification.getSender().getUserName() + " đã trả lời bình luận của bạn";
                    }

                    NotificationHelper.createNotificationChannel(getApplicationContext());
                    NotificationHelper.showNotification(
                            getApplicationContext(),
                            "Thông báo mới",
                            messageNotification
                    );
                } catch (Exception e) {
                    Log.e(TAG, "⚠️ Error parsing message: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (response != null) {
                    try {
                        Log.e(TAG, "❌ WebSocket Error: code=" + response.code() + ", message=" + response.message());
                    } catch (Exception e) {
                        Log.e(TAG, "❌ WebSocket Error: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ WebSocket Error (no response): " + (t != null ? t.getMessage() : "unknown"));
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.w(TAG, "🔌 WebSocket closed: " + reason);
            }
        });

        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (webSocket != null) {
            webSocket.close(1000, "App terminated");
        }
    }
}
