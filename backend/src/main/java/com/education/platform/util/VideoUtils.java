package com.education.platform.util;

import org.springframework.stereotype.Component;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.process.ProcessLocator;

import java.io.File;

/**
 * 视频工具类
 */
@Component
public class VideoUtils {

    /**
     * ffmpeg.exe 路径
     */
    private static final String ffmpegPath = "D:\\ffmpeg\\bin\\ffmpeg.exe";

    /**
     * 自定义 ProcessLocator
     */
    private static class FixedFFmpegLocator implements ProcessLocator {
        @Override
        public String getExecutablePath() {
            return ffmpegPath;
        }
    }

    private static final ProcessLocator LOCATOR = new FixedFFmpegLocator();
    private static final Encoder ENCODER = new Encoder(LOCATOR);

    /**
     * 打印视频信息
     */
    public static void printVideoInfo(File source) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(source, LOCATOR);
            MultimediaInfo info = multimediaObject.getInfo();

            System.out.println("视频格式: " + info.getFormat());
            System.out.println("视频时长: " + info.getDuration() + " ms");
            if (info.getVideo() != null) {
                System.out.println("宽度: " + info.getVideo().getSize().getWidth());
                System.out.println("高度: " + info.getVideo().getSize().getHeight());
                System.out.println("帧率: " + info.getVideo().getFrameRate());
            }
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取视频时长
     */
    public static long getVideoDuration(File source) throws EncoderException {
        MultimediaObject multimediaObject = new MultimediaObject(source, LOCATOR);
        MultimediaInfo info = multimediaObject.getInfo();
        return info.getDuration();
    }

}