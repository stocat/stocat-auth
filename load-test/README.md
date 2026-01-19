# Auth API Load Test

This folder contains a basic k6 load test for the auth-api module.

## Prerequisites
- auth-api running on localhost:8085 (default)
- k6 installed

## Run
```bash
k6 run load-test/auth-api-basic.js
```

## Config
- BASE_URL (default: http://localhost:8085)
- PASSWORD (default: P@ssw0rd!)

Example:
```bash
k6 run load-test/auth-api-basic.js -e BASE_URL=http://localhost:8085 -e PASSWORD='P@ssw0rd!'
```

## What it does
Each virtual user runs the following flow:
1) POST /auth/signup (unique email per VU/iteration)
2) POST /auth/login
3) GET /auth/errors

## Expected endpoints
- /auth/signup
- /auth/login
- /auth/errors
- /actuator/prometheus (for metrics)
