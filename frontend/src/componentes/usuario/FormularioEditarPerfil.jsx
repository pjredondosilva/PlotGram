import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthModal from "../auth/AuthModal.jsx";
import { useAuth } from "../../servicios/authContext.jsx";
import { actualizarMiPerfil, verificarContrasenaActual } from "../../servicios/auth.js";
import "../../paginas/usuario/estilos/feedUsuario.css";

const FORMULARIO_VACIO = {
    nombre: "",
    email: "",
    fotoPerfil: "",
    descripcion: "",
    nuevaContrasena: "",
    confirmacionNuevaContrasena: "",
};

const hasUpper = (s) => /[A-Z]/.test(s);
const hasDigit = (s) => /\d/.test(s);
const hasSpecial = (s) => /[.,!@#$%^&*()_\-+=\[\]{};:'"\\|<>/?]/.test(s);

export default function FormularioEditarPerfil({
                                                   open,
                                                   onClose,
                                                   user,
                                                   setUser,
                                               }) {
    const navigate = useNavigate();
    const { logout } = useAuth();

    const [paso, setPaso] = useState("verificacion");
    const [contrasenaActual, setContrasenaActual] = useState("");
    const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
    const [error, setError] = useState("");
    const [guardando, setGuardando] = useState(false);

    const vistaPrevia = useMemo(() => {
        const url = (formulario.fotoPerfil || "").trim();
        return url || null;
    }, [formulario.fotoPerfil]);

    const nuevaContrasena = formulario.nuevaContrasena;
    const quiereCambiarContrasena = nuevaContrasena.trim().length > 0;

    const reglasContrasena = useMemo(() => {
        return {
            length: nuevaContrasena.length >= 8,
            upper: hasUpper(nuevaContrasena),
            digit: hasDigit(nuevaContrasena),
            special: hasSpecial(nuevaContrasena),
        };
    }, [nuevaContrasena]);

    const nuevaContrasenaValida = Object.values(reglasContrasena).every(Boolean);

    const requiereNuevoLogin = useMemo(() => {
        const nombreActual = (user?.nombre || "").trim();
        const nombreNuevo = (formulario.nombre || "").trim();

        return nombreNuevo !== nombreActual || quiereCambiarContrasena;
    }, [formulario.nombre, quiereCambiarContrasena, user]);

    useEffect(() => {
        if (!open) return;

        setPaso("verificacion");
        setContrasenaActual("");
        setFormulario({
            nombre: user?.nombre ?? "",
            email: user?.email ?? "",
            fotoPerfil: user?.fotoPerfil ?? "",
            descripcion: user?.descripcion ?? "",
            nuevaContrasena: "",
            confirmacionNuevaContrasena: "",
        });
        setError("");
        setGuardando(false);
    }, [open, user]);

    async function verificar(e) {
        e.preventDefault();
        setError("");
        setGuardando(true);

        try {
            await verificarContrasenaActual(contrasenaActual);
            setPaso("edicion");
        } catch (e) {
            setError(e.message || "No se pudo verificar la contraseña actual.");
        } finally {
            setGuardando(false);
        }
    }

    async function guardar(e) {
        e.preventDefault();
        setError("");

        const nuevaContrasenaTrim = formulario.nuevaContrasena.trim();
        const confirmacion = formulario.confirmacionNuevaContrasena.trim();

        if (quiereCambiarContrasena && !nuevaContrasenaValida) {
            setError("La nueva contraseña no cumple los requisitos de seguridad.");
            return;
        }

        if (nuevaContrasenaTrim && nuevaContrasenaTrim !== confirmacion) {
            setError("La nueva contraseña y su confirmación no coinciden.");
            return;
        }

        setGuardando(true);

        try {
            const actualizado = await actualizarMiPerfil({
                nombre: formulario.nombre.trim(),
                email: formulario.email.trim(),
                fotoPerfil: formulario.fotoPerfil.trim(),
                descripcion: formulario.descripcion.trim(),
                contrasenaActual,
                nuevaContrasena: nuevaContrasenaTrim,
            });

            if (requiereNuevoLogin) {
                window.alert("Los cambios se han guardado correctamente. Debes iniciar sesión de nuevo.");
                await logout();
                onClose();
                navigate("/", { replace: true });
                return;
            }

            setUser?.(actualizado);
            onClose();
        } catch (e) {
            setError(e.message || "No se pudo actualizar el perfil.");
        } finally {
            setGuardando(false);
        }
    }

    return (
        <AuthModal open={open} onClose={onClose}>
            <div className="feed-formulario">
                {paso === "verificacion" ? (
                    <>
                        <h2>Editar perfil</h2>
                        <p className="feed-formulario-subtitulo">
                            Antes de modificar tus datos, introduce tu contraseña actual.
                        </p>

                        <form className="feed-formulario-grid" onSubmit={verificar}>
                            <label className="feed-formulario-campo">
                                <span>Contraseña actual</span>
                                <input
                                    className="pg-modal__input"
                                    type="password"
                                    value={contrasenaActual}
                                    onChange={(e) => setContrasenaActual(e.target.value)}
                                    placeholder="Introduce tu contraseña"
                                    required
                                />
                            </label>

                            {error && <div className="feed-formulario-error">{error}</div>}

                            <div className="feed-formulario-acciones">
                                <button
                                    type="button"
                                    className="feed-boton feed-boton-secundario"
                                    onClick={onClose}
                                >
                                    Cancelar
                                </button>

                                <button
                                    type="submit"
                                    className="feed-boton feed-boton-primario"
                                    disabled={guardando}
                                >
                                    {guardando ? "Verificando..." : "Continuar"}
                                </button>
                            </div>
                        </form>
                    </>
                ) : (
                    <>
                        <h2>Editar perfil</h2>
                        <p className="feed-formulario-subtitulo">
                            Actualiza tus datos personales y, si cambias tu nombre o tu contraseña, tendrás que iniciar sesión de nuevo.
                        </p>

                        <form className="feed-formulario-grid" onSubmit={guardar}>
                            <label className="feed-formulario-campo">
                                <span>Nombre de usuario</span>
                                <input
                                    className="pg-modal__input"
                                    value={formulario.nombre}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({ ...prev, nombre: e.target.value }))
                                    }
                                    maxLength={30}
                                    required
                                />
                            </label>

                            <label className="feed-formulario-campo">
                                <span>Email</span>
                                <input
                                    className="pg-modal__input"
                                    type="email"
                                    value={formulario.email}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({ ...prev, email: e.target.value }))
                                    }
                                    required
                                />
                            </label>

                            <label className="feed-formulario-campo">
                                <span className="feed-label-con-ayuda">
                                    <span>Imagen de perfil</span>

                                    <span className="feed-ayuda-wrapper" tabIndex={0}>
                                        <span
                                            className="feed-ayuda-icono"
                                            aria-label="Ayuda sobre cómo obtener la URL de una imagen"
                                        >
                                            i
                                        </span>

                                        <span className="feed-ayuda-tooltip" role="tooltip">
                                            Abre una imagen en internet, pulsa clic derecho sobre ella y selecciona
                                            <strong> “Copiar dirección de imagen” </strong>
                                            o la opción similar de tu navegador. Después pega aquí esa URL.
                                            La dirección debe empezar por
                                            <strong> http:// </strong>
                                            o
                                            <strong> https://</strong>.
                                        </span>
                                    </span>
                                </span>

                                <input
                                    className="pg-modal__input"
                                    type="url"
                                    value={formulario.fotoPerfil}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({ ...prev, fotoPerfil: e.target.value }))
                                    }
                                    placeholder="https://..."
                                    maxLength={255}
                                />
                            </label>

                            <div className="feed-formulario-preview feed-formulario-preview-perfil">
                                {vistaPrevia ? (
                                    <img src={vistaPrevia} alt="Vista previa del perfil" />
                                ) : (
                                    <div className="feed-formulario-preview-vacia">
                                        Sin imagen de perfil
                                    </div>
                                )}
                            </div>

                            <label className="feed-formulario-campo">
                                <span>Descripción</span>
                                <textarea
                                    className="pg-modal__input feed-textarea"
                                    value={formulario.descripcion}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({ ...prev, descripcion: e.target.value }))
                                    }
                                    maxLength={300}
                                    rows={4}
                                />
                            </label>

                            <div className="feed-formulario-separador" />

                            <label className="feed-formulario-campo">
                                <span>Nueva contraseña</span>
                                <input
                                    className="pg-modal__input"
                                    type="password"
                                    value={formulario.nuevaContrasena}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({ ...prev, nuevaContrasena: e.target.value }))
                                    }
                                    placeholder="Dejar en blanco si no se desea cambiar"
                                />
                            </label>

                            {quiereCambiarContrasena && (
                                <div className="pg-modal__rules">
                                    <div className={`pg-modal__rule ${reglasContrasena.length ? "is-ok" : "is-bad"}`}>
                                        <span className="pg-modal__mark">{reglasContrasena.length ? "✅" : "❌"}</span>
                                        8+ caracteres
                                    </div>
                                    <div className={`pg-modal__rule ${reglasContrasena.upper ? "is-ok" : "is-bad"}`}>
                                        <span className="pg-modal__mark">{reglasContrasena.upper ? "✅" : "❌"}</span>
                                        1 mayúscula
                                    </div>
                                    <div className={`pg-modal__rule ${reglasContrasena.digit ? "is-ok" : "is-bad"}`}>
                                        <span className="pg-modal__mark">{reglasContrasena.digit ? "✅" : "❌"}</span>
                                        1 número
                                    </div>
                                    <div className={`pg-modal__rule ${reglasContrasena.special ? "is-ok" : "is-bad"}`}>
                                        <span className="pg-modal__mark">{reglasContrasena.special ? "✅" : "❌"}</span>
                                        1 carácter especial (ej: . o ,)
                                    </div>
                                </div>
                            )}

                            <label className="feed-formulario-campo">
                                <span>Confirmar nueva contraseña</span>
                                <input
                                    className="pg-modal__input"
                                    type="password"
                                    value={formulario.confirmacionNuevaContrasena}
                                    onChange={(e) =>
                                        setFormulario((prev) => ({
                                            ...prev,
                                            confirmacionNuevaContrasena: e.target.value,
                                        }))
                                    }
                                    placeholder="Repite la nueva contraseña"
                                />
                            </label>

                            {error && <div className="feed-formulario-error">{error}</div>}

                            <div className="feed-formulario-acciones">
                                <button
                                    type="button"
                                    className="feed-boton feed-boton-secundario"
                                    onClick={onClose}
                                >
                                    Cancelar
                                </button>

                                <button
                                    type="submit"
                                    className="feed-boton feed-boton-primario"
                                    disabled={guardando || (quiereCambiarContrasena && !nuevaContrasenaValida)}
                                >
                                    {guardando
                                        ? (requiereNuevoLogin
                                            ? "Guardando cambios y cerrando sesión..."
                                            : "Guardando cambios...")
                                        : (requiereNuevoLogin
                                            ? "Guardar cambios y cerrar sesión"
                                            : "Guardar cambios")}
                                </button>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </AuthModal>
    );
}