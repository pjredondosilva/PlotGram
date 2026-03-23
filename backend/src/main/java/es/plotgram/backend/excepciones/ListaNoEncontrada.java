package es.plotgram.backend.excepciones;

public class ListaNoEncontrada extends RuntimeException {
    public ListaNoEncontrada() {
        super("La lista solicitada no existe.");
    }
}