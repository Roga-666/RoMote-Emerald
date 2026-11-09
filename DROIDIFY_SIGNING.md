# Signed RoMote Emerald update compatibility

RoMote Emerald already uses permanent release-signing credentials in the `sync-upstream.yml` workflow:

- `ROMOTE_EMERALD_KEYSTORE_BASE64`
- `ROMOTE_EMERALD_KEYSTORE_PASSWORD`
- `ROMOTE_EMERALD_KEY_ALIAS`
- `ROMOTE_EMERALD_KEY_PASSWORD`

**Do not replace the existing keystore.** Changing it will prevent Android from updating existing installations. Store a secure offline backup of the original signing keystore/passwords.

This change makes signed CI version codes monotonic using `100000 + workflow run number`; it preserves the package name `wseemann.media.romote.emerald`. Before publishing a new Emerald release, it downloads the latest prior signed release and validates that the signing certificate and package ID match and that versionCode increases.

The debug build workflow remains separate and should not be distributed as an in-place update to the signed release.
