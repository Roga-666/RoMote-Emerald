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

The revert restores versionCode 35 in app/build.gradle. Android will not install that as an update over versionCode 36 (2.4.0) or 37 (2.4.1). For a rollback APK that preserves the installed app's data, set versionCode to a number greater than any version already installed (at least 38 after 2.4.1), choose a new versionName, and build with the same Emerald signing key. Do not uninstall merely to change notification styles; select Classic instead.

## Implementation and verification

- Classic stays in `notification_remote_expanded.xml`; it and both existing widget layouts are unchanged.
- Thumb-Friendly is isolated in `notification_remote_thumb.xml` and `thumb_remote_*` resources. Artwork scales with its native hit regions.
- `NotificationStyle` uses stable stored values; unknown values fall back to Classic.
- Only notification creation consults this preference. Widgets keep their explicit layouts.
- `tools/generate_thumb_artwork.py` regenerates the vector assets with matplotlib; it is not required to build the app.
- Tests cover both styles' command routing, saved selection, switching an active notification, dismissed-notification behavior, inert previews, constrained bounds, and aligned hit regions.
