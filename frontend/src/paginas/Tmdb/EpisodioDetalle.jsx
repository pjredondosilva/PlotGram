import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesEpisodio } from "../../servicios/ServicioTmdb.js";
import { profileUrl, stillUrl } from "../../utils/tmdbImages.js";
import "./estilos/detalleTmdb.css";

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
        return () => { cancelled = true; };
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

            <div className="tmdb-hero">
                <div className="tmdb-hero-still">
                    {imagen
                        ? <img src={imagen} alt={episodioData.name} />
                        : <div className="tmdb-actor-placeholder">Sin imagen</div>}
                </div>

                <div className="tmdb-hero-contenido">
                    <h1>{episodioData.name}</h1>
                    <div className="tmdb-subtitulo">
                        Temporada {episodioData.seasonNumber} · Episodio {episodioData.episodeNumber}
                    </div>

                    <div className="tmdb-badges">
                        {episodioData.airDate && <span className="tmdb-badge">{episodioData.airDate}</span>}
                        {episodioData.runtime && <span className="tmdb-badge">{episodioData.runtime} min</span>}
                        {episodioData.voteAverage && (
                            <span className="tmdb-badge">Nota: {episodioData.voteAverage.toFixed(1)}</span>
                        )}
                    </div>

                    <div className="tmdb-bloque">
                        <h2>Sinopsis</h2>
                        <p>{episodioData.overview || "No hay sinopsis disponible."}</p>
                    </div>
                </div>
            </div>

            <div className="tmdb-bloque">
                <h2>Actores</h2>
                {episodioData.cast?.length ? (
                    <div className="tmdb-reparto">
                        {episodioData.cast.map((actor) => {
                            const foto = profileUrl(actor.profilePath);
                            return (
                                <article key={actor.id} className="tmdb-actor">
                                    {foto
                                        ? <img src={foto} alt={actor.name} />
                                        : <div className="tmdb-actor-placeholder" />}
                                    <div className="tmdb-actor-info">
                                        <strong>{actor.name}</strong>
                                        <span>{actor.character || "Personaje no disponible"}</span>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                ) : (
                    <div className="tmdb-vacio">No hay actores disponibles para este episodio.</div>
                )}
            </div>
        </section>
    );
}
