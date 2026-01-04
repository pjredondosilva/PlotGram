import { useState } from "react";
import { loginUser } from "../../servicios/auth";

export default function LoginForm({ onDone }) {
    const [nombre, setNombre] = useState("");
    const [contrasenia, setcontrasenia] = useState("");

    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    async function submit(e) {
        e.preventDefault();
        setErr("");
        setLoading(true);

        try {
            const data = await loginUser({ nombre, contrasenia});
            onDone?.();
        } catch (e) {
            setErr(e.message || "Credenciales incorrectas");
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <h2 style={{ color: "white", marginTop: 0 }}>Iniciar sesión</h2>

            <form onSubmit={submit} style={{ display: "grid", gap: 10 }}>
                <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Nombre de usuario" />
                <input type="password" value={contrasenia} onChange={(e) => setcontrasenia(e.target.value)} placeholder="Contraseña" />

                {err && <div style={{ color: "#ff6b6b" }}>{err}</div>}

                <button disabled={loading} type="submit">
                    {loading ? "Entrando..." : "Entrar"}
                </button>
            </form>
        </>
    );
}
