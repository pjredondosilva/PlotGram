import { useMemo, useState } from "react";
import { registerUser, loginUser, getMe } from "../../servicios/auth";

const hasUpper = (s) => /[A-Z]/.test(s);
const hasDigit = (s) => /\d/.test(s);
const hasSpecial = (s) => /[.,!@#$%^&*()_\-+=\[\]{};:'"\\|<>/?]/.test(s);
const isValidEmail = (s) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s);

export default function FormularioRegistro({ onDone, setUser, form, setForm, resetForm }) {
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    const { nombre, email, contrasenia } = form;

    const rules = useMemo(() => {
        return {
            length: contrasenia.length >= 8,
            upper: hasUpper(contrasenia),
            digit: hasDigit(contrasenia),
            special: hasSpecial(contrasenia),
        };
    }, [contrasenia]);

    const passwordOk = Object.values(rules).every(Boolean);

    async function submit(e) {
        e.preventDefault();
        setErr("");

        if (!passwordOk) {
            setErr("Contraseña débil: revisa los requisitos.");
            return;
        }

        const nombreTrim = nombre.trim();
        const emailTrim = email.trim();

        if (nombreTrim.length < 3) {
            setErr("El nombre debe tener al menos 3 caracteres.");
            return;
        }

        if (!isValidEmail(emailTrim)) {
            setErr("El email no tiene un formato válido.");
            return;
        }

        setLoading(true);
        try {
            await registerUser({
                nombre: nombreTrim,
                email: emailTrim,
                contrasenia,
            });

            await loginUser({ nombre: nombreTrim, contrasenia });
            const me = await getMe();
            setUser?.(me);

            resetForm?.();
            onDone?.();
        } catch (e) {
            setErr(e.message || "Error registrando usuario");
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <h2 className="pg-modal__title">Registrarse</h2>

            <form className="pg-modal__form" onSubmit={submit} noValidate>
                <input
                    className="pg-modal__input"
                    value={nombre}
                    onChange={(e) =>
                        setForm((prev) => ({ ...prev, nombre: e.target.value }))
                    }
                    placeholder="Nombre de usuario"
                    required
                    minLength={3}
                    maxLength={30}
                />

                <input
                    className="pg-modal__input"
                    value={email}
                    onChange={(e) =>
                        setForm((prev) => ({ ...prev, email: e.target.value }))
                    }
                    placeholder="Email"
                    type="email"
                    required
                />

                <input
                    className="pg-modal__input"
                    type="password"
                    value={contrasenia}
                    onChange={(e) =>
                        setForm((prev) => ({ ...prev, contrasenia: e.target.value }))
                    }
                    placeholder="Contraseña"
                    required
                    minLength={8}
                />

                <div className="pg-modal__rules">
                    <div className={`pg-modal__rule ${rules.length ? "is-ok" : "is-bad"}`}>
                        <span className="pg-modal__mark">{rules.length ? "✅" : "❌"}</span>
                        8+ caracteres
                    </div>
                    <div className={`pg-modal__rule ${rules.upper ? "is-ok" : "is-bad"}`}>
                        <span className="pg-modal__mark">{rules.upper ? "✅" : "❌"}</span>
                        1 mayúscula
                    </div>
                    <div className={`pg-modal__rule ${rules.digit ? "is-ok" : "is-bad"}`}>
                        <span className="pg-modal__mark">{rules.digit ? "✅" : "❌"}</span>
                        1 número
                    </div>
                    <div className={`pg-modal__rule ${rules.special ? "is-ok" : "is-bad"}`}>
                        <span className="pg-modal__mark">{rules.special ? "✅" : "❌"}</span>
                        1 carácter especial (ej: . o ,)
                    </div>
                </div>

                {err && <div className="pg-modal__error">{err}</div>}

                <button className="pg-modal__submit" disabled={loading || !passwordOk} type="submit">
                    {loading ? "Creando..." : "Crear cuenta"}
                </button>
            </form>
        </>
    );
}