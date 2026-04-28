package es.plotgram.backend.excepciones;

public class ListaYaRegistrada extends RuntimeException {

    private final String campo;

    public ListaYaRegistrada(String campo) {
        super("Ya tienes una lista con ese nombre. Elige otro nombre.");
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}