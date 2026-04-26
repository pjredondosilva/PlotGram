package es.plotgram.backend.rest.dto.tmdb;

import es.plotgram.backend.tmdb.dto.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MapeadorTmdb {
    public DPeliculaListado DtoPelicula(DPeliculaListadoRespuesta m) {
        var ids = (m.genreIds() == null) ? List.<Integer>of() : m.genreIds();
        return new DPeliculaListado(
                m.id(),
                m.title(),
                m.releaseDate(),
                m.posterPath(),
                ids,
                List.of()
        );
    }

    public DSerieListado DtoSerie(DSerieListadoRespuesta t) {
        var ids = (t.genreIds() == null) ? List.<Integer>of() : t.genreIds();
        return new DSerieListado(
                t.id(),
                t.name(),
                t.firstAirDate(),
                t.posterPath(),
                ids,
                List.of()
        );
    }

    public DPeliculaDetalle DtoPeliculaDetalle(DPeliculaDetalleRespuesta m) {
        return new DPeliculaDetalle(
                m.id(),
                m.title(),
                m.overview(),
                m.releaseDate(),
                m.posterPath(),
                m.backdropPath(),
                m.runtime(),
                m.voteAverage(),
                mapGeneros(m.genres()),
                mapReparto(m.credits()),
                mapTrailer(m.videos()),
                mapProviders(m.watchProviders()),
                mapRecomendaciones(m.recommendations()),
                mapDirector(m.credits()),
                mapGuionista(m.credits()),
                m.budget(),
                m.revenue(),
                mapPaisOrigen(m.productionCountries()),
                mapEstrenadaEnCines(m)
        );
    }

    public DSerieDetalle DtoSerieDetalle(DSerieDetalleRespuesta s) {
        var seasons = s.seasons() == null
                ? List.<DTemporadaResumen>of()
                : s.seasons().stream()
                .filter(temp -> temp.seasonNumber() != null && temp.seasonNumber() >= 0)
                .map(this::DtoTemporadaResumen)
                .toList();

        return new DSerieDetalle(
                s.id(),
                s.name(),
                s.overview(),
                s.firstAirDate(),
                s.posterPath(),
                s.backdropPath(),
                s.numberOfSeasons(),
                s.numberOfEpisodes(),
                s.voteAverage(),
                mapGeneros(s.genres()),
                seasons,
                mapReparto(s.credits()),
                mapCreadorSerie(s.createdBy()),
                mapPaisOrigenSerie(s.originCountry()),
                mapEstadoSerie(s.status()),
                mapProviders(s.watchProviders()),
                mapRecomendacionesSeries(s.recommendations())
        );
    }

    public DTemporadaResumen DtoTemporadaResumen(DTemporadaResumenRespuesta t) {
        return new DTemporadaResumen(
                t.id(),
                t.name(),
                t.overview(),
                t.seasonNumber(),
                t.episodeCount(),
                t.airDate(),
                t.posterPath()
        );
    }

    public DTemporadaDetalle DtoTemporadaDetalle(DTemporadaDetalleRespuesta t) {
        var episodios = t.episodes() == null
                ? List.<DEpisodioListado>of()
                : t.episodes().stream()
                .map(this::DtoEpisodioListado)
                .toList();

        Integer totalEpisodios = t.episodeCount() != null
                ? t.episodeCount()
                : episodios.size();

        return new DTemporadaDetalle(
                t.id(),
                t.name(),
                t.overview(),
                t.seasonNumber(),
                totalEpisodios,
                t.airDate(),
                t.posterPath(),
                episodios
        );
    }

    public DEpisodioListado DtoEpisodioListado(DEpisodioListadoRespuesta e) {
        return new DEpisodioListado(
                e.id(),
                e.name(),
                e.overview(),
                e.episodeNumber(),
                e.seasonNumber(),
                e.airDate(),
                e.stillPath(),
                e.voteAverage()
        );
    }

    public DEpisodioDetalle DtoEpisodioDetalle(DEpisodioDetalleRespuesta e) {
        return new DEpisodioDetalle(
                e.id(),
                e.name(),
                e.overview(),
                e.episodeNumber(),
                e.seasonNumber(),
                e.airDate(),
                e.stillPath(),
                e.runtime(),
                e.voteAverage(),
                mapRepartoEpisodio(e)
        );
    }

    private List<String> mapGeneros(List<DGeneroTmdb> genres) {
        return genres == null
                ? List.of()
                : genres.stream()
                .map(DGeneroTmdb::name)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .toList();
    }

    private List<DActorTmdb> mapReparto(DCreditosTmdbRespuesta credits) {
        if (credits == null || credits.cast() == null) {
            return List.of();
        }

        return credits.cast().stream()
                .limit(12)
                .map(this::DtoActor)
                .toList();
    }

    private DActorTmdb DtoActor(DRepartoTmdbRespuesta a) {
        return new DActorTmdb(
                a.id(),
                a.name(),
                traducirPersonaje(a.character()),
                a.profilePath()
        );
    }

    private String traducirPersonaje(String character) {
        if (character == null || character.isBlank()) {
            return character;
        }

        String texto = character;

        texto = texto.replaceAll("(?i)\\(voice\\)", "(voz)");
        texto = texto.replaceAll("(?i)voice", "voz");

        return texto;
    }

    private DTrailer mapTrailer(DVideosTmdbRespuesta videos) {
        if (videos == null || videos.results() == null || videos.results().isEmpty()) {
            return null;
        }

        return videos.results().stream()
                .filter(v -> v.key() != null && !v.key().isBlank())
                .filter(v -> "YouTube".equalsIgnoreCase(v.site()))
                .filter(v -> v.type() != null && v.type().equalsIgnoreCase("Trailer"))
                .sorted(
                        Comparator.comparing((DVideoTmdbRespuesta v) -> Boolean.FALSE.equals(v.official()))
                )
                .map(this::DtoTrailer)
                .findFirst()
                .orElse(null);
    }

    private DTrailer DtoTrailer(DVideoTmdbRespuesta v) {
        return new DTrailer(
                v.name(),
                v.key(),
                v.site()
        );
    }

    private DProveedoresPelicula mapProviders(DProveedoresConsultaTmdbRespuesta providers) {
        if (providers == null || providers.results() == null) {
            return new DProveedoresPelicula(List.of(), List.of(), List.of());
        }

        var es = providers.results().get("ES");
        if (es == null) {
            return new DProveedoresPelicula(List.of(), List.of(), List.of());
        }

        return new DProveedoresPelicula(
                mapProviderList(es.flatrate()),
                mapProviderList(es.rent()),
                mapProviderList(es.buy())
        );
    }

    private List<DProveedor> mapProviderList(List<DProveedorTmdbRespuesta> providers) {
        if (providers == null) {
            return List.of();
        }

        return providers.stream()
                .map(this::DtoProveedor)
                .toList();
    }

    private DProveedor DtoProveedor(DProveedorTmdbRespuesta p) {
        return new DProveedor(
                p.providerId(),
                p.providerName(),
                p.logoPath()
        );
    }

    private List<DPeliculaRelacionada> mapRecomendaciones(DPeliculasRelacionadasTmdbRespuesta recommendations) {
        if (recommendations == null || recommendations.results() == null) {
            return List.of();
        }

        return recommendations.results().stream()
                .limit(12)
                .map(this::DtoPeliculaRecomendada)
                .toList();
    }

    private DPeliculaRelacionada DtoPeliculaRecomendada(DPeliculaListadoRespuesta p) {
        return new DPeliculaRelacionada(
                p.id(),
                p.title(),
                p.releaseDate(),
                p.posterPath()
        );
    }
    private String mapDirector(DCreditosTmdbRespuesta credits) {
        if (credits == null || credits.crew() == null) {
            return null;
        }

        return credits.crew().stream()
                .filter(c -> c.job() != null && c.job().equalsIgnoreCase("Director"))
                .map(DCrewTmdbRespuesta::name)
                .findFirst()
                .orElse(null);
    }

    private String mapGuionista(DCreditosTmdbRespuesta credits) {
        if (credits == null || credits.crew() == null) {
            return null;
        }

        return credits.crew().stream()
                .filter(c -> c.job() != null && (
                        c.job().equalsIgnoreCase("Screenplay") ||
                                c.job().equalsIgnoreCase("Writer") ||
                                c.job().equalsIgnoreCase("Story")
                ))
                .map(DCrewTmdbRespuesta::name)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    private String mapPaisOrigen(List<DPaisProduccionTmdbRespuesta> countries) {
        if (countries == null || countries.isEmpty()) {
            return "No disponible";
        }

        return countries.stream()
                .map(pais -> resolverPaisOrigen(pais.name(), pais.iso31661()))
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .findFirst()
                .orElse("No disponible");
    }

    private Boolean mapEstrenadaEnCines(DPeliculaDetalleRespuesta m) {
        if (m == null) {
            return false;
        }

        if ("Released".equalsIgnoreCase(m.status())) {
            return true;
        }

        return tieneEstrenoTeatral(m.releaseDates());
    }

    private Boolean tieneEstrenoTeatral(DReleaseDatesTmdbRespuesta releaseDates) {
        if (releaseDates == null || releaseDates.results() == null) {
            return false;
        }

        return releaseDates.results().stream()
                .filter(pais -> pais.releaseDates() != null)
                .flatMap(pais -> pais.releaseDates().stream())
                .anyMatch(item -> item.type() != null && (item.type() == 2 || item.type() == 3));
    }

    private String traducirPaisPorIso(String iso2) {
        if (iso2 == null || iso2.isBlank()) return null;

        String nombre = new Locale("", iso2).getDisplayCountry(new Locale("es", "ES"));
        return (nombre == null || nombre.isBlank()) ? null : nombre;
    }

    private String resolverPaisOrigen(String nombreOriginal, String iso2) {
        String traducido = traducirPaisPorIso(iso2);
        if (traducido != null) return traducido;

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "No disponible";
        }

        return nombreOriginal;
    }

    private String mapCreadorSerie(List<DCreadorSerieTmdbRespuesta> createdBy) {
        if (createdBy == null || createdBy.isEmpty()) {
            return "No disponible";
        }

        return createdBy.stream()
                .map(DCreadorSerieTmdbRespuesta::name)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("No disponible");
    }

    private String mapPaisOrigenSerie(List<String> originCountry) {
        if (originCountry == null || originCountry.isEmpty()) {
            return "No disponible";
        }

        return originCountry.stream()
                .map(this::traducirPaisPorIso)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .findFirst()
                .orElse("No disponible");
    }

    private List<DSerieRelacionada> mapRecomendacionesSeries(DSeriesRelacionadasTmdbRespuesta recommendations) {
        if (recommendations == null || recommendations.results() == null) {
            return List.of();
        }

        return recommendations.results().stream()
                .limit(12)
                .map(this::DtoSerieRelacionada)
                .toList();
    }

    private DSerieRelacionada DtoSerieRelacionada(DSerieListadoRespuesta s) {
        return new DSerieRelacionada(
                s.id(),
                s.name(),
                s.firstAirDate(),
                s.posterPath()
        );
    }
    private String mapEstadoSerie(String status) {
        if (status == null || status.isBlank()) {
            return "No disponible";
        }

        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "returning series", "in production", "planned" -> "En emisión";
            case "ended" -> "Finalizada";
            case "canceled", "cancelled" -> "Cancelada";
            default -> status;
        };
    }

    private List<DActorTmdb> mapRepartoEpisodio(DEpisodioDetalleRespuesta episodio) {
        if (episodio == null) {
            return List.of();
        }

        List<DRepartoTmdbRespuesta> invitados = episodio.guestStars() == null
                ? List.of()
                : episodio.guestStars();

        List<DRepartoTmdbRespuesta> castRegular =
                episodio.credits() == null || episodio.credits().cast() == null
                        ? List.of()
                        : episodio.credits().cast();

        return java.util.stream.Stream.concat(invitados.stream(), castRegular.stream())
                .collect(java.util.stream.Collectors.toMap(
                        DRepartoTmdbRespuesta::id,
                        actor -> actor,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .limit(12)
                .map(this::DtoActor)
                .toList();
    }

    public DTrailer extraerTrailer(DVideosTmdbRespuesta videos) {
        return mapTrailer(videos);
    }
}
