"""Скачивает клиент Minecraft 1.21.1, берёт ванильные текстуры и перекрашивает их в белый (форма пикселей сохраняется).
Запускается в GitHub Actions (workflow recolor.yml)."""
import io, json, os, random, urllib.request, zipfile
from PIL import Image

VERSION = "1.21.1"
OUT = "src/main/resources/assets/saltupdate/textures"

def get(url):
    with urllib.request.urlopen(url) as r:
        return r.read()

manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
vurl = next(v["url"] for v in manifest["versions"] if v["id"] == VERSION)
client = json.loads(get(vurl))["downloads"]["client"]["url"]
jar = zipfile.ZipFile(io.BytesIO(get(client)))

def load(path):
    return Image.open(io.BytesIO(jar.read(f"assets/minecraft/textures/{path}.png"))).convert("RGBA")

def lum(p):
    return 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]

def whiten(im, white=(232, 228, 218)):
    """Весь блок: яркость пикселя относительно среднего умножается на белый цвет."""
    px = im.load()
    vals = [lum(px[x, y]) for x in range(im.width) for y in range(im.height) if px[x, y][3]]
    mean = sum(vals) / len(vals)
    for x in range(im.width):
        for y in range(im.height):
            p = px[x, y]
            if not p[3]:
                continue
            f = lum(p) / mean
            px[x, y] = tuple(max(0, min(255, int(c * f))) for c in white) + (p[3],)
    return im

def recolor_ore(im):
    """Руда: серая порода остаётся ванильной, цветные пиксели руды становятся белыми кристаллами."""
    px = im.load()
    for x in range(im.width):
        for y in range(im.height):
            r, g, b, a = px[x, y]
            if a and (max(r, g, b) - min(r, g, b)) > 28:
                t = max(0.0, min(1.0, (lum((r, g, b)) - 60) / 170))
                lo, hi = (150, 168, 196), (252, 253, 255)
                px[x, y] = tuple(int(lo[i] + (hi[i] - lo[i]) * t) for i in range(3)) + (a,)
    return im

def save(im, name):
    path = f"{OUT}/{name}.png"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

save(whiten(load("block/stone")), "block/salt_block")
save(whiten(load("block/polished_andesite")), "block/polished_salt")
save(whiten(load("block/stone_bricks")), "block/salt_bricks")
save(whiten(load("block/chiseled_stone_bricks")), "block/chiseled_salt")
save(recolor_ore(load("block/diamond_ore")), "block/salt_ore")
save(recolor_ore(load("block/deepslate_diamond_ore")), "block/deepslate_salt_ore")

# Солёная картошка: ванильный печёный картофель + крупинки соли
pot = load("item/baked_potato")
px = pot.load()
def inner(x, y):
    if not px[x, y][3]:
        return False
    return all(0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3]
               for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
cells = [(x, y) for x in range(16) for y in range(16) if inner(x, y)]
random.Random(4).shuffle(cells)
for (x, y) in cells[:8]:
    px[x, y] = (255, 255, 255, 255)
for (x, y) in cells[8:12]:
    px[x, y] = (236, 238, 242, 255)
save(pot, "item/salted_potato")
print("recolor done")
