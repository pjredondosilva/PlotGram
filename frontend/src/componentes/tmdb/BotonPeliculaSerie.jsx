export default function ToggleMediaType({ value, onChange }) {
    return (
        <div className="toggle">
            <button
                className={value === "movie" ? "active" : ""}
                onClick={() => onChange("movie")}
                type="button"
            >
                Películas
            </button>
            <button
                className={value === "tv" ? "active" : ""}
                onClick={() => onChange("tv")}
                type="button"
            >
                Series
            </button>
        </div>
    );
}
