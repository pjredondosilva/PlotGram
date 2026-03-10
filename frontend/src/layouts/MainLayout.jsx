import { Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import Header from "../componentes/layout/Header";
import AuthModal from "../componentes/auth/AuthModal";
import LoginForm from "../componentes/auth/FormularioInicioDeSesion.jsx";
import RegisterForm from "../componentes/auth/FormularioRegistro.jsx";
import { getMe, logout } from "../servicios/auth";

export default function MainLayout() {
    const [user, setUser] = useState(null);
    const [modal, setModal] = useState(null); // "login" | "register" | null

    useEffect(() => {
        getMe()
            .then(setUser)
            .catch(() => setUser(null));
    }, []);

    async function handleLogout() {
        try {
            await logout();
        } finally {
            setUser(null);
        }
    }

    return (
        <>
            <Header
                user={user}
                onLogin={() => setModal("login")}
                onRegister={() => setModal("register")}
                onLogout={handleLogout}
            />

            <main>
                <Outlet />
            </main>

            <AuthModal open={modal !== null} onClose={() => setModal(null)}>
                {modal === "login" && (
                    <LoginForm
                        onDone={() => setModal(null)}
                        setUser={setUser}
                    />
                )}

                {modal === "register" && (
                    <RegisterForm
                        onDone={() => setModal(null)}
                        setUser={setUser}
                    />
                )}
            </AuthModal>
        </>
    );
}
