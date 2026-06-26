.class public Lcom/omniclone/hooks/PackageHideHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static filterList(Ljava/util/List;)Ljava/util/List;
    .locals 2
    .param p0, "list"    # Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(
            "Ljava/util/List<",
            "Landroid/content/pm/ApplicationInfo;",
            ">;)",
            "Ljava/util/List<",
            "Landroid/content/pm/ApplicationInfo;",
            ">;"
        }
    .end annotation

    sget-object v0, Lcom/omniclone/runtime/OmniRuntime;->hideSpecificApps:[Ljava/lang/String;
    if-eqz v0, :cond_0
    array-length v1, v0
    if-nez v1, :cond_1
    :cond_0
    return-object p0
    :cond_1
    return-object p0
.end method
