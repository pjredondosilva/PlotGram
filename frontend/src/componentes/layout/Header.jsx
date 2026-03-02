import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./estilos/Header.css";

export default function Header({ user, onLogin, onRegister, onLogout }) {
    const navigate = useNavigate();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);

    useEffect(() => {
        function onDocClick(e) {
            if (!menuRef.current) return;
            if (!menuRef.current.contains(e.target)) setMenuOpen(false);
        }
        document.addEventListener("mousedown", onDocClick);
        return () => document.removeEventListener("mousedown", onDocClick);
    }, []);

    return (
        <header className="pg-header">
            <div className="pg-header__left">
                {/* Si tienes logo */}
                {/* <img className="pg-logo" src="/logo.png" alt="Plotgram" /> */}
                <div className="pg-brand" onClick={() => navigate("/")} style={{ cursor: "pointer" }}>
                    Plotgram
                </div>
            </div>

            <div className="pg-header__right">
                {!user ? (
                    <>
                        <button className="pg-link" onClick={onLogin}>Iniciar sesión</button>
                        <span className="pg-sep">|</span>
                        <button className="pg-link" onClick={onRegister}>Registrarse</button>
                    </>
                ) : (
                    <>
                        <button className="pg-link" onClick={() => navigate("/")}>Home</button>

                        <div className="pg-user" ref={menuRef}>
                            <button
                                className="pg-user__btn"
                                type="button"
                                onClick={() => setMenuOpen(v => !v)}
                            >
                                {user.nombre} <span className="pg-user__caret">▾</span>
                            </button>

                            {menuOpen && (
                                <div className="pg-user__menu">
                                    <button className="pg-user__item" type="button">Opción 1</button>
                                    <button className="pg-user__item" type="button">Opción 2</button>

                                    <div className="pg-user__sep" />

                                    <button className="pg-user__item pg-user__item--danger" type="button" onClick={onLogout}>
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
