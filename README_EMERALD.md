# RoMote Emerald

RoMote Emerald is a personal, emerald-themed fork of
[wseemann/RoMote](https://github.com/wseemann/RoMote). It keeps the original
Roku remote functionality and adds an expanded notification remote, a Quick
Settings launcher tile, an emerald home-screen widget, and custom branding.

## Emerald controls

- The expanded notification provides Up, Down, Left, Right, OK, Power, Home,
  and Mute controls.
- Opening the connected remote shows the persistent notification automatically.
- The Quick Settings tile can restore the notification manually.
- The app, widget, notification controls, and launcher artwork use the Emerald
  visual theme.

## Automated upstream updates

`sync-upstream.yml` checks the official `master` branch each week and can also
be run manually. When upstream has changed, it attempts a normal Git merge,
runs the unit tests, and builds RoMote Emerald. A successful update is pushed
to this repository and published as a GitHub Release. If Git cannot merge the
changes cleanly, the workflow opens an issue instead of publishing an
unverified APK.

No updater can guarantee that a customization will apply to every future
upstream redesign. A reported merge conflict means the Emerald patch needs a
small manual update.

## Signing configuration

Release builds use four environment variables. Their values belong in GitHub
Actions repository secrets and must never be committed:

- `ROMOTE_EMERALD_KEYSTORE_BASE64`
- `ROMOTE_EMERALD_KEYSTORE_PASSWORD`
- `ROMOTE_EMERALD_KEY_ALIAS`
- `ROMOTE_EMERALD_KEY_PASSWORD`

The same permanent key must sign every release so Android accepts future APKs
as updates.

## License

This fork retains RoMote's Apache License 2.0 and upstream copyright notice.
