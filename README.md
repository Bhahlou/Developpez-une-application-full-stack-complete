# P6-Full-Stack-reseau-dev

## Back

### Pré-requis pour le bon fonctionnement du back

    -> JDK 25
    -> Docker
    -> Docker Compose
    -> Maven x.x.x

### Démarrage du back

Pour démarrer le back, il faut :

- Démarrer Docker-Desktop sur votre poste de travail local
- Ouvrir un terminal dans le dossier back et exécuter la commande :

```
mvn spring-boot:run
```

Le container contenant la base de données est automatiquement créé au démarrage du back.

### Tests

- Lancez tous les tests + la vérification de couverture :
  `mvn verify`

Le rapport de couverture est disponible ici :
[back/target/site/jacoco/index.html](back/target/site/jacoco/index.html)

## Front

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 14.1.3.

Don't forget to install your node_modules before starting (`npm install`).

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

### Tests

- Lancez tous les tests + la vérification de couverture :
  `ng test`

Le rapport de couverture est disponible ici :
[front/coverage/front/index.html](front/coverage/front/index.html)

### Tests E2E

Les tests E2E (Cypress) tournent contre une base de données dédiée, isolée de la base de dev habituelle, et repartent de zéro à chaque test.

- Démarrer le back avec le profil `e2e` (démarre automatiquement la base e2e, conteneur Docker éphémère, et l'arrête à la fermeture) :
  `mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"`
- Démarrer le front : `npm start`
- Une fois les deux up, lancez tous les tests E2E + la vérification de couverture :
  `npm run e2e:coverage`

Le rapport de couverture est disponible ici :
[front/coverage/e2e/index.html](front/coverage/e2e/index.html)
