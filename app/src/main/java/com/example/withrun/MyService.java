//package com.example.withrun;
//
//import android.Manifest;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Binder;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.speech.tts.TextToSpeech;
//import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.tasks.OnSuccessListener;
//
//import java.util.Locale;
//import java.util.concurrent.Executor;
//
//public class MyService extends Service implements LocationListener, TextToSpeech.OnInitListener {
//
//    String TAG = "MyService";
//
//    FusedLocationProviderClient fusedLocationProviderClient;
//    LocationRequest locationRequest;
//    LocationCallback locationCallback;
//
//    private Thread mThread;
//
//    private long stopwatch = 0;
//    private int mCount = 0;
//    int participant = 0;
//
//    boolean isGPSEnable = false;
//    boolean isNetworkEnable = false;
//    double latitude, longitude, afterLatitude, afterLongitude;
//    double totalDistance, Distance;
//    long avgPace = 0;
//
//    int speakCount = 0;
//
//    LocationManager locationManager;
//    Location location;
//
//    NotificationManager manager;
//    NotificationCompat.Builder builder;
//
//    private TextToSpeech tts;
//
//
//    private IBinder mBinder = new MyBinder();
//
//    private final Float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0F;
//    private final int MIN_TIME_BW_UPDATES = 0;
//    String ACTION_STOP_SERVICE = "STOP";
//
//
//    public class MyBinder extends Binder {
//        public com.example.withrun.MyService getService() {
//            return com.example.withrun.MyService.this;
//        }
//    }
//
//    public MyService() {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    public int getmCount() {
//        return mCount;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//
////        getLocation();
//        tts = new TextToSpeech(this, this);
//
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
////        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
////            Log.d(TAG, "stop service");
////            goEndRace();
////        }
//
////        "startForeground".equals(intent.getAction())
//        stopwatch = intent.getIntExtra("stopwatch", 0);
//        participant = intent.getIntExtra("participant", 0);
//        Log.d(TAG, "인텐트 받아온 값" + intent.getIntExtra("stopwatch", 0));
//
//
//        startForegroundService();
//        if (mThread == null) {
//            mThread = new Thread("My Thread") {
//                @Override
//                public void run() {
//                    for (int i = 0; i < stopwatch; i++) {
//                        try {
//                            Thread.sleep(1000);
//                            mCount++;
//
//                            /* 페이스 계산 방법
//                            1. totalDistance (m) 의 소수점 첫번째 자리에서 반올림
//                            2. 시간 ( mCount ) / 거리 ( totalDistance ) 이후 소수점 첫번째 자리에서 반올림
//                            3. m 단위를 km 단위로 변경 ( x 1000 )
//
//                            totalDistance 단위 ( m )
//                             */
//                            Log.d(TAG, "서비스 동작 중 시간 " + mCount);
//
//                            if (totalDistance == 0.0) { // 아직 이동거리가 없는경우
//
//                                sendMessage(mCount, totalDistance, avgPace, Distance);
//
//                            } else { // 이동거리가 있는경우
//
//                                avgPace = Math.round(mCount / (double) (Math.round(totalDistance * 10) / (double) 10) * 1000);
////                                Log.d(TAG, "서비스 동작 중 " + avgPace);
//
//                                // ui변경을 위해 activity로 시간, 거리, 평균페이스 정보 전달
//                                sendMessage(mCount, totalDistance, avgPace, Distance);
//                            }
//
//                            updateNoti(); // notification에 현재시간, 거리 변경
//
//                            // 음성알림은 100m 단위로 알림  ( 기획은 1k 단위 알림 )
//                            if (Math.floor(totalDistance) % 1000 == 0.0 && (int) Math.floor(totalDistance) / 1000 != speakCount && totalDistance != 0.0) { // 시간, 거리, 현재 페이스
//                                speakCount = (int) Math.floor(totalDistance) / 1000; // 음성이 여러번 호출되지 않도록 1의자리를 설정해줌
//                                speakStatus();
//                            }
//
//                        } catch (InterruptedException e) {
//                            break;
//                        }
//                    }
//                }
//            };
//            mThread.start();
//        }
//
//        return START_REDELIVER_INTENT;
//    }
//
//    private void sendMessage(int stopwatch, double totalDistance, long avgPace, double distance) {
//
//        Intent intent = new Intent("MoveServiceFilter");
//        intent.putExtra("stopwatch", stopwatch);
//        intent.putExtra("totalDistance", totalDistance);
//        intent.putExtra("avgPace", avgPace);
//        intent.putExtra("distance", distance);
//
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }
//
//
//    void speakStatus () {
//
//        int hourTime = mCount / (60 * 60) % 24;
//        int minTime = mCount / 60 % 60;
//        int secTime = mCount % 60;
//
//        String hourString = "";
//        String minString = "";
//        String secString = "";
//
//        if ( hourTime != 0 ) {
//            hourString = hourTime + "시간";
//        }
//        if ( minTime != 0 ) {
//            minString = minTime + "분";
//        }
//        if ( secTime != 0 ) {
//            secString = secTime + "초";
//        }
//        CharSequence stringTime = "현재 경주 시간은 " + hourString + minString + secString;
//
//        int distanceKm = (int) Math.floor(totalDistance) / 1000;
//        int distanceMT = (int) (Math.floor(totalDistance) % 1000 ) / 10;
//
//        String kmString = "";
//        String mtString = distanceMT + " 키로미터";
//        if (distanceKm == 0) {
//            kmString = "영점";
//        } else {
//            kmString = distanceKm + " 점";
//        }
//
//        CharSequence stringDistance = "경주 거리 " + kmString + mtString;
//
//        long minutes =  avgPace / 60;
//        long seconds = avgPace % 60;
//        CharSequence stringPace = "평균 페이스 " + minutes + "분" +seconds+ "초";
//
//        tts.speak(stringTime,TextToSpeech.QUEUE_ADD,null,null);
////        try {
////            Thread.sleep(1000);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//        tts.speak(stringDistance,TextToSpeech.QUEUE_ADD,null,null);
//        tts.speak(stringPace,TextToSpeech.QUEUE_ADD,null,null);
//
//    }
//
//
//    @Override
//    public void onLocationChanged(@NonNull Location location) {
////        Log.d(TAG, "onLocationChanged 지나감 " + location.getProvider());
//
////        Log.d(TAG, "before 실시간 위도경도 " + latitude+" : "+longitude);
//
//        if ( mCount == 0 ) {
//            return;
//        }
//
//        if (location != null) {
//            afterLatitude = location.getLatitude();
//            afterLongitude = location.getLongitude();
//        }
//
//        Distance = DistanceByDegreeAndroid(latitude, longitude, afterLatitude, afterLongitude);
//        totalDistance = totalDistance + Distance;
//
//        Log.d(TAG, "after 실시간 위도경도+총거리 " + afterLatitude + " : " + afterLongitude + " : " + totalDistance);
//        Log.d(TAG, "실시간 두거리차이1 " + Distance);
//
//        latitude = afterLatitude;
//        longitude = afterLongitude;
//    }
//
//
//    public void startForegroundService() {
//        Log.d(TAG,"startForegroundService :  ");
//
//        builder = new NotificationCompat.Builder(this, "default");
//        builder.setSmallIcon(R.mipmap.ic_launcher);
//        builder.setContentTitle("러닝 경기 중");
//        builder.setContentText("러닝 시작");
//        builder.setColor(Color.RED);
//        builder.setOngoing(true);
//        builder.setOnlyAlertOnce(true);
//
//        Intent notificationIntent = new Intent(this, MyService.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//
//
//        Intent intent = new Intent(this, RaceScore.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntentaction = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.addAction(new NotificationCompat.Action(0, "러닝 종료", pendingIntentaction));
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(new NotificationChannel("default", "기본채널", NotificationManager.IMPORTANCE_DEFAULT));
//        }
//
//        startForeground(1, builder.build());
//    }
//
//    public void stopForegroundService() {
//        Log.d(TAG, "stopForegroundService : ");
//
//        mCount = 0;
//        totalDistance = 0.0;
//
//        updateNoti();
//
//        stopForeground(true);
//        stopSelf();
//
//        if ( mThread != null ) {
//            mThread.interrupt();
//            mThread = null;
//        }
//
//        if (tts != null) {
//            tts.stop();
//            tts.shutdown();
//        }
//
//    }
//
//    public void updateNoti() {
//
//        builder.setContentText(notiString());
//        manager.notify(1,builder.build());
//
//    }
//
//    private void getLocation() {
//        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        locationManager.getBestProvider(highCriteria (), true);
//
//        if (!isGPSEnable && !isNetworkEnable) {
//
//        } else {
//            if (isGPSEnable) {
//                location = null;
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                if (locationManager!=null){
//                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    if (location!=null){
//                        Log.d(TAG,location.getLatitude()+" "+location.getLongitude()+" GPS");
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                    }
//                }
//            }
//        }
//    }
//
//    public Criteria highCriteria () {
//
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setPowerRequirement(Criteria.POWER_HIGH);
//        criteria.setSpeedRequired(true);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(false);
//
//        return criteria;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void speakOut() {
//        Log.d(TAG, "speakOut 지남 ");
//
//        tts.setPitch((float) 0.5);
//        tts.setSpeechRate((float) 1.3);
//        tts.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null);
////        tts.speak(String.valueOf(mCount),TextToSpeech.QUEUE_FLUSH,null,null);
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onInit(int status) {
//        Log.d(TAG, "onInit 지남 ");
//
//        if (status == TextToSpeech.SUCCESS) {
//            int result = tts.setLanguage(Locale.KOREA);
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "not supported");
//            } else {
//                speakOut();
//            }
//        } else {
//            Log.e("TTS","initi failed");
//        }
//    }
//
//    //안드로이드 - 두지점(위도,경도) 사이의 거리
//    public double DistanceByDegreeAndroid(double _latitude1, double _longitude1, double _latitude2, double _longitude2){
//        Location startPos = new Location("PointA");
//        Location endPos = new Location("PointB");
//
//        startPos.setLatitude(_latitude1);
//        startPos.setLongitude(_longitude1);
//        endPos.setLatitude(_latitude2);
//        endPos.setLongitude(_longitude2);
//
//        double distance = startPos.distanceTo(endPos);
//
//        return distance;
//    }
//
//    public String twoDigitString(Long number) {
//        if (number == 0L) {
//            return "00";
//        } else if (number / 10 == 0L) {
//            return "0" + number;
//        } else
//            return number.toString();
//    }
//
//    public String oneDigiString(Long number) {
//        if (number == 0.0) {
//            return "0";
//        } else
//            return String.valueOf(number);
//    }
//
//    public String distanceDigitString(Long number) {
//        if ( number == 0L ) {
//            return "00";
//        } else if (number / 10 == 0L) {
//            return "0" + number;
//        } else if (number / 100 >= 1) {
//            String numCount = number.toString();
//            String Mt = numCount.substring(numCount.length()-2, numCount.length());
//            return Mt;
//        } else
//            return number.toString();
//    }
//
//    public String notiString() {
//        String stopwatchstr = (twoDigitString((long) (mCount / (60 * 60) % 24)) + " : " + twoDigitString((long) (mCount / 60 % 60)) + " : "
//                + twoDigitString((long) (mCount % 60)));
//        String notiText = "운동시간 :  " + stopwatchstr + ", 운동거리 :  " + oneDigiString((long) Math.floor(totalDistance/1000)) + " . " + distanceDigitString( (long) Math.floor(totalDistance/(double)10)) + "km";
//        return notiText;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy : ");
//    }
//
//    @Override // 위치공급자가 바뀔 때 호출
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override // 위치공급자 사용이 가능해질 때 호출
//    public void onProviderEnabled(String s) {
//
//    }
//
//    @Override // 위치공급자 사용이 불가능해질 때 호출
//    public void onProviderDisabled(String s) {
//
//    }
//}