import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesSeries } from "../../servicios/ServicioTmdb.js";
import { logoUrl, profileUrl } from "../../utils/tmdbImages.js";
import { posterUrl } from "../../utils/img.js";
import "./estilos/detalleTmdb.css";
import MediaGrid from "../../componentes/tmdb/ListaProyecto.jsx";

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
        return () => {
            cancelled = true;
        };
    }, [id]);

    const poster = useMemo(() => posterUrl(serie?.posterPath), [serie]);
    const temporadasDisponibles = serie?.seasons ?? [];
    const reparto = serie?.cast ?? [];
    const recomendacionesFormateadas = serie?.recommendations ?? [];
    const stream = serie?.providers?.stream ?? [];
    const rent = serie?.providers?.rent ?? [];
    const buy = serie?.providers?.buy ?? [];
    const totalProviders = stream.length + rent.length + buy.length;

    if (loading) return <div className="tmdb-cargando">Cargando ficha de la serie...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!serie) return <div className="tmdb-vacio">No se ha encontrado la serie.</div>;

    return (
        <section className="tmdb-detalle">
            <Link to="/?tipo=series" className="tmdb-volver">← Volver al listado</Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-poster">
                    {poster ? (
                        <img src={poster} alt={serie.name} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin imagen</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{serie.name}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {formatearFecha(serie.firstAirDate)}
                                </span>
                                <span className="tmdb-meta-chip">
                                    {serie.status || "No disponible"}
                                </span>
                            </div>
                        </div>

                        <div className="tmdb-datos-grid tmdb-datos-grid-compacta">
                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Creador</span>
                                <strong className="tmdb-valor">{serie.creator || "No disponible"}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">País de origen</span>
                                <strong className="tmdb-valor">{serie.paisOrigen || "No disponible"}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Temporadas</span>
                                <strong className="tmdb-valor">
                                    {serie.numberOfSeasons ?? "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Episodios</span>
                                <strong className="tmdb-valor">
                                    {serie.numberOfEpisodes ?? "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato tmdb-dato-ancho">
                                <span className="tmdb-dato-label">Géneros</span>
                                <strong className="tmdb-valor">
                                    {serie.genres?.length ? serie.genres.join(", ") : "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Disponibilidad</span>
                                <strong className="tmdb-valor">
                                    {totalProviders > 0 ? `${totalProviders} plataformas` : "No disponible"}
                                </strong>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Sinopsis</h2>
                            <p>{serie.overview || "No hay sinopsis disponible."}</p>
                        </div>

                        <div className="tmdb-resumen-acciones">
                            {temporadasDisponibles.length > 0 && (
                                <a className="tmdb-boton-primario" href="#temporadas">
                                    Ver temporadas
                                </a>
                            )}

                            {totalProviders > 0 && (
                                <a className="tmdb-boton-secundario" href="#donde-ver">
                                    Dónde verla
                                </a>
                            )}

                            {reparto.length > 0 && (
                                <a className="tmdb-boton-secundario" href="#reparto">
                                    Ver reparto
                                </a>
                            )}

                            {recomendacionesFormateadas.length > 0 && (
                                <a className="tmdb-boton-secundario" href="#relacionadas">
                                    Relacionadas
                                </a>
                            )}
                        </div>
                    </div>
                </div>
            </header>

            <section id="temporadas" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Temporadas</h2>
                    <p>Listado de temporadas disponibles de la serie</p>
                </div>

                {temporadasDisponibles.length ? (
                    <div className="tmdb-grid tmdb-grid-temporadas">
                        {temporadasDisponibles.map((temporada, index) => {
                            const posterTemporada = posterUrl(temporada.posterPath);

                            return (
                                <Link
                                    key={temporada.id ?? `temporada-${index}`}
                                    className="tmdb-card tmdb-card-temporada"
                                    to={`/series/${serie.id}/temporadas/${temporada.seasonNumber}`}
                                >
                                    {posterTemporada ? (
                                        <img
                                            className="tmdb-card-poster tmdb-card-poster-temporada"
                                            src={posterTemporada}
                                            alt={temporada.name}
                                        />
                                    ) : (
                                        <div className="tmdb-card-poster-placeholder tmdb-card-poster-temporada-placeholder">
                                            Sin imagen
                                        </div>
                                    )}

                                    <div className="tmdb-card-body">
                                        <h3>{temporada.name}</h3>
                                        <p>Temporada {temporada.seasonNumber}</p>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                ) : (
                    <div className="tmdb-vacio tmdb-vacio-interno">No hay temporadas disponibles.</div>
                )}
            </section>

            <section id="donde-ver" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Dónde verla</h2>
                    <p>Disponibilidad actual en España</p>
                </div>

                {!stream.length && !rent.length && !buy.length ? (
                    <div className="tmdb-vacio tmdb-vacio-interno">
                        No hay proveedores disponibles en España.
                    </div>
                ) : (
                    <div className="tmdb-provider-bloques">
                        <div className="tmdb-provider-grupo">
                            <h3>Suscripción</h3>
                            {stream.length ? (
                                <div className="tmdb-provider-list">
                                    {stream.map((p, index) => (
                                        <div
                                            key={`stream-${p.id ?? p.name ?? index}`}
                                            className="tmdb-provider-item"
                                        >
                                            {p.logoPath ? (
                                                <img src={logoUrl(p.logoPath)} alt={p.name} />
                                            ) : (
                                                <div className="tmdb-provider-logo-placeholder" />
                                            )}
                                            <span>{p.name}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="tmdb-provider-empty">No disponible.</p>
                            )}
                        </div>

                        <div className="tmdb-provider-grupo">
                            <h3>Alquiler</h3>
                            {rent.length ? (
                                <div className="tmdb-provider-list">
                                    {rent.map((p, index) => (
                                        <div
                                            key={`rent-${p.id ?? p.name ?? index}`}
                                            className="tmdb-provider-item"
                                        >
                                            {p.logoPath ? (
                                                <img src={logoUrl(p.logoPath)} alt={p.name} />
                                            ) : (
                                                <div className="tmdb-provider-logo-placeholder" />
                                            )}
                                            <span>{p.name}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="tmdb-provider-empty">No disponible.</p>
                            )}
                        </div>

                        <div className="tmdb-provider-grupo">
                            <h3>Compra</h3>
                            {buy.length ? (
                                <div className="tmdb-provider-list">
                                    {buy.map((p, index) => (
                                        <div
                                            key={`buy-${p.id ?? p.name ?? index}`}
                                            className="tmdb-provider-item"
                                        >
                                            {p.logoPath ? (
                                                <img src={logoUrl(p.logoPath)} alt={p.name} />
                                            ) : (
                                                <div className="tmdb-provider-logo-placeholder" />
                                            )}
                                            <span>{p.name}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="tmdb-provider-empty">No disponible.</p>
                            )}
                        </div>
                    </div>
                )}
            </section>

            <section id="reparto" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Reparto principal</h2>
                    <p>Actores destacados de la serie</p>
                </div>

                {reparto.length ? (
                    <div className="tmdb-reparto">
                        {reparto.map((actor, index) => {
                            const foto = profileUrl(actor.profilePath);

                            return (
                                <article key={actor.id ?? `actor-${index}`} className="tmdb-actor">
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
                    <div className="tmdb-vacio tmdb-vacio-interno">No hay reparto disponible.</div>
                )}
            </section>

            <section id="relacionadas" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Series relacionadas</h2>
                    <p>Otras series que pueden interesarte</p>
                </div>

                {recomendacionesFormateadas.length ? (
                    <MediaGrid items={recomendacionesFormateadas} type="tv" />
                ) : (
                    <div className="tmdb-vacio tmdb-vacio-interno">No hay recomendaciones disponibles.</div>
                )}
            </section>
        </section>
    );
}