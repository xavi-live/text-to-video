import { Link } from "react-router-dom";

function Home() {
  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col font-sans">
      <section className="flex flex-col justify-center items-center text-center py-24 px-6 md:px-32 bg-gradient-to-b from-gray-900 via-gray-800 to-gray-900">
        <h1 className="text-5xl md:text-7xl font-extrabold mb-6 tracking-wide text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-pink-500 to-indigo-400">
          Text to Video
        </h1>
        <p className="text-lg md:text-2xl mb-10 max-w-2xl opacity-80">
          Instantly turn your ideas into videos. No installation, no signup—just
          pure magic.
        </p>
        <div className="flex flex-col md:flex-row gap-6">
          <Link to="/videos">
            <button className="bg-indigo-600 hover:bg-indigo-500 transition px-8 py-4 rounded-xl font-semibold shadow-lg">
              Try the Demo
            </button>
          </Link>
          <button className="border border-indigo-500 text-indigo-400 hover:bg-indigo-700 hover:text-white transition px-8 py-4 rounded-xl font-semibold shadow-lg">
            How It Works
          </button>
        </div>
      </section>

      <section className="py-24 px-6 md:px-32 grid grid-cols-1 md:grid-cols-3 gap-10 text-center">
        <div className="bg-gray-800 p-8 rounded-2xl shadow-xl hover:scale-105 transform transition">
          <h2 className="text-2xl font-bold mb-4">Instant Creation</h2>
          <p className="opacity-80">
            Generate full videos from text prompts in seconds.
          </p>
        </div>
        <div className="bg-gray-800 p-8 rounded-2xl shadow-xl hover:scale-105 transform transition">
          <h2 className="text-2xl font-bold mb-4">Cinematic Quality</h2>
          <p className="opacity-80">
            Smooth, professional-looking videos that impress every time.
          </p>
        </div>
        <div className="bg-gray-800 p-8 rounded-2xl shadow-xl hover:scale-105 transform transition">
          <h2 className="text-2xl font-bold mb-4">Zero Setup</h2>
          <p className="opacity-80">
            No account, no downloads, just type and watch your video come alive.
          </p>
        </div>
      </section>

      <section className="flex flex-col justify-center items-center text-center py-24 px-6 md:px-32 bg-gradient-to-r from-indigo-900 via-purple-900 to-pink-900">
        <h2 className="text-4xl md:text-5xl font-bold mb-6 tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-pink-400 via-purple-400 to-indigo-400">
          Start Your Demo Experience
        </h2>
        <p className="mb-10 max-w-xl opacity-80">
          Experience our cutting-edge technology and see your ideas come to life
          instantly.
        </p>
        <button className="bg-indigo-600 hover:bg-indigo-500 transition px-10 py-5 rounded-2xl font-semibold shadow-xl text-lg">
          Launch Demo
        </button>
      </section>
    </div>
  );
}

export default Home;
