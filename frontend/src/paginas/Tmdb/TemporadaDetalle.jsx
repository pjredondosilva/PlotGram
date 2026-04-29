import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { DarDetallesTemporada } from "../../servicios/ServicioTmdb.js";
import { posterUrl } from "../../utils/img.js";
import { stillUrl } from "../../utils/tmdbImagenes.js";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import { crearContenidoListaTemporada } from "../../utils/contenidoLista.js";
import ModalAniadirALista from "../../componentes/listas/ModalAniadirALista.jsx";
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

function obtenerEstadoTemporada(fecha) {
    if (!fecha) return "No disponible";

    const estreno = new Date(`${fecha}T00:00:00`);
    if (Number.isNaN(estreno.getTime())) return "No disponible";

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    return estreno <= hoy ? "Estrenada" : "No estrenada";
}

export default function TemporadaDetalle() {
    const { id, temporada } = useParams();
    const location = useLocation();
    const { user } = useAuth();

    const [temporadaData, setTemporadaData] = useState(null);
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
                const data = await DarDetallesTemporada(id, temporada);
                if (!cancelled) setTemporadaData(data);
            } catch (e) {
                if (!cancelled) setErr(e.message || "No se pudo cargar la temporada.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, [id, temporada]);

    const poster = useMemo(() => posterUrl(temporadaData?.posterPath), [temporadaData]);
    const contenidoLista = useMemo(
        () => crearContenidoListaTemporada(id, temporadaData, poster),
        [id, temporadaData, poster]
    );

    const episodios = temporadaData?.episodes ?? [];
    const estado = obtenerEstadoTemporada(temporadaData?.airDate);
    const rutaVolver = location.state?.volverA || `/series/${id}`;
    const textoVolver = location.state?.textoVolver || "Volver a la serie";

    if (loading) return <div className="tmdb-cargando">Cargando temporada...</div>;
    if (err) return <div className="tmdb-error">{err}</div>;
    if (!temporadaData) return <div className="tmdb-vacio">No se ha encontrado la temporada.</div>;

    return (
        <section className="tmdb-detalle">
            <Link to={rutaVolver} className="tmdb-volver">← {textoVolver}</Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-poster">
                    {poster ? (
                        <img src={poster} alt={temporadaData.name} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin imagen</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{temporadaData.name || `Temporada ${temporadaData.seasonNumber}`}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {formatearFecha(temporadaData.airDate)}
                                </span>

                                <span className="tmdb-meta-chip">
                                    {estado}
                                </span>
                            </div>
                        </div>

                        <div className="tmdb-datos-grid tmdb-datos-grid-compacta">
                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Temporada</span>
                                <strong className="tmdb-valor">
                                    {temporadaData.seasonNumber ?? "No disponible"}
                                </strong>
                            </div>

                            <div className="tmdb-dato">
                                <span className="tmdb-dato-label">Episodios</span>
                                <strong className="tmdb-valor">
                                    {temporadaData.episodeCount ?? "No disponible"}
                                </strong>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Sinopsis</h2>
                            <p>{temporadaData.overview || "No hay sinopsis disponible."}</p>
                        </div>

                        <div className="tmdb-resumen-acciones">
                            {episodios.length > 0 && (
                                <a className="tmdb-boton-primario" href="#episodios">
                                    Ver episodios
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

            <section id="episodios" className="tmdb-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Episodios</h2>
                    <p>Listado de episodios de la temporada</p>
                </div>

                {episodios.length ? (
                    <div className="tmdb-grid tmdb-grid-episodios">
                        {episodios.map((episodio, index) => {
                            const still = stillUrl(episodio.stillPath);

                            return (
                                <Link
                                    key={episodio.id ?? `episodio-${index}`}
                                    className="tmdb-card tmdb-card-episodio"
                                    to={`/series/${id}/temporadas/${temporadaData.seasonNumber}/episodios/${episodio.episodeNumber}`}
                                    state={{
                                        volverA: `/series/${id}/temporadas/${temporadaData.seasonNumber}`,
                                        textoVolver: "Volver a la temporada",
                                    }}
                                >
                                    {still ? (
                                        <img
                                            className="tmdb-card-poster tmdb-card-poster-episodio"
                                            src={still}
                                            alt={episodio.name}
                                        />
                                    ) : (
                                        <div className="tmdb-card-poster-placeholder tmdb-card-poster-episodio-placeholder">
                                            Sin imagen
                                        </div>
                                    )}

                                    <div className="tmdb-card-body">
                                        <h3>Episodio {episodio.episodeNumber}: {episodio.name}</h3>
                                        <p>{formatearFecha(episodio.airDate)}</p>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                ) : (
                    <div className="tmdb-vacio tmdb-vacio-interno">No hay episodios disponibles.</div>
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