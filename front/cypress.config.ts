import { defineConfig } from 'cypress';
import coverageTask from 'cypress-monocart-coverage';
import { config as loadEnv } from 'dotenv';
import { createConnection } from 'mysql2/promise';
import { resolve } from 'node:path';

loadEnv({ path: resolve(__dirname, '../back/.env') });

const dbConfig = {
  host: process.env['E2E_DB_HOST'] ?? 'localhost',
  port: Number(process.env['E2E_DB_PORT'] ?? 3308),
  user: process.env['DB_USERNAME'],
  password: process.env['DB_PASSWORD'],
  database: process.env['DB_NAME'],
};

// Order doesn't matter functionally (FK checks are disabled around the
// truncation), but listing children before parents keeps it readable.
const TABLES = ['comments', 'subscriptions', 'posts', 'themes', 'users'];

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    // cy.env() (used for apiUrl) doesn't need this; only the deprecated
    // Cypress.env() global getter does, which we avoid using at all.
    allowCypressEnv: false,
    env: {
      apiUrl: 'http://localhost:8080/api',
    },
    setupNodeEvents(on, config) {
      on('task', {
        async 'db:reset'() {
          const connection = await createConnection(dbConfig);
          try {
            await connection.query('SET FOREIGN_KEY_CHECKS = 0');
            for (const table of TABLES) {
              await connection.query(`TRUNCATE TABLE \`${table}\``);
            }
            await connection.query('SET FOREIGN_KEY_CHECKS = 1');
          } finally {
            await connection.end();
          }
          return null;
        },
      });

      // Opt-in (npm run e2e:coverage): native V8 coverage via CDP, no source
      // instrumentation needed — works around esbuild/Vite not exposing an
      // Istanbul hook. Requires a Chromium-family browser (not Electron).
      // Mirrored into `expose` so the support file can read it synchronously
      // via Cypress.expose() without touching the deprecated Cypress.env().
      const coverageEnabled = Boolean(config.env['coverage']);
      config.expose = { ...config.expose, coverage: coverageEnabled };

      if (coverageEnabled) {
        return coverageTask(on, config, {
          name: 'MDD E2E Coverage',
          outputDir: './coverage/e2e',
          // entryFilter runs first, on raw V8 entries (before sourcemap
          // extraction). Excludes Cypress's own Test Runner UI (served under
          // the same localhost:4200 origin at /__cypress and /__/assets) and
          // restricts to JS/TS only, so the SPA's HTML shell (captured once
          // per route visited — login.html, register.html, ...) and global
          // CSS don't show up as unfiltered entries. Rules are checked in
          // order, first match wins.
          entryFilter: {
            '**/__cypress/**': false,
            '**/__/**': false,
            'http://localhost:4200/**/*.js': true,
            'http://localhost:4200/**/*.ts': true,
          },
          sourceFilter: {
            '**/node_modules/**': false,
            '**/src/app/**': true,
          },
          reports: ['v8', 'html-spa', 'lcov', 'console-summary'],
        });
      }

      return config;
    },
  },
});
