import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "../../paginas/usuario/estilos/feedUsuario.css";
import "../../paginas/tmdb/estilos/detalleTmdb.css";

export default function TarjetaLista({
                                         lista,
                                         nombreUsuario,
                                         idUsuario,
                                         esPropietario,
                                         menuAbierto,
                                         onAbrirMenu,
                                         onCerrarMenu,
                                         onEditar,
                                         onBorrar,
                                     }) {
    const navigate = useNavigate();
    const menuRef = useRef(null);

    useEffect(() => {
        if (!menuAbierto) return;

        function cerrar(e) {
            if (!menuRef.current?.contains(e.target)) {
                onCerrarMenu();
            }
        }

        window.addEventListener("mousedown", cerrar);
        return () => window.removeEventListener("mousedown", cerrar);
    }, [menuAbierto, onCerrarMenu]);

    function abrirDetalle() {
        if (idUsuario == null) {
            window.alert("No se puede abrir la lista porque falta el id del usuario autenticado.");
            return;
        }

        navigate(`/usuarios/${idUsuario}/feed/listas/${lista.id}`);
    }

    return (
        <article className="feed-lista-card tmdb-panel">
            {esPropietario && (
                <div className="feed-lista-card-menu" ref={menuRef}>
                    <button
                        type="button"
                        className="feed-menu-boton"
                        onClick={() => (menuAbierto ? onCerrarMenu() : onAbrirMenu(lista.id))}
                        aria-label={`Abrir opciones de la lista ${lista.nombre}`}
                    >
                        ⋯
                    </button>

                    {menuAbierto && (
                        <div className="feed-menu-desplegable">
                            <button type="button" onClick={() => onEditar(lista)}>
                                Editar
                            </button>
                            <button
                                type="button"
                                className="feed-menu-opcion-peligro"
                                onClick={() => onBorrar(lista)}
                            >
                                Borrar
                            </button>
                        </div>
                    )}
                </div>
            )}

            <button
                type="button"
                className="feed-lista-enlace"
                onClick={abrirDetalle}
            >
                <div className="feed-lista-portada">
                    {lista.imagenPortada ? (
                        <img src={lista.imagenPortada} alt={lista.nombre} />
                    ) : (
                        <div className="feed-lista-portada-vacia">Sin portada</div>
                    )}
                </div>

                <div className="feed-lista-cuerpo">
                    <div className="feed-lista-meta">
                        <span className="feed-lista-autor">{nombreUsuario}</span>
                        <span className="feed-lista-separador">·</span>
                        <span>{lista.numeroElementos} elementos</span>
                    </div>

                    <h3>{lista.nombre}</h3>
                    <p>{lista.descripcion || "Sin descripción."}</p>
                </div>
            </button>
        </article>
    );
}