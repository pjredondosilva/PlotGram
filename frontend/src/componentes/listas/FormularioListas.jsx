import { useEffect, useMemo, useState } from "react";
import "../../paginas/usuario/estilos/feedUsuario.css";

const VACIO = {
    nombre: "",
    descripcion: "",
    imagenPortada: "",
};

function construirFormulario(valoresIniciales = VACIO) {
    return {
        nombre: valoresIniciales?.nombre ?? "",
        descripcion: valoresIniciales?.descripcion ?? "",
        imagenPortada: valoresIniciales?.imagenPortada ?? "",
    };
}

export default function FormularioLista({
                                            onClose,
                                            onSubmit,
                                            titulo,
                                            textoBoton,
                                            valoresIniciales = VACIO,
                                        }) {
    const [formulario, setFormulario] = useState(() => construirFormulario(valoresIniciales));
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        setFormulario(construirFormulario(valoresIniciales));
        setError("");
        setGuardando(false);
    }, [valoresIniciales]);

    const vistaPrevia = useMemo(() => {
        const url = (formulario.imagenPortada || "").trim();
        return url || null;
    }, [formulario.imagenPortada]);

    async function enviar(e) {
        e.preventDefault();
        setError("");
        setGuardando(true);

        try {
            await onSubmit({
                nombre: formulario.nombre.trim(),
                descripcion: formulario.descripcion.trim(),
                imagenPortada: formulario.imagenPortada.trim(),
            });
            onClose();
        } catch (e) {
            setError(e.message || "No se pudo guardar la lista.");
        } finally {
            setGuardando(false);
        }
    }

    return (
        <div className="feed-formulario">
            <h2>{titulo}</h2>
            <p className="feed-formulario-subtitulo">
                Crea una portada cuidada y una descripción breve para tu lista.
            </p>

            <form className="feed-formulario-grid" onSubmit={enviar}>
                <label className="feed-formulario-campo">
                    <span>Nombre de la lista</span>
                    <input
                        className="pg-modal__input"
                        value={formulario.nombre}
                        onChange={(e) =>
                            setFormulario((prev) => ({ ...prev, nombre: e.target.value }))
                        }
                        placeholder="Ej. Mis thrillers favoritos"
                        maxLength={100}
                        required
                    />
                </label>

                <label className="feed-formulario-campo">
                    <span>Descripción</span>
                    <textarea
                        className="pg-modal__input feed-textarea"
                        value={formulario.descripcion}
                        onChange={(e) =>
                            setFormulario((prev) => ({ ...prev, descripcion: e.target.value }))
                        }
                        placeholder="Cuenta brevemente de qué trata esta lista"
                        maxLength={500}
                        rows={4}
                    />
                </label>

                <label className="feed-formulario-campo">
                    <span className="feed-label-con-ayuda">
                        <span>Imagen de portada</span>

                        <span className="feed-ayuda-wrapper" tabIndex={0}>
                            <span
                                className="feed-ayuda-icono"
                                aria-label="Ayuda sobre cómo obtener la URL de una imagen"
                            >
                                i
                            </span>

                            <span className="feed-ayuda-tooltip" role="tooltip">
                                Abre una imagen en internet, pulsa clic derecho sobre ella y selecciona
                                <strong> “Copiar dirección de imagen” </strong>
                                o la opción similar de tu navegador. Después pega aquí esa URL.
                                La dirección debe empezar por
                                <strong> http:// </strong>
                                o
                                <strong> https://</strong>.
                            </span>
                        </span>
                    </span>

                    <input
                        className="pg-modal__input"
                        type="url"
                        value={formulario.imagenPortada}
                        onChange={(e) =>
                            setFormulario((prev) => ({ ...prev, imagenPortada: e.target.value }))
                        }
                        placeholder="https://..."
                        maxLength={255}
                    />
                </label>

                <div className="feed-formulario-preview">
                    {vistaPrevia ? (
                        <img src={vistaPrevia} alt="Vista previa de portada" />
                    ) : (
                        <div className="feed-formulario-preview-vacia">
                            Vista previa de portada
                        </div>
                    )}
                </div>

                {error && <div className="feed-formulario-error">{error}</div>}

                <div className="feed-formulario-acciones">
                    <button
                        type="button"
                        className="feed-boton feed-boton-secundario"
                        onClick={onClose}
                    >
                        Cancelar
                    </button>

                    <button
                        type="submit"
                        className="feed-boton feed-boton-primario"
                        disabled={guardando}
                    >
                        {guardando ? "Guardando..." : textoBoton}
                    </button>
                </div>
            </form>
        </div>
    );
}