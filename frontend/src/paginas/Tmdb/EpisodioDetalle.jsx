import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesEpisodio } from "../../servicios/ServicioTmdb.js";
import { profileUrl, stillUrl } from "../../utils/tmdbImages.js";
import "./estilos/detalleTmdb.css";

function formatearFecha(fecha) {
    if (!fecha) return "Fecha no disponible";

    const d = new Date(`${fecha}T00:00:00`);
    if (Number.isNaN(d.getTime())) return fecha;

    return d.toLocaleDateString("es-ES", {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}

export default function EpisodioDetalle() {
    const { id, temporada, episodio } = useParams();
    const [episodioData, setEpisodioData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setErr("");

            try {
                const data = await DarDetallesEpisodio(id, temporada, episodio);
                if (!cancelled) setEpisodioData(data);
            } catch (e) {
                if (!cancelled) setErr(e.message || "No se pudo cargar el episodio.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, [id, temporada, episodio]);

    if (loading) return <div className="tmdb-cargando">Cargando episodio...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!episodioData) return <div className="tmdb-vacio">No se ha encontrado el episodio.</div>;

    const imagen = stillUrl(episodioData.stillPath);

    return (
        <section className="tmdb-detalle">
            <Link to={`/series/${id}/temporadas/${temporada}`} className="tmdb-volver">
                ← Volver a la temporada
            </Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-still">
                    {imagen ? (
                        <img src={imagen} alt={episodioData.name} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin imagen</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{episodioData.name}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {formatearFecha(episodioData.airDate)}
                                </span>

                            </div>
                        </div>

                        <div className="tmdb-datos-grid tmdb-datos-grid-compacta">
                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Temporada</span>
                                <strong className="tmdb-valor">
                                    {episodioData.seasonNumber ?? "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Episodio</span>
                                <strong className="tmdb-valor">
                                    {episodioData.episodeNumber ?? "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Duración</span>
                                <strong className="tmdb-valor">
                                    {episodioData.runtime ? `${episodioData.runtime} min` : "No disponible"}
                                </strong>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Sinopsis</h2>
                            <p>{episodioData.overview || "No hay sinopsis disponible."}</p>
                        </div>

                        {episodioData.cast?.length > 0 && (
                            <div className="tmdb-resumen-acciones">
                                <a className="tmdb-boton-primario" href="#reparto">
                                    Ver reparto
                                </a>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <section id="reparto" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Reparto principal</h2>
                    <p>Actores destacados del episodio</p>
                </div>

                {episodioData.cast?.length ? (
                    <div className="tmdb-reparto">
                        {episodioData.cast.map((actor) => {
                            const foto = profileUrl(actor.profilePath);

                            return (
                                <article key={actor.id} className="tmdb-actor">
                                    {foto ? (
                                        <img src={foto} alt={actor.name} />
                                    ) : (
                                        <div className="tmdb-actor-placeholder">Sin foto</div>
                                    )}

                                    <div className="tmdb-actor-info">
                                        <strong>{actor.name}</strong>
                                        <span>{actor.character || "Personaje no disponible"}</span>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                ) : (
                    <div className="tmdb-vacio tmdb-vacio-interno">
                        No hay actores disponibles para este episodio.
                    </div>
                )}
            </section>
        </section>
    );
}