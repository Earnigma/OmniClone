.class public Lcom/omniclone/hooks/BuildPropsHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getString(Ljava/lang/String;)Ljava/lang/String;
    .locals 1
    .param p0, "key"    # Ljava/lang/String;
    sget-object v0, Lcom/omniclone/runtime/OmniRuntime;->systemUserAgent:Ljava/lang/String;
    if-eqz v0, :cond_0
    return-object v0
    :cond_0
    return-object p0
.end method
