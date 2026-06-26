.class public Lcom/omniclone/hooks/ClipboardBlockHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getPrimaryClip()Landroid/content/ClipData;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public static setPrimaryClip(Landroid/content/ClipData;)V
    .locals 0
    .param p0, "clip"    # Landroid/content/ClipData;
    return-void
.end method
