### Version 1 – Archive Parsing Foundation

* Built the archive ingestion pipeline: file reading → wrapper stripping → JSON parsing.
* Converted Twitter archive entries into `Tweet` domain objects via Jackson deserialization.
* Added custom exception handling for archive read and parse failures.
* Implemented unit/integration tests for valid archives, malformed wrappers, and invalid JSON inputs.
