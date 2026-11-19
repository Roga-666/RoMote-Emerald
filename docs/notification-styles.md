# Notification styles and rollback

RoMote Emerald 2.4.0 adds **Settings → Notification style**. Thumb-Friendly is the default; choose Classic to restore the v2.3.2 control layout immediately. The selection persists across app restarts. An active remote notification updates when you select a style; a dismissed notification stays dismissed until reopened normally.

The previews inflate the actual control layouts without binding Roku commands. Your phone supplies the notification header and can allocate a different size. The existing home-screen and LockStar widgets retain their current layouts.

## Restore Classic without rebuilding

Open Settings → Notification style → Classic. This is the simplest way to undo the visual change on an installed app.

## Revert the code change

The original feature is isolated in commit `277b7e0a4f1225171dbd0754f99b24f10ea2301d`, on branch `feature/notification-style-picker`. This document is a separate commit.

Version 2.4.1 removes the five auxiliary labels and centers their icons in a separate commit, `257f5125b09d7ea2f9e9ae38f3b8fc76a4c852c0`. To restore only the labeled 2.4.0 artwork:

```bash
git revert --no-edit 257f5125b09d7ea2f9e9ae38f3b8fc76a4c852c0
```

To remove the complete style-picker feature from a clean checkout containing 2.4.1, revert the icon cleanup first, then the original feature:

```bash
git revert --no-edit 257f5125b09d7ea2f9e9ae38f3b8fc76a4c852c0
git revert --no-edit 277b7e0a4f1225171dbd0754f99b24f10ea2301d
```

If the icon cleanup is already reverted, run only the second command.

This reverses only the style picker, new artwork/layout, associated preferences and tests, and their version bumps. It retains all earlier notification sizing, hardware-volume, and widget changes from v2.3.2. The inverse patch was verified to restore the exact baseline tree.

If later commits touch the same files, Git may ask you to resolve conflicts. Use `git revert --abort` to cancel that attempt. When merging this PR, preserve the feature commit with a merge commit rather than squashing, so its rollback identity remains available.

The unchanged baseline is commit `bc944da8a914e043a44d3df84d41d1d45ae57165` on `feature/emerald-volume-lockscreen`. To inspect it independently without altering your working checkout:

```bash
git worktree add --detach ../romote-before-notification-styles bc944da8a914e043a44d3df84d41d1d45ae57165
```

## Install a build after reverting

The revert restores versionCode 35 in app/build.gradle. Android will not install that as an update over versionCode 36 (2.4.0) or 37 (2.4.1). Published builds use versionCode 100000 plus the publisher workflow run number. For a rollback APK that preserves installed data, keep the release versioning integration and use the publisher so it assigns a new, higher code. For a manual build, choose a versionCode greater than the currently installed release and use the same Emerald signing key. Later release changes to app/build.gradle may need to be retained when resolving a revert conflict. Do not uninstall merely to change notification styles; select Classic instead.

## Implementation and verification

- Classic stays in `notification_remote_expanded.xml`; it and both existing widget layouts are unchanged.
- Thumb-Friendly is isolated in `notification_remote_thumb.xml` and `thumb_remote_*` resources. Artwork scales with its native hit regions.
- `NotificationStyle` uses stable stored values; unknown values fall back to Classic.
- Only notification creation consults this preference. Widgets keep their explicit layouts.
- `tools/generate_thumb_artwork.py` regenerates the vector assets with matplotlib; it is not required to build the app.
- Tests cover both styles' command routing, saved selection, switching an active notification, dismissed-notification behavior, inert previews, constrained bounds, and aligned hit regions.

## Six-button Thumb-Friendly layout (2.4.3)

The auxiliary controls now use three columns and two rows: Play/Pause, Home, Power; then Mute, Volume down, Volume up. Icons remain centered with no visible labels; accessibility descriptions remain available. Play/Pause sends the same Roku PLAY toggle as the slim widget. Classic and both home-screen widgets remain unchanged. The D-pad vector and its hit-region proportions are unchanged; the left panel receives more width and both panels scale to the space supplied by Android.

This update is isolated on branch `feature/thumb-six-button-grid`. Revert its feature commit to restore the previous five-button Thumb-Friendly layout. Keep the publisher version-code integration and publish the revert with a higher CI version code so existing installations can update normally.

## Large independent controls (2.4.4)

Thumb-Friendly now uses individual image buttons in two columns and three rows. Left column: Volume up, Volume down, Mute. Right column: Power, Home, Play/Pause. The buttons fill the available left panel, with 8dp non-command gaps between them and the D-pad. They retain the existing icon shapes, emerald tint and graphite faces, with no visible labels. At a 280×164dp content area, each auxiliary target is at least 48dp in both directions and icons are at least 36dp. Very narrow or short system allocations must still scale down; no minimum-size promise is made for those hosts.

The D-pad artwork and hit-region proportions are unchanged. Removing our outer padding makes more of the app-owned content usable. The status-bar small icon is now a dedicated monochrome remote silhouette with transparent cutouts, rather than the Quick Settings tile artwork.

Android 12+ supplies a standard notification frame. The app can fill the custom content bounds, but cannot promise to reclaim Samsung's icon/header gutter or system padding. Settings previews show the app-owned controls, not a simulation of One UI's frame. Native layout tests validate panel fill, requested order, real gaps, non-overlap, icon size and 48dp targets at the stated content size; they do not constitute testing on a physical Samsung phone.

This update is isolated on branch `feature/comfortable-notification-controls`. Revert its feature commit to restore 2.4.3's three-column layout and previous small icon. Preserve CI version-code integration and publish the revert with a higher version code. Classic and the home-screen/LockStar widgets retain their layouts.

## Larger proportional D-pad (2.4.5)

The right panel now receives enough width for a 168dp D-pad at 280×180dp custom-content bounds (previously about 146dp). At a 164dp height it scales to 164dp. The original square vector and all overlay ratios remain unchanged, so the arrows, OK button and tap areas grow together.

The auxiliary faces narrow slightly to 48dp at 280dp content width. Their horizontal padding decreases from 6dp to 3dp so the approved 42dp icons at 180dp height (about 37dp at 164dp height) retain their scale. Button order, height, colors and 8dp gaps are unchanged. At 280×180dp all eleven command targets remain at least 48dp in each direction. Narrower allocations must scale down; tests still cover 216dp and 160dp hosts for fitting, with auxiliary targets at least 35dp wide at 216dp.

This adjustment is isolated on branch `feature/larger-proportional-dpad`. Revert its feature commit to restore 2.4.4 sizing; retain publisher versioning and publish the revert with a higher version code.
