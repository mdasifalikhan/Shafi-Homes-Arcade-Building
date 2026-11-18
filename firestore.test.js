const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_resident_1";
const BOB_UID = "bob_resident_2";
const ADMIN_EMAIL = "shafihomesarcade@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

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

test("Unauthenticated user: denied across all society collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("maintenance_dues")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("repair_tickets")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("contractors")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("schedule_entries")
      .get()
  );
});

test("UserProfile: owner create/update works; cross-user PII read & shadow fields denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTime(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      societyName: "Oakwood Residency",
      residentName: "Alice Sharma",
      unitLabel: "Tower A - 402",
      defaultMonthlyFee: 4500.0,
      activeRole: "RESIDENT",
      email: "alice@example.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Shadow/ghost field must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastTime(1000),
    })
  );

  // Valid partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeRole: "MANAGER",
      updatedAt: pastTime(1000),
    })
  );

  // Cross-user PII read & list must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("MaintenanceDue: create, partial payment transition, terminal lock, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTime(10000);
  const dueRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("maintenance_dues")
    .doc("due_nov_a402");

  // Create without optional null fields (Kotlin .filterValues { it != null })
  await assertSucceeds(
    dueRef.set({
      userId: ALICE_UID,
      unitNumber: "A-402",
      residentName: "Alice Sharma",
      billingPeriod: "Nov 2026",
      baseAmount: 3500.0,
      sinkingFund: 500.0,
      utilityAmount: 500.0,
      lateFee: 0.0,
      totalAmount: 4500.0,
      status: "PENDING",
      dueDateEpochDay: 20410,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("maintenance_dues")
      .doc("due_future")
      .set({
        userId: ALICE_UID,
        unitNumber: "A-403",
        residentName: "Future User",
        billingPeriod: "Dec 2026",
        baseAmount: 3500.0,
        sinkingFund: 500.0,
        utilityAmount: 500.0,
        lateFee: 0.0,
        totalAmount: 4500.0,
        status: "PENDING",
        dueDateEpochDay: 20440,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: ts,
      })
  );

  // Cross-user read & write rejected
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("maintenance_dues")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("maintenance_dues")
      .doc("due_nov_a402")
      .update({ status: "PAID", updatedAt: pastTime(1000) })
  );

  // Record payment -> PAID
  await assertSucceeds(
    dueRef.update({
      status: "PAID",
      paymentMethod: "UPI",
      receiptNumber: "RCP-202611-402",
      paidAtEpochMillis: Date.now() - 1000,
      updatedAt: pastTime(1000),
    })
  );

  // Terminal lock: owner cannot mutate after PAID
  await assertFails(
    dueRef.update({
      totalAmount: 100.0,
      status: "PENDING",
      updatedAt: pastTime(500),
    })
  );
});

test("Contractors, RepairTickets & ScheduleEntries: WriteBatch relational integrity & lifecycle", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTime(10000);

  const contractorRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("contractors")
    .doc("con_plumb_1");

  const ticketRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("repair_tickets")
    .doc("tkt_101");

  const scheduleRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("schedule_entries")
    .doc("sch_101");

  // 1. Create Contractor
  await assertSucceeds(
    contractorRef.set({
      userId: ALICE_UID,
      name: "Ramesh Kumar",
      company: "AquaFix Society Services",
      specialty: "PLUMBING",
      phoneSuffix: "4821",
      shiftStartHour: 8,
      shiftEndHour: 19,
      dailyCapacity: 4,
      hourlyRate: 450.0,
      available: true,
      completedJobs: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // 2. Orphaned ScheduleEntry (missing repair ticket) must fail existsAfter check
  await assertFails(
    scheduleRef.set({
      userId: ALICE_UID,
      ticketId: "tkt_missing",
      ticketTitle: "Orphan repair",
      unitLocation: "A-402",
      contractorId: "con_plumb_1",
      contractorName: "Ramesh Kumar",
      specialty: "PLUMBING",
      priority: "HIGH",
      scheduledDateEpochDay: 20411,
      timeSlot: "MORNING_09_12",
      status: "SCHEDULED",
      autoScheduled: true,
      matchReason: "Matched Plumbing",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // 3. Create RepairTicket
  await assertSucceeds(
    ticketRef.set({
      userId: ALICE_UID,
      unitLocation: "Tower A - 402",
      title: "Main riser valve leak",
      description: "Continuous drip near shaft shutoff valve on 4th floor.",
      category: "PLUMBING",
      priority: "HIGH",
      status: "LOGGED",
      estimatedCost: 1200.0,
      actualCost: 0.0,
      autoScheduled: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // 4. Atomic WriteBatch: auto-schedule entry + update ticket
  const batch = aliceDb.batch();
  batch.set(scheduleRef, {
    userId: ALICE_UID,
    ticketId: "tkt_101",
    ticketTitle: "Main riser valve leak",
    unitLocation: "Tower A - 402",
    contractorId: "con_plumb_1",
    contractorName: "Ramesh Kumar",
    specialty: "PLUMBING",
    priority: "HIGH",
    scheduledDateEpochDay: 20411,
    timeSlot: "MORNING_09_12",
    status: "SCHEDULED",
    autoScheduled: true,
    matchReason: "Specialty match (PLUMBING) • 0/4 daily slots used",
    createdAt: pastTime(2000),
    updatedAt: pastTime(2000),
  });
  batch.update(ticketRef, {
    status: "SCHEDULED",
    assignedContractorId: "con_plumb_1",
    assignedContractorName: "Ramesh Kumar",
    scheduledDateEpochDay: 20411,
    scheduledSlot: "MORNING_09_12",
    autoScheduled: true,
    updatedAt: pastTime(2000),
  });
  await assertSucceeds(batch.commit());

  // 5. Complete job batch + increment completedJobs
  const doneBatch = aliceDb.batch();
  doneBatch.update(scheduleRef, {
    status: "DONE",
    updatedAt: pastTime(500),
  });
  doneBatch.update(ticketRef, {
    status: "COMPLETED",
    actualCost: 1350.0,
    workNotes: "Replaced brass gate valve and pressure tested.",
    updatedAt: pastTime(500),
  });
  doneBatch.update(contractorRef, {
    completedJobs: 1,
    updatedAt: pastTime(500),
  });
  await assertSucceeds(doneBatch.commit());

  // 6. Terminal state lock after COMPLETED / DONE
  await assertFails(
    ticketRef.update({
      actualCost: 0.0,
      status: "LOGGED",
      updatedAt: pastTime(100),
    })
  );
  await assertFails(
    scheduleRef.update({
      status: "SCHEDULED",
      updatedAt: pastTime(100),
    })
  );
});

test("Bootstrapped admin: verified admin email can inspect records; unverified email spoof rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .set({
        userId: ALICE_UID,
        societyName: "Oakwood Residency",
        residentName: "Alice",
        unitLabel: "A-402",
        defaultMonthlyFee: 4500.0,
        activeRole: "RESIDENT",
        createdAt: pastTime(10000),
        updatedAt: pastTime(10000),
      });
  });

  const spoofedAdminDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: ADMIN_EMAIL,
      email_verified: false,
    })
    .firestore();
  await assertFails(spoofedAdminDb.collection("users").doc(ALICE_UID).get());

  const realAdminDb = testEnv
    .authenticatedContext("admin_uid", {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
  await assertSucceeds(realAdminDb.collection("users").doc(ALICE_UID).get());
});
