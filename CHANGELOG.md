# Changelog

## 5.0.0

### Breaking Changes

- Encrypted file format changed — plaintext password hash replaced with AES-GCM encrypted sentinel. Files from v4.x are not compatible.
- All password parameters changed from `String` to `char[]` across `FileEncryptorAES`, `FileDecryptorAES`, `PBKDF2Helper`, and `SecurePreferencesImpl`.
- PBKDF2 iteration defaults raised to OWASP 2023 minimums: `LOW` = 600,000, `MEDIUM` = 1,000,000, `HIGH` = 2,000,000. `CipherAES` and `PBKDF2Helper.Builder` default iterations raised to 600,000.
- `PBKDF2Helper.Builder.build()` parameter swap fixed — `saltSize` and `iterations` were passed in the wrong order. Serialized configs from v4.x will produce different keys.
- Domain exception hierarchy added: `CryptoException`, `EncryptionException`, `DecryptionException`, `InvalidPasswordException`, `KeyDerivationException`. All crypto classes throw these instead of `IllegalStateException`.
- `FileEncryptorAES` is now single-use — second call to `encrypt()` throws `IllegalStateException`.
- `FileDecryptorAES`, `ConcurrentCache`, and `SecurePreferences` now implement `AutoCloseable`.
- `DigestType` enum removed (was unused).
- Java source/target raised from 8 to 11.
- Version bumped from 4.0.0 to 5.0.0.

### Security Fixes

- Fixed `SecureRandom` seeded with `System.currentTimeMillis()` — now uses OS entropy.
- Removed plaintext SHA-256 password hash from encrypted file header (was an offline brute-force oracle). Replaced with AES-GCM encrypted sentinel for fast password validation without exposing key material.
- Passwords defensively copied and zeroed after use via `Arrays.fill`. `PBEKeySpec.clearPassword()` called after key derivation.
- `Constants.SENTINEL_PLAINTEXT` made private with defensive copy getter to prevent mutation.
- Fixed `ConcurrentCache.Holder.lastAccessed` race condition — now `volatile`.

### Bug Fixes

- Fixed `ConcurrentCache.putIfAbsent` NPE when no prior mapping exists.
- Fixed `FileEncryptorAES`/`FileDecryptorAES` buffer handling — was writing full buffer on partial reads, corrupting output with stale bytes.
- Fixed `FileEncryptorAES`/`FileDecryptorAES` progress calculation division by zero when `available()` < buffer size.
- Fixed `ImageCompressor` lock leak — locks now released in `finally` blocks. Eliminated TOCTOU race in `setSourceImage`.
- Fixed `LuhnChecker.checkNumberValidity("")` — was throwing `ArrayIndexOutOfBoundsException`, now throws `IllegalArgumentException`.

### Code Quality

- All classes marked `final`. Private constructors added to utility classes. Enum fields marked `final`.
- Null-check guard clauses added to all public methods.
- Wildcard imports replaced with explicit imports.
- `FileEncryptorAES.encrypt()` decomposed into `writeHeader()`, `writeSentinel()`, `encryptData()`.
- `FileDecryptorAES.decrypt()` decomposed into `readFileIv()`, `readPbkdf2Config()`, `validateSentinel()`, `decryptData()`.
- `HashSHA` error messages now include algorithm name and chain the cause.

### Dependencies

- Bouncy Castle `1.59` -> `1.78.1` (artifact changed from `bcprov-jdk15on` to `bcprov-jdk18on`)
- commons-codec `1.10` -> `1.17.1`
- JUnit `4.12` -> `4.13.2` (added `<scope>test</scope>`)
- maven-compiler-plugin `3.7.0` -> `3.13.0`
- maven-javadoc-plugin `2.10.4` -> `3.10.1`

### Tests

- Test count: 37 -> 70.
- Fixed `loadImageAsBytes` buffer bug in `ImageCompressionUtilsTest` and `ImageCompressorTest` — was writing full buffer on partial reads, identical to the production bug fixed above.
- Fixed `FileInputStream` resource leak in image test helpers.
- Fixed shared mutable `ImageCompressor` state across tests — each test now creates its own instance.
- Fixed `SecurePreferencesTest` persistent side effects — `Preferences` node cleaned up in `@Before`/`@After`.
- Moved test images from `test-images/` to `src/test/resources/test-images/` for IDE compatibility.
- All test methods renamed to scenario-based names.
- Added null guard tests for `HashSHA`, `CloneUtils`, `LuhnChecker`, `PBKDF2Helper`, `FileDecryptorAES`.
- Added known-answer SHA-256 test against NIST-published digest.
- Added empty input / boundary tests for `CipherAES`, `LuhnChecker`, `FileEncryptorDecryptorAES`.
- Added `putIfAbsent`, `containsKey`, `remove` tests for `ConcurrentCache`.
- Added `Double`, `Long`, missing-key tests for `SecurePreferencesTest`.
- Added `PBKDF2Helper.Builder` validation tests (null guards, malformed config, all key sizes).
- Added `FileDecryptorAES` reuse test, `ProgressMonitor` callback test, empty file round-trip test.
