package com.xs.charge.event;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-08 15:46
 * @email Xs.lin@foxmail.com
 */
public class NotifyEvent {

    public int status; //电池状态
    public int health; //健康
    public boolean present;
    public int level; //电池电量，数字
    public int scale ; //电池最大容量
    public int icon_small;
    public int plugged;//充电类型
    public int voltage; //电池伏数
    public int temperature; //电池温度
    public String technology ;//电池技术

    public NotifyEvent(int status, int health, boolean present, int level, int scale, int icon_small, int plugged, int voltage, int temperature, String technology) {
        this.status = status;
        this.health = health;
        this.present = present;
        this.level = level;
        this.scale = scale;
        this.icon_small = icon_small;
        this.plugged = plugged;
        this.voltage = voltage;
        this.temperature = temperature;
        this.technology = technology;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public int getIcon_small() {
        return icon_small;
    }

    public void setIcon_small(int icon_small) {
        this.icon_small = icon_small;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getPlugged() {
        return plugged;
    }

    public void setPlugged(int plugged) {
        this.plugged = plugged;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    @Override
    public String toString() {
        return "NotifyEvent{" +
                "status=" + status +
                ", health=" + health +
                ", present=" + present +
                ", level=" + level +
                ", scale=" + scale +
                ", icon_small=" + icon_small +
                ", plugged=" + plugged +
                ", voltage=" + voltage +
                ", temperature=" + temperature +
                ", technology='" + technology + '\'' +
                '}';
    }
}
