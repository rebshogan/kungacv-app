# KungaCV Security Specification

## 1. Data Invariants
1. **Strict User Ownership & Isolation**: Every document in `/users/{userId}/resumes/{resumeId}`, `/users/{userId}/coverLetters/{letterId}`, and `/users/{userId}/chatMessages/{messageId}` must belong exclusively to `userId == request.auth.uid` and carry an immutable `userId` field matching `request.auth.uid`.
2. **Strict Key Allowlisting & Schema Bounds**: Every create and update operation must validate all required keys, reject unknown "ghost" keys via `hasOnly()`, enforce string length bounds, and enforce bounded array sizes.
3. **Temporal & Immortal Field Integrity**: `createdAt` and `userId` are immutable on update (`incoming().createdAt == existing().createdAt` and `incoming().userId == existing().userId`). All timestamps must be server timestamps (`<= request.time`).

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `GET /users/alice/resumes/r1` with `auth = null` -> REJECT.
2. **Cross-User PII Read**: `GET /users/bob/resumes/r1` as `alice` -> REJECT.
3. **Identity Spoofing on Create**: `CREATE /users/alice/resumes/r1` with `userId: "bob"` -> REJECT.
4. **Shadow/Ghost Field Injection**: `CREATE /users/alice/resumes/r1` with `isAdmin: true` -> REJECT.
5. **String Oversize DoS**: `CREATE /users/alice/resumes/r1` with `title` of 500 chars (> 120 max) -> REJECT.
6. **Invalid Template Enum**: `CREATE /users/alice/resumes/r1` with `templateId: "hacked_template"` -> REJECT.
7. **Unbounded Array Injection**: `CREATE /users/alice/resumes/r1` with `skills` array of 60 items (> 50 max) -> REJECT.
8. **Future Timestamp Spoofing**: `CREATE /users/alice/resumes/r1` with `createdAt` in the future (`> request.time`) -> REJECT.
9. **Immortal Field Mutation (`createdAt`)**: `UPDATE /users/alice/resumes/r1` mutating `createdAt` -> REJECT.
10. **Owner Reassignment (`userId`)**: `UPDATE /users/alice/resumes/r1` mutating `userId` to `"bob"` -> REJECT.
11. **Value Poisoning on Update**: `UPDATE /users/alice/coverLetters/c1` setting `tone: "InvalidTone"` -> REJECT.
12. **Chat Message Role Spoofing**: `CREATE /users/alice/chatMessages/m1` with `role: "system_admin"` -> REJECT.
