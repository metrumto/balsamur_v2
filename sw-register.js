if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    navigator.serviceWorker.register('./sw.js')
      .then(registration => {
        console.log('Service Worker registrado correctamente:', registration.scope);
      })
      .catch(function (err) {
        console.log('Error al registrar el Service Worker', err);
      });
  });
}