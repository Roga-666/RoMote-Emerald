# RoMote Emerald

RoMote Emerald is an unofficial, independently maintained fork of
[wseemann/RoMote](https://github.com/wseemann/RoMote), an open-source Android
remote for Roku players and Roku TVs.

This fork retains RoMote's core remote-control functionality while adding an
emerald visual identity and faster access to commonly used controls. It is not
affiliated with or endorsed by the original RoMote developer, Roku, Google
Play, or F-Droid.

## Emerald features

- Expanded notification remote with Up, Down, Left, Right, OK, Power, Home,
  and Mute controls
- Notification shown automatically after opening a connected remote
- Quick Settings tile that can restore the notification manually
- Emerald application, remote-tab, widget, and notification styling
- Custom RoMote Emerald launcher icon
- Separate Android package name so it can coexist with the original RoMote app
- Automated upstream synchronization, testing, signing, and APK releases

## Downloads

RoMote Emerald is not distributed through the original RoMote listings on
Google Play or F-Droid. Builds produced for this fork are published on this
repository's [Releases](https://github.com/Roga-666/RoMote-Emerald/releases)
page when a release is available.

## Using the remote

If RoMote Emerald cannot detect your Roku device or send commands, make sure
Roku's **Control by mobile apps** setting is enabled. Roku documents that
setting in its [mobile-app troubleshooting guide](https://support.roku.com/article/217288467#section-2).

## Building

The app has two product flavors:

| Flavor | Build command | Notes |
|---|---|---|
| `play` | `./gradlew assemblePlayRelease` | Adds the Google Play in-app review dependency. |
| `foss` | `./gradlew assembleFossRelease` | Fully free build with no proprietary dependencies; the review prompt is a no-op. |

`play` is the default flavor for local development. Release builds use the
following environment variables, which should be stored as encrypted secrets
and never committed:

- `ROMOTE_EMERALD_KEYSTORE_BASE64`
- `ROMOTE_EMERALD_KEYSTORE_PASSWORD`
- `ROMOTE_EMERALD_KEY_ALIAS`
- `ROMOTE_EMERALD_KEY_PASSWORD`

## Automated upstream updates

The `sync-upstream.yml` workflow checks the official RoMote `master` branch
weekly and can also be run manually. When upstream changes, it attempts a
normal Git merge, runs the tests, builds and verifies a signed APK, pushes the
verified merge, and publishes a GitHub Release. If the merge conflicts, the
workflow opens an issue instead of publishing an unverified build.

No automated process can guarantee compatibility with every future upstream
redesign. A reported merge conflict means the Emerald modifications need a
manual update.

## Privacy

Like the upstream project, RoMote Emerald does not collect or share user data.

## Attribution and license

RoMote Emerald is derived from
[RoMote by William Seemann](https://github.com/wseemann/RoMote). The original
project's copyright notice, commit history, and contributor attribution are
retained. The Emerald branding, notification controls, Quick Settings access,
widget changes, and related integration work are modifications made for this
fork.

The project remains licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0). See
[`LICENSE`](LICENSE) for the complete terms. The Apache License does not imply
endorsement by the original author or grant rights to third-party trademarks.
