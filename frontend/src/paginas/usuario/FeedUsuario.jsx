import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../servicios/authContext.jsx";
import {
    borrarLista,
    crearLista,
    editarLista,
    obtenerMisListas,
} from "../../servicios/ServicioListas.js";
import FormularioLista from "../../componentes/listas/FormularioListas.jsx";
import FormularioEditarPerfil from "../../componentes/usuario/FormularioEditarPerfil.jsx";
import "../tmdb/estilos/detalleTmdb.css";
import "./estilos/feedUsuario.css";

function descripcionUsuarioPorDefecto(nombre) {
    if (!nombre) return "Todavía no has añadido una descripción.";
    return `El espacio personal de ${nombre} para organizar películas, series, temporadas y episodios.`;
}

function inicialUsuario(nombre) {
    return (nombre || "?").trim().charAt(0).toUpperCase();
}

function TarjetaLista({
                          lista,
                          nombreUsuario,
                          idUsuario,
                          menuAbierto,
                          onAbrirMenu,
                          onCerrarMenu,
                          onEditar,
                          onBorrar,
                      }) {
    const navigate = useNavigate();
    const menuRef = useRef(null);

    useEffect(() => {
        if (!menuAbierto) return;

        function cerrar(e) {
            if (!menuRef.current?.contains(e.target)) {
                onCerrarMenu();
            }
        }

        window.addEventListener("mousedown", cerrar);
        return () => window.removeEventListener("mousedown", cerrar);
    }, [menuAbierto, onCerrarMenu]);

    function abrirDetalle() {
        if (idUsuario == null) {
            window.alert("No se puede abrir la lista porque falta el id del usuario autenticado.");
            return;
        }

        navigate(`/usuarios/${idUsuario}/feed/listas/${lista.id}`);
    }

    return (
        <article className="feed-lista-card tmdb-panel">
            <div className="feed-lista-card-menu" ref={menuRef}>
                <button
                    type="button"
                    className="feed-menu-boton"
                    onClick={() => (menuAbierto ? onCerrarMenu() : onAbrirMenu(lista.id))}
                    aria-label={`Abrir opciones de la lista ${lista.nombre}`}
                >
                    ⋯
                </button>

                {menuAbierto && (
                    <div className="feed-menu-desplegable">
                        <button type="button" onClick={() => onEditar(lista)}>
                            Editar
                        </button>
                        <button
                            type="button"
                            className="feed-menu-opcion-peligro"
                            onClick={() => onBorrar(lista)}
                        >
                            Borrar
                        </button>
                    </div>
                )}
            </div>

            <button
                type="button"
                className="feed-lista-enlace"
                onClick={abrirDetalle}
            >
                <div className="feed-lista-portada">
                    {lista.imagenPortada ? (
                        <img src={lista.imagenPortada} alt={lista.nombre} />
                    ) : (
                        <div className="feed-lista-portada-vacia">Sin portada</div>
                    )}
                </div>

                <div className="feed-lista-cuerpo">
                    <div className="feed-lista-meta">
                        <span className="feed-lista-autor">{nombreUsuario}</span>
                        <span className="feed-lista-separador">·</span>
                        <span>{lista.numeroElementos} elementos</span>
                    </div>

                    <h3>{lista.nombre}</h3>
                    <p>{lista.descripcion || "Sin descripción."}</p>
                </div>
            </button>
        </article>
    );
}

export default function FeedUsuario() {
    const { idUsuario } = useParams();
    const navigate = useNavigate();
    const { user, setUser, logout, loadingMe } = useAuth();

    const [listas, setListas] = useState([]);
    const [cargandoListas, setCargandoListas] = useState(true);
    const [error, setError] = useState("");
    const [modalNuevaAbierto, setModalNuevaAbierto] = useState(false);
    const [modalEditarPerfilAbierto, setModalEditarPerfilAbierto] = useState(false);
    const [listaEnEdicion, setListaEnEdicion] = useState(null);
    const [menuAbiertoId, setMenuAbiertoId] = useState(null);

    const avatar = useMemo(() => inicialUsuario(user?.nombre), [user]);
    const descripcionUsuario = useMemo(
        () => (user?.descripcion?.trim() ? user.descripcion : descripcionUsuarioPorDefecto(user?.nombre)),
        [user]
    );

    useEffect(() => {
        if (loadingMe) return;

        if (!user) {
            setListas([]);
            setCargandoListas(false);
            return;
        }

        if (user.id == null) {
            setError("No se ha podido resolver el id del usuario autenticado.");
            setCargandoListas(false);
            return;
        }

        if (String(user.id) !== String(idUsuario)) {
            navigate(`/usuarios/${user.id}/feed`, { replace: true });
            return;
        }

        cargarListas();
    }, [user, loadingMe, idUsuario, navigate]);

    async function cargarListas() {
        setCargandoListas(true);
        setError("");

        try {
            const data = await obtenerMisListas();
            setListas(data ?? []);
        } catch (e) {
            setError(e.message || "No se pudieron cargar tus listas.");
        } finally {
            setCargandoListas(false);
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
        window.alert("Perfil actualizado. Inicia sesión de nuevo con tus credenciales actualizadas.");
    }

    if (loadingMe || cargandoListas) {
        return <div className="tmdb-cargando">Cargando tu feed...</div>;
    }

    if (!user) {
        return (
            <section className="tmdb-detalle feed-usuario">
                <div className="tmdb-panel feed-aviso">
                    <h1>Tu feed de listas</h1>
                    <p>Inicia sesión para crear y gestionar tus listas personales.</p>
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
                    {user.fotoPerfil ? (
                        <img className="feed-avatar-imagen" src={user.fotoPerfil} alt={user.nombre} />
                    ) : (
                        <div className="feed-avatar">{avatar}</div>
                    )}

                    <div className="feed-perfil-texto">
                        <div className="feed-perfil-superior">
                            <h1>{user.nombre}</h1>
                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">{listas.length} listas</span>
                                <span className="tmdb-meta-chip">Feed personal</span>
                            </div>
                        </div>

                        <p>{descripcionUsuario}</p>
                    </div>
                </div>

                <div className="feed-cabecera-acciones">
                    <button
                        type="button"
                        className="tmdb-boton-secundario"
                        onClick={() => setModalEditarPerfilAbierto(true)}
                    >
                        Editar perfil
                    </button>
                </div>
            </header>

            <section className="tmdb-bloque feed-bloque">
                <div className="tmdb-seccion-cabecera feed-bloque-cabecera">
                    <div>
                        <h2>Mis listas</h2>
                        <p>Organiza tu perfil con una cuadrícula visual al estilo de la aplicación.</p>
                    </div>

                    <button
                        type="button"
                        className="tmdb-boton-secundario"
                        onClick={() => setModalNuevaAbierto(true)}
                    >
                        Añadir lista
                    </button>
                </div>

                {error && <div className="tmdb-error">{error}</div>}

                {!listas.length ? (
                    <div className="feed-vacio">
                        <h3>Aún no has creado ninguna lista</h3>
                        <p>
                            Crea tu primera lista para empezar a llenar tu feed con portadas y colecciones personalizadas.
                        </p>
                        <button
                            type="button"
                            className="tmdb-boton-primario"
                            onClick={() => setModalNuevaAbierto(true)}
                        >
                            Crear primera lista
                        </button>
                    </div>
                ) : (
                    <div className="feed-listas-grid">
                        {listas.map((lista) => (
                            <TarjetaLista
                                key={lista.id}
                                lista={lista}
                                nombreUsuario={user.nombre}
                                idUsuario={user.id}
                                menuAbierto={menuAbiertoId === lista.id}
                                onAbrirMenu={setMenuAbiertoId}
                                onCerrarMenu={() => setMenuAbiertoId(null)}
                                onEditar={(listaSeleccionada) => {
                                    setMenuAbiertoId(null);
                                    setListaEnEdicion(listaSeleccionada);
                                }}
                                onBorrar={manejarBorrarLista}
                            />
                        ))}
                    </div>
                )}
            </section>

            <FormularioLista
                open={modalNuevaAbierto}
                onClose={() => setModalNuevaAbierto(false)}
                onSubmit={manejarCrearLista}
                titulo="Crear nueva lista"
                textoBoton="Crear lista"
            />

            <FormularioLista
                open={!!listaEnEdicion}
                onClose={() => setListaEnEdicion(null)}
                onSubmit={manejarEditarLista}
                titulo="Editar lista"
                textoBoton="Guardar cambios"
                valoresIniciales={listaEnEdicion}
            />

            <FormularioEditarPerfil
                open={modalEditarPerfilAbierto}
                onClose={() => setModalEditarPerfilAbierto(false)}
                user={user}
                setUser={setUser}
                onRequiereNuevoLogin={manejarNuevoLoginRequerido}
            />
        </section>
    );
}