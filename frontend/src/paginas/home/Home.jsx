import "./estilos/home.css";
import BotonPeliculaSerie from "../../componentes/tmdb/BotonPeliculaSerie.jsx";
import BarraDeBusqueda from "../../componentes/tmdb/BarraDeBusqueda";
import ListaProyecto from "../../componentes/tmdb/ListaProyecto.jsx";
import {
    getMovieGenres,
    getMovies,
    getSeries,
    getSeriesGenres
} from "../../servicios/ServicioTmdb.js";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

export default function Home() {
    const [searchParams, setSearchParams] = useSearchParams();
    const tipoUrl = searchParams.get("tipo");

    const [type, setType] = useState(tipoUrl === "series" ? "tv" : "movie");
    const [query, setQuery] = useState("");
    const [page, setPage] = useState(1);

    const [items, setItems] = useState([]);
    const [totalPages, setTotalPages] = useState(1);
    const [totalResults, setTotalResults] = useState(0);

    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    const [filtrosAbiertos, setFiltrosAbiertos] = useState(false);
    const [fechaDesde, setFechaDesde] = useState("");
    const [fechaHasta, setFechaHasta] = useState("");
    const [generosSeleccionados, setGenerosSeleccionados] = useState([]);
    const [generosDisponibles, setGenerosDisponibles] = useState([]);

    const busquedaTextoActiva = query.trim().length > 0;
    const hayFiltrosActivos =
        Boolean(fechaDesde) || Boolean(fechaHasta) || generosSeleccionados.length > 0;

    useEffect(() => {
        const tipo = searchParams.get("tipo");
        setType(tipo === "series" ? "tv" : "movie");
        setPage(1);
        setFechaDesde("");
        setFechaHasta("");
        setGenerosSeleccionados([]);
        setFiltrosAbiertos(false);
    }, [searchParams]);

    useEffect(() => {
        let cancelled = false;

        async function cargarGeneros() {
            try {
                const data = type === "movie"
                    ? await getMovieGenres()
                    : await getSeriesGenres();

                if (!cancelled) {
                    setGenerosDisponibles(Array.isArray(data) ? data : []);
                }
            } catch {
                if (!cancelled) {
                    setGenerosDisponibles([]);
                }
            }
        }

        cargarGeneros();

        return () => {
            cancelled = true;
        };
    }, [type]);

    useEffect(() => {
        let cancelled = false;
        const q = query.trim();

        async function load() {
            setLoading(true);
            setErr("");

            try {
                const filtros = q
                    ? {}
                    : {
                        fechaDesde,
                        fechaHasta,
                        generos: generosSeleccionados,
                    };

                const data = type === "movie"
                    ? await getMovies(q, page, filtros)
                    : await getSeries(q, page, filtros);

                if (!cancelled) {
                    const results = Array.isArray(data)
                        ? data
                        : Array.isArray(data?.results)
                            ? data.results
                            : [];

                    const total = Number.isInteger(data?.totalPages) ? data.totalPages : 1;
                    const totalResultados = Number.isInteger(data?.totalResults) ? data.totalResults : results.length;

                    setItems(results);
                    setTotalPages(Math.max(1, total));
                    setTotalResults(Math.max(0, totalResultados));

                    if (page > Math.max(1, total)) {
                        setPage(Math.max(1, total));
                    }
                }
            } catch (e) {
                if (!cancelled) {
                    setErr(e.message || "Error");
                    setItems([]);
                    setTotalPages(1);
                    setTotalResults(0);
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();

        return () => {
            cancelled = true;
        };
    }, [type, query, page, fechaDesde, fechaHasta, generosSeleccionados]);

    function toggleGenero(genero) {
        setPage(1);
        setGenerosSeleccionados((prev) =>
            prev.includes(genero)
                ? prev.filter((g) => g !== genero)
                : [...prev, genero]
        );
    }

    function limpiarFiltros() {
        setFechaDesde("");
        setFechaHasta("");
        setGenerosSeleccionados([]);
        setPage(1);
    }

    function manejarCambioFechaDesde(valor) {
        setPage(1);

        if (!valor) {
            setFechaDesde("");
            return;
        }

        if (fechaHasta && valor > fechaHasta) {
            setFechaDesde(valor);
            setFechaHasta(valor);
            return;
        }

        setFechaDesde(valor);
    }

    function manejarCambioFechaHasta(valor) {
        setPage(1);

        if (!valor) {
            setFechaHasta("");
            return;
        }

        if (fechaDesde && valor < fechaDesde) {
            setFechaDesde(valor);
            setFechaHasta(valor);
            return;
        }

        setFechaHasta(valor);
    }

    function cambiarPagina(nuevaPagina) {
        setPage(nuevaPagina);
        window.requestAnimationFrame(() => {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    }

    return (
        <div className="home">
            <div className="toolbar">
                <BotonPeliculaSerie
                    value={type}
                    onChange={(t) => {
                        setType(t);
                        setPage(1);
                        setSearchParams(t === "tv" ? { tipo: "series" } : { tipo: "peliculas" });
                    }}
                />

                <BarraDeBusqueda
                    value={query}
                    onChange={(v) => {
                        setQuery(v);
                        setPage(1);

                        if (v.trim()) {
                            setFechaDesde("");
                            setFechaHasta("");
                            setGenerosSeleccionados([]);
                            setFiltrosAbiertos(false);
                        }
                    }}
                    placeholder={type === "movie" ? "Buscar película..." : "Buscar serie..."}
                    filtrosAbiertos={filtrosAbiertos}
                    onToggleFiltros={() => {
                        if (!busquedaTextoActiva) {
                            setFiltrosAbiertos((prev) => !prev);
                        }
                    }}
                    fechaDesde={fechaDesde}
                    fechaHasta={fechaHasta}
                    onChangeFechaDesde={manejarCambioFechaDesde}
                    onChangeFechaHasta={manejarCambioFechaHasta}
                    generosDisponibles={generosDisponibles}
                    generosSeleccionados={generosSeleccionados}
                    onToggleGenero={toggleGenero}
                    onLimpiarFiltros={limpiarFiltros}
                    filtrosDeshabilitados={busquedaTextoActiva}
                />
            </div>

            {hayFiltrosActivos && !loading && !err && !busquedaTextoActiva && (
                <div className="filters-summary">
                    {totalResults} resultados filtrados en total · página {page} de {totalPages}
                </div>
            )}

            <div className="content">
                {loading && <div className="msg">Cargando...</div>}
                {err && <div className="msg error">{err}</div>}
                {!loading && !err && items.length === 0 && (
                    <div className="msg">
                        {busquedaTextoActiva
                            ? "No se han encontrado resultados para esa búsqueda."
                            : "No hay resultados que cumplan los filtros seleccionados."}
                    </div>
                )}
                {!loading && !err && items.length > 0 && (
                    <ListaProyecto items={items} type={type} />
                )}
            </div>

            <div className="pager">
                <button
                    disabled={page <= 1}
                    onClick={() => cambiarPagina(page - 1)}
                >
                    Anterior
                </button>

                <span>Página {page}</span>

                <button
                    disabled={page >= totalPages}
                    onClick={() => cambiarPagina(page + 1)}
                >
                    Siguiente
                </button>
            </div>
        </div>
    );
}