import { useState } from "react";
import FormInputField from "../components/FormInputField";
import api from "../api/axios";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import { toast } from "react-hot-toast";
import { login } from "../api/axios";

function LogIn() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const submit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await api.post(
        "/auth/login",
        { username, password },
        { withCredentials: true },
      );

      login(data.token);
      console.log(jwtDecode(data.token));

      setUsername("");
      setPassword("");

      navigate("/videos");
    } catch (err) {
      console.error(err);
      toast.error("Login failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center px-6">
      <div className="w-full max-w-md bg-gray-800 p-10 rounded-3xl shadow-2xl">
        <h1 className="text-4xl font-extrabold text-center mb-6 text-transparent bg-clip-text bg-linear-to-r from-purple-400 via-pink-500 to-indigo-400">
          Log In
        </h1>
        <form className="flex flex-col" onSubmit={submit}>
          <FormInputField
            name="Username"
            type="text"
            placeholder="username123"
            state={username}
            setState={setUsername}
          />
          <FormInputField
            name="Password"
            type="password"
            placeholder="******"
            state={password}
            setState={setPassword}
          />
          <button
            type="submit"
            disabled={loading}
            className="mt-6 bg-indigo-600 hover:bg-indigo-500 transition px-6 py-4 rounded-2xl font-semibold shadow-lg text-white text-lg"
          >
            {loading ? "Logging in..." : "Log In"}
          </button>
        </form>
        <p className="text-center text-gray-400 mt-4">
          Don't have an account?{" "}
          <span className="text-indigo-400 hover:underline cursor-pointer">
            <Link to="/signup">Sign Up</Link>
          </span>
        </p>
      </div>
    </div>
  );
}

export default LogIn;
