import { Outlet } from "react-router-dom";
import { useState } from "react";
import Header from "../componentes/layout/Header";
import AuthModal from "../componentes/auth/AuthModal";
import FormularioLogin from "../componentes/auth/FormularioInicioDeSesion.jsx";
import FormularioRegistro from "../componentes/auth/FormularioRegistro.jsx";
import { useAuth } from "../servicios/authContext.jsx";

const LOGIN_FORM_INICIAL = {
    nombre: "",
    contrasenia: "",
};

const REGISTER_FORM_INICIAL = {
    nombre: "",
    email: "",
    contrasenia: "",
};

export default function MainLayout() {
    const { user, setUser, logout } = useAuth();
    const [modal, setModal] = useState(null); // "login" | "register" | null

    const [loginForm, setLoginForm] = useState(LOGIN_FORM_INICIAL);
    const [registerForm, setRegisterForm] = useState(REGISTER_FORM_INICIAL);

    async function handleLogout() {
        try {
            await logout();
        } finally {
            setModal(null);
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
                    <FormularioLogin
                        form={loginForm}
                        setForm={setLoginForm}
                        onDone={() => setModal(null)}
                        setUser={setUser}
                        resetForm={() => setLoginForm(LOGIN_FORM_INICIAL)}
                    />
                )}

                {modal === "register" && (
                    <FormularioRegistro
                        form={registerForm}
                        setForm={setRegisterForm}
                        onDone={() => setModal(null)}
                        setUser={setUser}
                        resetForm={() => setRegisterForm(REGISTER_FORM_INICIAL)}
                    />
                )}
            </AuthModal>
        </>
    );
}