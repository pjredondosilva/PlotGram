import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../servicios/authContext.jsx";
import {
    borrarLista,
    editarLista,
    obtenerDetalleListaDeUsuario,
} from "../../servicios/ServicioListas.js";
import FormularioLista from "../../componentes/listas/FormularioListas.jsx";
import "../tmdb/estilos/detalleTmdb.css";
import "./estilos/feedUsuario.css";

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

function TarjetaContenido({ item }) {
    const enlace = normalizarEnlace(item.enlace);

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

    if (!enlace) {
        return <article className="feed-contenido-card tmdb-panel">{contenido}</article>;
    }

    if (enlace.startsWith("http")) {
        return (
            <a
                className="feed-contenido-card tmdb-panel"
                href={enlace}
                target="_blank"
                rel="noreferrer"
            >
                {contenido}
            </a>
        );
    }

    return (
        <Link className="feed-contenido-card tmdb-panel" to={enlace}>
            {contenido}
        </Link>
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
                            <TarjetaContenido key={item.idItem} item={item} />
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