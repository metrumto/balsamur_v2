if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    navigator.serviceWorker.register('sw.js')
      .catch(function (err) {
        console.log('Error al registrar el Service Worker', err);
      });
  });
}