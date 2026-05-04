import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import BuscadorUsuarios from "./BuscadorUsuarios.jsx";
import "./estilos/Encabezado.css";

function inicialUsuario(nombre) {
    return (nombre || "?").trim().charAt(0).toUpperCase();
}

export default function Encabezado({ user, onLogin, onRegister, onLogout }) {
    const navigate = useNavigate();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);

    const inicial = useMemo(() => inicialUsuario(user?.nombre), [user]);

    useEffect(() => {
        function onDocClick(e) {
            if (!menuRef.current) return;
            if (!menuRef.current.contains(e.target)) setMenuOpen(false);
        }
        document.addEventListener("mousedown", onDocClick);
        return () => document.removeEventListener("mousedown", onDocClick);
    }, []);

    useEffect(() => {
        setMenuOpen(false);
    }, [user]);

    return (
        <header className="pg-header">
            <div className="pg-header__left">
                <div className="pg-brand" onClick={() => navigate("/")} style={{ cursor: "pointer" }}>
                    Plotgram
                </div>
            </div>

            <div className="pg-header__right">
                {user && <BuscadorUsuarios />}
                {!user ? (
                    <>
                        <button className="pg-link" onClick={onLogin}>Iniciar sesión</button>
                        <span className="pg-sep">|</span>
                        <button className="pg-link" onClick={onRegister}>Registrarse</button>
                    </>
                ) : (
                    <>
                        <div className="pg-user" ref={menuRef}>
                            <div className="pg-user__identidad">
                                <span className="pg-user__nombre">{user.nombre}</span>

                                <button
                                    className="pg-user__avatar-btn"
                                    type="button"
                                    onClick={() => setMenuOpen((v) => !v)}
                                    aria-label="Abrir menú de usuario"
                                >
                                    {user.fotoPerfil ? (
                                        <img
                                            className="pg-user__avatar-img"
                                            src={user.fotoPerfil}
                                            alt={user.nombre}
                                        />
                                    ) : (
                                        <span className="pg-user__avatar-fallback">{inicial}</span>
                                    )}
                                </button>
                            </div>

                            {menuOpen && (
                                <div className="pg-user__menu">
                                    <button
                                        className="pg-user__item"
                                        type="button"
                                        onClick={() => {
                                            setMenuOpen(false);

                                            if (user?.id != null) {
                                                navigate(`/usuarios/${user.id}/feed`);
                                            } else {
                                                navigate("/");
                                            }
                                        }}
                                    >
                                        Mi feed
                                    </button>

                                    <div className="pg-user__sep" />

                                    <button
                                        className="pg-user__item pg-user__item--danger"
                                        type="button"
                                        onClick={onLogout}
                                    >
                                        Cerrar sesión
                                    </button>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>
        </header>
    );
}