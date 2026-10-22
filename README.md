# Semgrep Rule Writing With Claude Enablement Lab

Writing a Semgrep Rule with the Claude Code `semgrep-rule-creator` Skill

**Time:** 15-20 min
**Scenario:** A customer wants to detect PII being passed into logger calls in their Kotlin/Java services. They have a list of sensitive field-name keywords (`email`, `ssn`, `password`, `token`, `apiKey`, ...) and want to catch both direct field access (`logger.info(user.email)`) and indirect flows through intermediate variables.

You will use the `semgrep-rule-creator` Claude Code skill — a test-first rule authoring workflow built by Trail of Bits — to produce a working rule from a starter test file.

---

## Prerequisites (do this before the meeting)

1. **Claude Code installed**, signed in, working in your terminal.
2. **Semgrep installed**: `brew install semgrep` (or `pip install semgrep`).

Verify:
```bash
claude --version
semgrep --version
```

---

## ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) `Step 1 — Install the skill (~2 min)`

Inside a Claude Code session, run:

```
/plugin install trailofbits/skills/plugins/semgrep-rule-creator
```

If you don't already have the Trail of Bits marketplace added, Claude will prompt you to add it first. Approve.

Confirm it's available — at the start of a fresh session you should see `semgrep-rule-creator` listed under available skills.

> **Why a skill, not just "ask Claude"?** Skills are Anthropic-format prompt bundles that load only when relevant. This one ships a test-first workflow, AST-inspection guidance, and anti-pattern checklists that produce far better rules than an unguided prompt.

---

## ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) `Step 2 — Open the lab directory (~1 min)` 

```bash
cd Semgrep-Rule-Writing-With-Claude-Enablement/starter
ls
# PiiLoggingTest.kt
```

Open `PiiLoggingTest.kt`. It contains ~25 Kotlin test cases the customer cares about:

- **True positives** (`// ruleid: pii-logged`) — direct field access, indirect through assignment, string templates, multiple PII fields per log line, a variety of sensitive field names.
- **True negatives** (`// ok: pii-logged`) — non-PII fields (`user.id`, `tx.transactionId`), string literals only, generic local variables.

This is what test-first rule writing looks like: the test file *is* the spec.

---

## ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) `Step 3 — Drive the skill (~10 min)` 


Start a Claude Code session in the `starter/` directory and prompt:

```
Use the semgrep-rule-creator skill to build a Semgrep rule that passes the
test annotations in PiiLoggingTest.kt.

Context:
- Target languages: kotlin and java
- Detect PII passed into logger calls (slf4j-style: logger.info/warn/error/debug/trace)
- PII is identified by field name matching keywords like: password, ssn, email,
  phone, address, account, routing, token, apiKey, secret, customerName,
  customerAddress, accountNumber, routingNumber, etc.
- Catch indirect flows (val emailAddr = user.email; logger.info(emailAddr))
- Don't match logger calls that only contain non-PII data or string literals

Write the rule as pii-logged.yaml in this directory, then run
`semgrep --test --config pii-logged.yaml PiiLoggingTest.kt` until all
annotations pass.
```

**What you should observe the skill doing:**

1. **Reads the test file first** — it treats the `ruleid:` / `ok:` annotations as the spec.
2. **Picks `mode: taint`** — pattern matching alone can't follow `val emailAddr = user.email` into a later `logger.info(emailAddr)`. Taint mode with `symbolic_propagation: true` handles that.
3. **Defines pattern-sources** — `$OBJ.$FIELD` with a `metavariable-regex` on `$FIELD` matching the PII keyword list. Note the `(?i)` for case-insensitive, and that camelCase variants (`apiKey`, `phoneNumber`) are listed explicitly because the regex is anchored.
4. **Defines pattern-sinks** — `$LOGGER.$METHOD(...)` with a `metavariable-regex` constraining `$METHOD` to known logger method names. This is broader than hardcoding `slf4j.Logger` and works across logging libraries.
5. **Runs `semgrep --test`** — and iterates if anything fails.

If you want to see the iteration loop in action, edit `PiiLoggingTest.kt` to add a tricky new case (see Stretch goals below) before prompting.

---

## ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) `Step 4 — Verify (~2 min)`


When Claude reports passing tests, run it yourself:

```bash
semgrep --test --config pii-logged.yaml PiiLoggingTest.kt
```

Expected output:
```
1/1: ✓ All tests passed
```

Open the generated `pii-logged.yaml` and compare it to `../solution/pii-logged.yaml`. They will likely differ in regex composition and metadata; that's fine — the test annotations are the source of truth.

---

## Stretch goals (if time allows)

Pick one to extend the rule. Add the test case first, watch it fail, then ask the skill to make it pass.

1. **Add a sanitizer.** Mark `mask(x)` and `redact(x)` as safe so `logger.info(mask(user.email))` doesn't fire. (`pattern-sanitizers:`)
2. **Cover structured logging.** Add `logger.info("user", kv("email", user.email))` and make it fire.
3. **Reduce a false positive.** Add `logger.info("Address book updated")` — a string literal that *contains* the word "address" but no PII variable. Confirm it doesn't fire (it shouldn't — the metavariable regex is anchored).
4. **Port to Python.** Use the related `semgrep-rule-variant-creator` skill to generate a Python equivalent for `logging.getLogger().info(...)` calls.

---

## Discussion points for the room (~3 min)

- **Why the skill beats "just prompt Claude":** the skill enforces *test-first*, AST inspection, and a checklist of anti-patterns (overly-broad patterns, missing safe cases). Those are exactly the failure modes we see when customers write their first rule.
- **Where this fits in a customer conversation:** a prospect with an in-house pattern-matching tool (the regex-based PII catcher Toast had) can be moved to Semgrep by showing the rule is *taint-aware* — it follows assignments and method calls, which a regex can't.
- **Limits of this rule:** it's name-based, not content-based. It won't catch a field called `value1` that happens to hold a Social Security Number. That's a different rule — and a good follow-up conversation about combining static rules with runtime data classification.

---

## Files in this lab

```
Semgrep-Rule-Writing-With-Claude-Enablement/
├── README.md                          this file
├── starter/
│   └── PiiLoggingTest.kt              annotated test cases (the "spec")
└── solution/
    ├── pii-logged.yaml                reference rule
    └── PiiLoggingTest.kt              copy of the test file for verification
```
