const fs = require('fs');
const lines = fs.readFileSync('C:/Users/Profavor/.gemini/antigravity/brain/2c1a6b79-afef-4454-bd02-09bed4474c9b/.system_generated/logs/transcript_full.jsonl', 'utf8').split('\n');
for (const line of lines) {
  if (line.includes('"step_index":504')) {
    console.log(line);
  }
}
