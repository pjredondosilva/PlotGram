import React, { useState, useEffect, useRef } from 'react';
import { useChat } from '../../servicios/ContextoChat';
import { useAuth } from '../../servicios/ContextoDeAutenticacion';
import ReactMarkdown from 'react-markdown';
import plotbotIcon from '../../assets/plotbot-icon.png';
import './estilos/ChatWidget.css';

/**
 * Widget de Chat Inteligente (Chatbot).
 * Permite a los usuarios interactuar con un asistente de IA experto en cine.
 */
export default function ChatWidget() {
    const { user } = useAuth();
    const { uiContext } = useChat();
    
    const [open, setOpen] = useState(false);
    const [msgs, setMsgs] = useState([
        { rol: 'model', contenido: '¡Hola! Soy PlotBot, tu asistente de PlotGram. ¿En qué película o serie estás pensando hoy?' }
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState(null);
    
    const scrollRef = useRef(null);

    // Auto-scroll al recibir nuevos mensajes
    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [msgs, loading, open]);

    // El chat solo está disponible para usuarios logueados
    if (!user) return null;

    const handleSend = async (e) => {
        if (e) e.preventDefault();
        const text = input.trim();
        if (!text || loading) return;

        const newMsgs = [...msgs, { rol: 'user', contenido: text }];
        setMsgs(newMsgs);
        setInput('');
        setLoading(true);
        setErr(null);

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    mensajes: newMsgs,
                    contextoUI: uiContext ? JSON.stringify(uiContext) : ''
                })
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.error || 'Error al conectar con el servidor');
            }

            const data = await response.json();
            setMsgs(prev => [...prev, { rol: 'model', contenido: data.content }]);
        } catch (e) {
            setErr(e.message);
            setMsgs(prev => [...prev, { rol: 'model', contenido: 'Lo siento, he tenido un problema al procesar tu pregunta.' }]);
        } finally {
            setLoading(false);
        }
    };

    const clearHistory = () => {
        setMsgs([{ rol: 'model', contenido: 'Historial reiniciado. ¿En qué más puedo ayudarte?' }]);
        setErr(null);
    };

    return (
        <div className={`pg-chat-container ${open ? 'open' : ''}`}>
            {/* Botón Flotante (FAB) */}
            <button 
                className="pg-chat-fab" 
                onClick={() => setOpen(!open)}
                title={open ? "Cerrar asistente" : "Abrir asistente inteligente"}
            >
                {open ? '✕' : <img src={plotbotIcon} alt="PlotBot" style={{ width: '45px', height: '45px', objectFit: 'contain' }} />}
            </button>

            {/* Panel de Chat */}
            {open && (
                <div className="pg-chat-panel">
                    <header className="pg-chat-header">
                        <div className="pg-chat-title">
                            <span className="pg-chat-status"></span>
                            <img src={plotbotIcon} alt="PlotBot" style={{ width: '28px', height: '28px', borderRadius: '4px' }} />
                            PlotBot Asistente
                        </div>
                        <button className="pg-chat-clear" onClick={clearHistory} title="Limpiar chat">🧹</button>
                    </header>

                    <div className="pg-chat-body" ref={scrollRef}>
                        {msgs.map((m, i) => (
                            <div key={i} className={`pg-chat-msg ${m.rol}`}>
                                <div className="pg-chat-bubble">
                                    {m.rol === 'model' ? (
                                        <ReactMarkdown>{m.contenido}</ReactMarkdown>
                                    ) : (
                                        m.contenido
                                    )}
                                </div>
                            </div>
                        ))}
                        {loading && (
                            <div className="pg-chat-msg model">
                                <div className="pg-chat-bubble loading">
                                    <span>.</span><span>.</span><span>.</span>
                                </div>
                            </div>
                        )}
                        {err && <div className="pg-chat-err">Error: {err}</div>}
                    </div>

                    <form className="pg-chat-input-area" onSubmit={handleSend}>
                        <input 
                            type="text" 
                            placeholder="Escribe tu duda sobre cine..." 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !input.trim()}>
                            {loading ? '...' : '➤'}
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
}
