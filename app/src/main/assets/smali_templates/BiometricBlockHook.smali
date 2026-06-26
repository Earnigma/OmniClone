.class public Lcom/omniclone/hooks/BiometricBlockHook;
.super Ljava/lang/Object;

.method public static hook()V
    .locals 0
    return-void
.end method

.method public static authenticate(Landroid/hardware/biometrics/BiometricPrompt;Landroid/os/CancellationSignal;Ljava/util/concurrent/Executor;Landroid/hardware/biometrics/BiometricPrompt$AuthenticationCallback;)V
    .locals 2
    .param p0, "prompt"    # Landroid/hardware/biometrics/BiometricPrompt;
    .param p1, "cancel"    # Landroid/os/CancellationSignal;
    .param p2, "executor"    # Ljava/util/concurrent/Executor;
    .param p3, "callback"    # Landroid/hardware/biometrics/BiometricPrompt$AuthenticationCallback;

    new-instance v0, Landroid/hardware/biometrics/BiometricPrompt$AuthenticationResult;
    const/4 v1, 0x0
    invoke-direct {v0, v1, v1, v1}, Landroid/hardware/biometrics/BiometricPrompt$AuthenticationResult;-><init>(Landroid/hardware/biometrics/BiometricPrompt$CryptoObject;IZ)V
    invoke-virtual {p3, v0}, Landroid/hardware/biometrics/BiometricPrompt$AuthenticationCallback;->onAuthenticationSucceeded(Landroid/hardware/biometrics/BiometricPrompt$AuthenticationResult;)V
    return-void
.end method
