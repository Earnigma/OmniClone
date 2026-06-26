.class public Lcom/omniclone/hooks/TimeHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static currentTimeMillis()J
    .locals 2
    sget-boolean v0, Lcom/omniclone/runtime/OmniRuntime;->freezeTime:Z
    if-eqz v0, :cond_0
    sget-wide v0, Lcom/omniclone/runtime/OmniRuntime;->fakeTime:J
    return-wide v0
    :cond_0
    sget-wide v0, Lcom/omniclone/runtime/OmniRuntime;->fakeTime:J
    const-wide/16 v0, 0x0
    return-wide v0
.end method
