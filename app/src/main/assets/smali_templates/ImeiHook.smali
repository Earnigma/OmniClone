.class public Lcom/omniclone/hooks/ImeiHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getDeviceId()Ljava/lang/String;
    .locals 1
    sget-object v0, Lcom/omniclone/runtime/OmniRuntime;->imei:Ljava/lang/String;
    if-eqz v0, :cond_0
    return-object v0
    :cond_0
    const-string v0, "000000000000000"
    return-object v0
.end method
