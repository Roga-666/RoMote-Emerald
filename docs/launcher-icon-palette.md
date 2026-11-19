# Launcher icon palette (2.4.7)

The existing launcher artwork is recolored from its original green accent (#00B978) toward the notification/widget emerald (#00DDA3). This is a direct lossless pixel-color adjustment, without image generation, resizing, tracing or redrawing. Green shading and texture positions are retained. Dimensions, alpha values and all non-green pixels are unchanged.

The red power indicator is a separate vector layer (`ic_launcher_power_dot.xml`) composed by `ic_launcher_foreground.xml`. Both files, the adaptive-icon structure and the monochrome icon remain unchanged. Complete previews must render the foreground layer-list, rather than only `ic_launcher_art.png`, so the red indicator is included.

`tools/recolor_launcher_icon.py ORIGINAL.png OUTPUT.png` reproduces the color edit using Pillow. Supply the pre-recolor image from commit `ce733c968f2b28efd7b33a78e7e81b8f1e53c112` as ORIGINAL; do not repeatedly recolor the already-adjusted image. The script checks unchanged dimensions, alpha and non-green pixels before saving. The native launcher composition test checks the original red indicator and renders the complete icon for review.

The palette update is isolated on branch `feature/consistent-launcher-emerald`. Revert its feature commit to restore the old colors. Preserve the release version-code integration and publish a rollback with a higher code so installed apps can update without uninstalling.
