import { RouterProvider } from "react-router-dom";
import { router } from "./router";
import { AuthProvider } from "../servicios/ContextoDeAutenticacion.jsx";
import { ChatProvider } from "../servicios/ContextoChat.jsx";

export default function App() {
    return (
        <AuthProvider>
            <ChatProvider>
                <RouterProvider router={router} />
            </ChatProvider>
        </AuthProvider>
    );
}