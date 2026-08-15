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

function normalizeCode(value) { return value.trim().toUpperCase(); }
function hash(value) { return crypto.createHash('sha256').update(`${CODE_PEPPER}:${value}`).digest('hex'); }
function codeSetFingerprint(codes) { return crypto.createHash('sha256').update(codes.slice().sort().join('\n')).digest('hex'); }

async function init() {
  await pool.query(`CREATE TABLE IF NOT EXISTS licenses (
    code_hash CHAR(64) PRIMARY KEY,
    device_hash CHAR(64),
    activated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`);
  await pool.query('CREATE INDEX IF NOT EXISTS licenses_device_idx ON licenses(device_hash)');
  await pool.query(`CREATE TABLE IF NOT EXISTS license_config (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    codes_fingerprint CHAR(64) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`);

  const initialCodes = (process.env.INITIAL_CODES || '').split(',').map(normalizeCode).filter(Boolean);
  if (initialCodes.length !== 50) throw new Error('INITIAL_CODES must contain exactly 50 codes');
  if (new Set(initialCodes).size !== 50) throw new Error('INITIAL_CODES must contain 50 unique codes');
  if (initialCodes.some(code => !/^[A-Z0-9]{12}$/.test(code))) throw new Error('All activation codes must be exactly 12 alphanumeric characters');

  const fingerprint = codeSetFingerprint(initialCodes);
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const current = await client.query('SELECT codes_fingerprint FROM license_config WHERE id=1 FOR UPDATE');
    if (current.rowCount === 0 || current.rows[0].codes_fingerprint !== fingerprint) {
      await client.query('DELETE FROM licenses');
      for (const code of initialCodes) await client.query('INSERT INTO licenses (code_hash) VALUES ($1)', [hash(code)]);
      await client.query(`INSERT INTO license_config (id, codes_fingerprint, updated_at) VALUES (1, $1, NOW()) ON CONFLICT (id) DO UPDATE SET codes_fingerprint=EXCLUDED.codes_fingerprint, updated_at=NOW()`, [fingerprint]);
      console.log('Activation license set initialized/rotated: 50 codes');
    } else {
      const count = await client.query('SELECT COUNT(*)::int AS count FROM licenses');
      if (count.rows[0].count !== 50) throw new Error('License table is inconsistent: expected 50 codes');
    }
    await client.query('COMMIT');
  } catch (e) { await client.query('ROLLBACK'); throw e; }
  finally { client.release(); }
}

app.get('/health', async (_req, res) => {
  try { const count = await pool.query('SELECT COUNT(*)::int AS count FROM licenses'); res.json({ ok: true, licenses: count.rows[0].count }); }
  catch { res.status(503).json({ ok: false }); }
});

app.post('/activate', async (req, res) => {
  const { code, deviceId } = req.body || {};
  if (typeof code !== 'string' || typeof deviceId !== 'string' || !/^[A-Za-z0-9]{12}$/.test(code.trim()) || deviceId.length < 8) return res.status(400).json({ ok: false, error: 'invalid_request' });
  const codeHash = hash(normalizeCode(code));
  const deviceHash = hash(deviceId);
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const existing = await client.query('SELECT device_hash FROM licenses WHERE code_hash=$1 FOR UPDATE', [codeHash]);
    if (existing.rowCount === 0) { await client.query('ROLLBACK'); return res.status(403).json({ ok: false, error: 'invalid_code' }); }
    const bound = existing.rows[0].device_hash;
    if (bound && bound !== deviceHash) { await client.query('ROLLBACK'); return res.status(409).json({ ok: false, error: 'code_already_used' }); }
    if (!bound) await client.query('UPDATE licenses SET device_hash=$1, activated_at=NOW() WHERE code_hash=$2', [deviceHash, codeHash]);
    await client.query('COMMIT');
    return res.json({ ok: true });
  } catch (e) { await client.query('ROLLBACK').catch(() => {}); console.error(e); return res.status(500).json({ ok: false, error: 'server_error' }); }
  finally { client.release(); }
});

init().then(() => app.listen(PORT, () => console.log(`Activation server listening on ${PORT}`))).catch(err => { console.error(err); process.exit(1); });
