"""Regenerate the optional Thumb-Friendly vectors (requires matplotlib; not used by the APK build)."""
from pathlib import Path
import xml.etree.ElementTree as ET
from matplotlib.textpath import TextPath
from matplotlib.font_manager import FontProperties

RES = Path(__file__).resolve().parents[1] / 'app/src/main/res'
NS = 'http://schemas.android.com/apk/res/android'
ET.register_namespace('android', NS)
A = lambda key: '{' + NS + '}' + key
GREEN = '#FF00DDA3'
DARK = '#FF142024'

def vector(width, height):
    return ET.Element('vector', {A('width'): f'{width}dp', A('height'): f'{height}dp', A('viewportWidth'): str(width), A('viewportHeight'): str(height)})

def path(parent, data, color=None, stroke=None, width=1):
    attrs = {A('pathData'): data, A('fillColor'): color or '#00000000'}
    if stroke:
        attrs.update({A('strokeColor'): stroke, A('strokeWidth'): str(width)})
    return ET.SubElement(parent, 'path', attrs)

def shade(element, color):
    # Solid graphite keeps emerald outlines consistent across RemoteViews renderers.
    element.set(A('fillColor'), color)

def circle(parent, x, y, radius, color, stroke=None, width=1):
    return path(parent, f'M{x},{y-radius} A{radius},{radius} 0,1 0,{x},{y+radius} A{radius},{radius} 0,1 0,{x},{y-radius} Z', color, stroke, width)

def label(parent, value, x, baseline, size, color):
    glyphs = TextPath((0, 0), value, size=size, prop=FontProperties(family='DejaVu Sans', weight='bold'))
    bounds = glyphs.get_extents()
    offset = x - (bounds.x0 + bounds.x1) / 2
    commands = []
    for vertices, code in glyphs.iter_segments(curves=True):
        if code == 79:
            commands.append('Z')
            continue
        points = [f'{vertices[i] + offset:.3f},{baseline - vertices[i+1]:.3f}' for i in range(0, len(vertices), 2)]
        commands.append({1:'M',2:'L',3:'Q',4:'C'}[code] + ' '.join(points))
    path(parent, ' '.join(commands), color)

def icon(parent, name, x, y, size):
    source = ET.parse(RES / f'drawable/ic_notification_{name}.xml').getroot()
    group = ET.SubElement(parent, 'group', {A('translateX'):str(x), A('translateY'):str(y), A('scaleX'):str(size/24), A('scaleY'):str(size/24)})
    for child in source:
        for sub in child.iter():
            if A('fillColor') in sub.attrib:
                sub.set(A('fillColor'), GREEN)
        group.append(child)

def save(root, name):
    ET.indent(root)
    ET.ElementTree(root).write(RES / f'drawable/{name}.xml', encoding='utf-8', xml_declaration=True)

left = vector(96, 168)
# Two columns let the auxiliary targets grow alongside the main D-pad.
for name, x, y in [('home', 24, 28), ('power', 72, 28), ('mute', 48, 84)]:
    face = circle(left, x, y, 22.5, DARK, GREEN, 1.3)
    shade(face, '#FF253235')
    icon(left, name, x-15, y-15, 30)
pill = path(left, 'M25,116 H71 A24,24 0,0 1,71,164 H25 A24,24 0,0 1,25,116 Z', DARK, GREEN, 1.3)
shade(pill, '#FF253235')
path(left, 'M48,117 L48,163', stroke='#FF0C171A', width=1.5)
for name, x in [('volume_down',24),('volume_up',72)]:
    icon(left, name, x-15, 125, 30)
save(left, 'thumb_remote_auxiliary')

pad = vector(144, 144)
# Keep the square viewport while offering a larger intrinsic size to the host.
pad.set(A('width'), '168dp')
pad.set(A('height'), '168dp')
circle(pad,72,72,70.5,DARK,GREEN,1.8)
for rotation in [0,90,180,270]:
    group = ET.SubElement(pad,'group',{A('rotation'):str(rotation),A('pivotX'):'72',A('pivotY'):'72'})
    wedge = path(group, 'M24.1,24.1 A67.75,67.75 0,0 1,119.9,24.1 L94.6,49.4 A32,32 0,0 0,49.4,49.4 Z', DARK, '#FF0D171A', 1.1)
    shade(wedge, '#FF233033')
    icon(group, 'up', 58, 10, 28)
circle(pad,72,72,32.5,'#FF0A171A',GREEN,1.1)
face = circle(pad,72,72,29.5,GREEN)
shade(face,GREEN)
label(pad, 'OK', 72, 79.7, 22, '#FF102C2A')
save(pad,'thumb_remote_dpad')
print('Thumb-Friendly vector artwork regenerated.')
