export interface TestUser {
  username: string;
  email: string;
  password: string;
}

export interface TestTheme {
  title: string;
  description: string;
}

export interface TestPost {
  themeId: number;
  title: string;
  content: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

interface ThemeResponse {
  id: number;
  title: string;
  description: string;
  subscribed: boolean;
}

interface PostResponse {
  id: number;
  title: string;
  content: string;
  themeId: number;
  themeTitle: string;
  authorUsername: string;
  createdAt: string;
}

interface CommentResponse {
  id: number;
  content: string;
  authorUsername: string;
  createdAt: string;
}

// Must match ACCESS_TOKEN_KEY/REFRESH_TOKEN_KEY in auth.service.ts — used to
// seed an authenticated session directly into localStorage, bypassing the UI.
const ACCESS_TOKEN_KEY = 'mdd_access_token';
const REFRESH_TOKEN_KEY = 'mdd_refresh_token';

declare global {
  namespace Cypress {
    interface Chainable {
      /** Creates a user directly through the API, bypassing the UI, to set up test preconditions. */
      registerUser(user: TestUser): Chainable<Cypress.Response<AuthResponse>>;
      /** Registers a user via the API and visits `url` already authenticated as them. */
      authenticatedVisit(url: string, user: TestUser): Chainable<Cypress.Response<AuthResponse>>;
      /** Visits `url` already authenticated, given tokens obtained from a prior registerUser/login call. */
      visitAuthenticatedAs(url: string, tokens: AuthResponse): Chainable<void>;
      /** Creates a theme directly through the API, as an authenticated user, to set up test preconditions. */
      createTheme(theme: TestTheme, accessToken: string): Chainable<Cypress.Response<ThemeResponse>>;
      /** Subscribes to a theme directly through the API, as an authenticated user, to set up test preconditions. */
      subscribeToTheme(themeId: number, accessToken: string): Chainable<Cypress.Response<void>>;
      /** Creates a post directly through the API, as an authenticated user, to set up test preconditions. */
      createPost(post: TestPost, accessToken: string): Chainable<Cypress.Response<PostResponse>>;
      /** Creates a comment directly through the API, as an authenticated user, to set up test preconditions. */
      createComment(postId: number, content: string, accessToken: string): Chainable<Cypress.Response<CommentResponse>>;
    }
  }
}

Cypress.Commands.add('registerUser', (user: TestUser) => {
  return cy.env(['apiUrl']).then(({ apiUrl }) => {
    return cy.request('POST', `${apiUrl}/auth/register`, user);
  });
});

Cypress.Commands.add('authenticatedVisit', (url: string, user: TestUser) => {
  return cy.registerUser(user).then((response) => {
    cy.visitAuthenticatedAs(url, response.body);
    return cy.wrap(response, { log: false });
  });
});

Cypress.Commands.add('visitAuthenticatedAs', (url: string, tokens: AuthResponse) => {
  return cy.visit(url, {
    onBeforeLoad(win) {
      win.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
      win.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    },
  });
});

Cypress.Commands.add('createTheme', (theme: TestTheme, accessToken: string) => {
  return cy.env(['apiUrl']).then(({ apiUrl }) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/themes`,
      headers: { Authorization: `Bearer ${accessToken}` },
      body: theme,
    });
  });
});

Cypress.Commands.add('subscribeToTheme', (themeId: number, accessToken: string) => {
  return cy.env(['apiUrl']).then(({ apiUrl }) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/subscriptions/${themeId}`,
      headers: { Authorization: `Bearer ${accessToken}` },
    });
  });
});

Cypress.Commands.add('createPost', (post: TestPost, accessToken: string) => {
  return cy.env(['apiUrl']).then(({ apiUrl }) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/posts`,
      headers: { Authorization: `Bearer ${accessToken}` },
      body: post,
    });
  });
});

Cypress.Commands.add('createComment', (postId: number, content: string, accessToken: string) => {
  return cy.env(['apiUrl']).then(({ apiUrl }) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/posts/${postId}/comments`,
      headers: { Authorization: `Bearer ${accessToken}` },
      body: { content },
    });
  });
});

export {};
