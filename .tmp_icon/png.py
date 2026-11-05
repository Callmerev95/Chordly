import zlib, struct

def read_png(path):
    with open(path, 'rb') as f:
        data = f.read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'not png'
    pos = 8
    idat = b''
    w = h = bit = ctype = None
    plte = None
    trns = None
    while pos < len(data):
        ln = struct.unpack('>I', data[pos:pos+4])[0]
        typ = data[pos+4:pos+8]
        chunk = data[pos+8:pos+8+ln]
        pos += 12 + ln
        if typ == b'IHDR':
            w, h, bit, ctype, comp, filt, inter = struct.unpack('>IIBBBBB', chunk)
            assert bit == 8 and comp == 0 and filt == 0 and inter == 0, (bit, ctype, comp, filt, inter)
        elif typ == b'IDAT':
            idat += chunk
        elif typ == b'PLTE':
            plte = chunk
        elif typ == b'tRNS':
            trns = chunk
        elif typ == b'IEND':
            break
    raw = zlib.decompress(idat)
    ch = {0:1, 2:3, 3:1, 4:2, 6:4}[ctype]
    stride = w * ch
    out = bytearray(h * stride)
    prev = bytearray(stride)
    p = 0
    for y in range(h):
        ft = raw[p]; p += 1
        line = bytearray(raw[p:p+stride]); p += stride
        if ft == 1:
            for i in range(ch, stride):
                line[i] = (line[i] + line[i-ch]) & 255
        elif ft == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif ft == 3:
            for i in range(stride):
                a = line[i-ch] if i >= ch else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 255
        elif ft == 4:
            for i in range(stride):
                a = line[i-ch] if i >= ch else 0
                b = prev[i]
                c = prev[i-ch] if i >= ch else 0
                pp = a + b - c
                pa = abs(pp-a); pb = abs(pp-b); pc = abs(pp-c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 255
        out[y*stride:(y+1)*stride] = line
        prev = line
    # to RGBA
    rgba = bytearray(w*h*4)
    if ctype == 6:
        return w, h, out
    for i in range(w*h):
        if ctype == 2:
            r, g, b = out[i*3:i*3+3]
            a = 255
        elif ctype == 0:
            r = g = b = out[i]; a = 255
        elif ctype == 4:
            r = g = b = out[i*2]; a = out[i*2+1]
        elif ctype == 3:
            idx = out[i]
            r, g, b = plte[idx*3:idx*3+3]
            a = trns[idx] if trns and idx < len(trns) else 255
        rgba[i*4:i*4+4] = bytes([r, g, b, a])
    return w, h, rgba

def write_png(path, w, h, rgba):
    def chunk(typ, data):
        c = struct.pack('>I', len(data)) + typ + data
        crc = zlib.crc32(typ + data) & 0xffffffff
        return c + struct.pack('>I', crc)
    raw = bytearray()
    stride = w*4
    for y in range(h):
        raw.append(0)
        raw += rgba[y*stride:(y+1)*stride]
    out = b'\x89PNG\r\n\x1a\n'
    out += chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
    out += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    out += chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(out)
