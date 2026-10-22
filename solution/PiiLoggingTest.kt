import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val ssn: String,
    val password: String,
    val registrationDate: String
)

data class Transaction(
    val transactionId: String,
    val amount: Double,
    val customerName: String,
    val customerAddress: String
)

class Account(
    val accountNumber: String,
    val routingNumber: String,
    val token: String,
    val apiKey: String,
    val sessionId: String,
    val secret: String
)

val logger: Logger = LoggerFactory.getLogger("App")

// --- TRUE POSITIVES: Direct PII field access in logger ---

fun testDirectEmail(user: User) {
    // ruleid: pii-logged
    logger.info("User email: " + user.email)
}

fun testDirectPhone(user: User) {
    // ruleid: pii-logged
    logger.info("User phone: " + user.phoneNumber)
}

fun testDirectSsn(user: User) {
    // ruleid: pii-logged
    logger.warn("User SSN: " + user.ssn)
}

fun testDirectPassword(user: User) {
    // ruleid: pii-logged
    logger.error("Password: " + user.password)
}

fun testDirectCustomerName(tx: Transaction) {
    // ruleid: pii-logged
    logger.info("Customer: " + tx.customerName)
}

fun testDirectCustomerAddress(tx: Transaction) {
    // ruleid: pii-logged
    logger.info("Address: " + tx.customerAddress)
}

// --- TRUE POSITIVES: Indirect PII through variable assignment ---

fun testIndirectEmail(user: User) {
    val emailAddr = user.email
    // ruleid: pii-logged
    logger.info("Contact: " + emailAddr)
}

fun testIndirectSsn(user: User) {
    val socialNum = user.ssn
    // ruleid: pii-logged
    logger.debug("ID: " + socialNum)
}

// --- TRUE POSITIVES: String template with PII ---

fun testTemplateEmail(user: User) {
    // ruleid: pii-logged
    logger.info("User email: ${user.email}")
}

fun testTemplatePhone(user: User) {
    // ruleid: pii-logged
    logger.warn("Phone: ${user.phoneNumber}")
}

// --- TRUE POSITIVES: Multiple PII fields in one log ---

fun testMultiplePiiFields(user: User) {
    // ruleid: pii-logged
    logger.info("User: " + user.email + ", Phone: " + user.phoneNumber)
}

// --- TRUE POSITIVES: Various sensitive field names ---

fun testAccountNumber(acct: Account) {
    // ruleid: pii-logged
    logger.info("Account: " + acct.accountNumber)
}

fun testToken(acct: Account) {
    // ruleid: pii-logged
    logger.info("Token: " + acct.token)
}

fun testApiKey(acct: Account) {
    // ruleid: pii-logged
    logger.info("Key: " + acct.apiKey)
}

fun testSecret(acct: Account) {
    // ruleid: pii-logged
    logger.info("Secret: " + acct.secret)
}

// --- TRUE NEGATIVES: Non-PII fields ---

fun testSafeId(user: User) {
    // ok: pii-logged
    logger.info("User ID: " + user.id)
}

fun testSafeUsername(user: User) {
    // ok: pii-logged
    logger.info("Username: " + user.username)
}

fun testSafeRegistrationDate(user: User) {
    // ok: pii-logged
    logger.info("Registered: " + user.registrationDate)
}

fun testSafeTransactionId(tx: Transaction) {
    // ok: pii-logged
    logger.info("Transaction: " + tx.transactionId)
}

fun testSafeAmount(tx: Transaction) {
    // ok: pii-logged
    logger.info("Amount: " + tx.amount)
}

// --- TRUE NEGATIVES: String literal only ---

fun testSafeLiteralOnly() {
    // ok: pii-logged
    logger.info("Application started successfully")
}

fun testSafeStaticMessage() {
    // ok: pii-logged
    logger.warn("Connection pool exhausted")
}

// --- TRUE NEGATIVES: Non-PII variables ---

fun testSafeLocalVariable() {
    val status = "active"
    // ok: pii-logged
    logger.info("Status: " + status)
}

fun testSafeNumericVariable() {
    val count = 42
    // ok: pii-logged
    logger.debug("Count: " + count)
}
