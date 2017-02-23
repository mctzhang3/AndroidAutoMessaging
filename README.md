# AndroidAutoMessaging
Project to implement Android Auto messaging - send message notification to Car

Android Auto messaging (notification)
1. Declare Auto messaging support in manifest.xml and xml/automotive_app_desc
2. Define read and reply intent filters
3. Set up the conversation builder - using Notification.CarExtender.Builder
4. Sending Messages
notificationManager.notify( conversationId, notification );
