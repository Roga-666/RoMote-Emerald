"""Recolor only the original launcher's green pixels; never resize or redraw it.

Usage: python tools/recolor_launcher_icon.py ORIGINAL.png OUTPUT.png
Requires Pillow. Use the pre-recolor image from git as ORIGINAL when regenerating.
"""
import argparse
import json
from pathlib import Path

from PIL import Image


def recolor(source: Path, destination: Path) -> None:
    original = Image.open(source).convert('RGBA')
    result = original.copy()
    before, after = original.load(), result.load()
    changed = 0
    for y in range(original.height):
        for x in range(original.width):
            r, g, b, a = before[x, y]
            if a and g > r and g > b and b >= r:
                # Retain the neutral component and relative green shading. This maps
                # the original #00B978 accent exactly to the widget's #00DDA3.
                green = min(255, round(r + (g - r) * 221 / 185))
                blue = min(green, round(r + (b - r) * 163 / 120))
                pixel = (r, green, blue, a)
                after[x, y] = pixel
                changed += pixel != before[x, y]
    assert original.size == result.size
    assert original.getchannel('A').tobytes() == result.getchannel('A').tobytes()
    for y in range(original.height):
        for x in range(original.width):
            r, g, b, a = before[x, y]
            if not (a and g > r and g > b and b >= r):
                assert after[x, y] == before[x, y], 'A non-green pixel changed'
    destination.parent.mkdir(parents=True, exist_ok=True)
    options = {'icc_profile': original.info['icc_profile']} if original.info.get('icc_profile') else {}
    result.save(destination, **options)
    print(json.dumps({'size': result.size, 'changed_green_pixels': changed,
                      'alpha_unchanged': True, 'non_green_pixels_unchanged': True}))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', type=Path)
    parser.add_argument('destination', type=Path)
    args = parser.parse_args()
    recolor(args.source, args.destination)
