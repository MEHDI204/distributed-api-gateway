# Distributed API Gateway & Shared-State Rate Limiter

A high-performance, distributed API Gateway built with **Spring Boot** and **Java** that features real-time, synchronized rate limiting across multiple server nodes using **Valkey/Redis** as a shared memory store. 

The architecture is designed to prevent service abuse while maintaining full system visibility through a custom built-in observability and monitoring pipeline.

## Key Features

* **Distributed Architecture:** Multiple gateway instances can run concurrently (e.g., port `8080` and `8081`) to share traffic.
* **Shared-State Rate Limiting:** Utilizes a centralized Valkey data store to ensure a user's rate limit is synchronized globally, preventing users from bypassing limits by hitting different gateway nodes.
* **Dynamic Request Routing:** Transparently forwards authenticated traffic to downstream backend components based on configurable properties.
* **Production Observability (Phase 6):** * **Live Metrics Dashboard:** Exposes a clean json endpoint (`/gateway/metrics`) tracking system health, total requests, and block-rate percentages.
    * **Error Isolation:** Automatically catches and counts internal network/application exceptions explicitly, separating server errors from standard rate-limiting blocks (`429`).
    * **High-Performance Logging:** Intercepts traffic at the outermost layer (`@Order(1)`) to capture precision latency metrics for every single lifecycle event.
 
## Load Test

Load test: 499 requests, 40 concurrent, single API key
Results:   76 allowed (200) · 423 blocked (429) · 0 errors
Throughput: 267 req/sec · P50: 137ms · P95: 246ms · P99: 285ms
Block rate: 84.6% — consistent with token bucket config (MAX_TOKENS=5, refill 1/10s)

## Tech Stack

* **Backend Framework:** Spring Boot (Java)
* **Data Store:** Valkey / Redis (Distributed caching & shared token storage)
* **Testing Tools:** Curl, CLI

## Observability Snapshot

The gateway features a lightweight internal tracking service using thread-safe atomic counters (`AtomicLong`). When calling the administrative metrics endpoint, it yields real-time production analytics:

```json
{
  "totalRequests": 12,
  "blockedRequests": 3,
  "criticalErrors": 0,
  "blockRatePercentage": "25.00%",
  "statusCodes": {
    "200": 8,
    "401": 1,
    "429": 3
  }
}
