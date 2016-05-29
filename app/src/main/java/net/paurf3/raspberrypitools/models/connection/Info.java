package net.paurf3.raspberrypitools.models.connection;

import android.content.Context;

import com.jcraft.jsch.JSchException;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.activities.MainActivity;

import java.text.NumberFormat;

/**
 * Created by pau on 31/01/16.
 */
public class Info {

    //CPU
    private final String GET_CPU_TEMP = "cat /sys/class/thermal/thermal_zone0/temp";
    private final String GET_CPU_MAX_FREQ = "cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    private final String GET_CPU_MIN_FREQ = "cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
    private final String GET_CPU_CUR_FREQ = "cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";

    //RAM
    private final String GET_MEM_TOTAL = "cat /proc/meminfo | grep MemTotal | awk '{print $2}'"; //En KB
    private final String GET_MEM_FREE = "cat /proc/meminfo | grep MemFree | awk '{print $2}'"; //En KB

    //NETWORK
    private final String GET_ACTIVE_LOCAL_IP = "ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\\.){3}[0-9]*).*/\\2/p'";
    private final String GET_ACTIVE_NETWORK_INTERFACE = "ip route ls | sed -n 2p |awk '{print $3}'";

    //SYSTEM
    //private final String GET_UPTIME = "uptime | awk '{print $3\" \"$4;}' | sed 's/,*$//'"; <<<< DEPRECATED
    private final String GET_UPTIME = "cat /proc/uptime | awk '{print $1;}'";

    //SERVICES
    private final String CHECK_MINECRAFT_SRV_RUNNING = "netstat -anp | grep 25565";


    private String command;


    //CPU
    private String cpuTemp;
    private String cpuMaxFreq;
    private String cpuMinFreq;
    private String cpuCurFreq;

    //RAM
    private String memTotal;
    private String memFree;
    private String memUsed;

    //NETWORK
    private String activeLocalIP;
    private String activeNetworkInterface;

    //SYSTEM
    private String uptime;

    //SERVICES
    private boolean checkMinecraftSrvRunning;
    private String strCheckMinecraftSrvRunning;


    public Info(Boolean getInfo, Context context) throws JSchException, NumberFormatException, NullPointerException {

        if (getInfo) {
            //CPU
            cpuTemp = fetchCpuTemp();
            cpuMaxFreq = fetchCpuMaxFreq();
            cpuMinFreq = fetchCpuMinFreq();
            cpuCurFreq = fetchCpuCurFreq();
            //RAM
            memTotal = fetchMemTotal();
            memFree = fetchMemFree();
            memUsed = fetchMemUsed();
            //NETWORK
            activeLocalIP = fetchActiveIP();
            activeNetworkInterface = fetchActiveNetworkInterface();
            //DISK

            //SYSTEM
            uptime = fetchUpTime();

            //SERVICES
            checkMinecraftSrvRunning = checkMinecraftSrvRunning();
            if (checkMinecraftSrvRunning) {
                strCheckMinecraftSrvRunning = context.getResources().getString(R.string.service_running);
            } else {
                strCheckMinecraftSrvRunning = context.getResources().getString(R.string.service_not_running);
            }
        }

    }


    //CPU

    /**
     * Ejecuta comando para sacar la temperatura de la CPU y trata el string recibido
     *
     * @return String
     */
    public String fetchCpuTemp() throws JSchException, NumberFormatException, NullPointerException {
        double temp;
        String sTemp;
        String output = "";

        //Redondeamos al 2º decimal para tener 2 decimales
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        //Aplicamos el comando a ejecutar

        sTemp = MainActivity.ssh.execCommand(GET_CPU_TEMP);

        if (!sTemp.isEmpty()) {
            //Tratar output para que sea legible (p.def. saca: "37932")
            temp = Double.parseDouble(sTemp);
            temp = temp / 1000;


            //numberFormat.setRoundingMode(RoundingMode.DOWN); <<< Trunca en vez de redondear
            output = numberFormat.format(temp) + " ºC";


        }

        return output;

    }

    public String fetchCpuMaxFreq() throws JSchException {
        String output;

        output = MainActivity.ssh.execCommand(GET_CPU_MAX_FREQ);
        if (!output.isEmpty()) {
            output = output.substring(0, output.length() - 4);
            return output + " Mhz";
        } else {
            return output;
        }


    }

    public String fetchCpuMinFreq() throws JSchException {
        String output;

        output = MainActivity.ssh.execCommand(GET_CPU_MIN_FREQ);
        if (!output.isEmpty()) {
            output = output.substring(0, output.length() - 4);
            return output + " Mhz";
        } else {
            return output;
        }

    }

    public String fetchCpuCurFreq() throws JSchException {
        String output;

        output = MainActivity.ssh.execCommand(GET_CPU_CUR_FREQ);
        if (!output.isEmpty()) {
            output = output.substring(0, output.length() - 4);
            return output + " Mhz";
        } else {
            return output;
        }

    }

    //RAM
    public String fetchMemTotal() throws JSchException, NumberFormatException, NullPointerException {
        double memTotal;
        String sMemTotal;
        String output = "";

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        sMemTotal = MainActivity.ssh.execCommand(GET_MEM_TOTAL);
        if (!sMemTotal.isEmpty()) {
            memTotal = Double.parseDouble(sMemTotal);

            memTotal = memTotal / 1024; //De kB a MB

            output = numberFormat.format(memTotal) + " MB";
        }

        return output;

    }

    public String fetchMemFree() throws JSchException, NumberFormatException, NullPointerException {
        double memFree;
        String sMemFree;
        String output = "";

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);


        sMemFree = MainActivity.ssh.execCommand(GET_MEM_FREE);
        if (!sMemFree.isEmpty()) {
            memFree = Double.parseDouble(sMemFree);
            memFree = memFree / 1024; //De kB a MB

            output = numberFormat.format(memFree) + " MB";

        }

        return output;
    }

    public String fetchMemUsed() throws JSchException, NumberFormatException, NullPointerException {
        double memTotal;
        String sMemTotal;
        double memFree;
        String sMemFree;
        double memUsed;
        String output = "";

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        //Primero miramos la RAM total
        sMemTotal = MainActivity.ssh.execCommand(GET_MEM_TOTAL);
        //Segundo miramos la RAM libre
        sMemFree = MainActivity.ssh.execCommand(GET_MEM_FREE);

        if (!sMemTotal.isEmpty() && !sMemFree.isEmpty()) {
            memTotal = Double.parseDouble(sMemTotal);
            memFree = Double.parseDouble(sMemFree);

            //Tercero hacemos la resta y nos queda la RAM usada y la formatamos
            memUsed = memTotal - memFree;
            memUsed = memUsed / 1024; //De kB a MB

            output = numberFormat.format(memUsed) + " MB";

        }

        return output;
    }


    //NETWORK
    public String fetchActiveIP() throws JSchException {
        return MainActivity.ssh.execCommand(GET_ACTIVE_LOCAL_IP);
    }

    public String fetchActiveNetworkInterface() throws JSchException {
        return MainActivity.ssh.execCommand(GET_ACTIVE_NETWORK_INTERFACE);
    }

    //SYSTEM
    public String fetchUpTime() throws JSchException, NullPointerException {
        double uptime;
        String output;
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        output = MainActivity.ssh.execCommand(GET_UPTIME);
        if (!output.isEmpty()) {
            uptime = Double.parseDouble(output);
            uptime = uptime/3600;
            output = numberFormat.format(uptime);
        }

        return output;
    }


    /**
     * Comprueba si el servicio del servidor de Minecraft está activo mediante la comprobación
     * de escucha del puerto
     *
     * @return String
     */
    public boolean checkMinecraftSrvRunning() throws JSchException, NullPointerException {
        if (MainActivity.ssh.execCommand(CHECK_MINECRAFT_SRV_RUNNING).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }


    //CPU
    public String getCpuTemp() {
        return cpuTemp;
    }

    public void setCpuTemp(String cpuTemp) {
        this.cpuTemp = cpuTemp;
    }

    public String getCpuMaxFreq() {
        return cpuMaxFreq;
    }

    public void setCpuMaxFreq(String cpuMaxFreq) {
        this.cpuMaxFreq = cpuMaxFreq;
    }

    public String getCpuMinFreq() {
        return cpuMinFreq;
    }

    public void setCpuMinFreq(String cpuMinFreq) {
        this.cpuMinFreq = cpuMinFreq;
    }

    public String getCpuCurFreq() {
        return cpuCurFreq;
    }

    public void setCpuCurFreq(String cpuCurFreq) {
        this.cpuCurFreq = cpuCurFreq;
    }

    //RAM
    public String getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(String memTotal) {
        this.memTotal = memTotal;
    }

    public String getMemFree() {
        return memFree;
    }

    public void setMemFree(String memFree) {
        this.memFree = memFree;
    }

    public String getMemUsed() {
        return memUsed;
    }

    public void setMemUsed(String memUsed) {
        this.memUsed = memUsed;
    }

    //NETWORK

    public String getActiveNetworkInterface() {
        return activeNetworkInterface;
    }

    public void setActiveNetworkInterface(String activeNetworkInterface) {
        this.activeNetworkInterface = activeNetworkInterface;
    }

    public String getActiveLocalIP() {
        return activeLocalIP;
    }

    public void setActiveLocalIP(String activeLocalIP) {
        this.activeLocalIP = activeLocalIP;
    }


    //SYSTEM

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    //SERVICES

    public boolean isCheckMinecraftSrvRunning() {
        return checkMinecraftSrvRunning;
    }

    public void setCheckMinecraftSrvRunning(boolean checkMinecraftSrvRunning) {
        this.checkMinecraftSrvRunning = checkMinecraftSrvRunning;
    }

    public String getStrCheckMinecraftSrvRunning() {
        return strCheckMinecraftSrvRunning;
    }

    public void setStrCheckMinecraftSrvRunning(String strCheckMinecraftSrvRunning) {
        this.strCheckMinecraftSrvRunning = strCheckMinecraftSrvRunning;
    }
}
