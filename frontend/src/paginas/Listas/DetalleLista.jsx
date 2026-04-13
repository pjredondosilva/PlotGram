import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import {
    borrarLista,
    editarLista,
    eliminarElementoDeLista,
    obtenerDetalleListaDeUsuario,
} from "../../servicios/ServicioListas.js";
import FormularioLista from "../../componentes/listas/FormularioListas.jsx";
import "../Tmdb/estilos/detalleTmdb.css";
import "../usuario/estilos/feedUsuario.css";

function formatearFecha(fecha) {
    if (!fecha) return "Fecha no disponible";

    const d = new Date(fecha);
    if (Number.isNaN(d.getTime())) return fecha;

    return d.toLocaleDateString("es-ES", {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}

function etiquetaTipo(tipo) {
    switch (tipo) {
        case "PELICULA":
            return "Película";
        case "SERIE":
            return "Serie";
        case "TEMPORADA":
            return "Temporada";
        case "EPISODIO":
            return "Episodio";
        default:
            return tipo || "Contenido";
    }
}

function normalizarEnlace(enlace) {
    if (!enlace) return null;
    if (enlace.startsWith("http://") || enlace.startsWith("https://")) return enlace;
    return enlace.startsWith("/") ? enlace : `/${enlace}`;
}

function TarjetaContenido({
                              item,
                              esPropietario,
                              menuAbierto,
                              onAbrirMenu,
                              onCerrarMenu,
                              onEliminar,
                          }) {
    const enlace = normalizarEnlace(item.enlace);
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

    const contenido = (
        <>
            <div className="feed-contenido-imagen">
                {item.imagen ? (
                    <img src={item.imagen} alt={item.titulo} />
                ) : (
                    <div className="feed-contenido-imagen-vacia">Sin imagen</div>
                )}
            </div>

            <div className="feed-contenido-cuerpo">
                <div className="feed-contenido-cabecera">
                    <span className="feed-chip-tipo">{etiquetaTipo(item.tipo)}</span>
                    <span className="feed-contenido-fecha">{formatearFecha(item.fechaPublicacion)}</span>
                </div>

                <h3>{item.titulo}</h3>
                <p>{item.sinopsis || "Sin descripción disponible."}</p>
            </div>
        </>
    );

    let card;
    if (!enlace) {
        card = <article className="feed-contenido-card tmdb-panel">{contenido}</article>;
    } else if (enlace.startsWith("http")) {
        card = (
            <a
                className="feed-contenido-card tmdb-panel"
                href={enlace}
                target="_blank"
                rel="noreferrer"
            >
                {contenido}
            </a>
        );
    } else {
        card = (
            <Link className="feed-contenido-card tmdb-panel" to={enlace}>
                {contenido}
            </Link>
        );
    }

    return (
        <div className="feed-contenido-card-wrapper">
            {esPropietario && (
                <div className="feed-lista-card-menu" ref={menuRef}>
                    <button
                        type="button"
                        className="feed-menu-boton"
                        aria-label={`Abrir opciones de ${item.titulo}`}
                        onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            menuAbierto ? onCerrarMenu() : onAbrirMenu(item.idItem);
                        }}
                    >
                        ⋯
                    </button>

                    {menuAbierto && (
                        <div className="feed-menu-desplegable">
                            <button
                                type="button"
                                className="feed-menu-opcion-peligro"
                                onClick={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    onEliminar(item);
                                }}
                            >
                                Borrar
                            </button>
                        </div>
                    )}
                </div>
            )}

            {card}
        </div>
    );
}

export default function DetalleLista() {
    const { idUsuario, idLista } = useParams();
    const navigate = useNavigate();
    const { user, loadingMe } = useAuth();

    const [lista, setLista] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [modalEditarAbierto, setModalEditarAbierto] = useState(false);
    const [menuAbiertoId, setMenuAbiertoId] = useState(null);

    const esPropietario =
        user?.id != null && Number(user.id) === Number(idUsuario);

    const nombreAutor = esPropietario
        ? user?.nombre || `Usuario ${idUsuario}`
        : `Usuario ${idUsuario}`;

    const avatar = useMemo(() => {
        return (nombreAutor || "?").trim().charAt(0).toUpperCase();
    }, [nombreAutor]);

    const rutaVolver = esPropietario
        ? `/usuarios/${idUsuario}/feed`
        : "/";

    useEffect(() => {
        cargarDetalle();
    }, [idUsuario, idLista]);

    async function cargarDetalle() {
        setLoading(true);
        setError("");

        try {
            const data = await obtenerDetalleListaDeUsuario(idUsuario, idLista);
            setLista(data);
        } catch (e) {
            setError(e.message || "No se pudo cargar la lista.");
        } finally {
            setLoading(false);
        }
    }

    async function manejarEditar(dto) {
        await editarLista(idLista, dto);
        setModalEditarAbierto(false);
        await cargarDetalle();
    }

    async function manejarBorrar() {
        const confirmado = window.confirm(
            `¿Seguro que quieres borrar la lista "${lista?.nombre}"?`
        );
        if (!confirmado) return;

        await borrarLista(idLista);
        navigate(`/usuarios/${idUsuario}/feed`);
    }

    async function manejarEliminarElemento(item) {
        const confirmado = window.confirm(
            `¿Seguro que quieres quitar "${item.titulo}" de esta lista?`
        );
        if (!confirmado) return;

        try {
            await eliminarElementoDeLista(idLista, item.idItem);

            setLista((prev) => ({
                ...prev,
                elementos: (prev?.elementos || []).filter(
                    (elemento) => elemento.idItem !== item.idItem
                ),
            }));
            setMenuAbiertoId(null);
        } catch (e) {
            window.alert(e.message || "No se pudo eliminar el elemento de la lista.");
        }
    }

    if (loading) {
        return <div className="tmdb-cargando">Cargando lista...</div>;
    }

    if (error) {
        return <div className="tmdb-error">{error}</div>;
    }

    if (!lista) {
        return <div className="tmdb-vacio">No se ha encontrado la lista.</div>;
    }

    return (
        <section className="tmdb-detalle feed-usuario">
            <Link to={rutaVolver} className="tmdb-volver">
                ← Volver
            </Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-poster">
                    {lista.imagenPortada ? (
                        <img src={lista.imagenPortada} alt={lista.nombre} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin portada</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{lista.nombre}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {lista.elementos?.length ?? 0} elementos
                                </span>
                                <span className="tmdb-meta-chip">
                                    {esPropietario ? "Tu lista" : "Lista pública"}
                                </span>
                            </div>
                        </div>

                        <div className="feed-autor-lista">
                            <div className="feed-avatar feed-avatar-pequeno">{avatar}</div>
                            <div>
                                <strong>{nombreAutor}</strong>
                                <p>Creador de la lista</p>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Descripción</h2>
                            <p>{lista.descripcion || "Esta lista no tiene descripción."}</p>
                        </div>

                        {!loadingMe && esPropietario && (
                            <div className="tmdb-resumen-acciones">
                                <button
                                    type="button"
                                    className="tmdb-boton-primario"
                                    onClick={() => setModalEditarAbierto(true)}
                                >
                                    Editar lista
                                </button>

                                <button
                                    type="button"
                                    className="tmdb-boton-secundario feed-boton-peligro"
                                    onClick={manejarBorrar}
                                >
                                    Borrar lista
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <section className="tmdb-bloque feed-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Contenido de la lista</h2>
                    <p>Todos los elementos guardados dentro de esta colección.</p>
                </div>

                {lista.elementos?.length ? (
                    <div className="feed-contenido-grid">
                        {lista.elementos.map((item) => (
                            <TarjetaContenido
                                key={item.idItem}
                                item={item}
                                esPropietario={esPropietario}
                                menuAbierto={menuAbiertoId === item.idItem}
                                onAbrirMenu={setMenuAbiertoId}
                                onCerrarMenu={() => setMenuAbiertoId(null)}
                                onEliminar={manejarEliminarElemento}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="feed-vacio">
                        <h3>La lista todavía está vacía</h3>
                        <p>Cuando añadas películas, series, temporadas o episodios aparecerán aquí.</p>
                    </div>
                )}
            </section>

            <FormularioLista
                open={modalEditarAbierto}
                onClose={() => setModalEditarAbierto(false)}
                onSubmit={manejarEditar}
                titulo="Editar lista"
                textoBoton="Guardar cambios"
                valoresIniciales={lista}
            />
        </section>
    );
}