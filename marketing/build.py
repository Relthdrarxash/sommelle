"""Génère les supports visuels de Sommelle à partir des modèles HTML de ce dossier.

Sorties :
  site/fiche-prescripteurs.pdf          fiche A4 pour médecins et pharmaciens, avec QR code
  site/img/banniere.png                 aperçu des liens partagés (1024 × 500)
  fastlane/.../images/featureGraphic.png  bannière de la fiche Play Store (1024 × 500)
  fastlane/.../images/icon.png          icône de la fiche Play Store (512 × 512)

Prérequis : pip install playwright "qrcode[pil]" && playwright install chromium
Usage, depuis la racine du dépôt : python3 marketing/build.py
"""

import io
import shutil
import sys
from pathlib import Path

import qrcode
import qrcode.image.svg
from playwright.sync_api import sync_playwright

SITE_URL = "https://relthdrarxash.github.io/sommelle/"
ROOT = Path(__file__).resolve().parent.parent
MARKETING = ROOT / "marketing"
STORE_IMAGES = ROOT / "fastlane" / "metadata" / "android" / "fr-FR" / "images"


def qr_svg(url: str) -> str:
    image = qrcode.make(url, image_factory=qrcode.image.svg.SvgPathImage, box_size=10, border=1)
    buffer = io.BytesIO()
    image.save(buffer)
    svg = buffer.getvalue().decode("utf-8")
    svg = svg[svg.index("<svg"):]
    return svg.replace("<svg", '<svg role="img" aria-label="QR code vers le site de Sommelle"', 1)


def main() -> None:
    template = (MARKETING / "fiche-prescripteurs.html").read_text(encoding="utf-8")
    filled = MARKETING / "_fiche.html"
    filled.write_text(template.replace("{{QR}}", qr_svg(SITE_URL)).replace("{{URL}}", SITE_URL), encoding="utf-8")
    STORE_IMAGES.mkdir(parents=True, exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        page.goto(filled.as_uri())
        page.wait_for_load_state("networkidle")
        page.pdf(path=str(ROOT / "site" / "fiche-prescripteurs.pdf"), print_background=True, prefer_css_page_size=True)

        page.set_viewport_size({"width": 1024, "height": 500})
        page.goto((MARKETING / "banniere.html").as_uri())
        page.wait_for_load_state("networkidle")
        banner = ROOT / "site" / "img" / "banniere.png"
        page.screenshot(path=str(banner))
        shutil.copy(banner, STORE_IMAGES / "featureGraphic.png")

        page.set_viewport_size({"width": 512, "height": 512})
        page.goto((MARKETING / "icone-store.svg").as_uri())
        page.screenshot(path=str(STORE_IMAGES / "icon.png"))

        browser.close()
    filled.unlink()
    print("Supports générés.", file=sys.stderr)


if __name__ == "__main__":
    main()
