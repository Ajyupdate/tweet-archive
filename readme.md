### Version 1(01, June 2026) – Archive Parsing Foundation

* Built the archive ingestion pipeline: file reading → wrapper stripping → JSON parsing.
* Converted Twitter archive entries into `Tweet` domain objects via Jackson deserialization.
* Added custom exception handling for archive read and parse failures.
* Implemented unit/integration tests for valid archives, malformed wrappers, and invalid JSON inputs.

### Layer 2: Asynchronous Gemini Audit Service 22 June 2026

* Implemented `GeminiService` with Spring's `@Async` support to evaluate tweets concurrently using the Gemini API.
* Configured a dedicated `ThreadPoolTaskExecutor` for controlled asynchronous processing and future batch execution.
* Added prompt generation, Gemini request/response models, and automatic mapping of AI responses into `AuditResult` objects.
* Implemented robust error handling for missing API keys, empty/blocked responses, malformed JSON, and API failures.
