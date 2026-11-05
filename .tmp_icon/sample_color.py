#!/usr/bin/env python3
import sys
sys.path.insert(0, '/Users/rev/Myself/work/Private_Project/Music_Client/BitChord/.tmp_icon')
from png import read_png

w, h, rgba = read_png('/Users/rev/Myself/work/Private_Project/Music_Client/BitChord/Chordly_icon/Chordly_Launcher_Icon_512.png')
print(f"{w}x{h}")

r_sum = g_sum = b_sum = a_sum = 0
n = 0
for i in range(0, len(rgba), 4):
    r, g, b, a = rgba[i:i+4]
    if a > 10:
        r_sum += r
        g_sum += g
        b_sum += b
        a_sum += a
        n += 1

if n > 0:
    r_avg = r_sum // n
    g_avg = g_sum // n
    b_avg = b_sum // n
    a_avg = a_sum // n
    print(f"avg(opaque): #{r_avg:02X}{g_avg:02X}{b_avg:02X} alpha={a_avg}")
else:
    print("transparent")

bright = []
for i in range(0, len(rgba), 4):
    r, g, b, a = rgba[i:i+4]
    if a > 200 and (r > 200 or g > 100 or b > 100):
        bright.append((r, g, b))

if bright:
    bright.sort(key=lambda x: x[0]*0.3 + x[1]*0.6 + x[2]*0.1, reverse=True)
    r, g, b = bright[len(bright)//2]
    print(f"bright_med: #{r:02X}{g:02X}{b:02X}")
    r, g, b = bright[0]
    print(f"bright_max: #{r:02X}{g:02X}{b:02X}")
