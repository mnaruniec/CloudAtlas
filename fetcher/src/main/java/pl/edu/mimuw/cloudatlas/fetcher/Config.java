package pl.edu.mimuw.cloudatlas.fetcher;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
	public static final String GENERAL_SECTION = "general";
	public static final String NAME = "name";
	public static final String DEFAULT_NAME = "/uw/violet07";
	public static final String COLLECTION_INTERVAL_MS = "collection_interval_ms";
	public static final long DEFAULT_COLLECTION_INTERVAL_MS = 3000L;

	public static final String DISK_SECTION = "disk";
	public static final String MOUNT_POINTS = "mount_points";
	public static final String DEFAULT_MOUNT_POINTS = "/";

	private Wini ini;

	public Config(File file) throws IOException {
		this.ini = new Wini(file);
	}

	public <T> T getValue(String section, String key, Class<T> type) {
		return ini.get(section, key, type);
	}
	public <T> T getValue(String section, String key, Class<T> type, T def) {
		T res = ini.get(section, key, type);
		return res == null ? def : res;
	}

	public long getCollectionIntervalMs() {
		return getValue(
				GENERAL_SECTION, COLLECTION_INTERVAL_MS, long.class, DEFAULT_COLLECTION_INTERVAL_MS
		);
	}

	public List<File> getMountPoints() {
		String val = getValue(DISK_SECTION, MOUNT_POINTS, String.class, DEFAULT_MOUNT_POINTS);
		return Arrays.stream(val.split(":")).map(File::new).collect(Collectors.toList());
	}

	public String getName() {
		return getValue(GENERAL_SECTION, NAME, String.class, DEFAULT_NAME);
	}
}
