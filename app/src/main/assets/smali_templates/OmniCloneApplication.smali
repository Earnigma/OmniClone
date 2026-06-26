.class public Lcom/omniclone/runtime/OmniCloneApplication;
.super Landroid/app/Application;

.field private static final TAG:Ljava/lang/String; = "OmniCloneApp"

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/app/Application;-><init>()V
    return-void
.end method

.method public onCreate()V
    .locals 2
    invoke-super {p0}, Landroid/app/Application;->onCreate()V
    const-string v0, "OmniCloneApp"
    const-string v1, "OmniClone runtime initialized"
    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    invoke-static {p0}, Lcom/omniclone/runtime/OmniRuntime;->load(Landroid/content/Context;)V
    invoke-static {p0}, Lcom/omniclone/runtime/OmniCloneApplication;->loadHooks(Landroid/content/Context;)V
    return-void
.end method

.method private static loadHooks(Landroid/content/Context;)V
    .locals 0
    .param p0, "context"    # Landroid/content/Context;
    invoke-static {}, Lcom/omniclone/hooks/HookRegistry;->applyAll()V
    return-void
.end method
