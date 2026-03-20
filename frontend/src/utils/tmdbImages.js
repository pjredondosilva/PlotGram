const BASE = "https://image.tmdb.org/t/p";

function build(path, size) {
    return path ? `${BASE}/${size}${path}` : "";
}

export function backdropUrl(path) {
    return build(path, "w780");
}

export function posterDetailUrl(path) {
    return build(path, "w342");
}

export function stillUrl(path) {
    return build(path, "w780");
}

export function profileUrl(path) {
    return build(path, "w185");
}

export function logoUrl(path) {
    return build(path, "w92");
}