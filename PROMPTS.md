Paste these into a fresh session **one at a time, in order**. Wait for each one to finish (and answer any questions it
asks) before sending the next. Together they hold the full spec, so no other input is needed.

---

## 1. Ground rules

```
before we start, these rules apply to everything in this project:
- do not assume anything. if something isn't clear, ask me first. if there are options, give your recommendation and wait.
- don't add anything I didn't ask for. make it as simple as possible.
- never commit or push unless I explicitly ask.
- everything runs in docker. nothing should depend on what's installed on my machine.
- latest stable versions of everything, except java stays at 17. use gradle (wrapper), not maven.
- never log the api key, loan amounts or rejected input. no real secrets in any file.
- trim every input, in the frontend and the backend. never trust input: the backend sanitizes everything with an
  allow-list and rejects anything else.
- after every change, run the build with the tests in docker and tell me the result. keep the readme updated.
just confirm you understood, don't create anything yet.
```

## 2. The requirement

```
I need a working "commission quote app": java spring boot backend + react frontend.

the backend calls a third party vendor "commission quote api". create a mock of it, in the same spring boot app under /vendor.
- request: loanAmount, loanTermInMonths, riskBand (LOW / MEDIUM / HIGH)
- response: quoteId, commissionRate, totalCommission
- needs an "api-key" header, rejects invalid keys with 401
- randomly throws errors (20% of requests)

ui: a form (loan amount, loan term, risk band), a "Generate Quote" button, the result, a loading state and error messages.

don't write any code yet. tell me what you understood and ask whatever you need. do not assume.
```

## 3. Business rules

```
- rate: LOW 1.00%, MEDIUM 1.50%, HIGH 2.00%, +0.25% if the term is over 60 months.
- total = amount x rate / 100, rounded half up to 2 decimals. BigDecimal for money.
- amount 1,000 to 5,000,000 (max 2 decimals, AUD shown in the label). term 1 to 360 whole months. quoteId is a UUID.
- test scenarios: amount 1001 -> vendor 500 -> user gets 502. amount 1002 -> vendor waits 10s, our timeout is 5s -> 504.
- rates, scenarios, failure rate, timeouts and the api key come from application.yml, not code.
any questions ? do not assume.
```

## 4. Project setup

```
ok. set up the project: backend/ (spring boot, java 17, gradle wrapper, versions in gradle.properties) and frontend/
(react + vite, vitest + testing library, MUI for the ui). make sure it builds before adding code.
ask if you have any queries. do not assume.
```

## 5. Backend

```
write the backend. keep it modular: common (constants, error handling, logging, sanitizing, utils), quote (our api:
controller -> service -> vendor client) and vendor (the mock).
- the api key stays on the server. the mock checks it in a filter, in constant time.
- sanitize input while the json is read: number fields only digits and ".", risk band only the exact values.
  anything else -> 400 "Request contains invalid characters", without echoing or logging the input.
- vendor timeout -> 504, vendor error -> 502. a 200 with an empty response ({}) or an invalid quote is an error too.
- every error returns { code, message, fieldErrors, requestId }. code is a numbered error id (CQ-1xxx invalid request,
  CQ-2xxx vendor, CQ-3xxx mock, CQ-9xxx unexpected) the frontend can react to.
- logging: console only, no log file. a secure logger (strips line breaks) and one clear format per line:
  timestamp, level, request id, error code, logger, message. the request id goes back in the response and to the vendor.
- the mock vendor is off by default and turned on by the docker compose files. the app refuses to start without an api key.
- security headers on every response, and a health endpoint for docker.
add comments where needed. ask if you have any queries. do not assume.
```

## 6. Frontend

```
write the frontend with MUI. keep it modular: api (the only code that calls the backend), validation, components,
utils, constants.
- validate on submit with a message under each field (trimmed, numbers only, ranges). nothing is sent until valid.
- the button shows a spinner and is disabled while loading. old results are cleared on a new request.
- show errors in an alert with a reference line (error code + request id). for an empty response show: "The commission
  API returned an empty response, so a quote could not be generated for the given input. Please try again."
ask if you have any queries. do not assume.
```

## 7. Tests

```
add unit and integration tests for both apps, including the edge cases (invalid input, script tags, whitespace,
vendor error, timeout, empty response, wrong api key). tests run in parallel as part of the build, and a failure
fails the build. show me the counts. ask if you have any queries. do not assume.
```

## 8. Docker

```
- production: one container (spring boot serves the react build, the api and the mock), multi stage build that runs
  all tests, non root user, health check.
- dev mode: separate compose file with hot reload for frontend (vite) and backend (devtools).
- versions and ports in a root .env.
run both and confirm they work. ask if you have any queries. do not assume.
```

## 9. Readme

```
write a short readme: how to run (production and dev mode), how to run the tests, configuration, business rules,
test scenarios, api and error codes, project structure, a mermaid sequence diagram of a quote request, logging,
design decisions, future scope, and an "AI usage" section that mentions this prompts file. no unnecessary details.
ask if you have any queries. do not assume.
```

## 10. Final check

```
build everything with all tests, start production mode and go through the scenarios (normal quote, 1001, 1002,
invalid input, script tags, whitespace, wrong api key). tell me the results. don't commit.
if anything is unclear, ask. do not assume.
```
