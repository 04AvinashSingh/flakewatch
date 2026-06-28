import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-integrate',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="integrate-container fade-in">

      <!-- Step 1: API Key -->
      <div class="card step-card">
        <div class="step-header">
          <span class="step-number">1</span>
          <div>
            <h2>Get Your API Key</h2>
            <p class="step-desc">This key authenticates your CI/CD pipeline with Flakewatch.</p>
          </div>
        </div>

        <div class="api-key-box" *ngIf="!generatedKey">
          <button class="btn btn-primary btn-lg" (click)="generateKey()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 8px;">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
            </svg>
            Generate API Key
          </button>
        </div>

        <div class="api-key-result" *ngIf="generatedKey">
          <div class="key-display">
            <code>{{ generatedKey }}</code>
            <button class="btn btn-copy" (click)="copyToClipboard(generatedKey)" [class.copied]="copiedItem === 'key'">
              {{ copiedItem === 'key' ? '✓ Copied!' : 'Copy' }}
            </button>
          </div>
          <div class="key-warning">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
            Store this key securely. Add it as a secret in your CI/CD platform (e.g., GitHub Secrets).
          </div>
        </div>
      </div>

      <!-- Step 2: Choose CI Provider -->
      <div class="card step-card">
        <div class="step-header">
          <span class="step-number">2</span>
          <div>
            <h2>Choose Your CI/CD Provider</h2>
            <p class="step-desc">Select your platform to get ready-to-use integration code.</p>
          </div>
        </div>

        <div class="provider-grid">
          <button class="provider-card" [class.selected]="selectedProvider === 'github'" (click)="selectProvider('github')">
            <div class="provider-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
            </div>
            <span class="provider-name">GitHub Actions</span>
          </button>

          <button class="provider-card" [class.selected]="selectedProvider === 'gitlab'" (click)="selectProvider('gitlab')">
            <div class="provider-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor"><path d="M22.65 14.39L12 22.13 1.35 14.39a.84.84 0 01-.3-.94l1.22-3.78 2.44-7.51A.42.42 0 014.82 2a.43.43 0 01.58 0 .42.42 0 01.11.18l2.44 7.49h8.1l2.44-7.51A.42.42 0 0118.6 2a.43.43 0 01.58 0 .42.42 0 01.11.18l2.44 7.51L23 13.45a.84.84 0 01-.35.94z"/></svg>
            </div>
            <span class="provider-name">GitLab CI</span>
          </button>

          <button class="provider-card" [class.selected]="selectedProvider === 'jenkins'" (click)="selectProvider('jenkins')">
            <div class="provider-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
            </div>
            <span class="provider-name">Jenkins</span>
          </button>

          <button class="provider-card" [class.selected]="selectedProvider === 'curl'" (click)="selectProvider('curl')">
            <div class="provider-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
            </div>
            <span class="provider-name">curl / Shell</span>
          </button>
        </div>
      </div>

      <!-- Step 3: Copy Code -->
      <div class="card step-card" *ngIf="selectedProvider">
        <div class="step-header">
          <span class="step-number">3</span>
          <div>
            <h2>Add to Your Pipeline</h2>
            <p class="step-desc">Copy this code and add it to your CI/CD configuration file.</p>
          </div>
        </div>

        <div class="code-info" *ngIf="selectedProvider === 'github'">
          <span class="file-badge">📄 .github/workflows/test.yml</span>
          <p>Add this step <strong>after</strong> your test step. The <code>if: always()</code> ensures results are sent even when tests fail.</p>
        </div>
        <div class="code-info" *ngIf="selectedProvider === 'gitlab'">
          <span class="file-badge">📄 .gitlab-ci.yml</span>
          <p>Add the <code>after_script</code> block to your test job. It runs even if tests fail.</p>
        </div>
        <div class="code-info" *ngIf="selectedProvider === 'jenkins'">
          <span class="file-badge">📄 Jenkinsfile</span>
          <p>Add the <code>post → always</code> block after your test stage.</p>
        </div>
        <div class="code-info" *ngIf="selectedProvider === 'curl'">
          <span class="file-badge">📄 Terminal / Shell Script</span>
          <p>Run this command after your tests complete. Works with any CI system.</p>
        </div>

        <div class="code-block-wrapper">
          <div class="code-toolbar">
            <span class="code-lang">{{ getCodeLang() }}</span>
            <button class="btn btn-copy" (click)="copyToClipboard(getSnippet()); copiedItem = 'code'" [class.copied]="copiedItem === 'code'">
              {{ copiedItem === 'code' ? '✓ Copied!' : 'Copy Code' }}
            </button>
          </div>
          <pre class="code-block"><code>{{ getSnippet() }}</code></pre>
        </div>
      </div>

      <!-- Step 4: Verify -->
      <div class="card step-card" *ngIf="selectedProvider">
        <div class="step-header">
          <span class="step-number">4</span>
          <div>
            <h2>Verify Integration</h2>
            <p class="step-desc">Send a test webhook to confirm everything is connected.</p>
          </div>
        </div>

        <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
          <button class="btn btn-primary btn-lg" (click)="sendTestWebhook()" [disabled]="isVerifying">
            <svg *ngIf="!isVerifying" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 8px;">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
            {{ isVerifying ? 'Sending...' : 'Send Test Webhook' }}
          </button>

          <div class="verify-result success" *ngIf="verifyResult === 'success'">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
            </svg>
            Connected! Your pipeline can now send results to Flakewatch.
          </div>

          <div class="verify-result error" *ngIf="verifyResult === 'error'">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
            Connection failed. Make sure the backend is running on port 8080.
          </div>
        </div>
      </div>

      <!-- Webhook Payload Reference -->
      <div class="card step-card" *ngIf="selectedProvider">
        <div class="step-header">
          <span class="step-number">📖</span>
          <div>
            <h2>Payload Reference</h2>
            <p class="step-desc">Full JSON schema for the webhook payload.</p>
          </div>
        </div>

        <div class="code-block-wrapper">
          <div class="code-toolbar">
            <span class="code-lang">JSON</span>
          </div>
          <pre class="code-block"><code>{{ payloadReference }}</code></pre>
        </div>

        <div class="field-table">
          <div class="field-row header">
            <span>Field</span><span>Required</span><span>Description</span>
          </div>
          <div class="field-row"><span><code>eventId</code></span><span>✅</span><span>Unique ID per webhook call (UUID). Prevents duplicates.</span></div>
          <div class="field-row"><span><code>commitHash</code></span><span>✅</span><span>Git commit SHA that was tested</span></div>
          <div class="field-row"><span><code>branchName</code></span><span>✅</span><span>Branch name (e.g., main, feature/login)</span></div>
          <div class="field-row"><span><code>runnerId</code></span><span>✅</span><span>CI runner identifier</span></div>
          <div class="field-row"><span><code>results[].testIdentifier</code></span><span>✅</span><span>Fully qualified test name</span></div>
          <div class="field-row"><span><code>results[].suiteName</code></span><span>✅</span><span>Test class/suite name</span></div>
          <div class="field-row"><span><code>results[].status</code></span><span>✅</span><span>PASS, FAIL, or SKIPPED</span></div>
          <div class="field-row"><span><code>results[].durationMs</code></span><span>❌</span><span>Test duration in milliseconds</span></div>
          <div class="field-row"><span><code>results[].errorMessage</code></span><span>❌</span><span>Error message if failed</span></div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .integrate-container {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .step-card {
      padding: 32px;
    }

    .step-header {
      display: flex;
      align-items: flex-start;
      gap: 20px;
      margin-bottom: 24px;
    }

    .step-number {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 44px;
      height: 44px;
      min-width: 44px;
      border-radius: 12px;
      background: var(--accent-color);
      color: white;
      font-weight: 700;
      font-size: 1.1rem;
    }

    .step-header h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary);
    }

    .step-desc {
      color: var(--text-secondary);
      margin: 4px 0 0 0;
      font-size: 0.9rem;
    }

    .api-key-box {
      display: flex;
      justify-content: center;
      padding: 20px 0;
    }

    .btn-lg {
      padding: 14px 28px;
      font-size: 1rem;
      display: flex;
      align-items: center;
    }

    .api-key-result {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .key-display {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      background: rgba(25, 135, 84, 0.08);
      border: 1px solid rgba(25, 135, 84, 0.2);
      border-radius: 8px;
    }

    .key-display code {
      flex: 1;
      font-size: 1rem;
      color: var(--success-color);
      font-family: monospace;
    }

    .key-warning {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: rgba(255, 193, 7, 0.1);
      border: 1px solid rgba(255, 193, 7, 0.2);
      border-radius: 8px;
      color: var(--warning-color);
      font-size: 0.85rem;
    }

    @media (prefers-color-scheme: light) {
      .key-warning {
        color: #856404;
      }
    }

    .btn-copy {
      padding: 6px 12px;
      background: var(--bg-color);
      border: 1px solid var(--card-border);
      color: var(--text-primary);
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.85rem;
      transition: all 0.2s;
      white-space: nowrap;
    }

    .btn-copy:hover {
      background: var(--card-border);
    }

    .btn-copy.copied {
      background: rgba(25, 135, 84, 0.1);
      border-color: rgba(25, 135, 84, 0.3);
      color: var(--success-color);
    }

    .provider-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 16px;
    }

    .provider-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 24px 16px;
      background: var(--bg-color);
      border: 1px solid var(--card-border);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      color: var(--text-secondary);
    }

    .provider-card:hover {
      border-color: var(--accent-color);
      color: var(--text-primary);
    }

    .provider-card.selected {
      border-color: var(--accent-color);
      background: rgba(13, 110, 253, 0.05);
      color: var(--accent-color);
    }

    .provider-icon {
      opacity: 0.8;
    }

    .provider-card.selected .provider-icon,
    .provider-card:hover .provider-icon {
      opacity: 1;
    }

    .provider-name {
      font-weight: 600;
      font-size: 0.9rem;
    }

    .code-info {
      margin-bottom: 16px;
      padding: 16px;
      background: rgba(13, 110, 253, 0.05);
      border-radius: 8px;
      border-left: 3px solid var(--accent-color);
    }

    .code-info p {
      margin: 8px 0 0 0;
      color: var(--text-secondary);
      font-size: 0.9rem;
    }

    .code-info code {
      background: var(--bg-color);
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 0.85rem;
      color: var(--text-primary);
      border: 1px solid var(--card-border);
    }

    .file-badge {
      font-weight: 600;
      color: var(--accent-color);
      font-size: 0.95rem;
    }

    .code-block-wrapper {
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid var(--card-border);
    }

    .code-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 16px;
      background: var(--bg-color);
      border-bottom: 1px solid var(--card-border);
    }

    .code-lang {
      font-size: 0.8rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 600;
    }

    .code-block {
      margin: 0;
      padding: 20px;
      background: var(--card-bg);
      overflow-x: auto;
      font-size: 0.85rem;
      line-height: 1.5;
      color: var(--text-primary);
      font-family: monospace;
    }

    .verify-result {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 20px;
      border-radius: 6px;
      font-weight: 500;
      font-size: 0.9rem;
    }

    .verify-result.success {
      background: rgba(25, 135, 84, 0.1);
      border: 1px solid rgba(25, 135, 84, 0.25);
      color: var(--success-color);
    }

    .verify-result.error {
      background: rgba(220, 53, 69, 0.1);
      border: 1px solid rgba(220, 53, 69, 0.25);
      color: var(--danger-color);
    }

    .field-table {
      margin-top: 20px;
      border: 1px solid var(--card-border);
      border-radius: 8px;
      overflow: hidden;
    }

    .field-row {
      display: grid;
      grid-template-columns: 1fr 80px 2fr;
      padding: 12px 16px;
      border-bottom: 1px solid var(--card-border);
      font-size: 0.85rem;
      color: var(--text-secondary);
    }

    .field-row:last-child {
      border-bottom: none;
    }

    .field-row.header {
      background: var(--bg-color);
      font-weight: 600;
      color: var(--text-primary);
      text-transform: uppercase;
      font-size: 0.75rem;
      letter-spacing: 0.5px;
    }

    .field-row code {
      color: var(--accent-color);
      font-family: monospace;
      font-size: 0.8rem;
    }
  `]
})
export class IntegrateComponent {
  private http = inject(HttpClient);

  generatedKey = '';
  selectedProvider: string | null = null;
  copiedItem: string | null = null;
  isVerifying = false;
  verifyResult: 'success' | 'error' | null = null;

  payloadReference = `{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "commitHash": "a1b2c3d4e5f6",
  "branchName": "main",
  "runnerId": "github-actions-runner-1",
  "results": [
    {
      "testIdentifier": "com.myapp.UserTest.testLogin",
      "suiteName": "UserTest",
      "status": "PASS",
      "durationMs": 1200,
      "errorMessage": null
    },
    {
      "testIdentifier": "com.myapp.UserTest.testSignup",
      "suiteName": "UserTest",
      "status": "FAIL",
      "durationMs": 3400,
      "errorMessage": "Expected 200 but got 500"
    }
  ]
}`;

  generateKey() {
    // Generate a realistic-looking API key
    const chars = 'abcdef0123456789';
    let key = 'fw_live_';
    for (let i = 0; i < 32; i++) {
      key += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    this.generatedKey = key;
  }

  selectProvider(provider: string) {
    this.selectedProvider = provider;
    this.copiedItem = null;
  }

  getCodeLang(): string {
    switch (this.selectedProvider) {
      case 'github': return 'YAML';
      case 'gitlab': return 'YAML';
      case 'jenkins': return 'Groovy';
      case 'curl': return 'Bash';
      default: return '';
    }
  }

  getSnippet(): string {
    const apiKey = this.generatedKey || 'YOUR_API_KEY_HERE';
    const host = 'http://YOUR_FLAKEWATCH_HOST:8080';

    switch (this.selectedProvider) {
      case 'github':
        return `# Add this step AFTER your test step in your workflow file
# File: .github/workflows/test.yml

- name: Report Test Results to Flakewatch
  if: always()
  run: |
    curl -X POST ${host}/api/v1/ingest/test-results \\
      -H "Content-Type: application/json" \\
      -H "X-API-KEY: \${{ secrets.FLAKEWATCH_API_KEY }}" \\
      -d '{
        "eventId": "\${{ github.run_id }}-\${{ github.run_attempt }}",
        "commitHash": "\${{ github.sha }}",
        "branchName": "\${{ github.ref_name }}",
        "runnerId": "github-actions",
        "results": [
          {
            "testIdentifier": "com.example.MyTest.testMethod",
            "suiteName": "MyTest",
            "status": "PASS",
            "durationMs": 1200
          }
        ]
      }'

# Don't forget to add FLAKEWATCH_API_KEY to:
# Repository → Settings → Secrets → Actions
# Value: ${apiKey}`;

      case 'gitlab':
        return `# Add this to your test job in .gitlab-ci.yml

test:
  stage: test
  script:
    - mvn test    # or: npm test, pytest, etc.
  after_script:
    - |
      curl -X POST ${host}/api/v1/ingest/test-results \\
        -H "Content-Type: application/json" \\
        -H "X-API-KEY: $FLAKEWATCH_API_KEY" \\
        -d "{
          \\"eventId\\": \\"$CI_PIPELINE_ID-$CI_JOB_ID\\",
          \\"commitHash\\": \\"$CI_COMMIT_SHA\\",
          \\"branchName\\": \\"$CI_COMMIT_BRANCH\\",
          \\"runnerId\\": \\"gitlab-runner-$CI_RUNNER_ID\\",
          \\"results\\": [{
            \\"testIdentifier\\": \\"com.example.MyTest.testMethod\\",
            \\"suiteName\\": \\"MyTest\\",
            \\"status\\": \\"PASS\\",
            \\"durationMs\\": 1200
          }]
        }"

# Add FLAKEWATCH_API_KEY in:
# Settings → CI/CD → Variables
# Value: ${apiKey}`;

      case 'jenkins':
        return `// Add this to your Jenkinsfile

pipeline {
    agent any
    environment {
        FLAKEWATCH_API_KEY = credentials('flakewatch-api-key')
    }
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'   // or: npm test, pytest, etc.
            }
            post {
                always {
                    sh """
                        curl -X POST ${host}/api/v1/ingest/test-results \\
                          -H "Content-Type: application/json" \\
                          -H "X-API-KEY: \${FLAKEWATCH_API_KEY}" \\
                          -d '{
                            "eventId": "\${BUILD_ID}-\${BUILD_NUMBER}",
                            "commitHash": "\${GIT_COMMIT}",
                            "branchName": "\${GIT_BRANCH}",
                            "runnerId": "jenkins-\${NODE_NAME}",
                            "results": [{
                              "testIdentifier": "com.example.Test.run",
                              "suiteName": "ExampleSuite",
                              "status": "PASS",
                              "durationMs": 300
                            }]
                          }'
                    """
                }
            }
        }
    }
}

// Add 'flakewatch-api-key' in:
// Jenkins → Credentials → Global
// Value: ${apiKey}`;

      case 'curl':
        return `#!/bin/bash
# Run this after your tests complete.
# Works with any CI system.

FLAKEWATCH_URL="${host}"
API_KEY="${apiKey}"

curl -X POST "$FLAKEWATCH_URL/api/v1/ingest/test-results" \\
  -H "Content-Type: application/json" \\
  -H "X-API-KEY: $API_KEY" \\
  -d "{
    \\"eventId\\": \\"$(uuidgen)\\",
    \\"commitHash\\": \\"$(git rev-parse HEAD)\\",
    \\"branchName\\": \\"$(git branch --show-current)\\",
    \\"runnerId\\": \\"$(hostname)\\",
    \\"results\\": [
      {
        \\"testIdentifier\\": \\"com.example.MyTest.testMethod\\",
        \\"suiteName\\": \\"MyTest\\",
        \\"status\\": \\"PASS\\",
        \\"durationMs\\": 1200
      }
    ]
  }"

echo "✅ Results sent to Flakewatch!"`;

      default:
        return '';
    }
  }

  copyToClipboard(text: string) {
    navigator.clipboard.writeText(text);
    setTimeout(() => this.copiedItem = null, 2000);
  }

  sendTestWebhook() {
    this.isVerifying = true;
    this.verifyResult = null;

    // Always use the real test key for verification — generated keys
    // would need to be added to the backend's application.yml first
    const apiKey = 'fw_test_1234567890abcdef';
    const headers = {
      'Content-Type': 'application/json',
      'X-API-KEY': apiKey
    };

    const payload = {
      eventId: crypto.randomUUID(),
      commitHash: 'verify-' + Math.random().toString(36).substring(7),
      branchName: 'integration-test',
      runnerId: 'flakewatch-verify',
      results: [
        {
          testIdentifier: 'com.verify.IntegrationCheck.testConnection',
          suiteName: 'IntegrationCheck',
          status: 'PASS',
          durationMs: 100
        }
      ]
    };

    this.http.post('/api/v1/ingest/test-results', payload, { headers }).subscribe({
      next: () => {
        this.isVerifying = false;
        this.verifyResult = 'success';
      },
      error: () => {
        this.isVerifying = false;
        this.verifyResult = 'error';
      }
    });
  }
}
