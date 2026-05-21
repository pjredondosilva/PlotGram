const IMG_BASE = import.meta.env.VITE_TMDB_IMG_BASE;

export function posterUrl(posterPath) {
    if (!posterPath) return "";
    return `${IMG_BASE}${posterPath}`;
}
