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

const nowDate = () => new Date(Date.now() - 1000);

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

function validPropertyPayload(agentId, featured = false) {
  return {
    title: "Penthouse en Villa Morra",
    description: "Amplio penthouse con vista panorámica y terraza con parrilla en el corazón de Asunción.",
    operationType: "SALE",
    propertyType: "APARTMENT",
    price: 1850000000,
    currency: "PYG",
    department: "Asunción",
    city: "Asunción",
    neighborhood: "Villa Morra",
    address: "Av. Mariscal López esq. Senador Long",
    latitude: -25.2912,
    longitude: -57.5804,
    areaTotalM2: 245,
    areaBuiltM2: 210,
    bedrooms: 3,
    bathrooms: 4,
    parkingSpaces: 2,
    amenities: ["Piscina", "Quincho con parrilla", "Seguridad 24h"],
    imageUrls: ["villa_morra_1"],
    featured: featured,
    status: "active",
    agentId: agentId,
    agentName: "Carlos Benítez",
    agentAgency: "Yvy Capital Asunción",
    agentWhatsapp: "+595981234567",
    verified: true,
    viewsCount: 0,
    createdAt: nowDate(),
    updatedAt: nowDate(),
  };
}

function validVideoPayload(agentId) {
  return {
    propertyId: "prop_1",
    propertyTitle: "Penthouse en Villa Morra",
    agentId: agentId,
    planTierAtCreation: "PLAN_SENIOR",
    generationMode: "AUTOMATED_SLIDESHOW",
    aspectRatio: "9:16",
    selectedImages: ["villa_morra_1", "villa_morra_2"],
    includeBackgroundMusic: true,
    overlayHeadline: "Penthouse en Villa Morra",
    priceLabel: "Gs. 1.850.000.000",
    locationLabel: "Villa Morra, Asunción",
    highlightsLabel: "245 m² • 3 Dormitorios • 4 Baños",
    localVideoPath: "/data/user/0/com.aistudio.yvycapital.pykxmp/files/promo_prop_1.mp4",
    durationSeconds: 6,
    status: "READY",
    createdAt: nowDate(),
  };
}

test("1. Unauthenticated user: cannot read properties or profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("properties").where("status", "==", "active").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("2. User profile PII isolation: Alice cannot read Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Roberto Gómez",
      email: "roberto@yvy.com.py",
      preferredCurrency: "PYG",
      role: "CLIENT",
      planTier: "FREE",
      createdAt: nowDate(),
      updatedAt: nowDate(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("3. Advisor Membership Plans: enforces Plan Inicial max 5 listings & no featured listings in backend", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Create Alice with PLAN_INICIAL and 4 active listings
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alicia Duarte",
      email: "alicia@yvy.com.py",
      preferredCurrency: "PYG",
      role: "AGENT",
      planTier: "PLAN_INICIAL",
      activeListingsCount: 4,
      externalAgentMode: false,
      createdAt: nowDate(),
      updatedAt: nowDate(),
    })
  );

  // Plan Inicial cannot enable externalAgentMode
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      externalAgentMode: true,
      updatedAt: nowDate(),
    })
  );

  // Plan Inicial cannot publish a featured listing
  await assertFails(
    aliceDb.collection("properties").doc("prop_feat").set(validPropertyPayload(ALICE_UID, true))
  );

  // Plan Inicial can publish 5th non-featured listing when activeListingsCount == 4
  await assertSucceeds(
    aliceDb.collection("properties").doc("prop_5").set(validPropertyPayload(ALICE_UID, false))
  );

  // Update activeListingsCount to 5 (limit reached for PLAN_INICIAL)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeListingsCount: 5,
      updatedAt: nowDate(),
    })
  );

  // 6th listing must fail in backend for PLAN_INICIAL
  await assertFails(
    aliceDb.collection("properties").doc("prop_6").set(validPropertyPayload(ALICE_UID, false))
  );
});

test("4. Property creation: owner succeeds, spoofing agentId or ghost field fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("properties").doc("prop_1").set(validPropertyPayload(ALICE_UID, false))
  );

  // Spoof Bob's UID
  await assertFails(
    aliceDb.collection("properties").doc("prop_2").set(validPropertyPayload(BOB_UID, false))
  );

  // Shadow update with ghost field
  await assertFails(
    aliceDb.collection("properties").doc("prop_1").update({
      price: 1900000000,
      ghostAdminFlag: true,
      updatedAt: nowDate(),
    })
  );
});

test("5. Property query alignment: active properties query succeeds, unfiltered query fails", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("properties").doc("prop_active").set(validPropertyPayload(BOB_UID, false));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("properties").where("status", "==", "active").get()
  );
  await assertFails(
    aliceDb.collection("properties").get()
  );
});

test("6. Favorites isolation: Alice can manage her favorites, Bob cannot read or delete them", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("prop_1").set({
      userId: ALICE_UID,
      propertyId: "prop_1",
      title: "Penthouse en Villa Morra",
      price: 1850000000,
      currency: "PYG",
      operationType: "SALE",
      propertyType: "APARTMENT",
      city: "Asunción",
      neighborhood: "Villa Morra",
      areaTotalM2: 245,
      bedrooms: 3,
      bathrooms: 4,
      createdAt: nowDate(),
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").doc("prop_1").get()
  );
});

test("7. Inquiries: client can create, agent can update status, terminal CLOSED state is locked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("inquiries").doc("inq_1").set({
      propertyId: "prop_1",
      propertyTitle: "Penthouse en Villa Morra",
      userId: ALICE_UID,
      userName: "Alicia Duarte",
      userEmail: "alicia@yvy.com.py",
      userPhone: "+595981111222",
      agentId: BOB_UID,
      inquiryType: "VISIT",
      preferredDate: "Sábado 10:00 hs",
      message: "Me gustaría agendar una visita este sábado por la mañana.",
      status: "PENDING",
      createdAt: nowDate(),
      updatedAt: nowDate(),
    })
  );

  // Agent (Bob) updates status to CLOSED
  await assertSucceeds(
    bobDb.collection("inquiries").doc("inq_1").update({
      status: "CLOSED",
      updatedAt: nowDate(),
    })
  );

  // Terminal state lock: cannot update once CLOSED
  await assertFails(
    bobDb.collection("inquiries").doc("inq_1").update({
      status: "CONTACTED",
      updatedAt: nowDate(),
    })
  );
});

test("8. Senior Video Generator Backend Enforcement: blocks PLAN_INICIAL and PLAN_INTERMEDIO, allows PLAN_SENIOR", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Plan Inicial cannot create promo videos
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alicia Duarte",
      email: "alicia@yvy.com.py",
      preferredCurrency: "PYG",
      role: "AGENT",
      planTier: "PLAN_INICIAL",
      activeListingsCount: 1,
      externalAgentMode: false,
      createdAt: nowDate(),
      updatedAt: nowDate(),
    })
  );
  await assertFails(
    aliceDb.collection("property_videos").doc("vid_1").set(validVideoPayload(ALICE_UID))
  );

  // Plan Intermedio cannot create promo videos
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      planTier: "PLAN_INTERMEDIO",
      updatedAt: nowDate(),
    })
  );
  await assertFails(
    aliceDb.collection("property_videos").doc("vid_1").set(validVideoPayload(ALICE_UID))
  );

  // Plan Senior succeeds creating promo videos
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      planTier: "PLAN_SENIOR",
      updatedAt: nowDate(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("property_videos").doc("vid_1").set(validVideoPayload(ALICE_UID))
  );
});
