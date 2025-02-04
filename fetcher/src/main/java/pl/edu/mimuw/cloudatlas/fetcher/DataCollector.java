package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


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

	private Mean mean;

	private CPULoadThread loadThread;
	private ExecutorService executor;

	public DataCollector(OperatingSystemMXBean bean, SystemInfo systemInfo, Config config) {
		this.bean = bean;
		this.hal = systemInfo.getHardware();
		this.os = systemInfo.getOperatingSystem();
		this.mounts = config.getMountPoints();
		this.executor = Executors.newSingleThreadExecutor();
		initializeCPULoadThread(systemInfo, config);
		setMeanObject(config);
	}

	private void setMeanObject(Config config) {
		switch (config.getCPUAvgMethod()) {
			case "geometric":
				this.mean = Mean.GEOMETRIC_MEAN;
				break;
			case "harmonic":
				this.mean = Mean.HARMONIC_MEAN;
				break;
			case "min":
				this.mean = Mean.MIN;
				break;
			case "max":
				this.mean = Mean.MAX;
				break;
			case "arithmetic":
			default:
				this.mean = Mean.ARITHMETIC_MEAN;
		}
	}

	private void initializeCPULoadThread(SystemInfo systemInfo, Config config) {
		this.loadThread = new CPULoadThread(systemInfo.getHardware(), config.getCPUAvgPeriodMs());
		executor.execute(this.loadThread);
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
			result.put("cpu_load", toValue(getCPULoad()));
			try {
				result.put("logged_users", toValue(getLoggedUsers()));
			} catch (IOException e) {
				System.out.println(
						"Failed to read logged_users value, omitting. Exception: "
								+ e.getMessage()
				);
			}
			result.put("dns_names", toValue(getDNSNames()));
		} catch (Exception e) {
			System.out.println(
					"Unexpected exception when reading values, returning the gathered ones. Exception: "
							+ e.getMessage()
			);
		}
		return result;
	}

	private Value toValue(Long val) {
		return new ValueInt(val);
	}

	private Value toValue(String val) {
		return new ValueString(val);
	}

	private Value toValue(Double val) {
		return new ValueDouble(val);
	}

	private Value toValue(Set<String> val) {
		return new ValueSet(
				val.stream().map(ValueString::new).collect(Collectors.toSet()),
				TypePrimitive.STRING
		);
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
		for (File f : mounts) {
			result += f.getFreeSpace();
		}
		return result;
	}

	public long getTotalDisk() {
		long result = 0;
		for (File f : mounts) {
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

	public Double getCPULoad() {
		double[] cpuLoad = this.loadThread.getLoad();
		return mean.eval(cpuLoad);
	}

	public Set<String> getDNSNames() {
		Set<String> result = new HashSet<>();
		NetworkIF[] nics = hal.getNetworkIFs();
		for (NetworkIF nic : nics) {
			for (String[] addrs : new String[][]{nic.getIPv6addr(), nic.getIPv4addr()}) {
				for (String addr : addrs) {
					try {
						InetAddress ia = InetAddress.getByName(addr);
						if (!ia.isLoopbackAddress()) {
							String hostname = ia.getCanonicalHostName();
							if (!hostname.equals(addr)) {
								result.add(hostname);
								if (result.size() >= 3) {
									return result;
								}
							}
						}
					} catch (Exception e) {
						// skip
					}
				}
			}
		}
		return result;
	}
}
