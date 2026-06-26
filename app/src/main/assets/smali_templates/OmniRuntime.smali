.class public Lcom/omniclone/runtime/OmniRuntime;
.super Ljava/lang/Object;

.field public static androidId:Ljava/lang/String;
.field public static imei:Ljava/lang/String;
.field public static imsi:Ljava/lang/String;
.field public static wifiMac:Ljava/lang/String;
.field public static bluetoothMac:Ljava/lang/String;
.field public static ethernetMac:Ljava/lang/String;
.field public static gsfId:Ljava/lang/String;
.field public static googleAdId:Ljava/lang/String;
.field public static webViewUserAgent:Ljava/lang/String;
.field public static systemUserAgent:Ljava/lang/String;
.field public static latitude:D
.field public static longitude:D
.field public static mockLocation:Z
.field public static hideRoot:Z
.field public static hideSpecificApps:[Ljava/lang/String;
.field public static logDisabled:Z
.field public static proxyHost:Ljava/lang/String;
.field public static proxyPort:I
.field public static batteryLevel:I
.field public static fakeTime:J
.field public static freezeTime:Z

.method public static load(Landroid/content/Context;)V
    .locals 5
    .param p0, "context"    # Landroid/content/Context;

    const/4 v0, 0x0
    :try_start_0
    invoke-virtual {p0}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v1
    const-string v2, "smali_assets/hook_config.json"
    invoke-virtual {v1, v2}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;
    move-result-object v1
    invoke-static {v1}, Lcom/omniclone/runtime/OmniRuntime;->readJson(Ljava/io/InputStream;)V
    invoke-virtual {v1}, Ljava/io/InputStream;->close()V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :catch_0
    return-void
.end method

.method private static readJson(Ljava/io/InputStream;)V
    .locals 2
    .param p0, "input"    # Ljava/io/InputStream;

    new-instance v0, Ljava/io/BufferedReader;
    new-instance v1, Ljava/io/InputStreamReader;
    invoke-direct {v1, p0}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;)V
    invoke-direct {v0, v1}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    new-instance v1, Ljava/lang/StringBuilder;
    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    :loop
    invoke-virtual {v0}, Ljava/io/BufferedReader;->readLine()Ljava/lang/String;
    move-result-object v0
    if-eqz v0, :done
    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    goto :loop

    :done
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Lcom/omniclone/runtime/OmniRuntime;->parseJson(Ljava/lang/String;)V
    return-void
.end method

.method private static parseJson(Ljava/lang/String;)V
    .locals 3
    .param p0, "json"    # Ljava/lang/String;

    const-string v0, "\"androidId\""
    invoke-virtual {p0, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I
    move-result v1
    const/4 v2, -0x1
    if-eq v1, v2, :cond_0
    invoke-static {p0, v0}, Lcom/omniclone/runtime/OmniRuntime;->extractString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    sput-object v0, Lcom/omniclone/runtime/OmniRuntime;->androidId:Ljava/lang/String;

    :cond_0
    const-string v0, "\"imei\""
    invoke-virtual {p0, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I
    move-result v1
    if-eq v1, v2, :cond_1
    invoke-static {p0, v0}, Lcom/omniclone/runtime/OmniRuntime;->extractString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    sput-object v0, Lcom/omniclone/runtime/OmniRuntime;->imei:Ljava/lang/String;

    :cond_1
    const-string v0, "\"wifiMac\""
    invoke-virtual {p0, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I
    move-result v1
    if-eq v1, v2, :cond_2
    invoke-static {p0, v0}, Lcom/omniclone/runtime/OmniRuntime;->extractString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    sput-object v0, Lcom/omniclone/runtime/OmniRuntime;->wifiMac:Ljava/lang/String;

    :cond_2
    return-void
.end method

.method private static extractString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .locals 3
    .param p0, "json"    # Ljava/lang/String;
    .param p1, "key"    # Ljava/lang/String;

    invoke-virtual {p0, p1}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I
    move-result v0
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v1
    add-int/2addr v0, v1
    const-string v1, "\":\""
    invoke-virtual {p0, v1, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;I)I
    move-result v0
    add-int/lit8 v0, v0, 0x3
    const-string v1, "\""
    invoke-virtual {p0, v1, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;I)I
    move-result v1
    invoke-virtual {p0, v0, v1}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    move-result-object v2
    return-object v2
.end method
