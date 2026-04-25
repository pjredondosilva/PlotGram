import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import ModalSistema from "../Autenticacion/ModalSistema.jsx";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import { aniadirContenidoALista, obtenerMisListas } from "../../servicios/ServicioListas.js";
import "../../paginas/tmdb/estilos/detalleTmdb.css";

export default function ModalAniadirALista({ open, onClose, contenido, onAnadido }) {
    const { user } = useAuth();

    const [listas, setListas] = useState([]);
    const [listaSeleccionada, setListaSeleccionada] = useState(null);
    const [cargando, setCargando] = useState(false);
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState("");

    const rutaFeed = user?.id != null ? `/usuarios/${user.id}/feed` : "/";

    useEffect(() => {
        if (!open) {
            setError("");
            setListaSeleccionada(null);
            return;
        }

        let cancelado = false;

        async function cargarListas() {
            setCargando(true);
            setError("");

            try {
                const data = await obtenerMisListas();
                if (!cancelado) {
                    const listasCargadas = data ?? [];
                    setListas(listasCargadas);
                    setListaSeleccionada(listasCargadas.length ? listasCargadas[0].id : null);
                }
            } catch (e) {
                if (!cancelado) {
                    setError(e.message || "No se pudieron cargar tus listas.");
                }
            } finally {
                if (!cancelado) {
                    setCargando(false);
                }
            }
        }

        cargarListas();

        return () => {
            cancelado = true;
        };
    }, [open]);

    async function confirmar() {
        if (!listaSeleccionada || !contenido) return;

        setGuardando(true);
        setError("");

        try {
            await aniadirContenidoALista(listaSeleccionada, contenido);
            const lista = listas.find((item) => item.id === listaSeleccionada) ?? null;
            onAnadido?.(lista);
            onClose();
        } catch (e) {
            setError(e.message || "No se pudo añadir el contenido a la lista.");
        } finally {
            setGuardando(false);
        }
    }

    return (
        <ModalSistema open={open} onClose={onClose}>
            <div className="tmdb-selector-lista-modal">
                <div className="tmdb-selector-lista-cabecera">
                    <h2>Añadir a una lista</h2>
                    <p>
                        Selecciona una de tus listas para guardar este contenido.
                    </p>
                </div>

                {cargando ? (
                    <div className="tmdb-cargando tmdb-vacio-interno">Cargando tus listas...</div>
                ) : !listas.length ? (
                    <div className="tmdb-selector-lista-vacio">
                        <h3>Aún no tienes listas creadas</h3>
                        <p>Crea una lista en tu feed para poder guardar películas, series, temporadas o episodios.</p>
                        <Link className="tmdb-boton-primario" to={rutaFeed} onClick={onClose}>
                            Ir a mi feed
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="tmdb-selector-lista-grid">
                            {listas.map((lista) => (
                                <button
                                    key={lista.id}
                                    type="button"
                                    className={`tmdb-selector-lista-item ${listaSeleccionada === lista.id ? "is-activa" : ""}`}
                                    onClick={() => setListaSeleccionada(lista.id)}
                                >
                                    <div className="tmdb-selector-lista-portada">
                                        {lista.imagenPortada ? (
                                            <img src={lista.imagenPortada} alt={lista.nombre} />
                                        ) : (
                                            <div className="tmdb-selector-lista-portada-vacia">Sin portada</div>
                                        )}
                                    </div>

                                    <div className="tmdb-selector-lista-contenido">
                                        <div className="tmdb-selector-lista-meta">
                                            <span>{lista.numeroElementos} elementos</span>
                                        </div>
                                        <h3>{lista.nombre}</h3>
                                        <p>{lista.descripcion || "Sin descripción."}</p>
                                    </div>

                                    <div className="tmdb-selector-lista-check">
                                        {listaSeleccionada === lista.id ? "Seleccionada" : "Seleccionar"}
                                    </div>
                                </button>
                            ))}
                        </div>

                        {error && <div className="tmdb-error tmdb-vacio-interno">{error}</div>}

                        <div className="tmdb-selector-lista-acciones">
                            <button
                                type="button"
                                className="tmdb-boton-secundario"
                                onClick={onClose}
                            >
                                Cancelar
                            </button>

                            <button
                                type="button"
                                className="tmdb-boton-primario"
                                onClick={confirmar}
                                disabled={guardando || !listaSeleccionada}
                            >
                                {guardando ? "Añadiendo..." : "Añadir a la lista"}
                            </button>
                        </div>
                    </>
                )}
            </div>
        </ModalSistema>
    );
}