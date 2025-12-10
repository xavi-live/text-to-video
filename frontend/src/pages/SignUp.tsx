import { useState } from "react";
import FormInputField from "../components/FormInputField";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";

function SignUp() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const submit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await api.post("/auth/register", {
        username,
        email,
        password,
      });

      console.log(data);
      setUsername("");
      setEmail("");
      setPassword("");

      toast.success("Account created successfully!");
      navigate("/login");
    } catch (err) {
      console.error(err);
      alert("Sign up failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center px-6">
      <div className="w-full max-w-md bg-gray-800 p-10 rounded-3xl shadow-2xl">
        <h1 className="text-4xl font-extrabold text-center mb-6 text-transparent bg-clip-text bg-linear-to-r from-purple-400 via-pink-500 to-indigo-400">
          Sign Up
        </h1>
        <form className="flex flex-col gap-4" onSubmit={submit}>
          <FormInputField
            name="Username"
            type="text"
            placeholder="username123"
            state={username}
            setState={setUsername}
          />
          <FormInputField
            name="Email"
            type="email"
            placeholder="me@example.com"
            state={email}
            setState={setEmail}
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
            {loading ? "Signing up..." : "Sign Up"}
          </button>
        </form>
        <p className="text-center text-gray-400 mt-4">
          Already have an account?{" "}
          <span
            className="text-indigo-400 hover:underline cursor-pointer"
            onClick={() => navigate("/login")}
          >
            Log In
          </span>
        </p>
      </div>
    </div>
  );
}

export default SignUp;
