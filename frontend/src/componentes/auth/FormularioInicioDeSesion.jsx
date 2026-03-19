import { useState } from "react";
import { loginUser, getMe } from "../../servicios/auth";

export default function FormularioLogin({ onDone, setUser, form, setForm, resetForm }) {
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    const { nombre, contrasenia } = form;

    async function submit(e) {
        e.preventDefault();
        setErr("");
        setLoading(true);

        try {
            await loginUser({ nombre, contrasenia });
            const me = await getMe();
            setUser(me);

            resetForm?.();
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
                <input
                    className="pg-modal__input"
                    value={nombre}
                    onChange={(e) =>
                        setForm((prev) => ({ ...prev, nombre: e.target.value }))
                    }
                    placeholder="Nombre de usuario"
                />

                <input
                    className="pg-modal__input"
                    type="password"
                    value={contrasenia}
                    onChange={(e) =>
                        setForm((prev) => ({ ...prev, contrasenia: e.target.value }))
                    }
                    placeholder="Contraseña"
                />

                {err && <div style={{ color: "#ff6b6b" }}>{err}</div>}

                <button disabled={loading} type="submit">
                    {loading ? "Entrando..." : "Entrar"}
                </button>
            </form>
        </>
    );
}