.class public Lcom/omniclone/hooks/LocationHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;
    .locals 3
    .param p0, "provider"    # Ljava/lang/String;

    new-instance v0, Landroid/location/Location;
    invoke-direct {v0, p0}, Landroid/location/Location;-><init>(Ljava/lang/String;)V

    sget-wide v1, Lcom/omniclone/runtime/OmniRuntime;->latitude:D
    invoke-virtual {v0, v1, v2}, Landroid/location/Location;->setLatitude(D)V

    sget-wide v1, Lcom/omniclone/runtime/OmniRuntime;->longitude:D
    invoke-virtual {v0, v1, v2}, Landroid/location/Location;->setLongitude(D)V

    const/high16 v1, 0x41200000    # 10.0f
    invoke-virtual {v0, v1}, Landroid/location/Location;->setAccuracy(F)V

    return-object v0
.end method
