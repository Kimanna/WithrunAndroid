package com.example.withrun;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    //putExtra
    final static String EXTRA_ROOM_DETAIL = "EXTRA_ROOM_DETAIL"; // roomDetail로 연결 초대기능
    final static String EXTRA_FOLLOW = "EXTRA_FOLLOW"; // follow 팔로우
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // if(!isAppRunning(getApplicationContext())) { //백그라운드일떄만 보낸다.
        //json 데이터 페이로드
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        String dataJson = remoteMessage.getData().get("data");
        String type = remoteMessage.getData().get("type"); // type필드 없으면 null반환됨
        String roomNo = remoteMessage.getData().get("roomNo"); // type필드 없으면 null반환됨
        Log.d(TAG, title);
        Log.d(TAG, message);
        Gson gson = new Gson();
        Log.d(TAG, "노티 타입 => "+type);
        if (type.equals("invited")) {
            Room_object room = gson.fromJson(dataJson, Room_object.class);
            sendNotification(title, message, roomNo, room);
        }else if(type.equals("followRequest")){
            Follow_object followRequest = gson.fromJson(dataJson , Follow_object.class);
            sendNotification(title,message, roomNo, followRequest); // roomNo 안에 layoutno 들어있음 ( followLayout 1 )
        }else{

        }

        //  }
    }
    // [END receive_message]


    // [START on_new_token]


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
    }
    // [END on_new_token]


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, String roomNo, Object object) {

        //노티피케이션 아이디 값이 서로 다르면 각각 구분되어 알림이뜬다. (ex 댓글알림을 1로 설정한 경우 댓글알림이 여러개오면 하나의알림이 새로고침되는형식이고, 초대notifyId 1인 경우 followRequest와 알림이 구분되어 보여진다.)
        int notifyId = 0;
        PendingIntent pendingIntent = null;
        if (object instanceof Room_object) {
            Log.d(TAG, " 초대 노티 전송");
            Intent intent = new Intent(this, Home.class);
            Room_object invitedUser = (Room_object) object;
            intent.putExtra(EXTRA_ROOM_DETAIL, invitedUser);
            intent.putExtra("roomNo", roomNo);
            intent.putExtra("location", "Notification");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            notifyId = 1;
        }
        else if(object instanceof Follow_object){
            Log.d(TAG, " 팔로우 노티 전송");
            Intent intent = new Intent(this, Follow.class);
            Follow_object followRequest = (Follow_object) object;
            intent.putExtra(EXTRA_FOLLOW, followRequest);
            intent.putExtra("location", "Notification");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            notifyId = 2;
        }


        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fcm_default_channel","알림", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
    }

    //백그라운드일때만 푸시메세지 보내고싶을 때 사용
    private boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(context.getPackageName())) {
                Log.d(TAG, procInfos.get(i).processName);
                return true;
            }
        }

        return false;
    }


}