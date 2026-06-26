.class public Lcom/omniclone/hooks/MemoryHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static totalMemory()J
    .locals 2
    const-wide v0, 0x100000000L
    return-wide v0
.end method

.method public static freeMemory()J
    .locals 2
    const-wide v0, 0x80000000L
    return-wide v0
.end method
