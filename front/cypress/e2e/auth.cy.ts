describe('Authentication', () => {
  // Not a generic string like "Passw0rd!" — Chrome's breached-password check
  // flags common test passwords and pops a warning that blocks the flow.
  const user = {
    username: 'johndoe',
    email: 'john@doe.com',
    password: 'Kx9!qRvL3z',
  };

  it('registers a new user through the UI and lands on the feed', () => {
    cy.visit('/register');

    cy.get('input[aria-label="Nom d\'utilisateur"]').focus().type(user.username);
    cy.get('input[aria-label="Email"]').focus().type(user.email);
    cy.get('input[aria-label="Mot de passe"]').focus().type(user.password);
    cy.contains('button', "S'inscrire").click();

    cy.url().should('include', '/feed');
  });

  it('logs in an existing user', () => {
    cy.registerUser(user);

    cy.visit('/login');
    cy.get('input[aria-label="Email ou nom d\'utilisateur"]').focus().type(user.username);
    cy.get('input[aria-label="Mot de passe"]').focus().type(user.password);
    cy.contains('button', 'Se connecter').click();

    cy.url().should('include', '/feed');
  });

  it('shows an error and stays on the page when the username is already taken', () => {
    cy.registerUser(user);

    cy.visit('/register');
    cy.get('input[aria-label="Nom d\'utilisateur"]').focus().type(user.username);
    cy.get('input[aria-label="Email"]').focus().type('someoneelse@doe.com');
    cy.get('input[aria-label="Mot de passe"]').focus().type('Zq4!mPxT7h');
    cy.contains('button', "S'inscrire").click();

    cy.contains("Ce nom d'utilisateur est déjà pris.");
    cy.url().should('include', '/register');
  });

  it('shows an error and stays on the page when the email is already taken', () => {
    cy.registerUser(user);

    cy.visit('/register');
    cy.get('input[aria-label="Nom d\'utilisateur"]').focus().type('janedoe');
    cy.get('input[aria-label="Email"]').focus().type(user.email);
    cy.get('input[aria-label="Mot de passe"]').focus().type('Zq4!mPxT7h');
    cy.contains('button', "S'inscrire").click();

    cy.contains('Cette adresse email est déjà utilisée.');
    cy.url().should('include', '/register');
  });

  it('shows an error and stays on the page when the password is incorrect', () => {
    cy.registerUser(user);

    cy.visit('/login');
    cy.get('input[aria-label="Email ou nom d\'utilisateur"]').focus().type(user.username);
    cy.get('input[aria-label="Mot de passe"]').focus().type('Nq8!wZbF2r');
    cy.contains('button', 'Se connecter').click();

    cy.contains('Identifiant ou mot de passe incorrect.');
    cy.url().should('include', '/login');
  });

  it('redirects to /login when visiting a protected page without being authenticated', () => {
    cy.visit('/feed');

    cy.url().should('include', '/login');
  });

  it('redirects an already authenticated user away from /login', () => {
    cy.authenticatedVisit('/login', user);

    cy.url().should('include', '/feed');
  });

  it('redirects an already authenticated user away from /register', () => {
    cy.authenticatedVisit('/register', user);

    cy.url().should('include', '/feed');
  });

  it('shows validation errors when leaving the register form fields empty', () => {
    cy.visit('/register');
    // Errors only render once a field is touched(); blurring an empty field
    // triggers that without needing to type or submit.
    cy.get('input[aria-label="Nom d\'utilisateur"]').focus().blur();
    cy.get('input[aria-label="Email"]').focus().blur();
    cy.get('input[aria-label="Mot de passe"]').focus().blur();

    cy.contains("Nom d'utilisateur requis");
    cy.contains('Email requis');
    cy.contains('Mot de passe requis');
    cy.url().should('include', '/register');
  });

  it('shows a validation error for an invalid email format on register', () => {
    cy.visit('/register');
    cy.get('input[aria-label="Nom d\'utilisateur"]').focus().type('validname');
    cy.get('input[aria-label="Email"]').focus().type('not-an-email');
    cy.get('input[aria-label="Mot de passe"]').focus().type('Zq4!mPxT7h');
    cy.contains('button', "S'inscrire").click();

    cy.contains('Adresse email invalide');
    cy.url().should('include', '/register');
  });

  it('shows validation errors when leaving the login form fields empty', () => {
    cy.visit('/login');
    cy.get('input[aria-label="Email ou nom d\'utilisateur"]').focus().blur();
    cy.get('input[aria-label="Mot de passe"]').focus().blur();

    cy.contains('Identifiant requis');
    cy.contains('Mot de passe requis');
    cy.url().should('include', '/login');
  });

  it('goes back from the login page', () => {
    cy.visit('/');
    cy.contains('button', 'Se connecter').click();

    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/$/);
  });

  it('goes back from the register page', () => {
    cy.visit('/');
    cy.contains('button', "S'inscrire").click();

    cy.get('button[aria-label="Retour"]').click();

    cy.url().should('match', /\/$/);
  });

  it('toggles password visibility on the login page', () => {
    cy.visit('/login');

    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'password');
    cy.get('button[aria-label="Afficher le mot de passe"]').click();
    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'text');
    cy.get('button[aria-label="Masquer le mot de passe"]').click();
    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'password');
  });

  it('toggles password visibility on the register page', () => {
    cy.visit('/register');

    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'password');
    cy.get('button[aria-label="Afficher le mot de passe"]').click();
    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'text');
    cy.get('button[aria-label="Masquer le mot de passe"]').click();
    cy.get('input[aria-label="Mot de passe"]').should('have.attr', 'type', 'password');
  });
});
