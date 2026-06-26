.class public Lcom/omniclone/service/OmniAccessibilityService;
.super Landroid/accessibilityservice/AccessibilityService;

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/accessibilityservice/AccessibilityService;-><init>()V
    return-void
.end method

.method public onAccessibilityEvent(Landroid/view/accessibility/AccessibilityEvent;)V
    .locals 0
    .param p0, "this"    # Lcom/omniclone/service/OmniAccessibilityService;
    .param p1, "event"    # Landroid/view/accessibility/AccessibilityEvent;
    return-void
.end method

.method public onInterrupt()V
    .locals 0
    return-void
.end method

.method public onServiceConnected()V
    .locals 0
    invoke-super {p0}, Landroid/accessibilityservice/AccessibilityService;->onServiceConnected()V
    return-void
.end method
