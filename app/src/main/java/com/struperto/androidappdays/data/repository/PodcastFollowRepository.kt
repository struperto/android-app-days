package com.struperto.androidappdays.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import java.time.Instant

data class PodcastEpisode(
    val title: String,
    val publishedAt: String?,
    val link: String?,
    val duration: String?,
)

interface PodcastFollowRepository {
    suspend fun loadEpisodes(feedUrl: String, limit: Int = 10): List<PodcastEpisode>
}

class RssPodcastFollowRepository : PodcastFollowRepository {
    override suspend fun loadEpisodes(feedUrl: String, limit: Int): List<PodcastEpisode> =
        withContext(Dispatchers.IO) {
            val episodes = mutableListOf<PodcastEpisode>()
            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                val connection = URL(feedUrl).openConnection()
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("User-Agent", "Days-App/1.0")
                parser.setInput(connection.getInputStream(), null)
                var eventType = parser.eventType
                var inItem = false
                var title: String? = null
                var pubDate: String? = null
                var link: String? = null
                var duration: String? = null
                while (eventType != XmlPullParser.END_DOCUMENT && episodes.size < limit) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "item", "entry" -> {
                                    inItem = true
                                    title = null; pubDate = null; link = null; duration = null
                                }
                                "title" -> if (inItem) title = parser.nextText()
                                "pubDate", "published", "updated" -> if (inItem) pubDate = parser.nextText()
                                "link" -> if (inItem) link = parser.nextText()
                                "duration" -> if (inItem) duration = parser.nextText()
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if ((parser.name == "item" || parser.name == "entry") && inItem) {
                                if (title != null) {
                                    episodes += PodcastEpisode(
                                        title = title,
                                        publishedAt = pubDate,
                                        link = link,
                                        duration = duration,
                                    )
                                }
                                inItem = false
                            }
                        }
                    }
                    eventType = parser.next()
                }
            } catch (_: Exception) {
                // Return whatever we parsed so far
            }
            episodes
        }
}
