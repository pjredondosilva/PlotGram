import { createBrowserRouter } from "react-router-dom";
import MainLayout from "../layouts/MainLayout";
import Home from "../paginas/home/Home";
// import Login from "../paginas/login/Login";
// import Registro from "../paginas/registro/Registro";

export const router = createBrowserRouter([
    {
        element: <MainLayout />,
        children: [
            { path: "/", element: <Home /> },
        ],
    },
]);
