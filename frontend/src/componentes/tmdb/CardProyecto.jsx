import "./estilos/CardProyecto.css";
import { Link } from "react-router-dom";
import { posterUrl } from "../../utils/Img";

function formatearFecha(fecha) {
    if (!fecha) return "-";

    const d = new Date(`${fecha}T00:00:00`);
    if (Number.isNaN(d.getTime())) return fecha;

    return d.toLocaleDateString("es-ES", {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}
export default function MediaCard({ item, type }) {
    const title = type === "movie" ? item.title : item.name;
    const fechaOriginal = type === "movie" ? item.releaseDate : item.firstAirDate;
    const date = formatearFecha(fechaOriginal);
    const img = posterUrl(item.posterPath);
    const generos = item.genres?? [];
    const ruta = type === "movie"
        ? `/peliculas/${item.id}`
        : `/series/${item.id}`;


    return (
        <Link
            to={ruta}
            className="card-link"
            aria-label={`Ver ficha de ${title}`}
        >
        <div className="card">
            <div className="poster">
                {img ? <img src={img} alt={title} /> : <div className="noimg">Sin imagen</div>}
            </div>

            <div className="info">
                <div className="title">{title}</div>
                <div className="date">{date || "-"}</div>
                {Array.isArray(generos) && generos.length > 0 && (
                    <div className="genres">
                        {generos.slice(0, 6).map((g) => (
                            <span className="genre" key={g}>{g}</span>
                        ))}
                    </div>
                )}
            </div>
        </div>
        </Link>
    );
}
