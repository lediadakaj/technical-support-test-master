# Ticket: Saving order notes fails, frontend sends `text`, backend expects `body`

**Reported issue:** ISSUE-5: "Can't save notes on orders"

**Severity:** High. A relied-upon feature (shift-handover notes) is 100% broken for
saving, on every order, for every user. No data is lost or corrupted and reading
still works, but the team cannot record handovers at all. The fix is a low-risk change.

**Classification:** Frontend bug (request payload uses the wrong field name, frontend/backend contract mismatch).

## Summary

Saving a note always fails because the frontend posts the note under the JSON key
`text`, while the backend's `NoteRequest` expects the key `body`. The unknown `text`
field is ignored, `body` is left null, bean validation (`@NotBlank`) rejects the
request with HTTP 400, and the UI shows "Could not save the note." Reading notes is
unaffected because it uses a completely separate, correct path.

## Steps to reproduce

1. Open any order, e.g. `/orders/1`.
2. Existing notes display correctly.
3. Type anything in the note box and click **Save note**.
4. UI shows "Could not save the note. Please try again later." for any text, any order.

**Expected result:** the note is saved and appears in the list.

**Actual result:** every save fails, the POST returns HTTP 400.

## Investigation notes

1. **Narrowed read vs write.** Reading notes works (`GET /api/orders/{id}/notes`
   renders fine), only saving fails, so the problem is specific to the POST path, not notes as a whole.
2. **Compared the two sides of the save contract:**
   - Frontend (`frontend/src/pages/OrderDetail.jsx`, `saveNote`) posts
     `JSON.stringify({ text: noteText })`.
   - Backend (`src/main/java/com/orderdesk/dto/NoteRequest.java`) is
     `record NoteRequest(@NotBlank @Size(max=500) String body)` and the controller
     uses `request.body()`.
   The JSON key the client sends (`text`) does not match the field the server binds
   (`body`).
3. **Confirmed the mechanism by hitting the endpoint directly** (same order, both
   payloads):
   - `POST /api/orders/1/notes` with `{"text":"..."}` → **HTTP 400** (the unknown
     key is dropped, `body` is null, `@NotBlank` fails handled by
     `ApiExceptionHandler` as a 400 "Validation failed").
   - `POST /api/orders/1/notes` with `{"body":"..."}` → **HTTP 201 Created**, note
     persisted and returned.
   This proves the endpoint itself works, only the client's field name is wrong.
4. **Established which side is the outlier.** The whole rest of the notes feature uses
   `body`: the `OrderNote` model, the `NoteDto` response, and the frontend's own
   *render* of existing notes (`note.body` in `OrderDetail.jsx`). Only the save POST
   uses `text`. So `body` is the established contract and the POST is the lone
   inconsistency, the fix belongs on the frontend.

## Root cause

Field-name mismatch in the save request. `frontend/src/pages/OrderDetail.jsx`
serialises the note as `{ text: noteText }`, but the API contract (and everything
else in the feature) uses `body`. With `body` absent, `@NotBlank` on
`NoteRequest.body` fails and the request is rejected with HTTP 400, which the UI
surfaces as "Could not save the note."

## Proposed resolution

Send the note under the correct key:

```diff
- body: JSON.stringify({ text: noteText }),
+ body: JSON.stringify({ body: noteText }),
```

Chosen over changing the backend because `body` is already the contract used by the model, the response DTO and the note-rendering code, aligning the single outlier is
the minimal, consistent change and avoids touching a working, validated API.

**Verification:**
- Before fix: `POST /api/orders/1/notes` with `{"text":...}` → HTTP 400.
- After the fix, rebuilt the frontend and restarted, then exercised the real flow:
  the POST now sends `{"body":...}` → **HTTP 201**, the note persists and appears in
  the list. Reading existing notes still works (no regression). Validation still
  behaves correctly (an empty note is rejected by the same `@NotBlank` rule, which is
  the intended behaviour).

## Customer-facing update

Hello, thank you for the report. We have investigated this on our side and we figured it out that *saving* was broken across the board (reading was fine). When the Save button sent your note to the server, it was labelling the text with the wrong field name, so the
server rejected it as "empty" even though you'd typed something.

We have provided a fix on that, saving notes works again on every order.The failed saves didn't go through, so there's no cleanup needed on your side. Please resume using the
notes for shift handovers as normal, and let me know if you see any save fail again.

## Prevention 

- **Shared API contract.** Define the request/response shapes once and share
  them so the frontend cannot post a field name the backend doesn't accept. 
- **Integration test covering the real POST.** A test that submits the note exactly as the UI does and asserts HTTP 201 + persistence would have caught the broken
  contract immediately, a unit test that just calls the controller with a valid
  `NoteRequest` would not (it bypasses the JSON key). The test must go through the
  serialized JSON to be meaningful.
- **Reject unknown properties.** Configuring Jackson to fail on unknown JSON properties would have turned the silent "`text` ignored" into an
  explicit error naming the offending field, making the mismatch obvious in logs.