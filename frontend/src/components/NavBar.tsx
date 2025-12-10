import { Link, useNavigate } from "react-router-dom";
import { getToken, logout } from "../api/axios";

export default function NavBar() {
  const navigate = useNavigate();
  const token = getToken();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="flex justify-between items-center px-8 py-4 bg-gray-900 text-white shadow-md">
      <Link to="/">
        <div className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-pink-500 to-indigo-400">
          AI Video Lab
        </div>
      </Link>

      <div className="flex gap-6 text-lg">
        {token ? (
          <>
            <Link className="hover:text-indigo-400 transition" to="/videos">
              Videos
            </Link>
            <button
              className="hover:text-red-400 transition"
              onClick={handleLogout}
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link className="hover:text-indigo-400 transition" to="/login">
              Login
            </Link>
            <Link className="hover:text-indigo-400 transition" to="/signup">
              Sign Up
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
