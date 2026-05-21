import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import ModalSistema from "../autenticacion/ModalSistema.jsx";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import { listarValoraciones, valorarContenido } from "../../servicios/ServicioValoraciones.js";
import "./estilos/ModalValoraciones.css";

export default function ModalValoraciones({ open, onClose, tmdbId, tipo, contenidoMetadata, onNuevaValoracion }) {
    const { user } = useAuth();
    const [valoraciones, setValoraciones] = useState([]);
    const [loading, setLoading] = useState(false);
    const [puntuacion, setPuntuacion] = useState(0);
    const [hoverPuntuacion, setHoverPuntuacion] = useState(0);
    const [comentario, setComentario] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState("");
    const [tieneValoracionPropia, setTieneValoracionPropia] = useState(false);

    useEffect(() => {
        if (open) {
            cargarValoraciones();
            setPuntuacion(0);
            setComentario("");
            setError("");
        }
    }, [open, tmdbId, tipo]);

    async function cargarValoraciones() {
        setLoading(true);
        try {
            const data = await listarValoraciones(tipo, tmdbId);
            setValoraciones(data);
            
            // Comprobar si el usuario actual ya ha valorado
            const miVal = data.find(v => v.usuarioId === user?.id);
            if (miVal) {
                setTieneValoracionPropia(true);
                setPuntuacion(miVal.puntuacion);
                setComentario(miVal.comentario || "");
            } else {
                setTieneValoracionPropia(false);
            }
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();
        
        if (puntuacion === 0) {
            setError("No se ha introducido valoración");
            return;
        }

        setEnviando(true);
        setError("");
        try {
            const datos = {
                contenido: contenidoMetadata,
                puntuacion,
                comentario
            };
            await valorarContenido(datos);
            setPuntuacion(0);
            setComentario("");
            await cargarValoraciones();
            if (onNuevaValoracion) onNuevaValoracion();
        } catch (e) {
            setError(e.message || "No se pudo guardar la valoración.");
        } finally {
            setEnviando(false);
        }
    }

    if (!open) return null;

    return (
        <ModalSistema open={open} onClose={onClose} className="modal-valoraciones-ancho">
            <div className="tmdb-selector-lista-cabecera" style={{ padding: "1.5rem", borderBottom: "1px solid var(--border-color)" }}>
                <h2 style={{ color: "#fff", marginBottom: "0.5rem" }}>Reseñas y Valoraciones</h2>
                <p style={{ color: "var(--color-muted)" }}>Comparte tu opinión con la comunidad.</p>
            </div>

            <div className="modal-valoraciones-scroll-container">
                <div style={{ padding: "1.5rem" }}>
                    <form className="form-valoracion" onSubmit={handleSubmit}>
                        <h3>Tu opinión</h3>
                        <div className="estrellas-selector-wrapper">
                            <div className="estrellas-selector">
                                {[1, 2, 3, 4, 5].map((num) => (
                                    <button
                                        key={num}
                                        type="button"
                                        className={`estrella-btn ${(hoverPuntuacion || puntuacion) >= num ? "activa" : ""}`}
                                        onMouseEnter={() => setHoverPuntuacion(num)}
                                        onMouseLeave={() => setHoverPuntuacion(0)}
                                        onClick={() => setPuntuacion(num)}
                                        aria-label={`Valorar con ${num} estrellas`}
                                    >
                                        ⭐
                                    </button>
                                ))}
                            </div>
                            <span className="puntuacion-label">
                                {puntuacion > 0 ? `${puntuacion} de 5 estrellas` : "Selecciona una puntuación"}
                            </span>
                        </div>

                        <textarea
                            className="tmdb-textarea"
                            placeholder="Cuéntanos qué te ha parecido... (Opcional)"
                            value={comentario}
                            onChange={(e) => setComentario(e.target.value)}
                        />
                        
                        {error && <p className="valoracion-error-msg">{error}</p>}
                        
                        <div style={{ textAlign: "center", marginTop: error ? "0.5rem" : "0" }}>
                            <button
                                type="submit"
                                className="tmdb-boton-primario"
                                style={{ padding: "0.8rem 2.5rem", fontSize: "1rem" }}
                                disabled={enviando}
                            >
                                {enviando ? "Guardando..." : (tieneValoracionPropia ? "Editar mi reseña" : "Publicar reseña")}
                            </button>
                        </div>
                    </form>

                    <div className="lista-reseñas">
                        <h3 style={{ borderBottom: "1px solid #333", paddingBottom: "0.5rem", marginBottom: "1rem" }}>
                            Todas las reseñas
                        </h3>
                        
                        {loading ? (
                            <p className="tmdb-cargando">Cargando reseñas...</p>
                        ) : valoraciones.length === 0 ? (
                            <p style={{ color: "#999", textAlign: "center", padding: "2rem 0" }}>
                                No hay reseñas todavía. ¡Sé el primero en opinar!
                            </p>
                        ) : (
                            valoraciones.map((v) => (
                                <article key={v.id} className="reseña-item">
                                    <div className="reseña-header">
                                        {v.usuarioFoto ? (
                                            <img
                                                src={v.usuarioFoto}
                                                alt={v.usuarioNombre}
                                                className="reseña-avatar"
                                                onError={(e) => {
                                                    e.target.src = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y";
                                                }}
                                            />
                                        ) : (
                                            <div className="reseña-avatar-placeholder">
                                                {v.usuarioNombre.charAt(0).toUpperCase()}
                                            </div>
                                        )}
                                        <div className="reseña-usuario-info">
                                            <Link
                                                to={`/usuarios/${v.usuarioId}/feed`}
                                                className="reseña-usuario-nombre"
                                                onClick={onClose}
                                            >
                                                {v.usuarioNombre}
                                            </Link>
                                            <div className="reseña-estrellas">
                                                {"⭐".repeat(v.puntuacion)}
                                            </div>
                                        </div>
                                    </div>
                                    {v.comentario && (
                                        <p className="reseña-comentario">{v.comentario}</p>
                                    )}
                                    <div className="reseña-fecha">
                                        {new Date(v.fecha).toLocaleDateString("es-ES", {
                                            day: "numeric",
                                            month: "long",
                                            year: "numeric"
                                        })}
                                    </div>
                                </article>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </ModalSistema>
    );
}
