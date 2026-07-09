describe('Home', () => {
  it('navigates to the login page', () => {
    cy.visit('/');
    cy.contains('button', 'Se connecter').click();

    cy.url().should('include', '/login');
  });

  it('navigates to the register page', () => {
    cy.visit('/');
    cy.contains('button', "S'inscrire").click();

    cy.url().should('include', '/register');
  });
});
