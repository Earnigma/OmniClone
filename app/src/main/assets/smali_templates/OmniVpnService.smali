.class public Lcom/omniclone/service/OmniVpnService;
.super Landroid/net/VpnService;

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/net/VpnService;-><init>()V
    return-void
.end method

.method public onStartCommand(Landroid/content/Intent;II)I
    .locals 1
    .param p1, "intent"    # Landroid/content/Intent;
    .param p2, "flags"    # I
    .param p3, "startId"    # I
    const/4 v0, 0x2
    return v0
.end method
