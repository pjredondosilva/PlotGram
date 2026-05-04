import React from 'react';

export default function BotonNoticiasIdioma({ value, onChange }) {
    return (
        <div className="toggle">
            <button
                className={value === "es" ? "active" : ""}
                onClick={() => onChange("es")}
                type="button"
            >
                Español
            </button>
            <button
                className={value === "us" ? "active" : ""}
                onClick={() => onChange("us")}
                type="button"
            >
                Internacional
            </button>
        </div>
    );
}
