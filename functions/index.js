const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onValueWritten } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

/**
 * Notifica i destinatari di una chat quando arriva un nuovo messaggio.
 */
exports.onNewMessage = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    const message = event.data.data();
    const { chatId } = event.params;

    const chatDoc = await db.collection("chats").doc(chatId).get();
    const chat = chatDoc.data();
    if (!chat) return;

    const destinatari = (chat.partecipanti || []).filter((uid) => uid !== message.mittente);
    if (destinatari.length === 0) return;

    const [utenti, mittenteDoc] = await Promise.all([
      Promise.all(destinatari.map((uid) => db.collection("users").doc(uid).get())),
      db.collection("users").doc(message.mittente).get(),
    ]);

    const nomeMittente = mittenteDoc.data()?.nome || "Qualcuno";
    const tokens = utenti.map((doc) => doc.data()?.fcmToken).filter(Boolean);
    if (tokens.length === 0) return;

    await getMessaging().sendEachForMulticast({
      tokens,
      notification: {
        title: nomeMittente,
        body: message.testo,
      },
      data: { chatId },
    });
  }
);

/**
 * Quando bambino, mamma e babbo hanno tutti fatto il primo accesso, crea
 * automaticamente le tre chat di famiglia (sostituisce lo script manuale
 * scripts/create-chats.js).
 */
exports.onUserProfileWritten = onDocumentWritten(
  "users/{uid}",
  async (event) => {
    if (!event.data.after.exists) return;

    const snapshot = await db.collection("users").get();
    const byRole = {};
    snapshot.forEach((doc) => {
      byRole[doc.data().ruolo] = doc.id;
    });

    const bambino = byRole["BAMBINO"];
    const mamma = byRole["MAMMA"];
    const babbo = byRole["BABBO"];
    if (!bambino || !mamma || !babbo) return;

    const chats = [
      { id: "bambino-mamma", tipo: "bambino-mamma", partecipanti: [bambino, mamma] },
      { id: "bambino-babbo", tipo: "bambino-babbo", partecipanti: [bambino, babbo] },
      { id: "famiglia", tipo: "bambino-mamma-babbo", partecipanti: [bambino, mamma, babbo] },
    ];

    for (const chat of chats) {
      const ref = db.collection("chats").doc(chat.id);
      const existing = await ref.get();
      if (!existing.exists) {
        await ref.set(chat);
      }
    }
  }
);

/**
 * Avvisa mamma e babbo quando il bambino apre l'app (transizione a "online"
 * nel sistema di presenza della Realtime Database).
 */
exports.onBambinoOnline = onValueWritten(
  "/status/{uid}/online",
  async (event) => {
    const uid = event.params.uid;
    const prima = event.data.before.val();
    const dopo = event.data.after.val();

    if (dopo !== true || prima === true) return;

    const userDoc = await db.collection("users").doc(uid).get();
    const utente = userDoc.data();
    if (!utente || utente.ruolo !== "BAMBINO") return;

    const genitori = await db.collection("users").where("ruolo", "in", ["MAMMA", "BABBO"]).get();
    const tokens = genitori.docs.map((doc) => doc.data().fcmToken).filter(Boolean);
    if (tokens.length === 0) return;

    await getMessaging().sendEachForMulticast({
      tokens,
      notification: {
        title: "Picu",
        body: `${utente.nome} è online`,
      },
    });
  }
);
