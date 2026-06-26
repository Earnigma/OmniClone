.class public Lcom/omniclone/hooks/PermissionGrantHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static checkPermission(Ljava/lang/String;II)I
    .locals 1
    .param p0, "permission"    # Ljava/lang/String;
    .param p1, "pid"    # I
    .param p2, "uid"    # I
    const/4 v0, 0x0
    return v0
.end method

.method public static checkSelfPermission(Landroid/content/Context;Ljava/lang/String;)I
    .locals 1
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;
    const/4 v0, 0x0
    return v0
.end method
