package com.x0s.link.data

/**
 * profiles.js / colleges.js are plain JS files of the form:
 *   window.XOS_PROFILES = window.XOS_PROFILES || { nikhil: { handle:'...', ... } };
 * This is *almost* JSON — unquoted keys and single-quoted strings. This utility
 * extracts the object/array literal assigned to a given `window.NAME` and rewrites
 * it into valid JSON so org.json can parse it. It's intentionally conservative:
 * it only handles identifiers, strings, numbers, arrays and nested objects — the
 * subset actually used by the real x0s data files. Trailing commas are tolerated.
 */
object JsObjectExtractor {

    /** Pulls out the literal assigned to `window.<varName>` (last assignment wins, matching `a || {}` patterns). */
    fun extractLiteral(js: String, varName: String): String? {
        val marker = "window.$varName"
        var searchFrom = 0
        var lastLiteral: String? = null
        while (true) {
            val idx = js.indexOf(marker, searchFrom)
            if (idx == -1) break
            val eq = js.indexOf('=', idx)
            if (eq == -1) break
            // find first '{' or '[' after '=' that isn't part of `window.X || {}` fallback-only chain
            var i = eq + 1
            while (i < js.length && js[i].isWhitespace()) i++
            // skip a leading `window.X ||` fallback prefix if present
            if (js.startsWith(marker, i)) {
                val or = js.indexOf("||", i)
                if (or != -1) {
                    i = or + 2
                    while (i < js.length && js[i].isWhitespace()) i++
                }
            }
            if (i >= js.length || (js[i] != '{' && js[i] != '[')) { searchFrom = eq + 1; continue }
            val open = js[i]
            val close = if (open == '{') '}' else ']'
            var depth = 0
            var end = i
            var inStr = false
            var strCh = ' '
            var j = i
            while (j < js.length) {
                val c = js[j]
                if (inStr) {
                    if (c == '\\') { j += 2; continue }
                    if (c == strCh) inStr = false
                } else {
                    if (c == '\'' || c == '"') { inStr = true; strCh = c }
                    else if (c == open) depth++
                    else if (c == close) { depth--; if (depth == 0) { end = j; break } }
                }
                j++
            }
            lastLiteral = js.substring(i, end + 1)
            searchFrom = end + 1
        }
        return lastLiteral
    }

    /** Converts a JS object/array literal (unquoted keys, single quotes, trailing commas) into strict JSON. */
    fun toJson(jsLiteral: String): String {
        val sb = StringBuilder()
        var i = 0
        val n = jsLiteral.length
        while (i < n) {
            val c = jsLiteral[i]
            when {
                c == '\'' -> {
                    // single-quoted string -> double-quoted JSON string
                    sb.append('"')
                    i++
                    while (i < n && jsLiteral[i] != '\'') {
                        val ch = jsLiteral[i]
                        if (ch == '\\' && i + 1 < n) { sb.append(ch); sb.append(jsLiteral[i + 1]); i += 2; continue }
                        if (ch == '"') sb.append("\\\"") else sb.append(ch)
                        i++
                    }
                    sb.append('"')
                    i++ // closing quote
                }
                c == '"' -> {
                    // already double-quoted string, copy verbatim respecting escapes
                    sb.append('"')
                    i++
                    while (i < n && jsLiteral[i] != '"') {
                        val ch = jsLiteral[i]
                        if (ch == '\\' && i + 1 < n) { sb.append(ch); sb.append(jsLiteral[i + 1]); i += 2; continue }
                        sb.append(ch)
                        i++
                    }
                    sb.append('"')
                    i++
                }
                c == '/' && i + 1 < n && jsLiteral[i + 1] == '/' -> {
                    // line comment
                    while (i < n && jsLiteral[i] != '\n') i++
                }
                c.isLetter() || c == '_' || c == '$' -> {
                    // bareword: could be an unquoted key, or true/false/null
                    val start = i
                    while (i < n && (jsLiteral[i].isLetterOrDigit() || jsLiteral[i] == '_' || jsLiteral[i] == '$')) i++
                    val word = jsLiteral.substring(start, i)
                    var k = i
                    while (k < n && jsLiteral[k].isWhitespace()) k++
                    val isKey = k < n && jsLiteral[k] == ':'
                    if (isKey) sb.append('"').append(word).append('"')
                    else sb.append(word) // true/false/null literal
                }
                c == ',' -> {
                    // handle trailing comma before } or ]
                    var k = i + 1
                    while (k < n && jsLiteral[k].isWhitespace()) k++
                    if (k < n && (jsLiteral[k] == '}' || jsLiteral[k] == ']')) { i++; continue }
                    sb.append(c); i++
                }
                else -> { sb.append(c); i++ }
            }
        }
        return sb.toString()
    }
}
