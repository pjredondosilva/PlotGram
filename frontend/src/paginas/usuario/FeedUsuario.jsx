import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import {
    borrarLista,
    crearLista,
    editarLista,
    obtenerListasDeUsuario,
} from "../../servicios/ServicioListas.js";
import { obtenerUsuario } from "../../servicios/ServicioAutenticacion.js";
import FormularioLista from "../../componentes/listas/FormularioListas.jsx";
import TarjetaLista from "../../componentes/listas/TarjetaLista.jsx";
import FormularioEditarPerfil from "../../componentes/usuario/FormularioEditarPerfil.jsx";
import ModalSistema from "../../componentes/Autenticacion/ModalSistema.jsx";
import "../tmdb/estilos/detalleTmdb.css";
import "./estilos/feedUsuario.css";

function descripcionUsuarioPorDefecto(nombre) {
    if (!nombre) return "Todavía no has añadido una descripción.";
    return `El espacio personal de ${nombre} para organizar películas, series, temporadas y episodios.`;
}

function inicialUsuario(nombre) {
    return (nombre || "?").trim().charAt(0).toUpperCase();
}

export default function FeedUsuario() {
    const { idUsuario } = useParams();
    const navigate = useNavigate();
    const { user, setUser, logout, loadingMe } = useAuth();

    const [perfil, setPerfil] = useState(null);
    const [listas, setListas] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");
    const [modalNuevaAbierto, setModalNuevaAbierto] = useState(false);
    const [modalEditarPerfilAbierto, setModalEditarPerfilAbierto] = useState(false);
    const [listaEnEdicion, setListaEnEdicion] = useState(null);
    const [menuAbiertoId, setMenuAbiertoId] = useState(null);

    const esPropietario = useMemo(() => {
        return user && String(user.id) === String(idUsuario);
    }, [user, idUsuario]);

    const avatar = useMemo(() => inicialUsuario(perfil?.nombre), [perfil]);

    const descripcionUsuario = useMemo(
        () =>
            perfil?.descripcion?.trim()
                ? perfil.descripcion
                : descripcionUsuarioPorDefecto(perfil?.nombre),
        [perfil]
    );

    useEffect(() => {
        if (loadingMe) return;

        // Si no hay usuario, no cargamos nada (la UI mostrará el aviso de login)
        if (!user) {
            setCargando(false);
            return;
        }

        async function cargarDatos() {
            setCargando(true);
            setError("");
            try {
                // Cargar perfil
                if (esPropietario) {
                    setPerfil(user);
                } else {
                    const datosUsuario = await obtenerUsuario(idUsuario);
                    setPerfil(datosUsuario);
                }

                // Cargar listas
                const data = await obtenerListasDeUsuario(idUsuario);
                setListas(data ?? []);
            } catch (e) {
                setError(e.message || "No se pudieron cargar los datos del perfil.");
            } finally {
                setCargando(false);
            }
        }

        cargarDatos();
    }, [idUsuario, user, loadingMe, esPropietario]);

    async function cargarListas() {
        try {
            const data = await obtenerListasDeUsuario(idUsuario);
            setListas(data ?? []);
        } catch (e) {
            setError(e.message || "No se pudieron cargar las listas.");
        }
    }

    async function manejarCrearLista(dto) {
        await crearLista(dto);
        await cargarListas();
    }

    async function manejarEditarLista(dto) {
        if (!listaEnEdicion) return;
        await editarLista(listaEnEdicion.id, dto);
        setListaEnEdicion(null);
        await cargarListas();
    }

    async function manejarBorrarLista(lista) {
        const confirmado = window.confirm(
            `¿Seguro que quieres borrar la lista "${lista.nombre}"?`
        );
        if (!confirmado) return;
        await borrarLista(lista.id);
        setMenuAbiertoId(null);
        setListas((prev) => prev.filter((item) => item.id !== lista.id));
    }

    async function manejarNuevoLoginRequerido() {
        await logout();
        window.alert(
            "Perfil actualizado. Inicia sesión de nuevo con tus credenciales actualizadas."
        );
    }

    if (loadingMe || cargando) {
        return <div className="tmdb-cargando">Cargando perfil...</div>;
    }

    if (!user) {
        return (
            <section className="tmdb-detalle feed-usuario">
                <div className="tmdb-panel feed-aviso">
                    <h1>Acceso restringido</h1>
                    <p>Para ver perfiles de otros usuarios y sus listas, debes estar registrado e iniciar sesión en PlotGram.</p>
                    <Link to="/" className="tmdb-boton-primario">
                        Volver al inicio
                    </Link>
                </div>
            </section>
        );
    }

    if (!perfil) {
        return (
            <section className="tmdb-detalle feed-usuario">
                <div className="tmdb-panel feed-aviso">
                    <h1>{error ? "Error al cargar" : "Usuario no encontrado"}</h1>
                    <p>{error || "El perfil que buscas no existe o no está disponible."}</p>
                    <Link to="/" className="tmdb-boton-primario">
                        Volver al inicio
                    </Link>
                </div>
            </section>
        );
    }

    return (
        <section className="tmdb-detalle feed-usuario">
            <header className="tmdb-panel feed-cabecera">
                <div className="feed-perfil">
                    {perfil.fotoPerfil ? (
                        <img
                            className="feed-avatar-imagen"
                            src={perfil.fotoPerfil}
                            alt={perfil.nombre}
                            onError={(e) => {
                                e.target.onerror = null;
                                e.target.src = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y";
                            }}
                        />
                    ) : (
                        <div className="feed-avatar">{avatar}</div>
                    )}

                    <div className="feed-perfil-texto">
                        <div className="feed-perfil-superior">
                            <h1>{perfil.nombre}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {listas.length} listas
                                </span>
                                <span className="tmdb-meta-chip">
                                    {esPropietario ? "Feed personal" : "Perfil de usuario"}
                                </span>
                            </div>
                        </div>

                        <p>{descripcionUsuario}</p>
                    </div>
                </div>

                {esPropietario && (
                    <div className="feed-cabecera-acciones">
                        <button
                            type="button"
                            className="tmdb-boton-secundario"
                            onClick={() => setModalEditarPerfilAbierto(true)}
                        >
                            Editar perfil
                        </button>
                    </div>
                )}
            </header>

            <section className="tmdb-bloque feed-bloque">
                <div className="tmdb-seccion-cabecera feed-bloque-cabecera">
                    <div>
                        <h2>{esPropietario ? "Mis listas" : `Listas de ${perfil.nombre}`}</h2>
                        <p>
                            Explora las colecciones guardadas por este usuario.
                        </p>
                    </div>

                    {esPropietario && (
                        <button
                            type="button"
                            className="tmdb-boton-secundario"
                            onClick={() => setModalNuevaAbierto(true)}
                        >
                            Añadir lista
                        </button>
                    )}
                </div>

                {error && <div className="tmdb-error">{error}</div>}

                {!listas.length ? (
                    <div className="feed-vacio">
                        <h3>No hay listas públicas</h3>
                        <p>
                            Este usuario aún no ha creado ninguna lista o son privadas.
                        </p>
                    </div>
                ) : (
                    <div className="feed-listas-grid">
                        {listas.map((lista) => (
                            <TarjetaLista
                                key={lista.id}
                                lista={lista}
                                nombreUsuario={perfil.nombre}
                                idUsuario={perfil.id}
                                menuAbierto={menuAbiertoId === lista.id}
                                onAbrirMenu={esPropietario ? setMenuAbiertoId : () => {}}
                                onCerrarMenu={() => setMenuAbiertoId(null)}
                                onEditar={esPropietario ? (listaSeleccionada) => {
                                    setMenuAbiertoId(null);
                                    setListaEnEdicion(listaSeleccionada);
                                } : null}
                                onBorrar={esPropietario ? manejarBorrarLista : null}
                            />
                        ))}
                    </div>
                )}
            </section>

            {esPropietario && (
                <>
                    <ModalSistema
                        open={modalNuevaAbierto}
                        onClose={() => setModalNuevaAbierto(false)}
                    >
                        {modalNuevaAbierto && (
                            <FormularioLista
                                onClose={() => setModalNuevaAbierto(false)}
                                onSubmit={manejarCrearLista}
                                titulo="Crear nueva lista"
                                textoBoton="Crear lista"
                            />
                        )}
                    </ModalSistema>

                    <ModalSistema
                        open={!!listaEnEdicion}
                        onClose={() => setListaEnEdicion(null)}
                    >
                        {listaEnEdicion && (
                            <FormularioLista
                                onClose={() => setListaEnEdicion(null)}
                                onSubmit={manejarEditarLista}
                                titulo="Editar lista"
                                textoBoton="Guardar cambios"
                                valoresIniciales={listaEnEdicion}
                            />
                        )}
                    </ModalSistema>

                    <FormularioEditarPerfil
                        open={modalEditarPerfilAbierto}
                        onClose={() => setModalEditarPerfilAbierto(false)}
                        user={user}
                        setUser={setUser}
                        onRequiereNuevoLogin={manejarNuevoLoginRequerido}
                    />
                </>
            )}
        </section>
    );
}