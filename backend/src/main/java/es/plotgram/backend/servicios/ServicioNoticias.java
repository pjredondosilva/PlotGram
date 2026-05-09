package es.plotgram.backend.servicios;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import es.plotgram.backend.rest.dto.DNoticia;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la gestión, descarga y normalización de noticias de cine
 * mediante el consumo de feeds RSS/Atom.
 * Mantiene una caché en memoria de las noticias para un acceso rápido.
 */
@Service
public class ServicioNoticias {

    private record FeedConfig(String nombre, String url, String idioma) {}

    private static final List<FeedConfig> origenes = List.of(
            new FeedConfig("Espinof", "https://www.espinof.com/tag/noticias/rss2.xml", "es"),
            new FeedConfig("Espinof Cine", "https://www.espinof.com/tag/cine/rss2.xml", "es"),
            new FeedConfig("Espinof Series", "https://www.espinof.com/tag/serie/rss2.xml", "es"),
            new FeedConfig("Espinof Marvel", "https://www.espinof.com/tag/marvel/rss2.xml", "es"),
            new FeedConfig("Espinof DC", "https://www.espinof.com/tag/dc/rss2.xml", "es"),
            new FeedConfig("Sensacine", "https://www.sensacine.com/rss/noticias.xml", "es"),
            new FeedConfig("Variety", "https://variety.com/v/film/feed/", "us"),
            new FeedConfig("Hollywood Reporter", "https://www.hollywoodreporter.com/c/movies/feed/", "us"),
            new FeedConfig("Collider", "https://collider.com/feed/category/movie-news/", "us"),
            new FeedConfig("Deadline", "https://deadline.com/v/film/feed/", "us")
    );

    private final List<DNoticia> cacheNoticias = new CopyOnWriteArrayList<>();
    private Instant ultimaActualizacion = null;

    /**
     * Tarea programada que actualiza el feed de noticias automáticamente.
     * Se ejecuta cada 10 minutos (600.000 ms).
     * Combina las noticias nuevas con las existentes y elimina duplicados.
     */
    @Scheduled(fixedRate = 600000)
    public void actualizarNoticias() {
        List<DNoticia> todas = new ArrayList<>();

        for (FeedConfig config : origenes) {
            try {
                todas.addAll(procesarOrigen(config));
            } catch (Exception e) {
            }
        }
        List<DNoticia> combinadas = new ArrayList<>(todas);
        combinadas.addAll(cacheNoticias);

        Map<String, DNoticia> mapaSinDuplicados = new LinkedHashMap<>();
        for (DNoticia n : combinadas) {
            mapaSinDuplicados.putIfAbsent(n.url(), n);
        }

        List<DNoticia> listaFinal = new ArrayList<>(mapaSinDuplicados.values());
        listaFinal.sort((a, b) -> b.fechaPublicacion().compareTo(a.fechaPublicacion()));

        if (listaFinal.size() > 500) {
            listaFinal = listaFinal.subList(0, 500);
        }

        cacheNoticias.clear();
        cacheNoticias.addAll(listaFinal);
        ultimaActualizacion = Instant.now();
    }

    /**
     * Obtiene la lista actual de noticias almacenadas en la caché.
     * Si la caché está vacía, dispara una actualización inmediata.
     * 
     * @return Lista de objetos DNoticia ordenados por fecha.
     */
    public List<DNoticia> getNoticias() {
        if (cacheNoticias.isEmpty()) {
            actualizarNoticias();
        }
        return new ArrayList<>(cacheNoticias);
    }

    /**
     * Procesa un feed individual utilizando la librería ROME.
     * 
     * @param config Configuración del feed (nombre, url e idioma).
     * @return Lista de noticias extraídas y normalizadas del feed.
     */
    private List<DNoticia> procesarOrigen(FeedConfig config) throws Exception {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(config.url())));

        return feed.getEntries().stream().map(entry -> {
            String imagen = extraerImagen(entry);
            String desc = entry.getDescription() != null ? entry.getDescription().getValue() : "";
            desc = limpiarHtml(desc);

            return new DNoticia(
                    UUID.randomUUID().toString(),
                    entry.getTitle(),
                    desc,
                    entry.getLink(),
                    imagen,
                    config.nombre(),
                    config.idioma(),
                    entry.getPublishedDate() != null ? entry.getPublishedDate().toInstant() : Instant.now()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Extrae una URL de imagen válida de una entrada de feed.
     * Busca en los enclosures del feed o mediante patrones regex en el contenido.
     * 
     * @param entry Entrada del feed RSS/Atom.
     * @return URL de la imagen o null si no se encuentra.
     */
    private String extraerImagen(SyndEntry entry) {
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            return entry.getEnclosures().get(0).getUrl();
        }
        String content = entry.getDescription() != null ? entry.getDescription().getValue() : "";
        Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        if (m.find()) return m.group(1);
        return null;
    }

    /**
     * Limpia las etiquetas HTML de un texto para obtener una descripción plana.
     * 
     * @param html Texto con etiquetas HTML.
     * @return Texto limpio de etiquetas.
     */
    private String limpiarHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}
