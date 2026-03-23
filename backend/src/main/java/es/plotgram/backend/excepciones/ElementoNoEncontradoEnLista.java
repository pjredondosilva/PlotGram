package es.plotgram.backend.excepciones;

public class ElementoNoEncontradoEnLista extends RuntimeException {
  public ElementoNoEncontradoEnLista() {
    super("El elemento indicado no existe dentro de la lista.");
  }
}