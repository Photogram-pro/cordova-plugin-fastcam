const path = require("path");
const fs = require("fs");
const { promisify } = require("util");

const readdir = promisify(fs.readdir);
const stat = promisify(fs.stat);

async function getFiles(dir) {
  const subdirs = await readdir(dir);
  const files = await Promise.all(
    subdirs.map(async (subdir) => {
      const res = path.resolve(dir, subdir);
      return (await stat(res)).isDirectory() ? getFiles(res) : res;
    })
  );
  return files.reduce((a, f) => a.concat(f), []);
}

/**
 * Reads the plugin.xml
 * file and creates a source-file entry
 * for each Java file, as the standard
 * behaviour of that directive doesn't
 * suffice.
 */
async function main() {
  const pluginXmlPath = path.join(__dirname, "plugin.xml");
  const pluginXmlContent = fs
    .readFileSync(pluginXmlPath)
    .toString()
    .split("\n");

  const javaFolderPrefix = "src/android/java";
  const javaFolderPath = path.join(__dirname, javaFolderPrefix);
  const allFiles = await getFiles(javaFolderPath);
  const xmlEntries = allFiles
    .map((entry) => {
      return `
      <source-file src="${entry
        .replace(__dirname, "")
        .slice(1)
        .replace(
          new RegExp("\\\\", "g"),
          "/"
        )}" target-dir="src/com/cordovapluginfastcam" />
    `.trim();
    })
    .join("\n");

  const startLineNumber = pluginXmlContent.findIndex((line) =>
    line.includes("<java-files-start />")
  );
  const endLineNumber = pluginXmlContent.findIndex((line) =>
    line.includes("<java-files-end />")
  );
  let newLines = pluginXmlContent.filter(
    (_, i) => i <= startLineNumber || i >= endLineNumber
  );

  newLines.splice(startLineNumber + 1, 0, xmlEntries);
  const newPluginXmlContent = newLines.join("\n");
  fs.writeFileSync(pluginXmlPath, newPluginXmlContent);
}

main();
