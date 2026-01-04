import "./estilos/Header.css";
//import logo from "../../assets/logo.png";

export default function Header({ onLogin, onRegister }) {
    return (
        <header className="pg-header">
            <div className="pg-header__left">
                {/*<img className="pg-logo" src={logo} alt="Plotgram" />*/}
                <span className="pg-brand">Plotgram</span>
            </div>

            <div className="pg-header__right">
                <button className="pg-link" onClick={onLogin}>Iniciar sesión</button>
                <span className="pg-sep">|</span>
                <button className="pg-link" onClick={onRegister}>Registrarse</button>
            </div>
        </header>
    );
}
