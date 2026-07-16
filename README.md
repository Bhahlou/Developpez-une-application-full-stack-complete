<div align="center">

<img src="front/src/assets/logo_p6.png" alt="MDD logo" width="140"/>

# MDD — Monde de Dév

**Le réseau social des développeurs.** Abonnez-vous à des thèmes, publiez des articles, échangez en commentaires.

![Java](https://img.shields.io/badge/Java-25_LTS-ED8B00)
![Angular](https://img.shields.io/badge/Angular-22-DD0031)
![Spring Boot](https://img.shields.io/badge/SpringBoot-4.1-6DB33F)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)
![Tests](https://img.shields.io/badge/back_coverage-89%25%20unit%20%2F%2097%25%20integration-brightgreen)
![Tests](https://img.shields.io/badge/front_coverage-100%25%20unit%20%2F%2097%25%20e2e-brightgreen)

</div>

---

## Sommaire

- [À propos](#à-propos)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Démarrage rapide](#démarrage-rapide)
- [Tests](#tests)
- [API](#api)
- [Sécurité](#sécurité)
- [Structure du projet](#structure-du-projet)

## À propos

MDD est le MVP d'un réseau social pensé pour les développeurs : se tenir informé des sujets tech qui les intéressent (Java, Angular, DevOps...) et échanger avec des pairs, sans le bruit d'un réseau social généraliste.

Projet réalisé dans le cadre du parcours **Développeur Full-Stack Java/Angular** (OpenClassrooms).

## Fonctionnalités

| Fonctionnalité | Détail |
| --- | --- |
| Inscription / Connexion | Formulaire validé, session persistée via JWT |
| Profil utilisateur | Consultation et modification (username, email, mot de passe) |
| Thèmes | Consultation de tous les thèmes, création de thème |
| Abonnements | S'abonner / se désabonner à un thème |
| Fil d'actualité | Articles des thèmes suivis, triable par date |
| Articles | Rédaction et consultation d'un article |
| Commentaires | Ajout de commentaires sur un article |

## Stack technique

| Domaine | Choix | Pourquoi |
| --- | --- | --- |
| Back-end | Spring Boot 4.1 / Java 25 (LTS) | Dernière version stable, zéro dette de migration |
| Front-end | Angular 22, signals, standalone, zoneless | Aligné sur les pratiques Angular actuelles |
| UI | Angular Material 3 | Theming par tokens, bundle de styles réduit |
| Base de données | MySQL 8, dockerisée | CRUD relationnel simple, pas besoin de PostgreSQL |
| Auth | JWT (access + refresh token) | Stateless, adapté à une SPA |
| Tests back | JUnit 5, Mockito, Testcontainers | Intégration sur une vraie base MySQL, sans mocks |
| Tests front | Vitest, Cypress | Unitaire + e2e avec couverture instrumentée |
| Qualité | SonarCloud, Lighthouse, JaCoCo | Analyse statique et audit performance/accessibilité |

## Architecture

```
Navigateur ── HTTPS ──▶ Angular SPA ── REST + JWT Bearer ──▶ Spring Boot API ──▶ MySQL
                                          Controller → Service → Repository → Domain/DTO
```

Le back suit une architecture en couches strictes : les controllers ne manipulent jamais d'entité JPA directement, tout transite par des DTO (records) immuables.

## Démarrage rapide

### Prérequis

- JDK 25
- Docker + Docker Compose
- Node.js / npm

### Back-end

1. Démarrer Docker Desktop sur son poste de travail.
2. Dans `back/`, copier `.env.example` en `.env` et renseigner ses propres identifiants et son secret JWT.
3. Lancer l'API :

```bash
cd back
./mvnw spring-boot:run
```

Le conteneur MySQL est démarré et arrêté automatiquement avec le back (`spring-boot-docker-compose`), pas besoin de `docker-compose up` manuel. L'API démarre sur `http://localhost:8080`.

### Front-end

```bash
cd front
npm install
npm start
```

L'application est accessible sur `http://localhost:4200`.

## Tests

| Type | Outil | Portée | Résultat |
| --- | --- | --- | --- |
| Unitaire back | JUnit 5 + Mockito | Services, controllers | 89 % couverture, 103/103 ✅ |
| Intégration back | Spring Boot Test + Testcontainers | Auth, thèmes, abonnements, articles, commentaires | 97 % couverture, 42/42 ✅ |
| Unitaire front | Vitest | Services, guards, interceptor, store, composants | 100 % couverture, 121/121 ✅ |
| E2E front | Cypress | Accueil, auth, thèmes, fil, détail article, profil | 97,34 % couverture, 46/46 ✅ |

**Back** — tests + couverture :

```bash
cd back && mvn verify
```

Rapport de couverture disponible ici : [back/target/site/jacoco/index.html](back/target/site/jacoco/index.html)

**Front** — tests + couverture :

```bash
cd front && ng test
```

Rapport de couverture disponible ici : [front/coverage/front/index.html](front/coverage/front/index.html)

**E2E** — les tests tournent contre une base dédiée, isolée de la base de dev, et repartent de zéro à chaque test :

```bash
cd back && mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"   # démarre le back en profil e2e
cd front && npm start                                              # démarre le front
cd front && npm run e2e:coverage                                   # une fois les deux up
```

Rapport de couverture disponible ici : [front/coverage/e2e/index.html](front/coverage/e2e/index.html)

## API

Toutes les routes sont préfixées par `/api` et protégées par JWT Bearer, à l'exception des routes d'authentification.

| Endpoint | Méthode | Description |
| --- | --- | --- |
| `/api/auth/register` | POST | Inscription |
| `/api/auth/login` | POST | Connexion |
| `/api/auth/refresh` | POST | Renouvellement de token |
| `/api/auth/logout` | POST | Déconnexion |
| `/api/auth/me` | GET / PUT | Profil courant / modification |
| `/api/themes` | GET / POST | Liste des thèmes / création |
| `/api/subscriptions` | GET | Thèmes suivis |
| `/api/subscriptions/{themeId}` | POST / DELETE | Abonnement / désabonnement |
| `/api/posts` | GET / POST | Fil d'actualité / création d'article |
| `/api/posts/{id}` | GET | Détail d'un article |
| `/api/posts/{postId}/comments` | GET / POST | Commentaires d'un article |

Documentation interactive (Swagger UI) une fois l'API lancée : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — spécification OpenAPI brute sur `/v3/api-docs`.

## Sécurité

- Mots de passe hashés avec BCrypt, jamais renvoyés en clair.
- Authentification JWT stateless (access token court + refresh token en rotation).
- CORS restreint à l'origine du front.
- Validation systématique des entrées (Jakarta Bean Validation).
- Erreurs API génériques et codées : aucune donnée interne (stack trace, SQL) exposée au client.

## Structure du projet

```
.
├── back/     # API Spring Boot (Java 25)
│   └── src/main/java/com/openclassrooms/mddapi/
│       ├── controller/    # Endpoints REST
│       ├── service/       # Logique métier
│       ├── repository/    # Accès aux données (Spring Data JPA)
│       ├── model/         # Entités JPA
│       ├── dto/           # Contrats d'échange (records)
│       ├── exception/     # Exceptions métier + gestion centralisée
│       └── config/        # Sécurité, JWT, OpenAPI
└── front/    # SPA Angular 22
    └── src/app/
        ├── core/          # Services, guards, interceptors, store
        ├── layout/        # Header, layout principal
        └── pages/         # Écrans (auth, feed, thèmes, profil...)
```
