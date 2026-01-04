import "./estilos/CardProyecto.css";
import { posterUrl } from "../../utils/img";

export default function MediaCard({ item, type }) {
    const title = type === "movie" ? item.title : item.name;
    const date = type === "movie" ? item.releaseDate : item.firstAirDate;
    const img = posterUrl(item.posterPath);
    const generos = item.genres?? [];

    return (
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
    );
}
