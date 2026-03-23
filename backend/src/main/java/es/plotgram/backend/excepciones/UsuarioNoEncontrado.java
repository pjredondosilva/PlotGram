package es.plotgram.backend.excepciones;

public class UsuarioNoEncontrado extends RuntimeException {
    public UsuarioNoEncontrado() {
        super("El usuario no existe.");
    }
}