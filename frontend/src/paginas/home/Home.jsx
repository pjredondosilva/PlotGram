import { useEffect, useMemo, useState } from "react";
import "../estilos/home.css";
import BotonPeliculaSerie from "../../componentes/tmdb/BotonPeliculaSerie.jsx";
import BarraDeBusqueda from "../../componentes/tmdb/BarraDeBusqueda";
import ListaProyecto from "../../componentes/tmdb/ListaProyecto.jsx";
import { getMovies, getSeries } from "../../servicios/ServicioTmdb.js";

export default function Home() {
    const [type, setType] = useState("movie"); // "movie" | "tv"
    const [query, setQuery] = useState("");
    const [page, setPage] = useState(1);

    const [items, setItems] = useState([]);
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
                    ? await getMovies(q,page)
                    : await getSeries(q, page);

                if (!cancelled) setItems(Array.isArray(data) ? data : []);
            } catch (e) {
                if (!cancelled) {
                    setErr(e.message || "Error");
                    setItems([]);
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
                    onChange={(t) => { setType(t); setPage(1); }}
                />

                <BarraDeBusqueda
                    value={query}
                    onChange={(v) => { setQuery(v); setPage(1); }}
                    placeholder={type === "movie" ? "Buscar película..." : "Buscar serie..."}
                />
            </div>

            <div className="content">
                {loading && <div className="msg">Cargando...</div>}
                {err && <div className="msg error">{err}</div>}
                {!loading && !err && <ListaProyecto items={items} type={type} />}
            </div>

            <div className="pager">
                <button disabled={page <= 1} onClick={() => setPage(p => p - 1)}>Anterior</button>
                <span>Página {page}</span>
                <button onClick={() => setPage(p => p + 1)}>Siguiente</button>
            </div>
        </div>
    );
}
