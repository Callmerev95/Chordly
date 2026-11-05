#!/usr/bin/env python3
import sys
sys.path.insert(0, '/Users/rev/Myself/work/Private_Project/Music_Client/BitChord/.tmp_icon')
from png import read_png, write_png

src = sys.argv[1]
out = sys.argv[2]
w, h, rgba = read_png(src)
cx, cy = w/2.0, h/2.0
r = min(w, h) / 2.0

for y in range(h):
    for x in range(w):
        i = (y*w + x) * 4
        dx, dy = x - cx, y - cy
        dist = (dx*dx + dy*dy) ** 0.5
        if dist > r:
            rgba[i+3] = 0

write_png(out, w, h, rgba)
