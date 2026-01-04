import "./estilos/ListaProyecto.css";
import MediaCard from "./CardProyecto.jsx";

export default function MediaGrid({ items, type }) {
    return (
        <div className="grid">
            {items.map((item) => (
                <MediaCard key={item.id} item={item} type={type} />
            ))}
        </div>
    );
}
