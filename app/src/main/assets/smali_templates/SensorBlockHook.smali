.class public Lcom/omniclone/hooks/SensorBlockHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static zeroValues([F)[F
    .locals 2
    .param p0, "values"    # [F
    if-nez p0, :cond_0
    const/4 v0, 0x0
    return-object v0
    :cond_0
    const/4 v0, 0x0
    :goto_0
    array-length v1, p0
    if-ge v0, v1, :cond_1
    const/4 v1, 0x0
    aput v1, p0, v0
    add-int/lit8 v0, v0, 0x1
    goto :goto_0
    :cond_1
    return-object p0
.end method
