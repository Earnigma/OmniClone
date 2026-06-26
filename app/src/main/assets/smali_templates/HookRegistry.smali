.class public Lcom/omniclone/hooks/HookRegistry;
.super Ljava/lang/Object;

.method public static applyAll()V
    .locals 0
    invoke-static {}, Lcom/omniclone/hooks/AndroidIdHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/ImeiHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/MacAddressHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/BluetoothMacHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/BuildPropsHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/UserAgentHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/LocationHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/MockLocationHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/StorageRedirectHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/SensorBlockHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/ClipboardBlockHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/LogDisableHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/PackageHideHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/RootHideHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/TimeHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/BatteryHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/MemoryHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/BiometricBlockHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/PermissionGrantHook;->hook()V
    invoke-static {}, Lcom/omniclone/hooks/VolumeBlockHook;->hook()V
    return-void
.end method
