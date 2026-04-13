import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DarDetallesPeliculas } from "../../servicios/ServicioTmdb.js";
import { logoUrl, profileUrl } from "../../utils/tmdbImagenes.js";
import { posterUrl } from "../../utils/img.js";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import { crearContenidoListaPelicula } from "../../utils/contenidoLista.js";
import ModalAniadirALista from "../../componentes/listas/ModalAniadirALista.jsx";
import MediaGrid from "../../componentes/tmdb/ListaProyecto.jsx";
import "./estilos/detalleTmdb.css";

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

function formatearDinero(valor) {
    if (valor == null || valor <= 0) return "No disponible";

    return new Intl.NumberFormat("es-ES", {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 0,
    }).format(valor);
}

export default function PeliculaDetalle() {
    const { id } = useParams();
    const { user } = useAuth();

    const [pelicula, setPelicula] = useState(null);
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
                const data = await DarDetallesPeliculas(id);
                if (!cancelled) setPelicula(data);
            } catch (e) {
                if (!cancelled) setErr(e.message || "No se pudo cargar la película.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, [id]);

    const poster = useMemo(() => posterUrl(pelicula?.posterPath), [pelicula]);
    const contenidoLista = useMemo(
        () => crearContenidoListaPelicula(pelicula, poster),
        [pelicula, poster]
    );

    const stream = pelicula?.providers?.stream ?? [];
    const rent = pelicula?.providers?.rent ?? [];
    const buy = pelicula?.providers?.buy ?? [];
    const totalProviders = stream.length + rent.length + buy.length;
    const recomendacionesFormateadas = pelicula?.recommendations ?? [];

    const ingresosSuperanPresupuesto =
        pelicula?.budget > 0 &&
        pelicula?.revenue > 0 &&
        pelicula.revenue > pelicula.budget * 2.5;

    const claseIngresos = pelicula?.revenue > 0
        ? ingresosSuperanPresupuesto
            ? "tmdb-valor tmdb-valor-exito"
            : "tmdb-valor tmdb-valor-alerta"
        : "tmdb-valor";

    if (loading) return <div className="tmdb-cargando">Cargando ficha de la película...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!pelicula) return <div className="tmdb-vacio">No se ha encontrado la película.</div>;

    return (
        <section className="tmdb-detalle">
            <Link to="/" className="tmdb-volver">← Volver al listado</Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-poster">
                    {poster ? (
                        <img src={poster} alt={pelicula.title} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin imagen</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{pelicula.title}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {formatearFecha(pelicula.releaseDate)}
                                </span>

                                <span className="tmdb-meta-chip">
                                    {pelicula.runtime ? `${pelicula.runtime} min` : "Duración no disponible"}
                                </span>

                                <span className="tmdb-meta-chip">
                                    {pelicula.estrenadaEnCines ? "Estrenada" : "No estrenada"}
                                </span>
                            </div>
                        </div>

                        <div className="tmdb-datos-grid tmdb-datos-grid-compacta">
                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Dirección</span>
                                <strong className="tmdb-valor">{pelicula.director || "No disponible"}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Guion</span>
                                <strong className="tmdb-valor">{pelicula.guionista || "No disponible"}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">País de origen</span>
                                <strong className="tmdb-valor">{pelicula.paisOrigen || "No disponible"}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Disponibilidad</span>
                                <strong className="tmdb-valor">
                                    {totalProviders > 0 ? `${totalProviders} plataformas` : "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Presupuesto</span>
                                <strong className="tmdb-valor">{formatearDinero(pelicula.budget)}</strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Ingresos</span>
                                <strong className={claseIngresos}>{formatearDinero(pelicula.revenue)}</strong>
                            </div>

                            <div className="tmdb-dato tmdb-dato-ancho">
                                <span className="tmdb-dato-label">Géneros</span>
                                <strong className="tmdb-valor">
                                    {pelicula.genres?.length
                                        ? pelicula.genres.join(", ")
                                        : "No disponible"}
                                </strong>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Sinopsis</h2>
                            <p>{pelicula.overview || "No hay sinopsis disponible."}</p>
                        </div>

                        <div className="tmdb-resumen-acciones">
                            {pelicula.trailer?.key && (
                                <a className="tmdb-boton-primario" href="#trailer">
                                    Ver tráiler
                                </a>
                            )}


                            {totalProviders > 0 && (
                                <a className="tmdb-boton-secundario" href="#donde-ver">
                                    Dónde verla
                                </a>
                            )}

                            {pelicula.cast?.length > 0 && (
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

            <section className="tmdb-destacados">
                <div id="trailer" className="tmdb-panel tmdb-panel-trailer">
                    <div className="tmdb-seccion-cabecera">
                        <h2>Tráiler</h2>
                        <p>Vista previa principal de la película</p>
                    </div>

                    {pelicula.trailer?.key ? (
                        <div className="tmdb-trailer">
                            <iframe
                                src={`https://www.youtube.com/embed/${pelicula.trailer.key}`}
                                title={pelicula.trailer.name || `Tráiler de ${pelicula.title}`}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowFullScreen
                            />
                        </div>
                    ) : (
                        <div className="tmdb-vacio tmdb-vacio-interno">No hay tráiler disponible.</div>
                    )}
                </div>

                <aside id="donde-ver" className="tmdb-panel tmdb-panel-providers">
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
                </aside>
            </section>

            <section id="reparto" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Reparto principal</h2>
                    <p>Actores destacados de la película</p>
                </div>

                {pelicula.cast?.length ? (
                    <div className="tmdb-reparto">
                        {pelicula.cast.map((actor, index) => {
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
                    <h2>Recomendaciones</h2>
                    <p>Películas relacionadas que pueden interesarte</p>
                </div>

                {recomendacionesFormateadas.length ? (
                    <MediaGrid items={recomendacionesFormateadas} type="movie" />
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