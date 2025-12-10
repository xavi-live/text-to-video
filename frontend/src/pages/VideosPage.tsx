import { useEffect, useState } from "react";
import api from "../api/axios";
import FormInputField from "../components/FormInputField";

interface Video {
  id: string;
  title: string;
  duration: number;
  views: number;
  fileUrl: string;
}

function VideosPage() {
  const [username, setUsername] = useState("");
  const [videoScript, setVideoScript] = useState("");
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get("/users/profile");
        setUsername(data.username);
        setVideos(data.videos);
      } catch (err) {
        console.error(err);
      }
    })();
  }, []);

  const submit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!videoScript.trim()) return;
    setLoading(true);
    try {
      const { data } = await api.post(
        "/videos/generate",
        { instructions: videoScript },
        { withCredentials: true },
      );
      console.log(data);
      setVideoScript("");
      setVideos((prev) => [data, ...prev]);
    } catch (err) {
      console.error(err);
      alert("Failed to generate video.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white px-6 py-12">
      <h1 className="text-4xl font-extrabold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-pink-500 to-indigo-400">
        Welcome, {username || "User"}!
      </h1>

      <div className="max-w-xl mx-auto mb-12">
        <form onSubmit={submit} className="flex flex-col gap-4">
          <FormInputField
            name="Video Script"
            type="text"
            placeholder="Enter video instructions..."
            state={videoScript}
            setState={setVideoScript}
          />
          <button
            type="submit"
            disabled={loading}
            className="bg-indigo-600 hover:bg-indigo-500 transition px-6 py-4 rounded-2xl font-semibold shadow-lg text-lg"
          >
            {loading ? "Generating..." : "Generate Video"}
          </button>
        </form>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {videos.map((video) => (
          <div
            key={video.id}
            className="bg-gray-800 p-6 rounded-3xl shadow-xl hover:scale-105 transform transition"
          >
            <h2 className="text-2xl font-bold mb-2">{video.title}</h2>
            <p className="opacity-80 mb-1">Duration: {video.duration} sec</p>
            <p className="opacity-80 mb-3">Views: {video.views}</p>
            <a
              href={video.fileUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-block bg-indigo-600 hover:bg-indigo-500 transition px-4 py-2 rounded-xl font-semibold"
            >
              Watch Video
            </a>
          </div>
        ))}
      </div>
    </div>
  );
}

export default VideosPage;
