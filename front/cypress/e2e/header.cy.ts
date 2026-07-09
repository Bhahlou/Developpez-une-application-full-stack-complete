describe('Header', () => {
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('logs the user out and redirects to the login page', () => {
    cy.authenticatedVisit('/feed', user);

    cy.contains('button', 'Se déconnecter').click();

    cy.url().should('include', '/login');
  });

  it('requires logging in again after logging out', () => {
    cy.authenticatedVisit('/feed', user);
    cy.contains('button', 'Se déconnecter').click();

    cy.visit('/feed');

    cy.url().should('include', '/login');
  });
});
