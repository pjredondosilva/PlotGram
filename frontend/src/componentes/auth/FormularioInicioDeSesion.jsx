import { useState } from "react";
import { loginUser, getMe } from "../../servicios/auth";

export default function LoginForm({ onDone, setUser }) {
    const [nombre, setNombre] = useState("");
    const [contrasenia, setContrasenia] = useState("");

    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    async function submit(e) {
        e.preventDefault();
        setErr("");
        setLoading(true);

        try {
            const data = await loginUser({ nombre, contrasenia});
            const me = await getMe();
            setUser(me);
            onDone?.();
        } catch (e) {
            setErr(e.message || "Nombre o contraseña incorrectos");
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <h2 style={{ color: "white", marginTop: 0 }}>Iniciar sesión</h2>

            <form onSubmit={submit} style={{ display: "grid", gap: 10 }}>
                <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Nombre de usuario" />
                <input type="password" value={contrasenia} onChange={(e) => setContrasenia(e.target.value)} placeholder="Contraseña" />

                {err && <div style={{ color: "#ff6b6b" }}>{err}</div>}

                <button disabled={loading} type="submit">
                    {loading ? "Entrando..." : "Entrar"}
                </button>
            </form>
        </>
    );
}
