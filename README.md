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

### Where to start

As you may have seen if you already started the app, a simple home page containing a logo, a title and a button is available. If you take a look at its code (in the `home.component.html`) you will see that an external UI library is already configured in the project.

This library is `@angular/material`, it's one of the most famous in the angular ecosystem. As you can see on their docs (https://material.angular.io/), it contains a lot of highly customizable components that will help you design your interfaces quickly.

Note: I recommend to use material however it's not mandatory, if you prefer you can get rid of it.

Good luck!
