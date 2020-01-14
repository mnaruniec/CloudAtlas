package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.config.IniConfig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Config extends IniConfig {
	public static final String GENERAL_SECTION = "general";
	public static final String NAME = "name";
	public static final String DEFAULT_NAME = "/uw/violet07";
	public static final String COLLECTION_INTERVAL_MS = "collection_interval_ms";
	public static final long DEFAULT_COLLECTION_INTERVAL_MS = 3000L;

	public static final String DISK_SECTION = "disk";
	public static final String MOUNT_POINTS = "mount_points";
	public static final String DEFAULT_MOUNT_POINTS = "/";

	public static final String CPU_SECTION = "cpu";
	public static final String CPU_AVG_PERIOD_MS = "cpu_avg_period_ms";
	public static final long DEFAULT_CPU_AVG_PERIOD_MS = 3000L;
	public static final String CPU_AVG_METHOD = "cpu_avg_method";
	public static final String DEFAULT_CPU_AVG_METHOD = "arithmetic";

	public Config(File file) throws IOException {
		super(file);
	}

	public long getCollectionIntervalMs() {
		return getValue(
				GENERAL_SECTION, COLLECTION_INTERVAL_MS, Long.class, DEFAULT_COLLECTION_INTERVAL_MS
		);
	}

	public List<File> getMountPoints() {
		String val = getValue(DISK_SECTION, MOUNT_POINTS, String.class, DEFAULT_MOUNT_POINTS);
		return Arrays.stream(val.split(":")).map(File::new).collect(Collectors.toList());
	}

	public String getName() {
		return getValue(GENERAL_SECTION, NAME, String.class, DEFAULT_NAME);
	}

	public long getCPUAvgPeriodMs() {
		return getValue(CPU_SECTION, CPU_AVG_PERIOD_MS, Long.class, DEFAULT_CPU_AVG_PERIOD_MS);
	}

	public String getCPUAvgMethod() {
		return getValue(CPU_SECTION, CPU_AVG_METHOD, String.class, DEFAULT_CPU_AVG_METHOD).toLowerCase();
	}
}
