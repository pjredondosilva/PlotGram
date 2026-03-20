import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesTemporada } from "../../servicios/ServicioTmdb.js";
import { posterDetailUrl } from "../../utils/tmdbImages.js";
import "./estilos/detalleTmdb.css";

export default function TemporadaDetalle() {
    const { id, temporada } = useParams();
    const [temporadaData, setTemporadaData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setErr("");

            try {
                const data = await DarDetallesTemporada(id, temporada);
                if (!cancelled) setTemporadaData(data);
            } catch (e) {
                if (!cancelled) setErr(e.message || "No se pudo cargar la temporada.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [id, temporada]);

    if (loading) return <div className="tmdb-cargando">Cargando temporada...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!temporadaData) return <div className="tmdb-vacio">No se ha encontrado la temporada.</div>;

    const poster = posterDetailUrl(temporadaData.posterPath);

    return (
        <section className="tmdb-detalle">
            <Link to={`/series/${id}`} className="tmdb-volver">← Volver a la serie</Link>

            <div className="tmdb-hero">
                <div className="tmdb-hero-poster">
                    {poster
                        ? <img src={poster} alt={temporadaData.name} />
                        : <div className="tmdb-actor-placeholder">Sin imagen</div>}
                </div>

                <div className="tmdb-hero-contenido">
                    <h1>{temporadaData.name}</h1>
                    <div className="tmdb-subtitulo">
                        Temporada {temporadaData.seasonNumber} · {temporadaData.airDate || "Fecha no disponible"}
                    </div>

                    <div className="tmdb-badges">
                        {temporadaData.episodeCount && (
                            <span className="tmdb-badge">{temporadaData.episodeCount} episodios</span>
                        )}
                    </div>

                    <div className="tmdb-bloque">
                        <h2>Sinopsis</h2>
                        <p>{temporadaData.overview || "No hay sinopsis disponible."}</p>
                    </div>
                </div>
            </div>

            <div className="tmdb-bloque">
                <h2>Episodios</h2>
                {temporadaData.episodes?.length ? (
                    <div className="tmdb-grid">
                        {temporadaData.episodes.map((episodio) => (
                            <Link
                                key={episodio.id}
                                className="tmdb-card"
                                to={`/series/${id}/temporadas/${temporadaData.seasonNumber}/episodios/${episodio.episodeNumber}`}
                            >
                                <h4>Episodio {episodio.episodeNumber}: {episodio.name}</h4>
                                <p>{episodio.airDate || "Fecha no disponible"}</p>
                                <p>{episodio.overview || "Sin sinopsis disponible."}</p>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="tmdb-vacio">No hay episodios disponibles.</div>
                )}
            </div>
        </section>
    );
}
