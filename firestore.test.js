const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validResumePayload(userId, now) {
  return {
    userId,
    title: "Executive Product Resume",
    templateId: "executive_navy",
    fullName: "Alice Vance",
    jobTitle: "Principal Product Manager",
    email: "alice@example.com",
    phone: "+1 555 0192",
    location: "London, UK",
    website: "https://alicevance.dev",
    summary: "Seasoned product leader with 10+ years scaling B2B platforms.",
    experiences: [
      {
        id: "exp_1",
        role: "Principal PM",
        company: "Vertex Systems",
        period: "2022 - Present",
        location: "London",
        highlights: ["Increased ARR by 42% across enterprise accounts."]
      }
    ],
    educations: [
      {
        id: "edu_1",
        degree: "MSc Computer Science",
        institution: "Imperial College London",
        year: "2016",
        details: "Distinction"
      }
    ],
    skills: ["Product Strategy", "Kotlin", "AI Systems"],
    projects: [
      {
        id: "proj_1",
        name: "KungaCV",
        role: "Creator",
        description: "AI Resume Studio",
        link: "https://github.com/vend-pixel/kungacv"
      }
    ],
    createdAt: now,
    updatedAt: now,
  };
}

test("1. Unauthenticated user cannot read or create resumes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("resumes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("resumes").doc("r1").set(validResumePayload(ALICE_UID, new Date()))
  );
});

test("2. Authenticated owner can create, read, list, update, and delete their resume", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const createdAt = new Date(Date.now() - 2000);
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("resumes").doc("resume_1");

  await assertSucceeds(docRef.set(validResumePayload(ALICE_UID, createdAt)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("resumes").where("userId", "==", ALICE_UID).get()
  );

  await assertSucceeds(
    docRef.update({
      title: "Updated Executive CV",
      templateId: "editorial_serif",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("3. Cross-user access is strictly rejected", async () => {
  const createdAt = new Date(Date.now() - 2000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).collection("resumes").doc("bob_resume").set(
      validResumePayload(BOB_UID, createdAt)
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("resumes").doc("bob_resume").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("resumes").where("userId", "==", BOB_UID).get());
});

test("4. Shadow update / ghost field injection is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payload = {
    ...validResumePayload(ALICE_UID, new Date(Date.now() - 1000)),
    isVerifiedAdmin: true,
  };
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("resumes").doc("resume_ghost").set(payload)
  );
});

test("5. Invalid templateId enum or immutable createdAt mutation is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const createdAt = new Date(Date.now() - 5000);
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("resumes").doc("resume_enum");
  await assertSucceeds(docRef.set(validResumePayload(ALICE_UID, createdAt)));

  await assertFails(
    docRef.update({
      templateId: "invalid_template_id",
      updatedAt: new Date(),
    })
  );

  await assertFails(
    docRef.update({
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("6. Cover letter and Chat message rules enforce ownership and schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  const letterRef = aliceDb.collection("users").doc(ALICE_UID).collection("coverLetters").doc("cl_1");
  await assertSucceeds(
    letterRef.set({
      userId: ALICE_UID,
      resumeId: "resume_1",
      companyName: "Stripe",
      targetRole: "Staff Product Manager",
      jobDescription: "Lead global payments infrastructure.",
      tone: "Executive",
      content: "Dear Hiring Committee, I am writing to express my interest...",
      createdAt: now,
      updatedAt: now,
    })
  );

  const chatRef = aliceDb.collection("users").doc(ALICE_UID).collection("chatMessages").doc("msg_1");
  await assertSucceeds(
    chatRef.set({
      userId: ALICE_UID,
      role: "user",
      text: "Can you help me quantify my product leadership bullets?",
      modelTier: "gemini-3.8-flash",
      suggestedSummary: "",
      createdAt: now,
    })
  );

  // Chat messages are append-only (update forbidden, delete allowed by owner)
  await assertFails(chatRef.update({ text: "Modified history" }));
  await assertSucceeds(chatRef.delete());
});
