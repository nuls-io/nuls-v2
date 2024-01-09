/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.rpc.info;

import io.nuls.core.log.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

/**
 * Get LocalIPAddress, compatibleWindows、Linux
 * Get the local IP address, compatible with Windows, Linux
 *
 * @author tangyi
 * @date 2018/10/17
 */
public class HostInfo {


    /**
     * Get LocalIPaddress
     * Get local IP address
     *
     * @return String
     */
    public static String getLocalIP() {
        try {
            if (isWindowsOS()) {
                return InetAddress.getLocalHost().getHostAddress();
            } else {
                return getLinuxLocalIp();
            }
        } catch (Exception e) {
            Log.error(e);
            return "";
        }
    }


    /**
     * Determine if the operating system isWindows
     * Determine whether the operating system is Windows?
     *
     * @return boolean
     */
    private static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        String windows = "windows";
        if (osName.toLowerCase().contains(windows)) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }


    /**
     * obtainLinuxLowerIPaddress
     * Getting IP Address under Linux
     *
     * @return String
     * @throws SocketException Network disabled
     */
    private static String getLinuxLocalIp() throws SocketException {
        String ip = "";

        /*
        Traverse all physical and logical network interfaces of this machine
        Loop all the physical and logical network interfaces of the local machine
         */
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface networkInterface = en.nextElement();
            String name = networkInterface.getName();
            if (name.contains("docker") || name.contains("lo")) {
                continue;
            }

            /*
            applyNetworkInterface(network interface)Return to localip
            Return local IP using NetworkInterface
             */
            for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddress.nextElement();

                /*
                Exclude return addresses / Exclude loopback address
                 */
                if (inetAddress.isLoopbackAddress()) {
                    continue;
                }

                String ipAddress = inetAddress.getHostAddress();
                /*
                eliminateIPV6 / Exclude IPV6
                 */
                if (ipAddress.contains("::") || ipAddress.contains("0:0:") || ipAddress.contains("fe80")) {
                    continue;
                }

                /*
                eliminate127.0.0.1, return to the trueIPV4address
                Exclude 127.0.0.1 and return the true IPV4 address
                 */
                if ("127.0.0.1".equals(ip) || ipAddress.length() > 16) {
                    continue;
                }

                ip = ipAddress;
            }
        }

        return ip;
    }


    /**
     * stay10000-20000Randomly generate port numbers in, if already in use, regenerate them
     * Randomly get the port number, range from 10,000 to 20,000
     * Re-random if the port already exists
     *
     * @return Port
     */
    public static int randomPort() {
        int min = 10000;
        int max = 20000;
        Random random = new Random();
        int port = random.nextInt(max) % (max - min + 1) + min;

        if (isLocalPortUsing(port)) {
            return randomPort();
        } else {
            return port;
        }
    }


    /**
     * Verify if the port has been used locally
     * Test if the local port is being used
     *
     * @param port Port
     * @return boolean
     */
    private static boolean isLocalPortUsing(int port) {
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            Socket socket = new Socket(address, port);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
