import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getMe, logout as apiLogout } from "./auth.js";
import { useRenovacionJwt } from "./useRenovacionJwt.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loadingMe, setLoadingMe] = useState(true);
    const [sessionExpired, setSessionExpired] = useState(false);

    const refreshMe = useCallback(async () => {
        try {
            const me = await getMe();
            setUser(me);
            return me;
        } catch (e) {
            if (e?.status === 401) setUser(null);
            throw e;
        } finally {
            setLoadingMe(false);
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
            setUser(null);
            setSessionExpired(true);
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
            setUser(null);
            setSessionExpired(false);
        }
    }, []);

    const clearSessionExpired = useCallback(() => {
        setSessionExpired(false);
    }, []);

    const value = useMemo(
        () => ({
            user,
            setUser,
            refreshMe,
            logout,
            loadingMe,
            sessionExpired,
            clearSessionExpired,
        }),
        [user, refreshMe, logout, loadingMe, sessionExpired, clearSessionExpired]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
    return ctx;
}