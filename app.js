let advancedVisible = false;

const STORAGE_KEY = 'balsamur_params_v1';

/* -------------------------
   UI: Mostrar / ocultar panel avanzado
-------------------------- */
function toggleAvanzados() {
  advancedVisible = !advancedVisible;

  const panel = document.getElementById("advancedPanel");
  const btn = document.getElementById("btnAvanzados");

  panel.style.display = advancedVisible ? "block" : "none";

  btn.setAttribute("aria-expanded", advancedVisible);
}

/* -------------------------
   Utilidad: parseo seguro de números
-------------------------- */
function parseNum(id) {
  const el = document.getElementById(id);
  if (!el) return NaN;

  const v = el.value.trim();
  return parseFloat(v.replace(',', '.'));
}

/* -------------------------
   Guardar parámetros
-------------------------- */
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
    console.warn('No se pudieron guardar los parámetros', e);
  }
}

/* -------------------------
   Cargar parámetros
-------------------------- */
function loadParams() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;

    const params = JSON.parse(raw);
    if (!params) return;

    Object.keys(params).forEach(key => {
      const el = document.getElementById(key);
      if (el) el.value = params[key];
    });

  } catch (e) {
    console.warn('No se pudieron cargar los parámetros', e);
  }
}

/* -------------------------
   Cálculo principal
-------------------------- */
function calcular() {
  const errorDiv = document.getElementById('error');
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

  const valores = [
    anchoTeja,
    anchoCoronacion,
    taludInterior,
    taludExterior,
    alturaCoronacion,
    alturaFondo
  ];

  if (valores.some(v => isNaN(v))) {
    errorDiv.textContent = 'Revisa los parámetros, hay valores no válidos.';
    return;
  }

  const alturaMax = alturaCoronacion + alturaFondo;

  if (cota < 0 || cota > alturaMax) {
    errorDiv.textContent =
      `La altura debe estar entre 0 y ${alturaMax.toFixed(2)} m`;
    return;
  }

  /* -------------------------
     Cálculos
  -------------------------- */
  const terraplenInt =
    (anchoCoronacion / 2) + (cota * taludInterior) - (anchoTeja / 2);

  const desmonteInt =
    (anchoCoronacion / 2) + (cota * taludInterior) + (anchoTeja / 2);

  const exterior =
    (anchoCoronacion / 2) + (cota * taludExterior) - (anchoTeja / 2);

  /* -------------------------
     Pintar resultados
  -------------------------- */
  document.getElementById('terraplen-int').textContent =
    terraplenInt.toFixed(2) + ' m';

  document.getElementById('desmonte-int').textContent =
    desmonteInt.toFixed(2) + ' m';

  document.getElementById('exterior').textContent =
    exterior.toFixed(2) + ' m';

  /* -------------------------
     Guardar parámetros
  -------------------------- */
  saveParams();
}

/* -------------------------
   INIT
-------------------------- */
window.addEventListener("DOMContentLoaded", () => {

  loadParams();

  document.getElementById("btnCalcular")
    .addEventListener("click", calcular);

  document.getElementById("btnAvanzados")
    .addEventListener("click", toggleAvanzados);

});