package com.woltaxi.ipprotection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Security Service
 * Handles security validation, environment checks, and anti-tampering measures
 */
@Service
@Slf4j
public class SecurityService {
    
    /**
     * Check if the environment is secure
     */
    public boolean isSecureEnvironment(LicenseValidationService.ValidationContext context) {
        try {
            // Check for debugging tools
            if (isDebuggerAttached()) {
                log.warn("Debugger detected - security violation");
                return false;
            }
            
            // Check for known reverse engineering tools
            if (hasReverseEngineeringTools()) {
                log.warn("Reverse engineering tools detected");
                return false;
            }
            
            // Check system integrity
            if (!isSystemIntegrityValid()) {
                log.warn("System integrity check failed");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Security check failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if debugger is attached
     */
    private boolean isDebuggerAttached() {
        try {
            // Check JVM debug flags
            String jvmArgs = System.getProperty("java.vm.info", "");
            if (jvmArgs.contains("debug") || jvmArgs.contains("jdwp")) {
                return true;
            }
            
            // Check management bean for debugging
            java.lang.management.RuntimeMXBean runtime = 
                java.lang.management.ManagementFactory.getRuntimeMXBean();
            
            for (String arg : runtime.getInputArguments()) {
                if (arg.contains("-agentlib:jdwp") || 
                    arg.contains("-Xdebug") || 
                    arg.contains("-Xrunjdwp")) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.debug("Could not check debugger status: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check for known reverse engineering tools
     */
    private boolean hasReverseEngineeringTools() {
        try {
            // Check for known process names
            String[] suspiciousProcesses = {
                "ida.exe", "ida64.exe", "ollydbg.exe", "x32dbg.exe", "x64dbg.exe",
                "wireshark.exe", "fiddler.exe", "cheatengine.exe", "procmon.exe"
            };
            
            ProcessBuilder pb = new ProcessBuilder("tasklist");
            Process process = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String lowerLine = line.toLowerCase();
                for (String suspiciousProcess : suspiciousProcesses) {
                    if (lowerLine.contains(suspiciousProcess.toLowerCase())) {
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.debug("Could not check for reverse engineering tools: {}", e.getMessage());
            return false; // Assume secure if we can't check
        }
    }
    
    /**
     * Check system integrity
     */
    private boolean isSystemIntegrityValid() {
        try {
            // Check if running in virtual machine (potential analysis environment)
            if (isRunningInVM()) {
                log.warn("Running in virtual machine - potential analysis environment");
                return false;
            }
            
            // Check system time manipulation
            if (isSystemTimeManipulated()) {
                log.warn("System time manipulation detected");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.debug("System integrity check error: {}", e.getMessage());
            return true; // Assume valid if we can't check
        }
    }
    
    /**
     * Check if running in virtual machine
     */
    private boolean isRunningInVM() {
        try {
            // Check system properties for VM indicators
            String vendor = System.getProperty("java.vm.vendor", "").toLowerCase();
            String name = System.getProperty("java.vm.name", "").toLowerCase();
            
            if (vendor.contains("vmware") || vendor.contains("virtualbox") || 
                name.contains("vmware") || name.contains("virtualbox")) {
                return true;
            }
            
            // Check hardware info
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("windows")) {
                ProcessBuilder pb = new ProcessBuilder("wmic", "computersystem", "get", "model");
                Process process = pb.start();
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String lowerLine = line.toLowerCase();
                    if (lowerLine.contains("vmware") || lowerLine.contains("virtualbox") || 
                        lowerLine.contains("virtual")) {
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.debug("VM detection error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check for system time manipulation
     */
    private boolean isSystemTimeManipulated() {
        try {
            long systemTime = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            
            // Simple check - in a real implementation, you'd use more sophisticated methods
            // This is a basic example
            Thread.sleep(10);
            
            long systemTime2 = System.currentTimeMillis();
            long nanoTime2 = System.nanoTime();
            
            long systemDiff = systemTime2 - systemTime;
            long nanoDiff = (nanoTime2 - nanoTime) / 1_000_000; // Convert to milliseconds
            
            // If the difference is too large, time might be manipulated
            return Math.abs(systemDiff - nanoDiff) > 100;
            
        } catch (Exception e) {
            log.debug("Time manipulation check error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate secure hash for content validation
     */
    public String generateSecureHash(String content) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.error("Error generating secure hash: {}", e.getMessage(), e);
            return "";
        }
    }
}