const fs = require('fs');
const path = 'C:\\dev\\ai\\backend\\src\\main\\resources\\default_codes.json';
const data = JSON.parse(fs.readFileSync(path, 'utf8'));

const fieldTypeGroup = data.find(g => g.groupCode === 'FIELD_TYPE');

const newTypes = [
  { detailCode: "SELECT", name: { ko: "🔽 단일 선택 (Select)", en: "🔽 Single Select" }, sortOrder: 7, isActive: true },
  { detailCode: "MULTI_SELECT", name: { ko: "☑️ 다중 선택 (Multi-Select)", en: "☑️ Multi-Select" }, sortOrder: 8, isActive: true },
  { detailCode: "DECIMAL", name: { ko: "💲 십진수 (Decimal)", en: "💲 Decimal" }, sortOrder: 9, isActive: true },
  { detailCode: "FLOAT", name: { ko: "🔬 부동소수점 (Float)", en: "🔬 Float" }, sortOrder: 10, isActive: true },
  { detailCode: "INTEGER", name: { ko: "1️⃣ 정수 (Integer)", en: "1️⃣ Integer" }, sortOrder: 11, isActive: true },
  { detailCode: "DOMAIN_REFERENCE", name: { ko: "🔗 도메인 참조 (Reference)", en: "🔗 Domain Reference" }, sortOrder: 12, isActive: true },
  { detailCode: "TIME", name: { ko: "⏱️ 시간 (Time)", en: "⏱️ Time" }, sortOrder: 13, isActive: true },
  { detailCode: "HTML_TEXT", name: { ko: "🌐 HTML 텍스트", en: "🌐 HTML Text" }, sortOrder: 14, isActive: true },
  { detailCode: "CHECKBOX", name: { ko: "✅ 체크박스 (Checkbox)", en: "✅ Checkbox" }, sortOrder: 15, isActive: true },
  { detailCode: "CALCULATED", name: { ko: "🧮 계산식 (Calculated)", en: "🧮 Calculated" }, sortOrder: 16, isActive: true },
  { detailCode: "MULTILINGUAL", name: { ko: "🌍 다국어 (Multilingual)", en: "🌍 Multilingual" }, sortOrder: 17, isActive: true },
  { detailCode: "FILE", name: { ko: "📁 파일 첨부 (File)", en: "📁 File Attachment" }, sortOrder: 18, isActive: true }
];

const existingCodes = fieldTypeGroup.details.map(d => d.detailCode);

newTypes.forEach(t => {
  if (!existingCodes.includes(t.detailCode)) {
    fieldTypeGroup.details.push(t);
  }
});

fs.writeFileSync(path, JSON.stringify(data, null, 2), 'utf8');
console.log('Added missing field types to default_codes.json');
