package com.example.withrun;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {
    final static String TAG = "SendNotification";

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAdaimxoY:APA91bEOrHL5FpDkO7Ze4memhpRrf-6dky6usg5Jr42N0TSh9pEKwp6--zOnSXdUMYN9qqEAmxm6bkN1QgPX1OQ8x22vbNAHfL4tW9CG_cX4d-2JHjjOHc8srnvugNX25pLuVbZHgAqe";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendNotification(String regToken, String title, String messsage, String roomNo, Object object){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parms) {
                try {
                    Log.d(TAG, "상대방토큰 : " + regToken);
                    Log.d(TAG, "제목 : " + title);
                    Log.d(TAG, "메세지 : " + messsage);
                    String type = "";
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    //데이터 필드 담기
                    JSONObject dataJson = new JSONObject();
                    Gson gson =new Gson();
                    String data = gson.toJson(object); //객체JSON으로 변형해서 전달할거
                    dataJson.put("data", data);

//                    type담을 예정
                    if(object instanceof Room_object){
                        type = "invited";
                        dataJson.put("type",type );

                    }else if(object instanceof Follow_object){
                        type = "followRequest";
                        dataJson.put("type",type );
                    }


                    //title, body 담을 예정
                    dataJson.put("title", title);
                    dataJson.put("body", messsage);
                    dataJson.put("roomNo",roomNo );

                    //json에 puy
                    json.put("data", dataJson); //이건 데이터필드니깐 키필드값을 data로 해주어야한다.
                    Log.d(TAG, "노티 데이터 페이로드===> " + data);
                    Log.d(TAG, "노티  데이터타입 페이로드===> " + type);
                    Log.d("JSON" , json.toString());

                    //토큰 필드 담기
                    json.put("to", regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + SERVER_KEY)
                            .url(FCM_MESSAGE_URL)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d("TAG", finalResponse);
                }catch (Exception e){
                    Log.d("error", e+"");
                }
                return  null;
            }
        }.execute();
    }
}