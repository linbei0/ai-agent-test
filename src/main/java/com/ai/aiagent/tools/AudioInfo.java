package com.ai.aiagent.tools;

public class AudioInfo {
    private double sampleRate;
    private int channels;
    private String format;

    // Getters and Setters
    public double getSampleRate() { return sampleRate; }
    public void setSampleRate(double sampleRate) { this.sampleRate = sampleRate; }

    public int getChannels() { return channels; }
    public void setChannels(int channels) { this.channels = channels; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    @Override
    public String toString() {
        return "Sample Rate: " + sampleRate + ", Channels: " + channels + ", Format: " + format;
    }
}

