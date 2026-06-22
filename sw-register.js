if ("serviceWorker" in navigator) {
  window.addEventListener("load", async () => {
    try {
      const reg = await navigator.serviceWorker.register("./sw.js");

      // Fuerza actualización automática
      reg.addEventListener("updatefound", () => {
        const newWorker = reg.installing;

        newWorker.addEventListener("statechange", () => {
          if (newWorker.state === "installed" && navigator.serviceWorker.controller) {
            // nueva versión disponible → recarga automática
            window.location.reload();
          }
        });
      });

    } catch (err) {
      console.log("SW error:", err);
    }
  });
}