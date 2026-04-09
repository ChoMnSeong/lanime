package com.ensnif.lanime.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean("aniListWebClient")
    fun aniListWebClient(): WebClient = WebClient.builder()
        .baseUrl("https://graphql.anilist.co")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build()

    @Bean("jikanWebClient")
    fun jikanWebClient(): WebClient = WebClient.builder()
        .baseUrl("https://api.jikan.moe/v4")
        .defaultHeader("Accept", "application/json")
        .build()

    @Bean("kitsuWebClient")
    fun kitsuWebClient(): WebClient = WebClient.builder()
        .baseUrl("https://kitsu.app/api/edge")
        .defaultHeader("Accept", "application/vnd.api+json")
        .build()
}
