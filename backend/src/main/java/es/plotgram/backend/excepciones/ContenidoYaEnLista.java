package es.plotgram.backend.excepciones;

public class ContenidoYaEnLista extends RuntimeException {
    public ContenidoYaEnLista() {
        super("El contenido ya está añadido en la lista.");
    }
}