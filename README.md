# Picu

Chat privata di famiglia per Android: il bambino può scrivere a Mamma, a Babbo, o a entrambi insieme. Un solo APK, installato manualmente su ogni telefono — il ruolo viene chiesto al primo avvio e resta legato a quel device.

## Come funziona

- **App**: Android nativo (Kotlin + Jetpack Compose), un solo APK per tutti i ruoli.
- **Backend**: Firebase — Firestore (messaggi/profili), Realtime Database (solo per "chi è online"), Authentication anonima, Cloud Messaging (notifiche push), Cloud Functions (per inviare le notifiche).
- **Ruoli**: al primo avvio si sceglie Bambino / Mamma / Babbo + un codice famiglia. Il ruolo resta salvato solo sul device (dati locali dell'app); se li cancellate, il device torna a chiedere "chi sei?".
- **Chat fisse**: bambino↔mamma, bambino↔babbo, bambino↔mamma+babbo. Aggiungere in futuro un altro parente = rieseguire lo script di setup con il nuovo ruolo, nessuna modifica al codice.
- **Presenza online**: mamma/babbo vedono se il bambino è connesso in questo momento (utile visto che il suo telefono usa solo Wi-Fi di casa o hotspot).
- **Notifica di apertura app**: quando il bambino apre l'app e va online, mamma e babbo ricevono una notifica push automatica.

## Struttura del progetto

```
app/                    progetto Android Studio (Kotlin + Compose)
functions/               Cloud Functions (invio notifiche push)
scripts/create-chats.js  script one-shot per creare le 3 chat
firestore.rules          regole di sicurezza Firestore
database.rules.json      regole di sicurezza Realtime Database
firebase.json / .firebaserc
```

## Setup — passo per passo

### 1. Crea il progetto Firebase
1. Vai su [console.firebase.google.com](https://console.firebase.google.com) col tuo account Google e crea un nuovo progetto (es. "Picu").
2. Nel progetto, aggiungi un'app Android con package name `com.picu.app`.
3. Scarica il file `google-services.json` e mettilo in `app/google-services.json` (è ignorato da git, resta locale).
4. Attiva questi servizi dal menu laterale:
   - **Authentication** → tab "Sign-in method" → abilita **Anonima**.
   - **Firestore Database** → crea database (modalità produzione).
   - **Realtime Database** → crea database (modalità bloccata, le regole le carichiamo noi).
   - **Cloud Messaging** → non serve configurazione iniziale.
5. Passa al piano **Blaze** (Impostazioni → Utilizzo e fatturazione). Serve una carta come garanzia, ma per il volume di questa chat familiare **il costo resta €0** — serve solo perché le Cloud Functions (necessarie per le notifiche) richiedono questo piano.

### 2. Imposta il codice famiglia
Crea (se non esiste) il file `local.properties` nella root del progetto (è ignorato da git) e aggiungi:
```
PICU_FAMILY_CODE=scegli-un-codice-solo-vostro
```
Chi non conosce questo codice non può completare il setup nell'app.

### 3. Apri il progetto in Android Studio
1. Apri la cartella `Picu` con Android Studio.
2. Lascia che sincronizzi/generi il Gradle Wrapper al primo avvio (potrebbe chiederlo se manca il jar del wrapper).
3. Build → Make Project per verificare che compili.

### 4. Installa il Firebase CLI e collega il progetto
```
npm install -g firebase-tools
firebase login
```
Poi modifica `.firebaserc` mettendo il vero ID del progetto Firebase al posto di `IL-TUO-PROJECT-ID`.

### 5. Pubblica regole e Cloud Functions
```
cd functions && npm install && cd ..
firebase deploy --only firestore:rules,database,functions
```

### 6. Primo accesso di tutti e tre
Installa l'APK (build → debug va bene per iniziare) su tutti e tre i telefoni e fai fare a ciascuno il setup (ruolo + nome + codice famiglia). Questo crea i loro profili Firebase.

### 7. Crea le 3 chat
```
cd scripts
npm install firebase-admin
node create-chats.js
```
Segue le istruzioni nel file per generare `serviceAccountKey.json` dalla console Firebase la prima volta. Dopo, riaprendo l'app sui tre device, le chat compaiono.

### 8. Genera l'APK da installare manualmente
In Android Studio: Build → Generate Signed App Bundle / APK → APK, con una keystore vostra (Android Studio ve la crea al primo utilizzo). L'APK firmato si condivide via link/drive/USB e si installa abilitando "Installa da fonti sconosciute" sul telefono.

## Prossimi passi possibili
- **Foto/media nei messaggi**: aggiungere Firebase Storage (tier gratuito sufficiente per questo volume).
- **Geolocalizzazione facoltativa del bambino**: fattibile con `FusedLocationProviderClient` + un interruttore "condividi posizione" nel profilo, scritta su Firestore. Va progettata a parte per gestire bene permessi Android e consumo batteria — non ancora implementata in questa versione.
- **Altri parenti** (nonni, ecc.): basta rieseguire `create-chats.js` con il nuovo ruolo/chat, nessuna modifica al codice.
