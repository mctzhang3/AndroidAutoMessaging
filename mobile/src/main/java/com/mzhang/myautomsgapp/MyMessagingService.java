/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mzhang.myautomsgapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;

public class MyMessagingService extends Service {
    static final int MSG_SAY_HELLO = 1;

    public static final String READ_ACTION =
            "com.mzhang.myautomsgapp.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION =
            "com.mzhang.myautomsgapp.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String TAG = MyMessagingService.class.getSimpleName();
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
//    private NotificationManagerCompat mNotificationManager;

    @Override
    public void onCreate() {
//        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private Intent createIntent(int conversationId, String action) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(action)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void sendNotification(int conversationId, String message,
                                  String participant, long timestamp) {
        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversationId,
                createIntent(conversationId, READ_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a RemoteInput for receiving voice input in a Car Notification
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("Reply by voice")
                .build();

        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversationId,
                createIntent(conversationId, REPLY_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT);
//
        Notification.CarExtender.Builder unReadConversationsBuilder = new Notification.CarExtender.Builder(participant) ;
        unReadConversationsBuilder.addMessage(message);
        unReadConversationsBuilder.setLatestTimestamp(System.currentTimeMillis()) ;
        unReadConversationsBuilder.setReadPendingIntent( readPendingIntent)  ;
        unReadConversationsBuilder.setReplyAction(replyIntent, remoteInput);

        Log.d ( TAG, "originateCarExtendedNotification() carExtender") ;

        Notification.CarExtender carExtender =  new Notification.CarExtender() ;
//        carExtender.setColor(context.getResources().getColor(R.color.colorPrimary));
        carExtender.setLargeIcon(BitmapFactory.decodeResource(
                getApplicationContext().getResources(), R.drawable.alarm36)) ;
        carExtender.setUnreadConversation( unReadConversationsBuilder.build() ) ;
        Log.d ( TAG, "originateCarExtendedNotification() carExtender=" + carExtender) ;

        Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.alarm36)
                .setContentTitle ( participant)
                .setContentText(message )
                .setContentIntent(readPendingIntent)
//                .setColor( context.getResources().getColor(R.color.colorPrimary))
                .extend( carExtender)
                .build() ;

        Log.d ( TAG, "originateCarExtendedNotification() notification=" + notification) ;

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify( conversationId, notification );


        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.

    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int referenceId = (int) SystemClock.elapsedRealtime() ;

            String notification = msg.getData().getString("hello");

            sendNotification(referenceId, notification, "John Doe",
                    System.currentTimeMillis());
        }
    }
}
