package com.scrollguard.app.core.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * Fast Trie and HashSet engine for high-throughput URL inspection in AccessibilityService.
 */
class DomainTrie {
    private val exactDomains = HashSet<String>()
    private val root = TrieNode()
    private val restrictedKeywords = listOf(
        "porn", "xxx", "xvideos", "xnxx", "xhamster", "redtube", "youporn",
        "chaturbate", "stripchat", "hentai", "rule34", "erome", "fapello",
        "nsfw", "brazzers", "naughtyamerica", "bangbros", "adultwork",
        "camgirl", "cam4", "fetlife", "sexvid", "tube8"
    )

    private class TrieNode {
        val children = HashMap<Char, TrieNode>()
        var isEndOfWord = false
    }

    fun loadFromAssets(context: Context, filename: String = "adult_domains.txt") {
        try {
            context.assets.open(filename).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        val trimmed = line.trim().lowercase(Locale.ROOT)
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            addDomain(trimmed)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback default high-risk domains
            listOf(
                "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com",
                "redtube.com", "youporn.com", "chaturbate.com", "stripchat.com",
                "onlyfans.com", "brazzers.com", "rule34.xxx", "hentaihaven.xxx"
            ).forEach { addDomain(it) }
        }
    }

    fun addDomain(domain: String) {
        val cleanDomain = cleanUrl(domain)
        exactDomains.add(cleanDomain)

        // Also add reversed domain parts to Trie for suffix matching (.e.g, *.pornhub.com)
        var current = root
        for (char in cleanDomain.reversed()) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    /**
     * Checks if a raw URL string or search query matches an adult domain or keyword.
     */
    fun isRestricted(rawUrlOrText: String): Boolean {
        if (rawUrlOrText.isBlank()) return false
        val clean = cleanUrl(rawUrlOrText)

        // 1. Fast exact check
        if (exactDomains.contains(clean)) return true

        // 2. Check suffix matching (subdomains like m.pornhub.com)
        var current = root
        for (char in clean.reversed()) {
            val next = current.children[char] ?: break
            if (next.isEndOfWord) {
                // Matched a domain suffix
                return true
            }
            current = next
        }

        // 3. Keyword check in URL or search query
        for (keyword in restrictedKeywords) {
            if (clean.contains(keyword)) {
                return true
            }
        }

        return false
    }

    private fun cleanUrl(url: String): String {
        var clean = url.trim().lowercase(Locale.ROOT)
        if (clean.startsWith("https://")) clean = clean.substring(8)
        if (clean.startsWith("http://")) clean = clean.substring(7)
        if (clean.startsWith("www.")) clean = clean.substring(4)
        val slashIndex = clean.indexOf('/')
        if (slashIndex != -1) {
            clean = clean.substring(0, slashIndex)
        }
        val portIndex = clean.indexOf(':')
        if (portIndex != -1) {
            clean = clean.substring(0, portIndex)
        }
        return clean
    }
}
