import { useEffect, useState } from "react";
import api from "../api/axios";
import FormInputField from "../components/FormInputField";

interface Video {
  id: string;
  title: string;
  duration: number;
  views: number;
  fileUrl: string;
  createdDate: string;
}

function VideosPage() {
  const [username, setUsername] = useState("");
  const [videoScript, setVideoScript] = useState("");
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentVideo, setCurrentVideo] = useState<Video | null>(null);

  const handleWatch = async (video: Video) => {
    setCurrentVideo(video);

    setVideos((prev) =>
      prev.map((v) => (v.id === video.id ? { ...v, views: v.views + 1 } : v)),
    );

    try {
      await api.put(`/videos/update-view-count`, { id: video.id });
    } catch (err) {
      console.error("Failed to record view:", err);
      setVideos((prev) =>
        prev.map((v) => (v.id === video.id ? { ...v, views: v.views - 1 } : v)),
      );
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get("/users/profile");
        setUsername(data.username);

        const sortedVideos = data.videos.sort(
          (a: Video, b: Video) =>
            new Date(b.createdDate).getTime() -
            new Date(a.createdDate).getTime(),
        );
        setVideos(sortedVideos);
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
    <>
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
              <h2 className="text-2xl font-bold mb-2">{video.title}...</h2>
              <p className="opacity-80 mb-1">
                Duration: {formatDuration(video.duration)}
              </p>
              <p className="opacity-80 mb-3">Views: {video.views}</p>
              <button
                className="inline-block bg-indigo-600 hover:bg-indigo-500 transition px-4 py-2 rounded-xl font-semibold"
                onClick={() => handleWatch(video)}
              >
                Watch Video
              </button>
            </div>
          ))}
        </div>
      </div>

      {currentVideo && (
        <div
          className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50"
          onClick={() => setCurrentVideo(null)}
        >
          <div
            className="bg-gray-900 rounded-xl p-4 max-w-3xl w-full relative"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="absolute top-2 right-2 text-white text-xl font-bold"
              onClick={() => setCurrentVideo(null)}
            >
              &times;
            </button>
            <video
              src={currentVideo.fileUrl}
              controls
              autoPlay
              className="w-full rounded-xl"
            />
            <h2 className="text-white text-2xl font-bold mt-4">
              {currentVideo.title}...
            </h2>
            <p className="text-gray-300">
              Duration: {formatDuration(currentVideo.duration)}
            </p>
            <p className="text-gray-300">Views: {currentVideo.views}</p>
          </div>
        </div>
      )}
    </>
  );

  function formatDuration(value: string | number) {
    if (typeof value === "number") {
      const totalSeconds = Math.floor(value);
      const minutes = Math.floor(totalSeconds / 60);
      const seconds = totalSeconds % 60;
      return `${minutes.toString().padStart(2, "0")}:${seconds
        .toString()
        .padStart(2, "0")}`;
    }

    const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?/;
    const match = (value as string).match(regex);
    if (!match) return "00:00";

    const [, h, m, s] = match;
    const totalSeconds =
      parseInt(h || "0", 10) * 3600 +
      parseInt(m || "0", 10) * 60 +
      Math.floor(parseFloat(s || "0"));

    const mm = Math.floor(totalSeconds / 60)
      .toString()
      .padStart(2, "0");
    const ss = (totalSeconds % 60).toString().padStart(2, "0");
    return `${mm}:${ss}`;
  }
}

export default VideosPage;
