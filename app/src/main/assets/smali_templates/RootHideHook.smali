.class public Lcom/omniclone/hooks/RootHideHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static isRooted()Z
    .locals 1
    sget-boolean v0, Lcom/omniclone/runtime/OmniRuntime;->hideRoot:Z
    if-eqz v0, :cond_0
    const/4 v0, 0x0
    return v0
    :cond_0
    const/4 v0, 0x0
    return v0
.end method

.method public static fileExists(Ljava/lang/String;)Z
    .locals 1
    .param p0, "path"    # Ljava/lang/String;
    const-string v0, "/su/bin/su"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :cond_0
    const/4 v0, 0x0
    return v0
    :cond_0
    new-instance v0, Ljava/io/File;
    invoke-direct {v0, p0}, Ljava/io/File;-><init>(Ljava/lang/String;)V
    invoke-virtual {v0}, Ljava/io/File;->exists()Z
    move-result v0
    return v0
.end method
