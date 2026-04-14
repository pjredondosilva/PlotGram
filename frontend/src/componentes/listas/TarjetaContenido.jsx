import { useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import "../../paginas/usuario/estilos/feedUsuario.css";
import "../../paginas/tmdb/estilos/detalleTmdb.css";

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

export default function TarjetaContenido({
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