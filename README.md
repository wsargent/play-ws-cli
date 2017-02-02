# WS Cache Example  

This is a simple project that use Play WS Standalone with Caffiene as the JSR 107 cache implementation.

The main class starts up an actor that runs for 30 seconds, repeatedly querying a URL.  After the first query, subsequent queries are served from cache because the HTTP request has cache directives and an `Expires` header.

## Running

```
sbt run
```
