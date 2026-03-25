import { createBrowserRouter } from "react-router-dom";
import MainLayout from "../layouts/MainLayout";
import Home from "../paginas/home/Home";
import PeliculaDetalle from "../paginas/tmdb/PeliculaDetalle.jsx";
import SerieDetalle from "../paginas/tmdb/SerieDetalle.jsx";
import TemporadaDetalle from "../paginas/tmdb/TemporadaDetalle.jsx";
import FeedUsuario from "../paginas/usuario/FeedUsuario.jsx";
import EpisodioDetalle from "../paginas/tmdb/EpisodioDetalle.jsx";
import DetalleLista from "../paginas/usuario/DetalleLista.jsx";


export const router = createBrowserRouter([
    {
        element: <MainLayout />,
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
                path: "mi-feed",
                element: <FeedUsuario />,
            },
            {
                path: "series/:id",
                element: <SerieDetalle />,
            },
            {
                path: "listas/:id",
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
        ],
    },
]);
