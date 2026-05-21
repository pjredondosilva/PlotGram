import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getMe, logout as apiLogout } from "./ServicioAutenticacion.js";
import { useRenovacionJwt } from "./UseRenovacionJwt.js";

const ContextoDeAutenticacion = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUsuario] = useState(null);
    const [loadingMe, setcarga] = useState(true);
    const [sessionExpired, setSesionExpirada] = useState(false);

    const refreshMe = useCallback(async () => {
        try {
            const me = await getMe();
            setUsuario(me);
            return me;
        } catch (e) {
            if (e?.status === 401) setUsuario(null);
            throw e;
        } finally {
            setcarga(false);
        }
    }, []);

    useEffect(() => {
        refreshMe().catch(() => {});
    }, [refreshMe]);

    const handleSessionExpired = useCallback(async () => {
        try {
            await apiLogout();
        } catch {
            // Si falla la llamada, igualmente dejamos la app en estado anónimo
        } finally {
            setUsuario(null);
            setSesionExpirada(true);
        }
    }, []);

    useRenovacionJwt({
        enabled: !!user,
        onSessionExpired: handleSessionExpired,
    });

    const logout = useCallback(async () => {
        try {
            await apiLogout();
        } finally {
            setUsuario(null);
            setSesionExpirada(false);
        }
    }, []);

    const clearSessionExpired = useCallback(() => {
        setSesionExpirada(false);
    }, []);

    const value = useMemo(
        () => ({
            user,
            setUser: setUsuario,
            refreshMe,
            logout,
            loadingMe,
            sessionExpired,
            clearSessionExpired,
        }),
        [user, refreshMe, logout, loadingMe, sessionExpired, clearSessionExpired]
    );

    return <ContextoDeAutenticacion.Provider value={value}>{children}</ContextoDeAutenticacion.Provider>;
}

export function useAuth() {
    const ctx = useContext(ContextoDeAutenticacion);
    if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
    return ctx;
}