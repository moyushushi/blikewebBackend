package com.blike.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFmpegUtils {

    /**
     * 修复视频：将 moov 原子移到文件开头（faststart）
     * @param inputPath  输入视频文件路径
     * @param outputPath 输出视频文件路径
     * @return 是否成功
     */
    public static boolean fastStart(String inputPath, String outputPath) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(inputPath);
        command.add("-c");
        command.add("copy");
        command.add("-movflags");
        command.add("+faststart");
        command.add("-y");
        command.add(outputPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取视频时长（秒）
     * @param filePath 视频文件路径
     * @return 时长（秒），失败返回 0
     */
    public static double getVideoDurationInSeconds(String filePath) {
        List<String> command = new ArrayList<>();
        command.add("ffprobe");
        command.add("-v");
        command.add("error");
        command.add("-show_entries");
        command.add("format=duration");
        command.add("-of");
        command.add("default=noprint_wrappers=1:nokey=1");
        command.add(filePath);

        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            if (line != null && !line.isEmpty()) {
                return Double.parseDouble(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取格式化后的视频时长（如 "01:23" 或 "00:01:23"）
     * @param filePath 视频文件路径
     * @return 格式化字符串，失败返回 "00:00"
     */
    public static String getFormattedDuration(String filePath) {
        double seconds = getVideoDurationInSeconds(filePath);
        if (seconds <= 0) return "00:00";
        int totalSeconds = (int) Math.round(seconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    /**
     * 从视频中提取一帧作为封面
     * @param videoPath 视频文件绝对路径
     * @param outputImagePath 输出图片绝对路径
     * @param timestamp 时间点（秒）
     * @return 是否成功
     */
    public static boolean extractFrame(String videoPath, String outputImagePath, double timestamp) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(videoPath);
        command.add("-ss");
        command.add(String.valueOf(timestamp));
        command.add("-vframes");
        command.add("1");
        command.add("-f");
        command.add("image2");
        command.add("-y");  // 覆盖已存在文件
        command.add(outputImagePath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}