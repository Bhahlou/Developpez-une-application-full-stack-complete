describe('Edit profile', () => {
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('updates the profile and returns to the profile page', () => {
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Nom d\'utilisateur"]').should('have.value', user.username).clear().type('janedoe');
    cy.get('input[aria-label="Mot de passe actuel"]').focus().type(user.password);
    cy.get('button[type="submit"]').click();

    cy.contains('Profil mis à jour.');
    cy.url().should('match', /\/profile$/);
    cy.contains('janedoe');
  });

  it('shows validation errors when leaving the required fields empty', () => {
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Nom d\'utilisateur"]').clear().blur();
    cy.get('input[aria-label="Email"]').clear().blur();
    cy.get('input[aria-label="Mot de passe actuel"]').focus().blur();

    cy.contains("Nom d'utilisateur requis");
    cy.contains('Email requis');
    cy.contains('Mot de passe actuel requis');
  });

  it('shows a validation error for a weak new password', () => {
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Nouveau mot de passe"]').focus().type('abc').blur();

    cy.contains('Au moins 8 caractères, avec une majuscule, une minuscule, un chiffre et un caractère spécial');
  });

  it('shows an error when the current password is incorrect', () => {
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Mot de passe actuel"]').focus().type('WrongPassword1!');
    cy.get('button[type="submit"]').click();

    cy.contains('Identifiant ou mot de passe incorrect.');
  });

  it('shows an error when the new username is already taken', () => {
    cy.registerUser({ username: 'janedoe', email: 'jane@doe.com', password: 'Zq4!mPxT7h' });
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Nom d\'utilisateur"]').clear().type('janedoe');
    cy.get('input[aria-label="Mot de passe actuel"]').focus().type(user.password);
    cy.get('button[type="submit"]').click();

    cy.contains("Ce nom d'utilisateur est déjà pris.");
  });

  it('toggles visibility for both password fields', () => {
    cy.authenticatedVisit('/profile/edit', user);

    cy.get('input[aria-label="Mot de passe actuel"]').should('have.attr', 'type', 'password');
    cy.get('button[aria-label="Afficher le mot de passe"]').eq(0).click();
    cy.get('input[aria-label="Mot de passe actuel"]').should('have.attr', 'type', 'text');

    cy.get('input[aria-label="Nouveau mot de passe"]').should('have.attr', 'type', 'password');
    cy.get('button[aria-label="Afficher le mot de passe"]').eq(0).click();
    cy.get('input[aria-label="Nouveau mot de passe"]').should('have.attr', 'type', 'text');
  });

  it('goes back from the edit profile page', () => {
    cy.authenticatedVisit('/profile', user);
    cy.contains('button', 'Modifier mon profil').click();

    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/profile$/);
  });
});
