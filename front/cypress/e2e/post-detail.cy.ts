describe('Post detail', () => {
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('shows the post and its existing comments', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.createPost({ themeId: theme.body.id, title: 'Signals 101', content: 'Introduction aux signals.' }, token).then(
          (post) => {
            cy.createComment(post.body.id, 'Super article !', token);
            cy.visitAuthenticatedAs(`/feed/${post.body.id}`, response.body);
          },
        );
      });
    });

    cy.contains('h1', 'Signals 101').should('be.visible');
    cy.contains('Introduction aux signals.').should('be.visible');
    cy.contains('Angular').should('be.visible');
    cy.contains('Super article !').should('be.visible');
    cy.contains(user.username).should('be.visible');
  });

  it('adds a comment through the form', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.createPost({ themeId: theme.body.id, title: 'Signals 101', content: 'Introduction aux signals.' }, token).then(
          (post) => {
            cy.visitAuthenticatedAs(`/feed/${post.body.id}`, response.body);
          },
        );
      });
    });

    cy.get('textarea[aria-label="Écrivez ici votre commentaire"]').focus().type('Très intéressant.');
    cy.get('button[aria-label="Envoyer le commentaire"]').click();

    cy.contains('Très intéressant.');
    cy.get('textarea[aria-label="Écrivez ici votre commentaire"]').should('have.value', '');
  });

  it('shows a validation error when leaving the comment field empty', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.createPost({ themeId: theme.body.id, title: 'Signals 101', content: 'Introduction aux signals.' }, token).then(
          (post) => {
            cy.visitAuthenticatedAs(`/feed/${post.body.id}`, response.body);
          },
        );
      });
    });

    cy.get('textarea[aria-label="Écrivez ici votre commentaire"]').focus().blur();

    cy.contains('Commentaire requis').should('be.visible');
  });

  it('navigates back to the feed', () => {
    cy.registerUser(user).then((response) => {
      const token = response.body.accessToken;
      cy.createTheme({ title: 'Angular', description: 'Tout sur Angular.' }, token).then((theme) => {
        cy.createPost({ themeId: theme.body.id, title: 'Signals 101', content: 'Introduction aux signals.' }, token).then(
          (post) => {
            cy.visitAuthenticatedAs(`/feed/${post.body.id}`, response.body);
          },
        );
      });
    });

    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/feed$/);
  });

  it('shows an error when the post does not exist', () => {
    cy.authenticatedVisit('/feed/999999', user);

    cy.contains("Cet article n'existe plus.").should('be.visible');
  });
});
