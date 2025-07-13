package com.crew4j.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import com.javaagentai.aiagents.core.BasicAgent;
import com.javaagentai.aiagents.llm.LLMClient;
import com.javaagentai.aiagents.llm.GroqClient;
import com.javaagentai.aiagents.memory.ShortTermMemory;
import com.javaagentai.aiagents.tools.SearchTool;
import com.javaagentai.aiagents.tools.Tool;

import java.util.List;
import java.util.ArrayList;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration.class})
public class Crew4jSpringBootExampleApplication {

    

    public static void main(String[] args) {
        SpringApplication.run(Crew4jSpringBootExampleApplication.class, args);
    }

    @Bean
    public BasicAgent exampleAgent() {
        String googleSearchCseId = System.getenv("GOOGLE_SEARCH_CSE_ID");
        String googleSearchApiKey = System.getenv("GOOGLE_SEARCH_API_KEY");

        LLMClient llmClient = new GroqClient(System.getenv("GROQ_API_KEY"), "gemma2-9b-it");
        List<Tool> tools = new ArrayList<>();
        if (googleSearchCseId != null && !googleSearchCseId.isEmpty() && googleSearchApiKey != null && !googleSearchApiKey.isEmpty()) {
            tools.add(new SearchTool(googleSearchCseId, googleSearchApiKey));
        }

        return BasicAgent.builder()
                .name("ExampleAgent")
                .role("Performs example tasks using search")
                .llmClient(llmClient)
                .memory(new ShortTermMemory(1000))
                .tools(tools)
                .googleSearchCseId(googleSearchCseId)
                .googleSearchApiKey(googleSearchApiKey)
                .build();
    }

}
