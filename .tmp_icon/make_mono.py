#!/usr/bin/env python3
import sys
sys.path.insert(0, '/Users/rev/Myself/work/Private_Project/Music_Client/BitChord/.tmp_icon')
from png import read_png, write_png

SRC = '/Users/rev/Myself/work/Private_Project/Music_Client/BitChord/Chordly_icon/Chordly_Launcher_Icon_512.png'
w, h, rgba = read_png(SRC)

out = bytearray(w*h*4)
lo = 60.0
hi = 150.0
for i in range(w*h):
    r, g, b, a = rgba[i*4:i*4+4]
    lum = 0.299*r + 0.587*g + 0.114*b
    if lum <= lo:
        alpha = a
    elif lum >= hi:
        alpha = 0
    else:
        alpha = int(a * (hi - lum) / (hi - lo))
    out[i*4:i*4+4] = bytes([255, 255, 255, alpha])

write_png('/tmp/ic_monochrome_master_512.png', w, h, out)
print("master written")
