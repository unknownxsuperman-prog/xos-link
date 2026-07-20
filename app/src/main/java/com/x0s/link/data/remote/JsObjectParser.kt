package com.x0s.link.data.remote

/**
 * x0s.link ships its data as plain JS files, e.g.:
 *
 *   window.XOS_PROFILES = window.XOS_PROFILES || { nikhil: { userid:'nikhil', ... }, ... };
 *   window.XOS_COLLEGES = window.XOS_COLLEGES || [ { id:'rce', name:'...' }, ... ];
 *
 * This is *not* valid JSON (unquoted keys, single-quoted strings, trailing commas,
 * `|| {...}` fallback wrapper, `//` comments, trailing `;`). This object extracts the
 * literal assigned to a given `window.<NAME>` and rewrites it into strict JSON so it can
 * be parsed with Gson.
 *
 * This is a best-effort static-data extractor - it is NOT a JS engine. It works well for
 * plain data literals (strings, numbers, booleans, arrays, nested objects) which is what
 * profiles.js / colleges.js / Dishabase.js contain.
 */
object JsObjectParser {

    /**
     * Extracts the JS literal assigned to `window.<varName>` (or a bare `var/const/let varName =`)
     * from raw JS source, and converts it to valid JSON text.
     */
    fun extractAsJson(rawJs: String, varName: String): String? {
        val stripped = stripComments(rawJs)
        val literal = extractLiteral(stripped, varName) ?: return null
        return jsLiteralToJson(literal)
    }

    private fun stripComments(src: String): String {
        // Remove /* ... */ block comments and // line comments (naively, outside of strings).
        val sb = StringBuilder()
        var i = 0
        var inSingle = false
        var inDouble = false
        var inTemplate = false
        while (i < src.length) {
            val c = src[i]
            val next = if (i + 1 < src.length) src[i + 1] else '\u0000'
            if (!inSingle && !inDouble && !inTemplate && c == '/' && next == '/') {
                while (i < src.length && src[i] != '\n') i++
                continue
            }
            if (!inSingle && !inDouble && !inTemplate && c == '/' && next == '*') {
                i += 2
                while (i + 1 < src.length && !(src[i] == '*' && src[i + 1] == '/')) i++
                i += 2
                continue
            }
            if (!inDouble && !inTemplate && c == '\'' && (i == 0 || src[i - 1] != '\\')) inSingle = !inSingle
            if (!inSingle && !inTemplate && c == '"' && (i == 0 || src[i - 1] != '\\')) inDouble = !inDouble
            if (!inSingle && !inDouble && c == '`' && (i == 0 || src[i - 1] != '\\')) inTemplate = !inTemplate
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    private fun extractLiteral(src: String, varName: String): String? {
        // Look for "window.<varName> =" or "<varName> =" (var/const/let), then take the
        // first balanced {...} or [...] that follows (skipping a leading "window.X || " guard).
        val patterns = listOf(
            Regex("""window\.$varName\s*="""),
            Regex("""(?:var|let|const)\s+$varName\s*=""")
        )
        var startIdx = -1
        for (p in patterns) {
            val m = p.find(src)
            if (m != null) { startIdx = m.range.last + 1; break }
        }
        if (startIdx == -1) return null

        var i = startIdx
        // skip whitespace
        while (i < src.length && src[i].isWhitespace()) i++
        // skip a `window.X ||` guard if present, e.g. `window.XOS_PROFILES || {`
        val guard = Regex("""^window\.$varName\s*\|\|\s*""").find(src.substring(i))
        if (guard != null) i += guard.value.length

        if (i >= src.length || (src[i] != '{' && src[i] != '[')) return null
        val open = src[i]
        val close = if (open == '{') '}' else ']'
        var depth = 0
        var inSingle = false
        var inDouble = false
        val start = i
        while (i < src.length) {
            val c = src[i]
            if (!inDouble && c == '\'' && (i == 0 || src[i - 1] != '\\')) inSingle = !inSingle
            if (!inSingle && c == '"' && (i == 0 || src[i - 1] != '\\')) inDouble = !inDouble
            if (!inSingle && !inDouble) {
                if (c == open) depth++
                if (c == close) {
                    depth--
                    if (depth == 0) {
                        return src.substring(start, i + 1)
                    }
                }
            }
            i++
        }
        return null
    }

    /**
     * Converts a loose JS object/array literal into strict JSON:
     *  - quotes unquoted keys
     *  - converts single-quoted strings to double-quoted
     *  - removes trailing commas before `}` or `]`
     */
    fun jsLiteralToJson(literal: String): String {
        val sb = StringBuilder()
        var i = 0
        val n = literal.length
        while (i < n) {
            val c = literal[i]
            when {
                c == '\'' -> {
                    // single-quoted string -> double-quoted JSON string
                    val start = i + 1
                    var j = start
                    val buf = StringBuilder()
                    while (j < n && literal[j] != '\'') {
                        if (literal[j] == '\\' && j + 1 < n) {
                            buf.append(literal[j]); buf.append(literal[j + 1]); j += 2
                        } else if (literal[j] == '"') {
                            buf.append("\\\""); j++
                        } else {
                            buf.append(literal[j]); j++
                        }
                    }
                    sb.append('"').append(buf).append('"')
                    i = j + 1
                }
                c == '"' -> {
                    // already double-quoted string, copy through respecting escapes
                    sb.append(c)
                    var j = i + 1
                    while (j < n && literal[j] != '"') {
                        if (literal[j] == '\\' && j + 1 < n) { sb.append(literal[j]); sb.append(literal[j + 1]); j += 2 }
                        else { sb.append(literal[j]); j++ }
                    }
                    if (j < n) { sb.append('"'); j++ }
                    i = j
                }
                c == ',' -> {
                    // Trailing comma check: look ahead skipping whitespace for '}' or ']'
                    var j = i + 1
                    while (j < n && literal[j].isWhitespace()) j++
                    if (j < n && (literal[j] == '}' || literal[j] == ']')) {
                        // skip the comma entirely
                    } else {
                        sb.append(c)
                    }
                    i++
                }
                c == '{' || c == '[' -> { sb.append(c); i++ }
                c.isWhitespace() -> { sb.append(c); i++ }
                else -> {
                    // Could be an unquoted key, a bareword value (true/false/null), or a number.
                    if (c.isLetter() || c == '_' || c == '$') {
                        val start = i
                        var j = i
                        while (j < n && (literal[j].isLetterOrDigit() || literal[j] == '_' || literal[j] == '$')) j++
                        val word = literal.substring(start, j)
                        // find next non-space char to decide if this is a key (followed by ':')
                        var k = j
                        while (k < n && literal[k].isWhitespace()) k++
                        val followedByColon = k < n && literal[k] == ':'
                        if (followedByColon) {
                            sb.append('"').append(word).append('"')
                        } else if (word == "true" || word == "false" || word == "null") {
                            sb.append(word)
                        } else {
                            // Unknown bareword (e.g. a function ref) - stringify defensively.
                            sb.append('"').append(word).append('"')
                        }
                        i = j
                    } else {
                        sb.append(c)
                        i++
                    }
                }
            }
        }
        return sb.toString()
    }
}
