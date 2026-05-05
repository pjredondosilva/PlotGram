import React, { createContext, useContext, useState } from 'react';

const ChatContext = createContext();

/**
 * Proveedor de contexto para el Chatbot.
 * Permite que diferentes páginas (como detalle de película) inyecten información
 * sobre lo que el usuario está viendo para que el asistente tenga "contexto".
 */
export const ChatProvider = ({ children }) => {
    const [uiContext, setUiContext] = useState(null);

    return (
        <ChatContext.Provider value={{ uiContext, setUiContext }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => {
    const context = useContext(ChatContext);
    if (!context) {
        throw new Error('useChat debe usarse dentro de un ChatProvider');
    }
    return context;
};
