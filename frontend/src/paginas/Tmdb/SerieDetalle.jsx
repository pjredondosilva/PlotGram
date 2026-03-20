import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesSeries } from "../../servicios/ServicioTmdb.js";
import { posterDetailUrl, profileUrl } from "../../utils/tmdbImages.js";
import "./estilos/detalleTmdb.css";

export default function SerieDetalle() {
    const { id } = useParams();
    const [serie, setSerie] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setErr("");

            try {
                const data = await DarDetallesSeries(id);
                if (!cancelled) setSerie(data);
            } catch (e) {
                if (!cancelled) setErr(e.message || "No se pudo cargar la serie.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [id]);

    if (loading) return <div className="tmdb-cargando">Cargando ficha de la serie...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!serie) return <div className="tmdb-vacio">No se ha encontrado la serie.</div>;

    const poster = posterDetailUrl(serie.posterPath);

    return (
        <section className="tmdb-detalle">
            <Link to="/" className="tmdb-volver">← Volver al listado</Link>

            <div className="tmdb-hero">
                <div className="tmdb-hero-poster">
                    {poster
                        ? <img src={poster} alt={serie.name} />
                        : <div className="tmdb-actor-placeholder">Sin imagen</div>}
                </div>

                <div className="tmdb-hero-contenido">
                    <h1>{serie.name}</h1>
                    <div className="tmdb-subtitulo">{serie.firstAirDate || "Fecha no disponible"}</div>

                    <div className="tmdb-badges">
                        {serie.numberOfSeasons && <span className="tmdb-badge">{serie.numberOfSeasons} temporadas</span>}
                        {serie.numberOfEpisodes && <span className="tmdb-badge">{serie.numberOfEpisodes} episodios</span>}
                        {serie.voteAverage && <span className="tmdb-badge">Nota: {serie.voteAverage.toFixed(1)}</span>}
                        {serie.genres?.map((g) => <span key={g} className="tmdb-badge">{g}</span>)}
                    </div>

                    <div className="tmdb-bloque">
                        <h2>Sinopsis</h2>
                        <p>{serie.overview || "No hay sinopsis disponible."}</p>
                    </div>
                </div>
            </div>

            <div className="tmdb-bloque">
                <h2>Temporadas</h2>
                {serie.seasons?.length ? (
                    <div className="tmdb-grid">
                        {serie.seasons.map((temporada) => (
                            <Link
                                key={temporada.id}
                                className="tmdb-card"
                                to={`/series/${serie.id}/temporadas/${temporada.seasonNumber}`}
                            >
                                <h3>{temporada.name}</h3>
                                <p>Temporada {temporada.seasonNumber}</p>
                                <p>{temporada.episodeCount || 0} episodios</p>
                                <p>{temporada.airDate || "Fecha no disponible"}</p>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="tmdb-vacio">No hay temporadas disponibles.</div>
                )}
            </div>

            <div className="tmdb-bloque">
                <h2>Reparto principal</h2>
                {serie.cast?.length ? (
                    <div className="tmdb-reparto">
                        {serie.cast.map((actor) => {
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
                    <div className="tmdb-vacio">No hay reparto disponible.</div>
                )}
            </div>
        </section>
    );
}
