.class public Lcom/omniclone/hooks/UserAgentHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getUserAgentString()Ljava/lang/String;
    .locals 1
    sget-object v0, Lcom/omniclone/runtime/OmniRuntime;->webViewUserAgent:Ljava/lang/String;
    if-eqz v0, :cond_0
    return-object v0
    :cond_0
    const-string v0, "Mozilla/5.0 (Linux; Android 14; Mobile)"
    return-object v0
.end method
