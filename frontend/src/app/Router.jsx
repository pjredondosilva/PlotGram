import { createBrowserRouter } from "react-router-dom";
import LayoutPrincipal from "../layouts/LayoutPrincipal.jsx";
import Home from "../paginas/home/Home";
import PeliculaDetalle from "../paginas/tmdb/PeliculaDetalle.jsx";
import SerieDetalle from "../paginas/tmdb/SerieDetalle.jsx";
import TemporadaDetalle from "../paginas/tmdb/TemporadaDetalle.jsx";
import FeedUsuario from "../paginas/usuario/FeedUsuario.jsx";
import EpisodioDetalle from "../paginas/tmdb/EpisodioDetalle.jsx";
import DetalleLista from "../paginas/listas/DetalleLista.jsx";
import Noticias from "../paginas/noticias/Noticias.jsx";

export const router = createBrowserRouter([
    {
        element: <LayoutPrincipal />,
        children: [
            {
                index: true,
                element: <Home />,
            },
            {
                path: "peliculas/:id",
                element: <PeliculaDetalle />,
            },
            {
                path: "usuarios/:idUsuario/feed",
                element: <FeedUsuario />,
            },
            {
                path: "series/:id",
                element: <SerieDetalle />,
            },
            {
                path: "usuarios/:idUsuario/feed/listas/:idLista",
                element: <DetalleLista />,
            },
            {
                path: "series/:id/temporadas/:temporada",
                element: <TemporadaDetalle />,
            },
            {
                path: "series/:id/temporadas/:temporada/episodios/:episodio",
                element: <EpisodioDetalle />,
            },
            {
                path: "noticias",
                element: <Noticias />,
            },
        ],
    },
]);