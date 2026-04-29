import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { DarDetallesSeries } from "../../servicios/ServicioTmdb.js";
import { logoUrl, profileUrl } from "../../utils/tmdbImagenes.js";
import { posterUrl } from "../../utils/img.js";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import { crearContenidoListaSerie } from "../../utils/contenidoLista.js";
import ModalAniadirALista from "../../componentes/listas/ModalAniadirALista.jsx";
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

const URLS_PROVEEDORES = {
    2: "https://tv.apple.com/es",
    3: "https://play.google.com/store/movies",
    8: "https://www.netflix.com/es",
    9: "https://www.primevideo.com",
    10: "https://www.amazon.es/gp/video/storefront",
    11: "https://mubi.com/es",
    35: "https://rakuten.tv/es",
    63: "https://www.filmin.es",
    68: "https://www.microsoft.com/es-es/store/movies-and-tv",
    119: "https://www.primevideo.com",
    149: "https://ver.movistarplus.es",
    283: "https://www.crunchyroll.com/es",
    337: "https://www.disneyplus.com/es-es",
    384: "https://www.max.com/es/es",
    393: "https://flixole.com",
    110: "https://www.dazn.com/es-ES/home",
    350: "https://tv.apple.com/es/channel/tvs.sbd.4000",
    1773: "https://www.skyshowtime.com/es",
    1796: "https://www.netflix.com/es",
    1899: "https://www.max.com/es/es",
    netflix: "https://www.netflix.com/es",
    "amazon prime video": "https://www.primevideo.com",
    "prime video": "https://www.primevideo.com",
    "disney plus": "https://www.disneyplus.com/es-es",
    "disney+": "https://www.disneyplus.com/es-es",
    max: "https://www.max.com/es/es",
    "hbo max": "https://www.max.com/es/es",
    filmin: "https://www.filmin.es",
    "apple tv": "https://tv.apple.com/es",
    "apple tv plus": "https://tv.apple.com/es/channel/tvs.sbd.4000",
    "apple tv+": "https://tv.apple.com/es/channel/tvs.sbd.4000",
    "rakuten tv": "https://rakuten.tv/es",
    "movistar plus": "https://ver.movistarplus.es",
    "movistar plus+": "https://ver.movistarplus.es",
    "google play movies": "https://play.google.com/store/movies",
    "microsoft store": "https://www.microsoft.com/es-es/store/movies-and-tv",
    mubi: "https://mubi.com/es",
    crunchyroll: "https://www.crunchyroll.com/es",
    skyshowtime: "https://www.skyshowtime.com/es",
    flixole: "https://flixole.com",
    dazn: "https://www.dazn.com/es-ES/home",
};

function normalizarProveedor(nombre) {
    return (nombre || "")
        .trim()
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

function obtenerUrlProveedor(proveedor) {
    if (proveedor?.url) return proveedor.url;
    if (proveedor?.link) return proveedor.link;

    const id = proveedor?.id != null ? String(proveedor.id) : null;
    const nombre = normalizarProveedor(proveedor?.name);

    return (id && URLS_PROVEEDORES[id])
        || URLS_PROVEEDORES[nombre]
        || `https://www.google.com/search?q=${encodeURIComponent(proveedor?.name || "plataforma streaming")}`;
}

function ProviderItem({ provider }) {
    const url = obtenerUrlProveedor(provider);

    return (
        <a
            className="tmdb-provider-item"
            href={url}
            target="_blank"
            rel="noreferrer"
            title={`Abrir ${provider.name}`}
            aria-label={`Abrir ${provider.name}`}
        >
            {provider.logoPath ? (
                <img src={logoUrl(provider.logoPath)} alt={provider.name} />
            ) : (
                <div className="tmdb-provider-logo-placeholder" />
            )}
            <span>{provider.name}</span>
        </a>
    );
}

export default function SerieDetalle() {
    const { id } = useParams();
    const location = useLocation();
    const { user } = useAuth();

    const [serie, setSerie] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [modalListaAbierto, setModalListaAbierto] = useState(false);
    const [mensajeLista, setMensajeLista] = useState("");

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
    const contenidoLista = useMemo(
        () => crearContenidoListaSerie(serie, poster),
        [serie, poster]
    );

    const temporadasDisponibles = serie?.seasons ?? [];
    const reparto = serie?.cast ?? [];
    const recomendacionesFormateadas = serie?.recommendations ?? [];
    const stream = serie?.providers?.stream ?? [];
    const rent = serie?.providers?.rent ?? [];
    const buy = serie?.providers?.buy ?? [];
    const totalProviders = stream.length + rent.length + buy.length;
    const rutaVolver = location.state?.volverA || "/?tipo=series";
    const textoVolver = location.state?.textoVolver || "Volver al listado";

    if (loading) return <div className="tmdb-cargando">Cargando ficha de la serie...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!serie) return <div className="tmdb-vacio">No se ha encontrado la serie.</div>;

    return (
        <section className="tmdb-detalle">
            <Link to={rutaVolver} className="tmdb-volver">← {textoVolver}</Link>

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

                            {user && contenidoLista && (
                                <button
                                    type="button"
                                    className="tmdb-boton-secundario"
                                    onClick={() => setModalListaAbierto(true)}
                                >
                                    Añadir a una lista
                                </button>
                            )}
                        </div>

                        {mensajeLista && (
                            <p className="tmdb-estado-lista">{mensajeLista}</p>
                        )}
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
                                    state={{
                                        volverA: `/series/${serie.id}`,
                                        textoVolver: "Volver a la serie",
                                    }}
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
                                        <ProviderItem
                                            key={`stream-${p.id ?? p.name ?? index}`}
                                            provider={p}
                                        />
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
                                        <ProviderItem
                                            key={`rent-${p.id ?? p.name ?? index}`}
                                            provider={p}
                                        />
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
                                        <ProviderItem
                                            key={`buy-${p.id ?? p.name ?? index}`}
                                            provider={p}
                                        />
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

            <ModalAniadirALista
                open={modalListaAbierto}
                onClose={() => setModalListaAbierto(false)}
                contenido={contenidoLista}
                onAnadido={(lista) => {
                    setMensajeLista(`Añadido a "${lista?.nombre ?? "la lista"}".`);
                }}
            />
        </section>
    );
}