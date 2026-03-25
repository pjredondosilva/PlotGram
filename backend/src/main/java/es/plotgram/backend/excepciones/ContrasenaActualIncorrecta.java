package es.plotgram.backend.excepciones;

public class ContrasenaActualIncorrecta extends RuntimeException {
    public ContrasenaActualIncorrecta() {
        super("La contraseña actual no es correcta.");
    }
}