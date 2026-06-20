let advancedVisible = false;

const STORAGE_KEY = 'balsamur_params_v1';

function toggleAvanzados() {
  advancedVisible = !advancedVisible;
  const panel = document.getElementById('advancedPanel');
  panel.style.display = advancedVisible ? 'block' : 'none';
}

function parseNum(id) {
  const v = document.getElementById(id).value.trim();
  return parseFloat(v.replace(',', '.'));
}

function saveParams() {
  const params = {
    anchoTeja: document.getElementById('anchoTeja').value,
    anchoCoronacion: document.getElementById('anchoCoronacion').value,
    taludInterior: document.getElementById('taludInterior').value,
    taludExterior: document.getElementById('taludExterior').value,
    alturaCoronacion: document.getElementById('alturaCoronacion').value,
    alturaFondo: document.getElementById('alturaFondo').value,
  };
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(params));
  } catch (e) {
    console.warn('No se pudieron guardar los parámetros en localStorage', e);
  }
}

function loadParams() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const params = JSON.parse(raw);
    if (!params) return;

    if (params.anchoTeja !== undefined) document.getElementById('anchoTeja').value = params.anchoTeja;
    if (params.anchoCoronacion !== undefined) document.getElementById('anchoCoronacion').value = params.anchoCoronacion;
    if (params.taludInterior !== undefined) document.getElementById('taludInterior').value = params.taludInterior;
    if (params.taludExterior !== undefined) document.getElementById('taludExterior').value = params.taludExterior;
    if (params.alturaCoronacion !== undefined) document.getElementById('alturaCoronacion').value = params.alturaCoronacion;
    if (params.alturaFondo !== undefined) document.getElementById('alturaFondo').value = params.alturaFondo;
  } catch (e) {
    console.warn('No se pudieron leer los parámetros de localStorage', e);
  }
}

function calcular() {
  const errorDiv = document.getElementById('error');
  const interiorSpan = document.getElementById('interior');
  const exteriorSpan = document.getElementById('exterior');

  errorDiv.textContent = '';

  const cotaInput = document.getElementById('cota').value.trim();
  if (!cotaInput) {
    errorDiv.textContent = 'Introduce un valor de cota.';
    return;
  }

  const cota = parseFloat(cotaInput.replace(',', '.'));
  if (isNaN(cota)) {
    errorDiv.textContent = 'La cota no es un número válido.';
    return;
  }

  const anchoTeja = parseNum('anchoTeja');
  const anchoCoronacion = parseNum('anchoCoronacion');
  const taludInterior = parseNum('taludInterior');
  const taludExterior = parseNum('taludExterior');
  const alturaCoronacion = parseNum('alturaCoronacion');
  const alturaFondo = parseNum('alturaFondo');

  if ([anchoTeja, anchoCoronacion, taludInterior,
       taludExterior, alturaCoronacion, alturaFondo]
      .some(v => isNaN(v))) {
    errorDiv.textContent = 'Revisa los parámetros, hay valores no válidos.';
    return;
  }

  const alturaMax = alturaCoronacion + alturaFondo;

  if (cota < 0 || cota > alturaMax) {
    errorDiv.textContent =
      `La altura hasta coronación debe estar entre 0 y ${alturaMax.toFixed(2)} m`;
    return;
  }

const di = (anchoCoronacion / 2) + (cota * taludInterior) - (anchoTeja / 2);
const de = (anchoCoronacion / 2) + (cota * taludExterior) - (anchoTeja / 2);

// Exterior (usando taludExterior)
const terraplenExt = (anchoCoronacion / 2) + (cota * taludExterior) - (anchoTeja / 2);
const desmonteExt  = (anchoCoronacion / 2) + (cota * taludExterior) + (anchoTeja / 2);

// Actualizar spans principales
interiorSpan.textContent = di.toFixed(2) + ' m';
exteriorSpan.textContent = de.toFixed(2) + ' m';

// Cuadrados exterior
document.getElementById('desmonte-ext').textContent  = desmonteExt.toFixed(2) + ' m';
document.getElementById('terraplen-ext').textContent = terraplenExt.toFixed(2) + ' m';

// Guardar parámetros al calcular
saveParams();
}

// Cargar parámetros al abrir la página
window.addEventListener('DOMContentLoaded', loadParams);