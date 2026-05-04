import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../servicios/ContextoDeAutenticacion.jsx";
import {
    borrarLista,
    editarLista,
    eliminarElementoDeLista,
    obtenerDetalleListaDeUsuario,
} from "../../servicios/ServicioListas.js";
import FormularioLista from "../../componentes/listas/FormularioListas.jsx";
import TarjetaContenido from "../../componentes/listas/TarjetaContenido.jsx";
import ModalSistema from "../../componentes/Autenticacion/ModalSistema.jsx";
import "../Tmdb/estilos/detalleTmdb.css";
import "../usuario/estilos/feedUsuario.css";

export default function DetalleLista() {
    const { idUsuario, idLista } = useParams();
    const navigate = useNavigate();
    const { user, loadingMe } = useAuth();

    const [lista, setLista] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [modalEditarAbierto, setModalEditarAbierto] = useState(false);
    const [menuAbiertoId, setMenuAbiertoId] = useState(null);

    const esPropietario =
        user?.id != null && Number(user.id) === Number(idUsuario);

    const nombreAutor = lista?.nombreUsuario || (esPropietario ? user?.nombre : `Usuario ${idUsuario}`);

    const avatar = useMemo(() => {
        return (nombreAutor || "?").trim().charAt(0).toUpperCase();
    }, [nombreAutor]);

    const rutaVolver = `/usuarios/${idUsuario}/feed`;

    const estadoNavegacionContenido = useMemo(() => ({
        volverA: `/usuarios/${idUsuario}/feed/listas/${idLista}`,
        textoVolver: "Volver a la lista",
    }), [idUsuario, idLista]);

    useEffect(() => {
        cargarDetalle();
    }, [idUsuario, idLista]);

    async function cargarDetalle() {
        setLoading(true);
        setError("");

        try {
            const data = await obtenerDetalleListaDeUsuario(idUsuario, idLista);
            setLista(data);
        } catch (e) {
            setError(e.message || "No se pudo cargar la lista.");
        } finally {
            setLoading(false);
        }
    }

    async function manejarEditar(dto) {
        await editarLista(idLista, dto);
        setModalEditarAbierto(false);
        await cargarDetalle();
    }

    async function manejarBorrar() {
        const confirmado = window.confirm(
            `¿Seguro que quieres borrar la lista "${lista?.nombre}"?`
        );
        if (!confirmado) return;

        await borrarLista(idLista);
        navigate(`/usuarios/${idUsuario}/feed`);
    }

    async function manejarEliminarElemento(item) {
        const confirmado = window.confirm(
            `¿Seguro que quieres quitar "${item.titulo}" de esta lista?`
        );
        if (!confirmado) return;

        try {
            await eliminarElementoDeLista(idLista, item.idItem);

            setLista((prev) => ({
                ...prev,
                elementos: (prev?.elementos || []).filter(
                    (elemento) => elemento.idItem !== item.idItem
                ),
            }));
            setMenuAbiertoId(null);
        } catch (e) {
            window.alert(e.message || "No se pudo eliminar el elemento de la lista.");
        }
    }

    if (loading) {
        return <div className="tmdb-cargando">Cargando lista...</div>;
    }

    if (error) {
        return <div className="tmdb-error">{error}</div>;
    }

    if (!lista) {
        return <div className="tmdb-vacio">No se ha encontrado la lista.</div>;
    }

    return (
        <section className="tmdb-detalle feed-usuario">
            <Link to={rutaVolver} className="tmdb-volver">
                ← Volver
            </Link>

            <header className="tmdb-hero tmdb-panel">
                <div className="tmdb-hero-poster">
                    {lista.imagenPortada ? (
                        <img src={lista.imagenPortada} alt={lista.nombre} />
                    ) : (
                        <div className="tmdb-poster-placeholder">Sin portada</div>
                    )}
                </div>

                <div className="tmdb-hero-contenido">
                    <div className="tmdb-datos-panel tmdb-ficha-resumen">
                        <div className="tmdb-ficha-top">
                            <h1>{lista.nombre}</h1>

                            <div className="tmdb-meta-inline">
                                <span className="tmdb-meta-chip">
                                    {lista.elementos?.length ?? 0} elementos
                                </span>
                                <span className="tmdb-meta-chip">
                                    {esPropietario ? "Tu lista" : "Lista pública"}
                                </span>
                            </div>
                        </div>

                        <div className="feed-autor-lista">
                            <div className="feed-avatar feed-avatar-pequeno">{avatar}</div>
                            <div>
                                <strong>{nombreAutor}</strong>
                                <p>Creador de la lista</p>
                            </div>
                        </div>

                        <div className="tmdb-sinopsis tmdb-sinopsis-integrada">
                            <h2>Descripción</h2>
                            <p>{lista.descripcion || "Esta lista no tiene descripción."}</p>
                        </div>

                        {!loadingMe && esPropietario && (
                            <div className="tmdb-resumen-acciones">
                                <button
                                    type="button"
                                    className="tmdb-boton-primario"
                                    onClick={() => setModalEditarAbierto(true)}
                                >
                                    Editar lista
                                </button>

                                <button
                                    type="button"
                                    className="tmdb-boton-secundario feed-boton-peligro"
                                    onClick={manejarBorrar}
                                >
                                    Borrar lista
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <section className="tmdb-bloque feed-bloque">
                <div className="tmdb-seccion-cabecera">
                    <h2>Contenido de la lista</h2>
                    <p>Todos los elementos guardados dentro de esta colección.</p>
                </div>

                {lista.elementos?.length ? (
                    <div className="feed-contenido-grid">
                        {lista.elementos.map((item) => (
                            <TarjetaContenido
                                key={item.idItem}
                                item={item}
                                esPropietario={esPropietario}
                                menuAbierto={menuAbiertoId === item.idItem}
                                onAbrirMenu={setMenuAbiertoId}
                                onCerrarMenu={() => setMenuAbiertoId(null)}
                                onEliminar={manejarEliminarElemento}
                                estadoNavegacion={estadoNavegacionContenido}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="feed-vacio">
                        <h3>La lista todavía está vacía</h3>
                        <p>Cuando añadas películas, series, temporadas o episodios aparecerán aquí.</p>
                    </div>
                )}
            </section>

            <ModalSistema
                open={modalEditarAbierto}
                onClose={() => setModalEditarAbierto(false)}
            >
                {modalEditarAbierto && (
                    <FormularioLista
                        onClose={() => setModalEditarAbierto(false)}
                        onSubmit={manejarEditar}
                        titulo="Editar lista"
                        textoBoton="Guardar cambios"
                        valoresIniciales={lista}
                    />
                )}
            </ModalSistema>
        </section>
    );
}