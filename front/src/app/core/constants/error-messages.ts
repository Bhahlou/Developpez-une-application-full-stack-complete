export const ERROR_MESSAGES: Record<string, string> = {
  AUTH_BAD_CREDENTIALS: 'Identifiant ou mot de passe incorrect.',
  AUTH_INVALID_REFRESH_TOKEN: 'Votre session a expiré, merci de vous reconnecter.',
  AUTH_REFRESH_TOKEN_EXPIRED: 'Votre session a expiré, merci de vous reconnecter.',
  USER_USERNAME_TAKEN: "Ce nom d'utilisateur est déjà pris.",
  USER_EMAIL_TAKEN: 'Cette adresse email est déjà utilisée.',
  VALIDATION_ERROR: 'Merci de vérifier les informations saisies.',
  THEME_TITLE_TAKEN: 'Un thème avec ce titre existe déjà.',
};

export const DEFAULT_ERROR_MESSAGE = 'Une erreur est survenue, réessayez.';
