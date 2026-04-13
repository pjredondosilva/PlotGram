import { Outlet, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Encabezado from "../componentes/layout/Encabezado.jsx";
import ModalAutenticacion from "../componentes/Autenticacion/ModalAutenticacion.jsx";
import FormularioLogin from "../componentes/Autenticacion/FormularioInicioDeSesion.jsx";
import FormularioRegistro from "../componentes/Autenticacion/FormularioRegistro.jsx";
import { useAuth } from "../servicios/ContextoDeAutenticacion.jsx";

const LOGIN_FORM_INICIAL = {
    nombre: "",
    contrasenia: "",
};

const REGISTER_FORM_INICIAL = {
    nombre: "",
    email: "",
    contrasenia: "",
};

export default function LayoutPrincipal() {
    const navigate = useNavigate();
    const {
        user,
        setUser,
        logout,
        sessionExpired,
        clearSessionExpired,
    } = useAuth();

    const [modal, setModal] = useState(null); // "login" | "register" | null
    const [loginForm, setLoginForm] = useState(LOGIN_FORM_INICIAL);
    const [registerForm, setRegisterForm] = useState(REGISTER_FORM_INICIAL);

    useEffect(() => {
        if (!sessionExpired) return;

        setModal("login");
        navigate("/", { replace: true });
    }, [sessionExpired, navigate]);

    async function handleLogout() {
        try {
            await logout();
        } finally {
            setModal(null);
        }
    }

    function handleCloseModal() {
        setModal(null);
        clearSessionExpired();
    }

    function handleLoginDone() {
        setModal(null);
        clearSessionExpired();
        setLoginForm(LOGIN_FORM_INICIAL);
    }

    function handleRegisterDone() {
        setModal(null);
        clearSessionExpired();
        setRegisterForm(REGISTER_FORM_INICIAL);
    }

    return (
        <>
            <Encabezado
                user={user}
                onLogin={() => setModal("login")}
                onRegister={() => setModal("register")}
                onLogout={handleLogout}
            />

            <main>
                <Outlet />
            </main>

            <ModalAutenticacion open={modal !== null} onClose={handleCloseModal}>
                {modal === "login" && (
                    <>
                        {sessionExpired && (
                            <div
                                style={{
                                    marginBottom: "14px",
                                    padding: "12px 14px",
                                    borderRadius: "12px",
                                    border: "1px solid rgba(255,255,255,0.10)",
                                    background: "rgba(255,255,255,0.04)",
                                    color: "rgba(255,255,255,0.92)",
                                }}
                            >
                                <strong style={{ display: "block", marginBottom: "4px" }}>
                                    Tu sesión ha expirado
                                </strong>
                                <span style={{ color: "rgba(255,255,255,0.74)" }}>
                                    Por seguridad, debes iniciar sesión de nuevo para continuar.
                                </span>
                            </div>
                        )}

                        <FormularioLogin
                            form={loginForm}
                            setForm={setLoginForm}
                            onDone={handleLoginDone}
                            setUser={setUser}
                            resetForm={() => setLoginForm(LOGIN_FORM_INICIAL)}
                        />
                    </>
                )}

                {modal === "register" && (
                    <FormularioRegistro
                        form={registerForm}
                        setForm={setRegisterForm}
                        onDone={handleRegisterDone}
                        setUser={setUser}
                        resetForm={() => setRegisterForm(REGISTER_FORM_INICIAL)}
                    />
                )}
            </ModalAutenticacion>
        </>
    );
}