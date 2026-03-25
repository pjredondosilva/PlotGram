export function crearContenidoListaPelicula(pelicula, imagen) {
    if (!pelicula) return null;

    return {
        tmdbId: pelicula.id,
        tipo: "PELICULA",
        titulo: pelicula.title,
        imagen: imagen || null,
        fechaPublicacion: pelicula.releaseDate || null,
        sinopsis: pelicula.overview || "",
        enlace: `/peliculas/${pelicula.id}`,
        serieTmdbId: null,
        numeroTemporada: null,
        numeroEpisodio: null,
    };
}

export function crearContenidoListaSerie(serie, imagen) {
    if (!serie) return null;

    return {
        tmdbId: serie.id,
        tipo: "SERIE",
        titulo: serie.name,
        imagen: imagen || null,
        fechaPublicacion: serie.firstAirDate || null,
        sinopsis: serie.overview || "",
        enlace: `/series/${serie.id}`,
        serieTmdbId: null,
        numeroTemporada: null,
        numeroEpisodio: null,
    };
}

export function crearContenidoListaTemporada(idSerie, temporada, imagen) {
    if (!temporada) return null;

    const numeroTemporada = temporada.seasonNumber ?? null;
    const tituloBase = temporada.name || (numeroTemporada != null ? `Temporada ${numeroTemporada}` : "Temporada");

    return {
        tmdbId: temporada.id,
        tipo: "TEMPORADA",
        titulo: numeroTemporada != null ? `Temporada ${numeroTemporada}: ${tituloBase}` : tituloBase,
        imagen: imagen || null,
        fechaPublicacion: temporada.airDate || null,
        sinopsis: temporada.overview || "",
        enlace: `/series/${idSerie}/temporadas/${numeroTemporada}`,
        serieTmdbId: Number(idSerie),
        numeroTemporada,
        numeroEpisodio: null,
    };
}

export function crearContenidoListaEpisodio(idSerie, numeroTemporada, episodio, imagen) {
    if (!episodio) return null;

    const temporadaNumero = episodio.seasonNumber ?? Number(numeroTemporada);
    const episodioNumero = episodio.episodeNumber ?? null;
    const prefijo = temporadaNumero != null && episodioNumero != null
        ? `T${temporadaNumero} · E${episodioNumero}`
        : "Episodio";

    return {
        tmdbId: episodio.id,
        tipo: "EPISODIO",
        titulo: `${prefijo}: ${episodio.name}`,
        imagen: imagen || null,
        fechaPublicacion: episodio.airDate || null,
        sinopsis: episodio.overview || "",
        enlace: `/series/${idSerie}/temporadas/${temporadaNumero}/episodios/${episodioNumero}`,
        serieTmdbId: Number(idSerie),
        numeroTemporada: temporadaNumero,
        numeroEpisodio: episodioNumero,
    };
}