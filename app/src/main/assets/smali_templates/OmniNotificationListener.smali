.class public Lcom/omniclone/service/OmniNotificationListener;
.super Landroid/service/notification/NotificationListenerService;

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/service/notification/NotificationListenerService;-><init>()V
    return-void
.end method

.method public onNotificationPosted(Landroid/service/notification/StatusBarNotification;)V
    .locals 0
    .param p1, "sbn"    # Landroid/service/notification/StatusBarNotification;
    return-void
.end method

.method public onNotificationRemoved(Landroid/service/notification/StatusBarNotification;)V
    .locals 0
    .param p1, "sbn"    # Landroid/service/notification/StatusBarNotification;
    return-void
.end method
