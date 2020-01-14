package pl.edu.mimuw.cloudatlas.config;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class IniConfig {
	private Wini ini;

	public IniConfig(File file) throws IOException {
		this.ini = new Wini(file);
	}

	public <T> T getValue(String section, String key, Class<T> type) {
		return ini.get(section, key, type);
	}

	public <T> T getValueOrThrow(String section, String key, Class<T> type) {
		T res = ini.get(section, key, type);
		if (res == null) {
			throw new RuntimeException(
					"Key '" + key +
					"' from section '" + section +
					"' was not found in ini file '" + ini.getFile().getPath() + "'.");
		}
		return res;
	}

	public <T> T getValue(String section, String key, Class<T> type, T def) {
		T res = ini.get(section, key, type);
		return res == null ? def : res;
	}

}
