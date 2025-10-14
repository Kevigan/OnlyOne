package com.onlyone.app.data

sealed class ReportResult {
    object Success : ReportResult()
    object Duplicate : ReportResult()           // already reported by this user
    object Invalid : ReportResult()             // bad payload / not found, etc.
    data class Error(val message: String?) : ReportResult()
}

enum class ReportReason(val code: String) {
    RACISM("racism"),
    HARASSMENT("harassment"),
    SEXUAL("sexual"),
    SPAM("spam"),
    SELF_HARM("self-harm"),
    OTHER("other")
}
