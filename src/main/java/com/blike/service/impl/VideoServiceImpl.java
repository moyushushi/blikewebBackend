package com.blike.service.impl;

import com.blike.entity.Follow;
import com.blike.entity.FollowUserVO;
import com.blike.entity.Video;
import com.blike.mapper.FollowMapper;
import com.blike.mapper.UserMapper;
import com.blike.mapper.VideoMapper;
import com.blike.service.VideoService;
import com.blike.utils.FFmpegUtils;
import com.blike.utils.RedisCountUtil;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserMapper userMapper;   // 注入 UserMapper

    @Resource
    private FollowMapper followMapper;

    @Resource
    private RedisCountUtil redisCountUtil;

    @Override
    public void follow(Integer followerId, Integer followingId) {
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followMapper.insert(follow);
    }


    @Value("${file.upload.path}")
    private String uploadPath;
    @Value("${file.upload.cover-path}")
    private String coverPath;
    @Value("${file.upload.video-path}")
    private String videoPath;
    @Value("${file.upload.avatar-path}")
    private String avatarPath;


    @Override
    public List<Video> getVideoList() {
        List<Video> list = videoMapper.selectAll();
        for (Video video : list) {
            try {
                long playInc = redisCountUtil.getPlayCount(video.getId());
                long likeInc = redisCountUtil.getLikeCount(video.getId());
                long commentInc = redisCountUtil.getCommentCount(video.getId());
                video.setPlayCount(video.getPlayCount() + (int) playInc);
                video.setLikeCount(video.getLikeCount() + (int) likeInc);
                video.setCommentCount(video.getCommentCount() + (int) commentInc);
            } catch (Exception e) {
                System.err.println("叠加Redis计数失败，videoId=" + video.getId() + ", 错误：" + e.getMessage());
            }
        }
        return list;
    }
    @Override
    public Video getVideoById(Integer id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            long playInc = redisCountUtil.getPlayCount(id);
            long likeInc = redisCountUtil.getLikeCount(id);
            long commentInc = redisCountUtil.getCommentCount(id);
            video.setPlayCount(video.getPlayCount() + (int) playInc);
            video.setLikeCount(video.getLikeCount() + (int) likeInc);
            video.setCommentCount(video.getCommentCount() + (int) commentInc);
        }
        return video;
    }
    @Override
    public String uploadCover(MultipartFile file) {
        return uploadFile(file, coverPath, "cover");
    }

    @Override
    public VideoUploadResult uploadVideo(MultipartFile file) {
        // 1. 保存原始文件
        String originalFileName = uploadFile(file, videoPath, "video");
        String absoluteOriginalPath = uploadPath + "video/" + extractFileName(originalFileName);

        // 2. FFmpeg 修复（移动 moov 原子）
        String tempFixedPath = absoluteOriginalPath + ".temp.mp4";
        boolean success = FFmpegUtils.fastStart(absoluteOriginalPath, tempFixedPath);
        if (success) {
            new File(absoluteOriginalPath).delete();
            new File(tempFixedPath).renameTo(new File(absoluteOriginalPath));
            System.out.println("视频修复成功：" + absoluteOriginalPath);
        } else {
            System.err.println("视频修复失败，使用原始文件");
            new File(tempFixedPath).delete();
        }

        // 3. 获取视频时长
        String duration = FFmpegUtils.getFormattedDuration(absoluteOriginalPath);
        if (duration == null || duration.isEmpty()) {
            duration = "00:00";
        }

        VideoUploadResult result = new VideoUploadResult();
        result.setUrl(originalFileName);
        result.setDuration(duration);
        return result;
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, avatarPath, "avatar");
    }

    @Override
    public Video publishVideo(MultipartFile videoFile, MultipartFile coverFile,
                              String title, String desc, String category, Integer userId) {
        // 1. 上传视频并获取时长（假设已实现）
        VideoUploadResult uploadResult = uploadVideo(videoFile);
        String videoUrl = uploadResult.getUrl();
        String duration = uploadResult.getDuration();

        // 2. 上传封面
        String coverUrl;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadCover(coverFile);
        } else {
            // 从视频中提取一帧作为封面
            String videoAbsolutePath = uploadPath + "video/" + extractFileName(videoUrl); // videoUrl 格式如 "/upload/video/uuid.mp4"
            String coverFileName = UUID.randomUUID().toString() + ".jpg";
            String coverAbsolutePath = uploadPath + "cover/" + coverFileName;
            boolean success = FFmpegUtils.extractFrame(videoAbsolutePath, coverAbsolutePath, 1.0); // 取第1秒
            if (success) {
                coverUrl = coverPath + coverFileName; // coverPath 为 "/upload/cover/"
                System.out.println("自动生成封面成功：" + coverUrl);
            } else {
                coverUrl = "/upload/cover/default.jpg";
                System.err.println("自动生成封面失败，使用默认封面");
            }
        }

        // 3. 构建 Video 对象（不设置 author 和 avatar）
        Video video = new Video();
        video.setTitle(title);
        video.setCover(coverUrl);
        video.setUrl(videoUrl);
        video.setPlayCount(0);
        video.setTime(duration);
        video.setDesc(desc);
        video.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        video.setLikeCount(0);
        video.setCommentCount(0);
        video.setCategory(category);
        video.setUserId(userId);   // 关联用户ID

        // 4. 保存到数据库
        try {
            videoMapper.insert(video);
            System.out.println("视频插入成功，ID：" + video.getId());
        } catch (Exception e) {
            System.err.println("视频插入失败：" + e.getMessage());
            e.printStackTrace();  // 打印完整堆栈
            throw new RuntimeException("数据库插入失败", e);
        }
        return video;
    }

    @Override
    public List<Video> getVideosByUserId(Integer userId) {
        List<Video> list = videoMapper.selectByUserId(userId);
        for (Video video : list) {
            try {
                long playInc = redisCountUtil.getPlayCount(video.getId());
                long likeInc = redisCountUtil.getLikeCount(video.getId());
                long commentInc = redisCountUtil.getCommentCount(video.getId());
                video.setPlayCount(video.getPlayCount() + (int) playInc);
                video.setLikeCount(video.getLikeCount() + (int) likeInc);
                video.setCommentCount(video.getCommentCount() + (int) commentInc);
            } catch (Exception e) {
                System.err.println("叠加Redis计数失败，videoId=" + video.getId() + ", 错误：" + e.getMessage());
            }
        }
        return list;
    }

    // 通用文件上传方法
    private String uploadFile(MultipartFile file, String path, String type) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + suffix;
        File saveDir = new File(uploadPath + type + "/");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        try {
            file.transferTo(new File(saveDir, fileName));
            return path + fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public void unfollow(Integer followerId, Integer followingId) {
        followMapper.delete(followerId, followingId);
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        return followMapper.exists(followerId, followingId);
    }

    @Override
    public List<FollowUserVO> getFollowingList(Integer userId) {
        return followMapper.selectFollowingByUserId(userId);
    }

    @Override
    public void incrementPlayCount(Integer videoId) {
        redisCountUtil.incrementPlayCount(videoId);
    }
}