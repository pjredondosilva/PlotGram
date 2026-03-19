import { useEffect, useState } from "react";
import "../estilos/home.css";
import BotonPeliculaSerie from "../../componentes/tmdb/BotonPeliculaSerie.jsx";
import BarraDeBusqueda from "../../componentes/tmdb/BarraDeBusqueda";
import ListaProyecto from "../../componentes/tmdb/ListaProyecto.jsx";
import { getMovies, getSeries } from "../../servicios/ServicioTmdb.js";

export default function Home() {
    const [type, setType] = useState("movie");
    const [query, setQuery] = useState("");
    const [page, setPage] = useState(1);

    const [items, setItems] = useState([]);
    const [totalPages, setTotalPages] = useState(1);

    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    useEffect(() => {
        const q = query.trim();
        let cancelled = false;

        async function load() {
            setLoading(true);
            setErr("");

            try {
                const data = type === "movie"
                    ? await getMovies(q, page)
                    : await getSeries(q, page);

                if (!cancelled) {
                    const results = Array.isArray(data)
                        ? data
                        : Array.isArray(data?.results)
                            ? data.results
                            : [];

                    const total = Number.isInteger(data?.totalPages) ? data.totalPages : 1;

                    setItems(results);
                    setTotalPages(Math.max(1, total));

                    if (page > Math.max(1, total)) {
                        setPage(Math.max(1, total));
                    }
                }
            } catch (e) {
                if (!cancelled) {
                    setErr(e.message || "Error");
                    setItems([]);
                    setTotalPages(1);
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [type, query, page]);

    return (
        <div className="home">
            <div className="toolbar">
                <BotonPeliculaSerie
                    value={type}
                    onChange={(t) => {
                        setType(t);
                        setPage(1);
                    }}
                />

                <BarraDeBusqueda
                    value={query}
                    onChange={(v) => {
                        setQuery(v);
                        setPage(1);
                    }}
                    placeholder={type === "movie" ? "Buscar película..." : "Buscar serie..."}
                />
            </div>

            <div className="content">
                {loading && <div className="msg">Cargando...</div>}
                {err && <div className="msg error">{err}</div>}
                {!loading && !err && <ListaProyecto items={items} type={type} />}
            </div>

            <div className="pager">
                <button
                    disabled={page <= 1}
                    onClick={() => setPage(p => p - 1)}
                >
                    Anterior
                </button>

                <span>Página {page}</span>

                <button
                    disabled={page >= totalPages}
                    onClick={() => setPage(p => p + 1)}
                >
                    Siguiente
                </button>
            </div>
        </div>
    );
}