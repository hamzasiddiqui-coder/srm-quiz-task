# SRM Quiz Task Solution

## Approach

- Called API 10 times (poll 0–9)
- Added 5 second delay between requests
- Deduplicated using (roundId + participant)
- Aggregated scores
- Sorted leaderboard
- Submitted final result

## Tech Used
- Java
- HttpClient
- JSON Parsing

## How to Run
1. Add your regNo
2. Run Main.java

Handled idempotency and duplicate API responses using HashSet for consistent aggregation.
