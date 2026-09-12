#!/usr/bin/env node
// Os arquivos de "requisitos funcionais/" referenciam as imagens por nome
// (<tela>.png e <tela>--RQFn.png); mudar a nomenclatura quebra esses links.
import { mkdir, readdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import { pathToFileURL, fileURLToPath } from "node:url";
import puppeteer from "puppeteer";

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const INPUT = path.join(REPO, "mockups");
const OUTPUT = path.join(REPO, "resources", "mockups");

const scaleIndex = process.argv.indexOf("--scale");
const scale = scaleIndex === -1 ? 2 : Number(process.argv[scaleIndex + 1]);

function highlightCss(code) {
  return `
    .screen :has([data-rqf~="${code}"]) { overflow: visible !important; }
    [data-rqf~="${code}"] {
      position: relative; z-index: 1; overflow: visible !important;
      outline: 2px solid #2563eb; outline-offset: 4px; border-radius: 8px;
      box-shadow: 0 0 0 8px rgba(37, 99, 235, 0.12);
    }
    [data-rqf~="${code}"]::after {
      content: "${code}"; position: absolute; top: -14px; right: -10px; z-index: 2;
      background: #2563eb; color: #fff; font-size: 10px; font-weight: 600;
      letter-spacing: 0.02em; padding: 1px 7px; border-radius: 999px;
      font-family: "Inter Variable", Inter, system-ui, sans-serif; line-height: 1.5;
    }`;
}

function codesIn(html) {
  const codes = new Set();
  for (const m of html.matchAll(/data-rqf="([^"]+)"/g)) for (const c of m[1].split(/\s+/)) codes.add(c);
  return [...codes].sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
}

async function main() {
  const files = (await readdir(INPUT)).filter((f) => f.endsWith(".html")).sort();
  if (files.length === 0) throw new Error(`Nenhum .html em ${INPUT}`);
  await mkdir(OUTPUT, { recursive: true });

  const browser = await puppeteer.launch();
  let count = 0;
  try {
    const page = await browser.newPage();
    await page.setViewport({ width: 1100, height: 800, deviceScaleFactor: scale });
    for (const file of files) {
      const base = file.replace(/\.html$/, "");
      const url = pathToFileURL(path.join(INPUT, file)).href;
      const variants = [null, ...codesIn(await readFile(path.join(INPUT, file), "utf8"))];
      for (const code of variants) {
        await page.goto(url, { waitUntil: "load" });
        await page.evaluate(() => document.fonts.ready);
        if (code) await page.addStyleTag({ content: highlightCss(code) });
        const screen = await page.$(".screen");
        if (!screen) throw new Error(`${file} não tem elemento .screen`);
        const target = path.join(OUTPUT, code ? `${base}--${code}.png` : `${base}.png`);
        await writeFile(target, await screen.screenshot({ omitBackground: true, type: "png" }));
        console.log(`ok ${path.relative(REPO, target)}`);
        count++;
      }
    }
  } finally {
    await browser.close();
  }
  console.log(`${files.length} telas, ${count} imagens em ${path.relative(REPO, OUTPUT)}`);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
