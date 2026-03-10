import { useEffect, useRef } from "react";
import { getSessionState, refreshSession } from "./auth.js";

/**
 * Hook para renovación deslizante del JWT (Propuesta B).
 *
 * - TTL (backend): 40 min
 * - Ventana de renovación: 10 min
 * - Límite duro de sesión: 8h desde el login original
 *
 * Como el JWT vive en una cookie HttpOnly, no podemos leer su exp desde JS.
 * Por eso el backend expone /api/sesiones/estado con remainingSeconds.
 */
export function useRenovacionJwt({ enabled, onSessionExpired } = {}) {
  const timerRef = useRef(null);
  const runningRef = useRef(false);

  useEffect(() => {
    runningRef.current = Boolean(enabled);
    if (!enabled) {
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = null;
      return;
    }

    let cancelled = false;

    async function tick() {
      if (cancelled || !runningRef.current) return;

      try {
        const state = await getSessionState();
        const remaining = Number(state?.remainingSeconds ?? 0);
        const hardRemaining = Number(state?.hardRemainingSeconds ?? 0);
        const refreshWindow = Number(state?.refreshWindowSeconds ?? 600);

        // Si hemos llegado al límite duro, forzamos re-login.
        if (hardRemaining <= 0) {
          onSessionExpired?.();
          return;
        }

        // Si estamos dentro de la ventana, intentamos refrescar.
        if (remaining > 0 && remaining <= refreshWindow) {
          await refreshSession().catch(() => null);
        }

        // Estrategia adaptativa (pocas peticiones lejos de expirar, más cerca al final).
        const remainingAfter = Math.max(remaining, 0);
        let nextMs;

        if (remainingAfter > refreshWindow + 5 * 60) {
          // lejos de la ventana: cada 5 minutos
          nextMs = 5 * 60 * 1000;
        } else if (remainingAfter > refreshWindow) {
          // acercándonos: cada 2 minutos
          nextMs = 2 * 60 * 1000;
        } else {
          // dentro de ventana: cada 60s (por si el refresh falla por suspensión/idle)
          nextMs = 60 * 1000;
        }

        // Nunca programamos más allá de lo que queda de sesión dura.
        nextMs = Math.min(nextMs, Math.max(hardRemaining * 1000, 1000));

        timerRef.current = setTimeout(tick, nextMs);
      } catch (e) {
        // Si el backend responde 401, la sesión ya no es válida.
        if (e?.status === 401) {
          onSessionExpired?.();
          return;
        }
        // Errores de red: reintento suave.
        timerRef.current = setTimeout(tick, 60 * 1000);
      }
    }

    tick();

    return () => {
      cancelled = true;
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = null;
    };
  }, [enabled, onSessionExpired]);
}
