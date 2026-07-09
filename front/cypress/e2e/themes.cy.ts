describe('Themes', () => {
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('shows the empty state when there are no themes', () => {
    cy.authenticatedVisit('/themes', user);

    cy.contains('Aucun thème pour le moment.');
  });

  it('creates a theme through the dialog and shows it in the list', () => {
    cy.authenticatedVisit('/themes', user);

    cy.contains('button', 'Créer un thème').click();
    cy.get('input[aria-label="Titre"]').focus().type('Angular');
    cy.get('textarea[aria-label="Description"]').focus().type('Tout sur le framework Angular.');
    cy.get('button[type="submit"]').click();

    cy.contains('Thème créé.');
    cy.contains('h2', 'Angular');
  });

  it('shows an error when the theme title is already taken', () => {
    cy.registerUser(user).then((response) => {
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, response.body.accessToken);
      cy.visitAuthenticatedAs('/themes', response.body);
    });

    cy.contains('button', 'Créer un thème').click();
    cy.get('input[aria-label="Titre"]').focus().type('Angular');
    cy.get('textarea[aria-label="Description"]').focus().type('Un autre thème Angular.');
    cy.get('button[type="submit"]').click();

    cy.contains('Un thème avec ce titre existe déjà.');
  });

  it('shows validation errors when leaving the create theme fields empty', () => {
    cy.authenticatedVisit('/themes', user);

    cy.contains('button', 'Créer un thème').click();
    cy.get('input[aria-label="Titre"]').focus().blur();
    cy.get('textarea[aria-label="Description"]').focus().blur();

    cy.contains('Titre requis');
    cy.contains('Description requise');
  });

  it('subscribes to a theme from the themes list', () => {
    cy.registerUser(user).then((response) => {
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, response.body.accessToken);
      cy.visitAuthenticatedAs('/themes', response.body);
    });

    cy.contains('button', "S'abonner").click();

    cy.contains('Abonnement effectué.');
    cy.contains('button', 'Déjà abonné').should('be.disabled');
  });

  it('shows the empty state on the profile page when there are no subscriptions', () => {
    cy.authenticatedVisit('/profile', user);

    cy.contains('Aucun abonnement pour le moment.');
  });

  it('unsubscribes from a theme via the profile page', () => {
    cy.registerUser(user).then((response) => {
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, response.body.accessToken).then(
        (theme) => {
          cy.subscribeToTheme(theme.body.id, response.body.accessToken);
        },
      );
      cy.visitAuthenticatedAs('/profile', response.body);
    });

    cy.contains('h3', 'Angular');
    cy.contains('button', 'Se désabonner').click();

    cy.contains('Désabonnement effectué.');
    cy.contains('Aucun abonnement pour le moment.');
  });

  it('closes the create theme dialog without creating anything when cancelled', () => {
    cy.authenticatedVisit('/themes', user);

    cy.contains('button', 'Créer un thème').click();
    cy.contains('button', 'Annuler').click();

    cy.get('input[aria-label="Titre"]').should('not.exist');
    cy.contains('Aucun thème pour le moment.');
  });

  it('goes back from the profile page', () => {
    cy.authenticatedVisit('/feed', user);
    cy.visit('/profile');

    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/feed$/);
  });
});
