describe('Feed', () => {
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('shows the empty state when there are no posts', () => {
    cy.authenticatedVisit('/feed', user);

    cy.contains('Aucun article pour le moment. Abonnez-vous à un thème pour voir des articles ici.');
  });

  it('lists posts from subscribed themes and navigates to the detail page', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.subscribeToTheme(theme.body.id, token);
        cy.createPost({ themeId: theme.body.id, title: 'Signals 101', content: 'Introduction aux signals.' }, token);
      });
      cy.visitAuthenticatedAs('/feed', response.body);
    });

    cy.contains('h2', 'Signals 101').click();

    cy.url().should('match', /\/feed\/\d+$/);
    cy.contains('h1', 'Signals 101');
    cy.contains('Introduction aux signals.');
  });

  it('toggles the sort order', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.subscribeToTheme(theme.body.id, token);
        cy.createPost({ themeId: theme.body.id, title: 'First post', content: 'Content 1.' }, token);
        cy.createPost({ themeId: theme.body.id, title: 'Second post', content: 'Content 2.' }, token);
      });
      cy.visitAuthenticatedAs('/feed', response.body);
    });

    cy.get('mat-icon').contains('arrow_downward');
    cy.contains('button', 'Trier par').click();
    cy.get('mat-icon').contains('arrow_upward');
  });

  it('creates a post through the form and navigates to its detail page', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.subscribeToTheme(theme.body.id, token);
      });
      cy.visitAuthenticatedAs('/feed', response.body);
    });

    cy.intercept('GET', '**/api/themes').as('getThemes');
    cy.contains('a', 'Créer un article').click();
    // The select's DOM node exists as soon as the page renders, regardless of
    // whether the themes have loaded yet — open it only once they have, or
    // the panel opens empty and the option never appears.
    cy.wait('@getThemes');
    cy.get('[aria-label="Thème"]').click();
    cy.contains('mat-option', 'Angular').click();
    cy.get('input[aria-label="Titre de l\'article"]').focus().type('Mon premier article');
    cy.get('textarea[aria-label="Contenu de l\'article"]').focus().type('Le contenu de mon article.');
    cy.get('button[type="submit"]').click();

    cy.contains('Article créé.');
    cy.url().should('match', /\/feed\/\d+$/);
    cy.contains('h1', 'Mon premier article');
  });

  it('shows validation errors when leaving the create post fields empty', () => {
    cy.registerUser(user).then((response) => {
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, response.body.accessToken);
      cy.visitAuthenticatedAs('/feed', response.body);
    });

    cy.contains('a', 'Créer un article').click();
    cy.get('[aria-label="Thème"]').click();
    cy.get('body').type('{esc}');
    cy.get('input[aria-label="Titre de l\'article"]').focus().blur();
    cy.get('textarea[aria-label="Contenu de l\'article"]').focus().blur();

    cy.contains('Thème requis');
    cy.contains('Titre requis');
    cy.contains('Contenu requis');
  });

  it('goes back from the create post page', () => {
    cy.authenticatedVisit('/feed', user);

    cy.contains('a', 'Créer un article').click();
    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/feed$/);
  });
});
