.class public Lcom/omniclone/hooks/AndroidIdHook;
.super Ljava/lang/Object;

.method public static getAndroidId(Landroid/content/Context;)Ljava/lang/String;
    .locals 2
    .param p0, "context"    # Landroid/content/Context;

    invoke-static {}, Lcom/omniclone/runtime/HookConfig;->getInstance()Lcom/omniclone/runtime/HookConfig;
    move-result-object v0
    if-eqz v0, :cond_0
    invoke-virtual {v0}, Lcom/omniclone/runtime/HookConfig;->getIdentity()Lcom/omniclone/model/IdentityConfig;
    move-result-object v0
    if-eqz v0, :cond_0
    invoke-virtual {v0}, Lcom/omniclone/model/IdentityConfig;->getAndroidId()Ljava/lang/String;
    move-result-object v1
    if-eqz v1, :cond_0
    return-object v1

    :cond_0
    const-string v0, "0000000000000000"
    return-object v0
.end method

.method public static hook()V
    .locals 0
    return-void
.end method
