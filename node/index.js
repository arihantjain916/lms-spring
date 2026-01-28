const crypto = require("crypto");
const axios = require("axios")

const SECRET = "whsec_6a705f921c0e9703d1ebee0df6f316de";
const payload = JSON.stringify({ hello: "world" });

const timestamp = Math.floor(Date.now() / 1000);
const signedPayload = `${timestamp}.${payload}`;

const signature = crypto
  .createHmac("sha256", SECRET)
  .update(signedPayload)
  .digest("hex");

  console.log("signature", signature);


//  fetch("http://localhost:8080/api/webhook", {
//    method: "POST",
//    headers: {
//      "Content-Type": "application/json",
//      "X-Webhook-Signature": `t=${timestamp},v1=${signature}`
//    },
//    body: payload
//  });

(async function run() {
  const payload = { hello: "world" };
  const SECRET = "whsec_6a705f921c0e9703d1ebee0df6f316de";

  const body = JSON.stringify(payload);
  const timestamp = Math.floor(Date.now() / 1000);

  const signedPayload = `${timestamp}.${body}`;

  const signature = crypto
    .createHmac("sha256", SECRET)
    .update(signedPayload)
    .digest("hex");

  const res = await axios.post(
    "https://localhost:8080/api/webhook",
    body,
    {
      headers: {
        "Content-Type": "application/json",
        "X-Webhook-Signature": `t=${timestamp},v1=${signature}`
      }
    }
  );

  console.log("res:", res.data);
})();