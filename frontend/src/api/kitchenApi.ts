const API_BASE_URL = "http://localhost:8080/api";

export type Ingredient = {
  id: number;
  name: string;
  quantity: number;
  unit: string;
  location: string;
  expirationDate: string | null;
};

export async function getIngredients(): Promise<Ingredient[]> {
  const response = await fetch(`${API_BASE_URL}/ingredients`);

  if (!response.ok) {
    throw new Error("Failed to load ingredients");
  }

  return response.json();
}

export type AgentResponse = {
  response: string;
};

export async function sendAgentMessage(
  message: string
): Promise<AgentResponse> {
  const response = await fetch(`${API_BASE_URL}/agent/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ message }),
  });

  if (!response.ok) {
    throw new Error("Failed to contact kitchen assistant");
  }

  return response.json();
}