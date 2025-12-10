import axios from "axios";
import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from "axios";

type RefreshResponse = { token: string | null };

let authToken: string | null =
  typeof localStorage !== "undefined" ? localStorage.getItem("token") : null;

function setToken(token: string | null) {
  authToken = token;
  if (token) localStorage.setItem("token", token ?? "");
  else localStorage.removeItem("token");
}

export function getToken() {
  return authToken;
}

let isRefreshing = false;
type QueueItem = {
  resolve: (value: AxiosResponse) => void;
  reject: (reason?: any) => void;
  config: AxiosRequestConfig;
};
let refreshQueue: QueueItem[] = [];

const api: AxiosInstance = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers = config.headers ?? {};
    config.headers["Authorization"] = `Bearer ${authToken}`;
  }
  return config;
});

async function refreshToken(): Promise<string> {
  const { data } = await axios.post<RefreshResponse>(
    "http://localhost:8080/api/auth/refresh",
    {},
    { withCredentials: true },
  );
  if (!data.token) throw new Error("No token in refresh response");
  setToken(data.token);
  return data.token;
}

api.interceptors.response.use(
  (resp) => resp,
  async (error: AxiosError) => {
    const original = error.config;
    if (!original) return Promise.reject(error);

    if (error.response?.status !== 403) return Promise.reject(error);

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        refreshQueue.push({ resolve, reject, config: original });
      });
    }

    isRefreshing = true;
    try {
      const newToken = await refreshToken();

      refreshQueue.forEach((q) => {
        q.config.headers = {
          ...q.config.headers,
          Authorization: `Bearer ${newToken}`,
        };
        api(q.config).then(q.resolve).catch(q.reject);
      });
      refreshQueue = [];

      original.headers = {
        ...original.headers,
        Authorization: `Bearer ${newToken}`,
      };
      isRefreshing = false;
      return api(original);
    } catch (refreshErr) {
      setToken(null);
      refreshQueue.forEach((q) => q.reject(refreshErr));
      refreshQueue = [];
      isRefreshing = false;
      return Promise.reject(refreshErr);
    }
  },
);

export function login(token: string) {
  setToken(token);
}

export function logout() {
  setToken(null);
}

export default api;
