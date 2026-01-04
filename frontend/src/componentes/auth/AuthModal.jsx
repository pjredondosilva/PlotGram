import "./estilos/AuthModal.css";
import { useEffect } from "react";

export default function AuthModal({ open, onClose, children }) {
    useEffect(() => {
        function onKey(e) {
            if (e.key === "Escape") onClose();
        }
        if (open) window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    if (!open) return null;

    return (
        <div className="pg-modal__backdrop" onClick={onClose}>
            <div className="pg-modal__panel" onClick={(e) => e.stopPropagation()}>
                {children}
            </div>
        </div>
    );
}
