package pl.edu.mimuw.cloudatlas.fetcher;

import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class CPULoadThread implements Runnable {
	private CentralProcessor proc;
	private long intervalMs;

	private long[][] lastTicks = null;
	private double[] load = null;


	public CPULoadThread(HardwareAbstractionLayer hal, long intervalMs) {
		this.proc = hal.getProcessor();
		this.intervalMs = intervalMs;
	}

	public double[] getLoad() {
		return load;
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (lastTicks != null) {
					load = proc.getProcessorCpuLoadBetweenTicks(lastTicks);
				}
				lastTicks = proc.getProcessorCpuLoadTicks();
				Thread.sleep(intervalMs);
			}
		} catch (InterruptedException e) {
			System.out.println("CPU load thread interrupted.");
		}
	}
}
