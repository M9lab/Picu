/**
 * Script da eseguire UNA VOLTA, dopo che bambino, mamma e babbo hanno già
 * fatto il primo accesso dall'app (serve per generare i loro uid).
 *
 * Uso:
 *   1. Firebase Console -> Impostazioni progetto -> Account di servizio
 *      -> "Genera nuova chiave privata" -> salva come scripts/serviceAccountKey.json
 *   2. cd scripts && npm install firebase-admin
 *   3. node create-chats.js
 */
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

async function main() {
  const snapshot = await db.collection("users").get();
  const byRole = {};
  snapshot.forEach((doc) => {
    byRole[doc.data().ruolo] = doc.id;
  });

  const bambino = byRole["BAMBINO"];
  const mamma = byRole["MAMMA"];
  const babbo = byRole["BABBO"];

  if (!bambino || !mamma || !babbo) {
    console.error("Mancano ancora dei profili: fai fare il setup a tutti e tre prima di eseguire questo script.");
    console.error("Trovati finora:", byRole);
    return;
  }

  const chats = [
    { id: "bambino-mamma", tipo: "bambino-mamma", partecipanti: [bambino, mamma] },
    { id: "bambino-babbo", tipo: "bambino-babbo", partecipanti: [bambino, babbo] },
    { id: "famiglia", tipo: "bambino-mamma-babbo", partecipanti: [bambino, mamma, babbo] },
  ];

  for (const chat of chats) {
    await db.collection("chats").doc(chat.id).set(chat);
    console.log(`Creata chat: ${chat.id}`);
  }

  console.log("Fatto. Riapri l'app sui tre device per vedere le chat comparire.");
}

main();
