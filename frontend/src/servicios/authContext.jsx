import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getMe, logout as apiLogout } from "./auth.js";
import { useRenovacionJwt } from "./useRenovacionJwt.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loadingMe, setLoadingMe] = useState(true);

    const refreshMe = useCallback(async () => {
        try {
            const me = await getMe();
            setUser(me);
            return me;
        } catch (e) {
            // Si no hay sesión, dejamos user a null sin “romper” la app
            if (e?.status === 401) setUser(null);
            throw e;
        } finally {
            setLoadingMe(false);
        }
    }, []);

    // Carga inicial (si hay cookie válida -> user)
    useEffect(() => {
        refreshMe().catch(() => {});
    }, [refreshMe]);

    // Renovación deslizante mientras hay usuario autenticado
    useRenovacionJwt({
        enabled: !!user,
        onSessionExpired: () => setUser(null),
    });

    const logout = useCallback(async () => {
        try {
            await apiLogout();
        } finally {
            setUser(null);
        }
    }, []);

    const value = useMemo(
        () => ({ user, setUser, refreshMe, logout, loadingMe }),
        [user, refreshMe, logout, loadingMe]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
    return ctx;
}