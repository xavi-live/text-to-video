package live.xavi.text_to_video.services;

import live.xavi.text_to_video.dtos.ChunksApiResponseDto;
import live.xavi.text_to_video.dtos.VideoResponseDto;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.models.Video;
import live.xavi.text_to_video.repositories.VideoRepository;
import live.xavi.text_to_video.services.externalApis.TtsApi;
import live.xavi.text_to_video.services.externalApis.VideoChunksAndDescriptionApi;
import live.xavi.text_to_video.services.externalApis.VideosApi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoChunksAndDescriptionApi videoChunksAndDescriptionApi;
    private final VideosApi videosApi;
    private final TtsApi ttsApi;

    public Video createVideo(String videoInstruction, User user) throws IOException {

        // Split the video into chunks
        List<ChunksApiResponseDto> videoDataChunks =
                videoChunksAndDescriptionApi.QueryVideoDescriptiveChunks(videoInstruction);

        for (ChunksApiResponseDto chunk : videoDataChunks) {

            // fetch video url
            chunk.setMediaUrl(
                    videosApi.getFirstVideo(chunk.getMediaDescription()).getMediaUrl()
            );

            // generate speech audio
            byte[] audioBytes = ttsApi.getAudio(chunk.getStoryText());

            // create unique audio file per chunk
            Path tempAudioDir = Path.of("temporary-audio-files");
            Files.createDirectories(tempAudioDir); // ensure folder exists

            Path outputPath = tempAudioDir.resolve("output_" + chunk.getId() + ".mp3");
            Files.write(outputPath, audioBytes);
            chunk.setAudioUrl(outputPath.toString());
        }

        Video video = new Video();
        video.setCreatedDate(LocalDateTime.now());
        video.setInstructions(videoInstruction);
        video.setUser(user);

        try {
            // create final video and get path
            String finalVideoPath = editChunksIntoVideoAndGetPath(videoDataChunks);
            video.setFileUrl(finalVideoPath);

            // set duration
            double durationSeconds = getVideoDuration(finalVideoPath);
            video.setDuration(Duration.ofMillis((long) (durationSeconds * 1000)));

            // set size in bytes
            File videoFile = new File(finalVideoPath);
            video.setSizeInBytes(videoFile.length());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create video", e);
        }

        videoRepository.save(video);

        return video;
    }

private static final File OUTPUT_DIR = new File("videos");

private String editChunksIntoVideoAndGetPath(List<ChunksApiResponseDto> videoDataChunks) throws IOException, InterruptedException {

    // Ensure output folder exists
    if (!OUTPUT_DIR.exists()) OUTPUT_DIR.mkdirs();

    // Create a unique temp folder for this video
    File tempDir = new File("temp_" + UUID.randomUUID());
    if (!tempDir.mkdirs()) {
        throw new IOException("Failed to create temp directory: " + tempDir.getAbsolutePath());
    }

    // Temp final video path
    File tempFinalVideo = new File(tempDir, "final_video.mp4");
    List<File> tempFiles = new ArrayList<>();
    File listFile = new File(tempDir, "file_list.txt");

    try {
        // 1. Encode each chunk
        for (ChunksApiResponseDto chunk : videoDataChunks) {
            File tempOutputFile = new File(tempDir, "temp_pair_" + chunk.getId() + ".mp4");
            tempFiles.add(tempOutputFile);

            String command = String.format(
                    "ffmpeg -y -i \"%s\" -i \"%s\" -filter_complex " +
                            "\"[0:v]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,setsar=1[v];" +
                            "[1:a]aformat=sample_rates=48000:channel_layouts=stereo[a]\" " +
                            "-map \"[v]\" -map \"[a]\" " +
                            "-c:v libx264 -preset veryfast -crf 23 -r 30 -pix_fmt yuv420p " +
                            "-c:a aac -b:a 128k -shortest \"%s\"",
                    chunk.getMediaUrl(), chunk.getAudioUrl(), tempOutputFile.getAbsolutePath()
            );

            System.out.println("Encoding clip " + chunk.getId() + ": " + command);
            runCommand(command);
        }

        // 2. Create list file for concatenation
        try (PrintWriter writer = new PrintWriter(listFile)) {
            for (File tempFile : tempFiles) {
                writer.println("file '" + tempFile.getAbsolutePath() + "'");
            }
        }

        // 3. Concatenate into final video
        String concatCommand = String.format(
                "ffmpeg -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"",
                listFile.getAbsolutePath(), tempFinalVideo.getAbsolutePath()
        );

        System.out.println("Concatenating final video...");
        runCommand(concatCommand);

        // 4. Move final video to permanent output folder
        File finalVideo = new File(OUTPUT_DIR, "video_" + UUID.randomUUID() + ".mp4");
        if (!tempFinalVideo.renameTo(finalVideo)) {
            throw new IOException("Failed to move final video to output folder");
        }

        return finalVideo.getAbsolutePath();

    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException("Video processing failed", e);

    } finally {
        // Cleanup temp files and folder
        for (File tempFile : tempFiles) tempFile.delete();
        if (listFile.exists()) listFile.delete();
        tempDir.delete(); // will succeed if empty
    }
}




private void runCommand(String command) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
    pb.redirectErrorStream(true);
    Process process = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) System.out.println(line);
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
        throw new RuntimeException("FFmpeg command failed with exit code " + exitCode + ": " + command);
    }
}

private double getVideoDuration(String videoPath) throws IOException, InterruptedException {
    String command = String.format(
            "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"",
            videoPath
    );

    ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
    pb.redirectErrorStream(true);
    Process process = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line = reader.readLine();
        if (line != null) {
            return Double.parseDouble(line); // duration in seconds
        }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
        throw new RuntimeException("FFprobe command failed with exit code " + exitCode);
    }

    return 0;
}

public VideoResponseDto convertToVideoResponseDto (Video video) {
    VideoResponseDto videoResponse = new VideoResponseDto();
    videoResponse.setId(video.getId());
    videoResponse.setCreatedDate(video.getCreatedDate());
    videoResponse.setDuration(video.getDuration());
    videoResponse.setFileUrl(video.getFileUrl());
    videoResponse.setInstructions(video.getInstructions());
    videoResponse.setSizeInBytes(video.getSizeInBytes());
    videoResponse.setTitle(video.getTitle());
    videoResponse.setViews(video.getViews());

    return videoResponse;
}
}
