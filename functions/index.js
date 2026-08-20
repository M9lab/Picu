const { onDocumentCreated } = require("firebase-functions/v2/firestore");
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
