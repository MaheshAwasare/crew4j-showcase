# Crew4j Spring Boot Example

This project is a Spring Boot application that demonstrates the capabilities of the [crew4j](https://github.com/crew4j/crew4j) library. It provides a RESTful API for performing two main tasks: summarizing uploaded documents (RAG) and conducting internet searches to generate reports.

## Features

-   **Document Summarization (RAG):** Upload a PDF or plain text file to the `/crew4j/rag/summarize` endpoint to receive a concise summary of its content.
-   **Search and Report:** Provide a search query to the `/crew4f/searchAndReport` endpoint to get a report based on internet search results.

## Prerequisites

-   Java 17 or later
-   Maven
-   Environment variables for the following services:
    -   [Groq](https://wow.groq.com/): `GROQ_API_KEY`
    -   [Google Custom Search API](https://developers.google.com/custom-search/v1/overview): `GOOGLE_SEARCH_API_KEY` and `GOOGLE_SEARCH_CSE_ID`

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/crew4j/crew4j-spring-boot-example.git
    cd crew4j-spring-boot-example
    ```

2.  **Set up environment variables:**
    ```bash
    export GROQ_API_KEY="your_groq_api_key"
    export GOOGLE_SEARCH_API_KEY="your_google_search_api_key"
    export GOOGLE_SEARCH_CSE_ID="your_google_search_cse_id"
    ```

3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

The application will be available at `http://localhost:8080`.

## API Endpoints

### Document Summarization

-   **URL:** `/crew4j/rag/summarize`
-   **Method:** `POST`
-   **Description:** Summarizes the content of an uploaded file.
-   **Request:**
    -   `file`: A PDF or plain text file.
-   **Example using `curl`:**
    ```bash
    curl -X POST -F "file=@/path/to/your/document.pdf" http://localhost:8080/crew4j/rag/summarize
    ```

### Search and Report

-   **URL:** `/crew4j/searchAndReport`
-   **Method:** `GET`
-   **Description:** Performs an internet search and generates a report.
-   **Request:**
    -   `query`: The search query.
-   **Example using `curl`:**
    ```bash
    curl "http://localhost:8080/crew4j/searchAndReport?query=What+is+CrewAI"
    ```

## How It Works

This application uses `crew4j` to create and manage agents that perform specific tasks.

-   For document summarization, a `DocumentProcessor` agent processes the text, and a `Summarizer` agent generates the summary.
-   For search and reporting, a `SearchAgent` performs an internet search and compiles a report.

The agents are powered by the Groq LLM and leverage the Google Custom Search API for search capabilities.
