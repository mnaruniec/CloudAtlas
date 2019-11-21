package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCollector {
	private OperatingSystemMXBean bean;
	private List<File> mounts;

	public DataCollector(OperatingSystemMXBean bean, Config config) {
		this.bean = bean;
		this.mounts = config.getMountPoints();
	}

	public Map<String, Value> getValueMap() {
		Map<String, Value> result = new HashMap<>();
		result.put("free_ram", toValue(getFreeRam()));
		result.put("total_ram", toValue(getTotalRam()));
		result.put("free_swap", toValue(getFreeSwap()));
		result.put("total_swap", toValue(getTotalSwap()));
		result.put("free_disk", toValue(getFreeDisk()));
		result.put("total_disk", toValue(getTotalDisk()));
		result.put("kernel_ver", toValue(getKernelVer()));
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
}
