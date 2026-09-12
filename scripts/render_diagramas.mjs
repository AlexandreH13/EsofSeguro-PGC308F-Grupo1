#!/usr/bin/env node
// Os arquivos de "requisitos funcionais/" referenciam os PNGs por código do
// requisito (RQFn.png), extraído do heading `### RQFn` anterior a cada bloco.
// O sketch usa svg2roughjs com as opções padrão, Comic Sans MS inclusive, para
// replicar o modo "Rough" do Mermaid Live Editor.
import { mkdir, readdir, readFile, stat, writeFile } from "node:fs/promises";
import { createRequire } from "node:module";
import path from "node:path";
import { fileURLToPath } from "node:url";
import puppeteer from "puppeteer";

const require = createRequire(import.meta.url);
const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const HEADING = /^###\s+(RQF\d+)\b/gm;
const BLOCK = /```mermaid\n([\s\S]*?)```/g;

function parseArgs(argv) {
  const args = { input: "requisitos funcionais", output: "resources/diagramas", scale: 2, rough: true };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--input") args.input = argv[++i];
    else if (a === "--output") args.output = argv[++i];
    else if (a === "--scale") args.scale = Number(argv[++i]);
    else if (a === "--classic") args.rough = false;
    else throw new Error(`Argumento desconhecido: ${a}`);
  }
  return args;
}

function extract(markdown) {
  const diagrams = [];
  for (const match of markdown.matchAll(BLOCK)) {
    const before = markdown.slice(0, match.index);
    const headings = [...before.matchAll(HEADING)].map((m) => m[1]);
    const name = headings.at(-1) ?? `diagrama${diagrams.length + 1}`;
    diagrams.push({ name, code: match[1] });
  }
  return diagrams;
}

async function loadSources(input) {
  if (!(await stat(input)).isDirectory()) return [await readFile(input, "utf8")];
  const files = (await readdir(input)).filter((f) => f.endsWith(".md"));
  files.sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
  return Promise.all(files.map((f) => readFile(path.join(input, f), "utf8")));
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const input = path.resolve(REPO, args.input);
  const output = path.resolve(REPO, args.output);
  const diagrams = (await loadSources(input)).flatMap(extract);
  if (diagrams.length === 0) throw new Error(`Nenhum bloco mermaid em ${input}`);
  await mkdir(output, { recursive: true });

  const mermaidJs = await readFile(require.resolve("mermaid/dist/mermaid.min.js"), "utf8");
  const roughJs = await readFile(require.resolve("svg2roughjs/dist/svg2roughjs.umd.min.js"), "utf8");

  const browser = await puppeteer.launch();
  const failed = [];
  try {
    const page = await browser.newPage();
    await page.setContent('<html><body style="margin:0;background:transparent"><div id="src"></div><div id="out"></div></body></html>');
    await page.addScriptTag({ content: mermaidJs });
    await page.addScriptTag({ content: roughJs });
    await page.evaluate(() => window.mermaid.initialize({ startOnLoad: false, theme: "default" }));

    for (const { name, code } of diagrams) {
      const target = path.join(output, `${name}.png`);
      try {
        const size = await page.evaluate(async (code, rough) => {
          const src = document.getElementById("src");
          const out = document.getElementById("out");
          src.innerHTML = "";
          out.innerHTML = "";
          const { svg } = await window.mermaid.render(`m${Date.now()}`, code);
          src.innerHTML = svg;
          const el = src.querySelector("svg");
          const [, , w, h] = el.getAttribute("viewBox").split(/\s+/).map(Number);
          el.setAttribute("width", w);
          el.setAttribute("height", h);
          el.style.maxWidth = "none";
          let final = el;
          if (rough) {
            const converter = new window.svg2roughjs.Svg2Roughjs("#out");
            converter.svg = el;
            converter.seed = 1;
            await converter.sketch();
            src.innerHTML = "";
            final = out.querySelector("svg");
          }
          final.id = "final";
          return { w: Math.ceil(w), h: Math.ceil(h) };
        }, code, args.rough);
        await page.setViewport({ width: size.w + 20, height: size.h + 20, deviceScaleFactor: args.scale });
        const handle = await page.$("#final");
        const png = await handle.screenshot({ omitBackground: true, type: "png" });
        await writeFile(target, png);
        console.log(`ok ${path.relative(REPO, target)}`);
      } catch (err) {
        failed.push(name);
        console.error(`FALHOU ${name}: ${err.message}`);
      }
    }
  } finally {
    await browser.close();
  }
  if (failed.length) {
    console.error(`${failed.length} diagrama(s) com erro: ${failed.join(", ")}`);
    process.exit(1);
  }
  console.log(`${diagrams.length} diagramas gerados em ${path.relative(REPO, output)}`);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
