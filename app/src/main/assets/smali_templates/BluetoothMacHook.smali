.class public Lcom/omniclone/hooks/BluetoothMacHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getAddress()Ljava/lang/String;
    .locals 1
    sget-object v0, Lcom/omniclone/runtime/OmniRuntime;->bluetoothMac:Ljava/lang/String;
    if-eqz v0, :cond_0
    return-object v0
    :cond_0
    const-string v0, "02:00:00:00:00:00"
    return-object v0
.end method
