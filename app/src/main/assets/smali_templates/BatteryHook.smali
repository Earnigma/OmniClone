.class public Lcom/omniclone/hooks/BatteryHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static getIntExtra(Landroid/content/Intent;Ljava/lang/String;I)I
    .locals 1
    .param p0, "intent"    # Landroid/content/Intent;
    .param p1, "name"    # Ljava/lang/String;
    .param p2, "defaultValue"    # I
    const-string v0, "level"
    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :cond_0
    sget v0, Lcom/omniclone/runtime/OmniRuntime;->batteryLevel:I
    if-lez v0, :cond_0
    return v0
    :cond_0
    invoke-virtual {p0, p1, p2}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I
    move-result v0
    return v0
.end method
