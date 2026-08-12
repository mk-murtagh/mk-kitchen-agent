package com.marykatekitchen.mykitchen_agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAIConfig {

    @Bean
    public RestClient azureOpenAIRestClient(
            @Value("${AZURE_OPENAI_ENDPOINT}") String endpoint,
            @Value("${AZURE_OPENAI_API_KEY}") String apiKey) {

        String baseUrl = endpoint.endsWith("/")
                ? endpoint + "openai/v1"
                : endpoint + "/openai/v1";

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("api-key", apiKey)
                .build();
    }
}