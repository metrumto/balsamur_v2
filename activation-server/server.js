import express from 'express';
import Database from 'better-sqlite3';
import crypto from 'node:crypto';
import fs from 'node:fs';

const app = express();
app.use(express.json({ limit: '16kb' }));

const db = new Database(process.env.DB_PATH || './activation.db');
db.pragma('journal_mode = WAL');
db.exec(`CREATE TABLE IF NOT EXISTS codes (code_hash TEXT PRIMARY KEY, device_hash TEXT, activated_at TEXT);`);

const hash = value => crypto.createHash('sha256').update(value, 'utf8').digest('hex');
const normalize = value => String(value || '').trim().toUpperCase();

const codesFile = process.env.CODES_FILE || './codes.txt';
if (fs.existsSync(codesFile)) {
  const insert = db.prepare('INSERT OR IGNORE INTO codes(code_hash) VALUES (?)');
  const tx = db.transaction(lines => {
    for (const line of lines) {
      const code = normalize(line);
      if (/^[A-Z0-9]{4}(?:-[A-Z0-9]{4}){2}$/.test(code)) insert.run(hash(code));
    }
  });
  tx(fs.readFileSync(codesFile, 'utf8').split(/\r?\n/));
}

app.get('/health', (_req, res) => res.json({ ok: true }));

app.post('/activate', (req, res) => {
  const code = normalize(req.body?.code);
  const deviceId = String(req.body?.deviceId || '').trim();
  if (!/^[A-Z0-9]{4}(?:-[A-Z0-9]{4}){2}$/.test(code) || deviceId.length < 8 || deviceId.length > 256) {
    return res.status(400).json({ ok: false, error: 'invalid_request' });
  }

  const codeHash = hash(code);
  const deviceHash = hash(deviceId);
  const row = db.prepare('SELECT device_hash FROM codes WHERE code_hash = ?').get(codeHash);
  if (!row) return res.status(401).json({ ok: false, error: 'invalid_code' });
  if (row.device_hash && row.device_hash !== deviceHash) {
    return res.status(409).json({ ok: false, error: 'code_already_bound' });
  }
  if (!row.device_hash) {
    db.prepare('UPDATE codes SET device_hash = ?, activated_at = ? WHERE code_hash = ? AND device_hash IS NULL')
      .run(deviceHash, new Date().toISOString(), codeHash);
  }
  return res.json({ ok: true, deviceHash });
});

const port = Number(process.env.PORT || 3000);
app.listen(port, () => console.log(`BalsaMur activation server listening on ${port}`));
