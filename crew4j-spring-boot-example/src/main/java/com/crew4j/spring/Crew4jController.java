package com.crew4j.spring;

import com.javaagentai.aiagents.core.Agent;
import com.javaagentai.aiagents.core.BasicAgent;
import com.javaagentai.aiagents.core.Crew;
import com.javaagentai.aiagents.core.ProcessStrategy;
import com.javaagentai.aiagents.core.Task;
import com.javaagentai.aiagents.llm.GroqClient;
import com.javaagentai.aiagents.llm.LLMClient;

import com.javaagentai.aiagents.memory.ShortTermMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
public class Crew4jController {

    private String getFileContent(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IOException("Could not determine file type.");
        }

        if (contentType.equals("application/pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                return pdfStripper.getText(document);
            }
        } else if (contentType.equals("text/plain")) {
            return new String(file.getBytes());
        } else {
            throw new IOException("Unsupported file type: " + contentType);
        }
    }

    

    @PostMapping("/crew4j/rag/summarize")
    public String summarizeDocument(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        String fileContent = getFileContent(file);
        System.out.println("FILE CONTENTS:\n"+fileContent);
        String googleSearchCseId = System.getenv("GOOGLE_SEARCH_CSE_ID");
        String googleSearchApiKey = System.getenv("GOOGLE_SEARCH_API_KEY");

        String groqApiKey = System.getenv("GROQ_API_KEY");
        LLMClient groqClient = new GroqClient(groqApiKey, "gemma2-9b-it");

        Agent documentProcessor = BasicAgent.builder()
                .name("DocumentProcessor")
                .role("DocumentProcessor")
                .llmClient(groqClient)
                .memory(new ShortTermMemory(1000))
                .build();

        Agent summarizer = BasicAgent.builder()
                .name("Summarizer")
                .role("Generates concise summaries from provided text")
                .llmClient(groqClient)
                .memory(new ShortTermMemory(1000))
                .build();

        Task processDocumentTask = Task.builder()
                .description("Process the following document content:")
                .input(Collections.singletonMap("document_content", fileContent))
                .assignedAgent(documentProcessor)
                .build();

        Task summarizeTask = Task.builder()
                .description("Summarize the processed document content.")
                .assignedAgent(summarizer)
                .build();

        Crew crew = Crew.builder()
                .agents(Arrays.asList(documentProcessor, summarizer))
                .processStrategy(ProcessStrategy.SEQUENTIAL)
                .build();

        // Execute the first task, then pass its result as input to the second task
        CompletableFuture<String> result = crew.execute(processDocumentTask)
                .thenCompose(processedContent -> {
                    summarizeTask.setInput(Collections.singletonMap("processed_content", (Object) processedContent));
                    return crew.execute(summarizeTask);
                });

        return result.get();
    }

    @GetMapping("/crew4j/searchAndReport")
    public String searchAndReport(@RequestParam String query) throws Exception {
        String googleSearchCseId = System.getenv("GOOGLE_SEARCH_CSE_ID");
        String googleSearchApiKey = System.getenv("GOOGLE_SEARCH_API_KEY");

        if (googleSearchCseId == null || googleSearchCseId.isEmpty() || googleSearchApiKey == null || googleSearchApiKey.isEmpty()) {
            return "Google Search CSE ID and API Key environment variables are not set. Please set GOOGLE_SEARCH_CSE_ID and GOOGLE_SEARCH_API_KEY.";
        }

        String groqApiKey = System.getenv("GROQ_API_KEY");
        LLMClient groqClient = new GroqClient(groqApiKey, "gemma2-9b-it");

        Agent searchAgent = BasicAgent.builder()
                .name("SearchAgent")
                .role("Performs internet searches and summarizes findings")
                .llmClient(groqClient)
                .memory(new ShortTermMemory(1000))
                .googleSearchCseId(googleSearchCseId)
                .googleSearchApiKey(googleSearchApiKey)
                .build();

        Task searchTask = Task.builder()
                .description("Search the internet for '" + query + "' and provide a concise summary of the top results.")
                .input(Collections.singletonMap("search_query", query))
                .assignedAgent(searchAgent)
                .build();

        Crew crew = Crew.builder()
                .agents(Arrays.asList(searchAgent))
                .processStrategy(ProcessStrategy.SEQUENTIAL)
                .build();

        CompletableFuture<String> result = crew.execute(searchTask);

        return result.get();
    }
}
