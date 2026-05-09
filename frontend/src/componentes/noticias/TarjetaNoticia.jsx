import React from 'react';
import './TarjetaNoticia.css';
import imagenReserva from '../../assets/noticia-default.jpg';

const TarjetaNoticia = ({ noticia }) => {
    const formatearFecha = (fechaStr) => {
        const fecha = new Date(fechaStr);
        return fecha.toLocaleDateString('es-ES', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });
    };

    const imagenPorDefecto = imagenReserva;

    const srcImagen = noticia.imagen || noticia.image || imagenPorDefecto;

    return (
        <a 
            href={noticia.url} 
            target="_blank" 
            rel="noopener noreferrer" 
            className="tarjeta-noticia"
        >
            <div className="tarjeta-noticia__imagen-wrapper">
                <img 
                    src={srcImagen} 
                    alt={noticia.titulo} 
                    className="tarjeta-noticia__imagen"
                    loading="lazy"
                    onError={(e) => { 
                        if (e.target.src !== imagenPorDefecto) {
                            e.target.src = imagenPorDefecto;
                        }
                    }}
                />
                <div className="tarjeta-noticia__fuente">
                    {noticia.fuente}
                </div>
            </div>
            <div className="tarjeta-noticia__contenido">
                <span className="tarjeta-noticia__fecha">
                    {formatearFecha(noticia.fechaPublicacion)}
                </span>
                <h3 className="tarjeta-noticia__titulo">{noticia.titulo}</h3>
                <p className="tarjeta-noticia__descripcion">
                    {noticia.descripcion}
                </p>
            </div>
        </a>
    );
};

export default TarjetaNoticia;
