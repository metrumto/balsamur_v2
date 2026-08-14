import express from 'express';
import pg from 'pg';
import crypto from 'node:crypto';

const { Pool } = pg;
const app = express();
app.use(express.json({ limit: '8kb' }));
const pool = new Pool({ connectionString: process.env.DATABASE_URL, ssl: { rejectUnauthorized: false } });

const PORT = process.env.PORT || 10000;
const CODE_PEPPER = process.env.CODE_PEPPER;
if (!CODE_PEPPER) throw new Error('CODE_PEPPER is required');

function hash(value) {
  return crypto.createHash('sha256').update(`${CODE_PEPPER}:${value.trim().toUpperCase()}`).digest('hex');
}

async function init() {
  await pool.query(`CREATE TABLE IF NOT EXISTS licenses (
    code_hash CHAR(64) PRIMARY KEY,
    device_hash CHAR(64),
    activated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`);
  await pool.query('CREATE INDEX IF NOT EXISTS licenses_device_idx ON licenses(device_hash)');

  // INITIAL_CODES is supplied only through Render Environment Variables.
  // Plain-text codes are never committed to the repository. On startup they are
  // hashed and inserted if missing; only the hashes are persisted in PostgreSQL.
  const initialCodes = (process.env.INITIAL_CODES || '')
    .split(',')
    .map(s => s.trim().toUpperCase())
    .filter(Boolean);
  if (initialCodes.length) {
    if (initialCodes.length !== 50) throw new Error('INITIAL_CODES must contain exactly 50 codes');
    for (const code of initialCodes) {
      if (!/^[A-Z0-9]{16}$/.test(code)) throw new Error('All activation codes must be 16 alphanumeric characters');
      await pool.query('INSERT INTO licenses (code_hash) VALUES ($1) ON CONFLICT (code_hash) DO NOTHING', [hash(code)]);
    }
  }
}

app.get('/health', async (_req, res) => {
  try { await pool.query('SELECT 1'); res.json({ ok: true }); }
  catch { res.status(503).json({ ok: false }); }
});

app.post('/activate', async (req, res) => {
  const { code, deviceId } = req.body || {};
  if (typeof code !== 'string' || typeof deviceId !== 'string' || code.length < 8 || deviceId.length < 8) {
    return res.status(400).json({ ok: false, error: 'invalid_request' });
  }
  const codeHash = hash(code);
  const deviceHash = hash(deviceId);
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const existing = await client.query('SELECT device_hash FROM licenses WHERE code_hash=$1 FOR UPDATE', [codeHash]);
    if (existing.rowCount === 0) {
      await client.query('ROLLBACK');
      return res.status(403).json({ ok: false, error: 'invalid_code' });
    }
    const bound = existing.rows[0].device_hash;
    if (bound && bound !== deviceHash) {
      await client.query('ROLLBACK');
      return res.status(409).json({ ok: false, error: 'code_already_used' });
    }
    if (!bound) {
      await client.query('UPDATE licenses SET device_hash=$1, activated_at=NOW() WHERE code_hash=$2', [deviceHash, codeHash]);
    }
    await client.query('COMMIT');
    return res.json({ ok: true });
  } catch (e) {
    await client.query('ROLLBACK').catch(() => {});
    console.error(e);
    return res.status(500).json({ ok: false, error: 'server_error' });
  } finally { client.release(); }
});

init().then(() => app.listen(PORT, () => console.log(`Activation server listening on ${PORT}`))).catch(err => { console.error(err); process.exit(1); });
