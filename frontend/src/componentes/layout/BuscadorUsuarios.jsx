import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { buscarUsuarios } from "../../servicios/ServicioAutenticacion.js";
import "./estilos/BuscadorUsuarios.css";

export default function BuscadorUsuarios() {
    const navigate = useNavigate();
    const [query, setQuery] = useState("");
    const [resultados, setResultados] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [abierto, setAbierto] = useState(false);
    const contenedorRef = useRef(null);
    const timeoutRef = useRef(null);

    // Cerrar al hacer clic fuera
    useEffect(() => {
        function clicFuera(e) {
            if (contenedorRef.current && !contenedorRef.current.contains(e.target)) {
                setAbierto(false);
            }
        }
        document.addEventListener("mousedown", clicFuera);
        return () => document.removeEventListener("mousedown", clicFuera);
    }, []);

    // Búsqueda en tiempo real (debounced)
    useEffect(() => {
        if (timeoutRef.current) clearTimeout(timeoutRef.current);

        if (!query.trim() || query.length < 2) {
            setResultados([]);
            setAbierto(false);
            return;
        }

        timeoutRef.current = setTimeout(async () => {
            setCargando(true);
            try {
                const data = await buscarUsuarios(query);
                setResultados(data || []);
                setAbierto(true);
            } catch (err) {
                console.error("Error buscando usuarios:", err);
            } finally {
                setCargando(false);
            }
        }, 300);

        return () => clearTimeout(timeoutRef.current);
    }, [query]);

    function irAPerfil(id) {
        setAbierto(false);
        setQuery("");
        navigate(`/usuarios/${id}/feed`);
    }

    return (
        <div className="pg-search-users" ref={contenedorRef}>
            <div className="pg-search-users__input-wrapper">
                <input
                    type="text"
                    className="pg-search-users__input"
                    placeholder="Buscar usuarios..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onFocus={() => query.length >= 2 && setAbierto(true)}
                />
                {cargando && <div className="pg-search-users__loader" />}
            </div>

            {abierto && (
                <div className="pg-search-users__dropdown">
                    {resultados.length > 0 ? (
                        resultados.map((u) => (
                            <div
                                key={u.id}
                                className="pg-search-users__item"
                                onClick={() => irAPerfil(u.id)}
                            >
                                {u.fotoPerfil ? (
                                    <img src={u.fotoPerfil} alt={u.nombre} />
                                ) : (
                                    <div className="pg-search-users__avatar-fallback">
                                        {(u.nombre || "?").charAt(0).toUpperCase()}
                                    </div>
                                )}
                                <span>{u.nombre}</span>
                            </div>
                        ))
                    ) : (
                        <div className="pg-search-users__no-results">
                            No se han encontrado usuarios
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
