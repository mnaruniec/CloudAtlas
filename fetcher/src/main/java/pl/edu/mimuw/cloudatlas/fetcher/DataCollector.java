package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO
//		the average CPU load (over all cores) as cpu_load
//		a set of up to three DNS names of the machine dns_names
public class DataCollector {
	private static final String[] LOGGED_USERS_CMD = {
			"/bin/sh",
			"-c",
			"users | tr ' ' '\\n' | sort -u | wc -l"
	};

	private OperatingSystem os;
	private HardwareAbstractionLayer hal;
	private OperatingSystemMXBean bean;
	private List<File> mounts;

	public DataCollector(OperatingSystemMXBean bean, SystemInfo systemInfo, Config config) {
		this.bean = bean;
		this.hal = systemInfo.getHardware();
		this.os = systemInfo.getOperatingSystem();
		this.mounts = config.getMountPoints();
	}

	public Map<String, Value> getValueMap() {
		Map<String, Value> result = new HashMap<>();
		try {
			result.put("free_ram", toValue(getFreeRam()));
			result.put("total_ram", toValue(getTotalRam()));
			result.put("free_swap", toValue(getFreeSwap()));
			result.put("total_swap", toValue(getTotalSwap()));
			result.put("free_disk", toValue(getFreeDisk()));
			result.put("total_disk", toValue(getTotalDisk()));
			result.put("kernel_ver", toValue(getKernelVer()));
			result.put("num_processes", toValue(getNumProcesses()));
			result.put("num_cores", toValue(getNumCores()));
			try {
				result.put("logged_users", toValue(getLoggedUsers()));
			} catch (IOException e) {
				System.out.println(
					"Failed to read logged_users value, omitting. Exception: "
					+ e.getMessage()
				);
			}
		} catch (Exception e) {
			System.out.println(
				"Unexpected exception when reading values, returning gathered ones. Exception: "
				+ e.getMessage()
			);
		}
		return result;
	}

	private Value toValue(long val) {
		return new ValueInt(val);
	}

	private Value toValue(String val) {
		return new ValueString(val);
	}

	private Value toValue(Double val) {
		return new ValueDouble(val);
	}

	public long getFreeRam() {
		return bean.getFreePhysicalMemorySize();
	}

	public long getTotalRam() {
		return bean.getTotalPhysicalMemorySize();
	}

	public long getFreeSwap() {
		return bean.getFreeSwapSpaceSize();
	}

	public long getTotalSwap() {
		return bean.getTotalSwapSpaceSize();
	}

	public long getFreeDisk() {
		long result = 0;
		for (File f: mounts) {
			result += f.getFreeSpace();
		}
		return result;
	}

	public long getTotalDisk() {
		long result = 0;
		for (File f: mounts) {
			result += f.getTotalSpace();
		}
		return result;
	}

	public String getKernelVer() {
		return bean.getVersion();
	}

	public long getNumProcesses() {
		return os.getProcessCount();
	}

	public long getNumCores() {
		return hal.getProcessor().getLogicalProcessorCount();
	}

	public long getLoggedUsers() throws InterruptedException, IOException {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(LOGGED_USERS_CMD);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line;
			if ((line = reader.readLine()) == null) {
				throw new IOException("Did not receive expected output.");
			}
			try {
				return Long.parseLong(line);
			} catch (NumberFormatException e) {
				throw new IOException("Could not parse long.");
			}
		} finally {
			try {
				if (process != null) {
					process.waitFor();
				}
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted.");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
	}
}
