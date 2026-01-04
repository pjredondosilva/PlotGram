package es.plotgram.backend.excepciones;

public class UsuarioYaRegistrado extends RuntimeException {
    private final String campo;

    public UsuarioYaRegistrado(String campo) {
        super(campo.equals("email")
                ? "Ya existe una cuenta con ese email."
                : "Ese nombre de usuario ya está en uso.");
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
