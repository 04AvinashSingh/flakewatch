# 📁 ci-samples/ — Sample CI/CD Pipeline Integrations

This folder contains **ready-to-use** pipeline configuration files for all major CI/CD providers. Each file demonstrates how to integrate Flakewatch into a real CI/CD pipeline.

## Files

| File | CI Provider | Config File Location |
|---|---|---|
| `github-actions.yml` | GitHub Actions | `.github/workflows/test.yml` |
| `gitlab-ci.yml` | GitLab CI/CD | `.gitlab-ci.yml` |
| `Jenkinsfile` | Jenkins | `Jenkinsfile` |
| `report-to-flakewatch.sh` | Any (Universal) | Run after tests |

## Quick Start

### 1. Pick your CI provider
Copy the appropriate file to your project.

### 2. Set environment variables / secrets

| Variable | Description | Example |
|---|---|---|
| `FLAKEWATCH_URL` | Your Flakewatch server URL | `http://localhost:8080` |
| `FLAKEWATCH_API_KEY` | Your API key | `fw_live_abc123...` |

### 3. Push code
Your CI pipeline will automatically send test results to Flakewatch after every test run.

### 4. Check the Dashboard
Open your Flakewatch dashboard to see flaky tests detected across your pipeline runs.

## How It Works

```
Your Project Repo
    │
    ├── .github/workflows/test.yml  ← (or .gitlab-ci.yml, or Jenkinsfile)
    │       │
    │       ▼
    │   ┌──────────┐    ┌───────────┐    ┌─────────────┐
    │   │ Run Tests │───▶│ Send JSON │───▶│ Flakewatch  │
    │   │ (mvn/npm) │    │ via curl  │    │ /api/ingest │
    │   └──────────┘    └───────────┘    └─────────────┘
    │                                          │
    │                                          ▼
    │                                    ┌─────────────┐
    │                                    │ Dashboard    │
    │                                    │ Shows Flakes │
    │                                    └─────────────┘
```

## Testing Locally

You can test the universal shell script right now:

```bash
chmod +x ci-samples/report-to-flakewatch.sh
./ci-samples/report-to-flakewatch.sh
```

This will send sample test results to your local Flakewatch instance.
