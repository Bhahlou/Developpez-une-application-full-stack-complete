import './commands';

// Every test starts from a clean database instead of relying on state left
// behind by a previous test, so tests stay independent and order-agnostic.
beforeEach(() => {
  cy.task('db:reset');
});

// Only wired up for `npm run e2e:coverage` — cypress.config.ts registers the
// coverageBefore/coverageAfter tasks solely when the coverage env flag is on,
// so this must stay in sync or a coverage run looks for tasks that don't exist.
if (Cypress.expose('coverage')) {
  require('cypress-monocart-coverage/support');
}
