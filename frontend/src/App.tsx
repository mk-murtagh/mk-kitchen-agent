import BarcodeScanner from "./components/BarcodeScanner";

import { useEffect, useState, type FormEvent } from "react";
import {
  getIngredients,
  sendAgentMessage,
  type Ingredient,
} from "./api/kitchenApi";
import "./App.css";

type ChatMessage = {
  role: "user" | "assistant";
  text: string;
};

function App() {
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [loadingPantry, setLoadingPantry] = useState(true);
  const [pantryError, setPantryError] = useState("");
  const [showAddItem, setShowAddItem] = useState(false);

  const [message, setMessage] = useState("");
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [sendingMessage, setSendingMessage] = useState(false);
  const [chatError, setChatError] = useState("");

  useEffect(() => {
    async function loadIngredients() {
      try {
        const data = await getIngredients();
        setIngredients(data);
      } catch {
        setPantryError("Could not load pantry.");
      } finally {
        setLoadingPantry(false);
      }
    }

    loadIngredients();
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedMessage = message.trim();

    if (!trimmedMessage || sendingMessage) {
      return;
    }

    setChatMessages((current) => [
      ...current,
      {
        role: "user",
        text: trimmedMessage,
      },
    ]);

    setMessage("");
    setSendingMessage(true);
    setChatError("");

    try {
      const result = await sendAgentMessage(trimmedMessage);

      setChatMessages((current) => [
        ...current,
        {
          role: "assistant",
          text: result.response,
        },
      ]);
    } catch {
      setChatError("Could not reach the kitchen assistant.");
    } finally {
      setSendingMessage(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Smart kitchen dashboard</p>
          <h1>My Kitchen</h1>
          <p className="header-subtitle">
            Track what you have and get recipe ideas from your kitchen assistant.
          </p>
        </div>
      </header>

      <main className="dashboard">
        <section className="panel pantry-panel">
          <div className="panel-header">
            <div>
              <p className="section-label">Inventory</p>
              <h2>Pantry</h2>
            </div>
            <div className='pantry-header-actions'>
              <span className="count-badge">
              {ingredients.length} items
            </span>
            <button
              className="add-item-button"
              onClick={() => setShowAddItem(true)}
            >
              + Add Item
            </button>
            </div>
          </div>

          {loadingPantry && (
            <p className="muted-text">Loading pantry...</p>
          )}

          {pantryError && (
            <p className="error-text">{pantryError}</p>
          )}

          {!loadingPantry && !pantryError && (
            <div className="ingredient-grid">
              {ingredients.map((ingredient) => (
                <article className="ingredient-card" key={ingredient.id}>
                  <div className="ingredient-top-row">
                    <h3>{ingredient.name}</h3>

                    <span className="location-badge">
                      {ingredient.location}
                    </span>
                  </div>

                  <p className="ingredient-quantity">
                    {ingredient.quantity}{" "}
                    <span>{ingredient.unit}</span>
                  </p>

                  {ingredient.expirationDate && (
                    <p className="expiration-text">
                      Expires {ingredient.expirationDate}
                    </p>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel assistant-panel">
          <div className="panel-header">
            <div>
              <p className="section-label">AI Assistant</p>
              <h2>Kitchen Assistant</h2>
            </div>

            <span className="status-indicator">
              <span className="status-dot" />
              Online
            </span>
          </div>

          <div className="chat-window">
            {chatMessages.length === 0 && (
              <div className="empty-chat">
                <div className="empty-chat-icon">✦</div>

                <h3>What can I help you make?</h3>

                <p>
                  Ask about your pantry, expiring ingredients,
                  or what you should cook tonight.
                </p>
              </div>
            )}

            {chatMessages.map((chatMessage, index) => (
              <div
                key={index}
                className={`message-row ${chatMessage.role}`}
              >
                <div className="message-bubble">
                  <span className="message-author">
                    {chatMessage.role === "user"
                      ? "You"
                      : "Kitchen Assistant"}
                  </span>

                  <p>{chatMessage.text}</p>
                </div>
              </div>
            ))}

            {sendingMessage && (
              <div className="message-row assistant">
                <div className="message-bubble">
                  <span className="message-author">
                    Kitchen Assistant
                  </span>
                  <p className="muted-text">Thinking...</p>
                </div>
              </div>
            )}
          </div>

          {chatError && (
            <p className="error-text">{chatError}</p>
          )}

          <form className="chat-form" onSubmit={handleSubmit}>
            <input
              type="text"
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Ask what you should make..."
              disabled={sendingMessage}
            />

            <button
              type="submit"
              disabled={sendingMessage || !message.trim()}
            >
              Send
            </button>
          </form>
        </section>
      </main>
      {showAddItem && (
        <div
          className="modal-backdrop"
          onClick={() => setShowAddItem(false)}
        >
          <div
            className="add-item-modal"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <p className="section-label">Inventory</p>
                <h2>Add an Item</h2>
              </div>

              <button
                className="modal-close-button"
                onClick={() => setShowAddItem(false)}
                aria-label="Close"
              >
                ×
              </button>
            </div>

            <BarcodeScanner />
          </div>
        </div>
      )}
    </div>
  );
}

export default App;