import { useEffect, useState, type FormEvent } from "react";
import {
  getIngredients,
  sendAgentMessage,
  type Ingredient,
} from "./api/kitchenApi";

type ChatMessage = {
  role: "user" | "assistant";
  text: string;
};

function App() {
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [loadingPantry, setLoadingPantry] = useState(true);
  const [pantryError, setPantryError] = useState("");

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

    const userMessage: ChatMessage = {
      role: "user",
      text: trimmedMessage,
    };

    setChatMessages((current) => [...current, userMessage]);
    setMessage("");
    setSendingMessage(true);
    setChatError("");

    try {
      const result = await sendAgentMessage(trimmedMessage);

      const assistantMessage: ChatMessage = {
        role: "assistant",
        text: result.response,
      };

      setChatMessages((current) => [...current, assistantMessage]);
    } catch {
      setChatError("Could not reach the kitchen assistant.");
    } finally {
      setSendingMessage(false);
    }
  }

  return (
    <main>
      <h1>My Kitchen</h1>

      <section>
        <h2>Pantry</h2>

        {loadingPantry && <p>Loading pantry...</p>}

        {pantryError && <p>{pantryError}</p>}

        {!loadingPantry && !pantryError && (
          <ul>
            {ingredients.map((ingredient) => (
              <li key={ingredient.id}>
                {ingredient.name} — {ingredient.quantity} {ingredient.unit}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Kitchen Assistant</h2>

        {chatMessages.length === 0 && (
          <p>
            Ask me about your pantry or what you should cook.
          </p>
        )}

        <div>
          {chatMessages.map((chatMessage, index) => (
            <div key={index}>
              <strong>
                {chatMessage.role === "user" ? "You" : "Assistant"}:
              </strong>{" "}
              {chatMessage.text}
            </div>
          ))}

          {sendingMessage && (
            <p>
              <strong>Assistant:</strong> Thinking...
            </p>
          )}
        </div>

        {chatError && <p>{chatError}</p>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder="What should I make tonight?"
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
  );
}

export default App;