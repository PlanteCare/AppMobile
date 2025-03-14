# Application Mobile PlanteCare

Une application Android pour la gestion et la surveillance des plantes, avec intégration MQTT pour recevoir des données en temps réel.

## Fonctionnalités

- **Authentification Utilisateur**: Connexion sécurisée via l'API backend
- **Communication MQTT**: Réception de messages en temps réel depuis les capteurs des plantes
- **Interface Utilisateur Intuitive**: Design moderne et ergonomique
- **Stockage Local**: Gestion des tokens d'authentification avec SharedPreferences

## Stack Technique

- **Kotlin**: Langage de programmation principal
- **Retrofit**: Client HTTP pour les appels API REST
- **OkHttp**: Client HTTP pour les requêtes avancées
- **GSON**: Bibliothèque de sérialisation/désérialisation JSON
- **Paho MQTT**: Client MQTT pour la communication en temps réel
- **Jetpack Compose**: UI déclarative (partiellement implémentée)
- **Android Architecture Components**: Composants du cycle de vie Android

## Prérequis

- Android Studio Arctic Fox ou supérieur
- JDK 11 ou supérieur
- SDK Android 35 (Android 15)
- Gradle 8.10.2 ou supérieur

## Structure du Projet

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/plantecare/appmobile/
│   │   │   ├── activities/            # Activités Android
│   │   │   │   ├── MainActivity.kt    # Écran de connexion
│   │   │   │   └── HomeActivity.kt    # Écran d'accueil avec MQTT
│   │   │   ├── api/                   # Configuration API
│   │   │   │   ├── ApiService.kt      # Interface des services API
│   │   │   │   └── RetrofitClient.kt  # Configuration Retrofit
│   │   │   ├── models/                # Modèles de données
│   │   │   │   ├── LoginRequest.kt    # Requête de connexion
│   │   │   │   └── LoginResponse.kt   # Réponse de connexion
│   │   │   └── mqtt/                  # Gestion MQTT
│   │   │       └── MqttHandler.kt     # Gestionnaire de connexion MQTT
│   │   ├── res/                       # Ressources Android
│   │   │   ├── drawable/              # Images et icônes
│   │   │   ├── layout/                # Layouts XML
│   │   │   │   ├── activity_main.xml  # Layout de l'écran de connexion
│   │   │   │   └── activity_home.xml  # Layout de l'écran d'accueil
│   │   │   ├── values/                # Valeurs (couleurs, chaînes, etc.)
│   │   │   └── ...
│   │   └── AndroidManifest.xml        # Configuration de l'application
│   ├── androidTest/                   # Tests d'instrumentation
│   └── test/                          # Tests unitaires
└── build.gradle.kts                   # Configuration Gradle de l'application
```

## Configuration

### Configuration API

Le point de terminaison de l'API est configuré dans `RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "https://api.lyeshamrani.com/"
```

### Configuration MQTT

Le serveur MQTT est configuré dans `HomeActivity.kt`:

```kotlin
mqttHandler?.connect("tcp://lyeshamrani.com:1883", "AndroidClient")
mqttHandler?.subscribe("plantecare/message") { message ->
    // Traitement des messages
}
```

## Installation

1. Clonez le dépôt
2. Ouvrez le projet dans Android Studio
3. Synchronisez les dépendances Gradle
4. Connectez un appareil Android ou utilisez un émulateur
5. Exécutez l'application

## Fonctionnement

1. L'utilisateur se connecte avec son email et son mot de passe
2. Après authentification, l'utilisateur est redirigé vers l'écran d'accueil
3. L'application se connecte au serveur MQTT et affiche les messages reçus
4. Les messages MQTT sont affichés en temps réel avec horodatage

## Personnalisation

### Couleurs

Les couleurs de l'application sont définies dans `res/values/colors.xml`:

```xml
<resources>
    <color name="withe">#FFFFFF</color>
    <color name="black">#000000</color>
    <color name="plant_green">#7CAC5C</color>
    <color name="background">#F9FAFB</color>
    <color name="button_background">#EFEFEF</color>
    <color name="icon_color">#9CA3AF</color>
    <color name="icon_color_secondary">#4B5563</color>
</resources>
```

### Chaînes de caractères

Les chaînes de caractères sont définies dans `res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">PlanteCare</string>
    <string name="connexion">Connexion</string>
    <string name="mot_de_passe">Mot de passe</string>
    <string name="email">Email</string>
</resources>
```

## Dépannage

### Problèmes de connexion API

Vérifiez que l'API backend est en cours d'exécution et accessible. Les erreurs de connexion sont affichées via des toasts dans l'application.

### Problèmes de connexion MQTT

Vérifiez que le serveur MQTT est en cours d'exécution et que les informations de connexion sont correctes. Les erreurs MQTT sont affichées dans les logs de l'application.
