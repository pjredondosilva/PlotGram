import React, { useEffect, useState } from 'react';
import TarjetaNoticia from '../../componentes/noticias/TarjetaNoticia';
import BotonNoticiasIdioma from '../../componentes/noticias/BotonNoticiasIdioma';
import { obtenerNoticias } from '../../servicios/ServicioNoticias';
import './estilos/noticias.css';

const Noticias = () => {
    const [noticias, setNoticias] = useState([]);
    const [idioma, setIdioma] = useState('es');
    const [pagina, setPagina] = useState(1);
    const [loading, setLoading] = useState(true);
    const [loadingMas, setLoadingMas] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [err, setErr] = useState('');

    const cargarNoticias = async (reset = false) => {
        if (reset) {
            setLoading(true);
            setPagina(1);
            setHasMore(true);
        } else {
            setLoadingMas(true);
        }

        try {
            const nuevaPagina = reset ? 1 : pagina + 1;
            const data = await obtenerNoticias(idioma, nuevaPagina);
            
            if (data.length < 20) {
                setHasMore(false);
            }

            if (reset) {
                setNoticias(data);
            } else {
                setNoticias(prev => [...prev, ...data]);
                setPagina(nuevaPagina);
            }
            setErr('');
        } catch (e) {
            console.error("Error al cargar noticias:", e);
            setErr("No se han podido cargar las noticias.");
        } finally {
            setLoading(false);
            setLoadingMas(false);
        }
    };

    useEffect(() => {
        cargarNoticias(true);
    }, [idioma]);

    return (
        <div className="noticias-page">
            <header className="noticias-header">
                <div className="noticias-header__top">
                    <BotonNoticiasIdioma 
                        value={idioma} 
                        onChange={setIdioma} 
                    />
                </div>
            </header>

            <div className="noticias-content">
                {loading ? (
                    <div className="msg">Cargando noticias de última hora...</div>
                ) : err ? (
                    <div className="msg error">{err}</div>
                ) : (
                    <div className="noticias-grid-container">
                        <div className="noticias-grid">
                            {noticias.map((noticia) => (
                                <TarjetaNoticia key={noticia.id} noticia={noticia} />
                            ))}
                        </div>
                        
                        {hasMore && (
                            <div className="noticias-footer">
                                <button 
                                    className="btn-cargar-mas" 
                                    onClick={() => cargarNoticias(false)}
                                    disabled={loadingMas}
                                >
                                    {loadingMas ? "Cargando..." : "Cargar noticias anteriores"}
                                </button>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Noticias;
