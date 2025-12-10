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
    private final MinioUploadService minioUploadService;

    public Video createVideo(String videoInstruction, User user) throws IOException {

        // Split the video into chunks
        List<ChunksApiResponseDto> videoDataChunks =
                videoChunksAndDescriptionApi.QueryVideoDescriptiveChunks(videoInstruction);

        // Prepare temporary audio directory
        Path tempAudioDir = Path.of("temporary-audio-files");
        Files.createDirectories(tempAudioDir);

        List<Path> tempAudioFiles = new ArrayList<>();

        for (ChunksApiResponseDto chunk : videoDataChunks) {

            // fetch video url
            chunk.setMediaUrl(
                    videosApi.getFirstVideo(chunk.getMediaDescription()).getMediaUrl()
            );

            // generate speech audio
            byte[] audioBytes = ttsApi.getAudio(chunk.getStoryText());

            // create unique audio file per chunk
            Path outputPath = tempAudioDir.resolve("output_" + chunk.getId() + ".mp3");
            Files.write(outputPath, audioBytes);
            chunk.setAudioUrl(outputPath.toString());
            tempAudioFiles.add(outputPath);
        }

        Video video = new Video();
        video.setCreatedDate(LocalDateTime.now());
        video.setInstructions(videoInstruction);
        video.setUser(user);

        File finalVideoFile = null;

        try {
            // create final video and get path
            String finalVideoPath = editChunksIntoVideoAndGetPath(videoDataChunks);
            finalVideoFile = new File(finalVideoPath);

            // upload to Minio
            String presignedUrl = minioUploadService.upload(finalVideoPath);
            video.setFileUrl(presignedUrl);

            // set duration
            double durationSeconds = getVideoDuration(finalVideoPath);
            video.setDuration(Duration.ofMillis((long) (durationSeconds * 1000)));

            // set size in bytes
            video.setSizeInBytes(finalVideoFile.length());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create video", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Clean up temporary audio files
            for (Path audioFile : tempAudioFiles) {
                try { Files.deleteIfExists(audioFile); } catch (IOException ignored) {}
            }
            try { Files.deleteIfExists(tempAudioDir); } catch (IOException ignored) {}

            // Clean up final video file
            if (finalVideoFile != null && finalVideoFile.exists()) {
                finalVideoFile.delete();
            }
        }

        videoRepository.save(video);
        return video;
    }

    private String editChunksIntoVideoAndGetPath(List<ChunksApiResponseDto> videoDataChunks) throws IOException, InterruptedException {

        File tempDir = new File("temp_" + UUID.randomUUID());
        if (!tempDir.mkdirs()) {
            throw new IOException("Failed to create temp directory: " + tempDir.getAbsolutePath());
        }

        File tempFinalVideo = new File(tempDir, "final_video.mp4");
        List<File> tempFiles = new ArrayList<>();
        File listFile = new File(tempDir, "file_list.txt");

        try {
            // Encode each chunk
            for (ChunksApiResponseDto chunk : videoDataChunks) {
                File tempOutputFile = new File(tempDir, "temp_pair_" + chunk.getId() + ".mp4");
                tempFiles.add(tempOutputFile);

                String command = String.format(
                        "ffmpeg -y -loglevel error -i \"%s\" -i \"%s\" -filter_complex " +
                                "\"[0:v]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,setsar=1[v];" +
                                "[1:a]aformat=sample_rates=48000:channel_layouts=stereo[a]\" " +
                                "-map \"[v]\" -map \"[a]\" " +
                                "-c:v libx264 -preset veryfast -crf 23 -r 30 -pix_fmt yuv420p " +
                                "-c:a aac -b:a 128k -shortest \"%s\"",
                        chunk.getMediaUrl(), chunk.getAudioUrl(), tempOutputFile.getAbsolutePath()
                );

                runCommand(command);
            }

            // Create list file for concatenation
            try (PrintWriter writer = new PrintWriter(listFile)) {
                for (File tempFile : tempFiles) {
                    writer.println("file '" + tempFile.getAbsolutePath() + "'");
                }
            }

            // Concatenate final video
            String concatCommand = String.format(
                    "ffmpeg -y -loglevel error -f concat -safe 0 -i \"%s\" -c copy \"%s\"",
                    listFile.getAbsolutePath(), tempFinalVideo.getAbsolutePath()
            );
            runCommand(concatCommand);

            return tempFinalVideo.getAbsolutePath();

        } finally {
            // Cleanup temp files and folder
            for (File tempFile : tempFiles) if (tempFile.exists()) tempFile.delete();
            if (listFile.exists()) listFile.delete();
            tempDir.delete();
        }
    }

    private void runCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getInputStream().transferTo(OutputStream.nullOutputStream()); // discard output
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg command failed with exit code " + exitCode);
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
            if (line != null) return Double.parseDouble(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) throw new RuntimeException("FFprobe command failed with exit code " + exitCode);

        return 0;
    }

    public VideoResponseDto convertToVideoResponseDto(Video video) {
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