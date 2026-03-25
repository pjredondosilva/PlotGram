import { useMemo } from "react";

export default function BarraDeBusqueda({
                                            value,
                                            onChange,
                                            placeholder,
                                            filtrosAbiertos,
                                            onToggleFiltros,
                                            fechaDesde,
                                            fechaHasta,
                                            onChangeFechaDesde,
                                            onChangeFechaHasta,
                                            generosDisponibles = [],
                                            generosSeleccionados = [],
                                            onToggleGenero,
                                            onLimpiarFiltros,
                                            filtrosDeshabilitados = false,
                                        }) {
    const cantidadFiltrosActivos = useMemo(() => {
        let total = 0;

        if (fechaDesde) total += 1;
        if (fechaHasta) total += 1;
        total += generosSeleccionados.length;

        return total;
    }, [fechaDesde, fechaHasta, generosSeleccionados]);

    const hayFiltrosActivos = cantidadFiltrosActivos > 0;

    return (
        <div className="search-panel">
            <div className="search-row">
                <input
                    type="search"
                    className="search"
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    placeholder={placeholder}
                    aria-label="Buscar contenido"
                />

                <button
                    type="button"
                    className={`filter-toggle ${filtrosAbiertos ? "active" : ""} ${hayFiltrosActivos ? "has-filters" : ""}`}
                    onClick={onToggleFiltros}
                    aria-expanded={filtrosAbiertos}
                    aria-label="Mostrar u ocultar filtros"
                    disabled={filtrosDeshabilitados}
                >
                    <span>Filtros</span>
                    {hayFiltrosActivos && (
                        <span className="filter-badge">{cantidadFiltrosActivos}</span>
                    )}
                </button>

                {hayFiltrosActivos && !filtrosDeshabilitados && (
                    <button
                        type="button"
                        className="filter-clear"
                        onClick={onLimpiarFiltros}
                        aria-label="Limpiar filtros activos"
                    >
                        Limpiar
                    </button>
                )}
            </div>

            {filtrosDeshabilitados && (
                <p className="filters-note">
                    Los filtros globales por fecha y género se aplican al listado general. Limpia el texto de búsqueda para utilizarlos.
                </p>
            )}

            {filtrosAbiertos && !filtrosDeshabilitados && (
                <div className="filters-box">
                    <div className="filters-grid">
                        <div className="filters-section">
                            <h5 className="filters-section-title">Fecha</h5>

                            <div className="filters-dates">
                                <label className="filter-field">
                                    <span className="filter-label">Desde</span>
                                    <input
                                        className="filter-input"
                                        type="date"
                                        value={fechaDesde}
                                        max={fechaHasta || undefined}
                                        onChange={(e) => onChangeFechaDesde(e.target.value)}
                                    />
                                </label>

                                <label className="filter-field">
                                    <span className="filter-label">Hasta</span>
                                    <input
                                        className="filter-input"
                                        type="date"
                                        value={fechaHasta}
                                        min={fechaDesde || undefined}
                                        onChange={(e) => onChangeFechaHasta(e.target.value)}
                                    />
                                </label>
                            </div>
                        </div>

                        <div className="filters-section">
                            <h5 className="filters-section-title">Géneros</h5>

                            {generosDisponibles.length === 0 ? (
                                <p className="filters-empty">
                                    No hay géneros disponibles.
                                </p>
                            ) : (
                                <div className="filters-genres">
                                    {generosDisponibles.map((genero) => {
                                        const activo = generosSeleccionados.includes(genero);

                                        return (
                                            <button
                                                key={genero}
                                                type="button"
                                                className={`filter-chip ${activo ? "active" : ""}`}
                                                onClick={() => onToggleGenero(genero)}
                                                aria-pressed={activo}
                                            >
                                                {genero}
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}