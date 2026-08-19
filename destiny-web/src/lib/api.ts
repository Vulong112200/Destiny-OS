import type {
  ErrorResponse,
  MethodologyDto,
  ScenarioRunRequestInput,
  ScenarioRunResponse,
  SupportedScenarioType,
} from "./types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: ErrorResponse | null,
  ) {
    super(body?.message ?? `Yêu cầu tới API thất bại (HTTP ${status}).`);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    let body: ErrorResponse | null = null;
    try {
      body = (await response.json()) as ErrorResponse;
    } catch {
      // response body wasn't JSON - body stays null, ApiError falls back to a generic message
    }
    throw new ApiError(response.status, body);
  }

  return (await response.json()) as T;
}

export function listMethodologies(): Promise<MethodologyDto[]> {
  return request<MethodologyDto[]>("/api/v1/methodologies");
}

export function runScenario(
  scenarioType: SupportedScenarioType,
  input: ScenarioRunRequestInput,
): Promise<ScenarioRunResponse> {
  return request<ScenarioRunResponse>(`/api/v1/scenarios/${scenarioType.toLowerCase()}`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

/** Returns `null` on a 404 (unknown calculation id) rather than throwing - callers render "not found" honestly. */
export async function findCalculation(calculationId: string): Promise<ScenarioRunResponse | null> {
  try {
    return await request<ScenarioRunResponse>(
      `/api/v1/calculations/${encodeURIComponent(calculationId)}`,
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}
