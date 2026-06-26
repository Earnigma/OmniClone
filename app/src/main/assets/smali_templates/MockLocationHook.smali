.class public Lcom/omniclone/hooks/MockLocationHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static isFromMockProvider(Landroid/location/Location;)Z
    .locals 1
    .param p0, "location"    # Landroid/location/Location;
    sget-boolean v0, Lcom/omniclone/runtime/OmniRuntime;->mockLocation:Z
    if-nez v0, :cond_0
    const/4 v0, 0x0
    return v0
    :cond_0
    const/4 v0, 0x0
    return v0
.end method
